package com.example.postsapplication.domain.usecase

import com.example.postsapplication.domain.repository.PostRepository
import com.example.postsapplication.domain.result.UpdatePostResult
import javax.inject.Inject

class UpdatePostUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    suspend operator fun invoke(id: Int, title: String, body: String): UpdatePostResult {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return UpdatePostResult.BlankTitle

        repository.updatePost(id, trimmedTitle, body.trim())
        return UpdatePostResult.Success
    }
}
