package com.example.postsapplication.data.remote

import com.example.postsapplication.data.remote.dto.PostDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface PostApi {
    @Headers("Cache-Control: max-age=0")
    @GET("posts")
    suspend fun getPosts(): Response<List<PostDto>>
}
