package com.example.dadum.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//여기는 RetrofitClient인스턴스를 설정하는 코드
//object는 코틀린 전역에서 하나만 존재하는 싱글톤 객체를 정의할 떄 사용,
object RetrofitClient {
    //    서버의 기본 주소를 저장하는 상수
    private const val BASE_URL = "http://192.168.0.21:8080" //여기에 실제 서버 주소 입력
    // apiService는 ApiService타입의 변수 by lazy는 처음 사용할 때 한 번만 실행하고 저장되는 방식
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}