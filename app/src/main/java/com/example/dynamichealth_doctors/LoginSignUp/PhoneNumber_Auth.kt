package com.example.dynamichealth_doctors.LoginSignUp

import `in`.aabhasjindal.otptextview.OTPListener
import `in`.aabhasjindal.otptextview.OtpTextView
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
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
    private val tenant_id = "tenantId"

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
                    otpTextView.showSuccess();	// shows the success state to the user (can be set a bar color or drawable)

                    verifyOTPbutton.setOnClickListener{
                        progressIndicator.visibility = View.VISIBLE
                        verifyCode(otp, view)
                    }

                }
            }
        }
    }

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
                    FirebaseDatabase.getInstance().getReference("Doctors_List")
                        .child(Firebase.auth.currentUser?.phoneNumber.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    //doctor present in Firebase database
                                    val doc = snapshot.getValue(doc_modal::class.java)
                                    val sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString(tenant_phone, Firebase.auth.currentUser?.phoneNumber.toString())
                                    editor.putString(tenant_name, doc?.name)
                                    editor.putString(tenant_id,Firebase.auth.currentUser?.uid )
                                    editor.apply()
                                    val intent = Intent(mContext, MainActivity::class.java)
//                                    intent.putExtra("doc_name",doc?.name.toString() )
//                                    intent.putExtra("doc_uid", snapshot.key.toString())
                                    startActivity(intent)
                                    requireActivity().finish()
                                    Toast.makeText(mContext, "Hello ${doc?.name}", Toast.LENGTH_SHORT).show()

                                } else {
                                    //make it signup
                                    Toast.makeText(mContext, "Doctor Not exists", Toast.LENGTH_SHORT).show()
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

//    private fun setListener(view: View){
//        view.setOnClickListener {
//            val inputMethodManager = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
//        }
//
//        setTextChangeListener(fromEditText = inputcode1, targetEditText = inputcode2 )
//        setTextChangeListener(fromEditText = inputcode2, targetEditText = inputcode3 )
//        setTextChangeListener(fromEditText = inputcode3, targetEditText = inputcode4 )
//        setTextChangeListener(fromEditText = inputcode4, targetEditText = inputcode5 )
//        setTextChangeListener(fromEditText = inputcode5, targetEditText = inputcode6 )
//
//        setKeyListener(fromEditText = inputcode6, backToEditText = inputcode5)
//        setKeyListener(fromEditText = inputcode5, backToEditText = inputcode4)
//        setKeyListener(fromEditText = inputcode4, backToEditText = inputcode3)
//        setKeyListener(fromEditText = inputcode3, backToEditText = inputcode2)
//        setKeyListener(fromEditText = inputcode2, backToEditText = inputcode1)
//
//    }
//    private fun initFocus(){
//        inputcode1?.isEnabled = true
//
//        inputcode1?.postDelayed({
//            inputcode1?.requestFocus()
//            val inputMethodManager = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodManager.showSoftInput(inputcode1, 0)
//        },500)
//
//    }

//    private fun reset(){
//        inputcode1?.isEnabled = false
//        inputcode2?.isEnabled = false
//        inputcode3?.isEnabled = false
//        inputcode4?.isEnabled = false
//        inputcode5?.isEnabled = false
//        inputcode6?.isEnabled = false
//
//        inputcode1?.setText("")
//        inputcode2?.setText("")
//        inputcode3?.setText("")
//        inputcode4?.setText("")
//        inputcode5?.setText("")
//        inputcode6?.setText("")
//
//        initFocus()
//
//    }

//    private fun setTextChangeListener(
//        fromEditText:EditText? = null,
//        targetEditText:EditText? = null,
//
//    ){
//        fromEditText?.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//                if (!charSequence.toString().trim { it <= ' ' }.isEmpty()) {
//                    targetEditText?.let{editText->
//                        editText.isEnabled = true
//                        editText.requestFocus()
//                    }
//                    fromEditText.clearFocus()
//                    fromEditText.isEnabled = false
//                }
//            }
//            override fun afterTextChanged(editable: Editable) {}
//        })
//    }
//
//    private fun setKeyListener(fromEditText: EditText? = null, backToEditText:EditText? = null){
//        fromEditText?.setOnKeyListener { view: View?, keyCode: Int, event ->
//            if(keyCode == KeyEvent.KEYCODE_DEL){
//                backToEditText?.isEnabled  = true
//                backToEditText?.requestFocus()
//                backToEditText?.setText("")
//                fromEditText.clearFocus()
//                fromEditText.isEnabled = false
//            }
//            false
//        }
//
//    }



//    private fun setotpinputs() {
//        inputcode1?.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//                if (!charSequence.toString().trim { it <= ' ' }.isEmpty()) {
//                    inputcode2?.requestFocus()
//                }
//            }
//
//            override fun afterTextChanged(editable: Editable) {}
//        })
//        inputcode2?.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//                if (!charSequence.toString().trim { it <= ' ' }.isEmpty()) {
//                    inputcode3?.requestFocus()
//                }
//            }
//
//            override fun afterTextChanged(editable: Editable) {}
//        })
//        inputcode2?.setOnKeyListener(View.OnKeyListener { view: View?, keyCode: Int, keyEvent: KeyEvent? ->
//            if (keyCode == KeyEvent.KEYCODE_DEL) {
//                inputcode1?.requestFocus()
//            }
//            false
//        })
//        inputcode3?.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//                if (!charSequence.toString().trim { it <= ' ' }.isEmpty()) {
//                    inputcode4?.requestFocus()
//                }
//            }
//
//            override fun afterTextChanged(editable: Editable) {}
//        })
//        inputcode3?.setOnKeyListener(View.OnKeyListener { view: View?, keyCode: Int, keyEvent: KeyEvent? ->
//            if (keyCode == KeyEvent.KEYCODE_DEL) {
//                inputcode2?.requestFocus()
//            }
//            false
//        })
//        inputcode4?.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//                if (!charSequence.toString().trim { it <= ' ' }.isEmpty()) {
//                    inputcode5?.requestFocus()
//                }
//            }
//            override fun afterTextChanged(editable: Editable) {}
//        })
//        inputcode4?.setOnKeyListener(View.OnKeyListener { view: View?, keyCode: Int, keyEvent: KeyEvent? ->
//            if (keyCode == KeyEvent.KEYCODE_DEL) {
//                inputcode3?.requestFocus()
//            }
//            false
//        })
//        inputcode5?.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//                if (!charSequence.toString().trim { it <= ' ' }.isEmpty()) {
//                    inputcode6?.requestFocus()
//                }
//            }
//            override fun afterTextChanged(editable: Editable) {}
//        })
//        inputcode5?.setOnKeyListener{ view: View?, keyCode: Int, keyEvent: KeyEvent? ->
//            if (keyCode == KeyEvent.KEYCODE_DEL) {
//                inputcode4?.requestFocus()
//            }
//            false
//        }
//        inputcode6?.setOnKeyListener { view: View?, keyCode: Int, keyEvent: KeyEvent? ->
//            if (keyCode == KeyEvent.KEYCODE_DEL) {
//                inputcode5?.requestFocus()
//            }
//            false
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}