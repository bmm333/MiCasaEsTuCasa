package com.mobile.micasaestucasa.domain.model.admin


/**
 * Bookign statistics , calculated client side
 * from a firestore query on all booking.
 * */
data class BookingStats(

    val total:Int=0,
    val completed:Int=0,
    val active:Int=0,
    val pending:Int=0,
    val cancelled:Int=0,
    val rejected:Int=0,
)
