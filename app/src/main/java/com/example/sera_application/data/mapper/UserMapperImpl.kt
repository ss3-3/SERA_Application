package com.example.sera_application.data.mapper
//
//import com.example.sera_application.data.local.entity.UserEntity
//import com.example.sera_application.domain.model.User
//import com.example.sera_application.domain.model.enums.UserRole
//
//object UserMapperImpl : UserMapper {
//
//    override fun toDomain(entity: UserEntity): User {
//        return User(
//            userId = entity.userId,
//            fullName = entity.fullName,
//            email = entity.email,
//            phone = entity.phone,
//            role = try {
//                UserRole.valueOf(entity.role)
//            } catch (e: Exception) {
//                UserRole.PARTICIPANT // Default fallback
//            },
//            profileImagePath = entity.profileImagePath,
//            accountStatus = entity.accountStatus,
//            createdAt = entity.createdAt,
//            updatedAt = entity.updatedAt
//        )
//    }
//
//    override fun toEntity(domain: User): UserEntity {
//        return UserEntity(
//            userId = domain.userId,
//            fullName = domain.fullName,
//            email = domain.email,
//            phone = domain.phone,
//            role = domain.role.name,
//            profileImagePath = domain.profileImagePath,
//            accountStatus = domain.accountStatus,
//            createdAt = domain.createdAt,
//            updatedAt = domain.updatedAt
//        )
//    }
//
//    override fun toDomainList(entities: List<UserEntity>): List<User> {
//        return entities.map { toDomain(it) }
//    }
//
//    override fun toEntityList(domains: List<User>): List<UserEntity> {
//        return domains.map { toEntity(it) }
//    }
//}