package com.navdissanayake.domain.usecase.user

import com.navdissanayake.domain.model.User
import com.navdissanayake.domain.repository.IUsersRepository
import com.navdissanayake.domain.usecase.BaseUseCase

/**
 * Use case class for retrieving user from [IUsersRepository]
 */
class RetrieveUser(private val usersRepository: IUsersRepository) :
    BaseUseCase<User, RetrieveUser.RequestValues> {

    override suspend fun execute(requestValues: RequestValues): User =
        usersRepository.retrieveUser(requestValues.userLogin)

    data class RequestValues(val userLogin: String) :
        BaseUseCase.IRequestValues

}