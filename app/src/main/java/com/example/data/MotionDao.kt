package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MotionDao {

    // Projects
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    fun getProjectByIdFlow(id: Int): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Int): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Int)

    // Layers
    @Query("SELECT * FROM layers WHERE projectId = :projectId ORDER BY zIndex ASC")
    fun getLayersByProjectFlow(projectId: Int): Flow<List<LayerEntity>>

    @Query("SELECT * FROM layers WHERE projectId = :projectId ORDER BY zIndex ASC")
    suspend fun getLayersByProject(projectId: Int): List<LayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayer(layer: LayerEntity): Long

    @Update
    suspend fun updateLayer(layer: LayerEntity)

    @Query("DELETE FROM layers WHERE id = :layerId")
    suspend fun deleteLayerById(layerId: Int)

    @Query("DELETE FROM layers WHERE projectId = :projectId")
    suspend fun deleteLayersByProject(projectId: Int)

    // Keyframes
    @Query("SELECT * FROM keyframes WHERE layerId = :layerId ORDER BY timestampMs ASC")
    fun getKeyframesByLayerFlow(layerId: Int): Flow<List<KeyframeEntity>>

    @Query("SELECT * FROM keyframes WHERE layerId = :layerId ORDER BY timestampMs ASC")
    suspend fun getKeyframesByLayer(layerId: Int): List<KeyframeEntity>

    @Query("SELECT * FROM keyframes WHERE layerId IN (SELECT id FROM layers WHERE projectId = :projectId)")
    fun getKeyframesByProjectFlow(projectId: Int): Flow<List<KeyframeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyframe(keyframe: KeyframeEntity): Long

    @Query("DELETE FROM keyframes WHERE id = :keyframeId")
    suspend fun deleteKeyframeById(keyframeId: Int)

    @Query("DELETE FROM keyframes WHERE layerId = :layerId AND property = :property AND timestampMs = :timestampMs")
    suspend fun deleteSpecificKeyframe(layerId: Int, property: String, timestampMs: Long)

    @Query("DELETE FROM keyframes WHERE layerId = :layerId")
    suspend fun deleteKeyframesByLayer(layerId: Int)

    // Exports
    @Query("SELECT * FROM exports ORDER BY exportTimestamp DESC")
    fun getAllExports(): Flow<List<ExportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExport(export: ExportEntity): Long

    @Query("DELETE FROM exports WHERE id = :id")
    suspend fun deleteExportById(id: Int)
}
