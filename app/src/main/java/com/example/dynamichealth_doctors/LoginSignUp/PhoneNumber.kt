package com.example.dynamichealth_doctors.LoginSignUp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.dynamichealth_doctors.MainActivity
import com.example.dynamichealth_doctors.databinding.FragmentPhoneNumberBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit


class PhoneNumber : Fragment() {
    // TODO: Rename and change types of parameters

    private var binding: FragmentPhoneNumberBinding? = null

    private lateinit var login_btn: ExtendedFloatingActionButton
    private lateinit var phone_TIL: TextInputLayout
    private lateinit var phone_TIEDT: TextInputEditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var phone_number_entered: String
    private lateinit var mCallbacks: OnVerificationStateChangedCallbacks

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPhoneNumberBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phone_TIL = binding!!.phoneLoginTIL
        phone_TIEDT = binding!!.phoneLoginTIEDT
        login_btn = binding!!.loginButton
        progressIndicator = binding!!.progressBar
        mAuth = FirebaseAuth.getInstance()
        //mUser = mAuth.currentUser!!
        progressIndicator.visibility = View.VISIBLE

        if (isAdded) {
            progressIndicator.visibility = View.GONE
            mCallbacks = object : OnVerificationStateChangedCallbacks() {

                @SuppressLint("LongLogTag")
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    Log.d("On Verification Completed", "onVerificationCompleted:$credential")
                    //signInWithPhoneAuthCredential(credential)
                    progressIndicator.visibility = View.GONE
                }

                @SuppressLint("LongLogTag")
                override fun onVerificationFailed(e: FirebaseException) {
                    Log.w("On Verification Completed", "onVerificationFailed", e)
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        Toast.makeText(mContext, "Invalid Request", Toast.LENGTH_SHORT).show()
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        Toast.makeText(
                            mContext,
                            "The SMS quota for the project has been exceeded",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    progressIndicator.visibility = View.GONE
                }

                override fun onCodeSent(
                    verificationID: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    // When the code is send to user, we get a verification id...now in the next step, we will send the code(from user) and the
                    // verification id to the firebase to get the authentication done.
                    Toast.makeText(context, "Code Send", Toast.LENGTH_SHORT).show()

                    val bundle = Bundle()
                    bundle.putString("verificationID", verificationID)
                    view.findNavController().navigate(
                        com.example.dynamichealth_doctors.R.id.action_phoneNumber_to_phoneNumber_auth,
                        bundle
                    )
                }
            }
            login_btn.setOnClickListener {
                progressIndicator.visibility = View.VISIBLE
                phone_TIEDT.isCursorVisible = false
                val phone_number_entered = phone_TIEDT.text.toString()
                // valid phone number
                Log.d("phone number entered", "" + phone_number_entered)

                if (!TextUtils.isEmpty(phone_number_entered) && phone_number_entered.length == 10) {
                    val options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91".plus(phone_number_entered))       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(requireActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                } else {
                    Toast.makeText(context, "Please Enter valid Phone number", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

//    private fun Activity.hideKeyboard() {
//        hideKeyboard(currentFocus ?: View(this))
//    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}