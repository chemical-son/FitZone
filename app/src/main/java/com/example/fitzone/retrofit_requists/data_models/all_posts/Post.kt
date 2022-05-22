package com.example.fitzone.retrofit_requists.data_models.all_posts

data class Post(
    val caption: String,
    val comments: List<Comment>,
    val content: String,
    val created_at: String,
    val id: Int,
    val liked: Boolean,
    val likes: List<String>,
    val number_of_comments: Int,
    val number_of_likes: Int,
    val type: Int,
    val updated_at: String,
    val user_avatar: String,
    val user_id: Int,
    val username: String
)