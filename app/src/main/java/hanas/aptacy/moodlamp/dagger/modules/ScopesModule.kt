package hanas.aptacy.moodlamp.dagger.modules

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Named

@Module
class ScopesModule {
    @Provides
    @Named("IO")
    fun provideIOScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)

    @Provides
    @Named("UI")
    fun provideUIScope(): CoroutineScope = CoroutineScope(Dispatchers.Main)

}