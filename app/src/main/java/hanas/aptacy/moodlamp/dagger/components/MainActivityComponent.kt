package hanas.aptacy.moodlamp.dagger.components

import dagger.Component
import hanas.aptacy.moodlamp.MainActivity
import hanas.aptacy.moodlamp.dagger.modules.LeadingColorsServiceModule
import hanas.aptacy.moodlamp.dagger.modules.ScopesModule
import hanas.aptacy.moodlamp.dagger.modules.SpotifyModule

@Component(modules = [SpotifyModule::class, LeadingColorsServiceModule::class, ScopesModule::class])
interface MainActivityComponent {
    fun inject(activity: MainActivity)
}