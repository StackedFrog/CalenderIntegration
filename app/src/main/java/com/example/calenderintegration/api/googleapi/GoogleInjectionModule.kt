package com.example.calenderintegration.api.googleapi

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleAccountRepositoryModule {

    @Provides
    @Singleton
    fun provideGoogleAccountRepository(): GoogleAccountRepository = GoogleAccountRepository

    @Provides
    @Singleton
    fun provideGoogleSignIn(): GoogleSignIn = GoogleSignIn

    @Provides
    @Singleton
    fun provideCalendarApiService(): CalendarApiService = CalendarApiService
}
