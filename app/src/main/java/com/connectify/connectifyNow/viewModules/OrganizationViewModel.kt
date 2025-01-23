package com.connectify.connectifyNow.viewModules

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.connectify.connectifyNow.models.Organization

class OrganizationViewModel: ViewModel() {
    var organizations: LiveData<MutableList<Organization>>? = null

    private val organizationUseCases: OrganizationUseCases = OrganizationUseCases()

    fun createUserAsOrganizationOwner(email: String, password: String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.fireStoreAuthRepository.createUser(email, password, { userId ->
            onSuccessCallBack(userId)
        }, onFailureCallBack)
    }

    fun addOrganization(organization: Organization) = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.add(organization)
    }

    fun getOrganization(organizationId: String, callback: (organization: Organization) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.getOrganization(organizationId, callback);
    }

    fun refreshOrganizations() = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.refreshOrganizations()
    }

    fun setOrganizationsOnMap(callback: (Organization) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.setOrganizationsOnMap(callback)
    }

    fun update(organization: Organization, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            organizationUseCases.update(organization, data, onSuccessCallBack, onFailureCallBack)
        } catch (e: Exception) {
            onFailureCallBack()
        }
    }
}
