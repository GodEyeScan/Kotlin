package com.example.godeye.data.repository

import android.content.Context
import com.example.godeye.data.UserProfile
import com.example.godeye.data.database.GodEyeDatabase
import com.example.godeye.data.database.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para manejar el perfil del usuario (datos locales)
 * Estos datos NO se sincronizan con la nube
 */
class UserProfileRepository(context: Context) {

 private val userProfileDao = GodEyeDatabase.getDatabase(context).userProfileDao()

 /**
 * Obtiene el perfil del usuario desde la base de datos local
 */
 suspend fun getProfile(email: String): UserProfile? {
 return withContext(Dispatchers.IO) {
 userProfileDao.getProfile(email)?.toUserProfile()
 }
 }

 /**
 * Guarda o actualiza el perfil del usuario en la base de datos local
 */
 suspend fun saveProfile(profile: UserProfile) {
 withContext(Dispatchers.IO) {
 userProfileDao.insertOrUpdate(profile.toEntity())
 }
 }

 /**
 * Elimina el perfil del usuario
 */
 suspend fun deleteProfile(email: String) {
 withContext(Dispatchers.IO) {
 userProfileDao.delete(email)
 }
 }
}

// Extensiones para convertir entre UserProfile y UserProfileEntity
private fun UserProfile.toEntity(): UserProfileEntity {
 return UserProfileEntity(
 email = email,
 name = name,
 phone = phone,
 nit = nit
 )
}

private fun UserProfileEntity.toUserProfile(): UserProfile {
 return UserProfile(
 email = email,
 name = name,
 phone = phone,
 nit = nit
 )
}

