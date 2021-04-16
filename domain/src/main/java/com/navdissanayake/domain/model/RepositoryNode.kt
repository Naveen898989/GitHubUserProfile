package com.navdissanayake.domain.model

import com.google.gson.annotations.SerializedName

data class RepositoryNode(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("stargazerCount")
    val stargazerCount: Int,

    @SerializedName("languages")
    val languages: LanguageNodes,

    @SerializedName("owner")
    val owner: RepositoryOwner
)

data class LanguageNodes(
    @SerializedName("nodes")
    val nodes: List<LanguageNode>
)

data class LanguageNode(
    @SerializedName("color")
    val color: String,

    @SerializedName("name")
    val name: String
)

data class RepositoryOwner(
    @SerializedName("login")
    val login: String,

    @SerializedName("avatarUrl")
    val avatarUrl: String
)