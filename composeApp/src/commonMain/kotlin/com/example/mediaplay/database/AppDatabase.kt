package com.example.mediaplay.database

import androidx.room.*
import androidx.room.RoomDatabaseConstructor
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey val url: String,
    val title: String,
    val imageUrl: String?,
    val groupTitle: String?
)

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteItem)

    @Delete
    suspend fun delete(favorite: FavoriteItem)

    @Query("SELECT * FROM favorites")
    fun getAll(): Flow<List<FavoriteItem>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE url = :url)")
    fun isFavorite(url: String): Flow<Boolean>
}

@Entity(tableName = "playlists")
data class PlaylistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val serverUrl: String? = null,
    val username: String? = null,
    val password: String? = null,
    val lastUpdated: Long = 0
)

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistItem)

    @Delete
    suspend fun delete(playlist: PlaylistItem)

    @Query("SELECT * FROM playlists ORDER BY id DESC")
    fun getAll(): Flow<List<PlaylistItem>>
}

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val imageUrl: String? = null,
    val groupTitle: String? = null,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val playlistId: Int,
    val contentType: String = "LIVE"
)

@Dao
interface MediaItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItemEntity>)

    @Query("DELETE FROM media_items WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Int)

    @Query("""
        SELECT * FROM media_items 
        WHERE playlistId = :playlistId 
        AND contentType = :type
        AND (:category = 'Todas' OR groupTitle = :category)
        AND title LIKE '%' || :query || '%'
        ORDER BY id ASC LIMIT :limit
    """)
    fun getFiltered(playlistId: Int, type: String, category: String, query: String, limit: Int): Flow<List<MediaItemEntity>>

    @Query("SELECT DISTINCT groupTitle FROM media_items WHERE playlistId = :playlistId AND contentType = :type AND groupTitle IS NOT NULL ORDER BY groupTitle ASC")
    fun getCategories(playlistId: Int, type: String): Flow<List<String>>
}

@Database(entities = [FavoriteItem::class, PlaylistItem::class, MediaItemEntity::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun mediaItemDao(): MediaItemDao
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
