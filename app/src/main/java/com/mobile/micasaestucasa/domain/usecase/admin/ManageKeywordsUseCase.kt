package com.mobile.micasaestucasa.domain.usecase.admin

import com.mobile.micasaestucasa.domain.model.admin.Keyword
import com.mobile.micasaestucasa.domain.model.user.UserRole
import com.mobile.micasaestucasa.domain.repository.admin.AdminRepo
import com.mobile.micasaestucasa.domain.repository.user.UserRepo
import javax.inject.Inject

class ManageKeywordsUseCase @Inject constructor(private val adminRepo: AdminRepo,
                                                private val userRepo: UserRepo
) {

    /**
     * Add a keyword to the catalogue
     * */
    suspend fun addKeyword(label: String,adminId:String): Result<String>
    {
        if(label.isBlank())
            return Result.failure(IllegalArgumentException("KEyword lalel  cannot be blank"))
        if(adminId.isBlank())
            return Result.failure(IllegalArgumentException("AdminID not valid"))
        val user=userRepo.getCurrentUser()
        if(user==null||!user.roles.contains(UserRole.ADMIN))
            return Result.failure(SecurityException("Access denied"))
        return adminRepo.addKeyword(label,adminId)
    }

    /**
     * Deletes a keyword from the catalogue
     * */
    suspend fun deleteKeyword(keywordId: String,adminId:String): Result<Unit>
    {
        if(keywordId.isBlank())
            return Result.failure(IllegalArgumentException("Keywordid not valid"))
        val user=userRepo.getCurrentUser()
        if(user==null||!user.roles.contains(UserRole.ADMIN))
            return Result.failure(SecurityException("Access denied"))
        return adminRepo.deleteKeyword(keywordId,adminId)
    }

    /**
     * gets all the keywords avalible
     * accessible to everyone , dose not requrie admin access
     * */
    suspend fun getAllKeywords(): Result<List<Keyword>> =
        adminRepo.getAllKeywords()
}