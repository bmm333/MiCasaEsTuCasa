package com.mobile.micasaestucasa.domain.model.admin

/**
 * Used to filter properties,
 * admin exclusive users can only read
 * */
data class Keyword(
    val id: String = "",
    val label: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
