package com.navdissanayake.domain.usecase.user

import com.navdissanayake.domain.repository.IUsersRepository
import com.navdissanayake.domain.usecase.BaseUseCase

/**
 * Use case class to invalidate all cached data from [IUsersRepository].
 */
class InvalidateCachedData(private val usersRepository: IUsersRepository) :
    BaseUseCase<Boolean, InvalidateCachedData.RequestValues> {

    override suspend fun execute(requestValues: RequestValues): Boolean {
        return usersRepository.invalidateCachedData()
    }

    class RequestValues :
        BaseUseCase.IRequestValues

}