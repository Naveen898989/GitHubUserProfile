package com.navdissanayake.data.networking.user

import com.navdissanayake.domain.model.User

/**
 * Class used to deserialize user object from github API
 */
data class UserResponse(val user: User)