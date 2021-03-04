package hanas.aptacy.moodlamp.dagger.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import hanas.aptacy.moodlamp.MainActivity
import hanas.aptacy.moodlamp.MainActivityView
import javax.inject.Named

@Module
class MainActivityModule(private val mainActivity: MainActivity){
    @Provides
    @Named("activityContext")
    fun provideActivityContext(): Context = mainActivity

    @Provides
    @Named("applicationContext")
    fun provideApplicationContext(): Context = mainActivity.applicationContext

    @Provides
    fun provideMainActivityView(): MainActivityView = mainActivity
}