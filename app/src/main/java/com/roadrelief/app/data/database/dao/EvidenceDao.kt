package com.roadrelief.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.roadrelief.app.data.database.entity.EvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Insert
    suspend fun insertEvidence(evidence: EvidenceEntity)

    @Query("SELECT * FROM evidence WHERE caseId = :caseId")
    fun getEvidenceForCase(caseId: Long): Flow<List<EvidenceEntity>>
}
