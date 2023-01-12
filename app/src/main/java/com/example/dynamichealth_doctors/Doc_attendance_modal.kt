package com.example.dynamichealth_doctors

import android.net.Uri

data class Doc_attendance_modal(
    val date:String? = null,
    val location:String? = null,
    val time:String? = null,
    val timing:String? = null)
{
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
