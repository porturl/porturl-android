package org.friesoft.porturl.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import org.friesoft.porturl.BuildConfig
import org.friesoft.porturl.data.auth.SelfSignedConnectionBuilder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideConnectionBuilder(@ApplicationContext context: Context): ConnectionBuilder {
        return if (BuildConfig.DEBUG) {
            // Use custom builder that trusts our local Root CA in debug mode
            SelfSignedConnectionBuilder(context)
        } else {
            // Use AppAuth's standard HTTPS-only builder in production
            DefaultConnectionBuilder.INSTANCE
        }
    }

    @Provides
    @Singleton
    fun provideAppAuthConfiguration(connectionBuilder: ConnectionBuilder): AppAuthConfiguration {
        return AppAuthConfiguration.Builder()
            .setConnectionBuilder(connectionBuilder)
            .build()
    }
}
