package hanas.aptacy.moodlamp.services.pojo;

import android.graphics.Color;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LeadingColorBody {

    @SerializedName("color")
    @Expose
    private List<Integer> color = null;
    @SerializedName("pixel_count")
    @Expose
    private Integer pixelCount;

    public int getColor() {

        return  Color.rgb(color.get(0), color.get(1), color.get(2));
    }

    public float[] getHSV() {
        float[] hsv = new float[3];;
        Color.RGBToHSV(color.get(0), color.get(1), color.get(2), hsv);
        return hsv;
    }

    public void setColor(List<Integer> color) {
        this.color = color;
    }

    public Integer getPixelCount() {
        return pixelCount;
    }

    public void setPixelCount(Integer pixelCount) {
        this.pixelCount = pixelCount;
    }

}