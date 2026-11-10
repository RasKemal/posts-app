package com.example.postsapplication.ui.list

import com.example.postsapplication.domain.model.Post
import org.junit.Assert.assertEquals
import org.junit.Test

class PostListItemUiModelTest {

    private fun post(body: String) =
        Post(id = 1, title = "title", body = body, imageUrl = "url", isEdited = true)

    @Test
    fun `line breaks in the body become single spaces`() {
        val item = post("quia et suscipit\nsuscipit recusandae  \r\n  consequuntur").toListItemUiModel()

        assertEquals("quia et suscipit suscipit recusandae consequuntur", item.bodyPreview)
    }

    @Test
    fun `spaces inside a line are kept`() {
        assertEquals("a  b", post("a  b").toListItemUiModel().bodyPreview)
    }

    @Test
    fun `other fields are copied`() {
        val item = post("body").toListItemUiModel()

        assertEquals(PostListItemUiModel(1, "title", "body", "url", isEdited = true), item)
    }
}
