package com.navdissanayake.presenter.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Base class for extending Presenters. Coroutines are already setup to be used.
 */
abstract class BasePresenter<View>(private val defaultDispatcher: CoroutineDispatcher) {

    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + defaultDispatcher

    /**
     * Coroutine scope for running task off main thread.
     */
    val scope = CoroutineScope(coroutineContext)

    var view: View? = null

    fun attachView(view: View) {
        this.view = view
    }

    fun detachView() {
        this.view = null
    }

}