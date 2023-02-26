package com.example.dynamichealth_doctors

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class doc_modal(val firstName:String? = null, val lastName:String? =null, val address:String? = null,
                     val email:String? = null, val phone:String? = null, val date_of_birth:String? = null,val gender:String? = null ,
                     val timeOfRegistration:String? = null, val password:String? = null, val qualifications:String? = null){}
