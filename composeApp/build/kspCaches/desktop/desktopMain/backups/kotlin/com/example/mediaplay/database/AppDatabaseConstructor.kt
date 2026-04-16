package com.example.mediaplay.database

import androidx.room.RoomDatabaseConstructor

public actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  override fun initialize(): AppDatabase = com.example.mediaplay.database.AppDatabase_Impl()
}
