package com.example.postsapplication.domain.usecase

import com.example.postsapplication.core.common.EmptyResult
import com.example.postsapplication.core.common.Result
import com.example.postsapplication.domain.error.UpdatePostError
import com.example.postsapplication.domain.repository.PostRepository
import javax.inject.Inject

/** Validates and trims the edit before sending it; the title is required. */
class UpdatePostUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    suspend operator fun invoke(id: Int, title: String, body: String): EmptyResult<UpdatePostError> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return Result.Error(UpdatePostError.BlankTitle)

        return when (val result = repository.updatePost(id, trimmedTitle, body.trim())) {
            is Result.Success -> result
            is Result.Error -> Result.Error(UpdatePostError.Failed(result.error))
        }
    }
}
