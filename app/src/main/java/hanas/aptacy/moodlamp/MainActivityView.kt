package hanas.aptacy.moodlamp

import android.graphics.Bitmap
import hanas.aptacy.moodlamp.services.leadingcolors.pojo.LeadingColorBody

interface MainActivityView {
    fun setSongInfo(title: String?, artistName: String?, imageURLStr: String?)
    fun setColorsOfNewAlbumCover(leadingColors: List<LeadingColorBody>)
    fun showSpotifyConnectionError(throwable: Throwable?): Unit
    fun showLeadingColorServiceResponseError(t: Throwable?)
    fun setWallpaperToActivityBackground(bitmap: Bitmap?)
    fun setWallpaperFromBitmap(bitmap: Bitmap?)
    val screenSize: IntArray?
    var wallpaperSp: Bitmap?
    fun setFabColor(prominentColor: Int)
}