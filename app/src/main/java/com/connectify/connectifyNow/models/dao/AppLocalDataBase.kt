package com.connectify.connectifyNow.models.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.connectify.connectifyNow.base.MyApplication
import com.connectify.connectifyNow.models.Organization
import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.models.converters.Converters

@TypeConverters(Converters::class)
@Database(entities = [Post::class, Organization::class, Volunteer:: class], version = 1)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun getPostDao(): PostDao
    abstract fun getOrganizationDao(): OrganizationDao
    abstract fun getVolunteerDao(): VolunteerDao
}

object AppLocalDatabase {

    val db: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.appContext
            ?: throw IllegalStateException("Application context not available")

        Room.databaseBuilder(
            context,
            AppLocalDbRepository::class.java,
            "ConnectifyDatabase.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
