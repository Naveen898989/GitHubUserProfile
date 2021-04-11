package com.navdissanayake.domain.model

data class RepositoryNode(
    val name: String,
    val description: String,
    val stargazerCount: Int,
    val languages: LanguageNodes,
    val owner: RepositoryOwner
)