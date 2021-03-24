package hanas.aptacy.moodlamp.services.leadingcolors

import hanas.aptacy.moodlamp.services.leadingcolors.pojo.LeadingColorBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LeadingColorsService {
    @GET("/colors")
    suspend fun getColors(@Query("url") url: String?): List<LeadingColorBody?>?

    @POST("/wallpaper")
    suspend fun getWallpaper(@Query("batch") batch: Int,
                     @Query("x_size") height: Int,
                     @Query("y_size") width: Int,
                     @Body leadingColors: List<LeadingColorBody?>?): ResponseBody?
}