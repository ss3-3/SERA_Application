package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.UserEntity
import com.example.sera_application.domain.model.User

/**
 * Interface for User mapping operations
 * Defines contract for converting between Entity (database) and Domain (business logic)
 */
interface UserMapper {

    /**
     * Convert UserEntity (database) to User (domain model)
     * @param entity The user entity from database
     * @return User domain model
     */
    fun toDomain(entity: UserEntity): User

    /**
     * Convert User (domain model) to UserEntity (database)
     * @param domain The user domain model
     * @return UserEntity for database storage
     */
    fun toEntity(domain: User): UserEntity

    /**
     * Convert list of UserEntity to list of User
     * @param entities List of user entities from database
     * @return List of user domain models
     */
    fun toDomainList(entities: List<UserEntity>): List<User>

    /**
     * Convert list of User to list of UserEntity
     * @param domains List of user domain models
     * @return List of user entities for database storage
     */
    fun toEntityList(domains: List<User>): List<UserEntity>
}