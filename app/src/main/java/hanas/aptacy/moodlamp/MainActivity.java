package hanas.aptacy.moodlamp;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import hanas.aptacy.moodlamp.services.pojo.LeadingColorBody;


public class MainActivity extends AppCompatActivity implements MainActivityView {

    private MainActivityPresenter presenter;

    private LinearLayout colorFrame;
    private FrameLayout[] colorViews;
    private ImageView imageView;
    private ConstraintLayout mainLayout;
    private TextView title;
    private TextView artistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        this.getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        mainLayout = findViewById(R.id.main_layout);
        title = findViewById(R.id.song_title);
        artistName = findViewById(R.id.song_artist);
        title.setSelected(true);
        imageView = findViewById(R.id.imageView);
        colorFrame = findViewById(R.id.color_frame);
        colorViews = new FrameLayout[]{
                findViewById(R.id.color_1),
                findViewById(R.id.color_2),
                findViewById(R.id.color_3),
                findViewById(R.id.color_4),
                findViewById(R.id.color_5),
                findViewById(R.id.color_6),
        };

        presenter = new MainActivityPresenter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.connectWithLeadingColorsService();
        presenter.connectWithSpotifyAppRemote();
    }

    @Override
    public void setSongInfo(String titleStr, String artistNameStr, String imageURLStr) {
        title.setText(titleStr);
        artistName.setText(artistNameStr);
        Picasso.with(this).load(imageURLStr).into(imageView);
    }

    @Override
    public void setColorsOfNewAlbumCover(List<LeadingColorBody> leadingColors) {
        for (int i = 0; i < leadingColors.size() && i < colorViews.length; i++) {
            colorViews[i].setBackgroundColor(leadingColors.get(i).getColor());
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) colorViews[i].getLayoutParams();
            params.width = colorFrame.getWidth() * leadingColors.get(i).getPixelCount() / 4096;
            Log.d("Waga " + i + ": ", leadingColors.get(i).getPixelCount() + "");
            colorViews[i].setLayoutParams(params);
        }
    }

    @Override
    public void showSpotifyConnectionError(Throwable t) {
        Toast.makeText(MainActivity.this, "Can't connect :'(", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLeadingColorServiceResponseError(Throwable t) {
        Toast.makeText(MainActivity.this, t.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void connectWithSpotify(ConnectionParams connectionParams, Connector.ConnectionListener spotifyConnectionListener) {
        SpotifyAppRemote.connect(this, connectionParams, spotifyConnectionListener);
    }

    @Override
    public void setWallpaperFromBitmap(Bitmap bitmap) {
        try {
            View root = mainLayout.getRootView();
            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
            root.setBackground(ob);
            WallpaperManager myWallpaperManager = WallpaperManager
                    .getInstance(getApplicationContext());
            myWallpaperManager.setBitmap(bitmap);
            Toast.makeText(this, "Wallpaper set!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int[] getScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }


    @Override
    protected void onStop() {
        super.onStop();
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
