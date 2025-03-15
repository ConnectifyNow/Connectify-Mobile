package com.connectify.connectifyNow.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.connectify.connectifyNow.models.Organization
import com.connectify.connectifyNow.domains.OrganizationDomain

class OrganizationViewModel: ViewModel() {
    var organizations: LiveData<MutableList<Organization>>? = null

    private val organizationDomain: OrganizationDomain = OrganizationDomain()

    fun createUserAsOrganizationOwner(email: String, password: String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        organizationDomain.fireStoreAuthRepository.createUser(email, password, { userId ->
            onSuccessCallBack(userId)
        }, onFailureCallBack)
    }

    fun addOrganization(organization: Organization) = viewModelScope.launch(Dispatchers.IO) {
        organizationDomain.addOrganization(organization)
    }

    fun getOrganization(organizationId: String, callback: (organization: Organization) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        organizationDomain.getOrganizationById(organizationId, callback);
    }

    fun refreshOrganizations() = viewModelScope.launch(Dispatchers.IO) {
        organizationDomain.refreshOrganizations()
    }

    fun setOrganizationsOnMap(callback: (Organization) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        organizationDomain.setOrganizationsOnMap(callback)
    }

    fun update(organization: Organization, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            organizationDomain.updateOrganization(organization, data, onSuccessCallBack, onFailureCallBack)
        } catch (e: Exception) {
            onFailureCallBack()
        }
    }
}
