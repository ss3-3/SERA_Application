package com.example.sera_application.data.repository

import com.example.sera_application.data.local.UserGrowthData
import com.example.sera_application.data.local.dao.UserDao
import com.example.sera_application.data.mapper.UserMapper
import com.example.sera_application.data.remote.datasource.UserRemoteDataSource
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Implementation of UserRepository.
 * Coordinates user operations between remote datasource, local database, and domain layer.
 */
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val userDao: UserDao,
    private val mapper: UserMapper
) : UserRepository {

    override suspend fun getUserById(userId: String): User? {
        return try {
            // Try local first
            val localEntity = userDao.getUserById(userId)
            if (localEntity != null) {
                return mapper.toDomain(localEntity)
            }
            
            // Fetch from remote if not in local cache
            remoteDataSource.getUserProfile(userId)?.also { user ->
                // Cache locally
                userDao.insertUser(mapper.toEntity(user))
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            val remoteUsers = remoteDataSource.getAllUsers()
            
            // Cache locally
            if (remoteUsers.isNotEmpty()) {
                userDao.insertUsers(mapper.toEntityList(remoteUsers))
            }
            
            remoteUsers
        } catch (e: Exception) {
            // Fallback to local cache on error
            val localEntities = userDao.getAllUsers()
            mapper.toDomainList(localEntities)
        }
    }

    override suspend fun getPendingOrganizers(): List<User> {
        return try {
            remoteDataSource.getPendingOrganizers()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateUser(user: User): Boolean {
        return try {
            remoteDataSource.updateUserProfile(user)
            userDao.insertUser(mapper.toEntity(user))
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteUser(userId: String): Boolean {
        return try {
            // 1. Delete from remote
            remoteDataSource.deleteUser(userId)
            
            // 2. Delete from local
            val entity = userDao.getUserById(userId)
            entity?.let {
                userDao.deleteUser(it)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun approveOrganizer(userId: String): Boolean {
        return try {
            remoteDataSource.updateApprovalStatus(userId, true)
            // Refresh user from remote to get updated approvalStatus and approvedAt
            val user = remoteDataSource.getUserProfile(userId)
            if (user != null) {
                // Use the user data from Firestore which already has approvalStatus and approvedAt set
                userDao.insertUser(mapper.toEntity(user))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun rejectOrganizer(userId: String): Boolean {
        return try {
            remoteDataSource.updateApprovalStatus(userId, false)
            val user = getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(isApproved = false, accountStatus = "REJECTED")
                userDao.insertUser(mapper.toEntity(updatedUser))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun suspendUser(userId: String): Boolean {
        return try {
            val user = getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(accountStatus = "SUSPENDED")
                // Update both remote and local
                remoteDataSource.updateUserProfile(updatedUser)
                userDao.insertUser(mapper.toEntity(updatedUser))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun activateUser(userId: String): Boolean {
        return try {
            val user = getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(accountStatus = "ACTIVE")
                // Update both remote and local
                remoteDataSource.updateUserProfile(updatedUser)
                userDao.insertUser(mapper.toEntity(updatedUser))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


    // Add
    override suspend fun getTotalUserCount(): Int {
        return try {
            // Fetch all users from Firebase
            val remoteUsers = remoteDataSource.getAllUsers()
            
            // Sync to local database for caching
            if (remoteUsers.isNotEmpty()) {
                userDao.insertUsers(mapper.toEntityList(remoteUsers))
            }
            
            // Return actual count from Firebase data (source of truth)
            remoteUsers.size
        } catch (e: Exception) {
            // Fallback to local count if Firebase fetch fails
            android.util.Log.e("UserRepositoryImpl", "Error getting user count from Firebase: ${e.message}", e)
            try {
                userDao.getTotalUserCount()
            } catch (localException: Exception) {
                android.util.Log.e("UserRepositoryImpl", "Error getting user count from Room: ${localException.message}", localException)
                0
            }
        }
    }

    override suspend fun getUsersCreatedBetween(startDate: Long, endDate: Long): Int {
        return try {
            userDao.getUsersCreatedBetween(startDate, endDate)
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getUserGrowthTrend(days: Int, startDate: Long): List<UserGrowthData> {
        return try {
            userDao.getUserGrowthTrend(days, startDate)
        } catch (e: Exception) {
            List(days) { UserGrowthData(it.toString(), 0, 0) }
        }
    }

    override suspend fun getMonthlyUserGrowthTrend(startDate: Long): List<Int> {
        return try {
            val monthlyData = userDao.getMonthlyUserGrowthTrend(startDate)
            monthlyData.map { it.count }
        } catch (e: Exception) {
            List(12) { 0 }
        }
    }
}