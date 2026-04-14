package com.example.mediaplay.database

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
public class MediaItemDao_Impl(
  __db: RoomDatabase,
) : MediaItemDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMediaItemEntity: EntityInsertAdapter<MediaItemEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMediaItemEntity = object : EntityInsertAdapter<MediaItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `media_items` (`id`,`title`,`url`,`imageUrl`,`groupTitle`,`tvgId`,`tvgName`,`playlistId`,`contentType`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MediaItemEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.url)
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpImageUrl)
        }
        val _tmpGroupTitle: String? = entity.groupTitle
        if (_tmpGroupTitle == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpGroupTitle)
        }
        val _tmpTvgId: String? = entity.tvgId
        if (_tmpTvgId == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpTvgId)
        }
        val _tmpTvgName: String? = entity.tvgName
        if (_tmpTvgName == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpTvgName)
        }
        statement.bindLong(8, entity.playlistId.toLong())
        statement.bindText(9, entity.contentType)
      }
    }
  }

  public override suspend fun insertAll(items: List<MediaItemEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMediaItemEntity.insert(_connection, items)
  }

  public override fun getFiltered(
    playlistId: Int,
    type: String,
    category: String,
    query: String,
    limit: Int,
  ): Flow<List<MediaItemEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM media_items 
        |        WHERE playlistId = ? 
        |        AND contentType = ?
        |        AND (? = 'Todas' OR groupTitle = ?)
        |        AND title LIKE '%' || ? || '%'
        |        ORDER BY id ASC LIMIT ?
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("media_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, playlistId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, type)
        _argIndex = 3
        _stmt.bindText(_argIndex, category)
        _argIndex = 4
        _stmt.bindText(_argIndex, category)
        _argIndex = 5
        _stmt.bindText(_argIndex, query)
        _argIndex = 6
        _stmt.bindLong(_argIndex, limit.toLong())
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _cursorIndexOfUrl: Int = getColumnIndexOrThrow(_stmt, "url")
        val _cursorIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _cursorIndexOfGroupTitle: Int = getColumnIndexOrThrow(_stmt, "groupTitle")
        val _cursorIndexOfTvgId: Int = getColumnIndexOrThrow(_stmt, "tvgId")
        val _cursorIndexOfTvgName: Int = getColumnIndexOrThrow(_stmt, "tvgName")
        val _cursorIndexOfPlaylistId: Int = getColumnIndexOrThrow(_stmt, "playlistId")
        val _cursorIndexOfContentType: Int = getColumnIndexOrThrow(_stmt, "contentType")
        val _result: MutableList<MediaItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MediaItemEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_cursorIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_cursorIndexOfTitle)
          val _tmpUrl: String
          _tmpUrl = _stmt.getText(_cursorIndexOfUrl)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_cursorIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_cursorIndexOfImageUrl)
          }
          val _tmpGroupTitle: String?
          if (_stmt.isNull(_cursorIndexOfGroupTitle)) {
            _tmpGroupTitle = null
          } else {
            _tmpGroupTitle = _stmt.getText(_cursorIndexOfGroupTitle)
          }
          val _tmpTvgId: String?
          if (_stmt.isNull(_cursorIndexOfTvgId)) {
            _tmpTvgId = null
          } else {
            _tmpTvgId = _stmt.getText(_cursorIndexOfTvgId)
          }
          val _tmpTvgName: String?
          if (_stmt.isNull(_cursorIndexOfTvgName)) {
            _tmpTvgName = null
          } else {
            _tmpTvgName = _stmt.getText(_cursorIndexOfTvgName)
          }
          val _tmpPlaylistId: Int
          _tmpPlaylistId = _stmt.getLong(_cursorIndexOfPlaylistId).toInt()
          val _tmpContentType: String
          _tmpContentType = _stmt.getText(_cursorIndexOfContentType)
          _item =
              MediaItemEntity(_tmpId,_tmpTitle,_tmpUrl,_tmpImageUrl,_tmpGroupTitle,_tmpTvgId,_tmpTvgName,_tmpPlaylistId,_tmpContentType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getCategories(playlistId: Int, type: String): Flow<List<String>> {
    val _sql: String =
        "SELECT DISTINCT groupTitle FROM media_items WHERE playlistId = ? AND contentType = ? AND groupTitle IS NOT NULL ORDER BY groupTitle ASC"
    return createFlow(__db, false, arrayOf("media_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, playlistId.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, type)
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteByPlaylist(playlistId: Int) {
    val _sql: String = "DELETE FROM media_items WHERE playlistId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, playlistId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
