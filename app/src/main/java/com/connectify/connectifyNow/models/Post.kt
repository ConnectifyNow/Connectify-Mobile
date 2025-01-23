package com.connectify.connectifyNow.models;

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.connectify.connectifyNow.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import java.util.UUID

@Entity
data class Post(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    val ownerId: String,
    val title: String,
    val content: String,
    val imagePath: String,
    var lastUpdated: Long? = null
) {
    companion object {

        var lastUpdated: Long
            get() {
                return MyApplication.Globals
                    .appContext?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                    ?.getLong(GET_LAST_UPDATED, 0) ?: 0
            }
            set(value) {
                MyApplication.Globals
                    ?.appContext
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)?.edit()
                    ?.putLong(GET_LAST_UPDATED, value)?.apply()
            }

        const val POST_ID = "id"
        const val OWNER_ID_KEY = "ownerId"
        const val TITLE_KEY = "title"
        const val CONTENT_KEY = "content"
        const val IMAGE_PATH_KEY = "imagePath"
        const val LAST_UPDATED = "lastUpdated"
        const val GET_LAST_UPDATED = "get_last_updated"

        fun fromJSON(json: Map<String, Any>): Post {
            val ownerId = json[OWNER_ID_KEY] as? String ?: ""
            val title = json[TITLE_KEY] as? String ?: ""
            val content = json[CONTENT_KEY] as? String ?: ""
            val imagePath = json[IMAGE_PATH_KEY] as? String ?: ""

            val post = Post(
                ownerId = ownerId,
                title = title,
                content = content,
                imagePath = imagePath
            )

            post.id = json[POST_ID].toString()

            val timestamp: Timestamp? = json[LAST_UPDATED] as? Timestamp
            timestamp?.let {
                post.lastUpdated = it.seconds
            }

            return post
        }
    }

    val json: Map<String, Any>
        get() {
            return hashMapOf(
                POST_ID to id,
                OWNER_ID_KEY to ownerId,
                TITLE_KEY to title,
                CONTENT_KEY to content,
                IMAGE_PATH_KEY to imagePath,
                LAST_UPDATED to FieldValue.serverTimestamp()
            )
        }
}
