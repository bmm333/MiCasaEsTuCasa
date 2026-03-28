package com.mobile.micasaestucasa.data.dto

data class UserDTO(
    val id:String?=null,
    val name:String?=null,
    val email:String?=null,
    val roles:List<String>?=null
)
