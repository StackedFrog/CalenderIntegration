package com.example.calenderintegration.api.googleapi

import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.calenderintegration.api.googleapi.GoogleSignIn

@Module
@InstallIn(SingletonComponent::class)


object GoogleAccountRepositoryModule {

    @Provides
    fun provideGoogleAccountRepository(): GoogleAccountRepository = GoogleAccountRepository

    @Provides
    fun provideGoogleSignIn(): GoogleSignIn = GoogleSignIn
    @Provides
    fun provideCalendarApiService(): CalendarApiService = CalendarApiService
}