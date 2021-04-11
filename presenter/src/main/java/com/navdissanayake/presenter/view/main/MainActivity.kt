package com.navdissanayake.presenter.view.main

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.navdissanayake.data.Users
import com.navdissanayake.domain.model.User
import com.navdissanayake.presenter.BuildConfig
import com.navdissanayake.presenter.R
import com.navdissanayake.presenter.databinding.ActivityMainBinding
import com.navdissanayake.presenter.util.Helper
import com.navdissanayake.presenter.util.USER_LOGIN
import dagger.android.DaggerActivity
import javax.inject.Inject

class MainActivity : DaggerActivity(), MainPresenter.View {

    private lateinit var binding: ActivityMainBinding

    lateinit var presenter: MainPresenter

    @Inject
    lateinit var glide: RequestManager

    private val pinnedRepositoriesRecyclerAdapter =
        RepositoryAdapter(this, R.layout.item_vertical_repository_list)
    private val topRepositoriesRecyclerAdapter =
        RepositoryAdapter(this, R.layout.item_horizontal_repository_list)
    private val starredRepositoriesRecyclerAdapter =
        RepositoryAdapter(this, R.layout.item_horizontal_repository_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = MainPresenter(Users(this), this)

        init()
    }

    private fun init() {
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

        attemptLoadUser()
    }

    private fun attemptLoadUser() {
        if (Helper.isNetworkConnected(this)) {
            presenter.loadUser(USER_LOGIN)
        } else {
            showError(R.string.error_no_internet)
        }
    }

    override fun showLoading() {
        binding.layoutContent.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.layoutContent.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    override fun onLoadUserComplete(user: User) {
        glide
            .load(user.avatarUrl)
            .circleCrop()
            .into(binding.imageViewAvatar)

        binding.textViewName.text = user.name
        binding.textViewLogin.text = user.login
        binding.textViewEmail.text = user.email
        binding.textViewFollowers.text = getString(R.string.d_followers, user.followers.totalCount)
        binding.textViewFollowing.text = getString(R.string.d_following, user.following.totalCount)

        pinnedRepositoriesRecyclerAdapter.setItems(user.pinnedItems.nodes)
        topRepositoriesRecyclerAdapter.setItems(user.topRepositories.nodes)
        starredRepositoriesRecyclerAdapter.setItems(user.starredRepositories.nodes)
    }

    override fun onLoadUserError(e: Throwable) {
        showError(R.string.error_unknown, e)
    }

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