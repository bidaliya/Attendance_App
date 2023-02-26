package com.example.dynamichealth_doctors.LoginSignUp

import `in`.aabhasjindal.otptextview.OTPListener
import `in`.aabhasjindal.otptextview.OtpTextView
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.dynamichealth_doctors.MainActivity
import com.example.dynamichealth_doctors.databinding.FragmentPhoneNumberAuthBinding
import com.example.dynamichealth_doctors.doc_modal
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class PhoneNumber_Auth : Fragment() {

    private var binding: FragmentPhoneNumberAuthBinding? = null
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var verifyOTPbutton: ExtendedFloatingActionButton
    private var verificationID: String? = null
    private var phoneAuthCredential: PhoneAuthCredential? = null
    private val SHARED_PREFS = "sharedPrefs"
    private val tenant_phone = "tenantPhone"
    private val tenant_name = "tenantName"

    private lateinit var otpTextView: OtpTextView


    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPhoneNumberAuthBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isAdded){
            otpTextView = binding!!.otpView
            progressIndicator = binding!!.progressBar
            verifyOTPbutton = binding!!.otpVerifyBTN

            verificationID = requireArguments().getString("verificationID")
            otpTextView.otpListener = object : OTPListener {
                override fun onInteractionListener() {
                    // fired when user types something in the Otpbox
                }
                override fun onOTPComplete(otp: String) {
                    // fired when user has entered the OTP fully.
                    //Toast.makeText(mContext, "The OTP is $otp", Toast.LENGTH_SHORT).show()
                    otpTextView.showSuccess()	// shows the success state to the user (can be set a bar color or drawable)

                    verifyOTPbutton.setOnClickListener{
                        progressIndicator.visibility = View.VISIBLE
                        verifyCode(otp, view)
                    }

                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun verifyCode(code: String, view: View) {
        val inputMethodManager = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        verifyOTPbutton.isEnabled = false
        phoneAuthCredential = PhoneAuthProvider.getCredential(verificationID!!, code)

        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential!!)
            .addOnCompleteListener { task ->
                verifyOTPbutton.visibility = View.VISIBLE
                if (task.isSuccessful) {
                    //val uid = FirebaseAuth.getInstance().uid
                    //Log.d("user_phone", )
                    Log.d("insidePhoneAuth-PhoneNumber", Firebase.auth.currentUser?.phoneNumber.toString())
                    FirebaseDatabase.getInstance().getReference("Doctors_List")
                        .child(Firebase.auth.currentUser?.phoneNumber.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    //doctor present in Firebase database
                                    val doc = snapshot.getValue(doc_modal::class.java)
                                    val sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString(tenant_phone, doc?.phone.toString())
                                    editor.putString(tenant_name, doc?.firstName)
//                                    editor.putString(tenant_id,Firebase.auth.currentUser?.uid )
                                    editor.apply()
                                    val intent = Intent(mContext, MainActivity::class.java)
                                    startActivity(intent)
                                    requireActivity().finish()
                                    Toast.makeText(mContext, "Hello ${doc?.firstName+doc?.lastName}", Toast.LENGTH_SHORT).show()

                                } else {
                                    //make it signup
                                    Toast.makeText(mContext, "Doctor Not exists", Toast.LENGTH_SHORT).show()
                                    val sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString(tenant_phone, "")
                                    editor.putString(tenant_name,"")
//                                    editor.putString(tenant_id,"" )
                                    editor.apply()
                                    view.findNavController().navigate(
                                        com.example.dynamichealth_doctors.R.id.action_phoneNumber_auth_to_phoneNumber
                                    )
                                }
                                progressIndicator.visibility = View.GONE
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                } else {
                    verifyOTPbutton.isEnabled = true
                    progressIndicator.visibility = View.GONE
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(mContext, "The OTP entered was invalid", Toast.LENGTH_SHORT).show()
                    }

                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}