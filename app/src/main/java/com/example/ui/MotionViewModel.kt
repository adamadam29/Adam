package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MotionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MotionDatabase.getDatabase(application)
    private val repository = MotionRepository(database.motionDao())

    // 1. Projects and Exports flow
    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exports: StateFlow<List<ExportEntity>> = repository.allExports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Active Editing States
    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject: StateFlow<ProjectEntity?> = _activeProject.asStateFlow()

    private val _activeLayer = MutableStateFlow<LayerEntity?>(null)
    val activeLayer: StateFlow<LayerEntity?> = _activeLayer.asStateFlow()

    private val _playheadMs = MutableStateFlow(0L)
    val playheadMs: StateFlow<Long> = _playheadMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // 3. Dependent Flows for current active project
    val layers: StateFlow<List<LayerEntity>> = _activeProject
        .flatMapLatest { project ->
            if (project != null) {
                repository.getLayersForProject(project.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val keyframes: StateFlow<List<KeyframeEntity>> = _activeProject
        .flatMapLatest { project ->
            if (project != null) {
                repository.getKeyframesForProject(project.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. Navigation/UI states
    private val _currentScreen = MutableStateFlow("DASHBOARD") // "DASHBOARD", "EDITOR", "EXPORTS_GALLERY"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // 5. Exporter States
    private val _exportProgress = MutableStateFlow<Float?>(null) // null = idle, 0f-1f = exporting
    val exportProgress: StateFlow<Float?> = _exportProgress.asStateFlow()

    private val _exportStatusText = MutableStateFlow("")
    val exportStatusText: StateFlow<String> = _exportStatusText.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Pre-populate with cinematic templates if database is empty on first load
        viewModelScope.launch {
            repository.allProjects.first().let { currentList ->
                if (currentList.isEmpty()) {
                    repository.createTemplateProject("Cinematic Title Animation")
                    repository.createTemplateProject("Liquid Morphing Geometry")
                }
            }
        }
    }

    // --- SCREEN NAVIGATION ---
    fun navigateToDashboard() {
        stopPlayback()
        _currentScreen.value = "DASHBOARD"
        _activeProject.value = null
        _activeLayer.value = null
    }

    fun navigateToExports() {
        stopPlayback()
        _currentScreen.value = "EXPORTS_GALLERY"
        _activeProject.value = null
        _activeLayer.value = null
    }

    fun selectProject(project: ProjectEntity) {
        _activeProject.value = project
        _playheadMs.value = 0L
        _activeLayer.value = null
        _currentScreen.value = "EDITOR"
    }

    // --- PLAYBACK CONTROLLER ---
    fun togglePlayback() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        val project = _activeProject.value ?: return
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (_isPlaying.value) {
                delay(16) // ~60fps updates
                val now = System.currentTimeMillis()
                val delta = now - lastTime
                lastTime = now

                val nextPlayhead = _playheadMs.value + delta
                if (nextPlayhead >= project.durationMs) {
                    _playheadMs.value = 0L
                } else {
                    _playheadMs.value = nextPlayhead
                }
            }
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun seekTo(timeMs: Long) {
        val project = _activeProject.value ?: return
        _playheadMs.value = timeMs.coerceIn(0L, project.durationMs)
    }

    // --- THE KEYFRAME INTERPOLATOR ENGINE ---
    // Calculates what the property value should be at the current playheadMs
    fun getInterpolatedValue(
        layerId: Int,
        property: String,
        defaultValue: Float
    ): Float {
        val currentPlayhead = _playheadMs.value
        val allKfs = keyframes.value.filter { it.layerId == layerId && it.property == property }
        if (allKfs.isEmpty()) return defaultValue
        if (allKfs.size == 1) return allKfs[0].value

        val sorted = allKfs.sortedBy { it.timestampMs }
        val before = sorted.findLast { it.timestampMs <= currentPlayhead }
        val after = sorted.find { it.timestampMs >= currentPlayhead }

        if (before == null) return after?.value ?: defaultValue
        if (after == null) return before.value

        if (before.timestampMs == after.timestampMs) return before.value

        // Interpolate lineally
        val diff = (after.timestampMs - before.timestampMs).toFloat()
        if (diff == 0f) return before.value

        val progress = (currentPlayhead - before.timestampMs).toFloat() / diff
        // apply easing or linear
        return before.value + (after.value - before.value) * progress
    }

    // Check if keyframe exists at EXACT matching playhead
    fun hasKeyframeAtCurrentFrame(layerId: Int, property: String): Boolean {
        val currentHead = _playheadMs.value
        // Match keyframe within +/- 80ms window to cover loose scrubbing
        return keyframes.value.any {
            it.layerId == layerId &&
                    it.property == property &&
                    Math.abs(it.timestampMs - currentHead) < 100L
        }
    }

    // --- KEYFRAME ACTIONS ---
    fun setKeyframeForActiveLayer(property: String, value: Float) {
        val layer = _activeLayer.value ?: return
        val currentHead = _playheadMs.value
        viewModelScope.launch {
            // Delete keyframe in window if exists to overwrite
            repository.deleteSpecificKeyframe(layer.id, property, currentHead)
            
            // Insert new keyframe
            repository.insertKeyframe(
                KeyframeEntity(
                    layerId = layer.id,
                    timestampMs = currentHead,
                    property = property,
                    value = value
                )
            )
        }
    }

    fun removeKeyframeForActiveLayer(property: String) {
        val layer = _activeLayer.value ?: return
        val currentHead = _playheadMs.value
        viewModelScope.launch {
            val toDelete = keyframes.value.filter {
                it.layerId == layer.id &&
                        it.property == property &&
                        Math.abs(it.timestampMs - currentHead) < 100L
            }
            toDelete.forEach { kf ->
                repository.deleteKeyframe(kf.id)
            }
        }
    }

    // --- PROJECT CUSTOMIZATIONS ---
    fun createNewProject(name: String, ratio: String) {
        viewModelScope.launch {
            val (width, height) = when (ratio) {
                "VERTICAL_9_16" -> Pair(1080, 1920)
                "LANDSCAPE_16_9" -> Pair(1920, 1080)
                else -> Pair(1080, 1080) // 1:1 SQUARE
            }
            val projectId = repository.insertProject(
                ProjectEntity(
                    name = name,
                    width = width,
                    height = height,
                    durationMs = 6000L,
                    fps = 30
                )
            ).toInt()

            // Insert matching default Canvas backdrop layer
            repository.insertLayer(
                LayerEntity(
                    projectId = projectId,
                    name = "Grid Canvas",
                    type = "IMAGE",
                    zIndex = 0,
                    startMs = 0L,
                    endMs = 6000L,
                    content = "grid_board",
                    colorHex = "#101216"
                )
            )

            // Auto-select and navigate to editor
            val newProj = repository.getProjectById(projectId)
            if (newProj != null) {
                selectProject(newProj)
            }
        }
    }

    fun updateProjectDuration(newDurationMs: Long) {
        val proj = _activeProject.value ?: return
        val updated = proj.copy(durationMs = newDurationMs)
        _activeProject.value = updated
        viewModelScope.launch {
            repository.insertProject(updated)
        }
    }

    fun deleteProject(projectId: Int) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }

    // --- LAYER CRUD OPERATIONS ---
    fun selectLayer(layer: LayerEntity?) {
        _activeLayer.value = layer
    }

    fun addTextLayer(text: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val maxZ = layers.value.maxOfOrNull { it.zIndex } ?: 0
            val layerId = repository.insertLayer(
                LayerEntity(
                    projectId = proj.id,
                    name = "Text: \"$text\"",
                    type = "TEXT",
                    zIndex = maxZ + 1,
                    startMs = 0L,
                    endMs = proj.durationMs,
                    content = text,
                    colorHex = "#FFFFFF"
                )
            ).toInt()

            // Insert initial default positioning keyframe to center
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "X", value = 0f))
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "Y", value = 0f))
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "SCALE", value = 1f))
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "ROTATION", value = 0f))
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "OPACITY", value = 1f))
        }
    }

    fun addShapeLayer(shapeType: String) {
        val proj = _activeProject.value ?: return
        val colorHex = when (shapeType) {
            "circle" -> "#FF2E93" // pink
            "star" -> "#00F2FE" // cyan
            "square" -> "#FFB703" // amber
            "heart" -> "#FF3366" // red
            else -> "#9D4EDD" // purple
        }
        viewModelScope.launch {
            val maxZ = layers.value.maxOfOrNull { it.zIndex } ?: 0
            val layerId = repository.insertLayer(
                LayerEntity(
                    projectId = proj.id,
                    name = "${shapeType.uppercase()} Node",
                    type = "SHAPE",
                    zIndex = maxZ + 1,
                    startMs = 0L,
                    endMs = proj.durationMs,
                    content = shapeType,
                    colorHex = colorHex
                )
            ).toInt()

            // Insert base position keyframes
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "X", value = 0f))
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "Y", value = 0f))
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "SCALE", value = 1f))
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "ROTATION", value = 0f))
            repository.insertKeyframe(KeyframeEntity(layerId = layerId, timestampMs = 0L, property = "OPACITY", value = 1f))
        }
    }

    fun addAudioLayer(audioTrackName: String) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val maxZ = layers.value.maxOfOrNull { it.zIndex } ?: 0
            repository.insertLayer(
                LayerEntity(
                    projectId = proj.id,
                    name = "Audio: $audioTrackName",
                    type = "AUDIO",
                    zIndex = maxZ + 1,
                    startMs = 0L,
                    endMs = proj.durationMs,
                    content = audioTrackName,
                    volume = 1.0f
                )
            )
        }
    }

    fun duplicateLayer(layer: LayerEntity) {
        viewModelScope.launch {
            val maxZ = layers.value.maxOfOrNull { it.zIndex } ?: 0
            val newLayerId = repository.insertLayer(
                layer.copy(id = 0, zIndex = maxZ + 1, name = "${layer.name} Copy")
            ).toInt()

            // Duplicate all matching keyframes
            val existingKfs = keyframes.value.filter { it.layerId == layer.id }
            existingKfs.forEach { kf ->
                repository.insertKeyframe(kf.copy(id = 0, layerId = newLayerId))
            }
        }
    }

    fun updateLayerDetails(layer: LayerEntity) {
        viewModelScope.launch {
            repository.updateLayer(layer)
            // Keep active layer object synced
            if (_activeLayer.value?.id == layer.id) {
                _activeLayer.value = layer
            }
        }
    }

    fun deleteLayerFromProject(layer: LayerEntity) {
        viewModelScope.launch {
            repository.deleteLayer(layer.id)
            if (_activeLayer.value?.id == layer.id) {
                _activeLayer.value = null
            }
        }
    }

    // --- VIDEO EXPORTER ENGINES ---
    fun runProjectExportSimulated(resolution: String, exportFps: Int) {
        val project = _activeProject.value ?: return
        _exportProgress.value = 0f
        _exportStatusText.value = "Initializing graphics engine..."
        stopPlayback()

        viewModelScope.launch {
            val renderPhrases = listOf(
                "Loading vector frames...",
                "Rasterizing Compose canvas vectors...",
                "Compiling translation timelines kinects...",
                "Baking keyframed motion matrices...",
                "Blending color matrices & shading shaders...",
                "Applying linear easing interpolations...",
                "Rendering H.264 video streams...",
                "Synthesizing audio wave packets...",
                "Multiplexing audio and video timeline...",
                "Wrapping MP4 container structures...",
                "Finalizing file headers..."
            )

            val totalSteps = 40
            for (step in 1..totalSteps) {
                delay(120) // Simulated work
                val progress = step.toFloat() / totalSteps.toFloat()
                _exportProgress.value = progress
                
                // Select a status phrase
                val phraseIndex = ((progress * (renderPhrases.size - 1))).toInt()
                _exportStatusText.value = "${renderPhrases[phraseIndex]} (${(progress * 100).toInt()}%)"
            }

            // Saving finished simulation export
            val randomSize = String.format("%.1f MB", 3.0f + (Math.random() * 8.5f))
            val thumbnailPreset = layers.value.find { it.type == "SHAPE" }?.content ?: "star"
            repository.insertExport(
                ExportEntity(
                    projectId = project.id,
                    projectName = project.name,
                    resolution = resolution,
                    fps = exportFps,
                    fileSize = randomSize,
                    thumbnailPreset = thumbnailPreset
                )
            )

            _exportProgress.value = 1f
            _exportStatusText.value = "Export successful! Video saved to studio folder."
            delay(1200)
            _exportProgress.value = null // reset dialog
            navigateToExports()
        }
    }

    fun deleteExportFile(export: ExportEntity) {
        viewModelScope.launch {
            repository.deleteExport(export.id)
        }
    }
}
