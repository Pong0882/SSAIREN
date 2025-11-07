package com.example.ssairen_app.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 파일 업로드 응답 DTO
 * 백엔드 FileUploadResponse와 동일한 구조
 */
data class FileUploadResponse(
    @SerializedName("fileName")
    val fileName: String,

    @SerializedName("originalFileName")
    val originalFileName: String,

    @SerializedName("fileSize")
    val fileSize: Long,

    @SerializedName("contentType")
    val contentType: String,

    @SerializedName("bucketName")
    val bucketName: String,

    @SerializedName("fileUrl")
    val fileUrl: String
)
