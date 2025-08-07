package com.roadrelief.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.roadrelief.app.data.database.dao.CaseDao
import com.roadrelief.app.data.database.dao.EvidenceDao
import com.roadrelief.app.data.database.dao.UserDao
import com.roadrelief.app.data.database.entity.CaseEntity
import com.roadrelief.app.data.database.entity.EvidenceEntity
import com.roadrelief.app.data.database.entity.UserEntity

@Database(
    entities = [UserEntity::class, CaseEntity::class, EvidenceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun caseDao(): CaseDao
    abstract fun evidenceDao(): EvidenceDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE cases ADD COLUMN vehicleDamageDescription TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}