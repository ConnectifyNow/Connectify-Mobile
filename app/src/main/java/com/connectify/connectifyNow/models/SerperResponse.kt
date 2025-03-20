package com.connectify.connectifyNow.models

data class LocationsResponse (val places: Array<Location>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocationsResponse

        return places.contentEquals(other.places)
    }

    override fun hashCode(): Int {
        return places.contentHashCode()
    }
}