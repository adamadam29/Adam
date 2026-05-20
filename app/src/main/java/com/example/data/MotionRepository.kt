package com.example.data

import kotlinx.coroutines.flow.Flow

class MotionRepository(private val motionDao: MotionDao) {

    val allProjects: Flow<List<ProjectEntity>> = motionDao.getAllProjects()
    val allExports: Flow<List<ExportEntity>> = motionDao.getAllExports()

    fun getProjectByIdFlow(id: Int): Flow<ProjectEntity?> = motionDao.getProjectByIdFlow(id)
    suspend fun getProjectById(id: Int): ProjectEntity? = motionDao.getProjectById(id)

    fun getLayersForProject(projectId: Int): Flow<List<LayerEntity>> =
        motionDao.getLayersByProjectFlow(projectId)

    fun getKeyframesForProject(projectId: Int): Flow<List<KeyframeEntity>> =
        motionDao.getKeyframesByProjectFlow(projectId)

    fun getKeyframesForLayer(layerId: Int): Flow<List<KeyframeEntity>> =
        motionDao.getKeyframesByLayerFlow(layerId)

    suspend fun insertProject(project: ProjectEntity): Long =
        motionDao.insertProject(project)

    suspend fun deleteProject(projectId: Int) {
        motionDao.deleteKeyframesByLayer(projectId) // Note: simple cleanup
        motionDao.deleteLayersByProject(projectId)
        motionDao.deleteProjectById(projectId)
    }

    suspend fun insertLayer(layer: LayerEntity): Long =
        motionDao.insertLayer(layer)

    suspend fun updateLayer(layer: LayerEntity) =
        motionDao.updateLayer(layer)

    suspend fun deleteLayer(layerId: Int) {
        motionDao.deleteKeyframesByLayer(layerId)
        motionDao.deleteLayerById(layerId)
    }

    suspend fun insertKeyframe(keyframe: KeyframeEntity): Long =
        motionDao.insertKeyframe(keyframe)

    suspend fun deleteKeyframe(keyframeId: Int) =
        motionDao.deleteKeyframeById(keyframeId)

    suspend fun deleteSpecificKeyframe(layerId: Int, property: String, timestampMs: Long) =
        motionDao.deleteSpecificKeyframe(layerId, property, timestampMs)

    suspend fun insertExport(export: ExportEntity): Long =
        motionDao.insertExport(export)

    suspend fun deleteExport(exportId: Int) =
        motionDao.deleteExportById(exportId)

    // Prepopulate starting projects to feel premium
    suspend fun createTemplateProject(templateName: String): Int {
        val duration = 8000L // 8 seconds
        val projectId = insertProject(
            ProjectEntity(
                name = templateName,
                width = 1080,
                height = 1080,
                durationMs = duration,
                fps = 30
            )
        ).toInt()

        when (templateName) {
            "Cinematic Title Animation" -> {
                // Background Layer (Image placeholder/dark panel)
                val bgLayerId = insertLayer(
                    LayerEntity(
                        projectId = projectId,
                        name = "Deep Space Grid",
                        type = "IMAGE",
                        zIndex = 1,
                        startMs = 0L,
                        endMs = duration,
                        content = "nebula_bg",
                        colorHex = "#0B0C10"
                    )
                ).toInt()

                // Star Shape Layer that pulses and spins!
                val shapeLayerId = insertLayer(
                    LayerEntity(
                        projectId = projectId,
                        name = "Glowing Cyber Star",
                        type = "SHAPE",
                        zIndex = 2,
                        startMs = 500L,
                        endMs = 7500L,
                        content = "star",
                        colorHex = "#00F2FE", // Electric Cyan
                        strokeWidth = 3f
                    )
                ).toInt()

                // Add keyframes for Shape: pulses back and forth, rotates full circle!
                // SCALE keyframes
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 500L, property = "SCALE", value = 0.0f))
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 2000L, property = "SCALE", value = 1.6f))
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 4000L, property = "SCALE", value = 1.0f))
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 6000L, property = "SCALE", value = 1.5f))
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 7500L, property = "SCALE", value = 0.0f))

                // ROTATION keyframes (in degrees)
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 500L, property = "ROTATION", value = 0f))
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 4000L, property = "ROTATION", value = 180f))
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 7500L, property = "ROTATION", value = 360f))

                // POSITION Y keyframes (float offsets from center: -300 to 300)
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 500L, property = "Y", value = -150f))
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 4000L, property = "Y", value = 150f))
                insertKeyframe(KeyframeEntity(layerId = shapeLayerId, timestampMs = 7500L, property = "Y", value = -150f))

                // Text Layer that fades in and out
                val textLayerId = insertLayer(
                    LayerEntity(
                        projectId = projectId,
                        name = "Title: MOTIONSTUDIO",
                        type = "TEXT",
                        zIndex = 3,
                        startMs = 1500L,
                        endMs = 7000L,
                        content = "MOTION DECK",
                        colorHex = "#FFFFFF" // White
                    )
                ).toInt()

                // Text OPACITY keyframes
                insertKeyframe(KeyframeEntity(layerId = textLayerId, timestampMs = 1500L, property = "OPACITY", value = 0.0f))
                insertKeyframe(KeyframeEntity(layerId = textLayerId, timestampMs = 2500L, property = "OPACITY", value = 1.0f))
                insertKeyframe(KeyframeEntity(layerId = textLayerId, timestampMs = 6000L, property = "OPACITY", value = 1.0f))
                insertKeyframe(KeyframeEntity(layerId = textLayerId, timestampMs = 7000L, property = "OPACITY", value = 0.0f))

                // Text Scale pop
                insertKeyframe(KeyframeEntity(layerId = textLayerId, timestampMs = 1500L, property = "SCALE", value = 0.6f))
                insertKeyframe(KeyframeEntity(layerId = textLayerId, timestampMs = 2500L, property = "SCALE", value = 1.1f))
                insertKeyframe(KeyframeEntity(layerId = textLayerId, timestampMs = 6000L, property = "SCALE", value = 1.0f))

                // Synth Soundtrack Layer
                insertLayer(
                    LayerEntity(
                        projectId = projectId,
                        name = "Midnight Synthwave",
                        type = "AUDIO",
                        zIndex = 4,
                        startMs = 0L,
                        endMs = duration,
                        content = "cyberpunk_synth.mp3",
                        volume = 0.8f
                    )
                )
            }
            "Liquid Morphing Geometry" -> {
                // Purple backdrop
                insertLayer(
                    LayerEntity(
                        projectId = projectId,
                        name = "Retro Gradient Wall",
                        type = "IMAGE",
                        zIndex = 1,
                        startMs = 0L,
                        endMs = duration,
                        content = "indigo_neon",
                        colorHex = "#161326"
                    )
                )

                // Circle morphing position
                val circleId = insertLayer(
                    LayerEntity(
                        projectId = projectId,
                        name = "Retro Sun",
                        type = "SHAPE",
                        zIndex = 2,
                        startMs = 0L,
                        endMs = duration,
                        content = "circle",
                        colorHex = "#FF2E93" // Hot Pink
                    )
                ).toInt()

                // Circle keyframes: orbits horizontally
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 0L, property = "X", value = -400f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 2000L, property = "X", value = 0f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 4000L, property = "X", value = 400f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 6000L, property = "X", value = 0f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 8000L, property = "X", value = -400f))

                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 0L, property = "Y", value = -200f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 2000L, property = "Y", value = -50f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 4000L, property = "Y", value = -200f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 6000L, property = "Y", value = -50f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 8000L, property = "Y", value = -200f))

                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 0L, property = "SCALE", value = 1.0f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 4000L, property = "SCALE", value = 2.4f))
                insertKeyframe(KeyframeEntity(layerId = circleId, timestampMs = 8000L, property = "SCALE", value = 1.0f))

                // Square in background dancing offset
                val squareId = insertLayer(
                    LayerEntity(
                        projectId = projectId,
                        name = "Sleek Matrix Square",
                        type = "SHAPE",
                        zIndex = 1,
                        startMs = 1000L,
                        endMs = 7000L,
                        content = "square",
                        colorHex = "#9D4EDD", // Electric Violet
                        strokeWidth = 4f
                    )
                ).toInt()

                insertKeyframe(KeyframeEntity(layerId = squareId, timestampMs = 1000L, property = "ROTATION", value = 45f))
                insertKeyframe(KeyframeEntity(layerId = squareId, timestampMs = 4000L, property = "ROTATION", value = 225f))
                insertKeyframe(KeyframeEntity(layerId = squareId, timestampMs = 7000L, property = "ROTATION", value = 405f))

                // Techno beat
                insertLayer(
                    LayerEntity(
                        projectId = projectId,
                        name = "Acid House Bassline",
                        type = "AUDIO",
                        zIndex = 3,
                        startMs = 0L,
                        endMs = duration,
                        content = "ambient_bass.mp3",
                        volume = 0.9f
                    )
                )
            }
            else -> {
                // Default simple Blank project pre-loaded elements
                val heartId = insertLayer(
                    LayerEntity(
                        projectId = projectId,
                        name = "Heart Beat",
                        type = "SHAPE",
                        zIndex = 1,
                        startMs = 0L,
                        endMs = duration,
                        content = "heart",
                        colorHex = "#FF3366"
                    )
                ).toInt()
                
                insertKeyframe(KeyframeEntity(layerId = heartId, timestampMs = 0L, property = "SCALE", value = 1.0f))
                insertKeyframe(KeyframeEntity(layerId = heartId, timestampMs = 1000L, property = "SCALE", value = 1.5f))
                insertKeyframe(KeyframeEntity(layerId = heartId, timestampMs = 2000L, property = "SCALE", value = 1.0f))
                insertKeyframe(KeyframeEntity(layerId = heartId, timestampMs = 3000L, property = "SCALE", value = 1.5f))
                insertKeyframe(KeyframeEntity(layerId = heartId, timestampMs = 4000L, property = "SCALE", value = 1.0f))
                insertKeyframe(KeyframeEntity(layerId = heartId, timestampMs = 5000L, property = "SCALE", value = 1.5f))
                insertKeyframe(KeyframeEntity(layerId = heartId, timestampMs = 6000L, property = "SCALE", value = 1.0f))
            }
        }

        return projectId
    }
}
