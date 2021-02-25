package hanas.aptacy.moodlamp;

import android.graphics.Bitmap;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;

import java.util.List;

import hanas.aptacy.moodlamp.services.pojo.LeadingColorBody;

public interface MainActivityView {
    void setSongInfo(String title, String artistName, String imageURLStr);
    void setColorsOfNewAlbumCover(List<LeadingColorBody> leadingColors);
    void showSpotifyConnectionError(Throwable throwable);
    void showLeadingColorServiceResponseError(Throwable t);
    void connectWithSpotify(ConnectionParams connectionParams, Connector.ConnectionListener spotifyConnectionListener);
    void setWallpaperFromBitmap(Bitmap bitmap);
    int[] getScreenSize();
}
