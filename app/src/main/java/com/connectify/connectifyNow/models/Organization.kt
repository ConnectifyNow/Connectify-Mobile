package com.connectify.connectifyNow.models;

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.connectify.connectifyNow.base.MyApplication
import com.firebase.geofire.core.GeoHash
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import java.util.*

@Entity
data class Organization(
    @PrimaryKey(autoGenerate = false)
    var id: String = UUID.randomUUID().toString(), // temp id
    val name: String,
    val email: String,
    val logo: String,
    val bio: String,
    val location: OrganizationLocation = OrganizationLocation("Unknown", GeoPoint(0.0, 0.0), GeoHash(0.0, 0.0)),
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
        const val LOGO_KEY = "logo"
        const val BIO_KEY = "bio"
        const val LOCATION_KEY = "location"
        const val LAST_UPDATED = "lastUpdated"
        const val GET_LAST_UPDATED = "get_last_updated"

        fun fromJSON(json: Map<String, Any>): Organization {
            val name = json[NAME_KEY] as? String ?: ""
            val email = json[EMAIL_KEY] as? String ?: ""
            val logo = json[LOGO_KEY] as? String ?: ""
            val bio = json[BIO_KEY] as? String ?: ""
            val location = json[LOCATION_KEY] as? OrganizationLocation ?: OrganizationLocation("Unknown", GeoPoint(0.0, 0.0), GeoHash(0.0, 0.0))

            val organization = Organization(
                name = name,
                email = email,
                logo = logo,
                location = location,
                bio = bio
            )

            val timestamp: Timestamp? = json[LAST_UPDATED] as? Timestamp
            timestamp?.let {
                organization.lastUpdated = it.seconds
            }

            return organization
        }
    }

    val json: Map<String, Any>
        get() {
            return hashMapOf(
                ID_KEY to id,
                NAME_KEY to name,
                EMAIL_KEY to email,
                LOGO_KEY to logo,
                BIO_KEY to bio,
                LOCATION_KEY to location,
                LAST_UPDATED to FieldValue.serverTimestamp()
            )
        }
}
