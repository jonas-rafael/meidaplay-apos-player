package com.example.mediaplay.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/Documents/mediaplay.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    ).setDriver(BundledSQLiteDriver())
}
