package com.example.mediaplay.database

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
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
public class FavoriteDao_Impl(
  __db: RoomDatabase,
) : FavoriteDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFavoriteItem: EntityInsertAdapter<FavoriteItem>

  private val __deleteAdapterOfFavoriteItem: EntityDeleteOrUpdateAdapter<FavoriteItem>
  init {
    this.__db = __db
    this.__insertAdapterOfFavoriteItem = object : EntityInsertAdapter<FavoriteItem>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `favorites` (`url`,`title`,`imageUrl`,`groupTitle`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FavoriteItem) {
        statement.bindText(1, entity.url)
        statement.bindText(2, entity.title)
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpImageUrl)
        }
        val _tmpGroupTitle: String? = entity.groupTitle
        if (_tmpGroupTitle == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpGroupTitle)
        }
      }
    }
    this.__deleteAdapterOfFavoriteItem = object : EntityDeleteOrUpdateAdapter<FavoriteItem>() {
      protected override fun createQuery(): String = "DELETE FROM `favorites` WHERE `url` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FavoriteItem) {
        statement.bindText(1, entity.url)
      }
    }
  }

  public override suspend fun insert(favorite: FavoriteItem): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfFavoriteItem.insert(_connection, favorite)
  }

  public override suspend fun delete(favorite: FavoriteItem): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfFavoriteItem.handle(_connection, favorite)
  }

  public override fun getAll(): Flow<List<FavoriteItem>> {
    val _sql: String = "SELECT * FROM favorites"
    return createFlow(__db, false, arrayOf("favorites")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfUrl: Int = getColumnIndexOrThrow(_stmt, "url")
        val _cursorIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _cursorIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _cursorIndexOfGroupTitle: Int = getColumnIndexOrThrow(_stmt, "groupTitle")
        val _result: MutableList<FavoriteItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: FavoriteItem
          val _tmpUrl: String
          _tmpUrl = _stmt.getText(_cursorIndexOfUrl)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_cursorIndexOfTitle)
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
          _item = FavoriteItem(_tmpUrl,_tmpTitle,_tmpImageUrl,_tmpGroupTitle)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun isFavorite(url: String): Flow<Boolean> {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM favorites WHERE url = ?)"
    return createFlow(__db, false, arrayOf("favorites")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, url)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
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
