package com.connectify.connectifyNow.viewModules

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.connectify.connectifyNow.models.Organization

class OrganizationViewModel: ViewModel() {
    var companies: LiveData<MutableList<Organization>>? = null

    private val organizationUseCases: OrganizationUseCases = OrganizationUseCases()

    fun createUserAsOrganizationOwner(email: String, password: String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.fireStoreAuthRepository.createUser(email, password, { userId ->
            onSuccessCallBack(userId)
        }, onFailureCallBack)
    }

    fun addOrganization(company: Organization) = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.add(company)
    }

    fun getOrganization(companyId: String, callback: (company: Organization) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.getOrganization(companyId, callback);
    }

    fun refreshOrganizations() = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.refreshCompanies()
    }

    fun setOrganizationsOnMap(callback: (Organization) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        organizationUseCases.setCompaniesOnMap(callback)
    }

    fun update(organization: Organization, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            organizationUseCases.update(company, data, onSuccessCallBack, onFailureCallBack)
        } catch (e: Exception) {
            onFailureCallBack()
        }
    }
}
