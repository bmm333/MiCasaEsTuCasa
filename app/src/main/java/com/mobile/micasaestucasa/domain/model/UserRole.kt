package com.mobile.micasaestucasa.domain.model

enum class UserRole{
    GUEST,OWNER,ADMIN;
    companion object{
        fun fromString(value:String):UserRole?=
            entries.find{it.name==value.uppercase()}
    }
}
