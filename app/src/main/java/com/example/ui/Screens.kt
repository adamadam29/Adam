package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AppContent(viewModel: MotionViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                "DASHBOARD" -> DashboardScreen(viewModel)
                "EDITOR" -> WorkspaceEditorScreen(viewModel)
                "EXPORTS_GALLERY" -> ExportsGalleryScreen(viewModel)
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN (Project Management)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MotionViewModel) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(ElectricCyan, ChromaPink)))
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text(
                                text = "M",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "MOTION STUDIO",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateToExports() },
                        modifier = Modifier.testTag("exports_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home, // Replace with Home
                            contentDescription = "Exports Folder",
                            tint = ElectricCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepObsidian
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = ChromaPink,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("create_project_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Project", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepObsidian)
        ) {
            // Welcome banner using Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(LightSlate, DeepObsidian)
                        )
                    )
                    .border(1.dp, LightGreyBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Create Kinetic Motion Graphics",
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Design multi-layer timelines, interpolate custom keyframe motions, and render exports in professional frame resolutions.",
                        color = MutedGrey,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "YOUR PROJECTS",
                color = NeutralWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .wrapContentSize(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow, // Replace
                            contentDescription = "Empty Projects",
                            tint = DarkGreyMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No projects found",
                            color = NeutralWhite,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create your first cinematic timeline keyframe project by tapping the floating button below.",
                            color = MutedGrey,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(projects) { project ->
                        ProjectItemCard(
                            project = project,
                            onSelect = { viewModel.selectProject(project) },
                            onDelete = { viewModel.deleteProject(project.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, ratio ->
                viewModel.createNewProject(name, ratio)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    // Replaced Card with dynamic Box to avoid BOM class clash issues
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightSlate)
            .border(1.dp, LightGreyBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .testTag("project_card_${project.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Simulated preview thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F111A))
                    .drawBehind {
                        drawCircle(
                            color = ElectricCyan.copy(alpha = 0.15f),
                            radius = size.width / 3f,
                            center = Offset(size.width * 0.2f, size.height * 0.3f)
                        )
                        drawCircle(
                            color = ChromaPink.copy(alpha = 0.15f),
                            radius = size.width / 4f,
                            center = Offset(size.width * 0.8f, size.height * 0.7f)
                        )
                        val step = size.height / 5f
                        for (i in 1..4) {
                            drawLine(
                                color = LightGreyBorder.copy(alpha = 0.1f),
                                start = Offset(0f, i * step),
                                end = Offset(size.width, i * step),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star, // Replace with standard Star
                    contentDescription = null,
                    tint = ElectricCyan.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                )

                // Ratio Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    val ratioText = when {
                        project.width > project.height -> "16:9"
                        project.width < project.height -> "9:16"
                        else -> "1:1"
                    }
                    Text(
                        text = ratioText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.name,
                color = NeutralWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Duration: ${(project.durationMs / 1000f)}s | ${project.fps} FPS",
                color = MutedGrey,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_project_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Project",
                        tint = DarkGreyMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateProjectDialog(onDismiss: () -> Unit, onCreate: (name: String, ratio: String) -> Unit) {
    var name by remember { mutableStateOf("My Motion Scene") }
    var selectedRatio by remember { mutableStateOf("SQUARE_1_1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Project settings",
                color = NeutralWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NeutralWhite,
                        unfocusedTextColor = NeutralWhite,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = LightGreyBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CANVAS ASPECT RATIO",
                    color = MutedGrey,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RatioOptionItem(
                        title = "1:1 Square",
                        ratio = "SQUARE_1_1",
                        icon = Icons.Default.Check, // standard Checklist check
                        selected = selectedRatio == "SQUARE_1_1",
                        onClick = { selectedRatio = "SQUARE_1_1" }
                    )
                    RatioOptionItem(
                        title = "9:16 Vertical",
                        ratio = "VERTICAL_9_16",
                        icon = Icons.Default.Favorite, // standard Heart/Favorite
                        selected = selectedRatio == "VERTICAL_9_16",
                        onClick = { selectedRatio = "VERTICAL_9_16" }
                    )
                    RatioOptionItem(
                        title = "16:9 Cinema",
                        ratio = "LANDSCAPE_16_9",
                        icon = Icons.Default.Build, // standard build
                        selected = selectedRatio == "LANDSCAPE_16_9",
                        onClick = { selectedRatio = "LANDSCAPE_16_9" }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, selectedRatio) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_create_project_button")
            ) {
                Text("Create Timeline", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedGrey)
            }
        },
        containerColor = LightSlate
    )
}

@Composable
fun RowScope.RatioOptionItem(
    title: String,
    ratio: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) ElectricCyan.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                1.dp,
                if (selected) ElectricCyan else LightGreyBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) ElectricCyan else MutedGrey,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (selected) NeutralWhite else MutedGrey,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// 2. TIMELINE GRAPHICS WORKSPACE (THE EDITOR)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceEditorScreen(viewModel: MotionViewModel) {
    val project by viewModel.activeProject.collectAsStateWithLifecycle()
    val layers by viewModel.layers.collectAsStateWithLifecycle()
    val keyframes by viewModel.keyframes.collectAsStateWithLifecycle()
    val activeLayer by viewModel.activeLayer.collectAsStateWithLifecycle()
    val playheadMs by viewModel.playheadMs.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val exportStatusText by viewModel.exportStatusText.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("PROPERTIES") }
    var showExportDialog by remember { mutableStateOf(false) }

    if (project == null) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project!!.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeutralWhite
                        )
                        Text(
                            text = "Canvas: ${project!!.width}x${project!!.height}px",
                            fontSize = 10.sp,
                            color = MutedGrey
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateToDashboard() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeutralWhite)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_project_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, // standard Share icon
                            contentDescription = "Export Scene",
                            tint = ElectricCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightSlate
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepObsidian)
        ) {
            WorkspaceInteractiveCanvas(
                project = project!!,
                layers = layers,
                activeLayer = activeLayer,
                playheadMs = playheadMs,
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
                    .background(Color(0xFF07080A))
            )

            HorizontalDivider(color = LightGreyBorder)

            PlaybackTransportRow(
                playheadMs = playheadMs,
                durationMs = project!!.durationMs,
                isPlaying = isPlaying,
                onTogglePlay = { viewModel.togglePlayback() },
                onRewind = { viewModel.seekTo(0L) },
                onSeek = { viewModel.seekTo(it) }
            )

            HorizontalDivider(color = LightGreyBorder)

            TimelineTracksPanel(
                durationMs = project!!.durationMs,
                playheadMs = playheadMs,
                layers = layers,
                keyframes = keyframes,
                activeLayer = activeLayer,
                onSelectLayer = { viewModel.selectLayer(it) },
                onSeek = { viewModel.seekTo(it) },
                onUpdateLayer = { viewModel.updateLayerDetails(it) },
                onDeleteLayer = { viewModel.deleteLayerFromProject(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(LightSlate)
            )

            HorizontalDivider(color = LightGreyBorder)

            BottomWorkspaceControlsPanel(
                activeTab = activeTab,
                onSelectTab = { activeTab = it },
                activeLayer = activeLayer,
                playheadMs = playheadMs,
                viewModel = viewModel,
                layers = layers,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .background(DeepObsidian)
            )
        }
    }

    if (showExportDialog) {
        ExportStudioDialog(
            projectName = project!!.name,
            onDismiss = { showExportDialog = false },
            onConfirmExport = { resolution, fps ->
                showExportDialog = false
                viewModel.runProjectExportSimulated(resolution, fps)
            }
        )
    }

    if (exportProgress != null) {
        Dialog(onDismissRequest = {}) {
            // Replaced Card with Box to avoid BOM mismatch issues
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightSlate)
                    .border(1.dp, LightGreyBorder, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        progress = exportProgress!!,
                        color = ElectricCyan,
                        strokeWidth = 5.dp,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Rendering Kinetic Scene",
                        fontWeight = FontWeight.Bold,
                        color = NeutralWhite,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = exportStatusText,
                        color = MutedGrey,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Interactive Stage
@Composable
fun WorkspaceInteractiveCanvas(
    project: ProjectEntity,
    layers: List<LayerEntity>,
    activeLayer: LayerEntity?,
    playheadMs: Long,
    viewModel: MotionViewModel,
    modifier: androidx.compose.ui.Modifier // Specified explicitly to avoid resolution type issues
) {
    BoxWithConstraints(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }

        val ratioW = containerWidthPx / project.width.toFloat()
        val ratioH = containerHeightPx / project.height.toFloat()
        val scale = Math.min(ratioW, ratioH) * 0.9f

        val canvasW = (project.width * scale / with(density) { density.density }).dp
        val canvasH = (project.height * scale / with(density) { density.density }).dp

        Box(
            modifier = Modifier
                .size(canvasW, canvasH)
                .background(Color.Black)
                .border(1.dp, Color.White.copy(alpha = 0.15f))
                .clipToBounds()
        ) {
            layers.forEach { layer ->
                if (playheadMs >= layer.startMs && playheadMs <= layer.endMs) {
                    val kfX = viewModel.getInterpolatedValue(layer.id, "X", 0f)
                    val kfY = viewModel.getInterpolatedValue(layer.id, "Y", 0f)
                    val kfScale = viewModel.getInterpolatedValue(layer.id, "SCALE", 1f)
                    val kfRotation = viewModel.getInterpolatedValue(layer.id, "ROTATION", 0f)
                    val kfOpacity = viewModel.getInterpolatedValue(layer.id, "OPACITY", 1f)

                    val isSelected = activeLayer?.id == layer.id

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = kfX
                                translationY = kfY
                                scaleX = kfScale
                                scaleY = kfScale
                                rotationZ = kfRotation
                                alpha = kfOpacity
                            }
                            .pointerInput(layer.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (isSelected) {
                                        viewModel.setKeyframeForActiveLayer("X", kfX + dragAmount.x)
                                        viewModel.setKeyframeForActiveLayer("Y", kfY + dragAmount.y)
                                    } else {
                                        viewModel.selectLayer(layer)
                                    }
                                }
                            }
                            .clickable(enabled = !isSelected) { viewModel.selectLayer(layer) },
                        contentAlignment = Alignment.Center
                    ) {
                        RenderLayerContent(layer = layer)

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .border(
                                        2.dp,
                                        Brush.sweepGradient(listOf(ElectricCyan, ChromaPink, ElectricCyan)),
                                        RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(ElectricCyan).align(Alignment.TopStart))
                                Box(modifier = Modifier.size(8.dp).background(ElectricCyan).align(Alignment.TopEnd))
                                Box(modifier = Modifier.size(8.dp).background(ElectricCyan).align(Alignment.BottomStart))
                                Box(modifier = Modifier.size(8.dp).background(ElectricCyan).align(Alignment.BottomEnd))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Render context helper
@Composable
fun RenderLayerContent(layer: LayerEntity) {
    val color = remember(layer.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(layer.colorHex))
        } catch (e: Exception) {
            Color(0xFF00F2FE)
        }
    }

    when (layer.type) {
        "TEXT" -> {
            Text(
                text = layer.content,
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
        "SHAPE" -> {
            Canvas(modifier = Modifier.size(100.dp)) {
                when (layer.content) {
                    "circle" -> {
                        if (layer.strokeWidth > 0f) {
                            drawCircle(
                                color = color,
                                radius = size.width / 2.2f,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(layer.strokeWidth * density)
                            )
                        } else {
                            drawCircle(color = color, radius = size.width / 2.2f)
                        }
                    }
                    "square" -> {
                        val side = size.width * 0.8f
                        val offset = (size.width - side) / 2f
                        if (layer.strokeWidth > 0f) {
                            drawRect(
                                color = color,
                                topLeft = Offset(offset, offset),
                                size = androidx.compose.ui.geometry.Size(side, side),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(layer.strokeWidth * density)
                            )
                        } else {
                            drawRect(
                                color = color,
                                topLeft = Offset(offset, offset),
                                size = androidx.compose.ui.geometry.Size(side, side)
                            )
                        }
                    }
                    "triangle" -> {
                        val path = Path().apply {
                            moveTo(size.width / 2f, size.height * 0.1f)
                            lineTo(size.width * 0.9f, size.height * 0.9f)
                            lineTo(size.width * 0.1f, size.height * 0.9f)
                            close()
                        }
                        if (layer.strokeWidth > 0f) {
                            drawPath(
                                path = path,
                                color = color,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(layer.strokeWidth * density)
                            )
                        } else {
                            drawPath(path = path, color = color)
                        }
                    }
                    "star" -> {
                        val path = Path().apply {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val outerRadius = size.width * 0.48f
                            val innerRadius = outerRadius * 0.4f
                            var angle = -Math.PI / 2f
                            val increment = Math.PI / 5f
                            for (i in 0 until 10) {
                                val r = if (i % 2 == 0) outerRadius else innerRadius
                                val x = centerX + r * cos(angle).toFloat()
                                val y = centerY + r * sin(angle).toFloat()
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                                angle += increment
                            }
                            close()
                        }
                        if (layer.strokeWidth > 0f) {
                            drawPath(
                                path = path,
                                color = color,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(layer.strokeWidth * density)
                            )
                        } else {
                            drawPath(path = path, color = color)
                        }
                    }
                    "heart" -> {
                        val path = Path().apply {
                            val w = size.width
                            val h = size.height
                            moveTo(w / 2f, h * 0.3f)
                            cubicTo(w * 0.15f, h * 0.05f, 0f, h * 0.38f, w / 2f, h * 0.88f)
                            cubicTo(w, h * 0.38f, w * 0.85f, h * 0.05f, w / 2f, h * 0.3f)
                        }
                        if (layer.strokeWidth > 0f) {
                            drawPath(
                                path = path,
                                color = color,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(layer.strokeWidth * density)
                            )
                        } else {
                            drawPath(path = path, color = color)
                        }
                    }
                }
            }
        }
        "IMAGE" -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                when (layer.content) {
                    "grid_board" -> {
                        val verticalLines = (size.width / 40.dp.toPx()).toInt()
                        val horizontalLines = (size.height / 40.dp.toPx()).toInt()

                        for (i in 0..verticalLines) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(i * 40.dp.toPx(), 0f),
                                end = Offset(i * 40.dp.toPx(), size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (i in 0..horizontalLines) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, i * 40.dp.toPx()),
                                end = Offset(size.width, i * 40.dp.toPx()),
                                strokeWidth = 1f
                            )
                        }
                    }
                    "nebula_bg" -> {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(ChromaPink.copy(alpha = 0.3f), Color.Transparent),
                                center = Offset(size.width * 0.3f, size.height * 0.3f),
                                radius = size.width * 0.8f
                            )
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(ElectricCyan.copy(alpha = 0.3f), Color.Transparent),
                                center = Offset(size.width * 0.8f, size.height * 0.8f),
                                radius = size.width * 0.8f
                            )
                        )
                    }
                    "indigo_neon" -> {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF16113A), Color(0xFF040209)),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )
                        )
                        drawLine(
                            color = FluidPurple.copy(alpha = 0.3f),
                            start = Offset(0f, size.height * 0.6f),
                            end = Offset(size.width, size.height * 0.9f),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                    else -> {
                        drawRect(color = color.copy(alpha = 0.1f))
                    }
                }
            }
        }
        "AUDIO" -> {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add, // Standard core symbol
                    contentDescription = null,
                    tint = ElectricCyan.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// Playback seeking controls
@Composable
fun PlaybackTransportRow(
    playheadMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onRewind: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onRewind,
            modifier = Modifier.testTag("rewind_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Rewind", tint = MutedGrey)
        }

        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier.testTag("play_pause_button")
        ) {
            Text(
                text = if (isPlaying) "⏸" else "▶",
                fontSize = 28.sp,
                color = if (isPlaying) ChromaPink else ElectricCyan
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatTime(playheadMs),
            color = NeutralWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(54.dp)
        )

        Slider(
            value = playheadMs.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..durationMs.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = ChromaPink,
                activeTrackColor = ElectricCyan,
                inactiveTrackColor = DarkGreyMuted
            ),
            modifier = Modifier
                .weight(1f)
                .testTag("playhead_slider")
        )

        Text(
            text = formatTime(durationMs),
            color = MutedGrey,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(54.dp),
            textAlign = TextAlign.End
        )
    }
}

private fun formatTime(ms: Long): String {
    val sec = ms / 1000
    val dec = (ms % 1000) / 10
    return String.format("%02d.%02d", sec, dec)
}

// Timeline Channels Panel
@Composable
fun TimelineTracksPanel(
    durationMs: Long,
    playheadMs: Long,
    layers: List<LayerEntity>,
    keyframes: List<KeyframeEntity>,
    activeLayer: LayerEntity?,
    onSelectLayer: (LayerEntity) -> Unit,
    onSeek: (Long) -> Unit,
    onUpdateLayer: (LayerEntity) -> Unit,
    onDeleteLayer: (LayerEntity) -> Unit,
    modifier: androidx.compose.ui.Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MULTIFLOW TIMELINE TRACKS",
                color = MutedGrey,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (activeLayer != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Active: \"${activeLayer.name}\"",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onDeleteLayer(activeLayer) },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_timeline_layer")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete layer", tint = ChromaPink, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(layers) { layer ->
                val isSelected = activeLayer?.id == layer.id
                val layerKfs = keyframes.filter { it.layerId == layer.id }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DarkGreyContainer else Color.Black.copy(alpha = 0.2f))
                        .border(
                            1.dp,
                            if (isSelected) ElectricCyan.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectLayer(layer) }
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (layer.type) {
                                    "TEXT" -> Icons.Default.Info
                                    "SHAPE" -> Icons.Default.Build
                                    "IMAGE" -> Icons.Default.Home
                                    else -> Icons.Default.Add
                                },
                                contentDescription = null,
                                tint = if (isSelected) ElectricCyan else MutedGrey,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = layer.name,
                                color = if (isSelected) NeutralWhite else MutedGrey,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 140.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Trim: ${(layer.startMs / 1000f)}s to ${(layer.endMs / 1000f)}s",
                                color = DarkGreyMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        val startPct = layer.startMs.toFloat() / durationMs.toFloat()
                        val endPct = layer.endMs.toFloat() / durationMs.toFloat()
                        val spanWidth = (endPct - startPct).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(spanWidth)
                                .align(Alignment.CenterStart)
                                .offset(x = (startPct * 280).dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            ElectricCyan.copy(alpha = 0.6f),
                                            ChromaPink.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )

                        layerKfs.distinctBy { it.timestampMs }.forEach { kf ->
                            val kfPct = kf.timestampMs.toFloat() / durationMs.toFloat()
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .offset(x = (kfPct * 270).dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(KeyframeDiamondBlue)
                            )
                        }

                        val playheadPct = playheadMs.toFloat() / durationMs.toFloat()
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = (playheadPct * 270).dp)
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(Color.White)
                        )
                    }
                }
            }
        }
    }
}

// PROPERTIES / ELEMENTS TABBED LAYOUT
@Composable
fun BottomWorkspaceControlsPanel(
    activeTab: String,
    onSelectTab: (String) -> Unit,
    activeLayer: LayerEntity?,
    playheadMs: Long,
    layers: List<LayerEntity>,
    viewModel: MotionViewModel,
    modifier: androidx.compose.ui.Modifier
) {
    var selectedTabState by remember { mutableStateOf(activeTab) }

    Column(modifier = modifier) {
        // Simple and robust Tab switcher representing properties and addition screens
        TabRow(
            selectedTabIndex = if (selectedTabState == "PROPERTIES") 0 else 1,
            containerColor = LightSlate,
            contentColor = ElectricCyan
        ) {
            Tab(
                selected = selectedTabState == "PROPERTIES",
                onClick = { selectedTabState = "PROPERTIES" },
                modifier = Modifier.testTag("tab_properties")
            ) {
                Text(
                    text = "PROPERTIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (selectedTabState == "PROPERTIES") NeutralWhite else MutedGrey
                )
            }
            Tab(
                selected = selectedTabState == "ADD_ELEMENTS",
                onClick = { selectedTabState = "ADD_ELEMENTS" },
                modifier = Modifier.testTag("tab_add_elements")
            ) {
                Text(
                    text = "ADD ELEMENTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = if (selectedTabState == "ADD_ELEMENTS") NeutralWhite else MutedGrey
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DeepObsidian)
        ) {
            if (selectedTabState == "PROPERTIES") {
                if (activeLayer == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No selected layers\nTap a component on the canvas or timeline track.",
                            color = MutedGrey,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    PropertiesControlSuite(
                        layer = activeLayer,
                        playheadMs = playheadMs,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                AddNodesControlSuite(
                    onAddText = { viewModel.addTextLayer(it) },
                    onAddShape = { viewModel.addShapeLayer(it) },
                    onAddAudio = { viewModel.addAudioLayer(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Suite parameters
@Composable
fun PropertiesControlSuite(
    layer: LayerEntity,
    playheadMs: Long,
    viewModel: MotionViewModel,
    modifier: androidx.compose.ui.Modifier
) {
    var selectedProperty by remember { mutableStateOf("SCALE") }
    val propertyOptions = listOf("SCALE", "X", "Y", "ROTATION", "OPACITY")

    val rangeMin = when (selectedProperty) {
        "SCALE" -> 0.0f
        "X" -> -600.0f
        "Y" -> -600.0f
        "ROTATION" -> 0.0f
        else -> 0.0f
    }
    val rangeMax = when (selectedProperty) {
        "SCALE" -> 3.5f
        "X" -> 600.0f
        "Y" -> 600.0f
        "ROTATION" -> 360.0f
        else -> 1.0f
    }

    val interpolatedVal = viewModel.getInterpolatedValue(
        layerId = layer.id,
        property = selectedProperty,
        defaultValue = when (selectedProperty) {
            "SCALE" -> 1.0f
            "OPACITY" -> 1.0f
            else -> 0.0f
        }
    )

    val keyframeMarkerExists = viewModel.hasKeyframeAtCurrentFrame(layer.id, selectedProperty)

    LazyColumn(
        modifier = modifier.padding(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MOTION KINEMATICS",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            if (keyframeMarkerExists) {
                                viewModel.removeKeyframeForActiveLayer(selectedProperty)
                            } else {
                                viewModel.setKeyframeForActiveLayer(selectedProperty, interpolatedVal)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (keyframeMarkerExists) ChromaPink else ElectricCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("keyframe_action_button")
                    ) {
                        Text(
                            text = if (keyframeMarkerExists) "Delete Keyframe -💎" else "Insert Keyframe +💎",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                propertyOptions.forEach { property ->
                    val isPropSel = selectedProperty == property
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPropSel) ElectricCyan.copy(alpha = 0.15f) else LightSlate)
                            .border(
                                1.dp,
                                if (isPropSel) ElectricCyan else LightGreyBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedProperty = property }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = property,
                                color = if (isPropSel) NeutralWhite else MutedGrey,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val kfCount = viewModel.keyframes.value.filter { it.layerId == layer.id && it.property == property }.size
                            if (kfCount > 0) {
                                Text(
                                    text = "💎 x$kfCount",
                                    color = ElectricCyan,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // Replaced Card with Box to avoid BOM class differences
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightSlate)
                    .border(1.dp, LightGreyBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Interpolated Value:",
                            color = MutedGrey,
                            fontSize = 11.sp
                        )
                        Text(
                            text = String.format("%.2f", interpolatedVal),
                            color = ElectricCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = interpolatedVal.coerceIn(rangeMin, rangeMax),
                        onValueChange = { newValue ->
                            viewModel.setKeyframeForActiveLayer(selectedProperty, newValue)
                        },
                        valueRange = rangeMin..rangeMax,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = DarkGreyMuted
                        ),
                        modifier = Modifier.testTag("property_adjuster_slider")
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "STYLES & MODIFIERS",
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightSlate)
                    .border(1.dp, LightGreyBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = layer.name,
                        onValueChange = { viewModel.updateLayerDetails(layer.copy(name = it)) },
                        label = { Text("Layer Label", fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NeutralWhite,
                            unfocusedTextColor = NeutralWhite,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = LightGreyBorder
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("layer_label_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (layer.type == "TEXT") {
                        OutlinedTextField(
                            value = layer.content,
                            onValueChange = { viewModel.updateLayerDetails(layer.copy(content = it)) },
                            label = { Text("Text content", fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = NeutralWhite,
                                unfocusedTextColor = NeutralWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = LightGreyBorder
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("layer_content_text_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = "Layer Tint / Color",
                        color = MutedGrey,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val colorPresets = listOf("#00F2FE", "#FFFF2E93", "#9D4EDD", "#FFFFB703", "#FFFFFF", "#FF3366")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorPresets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(preset)))
                                    .border(
                                        2.dp,
                                        if (layer.colorHex.equals(preset, ignoreCase = true)) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable {
                                        viewModel.updateLayerDetails(layer.copy(colorHex = preset))
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.duplicateLayer(layer) },
                        colors = ButtonDefaults.buttonColors(containerColor = FluidPurple),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("duplicate_layer_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Duplicate Layer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Add element forms
@Composable
fun AddNodesControlSuite(
    onAddText: (String) -> Unit,
    onAddShape: (String) -> Unit,
    onAddAudio: (String) -> Unit,
    modifier: androidx.compose.ui.Modifier
) {
    var textInput by remember { mutableStateOf("KINETIC DECK") }

    LazyColumn(
        modifier = modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "ADD TEXT LAYER",
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NeutralWhite,
                        unfocusedTextColor = NeutralWhite,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = LightGreyBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_text_input_field")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onAddText(textInput)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp).testTag("add_text_layer_button")
                ) {
                    Text("Add Text", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        item {
            Text(
                text = "ADD GEOMETRIC SHAPES (KEYFRAMEABLE)",
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            val shapes = listOf("star", "circle", "square", "triangle", "heart")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                shapes.forEach { shape ->
                    Button(
                        onClick = { onAddShape(shape) },
                        colors = ButtonDefaults.buttonColors(containerColor = LightSlate),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LightGreyBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("add_shape_${shape}"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = shape.uppercase(),
                            fontSize = 9.sp,
                            color = NeutralWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "ADD SOUNDTRACK TRACK (SIMULATED)",
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            val audioTracks = listOf("Midnight Synthwave", "Acid House Bassline", "Lo-Fi Lullaby", "Cyber Industrial")
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                audioTracks.forEach { audio ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightSlate)
                            .clickable { onAddAudio(audio) }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = audio, color = NeutralWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.Add, contentDescription = "Add Track", tint = MutedGrey, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExportStudioDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirmExport: (resolution: String, fps: Int) -> Unit
) {
    var selectedRes by remember { mutableStateOf("1080p Full HD") }
    val resOptions = listOf("720p HD", "1080p Full HD", "4K Ultra HD")

    var selectedFps by remember { mutableStateOf(30) }
    val fpsOptions = listOf(24, 30, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export Render Studio",
                color = NeutralWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Configure file encoding parameters for rendering \"$projectName\".",
                    color = MutedGrey,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "RESOLUTION PRESETS",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    resOptions.forEach { res ->
                        val isSel = selectedRes == res
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) ElectricCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (isSel) ElectricCyan else LightGreyBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedRes = res }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = res,
                                color = if (isSel) NeutralWhite else MutedGrey,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FRAMERATE SPEED",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    fpsOptions.forEach { fps ->
                        val isSel = selectedFps == fps
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) ElectricCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (isSel) ElectricCyan else LightGreyBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedFps = fps }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${fps} FPS",
                                color = if (isSel) NeutralWhite else MutedGrey,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmExport(selectedRes, selectedFps) },
                colors = ButtonDefaults.buttonColors(containerColor = ChromaPink, contentColor = Color.White),
                modifier = Modifier.testTag("confirm_export_studio_button")
            ) {
                Text("Start Render", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedGrey)
            }
        },
        containerColor = LightSlate
    )
}

// ==========================================
// 3. EXPORTS RENDER GALLERY SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportsGalleryScreen(viewModel: MotionViewModel) {
    val exports by viewModel.exports.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home, // standard
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "STUDIO EXPORTS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateToDashboard() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Dashboard", tint = NeutralWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightSlate
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepObsidian)
        ) {
            if (exports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // standard
                            contentDescription = null,
                            tint = DarkGreyMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No exports in studio",
                            color = NeutralWhite,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Launch is empty. Export kinetic videos from your editor workspace to compile actual MP4 media streams.",
                            color = MutedGrey,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(exports) { export ->
                        ExportCardItem(
                            export = export,
                            onDelete = { viewModel.deleteExportFile(export) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportCardItem(export: ExportEntity, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightSlate)
            .border(1.dp, LightGreyBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("export_card_${export.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0D0F16)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build, // Standard symbol
                    contentDescription = null,
                    tint = ChromaPink,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = export.projectName,
                    color = NeutralWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${export.resolution} | ${export.fps}fps | ${export.fileSize}",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                val date = remember(export.exportTimestamp) {
                    val format = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
                    format.format(java.util.Date(export.exportTimestamp))
                }
                Text(
                    text = "Rendered: $date",
                    color = MutedGrey,
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_export_button_${export.id}")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete export", tint = DarkGreyMuted)
            }
        }
    }
}
