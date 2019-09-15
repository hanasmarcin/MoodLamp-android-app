package hanas.aptacy.moodlamp;

import java.util.List;

import hanas.aptacy.moodlamp.pojo.LeadingColorBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LeadingColorsService {
        @GET("/colors") // deklarujemy endpoint oraz metodę
        Call<List<LeadingColorBody>> getColors(@Query("url") String url);
}
