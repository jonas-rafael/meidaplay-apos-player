package com.example.mediaplay.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _favoriteDao: Lazy<FavoriteDao> = lazy {
    FavoriteDao_Impl(this)
  }


  private val _playlistDao: Lazy<PlaylistDao> = lazy {
    PlaylistDao_Impl(this)
  }


  private val _mediaItemDao: Lazy<MediaItemDao> = lazy {
    MediaItemDao_Impl(this)
  }


  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(5,
        "25aabea451a611fec199d760219a4584", "489a9334dde5b983ef443dbddc570d99") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `favorites` (`url` TEXT NOT NULL, `title` TEXT NOT NULL, `imageUrl` TEXT, `groupTitle` TEXT, PRIMARY KEY(`url`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `playlists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `url` TEXT NOT NULL, `serverUrl` TEXT, `username` TEXT, `password` TEXT, `lastUpdated` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `media_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `url` TEXT NOT NULL, `imageUrl` TEXT, `groupTitle` TEXT, `tvgId` TEXT, `tvgName` TEXT, `playlistId` INTEGER NOT NULL, `contentType` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '25aabea451a611fec199d760219a4584')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `favorites`")
        connection.execSQL("DROP TABLE IF EXISTS `playlists`")
        connection.execSQL("DROP TABLE IF EXISTS `media_items`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsFavorites: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFavorites.put("url", TableInfo.Column("url", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFavorites.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFavorites.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFavorites.put("groupTitle", TableInfo.Column("groupTitle", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFavorites: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFavorites: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFavorites: TableInfo = TableInfo("favorites", _columnsFavorites,
            _foreignKeysFavorites, _indicesFavorites)
        val _existingFavorites: TableInfo = read(connection, "favorites")
        if (!_infoFavorites.equals(_existingFavorites)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |favorites(com.example.mediaplay.database.FavoriteItem).
              | Expected:
              |""".trimMargin() + _infoFavorites + """
              |
              | Found:
              |""".trimMargin() + _existingFavorites)
        }
        val _columnsPlaylists: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlaylists.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylists.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylists.put("url", TableInfo.Column("url", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylists.put("serverUrl", TableInfo.Column("serverUrl", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylists.put("username", TableInfo.Column("username", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylists.put("password", TableInfo.Column("password", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylists.put("lastUpdated", TableInfo.Column("lastUpdated", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlaylists: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlaylists: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPlaylists: TableInfo = TableInfo("playlists", _columnsPlaylists,
            _foreignKeysPlaylists, _indicesPlaylists)
        val _existingPlaylists: TableInfo = read(connection, "playlists")
        if (!_infoPlaylists.equals(_existingPlaylists)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |playlists(com.example.mediaplay.database.PlaylistItem).
              | Expected:
              |""".trimMargin() + _infoPlaylists + """
              |
              | Found:
              |""".trimMargin() + _existingPlaylists)
        }
        val _columnsMediaItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMediaItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMediaItems.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMediaItems.put("url", TableInfo.Column("url", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMediaItems.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMediaItems.put("groupTitle", TableInfo.Column("groupTitle", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMediaItems.put("tvgId", TableInfo.Column("tvgId", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMediaItems.put("tvgName", TableInfo.Column("tvgName", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMediaItems.put("playlistId", TableInfo.Column("playlistId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMediaItems.put("contentType", TableInfo.Column("contentType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMediaItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMediaItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMediaItems: TableInfo = TableInfo("media_items", _columnsMediaItems,
            _foreignKeysMediaItems, _indicesMediaItems)
        val _existingMediaItems: TableInfo = read(connection, "media_items")
        if (!_infoMediaItems.equals(_existingMediaItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |media_items(com.example.mediaplay.database.MediaItemEntity).
              | Expected:
              |""".trimMargin() + _infoMediaItems + """
              |
              | Found:
              |""".trimMargin() + _existingMediaItems)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "favorites", "playlists",
        "media_items")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(FavoriteDao::class, FavoriteDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PlaylistDao::class, PlaylistDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MediaItemDao::class, MediaItemDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun favoriteDao(): FavoriteDao = _favoriteDao.value

  public override fun playlistDao(): PlaylistDao = _playlistDao.value

  public override fun mediaItemDao(): MediaItemDao = _mediaItemDao.value
}
