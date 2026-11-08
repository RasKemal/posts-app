package com.example.postsapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The demo API accepts writes but never persists them, so these flags stop a refresh from
 * undoing user changes: [isLocallyModified] keeps edits, [isDeleted] keeps deleted posts hidden.
 * With a real backend, deleted rows would simply be removed.
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
    val isLocallyModified: Boolean = false,
    val isDeleted: Boolean = false,
)
