package com.example.linkkoy3.data

data class User(val id: String, val name: String, val email: String)

data class Link(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val folderId: String? = null,
    val userId: String = ""
)

data class Folder(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val color: String? = null, // YENİ EKLENDİ
    val parentId: String? = null,
    val userId: String = ""
)

data class AuthResponse(val token: String)

data class SearchResponse(val folders: List<Folder>, val links: List<Link>)
