package com.connectify.connectifyNow.viewModules

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompanyViewModel: ViewModel() {
    var companies: LiveData<MutableList<Company>>? = null

    private val companyUseCases: CompanyUseCases = CompanyUseCases()

    fun createUserAsCompanyOwner(email: String, password: String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        companyUseCases.fireStoreAuthRepository.createUser(email, password, { userId ->
            onSuccessCallBack(userId)
        }, onFailureCallBack)
    }

    fun addCompany(company: Company) = viewModelScope.launch(Dispatchers.IO) {
        companyUseCases.add(company)
    }

    fun getCompany(companyId: String, callback: (company: Company) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        companyUseCases.getCompany(companyId, callback);
    }

    fun refreshCompanies() = viewModelScope.launch(Dispatchers.IO) {
        companyUseCases.refreshCompanies()
    }

    fun setCompaniesOnMap(callback: (Company) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        companyUseCases.setCompaniesOnMap(callback)
    }

    fun update(company: Company, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            companyUseCases.update(company, data, onSuccessCallBack, onFailureCallBack)
        } catch (e: Exception) {
            onFailureCallBack()
        }
    }
}
