package com.example.firebasechat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasechat.R

import com.example.firebasechat.model.User

class UserAdapter(userArrayList: ArrayList<User>?) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    var users: ArrayList<User> ?= userArrayList
    var listener : OnUserClickedListener ?=null

    fun setOnUserClickedListener(listener: OnUserClickedListener){
        this.listener = listener
    }

    inner class UserViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val avatarImageView = itemView.findViewById<ImageView>(R.id.avatarImageView)
        val userNameTextView = itemView.findViewById<TextView>(R.id.userNameTextView)

        fun bind(listener: OnUserClickedListener){
            itemView.setOnClickListener {
                if (listener !=null){
                    val position = adapterPosition
                    if (position !=RecyclerView.NO_POSITION){
                        listener.onUserClicked(position)
                    }
                }
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder =
        UserViewHolder(LayoutInflater.from(parent.context).
        inflate(R.layout.user_item, parent, false))

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = users!!.get(position)
        holder.avatarImageView.setImageResource(currentUser.avatarMockUpResource)
        holder.userNameTextView.text = currentUser.name
        listener?.let { holder.bind(it) } //возможно необходимо перенести вызов
    }

    override fun getItemCount(): Int = users!!.size

    interface OnUserClickedListener{
        fun onUserClicked(position: Int)
    }
}