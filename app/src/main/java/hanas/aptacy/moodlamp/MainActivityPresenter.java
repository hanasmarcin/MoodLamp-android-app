package hanas.aptacy.moodlamp;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hanas.aptacy.moodlamp.services.LeadingColorsService;
import hanas.aptacy.moodlamp.services.pojo.LeadingColorBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivityPresenter {

    private static final String LEADING_COLORS_URL = "https://moodlamp-leading-color.herokuapp.com";
    private static final String CLIENT_ID = "6d7d697321a74a419a8ede8b74c6a7c8";
    private static final String REDIRECT_URI = "https://trello.com/b/zYzvg43L/moodlamp";

    private SpotifyAppRemote spotifyAppRemote;
    private LeadingColorsService leadingColorsService;
    private Track previousTrack;

    private MainActivityView view;

    public MainActivityPresenter(MainActivityView view) {
        this.view = view;
    }

    public void connectWithLeadingColorsService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        httpClient.addInterceptor(logging);  // <-- this is the important line!

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LEADING_COLORS_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        leadingColorsService = retrofit.create(LeadingColorsService.class);
    }

    private class SpotifyConnectionListener implements Connector.ConnectionListener {
        @Override
        public void onConnected(SpotifyAppRemote mSpotifyAppRemote) {
            spotifyAppRemote = mSpotifyAppRemote;
            Log.d("MainActivity", "Connected! Yay!");

            // Now you can start interacting with App Remote
            spotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState()
                    .setEventCallback(MainActivityPresenter.this::newSpotifySong);
        }

        @Override
        public void onFailure(Throwable throwable) {
            view.showSpotifyConnectionError(throwable);
            Log.e("MainActivity", throwable.getMessage(), throwable);

            // Something went wrong when attempting to connect! Handle errors here
        }
    }

    public void connectWithSpotifyAppRemote() {
        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        view.connectWithSpotify(connectionParams, new SpotifyConnectionListener());
    }


    private void newSpotifySong(PlayerState playerState) {
        final Track track = playerState.track;
        if (track != null && (previousTrack == null || !track.imageUri.equals(previousTrack.imageUri))) {
            String imageUri = track.imageUri.raw;
            String imageId = imageUri.substring(imageUri.lastIndexOf(":") + 1);
            String imageURLString = "https://i.scdn.co/image/" + imageId;
            view.setSongInfo(track.name, track.artist.name, imageURLString);
            Call<List<LeadingColorBody>> call = leadingColorsService.getColors(imageURLString);
            call.enqueue(new Callback<List<LeadingColorBody>>() {
                @Override
                public void onResponse(Call<List<LeadingColorBody>> call, Response<List<LeadingColorBody>> response) {
                    List<LeadingColorBody> leadingColors = response.body();
                    view.setColorsOfNewAlbumCover(leadingColors);
                    int[] screenSize = view.getScreenSize();
                    Call<ResponseBody> callImage = leadingColorsService.getWallpaper(10000, screenSize[1], screenSize[0], leadingColors);
                    callImage.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            InputStream is = response.body().byteStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            view.setWallpaperFromBitmap(bitmap);
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            view.showLeadingColorServiceResponseError(t);
                            Log.e("API failure", t.getMessage(), t);
                        }
                    });
                    Log.d("API response", response.toString());
                }

                @Override
                public void onFailure(Call<List<LeadingColorBody>> call, Throwable t) {
                    view.showLeadingColorServiceResponseError(t);
                    Log.e("API failure", t.getMessage(), t);
                }
            });
            previousTrack = track;
        }
    }
}
