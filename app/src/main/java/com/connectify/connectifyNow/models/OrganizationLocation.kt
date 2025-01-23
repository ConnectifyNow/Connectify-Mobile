package com.connectify.connectifyNow.models;

import com.firebase.geofire.core.GeoHash
import com.google.firebase.firestore.GeoPoint

data class OrganizationLocation(val address: String, val location: GeoPoint, val geoHash: GeoHash)
