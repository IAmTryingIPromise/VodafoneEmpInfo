package com.example.vodafoneempinfo

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppSingletonModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideSharePointRepository(): SharePointRepository {
        return SharePointRepository()
    }

    @Provides
    @Singleton
    fun provideExcelRepository(
        client: OkHttpClient
    ): ExcelRepository {
        return ExcelRepository(
            client = client
        )
    }
}

@Module
@InstallIn(ActivityComponent::class)
object AppActivityModule {

    @Provides
    fun provideAuthRepository(
        @ActivityContext context: Context
    ): AuthRepository {
        return AuthRepository(
            context = context
        )
    }
}

@HiltAndroidApp
class VodafoneEmpInfoApp : Application()