package com.connectify.connectifyNow.viewModules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudentViewModel: ViewModel() {
    val studentUseCases: StudentUseCases = StudentUseCases()

    fun createUserAsStudent(email: String, password: String, onSuccessCallBack: (String?) -> Unit, onFailureCallBack: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        studentUseCases.fireStoreAuthRepository.createUser(email, password, { userId ->
            onSuccessCallBack(userId)
        }, onFailureCallBack)
    }

    fun getAllStudents() = viewModelScope.launch(Dispatchers.IO) {
        studentUseCases.getAllStudents()
    }

    fun getStudent(studentId: String, callback: (student: Student) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        studentUseCases.getStudent(studentId, callback);
    }

    fun addStudent(student: Student) = viewModelScope.launch(Dispatchers.IO) {
        studentUseCases.addStudent(student)
    }

    fun update(student: Student, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            studentUseCases.update(student, data, onSuccessCallBack, onFailureCallBack)
        } catch (e: Exception) {
            onFailureCallBack()
        }
    }




}
