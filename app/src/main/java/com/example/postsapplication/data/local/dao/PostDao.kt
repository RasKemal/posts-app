package com.example.postsapplication.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.postsapplication.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Query(
        """
        SELECT * FROM posts
        WHERE isDeleted = 0
          AND (:query IS NULL OR title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%')
        ORDER BY id
        """,
    )
    fun pagingSource(query: String?): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM posts WHERE id = :id AND isDeleted = 0")
    fun observePost(id: Int): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPost(id: Int): PostEntity?

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int

    @Query("UPDATE posts SET isDeleted = 1 WHERE id = :id")
    suspend fun markDeleted(id: Int)

    @Query("UPDATE posts SET title = :title, body = :body, isLocallyModified = 1 WHERE id = :id")
    suspend fun updateContent(id: Int, title: String, body: String)

    /** Applies a full server snapshot without touching posts the user edited or deleted. */
    @Transaction
    suspend fun replaceWithRemote(posts: List<PostEntity>) {
        deleteMissing(posts.map { it.id })
        insertOrIgnore(posts)
        posts.forEach { updateFromRemote(it.id, it.userId, it.title, it.body) }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(posts: List<PostEntity>)

    @Query(
        """
        UPDATE posts SET userId = :userId, title = :title, body = :body
        WHERE id = :id AND isLocallyModified = 0 AND isDeleted = 0
        """,
    )
    suspend fun updateFromRemote(id: Int, userId: Int, title: String, body: String)

    @Query("DELETE FROM posts WHERE id NOT IN (:ids)")
    suspend fun deleteMissing(ids: List<Int>)
}
