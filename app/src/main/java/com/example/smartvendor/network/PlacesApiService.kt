package com.example.smartvendor.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String = "store",
        @Query("key") apiKey: String
    ): PlacesResponse
}

data class PlacesResponse(
    @SerializedName("results") val results: List<PlaceResult>,
    @SerializedName("status") val status: String
)

data class PlaceResult(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("vicinity") val vicinity: String,
    @SerializedName("geometry") val geometry: PlaceGeometry,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("user_ratings_total") val userRatingsTotal: Int?,
    @SerializedName("types") val types: List<String>,
    @SerializedName("photos") val photos: List<PlacePhoto>?
)

data class PlaceGeometry(
    @SerializedName("location") val location: PlaceLocation
)

data class PlaceLocation(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

data class PlacePhoto(
    @SerializedName("photo_reference") val photoReference: String
)
