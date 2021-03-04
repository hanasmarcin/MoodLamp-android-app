package hanas.aptacy.moodlamp.dagger.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import hanas.aptacy.moodlamp.services.spotify.SpotifyService
import hanas.aptacy.moodlamp.services.spotify.SpotifyServiceImpl
import javax.inject.Named

@Module(includes = [MainActivityModule::class])
class SpotifyModule() {
    @Provides
    fun provideSpotifyService(@Named("activityContext") context: Context?): SpotifyService = SpotifyServiceImpl(context)
}