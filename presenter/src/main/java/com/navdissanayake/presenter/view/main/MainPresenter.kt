package com.navdissanayake.presenter.view.main

import com.navdissanayake.data.Users
import com.navdissanayake.domain.model.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainPresenter @Inject constructor(private val users: Users, private val view: View) {

    private val disposables: CompositeDisposable = CompositeDisposable()

    fun loadUser(userLogin: String) {
        disposables.add(users.retrieveUser(userLogin)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view.showLoading() }
            .doFinally { view.hideLoading() }
            .subscribe({ view.onLoadUserComplete(it) }, { view.onLoadUserError(it) })
        )
    }

    interface View {
        fun showLoading()
        fun hideLoading()
        fun onLoadUserComplete(user: User)
        fun onLoadUserError(e: Throwable)
    }

}
