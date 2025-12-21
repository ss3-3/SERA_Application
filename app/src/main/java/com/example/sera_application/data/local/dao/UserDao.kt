package com.example.sera_application.data.local.dao

import androidx.room.*
import com.example.sera_application.data.local.UserGrowthData
import com.example.sera_application.data.local.entity.UserEntity
import com.example.sera_application.domain.usecase.report.UserGrowthTrend

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    // Add
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUserCount(): Int

    // Add
    @Query("""
        SELECT COUNT(*) FROM users 
        WHERE createdAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getUsersCreatedBetween(startDate: Long, endDate: Long): Int

    // Add
    @Query("""
        SELECT 
            date(createdAt/1000, 'unixepoch') as date,
            COUNT(*) as newUsers,
            (SELECT COUNT(*) FROM users u2 WHERE u2.createdAt <= u1.createdAt) as totalUsers
        FROM users u1
        WHERE createdAt >= :startDate
        GROUP BY date
        ORDER BY date
        LIMIT :days
    """)
    suspend fun getUserGrowthTrend(days: Int, startDate: Long): List<UserGrowthData>

    @Query("""
        SELECT 
            CAST(strftime('%d', datetime(createdAt/1000, 'unixepoch')) AS INTEGER) / 5 * 5 as period,
            COUNT(*) as count
        FROM users
        WHERE createdAt >= :startDate
        GROUP BY period
        ORDER BY period
    """)
    suspend fun getMonthlyUserGrowthTrend(startDate: Long): List<MonthlyGrowthData>

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

data class MonthlyGrowthData(
    val period: Int,
    val count: Int
)