package hanas.aptacy.moodlamp.services.spotify

import android.content.Context
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track

class SpotifyServiceImpl(private val context: Context?) : SpotifyService {

    companion object {
        private const val CLIENT_ID = "6d7d697321a74a419a8ede8b74c6a7c8"
        private const val REDIRECT_URI = "https://trello.com/b/zYzvg43L/moodlamp"
    }

    override fun connect(callback: SpotifyService.Callback) {
        try {
            val connectionParams = ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(true)
                    .build()

            val spotifyConnectionListener = object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    callback.onSpotifyConnected()
                    spotifyAppRemote.playerApi
                            .subscribeToPlayerState()
                            .setEventCallback { newTrack(it, callback::onNewSpotifyTrack)}
                }
                override fun onFailure(throwable: Throwable?) = callback.onSpotifyConnectionFailure(throwable)
            }

            SpotifyAppRemote.connect(context, connectionParams, spotifyConnectionListener)
        } catch (error: CouldNotFindSpotifyApp) {
            error.printStackTrace()
        }
    }

    private fun newTrack(playerState: PlayerState, onNewSpotifyTrack: (String?, String?, String?) -> Unit) {
        val track: Track? = playerState.track
        val imageUri = track?.imageUri?.raw
        val imageId = imageUri?.substring(imageUri.lastIndexOf(":") + 1)
        val imageURLString = "https://i.scdn.co/image/$imageId"
        onNewSpotifyTrack(track?.name, track?.artist?.name, imageURLString)
    }

}