package hanas.aptacy.moodlamp.dagger.modules

import dagger.Module
import dagger.Provides
import hanas.aptacy.moodlamp.services.leadingcolors.LeadingColorsService
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
class LeadingColorsServiceModule {


    companion object {
        private const val LEADING_COLORS_URL = "https://spotify-color-wallpapers-oxle3cwiga-ey.a.run.app" //"https://moodlamp-leading-color.herokuapp.com";
    }

    private val dispatcher = Dispatcher()

    @Provides
    fun provideDispatcher(): Dispatcher = dispatcher

    @Provides
    fun provideService(): LeadingColorsService {
        val httpClient = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .dispatcher(dispatcher)

        val retrofit = Retrofit.Builder()
                .baseUrl(LEADING_COLORS_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
        val service = retrofit.create(LeadingColorsService::class.java)
        return service
    }
}