package com.example.postsapplication.ui.screens.list.model

import com.example.postsapplication.domain.model.Post
import org.junit.Assert.assertEquals
import org.junit.Test

class PostListItemUiModelTest {

    private fun post(body: String) =
        Post(id = 1, title = "title", body = body, imageUrl = "url")

    @Test
    fun `line breaks in the body become single spaces`() {
        val item = post("quia et suscipit\nsuscipit recusandae  \r\n  consequuntur").toListItemUiModel(query = null)

        assertEquals("quia et suscipit suscipit recusandae consequuntur", item.bodyPreview)
    }

    @Test
    fun `spaces inside a line are kept`() {
        assertEquals("a  b", post("a  b").toListItemUiModel(query = null).bodyPreview)
    }

    @Test
    fun `other fields are copied`() {
        val item = post("body").toListItemUiModel(query = null)

        assertEquals(PostListItemUiModel(1, "#1", "title", "body", "url"), item)
    }

    @Test
    fun `list key includes the query so different searches never share keys`() {
        assertEquals("qui#1", post("body").toListItemUiModel(query = "qui").listKey)
        assertEquals("#1", post("body").toListItemUiModel(query = null).listKey)
    }
}
