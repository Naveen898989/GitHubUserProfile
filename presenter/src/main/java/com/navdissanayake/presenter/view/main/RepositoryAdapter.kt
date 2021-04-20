package com.navdissanayake.presenter.view.main

import android.app.Activity
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.navdissanayake.domain.model.RepositoryNode
import com.navdissanayake.presenter.R

/**
 * RecyclerView adapter to display repositories. Use [setItems()][setItems] update items.
 *
 * @param activity Activity instance using this adapter. Used by Glide to handle lifecycle.
 * @param itemLayoutResource Layout resource to inflate.
 */
class RepositoryAdapter(
    private val activity: Activity,
    @LayoutRes private val itemLayoutResource: Int
) : RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    private val itemList: ArrayList<RepositoryNode> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(activity).inflate(itemLayoutResource, parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repositoryNode: RepositoryNode = itemList[position]

        Glide
            .with(activity)
            .load(repositoryNode.owner.avatarUrl)
            .circleCrop()
            .into(holder.ownerAvatarImageView)

        holder.ownerLoginTextView.text = repositoryNode.owner.login
        holder.nameTextView.text = repositoryNode.name
        holder.descriptionTextView.text = repositoryNode.description
        holder.starredTextView.text = repositoryNode.stargazerCount.toString()

        if (repositoryNode.languages.nodes.isNotEmpty()) {
            holder.languageTextView.text = repositoryNode.languages.nodes[0].name

            tintLanguageColorCircle(
                holder.languageTextView,
                Color.parseColor(repositoryNode.languages.nodes[0].color)
            )
        } else {
            holder.languageTextView.text = ""

            tintLanguageColorCircle(holder.languageTextView, Color.WHITE)
        }
    }

    private fun tintLanguageColorCircle(textView: TextView, @ColorInt color: Int) {
        val drawable: Drawable? = textView.compoundDrawablesRelative[0]
        if (drawable != null) {
            drawable.colorFilter = PorterDuffColorFilter(
                color,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    /**
     * Set the items in the list and update view.
     */
    fun setItems(itemList: List<RepositoryNode>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)

        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ownerAvatarImageView: ImageView = itemView.findViewById(R.id.imageViewAvatar)
        val ownerLoginTextView: TextView = itemView.findViewById(R.id.textViewLogin)
        val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        val starredTextView: TextView = itemView.findViewById(R.id.textViewStarred)
        val languageTextView: TextView = itemView.findViewById(R.id.textViewLanguage)
    }

}