package hanas.aptacy.moodlamp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.squareup.picasso.Picasso;

import java.util.List;

import hanas.aptacy.moodlamp.services.pojo.LeadingColorBody;


public class MainActivity extends AppCompatActivity implements MainActivityView {

    private MainActivityPresenter presenter;

    private LinearLayout colorFrame;
    private FrameLayout[] colorViews;
    private ImageView imageView;

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

        presenter = new MainActivityPresenter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.connectWithLeadingColorsService();
        presenter.connectWithSpotifyAppRemote();
    }

    public void setAlbumCoverImage(String imageURLString) {
        Picasso.with(this).load(imageURLString).into(imageView);
    }

    public void setColorsOfNewAlbumCover(List<LeadingColorBody> leadingColors) {
        for (int i = 0; i < leadingColors.size(); i++) {
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
