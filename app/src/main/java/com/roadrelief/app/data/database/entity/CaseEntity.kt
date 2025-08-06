package com.roadrelief.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val incidentDate: Long,
    val authority: String,
    val description: String,
    val compensation: Double,
    val status: String
)
