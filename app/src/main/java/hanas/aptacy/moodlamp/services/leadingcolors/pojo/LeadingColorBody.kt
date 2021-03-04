package hanas.aptacy.moodlamp.services.leadingcolors.pojo

import android.graphics.Color
import com.google.gson.annotations.SerializedName

data class LeadingColorBody(
        @SerializedName("color") var color: List<Int>,
        @SerializedName("pixel_count") var pixelCount: Int
) {
    fun getColorInt(): Int {
        return Color.rgb(color[0], color[1], color[2])
    }

    val hsv: FloatArray
        get() {
            val hsv = FloatArray(3)
            Color.RGBToHSV(color[0], color[1], color[2], hsv)
            return hsv
        }
}