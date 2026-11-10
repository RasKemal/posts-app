package com.example.postsapplication.data.remote

import com.example.postsapplication.data.remote.dto.PostDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path

interface PostApi {
    @Headers("Cache-Control: max-age=0")
    @GET("posts")
    suspend fun getPosts(): Response<List<PostDto>>

    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: PostDto): PostDto

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Int)
}
