package com.connectify.connectifyNow.models.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.connectify.connectifyNow.models.Post

@Dao
interface PostDao {

    @Query("SELECT * FROM post")
    fun getAllPosts(): LiveData<MutableList<Post>>

    @Query("SELECT * FROM POST WHERE id=:id")
    fun getPostById(id: String): LiveData<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(post: Post)

    @Update
    fun updatePost(post: Post)

    @Delete
    fun deletePost(post: Post)

    @Query("DELETE FROM post")
    fun deleteAllPosts()
}
