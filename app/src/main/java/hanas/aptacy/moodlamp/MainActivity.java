package hanas.aptacy.moodlamp;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import hanas.aptacy.moodlamp.pojo.LeadingColorBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    private static final String LEADING_COLORS_URL = "https://moodlamp-leading-color.herokuapp.com";
    private static final String CLIENT_ID = "6d7d697321a74a419a8ede8b74c6a7c8";
    private static final String REDIRECT_URI = "https://trello.com/b/zYzvg43L/moodlamp";
    private SpotifyAppRemote mSpotifyAppRemote;
    private LinearLayout colorFrame;
    private FrameLayout[] colorViews;

    private ImageView imageView;
    Retrofit retrofit;
    private LeadingColorsService leadingColorsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.imageView);
        colorFrame = findViewById(R.id.color_frame);
        colorViews = new FrameLayout[]{
                findViewById(R.id.color_1),
                findViewById(R.id.color_2),
                findViewById(R.id.color_3)
        };
//        FloatingActionButton fab = findViewById(R.id.fab);
//        setFloatingActionButton(fab);


    }

//    private void setFloatingActionButton(FloatingActionButton fab){
//        fab.setOnClickListener(view -> {
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
//        });
//    }

    @Override
    protected void onStart() {
        super.onStart();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
// add your other interceptors …

// add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LEADING_COLORS_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        leadingColorsService = retrofit.create(LeadingColorsService.class);

        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Toast.makeText(MainActivity.this, "Can't connect :'(", Toast.LENGTH_SHORT).show();
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });

    }

    Track previousTrack = null;

    private void connected() {
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null && (previousTrack == null || !track.imageUri.equals(previousTrack.imageUri))) {
                        String imageUri = track.imageUri.raw;
                        String imageId = imageUri.substring(imageUri.lastIndexOf(":")+1);
                        String imageURLString = "https://i.scdn.co/image/" + imageId;
                        Picasso.with(this).load(imageURLString).into(imageView);
                        Log.d("xd", leadingColorsService.toString());
                        Call<List<LeadingColorBody>> call = leadingColorsService.getColors(imageURLString);
                        call.enqueue(new Callback<List<LeadingColorBody>>() {
                            @Override
                            public void onResponse(Call<List<LeadingColorBody>> call, Response<List<LeadingColorBody>> response) {
                                List<LeadingColorBody>  leadingColors= response.body();
                                leadingColors.remove(0);
                                leadingColors.remove(0);
//                                List<Integer> colors = leadingColors.stream()
//                                        .map(LeadingColorBody::getColor)
//                                        .collect(Collectors.toList());
                                for (int i = 0; i < leadingColors.size(); i++) {
                                    colorViews[i].setBackgroundColor(leadingColors.get(i).getColor());
                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) colorViews[i].getLayoutParams();
                                    params.width = colorFrame.getWidth()*leadingColors.get(i).getPixelCount()/4096;
                                    Log.d("Waga "+i+": ", leadingColors.get(i).getPixelCount()+"");
                                    colorViews[i].setLayoutParams(params);
                                }
                                Log.d("API response", response.toString());
                            }

                            @Override
                            public void onFailure(Call<List<LeadingColorBody>> call, Throwable t) {
                                Toast.makeText(MainActivity.this, t.toString(), Toast.LENGTH_LONG).show();
                                Log.e("API failure", t.getMessage(), t);
                            }
                        });
                        previousTrack = track;
                    }
                });

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Aaand we will finish off here.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
