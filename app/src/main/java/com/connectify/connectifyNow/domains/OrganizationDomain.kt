package com.connectify.connectifyNow.domains
import android.util.Log
import com.connectify.connectifyNow.models.Organization
import com.connectify.connectifyNow.repositories.Auth.FireStoreAuthRepository
import com.connectify.connectifyNow.repositories.Organization.FireStoreOrganizationRepository
import com.connectify.connectifyNow.repositories.Organization.LocalStoreOrganizationRepository
import java.util.concurrent.Executors

class OrganizationDomain {
    private val localStoreOrganizationRepository: LocalStoreOrganizationRepository = LocalStoreOrganizationRepository()
    private val fireStoreOrganizationRepository: FireStoreOrganizationRepository = FireStoreOrganizationRepository()
    val fireStoreAuthRepository: FireStoreAuthRepository = FireStoreAuthRepository()

    private var executor = Executors.newSingleThreadExecutor()

    fun getOrganizationById(id: String, callback: (organization: Organization) -> Unit) {
        fireStoreOrganizationRepository.getOrganization(id, callback);
    }

    fun refreshOrganizations() {

        val lastUpdated: Long = Organization.lastUpdated

        fireStoreOrganizationRepository.getOrganizations(lastUpdated) { organizations ->
            Log.i("TAG", "Firebase returned ${organizations.size}, lastUpdated: $lastUpdated")
            executor.execute {
                var time = lastUpdated
                for (organization in organizations) {
                    localStoreOrganizationRepository.add(organization)

                    organization.lastUpdated?.let {
                        if (time < it)
                            time = organization.lastUpdated ?: System.currentTimeMillis()
                    }
                }

                Organization.lastUpdated = time
            }
        }
    }

    suspend fun addOrganization(organization: Organization) {
        fireStoreOrganizationRepository.addOrganization(organization) { id ->
            fireStoreOrganizationRepository.setOrganizationUserType(id)
            refreshOrganizations()
        }
    }

    fun updateOrganization(organization: Organization, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) {
        fireStoreOrganizationRepository.updateOrganization(organization, data, onSuccessCallBack, onFailureCallBack)
    }

    fun setOrganizationsOnMap(callback: (Organization) -> Unit) {
        fireStoreOrganizationRepository.setOrganizationsOnMap(callback)
    }
}
