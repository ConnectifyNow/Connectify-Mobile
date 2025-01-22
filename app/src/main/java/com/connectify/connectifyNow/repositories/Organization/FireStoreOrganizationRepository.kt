package com.connectify.connectifyNow.repositories.Organization

import android.util.Log
import com.connectify.connectifyNow.models.Organization
import com.connectify.connectifyNow.models.OrganizationLocation
import com.connectify.connectifyNow.models.Type
import com.connectify.connectifyNow.models.UserType
import com.connectify.connectifyNow.repositories.ApiManager
import com.connectify.connectifyNow.repositories.Auth.FireStoreAuthRepository.Companion.USER_TYPE_COLLECTION_PATH
import com.firebase.geofire.core.GeoHash
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

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

                                val organizationLocation = OrganizationLocation(
                                    address,
                                    geoPoint,
                                    GeoHash(latitude, longitude)
                                )

                                val organization = Organization(
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

    fun getOrganizations(since: Long, callback: (List<Organization>) -> Unit) {
        apiManager.db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .whereGreaterThanOrEqualTo(Organization.LAST_UPDATED, Timestamp(since, 0))
            .get().addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val organizations: MutableList<Organization> = mutableListOf()
                        for (json in it.result) {
                            val organization = Organization.fromJSON(json.data)
                            organizations.add(organization)
                        }
                        callback(organizations)
                    }
                    false -> callback(listOf())
                }
            }
    }

    suspend fun addOrganization(organization: Organization, onSuccess: (String) -> Unit): String {
        val documentReference = apiManager.db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .add(organization.json)
            .await()

        onSuccess(organization.id)
        return documentReference.id
    }

    fun setOrganizationInUserTypeDB(documentReferenceId: String) = run {
        val userType = UserType(Type.ORGANIZATION)
        apiManager.db.collection(USER_TYPE_COLLECTION_PATH).document(documentReferenceId).set(userType)
    }

    fun setOrganizationsOnMap(callback: (Organization) -> Unit) {
        val organizationsReference = apiManager.db.collection(ORGANIZATIONS_COLLECTION_PATH)
        organizationsReference.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val organizationId = document.id
                    fetchOrganizationLocation(organizationId, callback)
                }
            }
    }

    private fun fetchOrganizationLocation(organizationId: String, callback: (Organization) -> Unit) {
        val organizationDocument = apiManager.db.collection(ORGANIZATIONS_COLLECTION_PATH)
        organizationDocument.document(organizationId).get()
            .addOnSuccessListener { documentSnapshot ->
                val data = documentSnapshot.data
                if (data != null) {
                    val locationData = data["location"] as? Map<*, *>
                    if (locationData != null) {
                        val organizationLocation = buildOrganizationLocation(locationData)

                        val timestamp: Timestamp? = data["lastUpdated"] as? Timestamp
                        var lastUpdated: Long? = null
                        timestamp?.let {
                            lastUpdated = it.seconds
                        }

                        val organization = Organization(
                            name = data["name"] as String,
                            email = data["email"] as String,
                            logo = data["logo"] as String,
                            bio = data["bio"] as String,
                            location = organizationLocation,
                            lastUpdated = lastUpdated ?: 0 // Default to 0 if timestamp is null
                        )
                        callback(organization)
                    }
                }
            }
            .addOnFailureListener { locationException ->
                Log.e("Firestore", "Error getting location documents: $locationException")
            }
    }

    private fun buildOrganizationLocation(locationData: Map<*, *>?): OrganizationLocation {
        val address = locationData?.get("address") as String
        val geoPoint = locationData["location"] as GeoPoint
        val latitude = geoPoint.latitude
        val longitude = geoPoint.longitude

        return OrganizationLocation(
            address,
            geoPoint,
            GeoHash(latitude, longitude)
        )
    }

    fun updateOrganization(organization: Organization, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit){
        apiManager.db.collection(ORGANIZATIONS_COLLECTION_PATH).whereEqualTo("id", organization.id).get().addOnSuccessListener {
            apiManager.db.collection(ORGANIZATIONS_COLLECTION_PATH).document(it.documents[0].id).update(data)
                .addOnSuccessListener { onSuccessCallBack() }
                .addOnFailureListener { onFailureCallBack() }
        }
    }
    
    
}