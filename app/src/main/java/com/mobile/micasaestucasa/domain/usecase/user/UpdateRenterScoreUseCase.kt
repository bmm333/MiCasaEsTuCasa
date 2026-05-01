package com.mobile.micasaestucasa.domain.usecase.user

import com.mobile.micasaestucasa.domain.repository.review.ReviewRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class UpdateRenterScoreUseCase @Inject constructor(private val userRepo: UserRepo,private val reviewRepo: ReviewRepo) {

}