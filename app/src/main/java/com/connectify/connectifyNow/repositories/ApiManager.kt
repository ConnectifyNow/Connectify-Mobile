package com.connectify.connectifyNow.repositories

import com.connectify.connectifyNow.models.dao.AppLocalDatabase
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

class ApiManager {

    val db = Firebase.firestore
    val firebaseAuth = FirebaseAuth.getInstance()
    val dao = AppLocalDatabase.db

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }
        db.firestoreSettings = settings
    }
}