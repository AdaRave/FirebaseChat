package com.example.firebasechat.adapter


import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firebasechat.model.Message
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.firebasechat.R

class MessageAdapter(context: Context, resource: Int, messages: List<Message>) :
    ArrayAdapter<Message> (context, resource, messages) {

        val messages : List<Message> = messages


    val activity : Activity
            get() = this.context as Activity
//    val resource =this.resource

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var viewHolder : ViewHolder
        var  convertView:View ?= null

        val layoutInflater : LayoutInflater =
            activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val message = getItem(position)

        var layoutResource = 0
        val viewType = getItemViewType(position)

        layoutResource = if (viewType == 0) R.layout.my_message_item
        else R.layout.your_message_item


        //val inflater = LayoutInflater.from(context)
        //var rowView = inflater.inflate(R.layout.message_item, parent, false)


        if (convertView != null) viewHolder = convertView.tag as ViewHolder
        else{
            convertView= layoutInflater.inflate(layoutResource, parent, false)
            viewHolder = ViewHolder(convertView)
            convertView!!.tag = viewHolder

        }



        var isText = message?.imageUrl == null

        if (isText){
            viewHolder.messageTextView!!.visibility = View.VISIBLE
            viewHolder.photoImageView!!.visibility = View.INVISIBLE
            viewHolder.messageTextView!!.text = message?.text
        }else{
            viewHolder.messageTextView!!.visibility = View.GONE
            viewHolder.photoImageView!!.visibility = View.VISIBLE
            Glide.with(viewHolder.photoImageView!!.context).load(message?.imageUrl)
                .into(viewHolder!!.photoImageView!!)
        }


        return convertView
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages.get(position)
        return if (message.isMine) 0
        else 1
    }

    override fun getViewTypeCount(): Int {
        return 2
    }



    inner class ViewHolder{
        var photoImageView : ImageView?= null
        var  messageTextView : TextView? = null

        constructor(view : View){
            photoImageView = view.findViewById(R.id.photoImageView)
            messageTextView = view.findViewById(R.id.messageTextView)
        }

//        fun ViewHolder(view : View){
//
//
//       }
    }

}