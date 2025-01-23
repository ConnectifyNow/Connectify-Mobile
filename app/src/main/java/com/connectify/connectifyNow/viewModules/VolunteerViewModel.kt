package com.connectify.connectifyNow.viewModules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VolunteerViewModel: ViewModel() {
    val volunteerUseCases: VolunteerUseCases = VolunteerUseCases()

    fun createUserAsVolunteer(email: String, password: String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        volunteerUseCases.fireStoreAuthRepository.createUser(email, password, { userId ->
            onSuccessCallBack(userId)
        }, onFailureCallBack)
    }

    fun getAllVolunteers() = viewModelScope.launch(Dispatchers.IO) {
        studentUseCases.getAllVolunteers()
    }

    fun getVolunteer(studentId: String, callback: (student: Volunteer) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        studentUseCases.getVolunteer(studentId, callback);
    }

    fun addVolunteer(student: Volunteer) = viewModelScope.launch(Dispatchers.IO) {
        studentUseCases.addVolunteer(student)
    }

    fun update(student: Volunteer, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            studentUseCases.update(student, data, onSuccessCallBack, onFailureCallBack)
        } catch (e: Exception) {
            onFailureCallBack()
        }
    }




}
