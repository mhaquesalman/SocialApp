package com.salman.socialapp.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salman.socialapp.R
import com.salman.socialapp.model.User
import com.salman.socialapp.network.BASE_URL
import com.salman.socialapp.ui.activities.ProfileActivity
import com.salman.socialapp.ui.activities.USER_ID
import kotlinx.android.synthetic.main.item_search.view.*

class SearchAdapter(val context: Context) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

     private var userList: ArrayList<User> = ArrayList()

     fun updateList(list: List<User>) {
        userList.clear()
        userList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val user = userList.get(position)

        holder.itemView.user_name.text = user.name

        val userUri = Uri.parse(user.profileUrl)
        val userImage = if (userUri.authority == null) {
            BASE_URL + user.profileUrl
        } else {
            user.profileUrl
        }
        if (!userImage!!.isEmpty()) {
            Glide.with(context)
                .load(userImage)
                .placeholder(R.drawable.default_profile_placeholder)
                .into(holder.itemView.user_image)
        }

        holder.itemView.setOnClickListener {
            val inputMethodManager =
               context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            context.startActivity(Intent(context, ProfileActivity::class.java)
                .putExtra(USER_ID, userList.get(holder.adapterPosition).uid))
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}