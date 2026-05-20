package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val width: Int = 1080,
    val height: Int = 1080,
    val durationMs: Long = 6000L, // 6 seconds default
    val fps: Int = 30,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "layers")
data class LayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val name: String,
    val type: String, // "TEXT", "SHAPE", "IMAGE", "AUDIO"
    val zIndex: Int,
    val startMs: Long = 0L,
    val endMs: Long = 6000L,
    val content: String = "", // text string, or shape type ("circle", "square", "star", "triangle", "heart") or image preset, or audio asset
    val colorHex: String = "#00F2FE", // default teal
    val volume: Float = 1.0f,
    val strokeWidth: Float = 0f, // 0 for fill, >0 for stroke outline
    val blendMode: String = "NORMAL" // "NORMAL", "MULTIPLY", "SCREEN", "DIFFERENCE"
)

@Entity(tableName = "keyframes")
data class KeyframeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val layerId: Int,
    val timestampMs: Long,
    val property: String, // "X", "Y", "SCALE", "ROTATION", "OPACITY"
    val value: Float
)

@Entity(tableName = "exports")
data class ExportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val projectName: String,
    val resolution: String,
    val fps: Int,
    val fileSize: String,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val thumbnailPreset: String = "circle"
)
