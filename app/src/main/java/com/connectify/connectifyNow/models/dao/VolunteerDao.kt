package com.connectify.connectifyNow.models.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.connectify.connectifyNow.models.Volunteer

@Dao
interface VolunteerDao {

    @Query("SELECT * FROM volunteer")
    fun getAllVolunteers(): LiveData<MutableList<Volunteer>>

    @Insert
    fun addVolunteer(volunteer: Volunteer)

    @Update
    fun updateVolunteer(volunteer: Volunteer)

    @Delete
    fun deleteVolunteer(volunteer: Volunteer)

    @Query("DELETE FROM volunteer")
    suspend fun deleteAllVolunteers()

}
