package hanas.aptacy.moodlamp

import android.graphics.BitmapFactory
import hanas.aptacy.moodlamp.services.leadingcolors.LeadingColorsService
import hanas.aptacy.moodlamp.services.leadingcolors.pojo.LeadingColorBody
import hanas.aptacy.moodlamp.services.spotify.SpotifyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.ResponseBody
import javax.inject.Inject

class MainActivityPresenter @Inject constructor(
        private val view: MainActivityView,
        private val dispatcher: Dispatcher,
        private val leadingColorsService: LeadingColorsService,
        private val scope: CoroutineScope
) : SpotifyService.Callback {

    private var previousTrackImageUri: String? = null

    @Inject
    fun runSpotifyService(spotifyService: SpotifyService) {
        spotifyService.connect(this)
    }

    private suspend fun getColorsSuccessful(response: List<LeadingColorBody?>?) {
        val responseBody = response ?: return
        val leadingColors: List<LeadingColorBody> = responseBody.filterNotNull()
        val prominentColor: LeadingColorBody = leadingColors
                .filter { leadingColorBody: LeadingColorBody -> leadingColorBody.hsv[2] > 0.2 && leadingColorBody.hsv[2] < 0.8 }
                .maxByOrNull { leadingColorBody: LeadingColorBody -> leadingColorBody.hsv[1] }
                ?: leadingColors[0]

        withContext(Dispatchers.Main) {
            view.setFabColor(prominentColor.getColorInt())
            view.setColorsOfNewAlbumCover(leadingColors)
            val screenSize: IntArray = view.screenSize ?: intArrayOf(2000, 1000)
            withContext(Dispatchers.IO) {
                val response = leadingColorsService.getWallpaper(2000, screenSize[1], screenSize[0], leadingColors)
                getWallpaperSuccessful(response)
            }
        }
    }

    private suspend fun getWallpaperSuccessful(response: ResponseBody?) {
        val responseBody = response ?: return
        val bitmap = BitmapFactory.decodeStream(responseBody.byteStream())
        withContext(Dispatchers.Main) {
            view.setWallpaperToActivityBackground(bitmap)
            view.setWallpaperFromBitmap(bitmap)
        }
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
            previousTrackImageUri = coverUrl
            scope.launch {
                try {
                    val response = leadingColorsService.getColors(coverUrl)
                    getColorsSuccessful(response)
                } catch (t: Throwable) {
                    withContext(Dispatchers.Main) {
                        serviceFailure(t)
                    }
                }
            }
        }
    }
}