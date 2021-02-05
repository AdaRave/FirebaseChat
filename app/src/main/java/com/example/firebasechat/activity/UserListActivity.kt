package com.example.firebasechat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasechat.R
import com.example.firebasechat.adapter.UserAdapter
import com.example.firebasechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_user_list.*

class UserListActivity : AppCompatActivity() {

    private var usersDatabaseReference : DatabaseReference?=null
    private var usersChildEventListener: ChildEventListener? = null
    private var auth: FirebaseAuth = Firebase.auth

    private var userArrayList : ArrayList<User> = ArrayList()

    val usAdapter : UserAdapter = UserAdapter(userArrayList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        attachUserDatabaseReferenceListener()
        buildRecyclerView()
    }

    private fun attachUserDatabaseReferenceListener() {
        usersDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        if (usersChildEventListener == null){
            usersChildEventListener = object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    var user = snapshot.getValue(User::class.java)

                    if (user!!.id != auth.currentUser!!.uid){
                        user!!.avatarMockUpResource = R.drawable.ic_baseline_emoji_emotions_24
                        userArrayList?.add(user)
                        usAdapter.notifyDataSetChanged()
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
    }

    private fun buildRecyclerView() {
        userListRecyclerView.setHasFixedSize(true)
        userListRecyclerView.addItemDecoration(DividerItemDecoration(userListRecyclerView.context,
            DividerItemDecoration.VERTICAL))
        userListRecyclerView.layoutManager = LinearLayoutManager(this)
        userListRecyclerView.adapter = usAdapter

        usAdapter.setOnUserClickedListener(object : UserAdapter.OnUserClickedListener{
            override fun onUserClicked(position: Int) {
                goToChat(position)
            }

        })
    }

    private fun goToChat(position: Int) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("recipientUserId", userArrayList[position].id)
        intent.putExtra("recipientUserName", userArrayList[position].name)
        startActivity(intent)
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
}