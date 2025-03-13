package com.connectify.connectifyNow.models.converters;

import androidx.room.TypeConverter;
import com.connectify.connectifyNow.models.OrganizationLocation;
import com.google.gson.Gson;

class Converters {
    @TypeConverter
    fun fromOrganizationLocation(location: OrganizationLocation?): String? {
        return location?.let { Gson().toJson(it); }
    }

    @TypeConverter
    fun toOrganizationLocation(locationJson: String?): OrganizationLocation? {
        return locationJson?.let { Gson().fromJson(it, OrganizationLocation::class.java) }
    }
}
