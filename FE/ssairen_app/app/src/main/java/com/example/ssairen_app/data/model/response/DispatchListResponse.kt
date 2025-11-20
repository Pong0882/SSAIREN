// DispatchListResponse.kt
package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 출동지령 내역 목록 조회 API 응답
 */
data class DispatchListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: DispatchListData?,

    @SerializedName("timestamp")
    val timestamp: String
)

data class DispatchListData(
    @SerializedName("fire_state")
    val fireState: FireState,

    @SerializedName("dispatches")
    val dispatches: List<Dispatch>,

    @SerializedName("pagination")
    val pagination: Pagination
)

data class FireState(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

data class Dispatch(
    @SerializedName("id")
    val id: Int,

    @SerializedName("disasterNumber")
    val disasterNumber: String,

    @SerializedName("disasterType")
    val disasterType: String,

    @SerializedName("disasterSubtype")
    val disasterSubtype: String,

    @SerializedName("reporterName")
    val reporterName: String,

    @SerializedName("reporterPhone")
    val reporterPhone: String,

    @SerializedName("locationAddress")
    val locationAddress: String,

    @SerializedName("incidentDescription")
    val incidentDescription: String,

    @SerializedName("dispatchLevel")
    val dispatchLevel: String,

    @SerializedName("dispatchOrder")
    val dispatchOrder: Int,

    @SerializedName("dispatchStation")
    val dispatchStation: String,

    @SerializedName("date")
    val date: String
)

data class Pagination(
    @SerializedName("next_cursor")
    val nextCursor: String?,

    @SerializedName("has_more")
    val hasMore: Boolean
)
