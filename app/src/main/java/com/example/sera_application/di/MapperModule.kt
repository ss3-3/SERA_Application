package com.example.sera_application.di

import com.example.sera_application.data.mapper.EventMapper
import com.example.sera_application.data.mapper.EventMapperImpl
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
}
