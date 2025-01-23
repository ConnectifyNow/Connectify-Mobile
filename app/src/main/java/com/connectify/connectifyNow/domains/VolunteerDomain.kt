package com.android.skillsync.domain

import androidx.lifecycle.LiveData
import com.android.skillsync.models.Student.Student
import com.android.skillsync.repoistory.Auth.FireStoreAuthRepository
import com.android.skillsync.repoistory.Student.FireStoreStudentRepository
import com.android.skillsync.repoistory.Student.LocalStoreUserRepository

class StudentUseCases {

    val localStoreStudentRepository: LocalStoreUserRepository = LocalStoreUserRepository()
    val fireStoreStudentRepository: FireStoreStudentRepository = FireStoreStudentRepository()
    val fireStoreAuthRepository: FireStoreAuthRepository = FireStoreAuthRepository()

    val studentLiveData: LiveData<MutableList<Student>> get() = localStoreStudentRepository.student

    fun getAllStudents() {
        studentLiveData
    }

    fun getStudent(studentId: String, callback: (student: Student) -> Unit) {
        fireStoreStudentRepository.getStudent(studentId, callback);
    }

    suspend fun addStudent(student: Student) {
        fireStoreStudentRepository.addStudent(student) { studentId ->
            fireStoreStudentRepository.setStudentInUserTypeDB(studentId)
        }
        localStoreStudentRepository.addStudent(student)
    }

    suspend fun update(student: Student, data: Map<String, Any>, onSuccessCallBack: () -> Unit, onFailureCallBack: () -> Unit) {
        fireStoreStudentRepository.updateStudent(student, data, onSuccessCallBack, onFailureCallBack)
        localStoreStudentRepository.updateStudent(student)
    }
}
