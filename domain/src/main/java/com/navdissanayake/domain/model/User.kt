package com.navdissanayake.domain.model

data class User(
    val name: String,
    val login: String,
    val email: String,
    val avatarUrl: String,
    val followers: Followers,
    val following: Following,
    val pinnedItems: RepositoryNodes,
    val topRepositories: RepositoryNodes,
    val starredRepositories: RepositoryNodes
)