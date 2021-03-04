package hanas.aptacy.moodlamp.services.spotify

interface SpotifyService {
    fun connect(callback: Callback);
    interface Callback {
        fun onSpotifyConnected()
        fun onSpotifyConnectionFailure(throwable: Throwable?)
        fun onNewSpotifyTrack(trackName: String?, artist: String?, coverUrl: String?)
    }
}