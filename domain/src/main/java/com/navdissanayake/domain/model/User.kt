package com.navdissanayake.domain.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("name")
    val name: String,

    @SerializedName("login")
    val login: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("avatarUrl")
    val avatarUrl: String,

    @SerializedName("followers")
    val followers: Followers,

    @SerializedName("following")
    val following: Following,

    @SerializedName("pinnedItems")
    val pinnedItems: RepositoryNodes,

    @SerializedName("topRepositories")
    val topRepositories: RepositoryNodes,

    @SerializedName("starredRepositories")
    val starredRepositories: RepositoryNodes
)

data class Following(
    @SerializedName("totalCount")
    val totalCount: Int
)

data class Followers(
    @SerializedName("totalCount")
    val totalCount: Int
)

data class RepositoryNodes(
    @SerializedName("nodes")
    val nodes: List<RepositoryNode>
)