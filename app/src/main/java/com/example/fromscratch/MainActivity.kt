package com.example.fromscratch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fromscratch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonStartService.setOnClickListener {
            startAccessibilityService()
        }

        binding.switchEnableService.setOnCheckedChangeListener { _, isChecked ->
            AutoClickerAccessibilityService.setServiceEnabled(this, isChecked)
        }

        // Učitaj sačuvano stanje
        val prefs = getSharedPreferences("AutoClickerPrefs", MODE_PRIVATE)
        binding.switchEnableService.isChecked = prefs.getBoolean("service_enabled", false)

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // Korisnik nije odobrio dozvolu
                Toast.makeText(this, "Dozvola za prikaz preko drugih aplikacija je neophodna", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1234
    }

    private fun startAccessibilityService() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}