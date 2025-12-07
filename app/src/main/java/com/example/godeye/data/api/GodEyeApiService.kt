package com.example.godeye.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de la API de GodEye en la nube
 *
 * Base URL: https://gateway.helmer-pardo.com
 */
interface GodEyeApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/register-admin")
    suspend fun registerAdmin(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("reports")
    suspend fun createReport(
        @Header("Authorization") authorization: String,
        @Body request: CreateReportRequest
    ): Response<ReportResponse>

    @GET("reports")
    suspend fun getReports(
        @Header("Authorization") authorization: String
    ): Response<List<ReportResponse>>

    @GET("reports/{id}")
    suspend fun getReportById(
        @Header("Authorization") authorization: String,
        @Path("id") reportId: String
    ): Response<ReportResponse>

    @GET("reports/check/{placa}")
    suspend fun searchReportByPlate(
        @Header("Authorization") authorization: String,
        @Path("placa") placa: String
    ): Response<CheckPlateResponse>

    @GET("admin/reports")
    suspend fun getReportsAdmin(
        @Header("Authorization") authorization: String
    ): Response<List<ReportResponse>>

    @POST("history")
    suspend fun createHistory(
        @Header("Authorization") authorization: String,
        @Body request: CreateHistoryRequest
    ): Response<HistoryResponse>

    @GET("history")
    suspend fun getHistory(
        @Header("Authorization") authorization: String
    ): Response<List<HistoryResponse>>

    /**
     * Sube una foto de evidencia y crea un registro de historial asociado a un reporte
     * POST /files/upload-history
     */
    @Multipart
    @POST("files/upload-history")
    suspend fun uploadHistory(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("reportId") reportId: RequestBody,
        @Part("timestamp") timestamp: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody
    ): Response<UploadHistoryResponse>

    /**
     * Obtiene todo el historial asociado a un reporte específico
     * GET /history/report/{reportId}
     */
    @GET("history/report/{reportId}")
    suspend fun getHistoryByReportId(
        @Header("Authorization") authorization: String,
        @Path("reportId") reportId: String
    ): Response<ApiResponseWrapper<List<HistoryResponse>>>
}

