package com.example.sera_application.data.local.dao

import androidx.room.*
import com.example.sera_application.data.local.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun getUsersByRole(role: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("UPDATE users SET accountStatus = :status WHERE userId = :userId")
    suspend fun updateUserStatus(userId: String, status: String)

    @Query("UPDATE users SET role = :role WHERE userId = :userId")
    suspend fun updateUserRole(userId: String, role: String)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}