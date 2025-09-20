package com.example.dadum.network

import com.google.gson.annotations.SerializedName

data class LoginResponse (
    @SerializedName("usersId")
    val usersId: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String
)
