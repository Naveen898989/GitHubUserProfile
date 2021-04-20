package com.navdissanayake.domain.usecase.user

import com.navdissanayake.domain.model.User
import com.navdissanayake.domain.repository.IUsersRepository
import com.navdissanayake.domain.usecase.BaseUseCase

/**
 * Use case class for retrieving cached user data from [IUsersRepository]
 */
class RetrieveCachedUser(private val usersRepository: IUsersRepository) :
    BaseUseCase<User?, RetrieveCachedUser.RequestValues> {

    override suspend fun execute(requestValues: RequestValues): User? =
        usersRepository.retrieveCachedUser(requestValues.userLogin)

    data class RequestValues(val userLogin: String) :
        BaseUseCase.IRequestValues
}