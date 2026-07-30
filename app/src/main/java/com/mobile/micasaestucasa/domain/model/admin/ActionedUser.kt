package com.mobile.micasaestucasa.domain.model.admin

import com.mobile.micasaestucasa.domain.model.user.UserStatus

/**
 * Lightweight model for users that have been actioned (suspended/banned)
 * by an admin. Used in the User Management tab of the Admin Panel.
 */
data class ActionedUser(
    val id: String,
    val name: String,
    val email: String,
    val status: UserStatus,
    val propertiesOnHold: Int = 0
)
