package com.connectify.connectifyNow.repositories.Volunteer

class FireStoreVolunteerRepository {
    companion object {
        const val USERS_COLLECTION_PATH = "users"
    }

    val apiManager = ApiManager()

    suspend fun addStudent(student: Student, onSuccess: (String) -> Unit): String {

        val documentReference = apiManager.db.collection(USERS_COLLECTION_PATH)
            .add(student.json)
            .await()

        onSuccess(student.id)
        return documentReference.id
    }

}