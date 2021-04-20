package com.navdissanayake.domain.usecase

/**
 * Base class to extend from when creating use cases. [Response] generic will be the response
 * returned by execute function. [Request] generic can be used for passing parameters.
 */
interface BaseUseCase<Response, Request : BaseUseCase.IRequestValues> {

    /**
     * Execute use case with given request values. Implement [IRequestValues] with fields to pass
     * parameters to this function.
     */
    suspend fun execute(requestValues: Request): Response

    /**
     * Interface used to implement to pass parameters to execute function.
     */
    interface IRequestValues

}