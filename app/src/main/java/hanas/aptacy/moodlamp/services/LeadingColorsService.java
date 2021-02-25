package hanas.aptacy.moodlamp.services;

import java.util.List;

import hanas.aptacy.moodlamp.services.pojo.LeadingColorBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LeadingColorsService {
        @GET("/colors") // deklarujemy endpoint oraz metodę
        Call<List<LeadingColorBody>> getColors(@Query("url") String url);

        @POST("/wallpaper") // deklarujemy endpoint oraz metodę
        Call<ResponseBody> getWallpaper(@Query("batch") int batch,
                                        @Query("x_size") int height,
                                        @Query("y_size") int width,
                                        @Body List<LeadingColorBody> leadingColors);

}
