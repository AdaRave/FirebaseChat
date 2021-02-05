package com.example.firebasechat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.firebasechat.R
import com.example.firebasechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sing_in.*

class SingInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "SingInActivity"
    private var loginModeActive = false

    val database = FirebaseDatabase.getInstance()//получили доступ к базе данных
    var usersDatabaseReference : DatabaseReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_in)

        usersDatabaseReference = database.reference.child("users")

        auth = Firebase.auth

       loginSignUpButton.setOnClickListener {
           loginSignUpUser(emailEditText.text.toString().trim(), passwordEditText.text.toString().trim()) }

        if (auth.currentUser != null){
            startActivity(Intent(this, UserListActivity::class.java))
        }
    }

    private fun loginSignUpUser(email: String, password:String) {

        if (loginModeActive){
            if (passwordEditText.text.toString().trim().length <7){
                Toast.makeText(this, "Passwords must be at least 7 characters", Toast.LENGTH_LONG).show()
            }else if (emailEditText.text.toString().trim().equals("")){
                Toast.makeText(this, "Please input your email", Toast.LENGTH_LONG).show()
            }else{
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            val intent = Intent(this, UserListActivity::class.java)
                            intent.putExtra("userName",  nameEditText.text.toString().trim())
                            startActivity(intent)

                        } else {

                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()

                        }
                    }
            }

        }else {
            if (!passwordEditText.text.toString().equals(repeatPasswordEditText.text.toString())) {
                Toast.makeText(this, "Passwords don't mach", Toast.LENGTH_LONG).show()

            } else if (passwordEditText.text.toString().trim().length <7){
                Toast.makeText(this, "Passwords must be at least 7 characters", Toast.LENGTH_LONG).show()
            }else if (emailEditText.text.toString().trim().equals("")){
                Toast.makeText(this, "Please input your email", Toast.LENGTH_LONG).show()
            }
            else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser

                            createUser(user)

                            val intent = Intent(this, UserListActivity::class.java)
                            intent.putExtra("userName",  nameEditText.text.toString().trim())
                            startActivity(intent)

                        //updateUI(user)
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                this, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            //updateUI(null)
                        }
                    }
            }
        }


    }



    private fun createUser(firebaseUser: FirebaseUser?) {
        var user: User = User()
        user.id = firebaseUser!!.uid
        user.email = firebaseUser.email.toString()
        user.name = nameEditText.text.toString().trim()

        usersDatabaseReference!!.push().setValue(user)
    }


    fun toggleLoginMade(view: View) {
        if (loginModeActive){
            loginModeActive = false
            loginSignUpButton.text = "Sign Up"
            toggleLoginSignUpTextView.text = "Or, log in"
            repeatPasswordEditText.visibility  = View.VISIBLE
        }else{
            loginModeActive = true
            loginSignUpButton.text = "Log in"
            toggleLoginSignUpTextView.text = "Or, sign up"
            repeatPasswordEditText.visibility  = View.GONE
        }
    }
}