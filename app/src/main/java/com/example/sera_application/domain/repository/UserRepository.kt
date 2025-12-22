package com.example.sera_application.domain.repository

import com.example.sera_application.data.local.UserGrowthData
import com.example.sera_application.domain.model.User

interface UserRepository {

    suspend fun getUserById(userId: String): User?

    suspend fun getAllUsers(): List<User>

    suspend fun getPendingOrganizers(): List<User>

    suspend fun updateUser(user: User): Boolean

    suspend fun deleteUser(userId: String): Boolean

    suspend fun approveOrganizer(userId: String): Boolean

    suspend fun rejectOrganizer(userId: String): Boolean

    suspend fun suspendUser(userId: String): Boolean

    suspend fun activateUser(userId: String): Boolean


    // Add
    suspend fun getTotalUserCount(): Int
    suspend fun getUsersCreatedBetween(startDate: Long, endDate: Long): Int
    suspend fun getUserGrowthTrend(days: Int, startDate: Long): List<UserGrowthData>
    suspend fun getMonthlyUserGrowthTrend(startDate: Long): List<Int>
}