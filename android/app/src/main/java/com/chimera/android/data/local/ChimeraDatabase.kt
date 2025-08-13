package com.chimera.android.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.chimera.android.data.local.dao.RankingDao
import com.chimera.android.data.local.entity.CachedRanking

@Database(
    entities = [CachedRanking::class],
    version = 1,
    exportSchema = false
)
abstract class ChimeraDatabase : RoomDatabase() {
    
    abstract fun rankingDao(): RankingDao
    
    companion object {
        const val DATABASE_NAME = "chimera_database"
    }
}