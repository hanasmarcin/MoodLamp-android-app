package hanas.aptacy.moodlamp

import android.graphics.BitmapFactory
import hanas.aptacy.moodlamp.services.leadingcolors.LeadingColorsService
import hanas.aptacy.moodlamp.services.leadingcolors.pojo.LeadingColorBody
import hanas.aptacy.moodlamp.services.spotify.SpotifyService
import okhttp3.Dispatcher
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class MainActivityPresenter @Inject constructor(
        private val view: MainActivityView,
        private val dispatcher: Dispatcher,
        private val leadingColorsService: LeadingColorsService
) : SpotifyService.Callback {

    private var previousTrackImageUri: String? = null

    @Inject
    fun runSpotifyService(spotifyService: SpotifyService) {
        spotifyService.connect(this)
    }

    private fun getColorsSuccessful(response: Response<List<LeadingColorBody?>?>) {
        val responseBody = response.body() ?: return
        val leadingColors: List<LeadingColorBody> = responseBody.filterNotNull()
        val prominentColor: LeadingColorBody = leadingColors
                .filter { leadingColorBody: LeadingColorBody -> leadingColorBody.hsv[2] > 0.2 && leadingColorBody.hsv[2] < 0.8 }
                .maxByOrNull { leadingColorBody: LeadingColorBody -> leadingColorBody.hsv[1] }
                ?: leadingColors[0]

        view.setFabColor(prominentColor.getColorInt())
        view.setColorsOfNewAlbumCover(leadingColors)
        val screenSize: IntArray = view.screenSize ?: intArrayOf(2000, 1000)
        val callImage = leadingColorsService.getWallpaper(2000, screenSize[1], screenSize[0], leadingColors)
        callImage?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) = getWallpaperSuccessful(response)
            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) = serviceFailure(t)
        })
    }

    private fun getWallpaperSuccessful(response: Response<ResponseBody?>) {
        val responseBody = response.body() ?: return
        val bitmap = BitmapFactory.decodeStream(responseBody.byteStream())
        view.setWallpaperToActivityBackground(bitmap)
        view.setWallpaperFromBitmap(bitmap)
    }

    private fun serviceFailure(throwable: Throwable) {
        view.showLeadingColorServiceResponseError(throwable)
    }

    override fun onSpotifyConnected() = Unit

    override fun onSpotifyConnectionFailure(throwable: Throwable?) = view.showSpotifyConnectionError(throwable)

    private fun isNewWallpaperNeeded(trackImageUri: String?) = trackImageUri != previousTrackImageUri

    override fun onNewSpotifyTrack(trackName: String?, artist: String?, coverUrl: String?) {
//        if (!this::dispatcher.isInitialized || !this::leadingColorsService.isInitialized) return

        if (isNewWallpaperNeeded(coverUrl)) {
            dispatcher.cancelAll()
            view.setSongInfo(trackName, artist, coverUrl)
            val call = leadingColorsService.getColors(coverUrl)
            call?.enqueue(object : Callback<List<LeadingColorBody?>?> {
                override fun onResponse(call: Call<List<LeadingColorBody?>?>, response: Response<List<LeadingColorBody?>?>) = getColorsSuccessful(response)
                override fun onFailure(call: Call<List<LeadingColorBody?>?>, t: Throwable) = serviceFailure(t)
            })
            previousTrackImageUri = coverUrl
        }
    }
}