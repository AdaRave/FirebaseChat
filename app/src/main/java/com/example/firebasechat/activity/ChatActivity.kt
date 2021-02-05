package com.example.firebasechat.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasechat.R
import com.example.firebasechat.adapter.MessageAdapter
import com.example.firebasechat.model.Message
import com.example.firebasechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var adapter : MessageAdapter ?=null

    private var userName : String ="Default User"
    private var recipientUserId : String ?=null
    private var recipientUserName : String ?=null

    private val database = FirebaseDatabase.getInstance()//получили доступ к базе данных
    private var messageDatabaseReference : DatabaseReference ?=null
    private var messagesChildEventListener: ChildEventListener? = null

    private var usersDatabaseReference : DatabaseReference ?=null
    private var usersChildEventListener: ChildEventListener? = null
    private var auth: FirebaseAuth = Firebase.auth
    private val storage : FirebaseStorage = FirebaseStorage.getInstance()

    private val RC_IMAGE_PICKER = 111;

    private var chatImageStorageReference : StorageReference ?=null



    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_chat)

            val intent = intent

            if (intent!=null){
                userName= intent.getStringExtra("userName").toString()
                recipientUserId = intent.getStringExtra("recipientUserId")
                recipientUserName = intent.getStringExtra("recipientUserName")
            }

            title = "Chat with $recipientUserName"

            messageDatabaseReference = database.reference.child("messages")
            usersDatabaseReference = database.reference.child("users")

            chatImageStorageReference = storage.reference.child("chat_images")

            var messages : List<Message> = ArrayList<Message>()
            adapter = MessageAdapter(
                this,
                R.layout.message_item, messages
            )
            messageListView.adapter = adapter

      
            progressBar.visibility = ProgressBar.INVISIBLE

            messageEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    sendMessageButton.isEnabled = !p0?.isBlank()!!
                }

            })

        messageEditText.filters +=InputFilter.LengthFilter(15)
            
            messagesChildEventListener = object : ChildEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)
                    if ((message!!.sender.equals(auth.currentUser!!.uid) && message!!.recipient.equals(recipientUserId))
                    ){
                        message.isMine = true
                        adapter?.add(message)
                    } else if ( (message!!.recipient.equals(auth.currentUser!!.uid) && message!!.sender.equals(recipientUserId))
                    ){
                        message.isMine = false
                        adapter?.add(message)
                    }

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

            }
            messageDatabaseReference!!.addChildEventListener(messagesChildEventListener as ChildEventListener)

        usersChildEventListener = object  : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var user = snapshot.getValue(User::class.java)
                if (user!!.id.equals(FirebaseAuth.getInstance().currentUser!!.uid)){
                    userName = user.name
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        }

        usersDatabaseReference!!.addChildEventListener(usersChildEventListener as ChildEventListener)


    }

    fun sendMessage(view: View) {
        var message = Message()
        message.text = messageEditText.text.toString()
        message.name = userName
        message.imageUrl = null
        message.sender = auth.currentUser!!.uid
        message.recipient = recipientUserId

        messageDatabaseReference!!.push().setValue(message)

        messageEditText.text = null
    }
    fun sendImage(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Choose an image"), RC_IMAGE_PICKER)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.sign_out -> {
                Firebase.auth.signOut()
                startActivity(Intent(this, SingInActivity::class.java))
                true
            }
            else-> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK){
            var selectedImageUri : Uri = data!!.data!!
            val imageReference = chatImageStorageReference!!.child(selectedImageUri.lastPathSegment.toString())
            val uploadTask = imageReference.putFile(selectedImageUri)
            val urlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val message = Message()
                    message.imageUrl = downloadUri.toString()
                    message.name = userName
                    message.sender = auth.currentUser!!.uid
                    message.recipient = recipientUserId
                    messageDatabaseReference!!.push().setValue(message)
                } else {
                    // Handle failures
                    // ...
                }
            }

        }
    }
}