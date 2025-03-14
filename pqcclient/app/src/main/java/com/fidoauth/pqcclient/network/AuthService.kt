package com.fidoauth.pqcclient.network

import com.fidoauth.pqcclient.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {

    // Start registration process (server provides challenge)
    @GET("auth/register/start")
    suspend fun startRegistration(@Query("username") username: String): RegistrationRequestDto

    // Finish registration (client sends signed challenge and keys)
    @POST("auth/register/finish")
    suspend fun finishRegistration(
        @Body registrationResponse: RegistrationResponseDto,
        @Query("username") username: String
    ): RegistrationFinishDto

    // Start login process (server provides challenge)
    @POST("auth/login/start")
    suspend fun startLogin(@Query("username") username: String): LoginRequestDto

    // Finish login (client sends signed challenge for verification)
    @POST("auth/login/finish")
    suspend fun finishLogin(
        @Body loginResponse: LoginResponseDto,
        @Query("username") username: String
    ): LoginFinishDto

    @GET("api/user")
    suspend fun getProtectedData(
    ): String
}
