package hanas.aptacy.moodlamp;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.transition.Scene;
import androidx.transition.TransitionManager;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.FadeProvider;
import com.google.android.material.transition.MaterialSharedAxis;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hanas.aptacy.moodlamp.services.ImgUtils;
import hanas.aptacy.moodlamp.services.pojo.LeadingColorBody;


public class MainActivity extends AppCompatActivity implements MainActivityView {

    private static final String TAG = "MainActivity";

    private MainActivityPresenter presenter;

    private ConstraintLayout baseCsl;
    private FrameLayout baseFrl;
    private LinearLayout colorFrame;
    private FrameLayout[] colorFields;
    private ImageView songImage;
    private ConstraintLayout contentCsl;
    private TextView title;
    private TextView artistName;
    private FloatingActionButton expandWallpaperActions;
    private WallpaperAction[] wallpaperActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        this.getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_main);

        baseCsl = findViewById(R.id.csl_main_base);
        baseFrl = findViewById(R.id.frl_main_base);
        contentCsl = findViewById(R.id.csl_main_content);
        title = findViewById(R.id.tv_main_song_title);
        artistName = findViewById(R.id.tv_main_song_artist);
        title.setSelected(true);
        songImage = findViewById(R.id.iv_main_song_image);
        colorFrame = findViewById(R.id.lnl_main_dominant_colors_bar);
        colorFields = new FrameLayout[]{
                findViewById(R.id.frl_main_color_1),
                findViewById(R.id.frl_main_color_2),
                findViewById(R.id.frl_main_color_3),
                findViewById(R.id.frl_main_color_4),
                findViewById(R.id.frl_main_color_5),
                findViewById(R.id.frl_main_color_6),
        };
        expandWallpaperActions = findViewById(R.id.fab_main_expand_wallpaper_actions);
        wallpaperActions = new WallpaperAction[]{
                new WallpaperAction(findViewById(R.id.fab_main_save_wallpaper), findViewById(R.id.mcv_main_save), fab -> saveWallpaperOnClick()),
                new WallpaperAction(findViewById(R.id.fab_main_set_wallpaper), findViewById(R.id.mcv_main_set), fab -> setWallpaperFromBitmap(wallpaper))
        };

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        contentCsl.setLayoutTransition(layoutTransition);

        LayoutTransition layoutTransition2 = new LayoutTransition();
        baseCsl.setLayoutTransition(layoutTransition2);

        layoutTransition2.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
//                Log.d(TAG,"\n\n startTransition: in "+container+" view "+view+" type "+ descr(transitionType));
            }

            @Override
            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                Log.d(TAG, "\n\n endTransition: in " + container + " view " + view + " type " + descr(transitionType));
                if (transitionType == LayoutTransition.DISAPPEARING) {
                    for (int i = wallpaperActions.length - 1; i >= 0; i--) {
                        if (view.equals(wallpaperActions[i].actionFAB))
                            wallpaperActions[i].actionFABVisible = false;
                        else if (view.equals(wallpaperActions[i].label))
                            wallpaperActions[i].labelVisible = false;
                        if (wallpaperActions[i].containsView(view) && i > 0 && wallpaperActions[i].isFullyInvisible() && wallpaperActions[i-1].isFullyVisible()) {
                            layoutTransition2.disableTransitionType(LayoutTransition.DISAPPEARING);
                            baseCsl.removeView(wallpaperActions[i].label);
                            baseCsl.removeView(wallpaperActions[i].actionFAB);
                            layoutTransition2.enableTransitionType(LayoutTransition.DISAPPEARING);

                            TransitionManager.go(new Scene(baseCsl), new MaterialSharedAxis(MaterialSharedAxis.X, true));
                            wallpaperActions[i - 1].label.setVisibility(View.GONE);
                            wallpaperActions[i - 1].actionFAB.setVisibility(View.GONE);
                        }
                    }
                } else if (transitionType == LayoutTransition.APPEARING) {
                    for (int i = 0; i < wallpaperActions.length; i++) {
                        if (view.equals(wallpaperActions[i].actionFAB))
                            wallpaperActions[i].actionFABVisible = true;
                        else if (view.equals(wallpaperActions[i].label))
                            wallpaperActions[i].labelVisible = true;
                        if (wallpaperActions[i].containsView(view) && i < wallpaperActions.length - 1 && wallpaperActions[i].isFullyVisible() && wallpaperActions[i+1].isFullyInvisible()) {
                            layoutTransition2.disableTransitionType(LayoutTransition.APPEARING);
                            baseCsl.addView(wallpaperActions[i + 1].label);
                            baseCsl.addView(wallpaperActions[i + 1].actionFAB);
                            layoutTransition2.enableTransitionType(LayoutTransition.APPEARING);

                            TransitionManager.go(new Scene(baseCsl), new MaterialSharedAxis(MaterialSharedAxis.X, false));
                            wallpaperActions[i + 1].label.setVisibility(View.VISIBLE);
                            wallpaperActions[i + 1].actionFAB.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            String descr(int transitionType) {
                String[] m = new String[]{"CHANGE_APPEARING", "CHANGE_DISAPPEARING", "APPEARING", "DISAPPEARING"};
                return "" + transitionType + ": " + m[transitionType & 3] + " changing=" + (transitionType & LayoutTransition.CHANGING);
            }
        });

        presenter = new MainActivityPresenter(this);
        wallpaperActionsState = WallpaperActionsState.EXPANDED;

        expandWallpaperActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wallpaperActionsState == WallpaperActionsState.HIDDEN) {
                    expandWallpaperActions.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_action_close));
                    expandWallpaperActions.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B71C1C")));
                    TransitionManager.go(new Scene(baseCsl), new MaterialSharedAxis(MaterialSharedAxis.X, false));
                    wallpaperActions[0].label.setVisibility(View.VISIBLE);
                    wallpaperActions[0].actionFAB.setVisibility(View.VISIBLE);
                    wallpaperActionsState = WallpaperActionsState.EXPANDED;
                } else {
                    expandWallpaperActions.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_action_image));
                    expandWallpaperActions.setBackgroundTintList(ColorStateList.valueOf(prominentColor != null ? prominentColor : ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));
                    TransitionManager.go(new Scene(baseCsl), new MaterialSharedAxis(MaterialSharedAxis.X, true));
                    wallpaperActions[1].label.setVisibility(View.GONE);
                    wallpaperActions[1].actionFAB.setVisibility(View.GONE);
                    wallpaperActionsState = WallpaperActionsState.HIDDEN;
                }
            }
        });
    }

    private void saveWallpaperOnClick() {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            List<String> missingPermissions = new ArrayList<>();
            String[] expectedPermissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            for (String permission : expectedPermissions) {
                if (!checkIfAlreadyHavePermission(permission)) {
                    missingPermissions.add(permission);
                }
            }
            if (!missingPermissions.isEmpty())
                requestForSpecificPermission(missingPermissions.toArray(new String[0]));
            else {
                saveWallpaperToFile(wallpaper);
            }
        }
    }

    private void saveWallpaperToFile(Bitmap wallpaper) {
        if (wallpaper != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ImgUtils.saveBitmap(this, wallpaper);
            } else {
                ImgUtils.createExternalStoragePublicPicture(this, wallpaper);
            }
            Toast.makeText(this, "Wallpaper saved to file!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkIfAlreadyHavePermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestForSpecificPermission(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveWallpaperToFile(wallpaper);
                } else {
                    //not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    Bitmap wallpaper;

    private enum WallpaperActionsState {
        EXPANDED, HIDDEN
    }

    static class WallpaperAction {
        boolean actionFABVisible = true;
        FloatingActionButton actionFAB;
        boolean labelVisible = true;
        MaterialCardView label;

        public WallpaperAction(FloatingActionButton actionFAB, MaterialCardView label, View.OnClickListener listenerFAB) {
            this.actionFAB = actionFAB;
            this.label = label;
            this.actionFAB.setOnClickListener(listenerFAB);
        }

        public WallpaperAction(FloatingActionButton actionFAB, MaterialCardView label) {
            this.actionFAB = actionFAB;
            this.label = label;
        }

        boolean containsView(@NonNull View view) {
            return view.equals(actionFAB) || view.equals(label);
        }

        boolean isFullyVisible() {
            return actionFABVisible && labelVisible;
        }

        boolean isFullyInvisible() {
            return !actionFABVisible && !labelVisible;
        }
    }

    WallpaperActionsState wallpaperActionsState;

    Integer prominentColor;

    @Override
    protected void onStart() {
        super.onStart();
        presenter.connectWithLeadingColorsService();
        presenter.connectWithSpotifyAppRemote();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void setSongInfo(String titleStr, String artistNameStr, String imageURLStr) {
        title.setText(titleStr);
        artistName.setText(artistNameStr);
        Picasso.with(this).load(imageURLStr).into(songImage);
        Arrays.stream(colorFields).forEach(colorView -> colorView.setVisibility(View.GONE));
    }

    @Override
    public void setColorsOfNewAlbumCover(List<LeadingColorBody> leadingColors) {
        for (int i = 0; i < leadingColors.size() && i < colorFields.length; i++) {
            colorFields[i].setVisibility(View.VISIBLE);
            colorFields[i].setBackgroundColor(leadingColors.get(i).getColor());
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) colorFields[i].getLayoutParams();
            params.width = colorFrame.getWidth() * leadingColors.get(i).getPixelCount() / 4096;
            colorFields[i].setLayoutParams(params);
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
    public void setWallpaperToActivityBackground(@NonNull Bitmap bitmap) {
        wallpaper = bitmap;
        BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
        FadeProvider transform = new FadeProvider();
        transform.setIncomingEndThreshold(1f);
        transform.createDisappear((ViewGroup) baseFrl.getParent(), baseFrl).start();
        baseFrl.setBackground(ob);
        transform.createAppear((ViewGroup) baseFrl.getParent(), baseFrl).start();
    }

    @Override
    public void setWallpaperFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "Wait for a new wallpaper!", Toast.LENGTH_SHORT).show();
        }
        try {
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
    public void setFabColor(int prominentColor) {
        this.prominentColor = prominentColor;
        if (wallpaperActionsState == WallpaperActionsState.HIDDEN)
            expandWallpaperActions.setBackgroundTintList(ColorStateList.valueOf(prominentColor));
        Arrays.stream(wallpaperActions).forEach(wallpaperAction -> wallpaperAction.actionFAB.setBackgroundTintList(ColorStateList.valueOf(prominentColor)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_btm_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.mni_main_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
