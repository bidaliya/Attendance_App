
package com.example.dynamichealth_doctors

import android.R.attr.bitmap
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media.insertImage
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class ConfirmationDialog : DialogFragment() {

    private lateinit var doc_name: TextView
    private lateinit var attendance_date: TextView
    private lateinit var attendance_timings: TextView
    private lateinit var doc_location: TextView
    private lateinit var doc_sign: ImageView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var confirm_btn: ExtendedFloatingActionButton
    private lateinit var cancel_btn: ImageButton

    private var mContext: Context? = null

    private val SHARED_PREFS = "sharedPrefs"
    private val tenant_phone = "tenantPhone"
    private val tenant_name = "tenantName"
    private val tenant_id = "tenantId"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner)
        val rootView = inflater.inflate(R.layout.confirmation_dialog, container, false)
        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && mContext != null) {
            operation(mContext!!)
        }
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        if (isAdded) {
            checkIfFragmentAttached {
                doc_name = rootView.findViewById(R.id.doctor_name)
                attendance_date = rootView.findViewById(R.id.doctor_attendance_date)
                attendance_timings = rootView.findViewById(R.id.doctor_timing)
                doc_location = rootView.findViewById(R.id.doctor_location)
                doc_sign = rootView.findViewById(R.id.doctor_sign)
                progressIndicator = rootView.findViewById(R.id.attendance_upload_progressbar)
                confirm_btn = rootView.findViewById(R.id.confirm_btn)
                cancel_btn = rootView.findViewById(R.id.cancel_button)

                val bundle = arguments
                val address = bundle?.getString("address", "")
                val name = bundle?.getString("name", "")
                val timimg = bundle?.getString("timing", "")
                val doc_sign_bitmap = bundle?.getParcelable<Bitmap>("doc_sign_bitmap")
                val doc_id = bundle?.getString("id")

                //var looper = Looper.getMainLooper()
                val date = getCurrentDateTime()
                doc_name.text = name
                attendance_date.text = SimpleDateFormat("dd-MM-yyyy", Locale("IN")).format(date)
                attendance_timings.text =
                    SimpleDateFormat("HH:mm", Locale("IN")).format(date).plus("(").plus(timimg)
                        .plus(")")
                doc_location.text = address
                doc_sign.setImageBitmap(doc_sign_bitmap)

                cancel_btn.setOnClickListener {
                    dismiss()
                }

                confirm_btn.setOnClickListener {
                    progressIndicator.visibility = View.VISIBLE
                    val doc_attendance = Doc_attendance_modal(
                        attendance_date.text.toString(),
                        address,
                        SimpleDateFormat("HH:mm", Locale("IN")).format(date),
                        timimg
                    )

                    if (timimg == "IN") {
                        Firebase.database.reference.child("Attendance").child(doc_id.toString())
                            .child(attendance_date.text.toString()).child("IN")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        // attendance already exists for the particular date
                                        Toast.makeText(
                                            this@checkIfFragmentAttached,
                                            "IN Attendance already taken",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        progressIndicator.visibility = View.INVISIBLE
                                        dismiss()
                                    } else {
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            Firebase.database.reference.child("Attendance")
                                                .child(doc_id!!)
                                                .child(attendance_date.text.toString())
                                                .child("IN")
                                                .setValue(doc_attendance).await()
                                            val uri = getImageUri(this@checkIfFragmentAttached,
                                                doc_sign_bitmap!!
                                            )
                                            Firebase.storage.reference
                                                .child("Doctors_Attendance/$doc_id/${attendance_date.text}/IN/doc_sign.jpg")
                                                .putBytes(uri, storageMetadata {
                                                    contentType = "${address}"
                                                }).await()

                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@checkIfFragmentAttached,
                                                    "Data Inserted",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                progressIndicator.visibility = View.INVISIBLE
                                                dismiss()
                                            }
                                        }

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d("exception1", error.toString())
                                }

                            })

                    } else {
                        Firebase.database.reference.child("Attendance").child(doc_id!!)
                            .child(attendance_date.text.toString()).child("OUT")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        // attendance already exists for the particular date and OUT
                                        Toast.makeText(
                                            this@checkIfFragmentAttached,
                                            "OUT Attendance already taken",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        progressIndicator.visibility = View.INVISIBLE
                                        dismiss()
                                    } else {
                                        lifecycleScope.launch(Dispatchers.IO) {

                                            Firebase.database.reference.child("Attendance")
                                                .child(doc_id)
                                                .child(attendance_date.text.toString())
                                                .child("OUT")
                                                .setValue(doc_attendance).await()

                                            val uri = getImageUri(this@checkIfFragmentAttached,
                                                doc_sign_bitmap!!
                                            )
                                            Firebase.storage.reference
                                                .child("Doctors_Attendance/$doc_id/${attendance_date.text}/OUT/doc_sign.jpg")
                                                .putBytes(uri, storageMetadata {
                                                    contentType = "${address}"
                                                }).await()

                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@checkIfFragmentAttached,
                                                    "Data Inserted",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                progressIndicator.visibility = View.INVISIBLE
                                                dismiss()
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d("exception2", error.toString())
                                }

                            })
                    }



                }
            }


        }

    }

    fun getImageUri(inContext: Context, inImage: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }


    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDetach() {
        super.onDetach()
    }
}

