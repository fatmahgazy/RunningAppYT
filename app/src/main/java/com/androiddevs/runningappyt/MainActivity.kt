package com.androiddevs.runningappyt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.runningappyt.databinding.ActivityMainBinding
import com.androiddevs.runningappyt.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToTrackingFragmentIfNeeded(intent)

        setSupportActionBar(toolbar)

        bottomNavigationView.setupWithNavController(nav_host_fragment.findNavController())

        nav_host_fragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->

                when (destination.id) {
                    R.layout.fragment_settings, R.layout.fragment_run, R.layout.fragment_statistics ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE


                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){

        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT)
        {
            nav_host_fragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }

    }
}
