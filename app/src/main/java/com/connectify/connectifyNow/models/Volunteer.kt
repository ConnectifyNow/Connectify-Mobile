package com.connectify.connectifyNow.models

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.connectify.connectifyNow.base.MyApplication
import com.google.firebase.firestore.FieldValue
import java.util.UUID

@Entity
data class Volunteer(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val institution: String,
    val image: String,
    val bio: String,
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

        const val ID_KEY = "id"
        const val NAME_KEY = "name"
        const val EMAIL_KEY = "email"
        const val INSTITUTION_KEY = "institution"
        const val IMAGE_KEY = "image"
        const val BIO_KEY = "bio"
        const val LAST_UPDATED = "lastUpdated"
        private const val GET_LAST_UPDATED = "get_last_updated"
    }

    val json: Map<String, Any>
        get() {
            return hashMapOf(
                ID_KEY to id,
                EMAIL_KEY to email,
                NAME_KEY to name,
                INSTITUTION_KEY to institution,
                IMAGE_KEY to image,
                BIO_KEY to bio,
                LAST_UPDATED to FieldValue.serverTimestamp()
            )
        }
}
