package com.example.linkkoy3.api

import com.example.linkkoy3.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<User>

    @GET("api/folders")
    suspend fun getFolders(@Query("parent_id") parentId: String? = null): Response<List<Folder>>

    @POST("api/folders")
    suspend fun createFolder(@Body request: Map<String, String?>): Response<Folder>

    @GET("api/folders/{folder_id}")
    suspend fun getFolder(@Path("folder_id") folderId: String): Response<Folder>

    @PUT("api/folders/{folder_id}")
    suspend fun updateFolder(@Path("folder_id") folderId: String, @Body request: Map<String, String?>): Response<Folder>

    @DELETE("api/folders/{folder_id}")
    suspend fun deleteFolder(@Path("folder_id") folderId: String): Response<Map<String, String>>

    @GET("api/links")
    suspend fun getLinks(@Query("folder_id") folderId: String): Response<List<Link>>

    @POST("api/links")
    suspend fun createLink(@Body request: Map<String, String>): Response<Link>

    @PUT("api/links/{link_id}")
    suspend fun updateLink(@Path("link_id") linkId: String, @Body request: Map<String, String>): Response<Link>

    @DELETE("api/links/{link_id}")
    suspend fun deleteLink(@Path("link_id") linkId: String): Response<Map<String, String>>

    @GET("api/search")
    suspend fun search(@Query("q") query: String): Response<SearchResponse>
}
