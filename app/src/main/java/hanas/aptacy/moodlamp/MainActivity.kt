package hanas.aptacy.moodlamp

import android.Manifest
import android.animation.LayoutTransition
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.FadeProvider
import com.google.android.material.transition.MaterialSharedAxis
import com.squareup.picasso.Picasso
import hanas.aptacy.moodlamp.dagger.components.DaggerMainActivityComponent
import hanas.aptacy.moodlamp.databinding.ActivityMainBinding
import hanas.aptacy.moodlamp.databinding.ActivityMainBinding.inflate
import hanas.aptacy.moodlamp.databinding.ContentMainBinding
import hanas.aptacy.moodlamp.dagger.modules.LeadingColorsServiceModule
import hanas.aptacy.moodlamp.dagger.modules.MainActivityModule
import hanas.aptacy.moodlamp.dagger.modules.SpotifyModule
import hanas.aptacy.moodlamp.services.leadingcolors.pojo.LeadingColorBody
import hanas.aptacy.moodlamp.utlis.ImgUtils.createExternalStoragePublicPicture
import hanas.aptacy.moodlamp.utlis.ImgUtils.saveBitmap
import java.io.IOException
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainActivityView {

    @Inject
    lateinit var presenter: MainActivityPresenter
    private lateinit var base: ActivityMainBinding
    private lateinit var content: ContentMainBinding
    private lateinit var colorFields: List<FrameLayout>
    private lateinit var wallpaperActions: List<WallpaperAction>

    private fun setNightMode() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setNightMode()

        this.window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        base = inflate(layoutInflater)
        content = base.contentMain
        setContentView(base.root)

        colorFields = listOf(
                content.frlMainColor1,
                content.frlMainColor2,
                content.frlMainColor3,
                content.frlMainColor4,
                content.frlMainColor5,
                content.frlMainColor6
        )
        wallpaperActions = listOf(
                WallpaperAction(base.fabMainSaveWallpaper, base.mcvMainSave) { saveWallpaperOnClick() },
                WallpaperAction(base.fabMainSetWallpaper, base.mcvMainSet) { setWallpaperFromBitmap(wallpaperSp) }
        )
//        content.tvMainSongTitle.isSelected = true
        content.cslMainContent.layoutTransition = LayoutTransition()
        content.cslMainContent.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        base.cslMainBase.layoutTransition = LayoutTransition()
        base.cslMainBase.layoutTransition.addTransitionListener(object : LayoutTransition.TransitionListener {
            override fun startTransition(transition: LayoutTransition, container: ViewGroup, view: View, transitionType: Int) = Unit
            override fun endTransition(transition: LayoutTransition, container: ViewGroup, view: View, transitionType: Int) = endTransition(view, transitionType)
        })
        wallpaperActionsState = WallpaperActionsState.EXPANDED
        base.babMainAppBar.setOnMenuItemClickListener {
            if (it.itemId == R.id.mni_main_settings) {
                startSettingsActivity()
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener false
        }

        base.fabMainExpandWallpaperActions.setOnClickListener {
            when (wallpaperActionsState) {
                WallpaperActionsState.HIDDEN -> {
                    base.fabMainExpandWallpaperActions.apply {
                        setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_action_close))
                        backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.colorErrorDark))
                    }
                    TransitionManager.go(Scene(base.cslMainBase), MaterialSharedAxis(MaterialSharedAxis.X, false))
                    wallpaperActions[0].apply {
                        label.visibility = View.VISIBLE
                        actionFAB.visibility = View.VISIBLE
                    }
                    wallpaperActionsState = WallpaperActionsState.EXPANDED
                }
                WallpaperActionsState.EXPANDED -> {
                    base.fabMainExpandWallpaperActions.apply {
                        setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_action_image))
                        backgroundTintList = ColorStateList.valueOf((prominentColor
                                ?: ContextCompat.getColor(this@MainActivity, R.color.colorPrimary)))
                    }
                    TransitionManager.go(Scene(base.cslMainBase), MaterialSharedAxis(MaterialSharedAxis.X, true))
                    wallpaperActions[wallpaperActions.lastIndex].apply {
                        label.visibility = View.GONE
                        actionFAB.visibility = View.GONE
                    }
                    wallpaperActionsState = WallpaperActionsState.HIDDEN
                }
            }
        }
    }

    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        val component = DaggerMainActivityComponent.builder()
                .mainActivityModule(MainActivityModule(this))
                .spotifyModule(SpotifyModule())
                .leadingColorsServiceModule(LeadingColorsServiceModule())
                .build()
        component.inject(this)
    }

    private fun endTransition(view: View, transitionType: Int) {
        when (transitionType) {
            LayoutTransition.DISAPPEARING -> {
                wallpaperActions.filter { view == it.actionFAB }.forEach { it.actionFABVisible = false }
                wallpaperActions.filter { view == it.label }.forEach { it.labelVisible = false }
                for ((i, action) in wallpaperActions.withIndex().reversed()) {
                    val previousAction = wallpaperActions.getOrNull(i - 1) ?: return
                    if (action.containsView(view) && action.isFullyInvisible && previousAction.isFullyVisible) {
                        hidePreviousFab(action, previousAction)
                    }
                }
            }
            LayoutTransition.APPEARING -> {
                wallpaperActions.filter { view == it.actionFAB }.forEach { it.actionFABVisible = true }
                wallpaperActions.filter { view == it.label }.forEach { it.labelVisible = true }
                for ((i, action) in wallpaperActions.withIndex()) {
                    val nextAction = wallpaperActions.getOrNull(i + 1) ?: return
                    if (action.containsView(view) && action.isFullyVisible && nextAction.isFullyInvisible) {
                        showNextFab(nextAction)
                    }
                }
            }
        }
    }

    private fun hidePreviousFab(action: WallpaperAction, previousAction: WallpaperAction) {
        base.cslMainBase.run {
            layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING)
            removeView(action.label)
            removeView(action.actionFAB)
            layoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING)
        }
        TransitionManager.go(Scene(base.cslMainBase), MaterialSharedAxis(MaterialSharedAxis.X, true))
        previousAction.apply {
            label.visibility = View.GONE
            actionFAB.visibility = View.GONE
        }
    }

    private fun showNextFab(nextAction: WallpaperAction) {
        base.cslMainBase.run {
            layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
            addView(nextAction.label)
            addView(nextAction.actionFAB)
            layoutTransition.enableTransitionType(LayoutTransition.APPEARING)
        }
        TransitionManager.go(Scene(base.cslMainBase), MaterialSharedAxis(MaterialSharedAxis.X, false))
        nextAction.apply {
            label.visibility = View.VISIBLE
            actionFAB.visibility = View.VISIBLE
        }
    }

    private fun saveWallpaperOnClick() {
        val myVersion = Build.VERSION.SDK_INT
        if (myVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            val expectedPermissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val missingPermissions = expectedPermissions.filter { !checkIfAlreadyHavePermission(it) }
            if (missingPermissions.isNotEmpty())
                requestForSpecificPermissions(missingPermissions.toTypedArray())
            else
                wallpaperSp?.let { saveWallpaperToFile(it) }
        }
    }

    private fun saveWallpaperToFile(wallpaper: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            saveBitmap(this, wallpaper)
        else
            createExternalStoragePublicPicture(this, wallpaper)
        Toast.makeText(this, "Wallpaper saved to file!", Toast.LENGTH_SHORT).show()
    }

    private fun checkIfAlreadyHavePermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestForSpecificPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(this, permissions, 101)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            101 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                wallpaperSp?.let { saveWallpaperToFile(it) }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    enum class WallpaperActionsState {
        EXPANDED, HIDDEN
    }

    class WallpaperAction(
            var actionFAB: FloatingActionButton,
            var label: MaterialCardView,
            var actionFABVisible: Boolean = true,
            var labelVisible: Boolean = true
    ) {

        constructor(actionFAB: FloatingActionButton, label: MaterialCardView, listenerFAB: View.OnClickListener?) : this(actionFAB, label) {
            this.actionFAB.setOnClickListener(listenerFAB)
        }

        fun containsView(view: View) = view == actionFAB || view == label

        val isFullyVisible: Boolean
            get() = actionFABVisible && labelVisible
        val isFullyInvisible: Boolean
            get() = !actionFABVisible && !labelVisible
    }

    private lateinit var wallpaperActionsState: WallpaperActionsState
    private var prominentColor: Int? = null

    override fun setSongInfo(title: String?, artistName: String?, imageURLStr: String?) {
        content.tvMainSongTitle.text = title
        content.tvMainSongArtist.text = artistName
        Picasso.with(this).load(imageURLStr).into(content.ivMainSongImage)
        colorFields.forEach { it.visibility = View.GONE }
    }

    override fun setColorsOfNewAlbumCover(leadingColors: List<LeadingColorBody>) {
        colorFields.withIndex().forEach { (i, view) ->
            val color = leadingColors.getOrElse(i) { return@forEach }
            view.apply {
                visibility = View.VISIBLE
                setBackgroundColor(color.getColorInt())
                view.layoutParams.width = content.lnlMainDominantColorsBar.width * leadingColors[i].pixelCount / 4096
            }
        }
    }

    override fun showSpotifyConnectionError(throwable: Throwable?) {
        Toast.makeText(this@MainActivity, "Can't connect :'(", Toast.LENGTH_SHORT).show()
    }

    override fun showLeadingColorServiceResponseError(t: Throwable?) {
        Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_LONG).show()
        Log.e("API failure", t?.message, t)
    }

    override fun setWallpaperToActivityBackground(bitmap: Bitmap?) {
        wallpaperSp = bitmap
        val ob = BitmapDrawable(resources, bitmap)
        val transform = FadeProvider()
        transform.incomingEndThreshold = 1f
        transform.createDisappear((base.frlMainBase.parent as ViewGroup), base.frlMainBase)?.start()
        base.frlMainBase.background = ob
        transform.createAppear((base.frlMainBase.parent as ViewGroup), base.frlMainBase)?.start()
    }

    override fun setWallpaperFromBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            Toast.makeText(this, "Wait for a new wallpaper!", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val myWallpaperManager = WallpaperManager.getInstance(applicationContext)
            myWallpaperManager.setBitmap(bitmap)
            Toast.makeText(this, "Wallpaper set!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override val screenSize: IntArray
        get() {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return intArrayOf(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }

    override var wallpaperSp: Bitmap? = null

    override fun setFabColor(prominentColor: Int) {
        this.prominentColor = prominentColor
        if (wallpaperActionsState == WallpaperActionsState.HIDDEN)
            base.fabMainExpandWallpaperActions.backgroundTintList = ColorStateList.valueOf(prominentColor)
        wallpaperActions.forEach { it.actionFAB.backgroundTintList = ColorStateList.valueOf(prominentColor) }
    }
}