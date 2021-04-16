package com.navdissanayake.presenter.view.main

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.navdissanayake.domain.model.User
import com.navdissanayake.domain.repository.IUsersRepository
import com.navdissanayake.presenter.BuildConfig
import com.navdissanayake.presenter.R
import com.navdissanayake.presenter.databinding.ActivityMainBinding
import com.navdissanayake.data.util.NoInternetException
import com.navdissanayake.presenter.util.USER_LOGIN
import dagger.android.DaggerActivity
import javax.inject.Inject

class MainActivity : DaggerActivity(), MainPresenter.View {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var usersRepository: IUsersRepository

    private val pinnedRepositoriesRecyclerAdapter =
        RepositoryAdapter(this, R.layout.item_vertical_repository_list)
    private val topRepositoriesRecyclerAdapter =
        RepositoryAdapter(this, R.layout.item_horizontal_repository_list)
    private val starredRepositoriesRecyclerAdapter =
        RepositoryAdapter(this, R.layout.item_horizontal_repository_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: CoordinatorLayout = binding.root
        setContentView(view)

        init()
    }

    override fun onDestroy() {
        presenter.detachView()

        super.onDestroy()
    }

    /**
     * Initialize activity
     */
    private fun init() {
        initPresenter()
        initViews()

        attemptLoadUser()
    }

    override fun onRetainNonConfigurationInstance(): Any {
        return presenter
    }

    /**
     * Initialize presenter and attach view. If presenter was previously retained grab it.
     */
    private fun initPresenter() {
        presenter = if (lastNonConfigurationInstance != null) {
            lastNonConfigurationInstance as MainPresenter
        } else {
            MainPresenter(applicationContext, usersRepository)
        }
        presenter.attachView(this)
    }

    /**
     * Initialize view.
     */
    private fun initViews() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            presenter.invalidateCache()
            attemptLoadUser()
        }

        binding.recyclerViewPinned.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pinnedRepositoriesRecyclerAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.divider_space_item
                        )!!
                    )
                }
            )
        }
        binding.recyclerViewTopRepositories.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = topRepositoriesRecyclerAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.HORIZONTAL
                ).apply {
                    setDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.divider_space_item
                        )!!
                    )
                }
            )
        }
        binding.recyclerViewStarredRepositories.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = starredRepositoriesRecyclerAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.HORIZONTAL
                ).apply {
                    setDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.divider_space_item
                        )!!
                    )
                }
            )
        }
    }

    /**
     * Attempt to load user data. Checks for internet connection and shows message here.
     */
    private fun attemptLoadUser() {
        presenter.loadUser(USER_LOGIN)
    }

    override fun showLoading() {
        binding.swipeRefreshLayout.isRefreshing = true
    }

    override fun hideLoading() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onLoadUserComplete(user: User) {
        // Content hidden at start. So show it if load user was a success.
        binding.layoutContent.visibility = View.VISIBLE

        glide
            .load(user.avatarUrl)
            .placeholder(R.drawable.ic_avatar_placeholder_24)
            .error(R.drawable.ic_error_24)
            .circleCrop()
            .into(binding.imageViewAvatar)

        binding.textViewName.text = user.name
        binding.textViewLogin.text = user.login
        binding.textViewEmail.text = user.email
        binding.textViewFollowers.text = resources.getQuantityString(
            R.plurals.d_followers,
            user.followers.totalCount,
            user.followers.totalCount
        )
        binding.textViewFollowing.text = getString(R.string.d_following, user.following.totalCount)

        pinnedRepositoriesRecyclerAdapter.setItems(user.pinnedItems.nodes)
        topRepositoriesRecyclerAdapter.setItems(user.topRepositories.nodes)
        starredRepositoriesRecyclerAdapter.setItems(user.starredRepositories.nodes)
    }

    override fun onLoadUserError(e: Throwable?) {
        if (e is NoInternetException) {
            showError(R.string.error_no_internet)
        } else {
            showError(R.string.error_unknown, e)
        }
    }

    /**
     * Show error to user.
     * @param error String resource id of the error to be shown.
     * @param e Optional throwable object if available to be printed on logcat. Will only log if in
     * debug mode.
     */
    private fun showError(@StringRes error: Int, e: Throwable? = null) {
        if (BuildConfig.DEBUG && e != null) {
            e.printStackTrace()
        }

        Snackbar
            .make(binding.root, error, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) { attemptLoadUser() }
            .show()
    }
}