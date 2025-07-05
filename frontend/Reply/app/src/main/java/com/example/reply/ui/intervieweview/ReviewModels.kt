package com.example.reply.ui.intervieweview

data class InterviewReview(
    val id: Int,
    val title: String,
    val created_at: String,
    val duration: Duration,
    val raw_file_path: String?,
    val review_content: String?,
    val start_time: String?,
    val tags: Tags?,
    val user_id: Int
)

data class Duration(val h: Int, val m: Int, val s: Int)

data class Tags(
    val focus: List<String>?,
    val job: String?,
    val style: String?
)
