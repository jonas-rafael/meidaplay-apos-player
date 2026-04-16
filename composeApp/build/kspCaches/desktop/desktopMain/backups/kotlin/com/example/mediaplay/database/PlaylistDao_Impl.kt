package com.example.mediaplay.database

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PlaylistDao_Impl(
  __db: RoomDatabase,
) : PlaylistDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPlaylistItem: EntityInsertAdapter<PlaylistItem>

  private val __deleteAdapterOfPlaylistItem: EntityDeleteOrUpdateAdapter<PlaylistItem>
  init {
    this.__db = __db
    this.__insertAdapterOfPlaylistItem = object : EntityInsertAdapter<PlaylistItem>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `playlists` (`id`,`name`,`url`,`serverUrl`,`username`,`password`,`lastUpdated`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlaylistItem) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.url)
        val _tmpServerUrl: String? = entity.serverUrl
        if (_tmpServerUrl == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpServerUrl)
        }
        val _tmpUsername: String? = entity.username
        if (_tmpUsername == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpUsername)
        }
        val _tmpPassword: String? = entity.password
        if (_tmpPassword == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpPassword)
        }
        statement.bindLong(7, entity.lastUpdated)
      }
    }
    this.__deleteAdapterOfPlaylistItem = object : EntityDeleteOrUpdateAdapter<PlaylistItem>() {
      protected override fun createQuery(): String = "DELETE FROM `playlists` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlaylistItem) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
  }

  public override suspend fun insert(playlist: PlaylistItem): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfPlaylistItem.insert(_connection, playlist)
  }

  public override suspend fun delete(playlist: PlaylistItem): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfPlaylistItem.handle(_connection, playlist)
  }

  public override fun getAll(): Flow<List<PlaylistItem>> {
    val _sql: String = "SELECT * FROM playlists ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("playlists")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _cursorIndexOfUrl: Int = getColumnIndexOrThrow(_stmt, "url")
        val _cursorIndexOfServerUrl: Int = getColumnIndexOrThrow(_stmt, "serverUrl")
        val _cursorIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _cursorIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _cursorIndexOfLastUpdated: Int = getColumnIndexOrThrow(_stmt, "lastUpdated")
        val _result: MutableList<PlaylistItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlaylistItem
          val _tmpId: Int
          _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_cursorIndexOfName)
          val _tmpUrl: String
          _tmpUrl = _stmt.getText(_cursorIndexOfUrl)
          val _tmpServerUrl: String?
          if (_stmt.isNull(_cursorIndexOfServerUrl)) {
            _tmpServerUrl = null
          } else {
            _tmpServerUrl = _stmt.getText(_cursorIndexOfServerUrl)
          }
          val _tmpUsername: String?
          if (_stmt.isNull(_cursorIndexOfUsername)) {
            _tmpUsername = null
          } else {
            _tmpUsername = _stmt.getText(_cursorIndexOfUsername)
          }
          val _tmpPassword: String?
          if (_stmt.isNull(_cursorIndexOfPassword)) {
            _tmpPassword = null
          } else {
            _tmpPassword = _stmt.getText(_cursorIndexOfPassword)
          }
          val _tmpLastUpdated: Long
          _tmpLastUpdated = _stmt.getLong(_cursorIndexOfLastUpdated)
          _item =
              PlaylistItem(_tmpId,_tmpName,_tmpUrl,_tmpServerUrl,_tmpUsername,_tmpPassword,_tmpLastUpdated)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
