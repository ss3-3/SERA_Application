package com.example.sera_application.di

import com.example.sera_application.data.mapper.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MapperModule {

    @Binds
    @Singleton
    abstract fun bindEventMapper(
        eventMapperImpl: EventMapperImpl
    ): EventMapper

    @Binds
    @Singleton
    abstract fun bindUserMapper(
        userMapperImpl: UserMapperImpl
    ): UserMapper

    @Binds
    @Singleton
    abstract fun bindReservationMapper(
        reservationMapperImpl: ReservationMapperImpl
    ): ReservationMapper
}
