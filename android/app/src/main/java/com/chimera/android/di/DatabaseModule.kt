package com.chimera.android.di

import android.content.Context
import androidx.room.Room
import com.chimera.android.data.local.ChimeraDatabase
import com.chimera.android.data.local.dao.RankingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideChimeraDatabase(@ApplicationContext context: Context): ChimeraDatabase {
        return Room.databaseBuilder(
            context,
            ChimeraDatabase::class.java,
            ChimeraDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideRankingDao(database: ChimeraDatabase): RankingDao {
        return database.rankingDao()
    }
}