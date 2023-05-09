package com.example.navigationdrawerdemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.navigationdrawerdemo.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.coroutineContext
import kotlin.math.abs


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var adapted = false
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout

        drawerLayout.viewTreeObserver.addOnGlobalLayoutListener {
            if (adapted) {
                return@addOnGlobalLayoutListener
            }
            Log.d("TAG", "Adapting...")
            // Changing logo
            val logo = BitmapFactory.decodeResource(resources, R.drawable.isulogo)
            val transLogo = white2transparent(logo)
            binding.drawerLayout.findViewById<ImageView>(R.id.logo).setImageBitmap(transLogo)

            // Changing gradient
            val currentGrad = ResourcesCompat.getDrawable(resources, R.drawable.side_nav_bar, null)
                    as GradientDrawable
            changeGrad(currentGrad, Color.BLUE)
            adapted = true
        }

        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_about), drawerLayout)
        Log.d("TAG", "Config set")
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun white2transparent(img: Bitmap): Bitmap {
        val result = img.copy(img.config, true)
        for (x in 0 until img.width) {
            for (y in 0 until img.height) {
                if (abs(result.getPixel(x, y) - Color.WHITE) < abs(result.getPixel(x,y) - Color.BLUE)) {
                    result.setPixel(x, y, Color.TRANSPARENT)
                }
            }
        }
        return result
    }

    // Ideally it should return new gradient, but it already works quite slow
    @RequiresApi(Build.VERSION_CODES.N)
    private fun changeGrad(srcGrad: GradientDrawable, targetColor: Int) {
        // GradientDrawable.colors is mutable, so let's copy the current state
        val baseColors = srcGrad.colors!!
        val colorsNum = baseColors.size
        val newColors = IntArray(colorsNum)

//        newColors[0] = targetColor
//        for (i in 1 until colorsNum) {
//            val difference = baseColors[i] - baseColors[i - 1]
//            newColors[i] = newColors[i - 1] + difference

        // To save the color's gradient logic, we'll use HSL.
        // In case of different alpha-channels, we would need to save them in a variable.
        for (i in 0 until colorsNum) {
            val newHSL = FloatArray(3)
            val targetHSL = FloatArray(3)
            ColorUtils.colorToHSL(baseColors[i], newHSL)
            ColorUtils.colorToHSL(targetColor, targetHSL)
            newHSL[0] = targetHSL[0]
            newColors[i] = ColorUtils.HSLToColor(newHSL)

            // Should be in a different function, but it would be redundant for our purposes
            newColors[i] = ColorUtils.setAlphaComponent(newColors[i], 77)
        }

        srcGrad.colors = newColors
    }
}