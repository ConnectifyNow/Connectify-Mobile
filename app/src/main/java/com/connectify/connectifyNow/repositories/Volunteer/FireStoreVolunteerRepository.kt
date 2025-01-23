package com.connectify.connectifyNow.repositories.Volunteer

import android.util.Log
import com.connectify.connectifyNow.models.Type
import com.connectify.connectifyNow.models.UserType
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.repositories.ApiManager
import com.connectify.connectifyNow.repositories.Auth.FireStoreAuthRepository.Companion.USER_TYPE_COLLECTION_PATH
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import java.security.Timestamp

class FireStoreVolunteerRepository {
    companion object {
        const val USERS_COLLECTION_PATH = "users"
    }

    val apiManager = ApiManager()

    suspend fun addVolunteer(volunteer: Volunteer, onSuccess: (String) -> Unit): String {

        val documentReference = apiManager.db.collection(USERS_COLLECTION_PATH)
            .add(volunteer.json)
            .await()

        onSuccess(volunteer.id)
        return documentReference.id
    }

    fun getVolunteerById(volunteerId: String, callback: (volunteer: Volunteer) -> Unit) {
        val volunteerDocument = apiManager.db.collection(USERS_COLLECTION_PATH)

        volunteerDocument.whereEqualTo("id", volunteerId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.isEmpty) {
                    val data = documentSnapshot.documents[0].data;

                    if (data != null) {
                        if (data.isNotEmpty()) {
                            val timestamp: Timestamp? = data["lastUpdated"] as? Timestamp
                            var lastUpdated: Long? = null
                            timestamp?.let {
                                lastUpdated = it.seconds
                            }

                            val volunteer = Volunteer(
                                name = data["name"] as String,
                                email = data["email"] as String,
                                bio = data["bio"] as String,
                                institution = data["institution"] as String,
                                image = data["image"] as String,
                                lastUpdated = lastUpdated ?: 0
                            )

                            callback(volunteer)
                        }
                    }
                }
            }
            .addOnFailureListener { locationException ->
                Log.e("Firestore", "Error getting volunteer: $locationException")
            }
    }

    fun setVolunteerUserType(organizationId: String) = run {
        val userType = UserType(Type.VOLUNTEER)
        apiManager.db.collection(USER_TYPE_COLLECTION_PATH).document(organizationId).set(userType)
    }

    fun updateVolunteer(volunteer: Volunteer, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit){
        apiManager.db.collection(USERS_COLLECTION_PATH).whereEqualTo("id", volunteer.id).get().addOnSuccessListener {
            apiManager.db.collection(USERS_COLLECTION_PATH).document(it.documents[0].id).update(data)
                .addOnSuccessListener { onSuccessCallBack() }
                .addOnFailureListener { onFailureCallBack() }
        }
    }

    fun deleteVolunteer(volunteer: Volunteer, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit){
        apiManager.db.collection(USERS_COLLECTION_PATH).document(volunteer.id).delete()
            .addOnSuccessListener { onSuccessCallBack() }
            .addOnFailureListener { onFailureCallBack() }
    }
    
}