package com.example.dynamichealth_doctors.LoginSignUp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.dynamichealth_doctors.MainActivity
import com.example.dynamichealth_doctors.databinding.ActivityLoginBinding
import com.example.dynamichealth_doctors.doc_modal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val mAuth: FirebaseAuth? = null
    private val SHARED_PREFS = "sharedPrefs"
    private val tenant_phone = "tenantPhone"
    private val tenant_name = "tenantName"
    private val tenant_id = "tenantId"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        if(!TextUtils.isEmpty(sharedPreferences.getString(tenant_id, "").toString())){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            //intent.putExtra("doc_name_from_Login", doc?.name.toString())
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            Log.d("nameFromLogin", sharedPreferences.getString(tenant_name, "").toString())
            startActivity(intent)
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

        Log.d("tenantID_ONSTART", sharedPreferences.getString(tenant_id, "").toString())

        if(!TextUtils.isEmpty(sharedPreferences.getString(tenant_id, "").toString())){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        //intent.putExtra("doc_name_from_Login", doc?.name.toString())
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            Log.d("nameFromLogin", sharedPreferences.getString(tenant_name, "").toString())
            startActivity(intent)
            finish()
        }

    }

    override fun onRestart() {
        super.onRestart()
        onStart()
    }
}

// Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = Firebase.auth.currentUser
//        val phoneNumber = Firebase.auth.currentUser?.phoneNumber
//        if (currentUser != null) {
//            Firebase.database.reference.child("Doctors_List").child(phoneNumber.toString()).addListenerForSingleValueEvent(object :ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if(snapshot.exists()){
//                        Log.d("phoneNumber", phoneNumber.toString())
//                        val doc = snapshot.getValue(doc_modal::class.java)
//                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                        intent.putExtra("doc_name_from_Login", doc?.name.toString())
//                        Log.d("nameFromLogin", doc?.name.toString())
//                        startActivity(intent)
//                        finish()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//
//            })
//
//        }
//        else{
//            Log.d("nameFromLogin", "current user is null")
//        }