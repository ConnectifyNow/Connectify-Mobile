package com.connectify.connectifyNow.repositories.Organization

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

class FireStoreOrganizationRepository {

    companion object {
        const val ORGANIZATIONS_COLLECTION_PATH = "organizations"
    }

    val apiManager = ApiManager()

    fun getOrganization(organizationId: String, callback: (organization: Organization) -> Unit) {
        val organizationDocument = apiManager.db.collection(ORGANIZATIONS_COLLECTION_PATH)

        organizationDocument.whereEqualTo("id", organizationId).get()
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

                            val locationData = data["location"] as? Map<*, *>
                            if (locationData != null) {
                                val address = locationData["address"] as String
                                val geoPoint = locationData["location"] as GeoPoint
                                val latitude = geoPoint.latitude
                                val longitude = geoPoint.longitude

                                val organizationLocation = organizationLocation(
                                    address,
                                    geoPoint,
                                    GeoHash(latitude, longitude)
                                )

                                val organization = organization(
                                    name = data["name"] as String,
                                    email = data["email"] as String,
                                    bio = data["bio"] as String,
                                    location = organizationLocation,
                                    logo = data["logo"] as String,
                                    lastUpdated = lastUpdated ?: 0
                                )

                                callback(organization)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { locationException ->
                Log.e("Firestore", "Error getting organization: $locationException")
            }
    }

    fun getorganizations(since: Long, callback: (List<Organization>) -> Unit) {
        apiManager.db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .whereGreaterThanOrEqualTo(organization.LAST_UPDATED, Timestamp(since, 0))
            .get().addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val organizations: MutableList<Organization> = mutableListOf()
                        for (json in it.result) {
                            val organization = organization.fromJSON(json.data)
                            organizations.add(organization)
                        }
                        callback(organizations)
                    }
                    false -> callback(listOf())
                }
            }
    }

    suspend fun addorganization(organization: organization, onSuccess: (String) -> Unit): String {
        val documentReference = apiManager.db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .add(organization.json)
            .await()

        onSuccess(organization.id)
        return documentReference.id
    }

    fun setorganizationInUserTypeDB(documentReferenceId: String) = run {
        val userType = UserType(Type.organization)
        apiManager.db.collection(USER_TYPE_COLLECTION_PATH).document(documentReferenceId).set(userType)
    }


    
}