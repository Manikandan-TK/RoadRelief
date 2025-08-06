package com.roadrelief.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.roadrelief.app.data.database.entity.CaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Insert
    suspend fun insertCase(caseEntity: CaseEntity): Long

    @Query("SELECT * FROM cases ORDER BY incidentDate DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :caseId")
    fun getCaseById(caseId: Long): Flow<CaseEntity?>
}
