package com.example.mediaplay.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

private lateinit var applicationContext: Context

fun initDatabaseContext(context: Context) {
    applicationContext = context
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = applicationContext.getDatabasePath("mediaplay.db")
    return Room.databaseBuilder<AppDatabase>(
        context = applicationContext,
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .fallbackToDestructiveMigration(true)
}
