package com.androiddevs.runningappyt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
        bottomNavigationView.setOnNavigationItemReselectedListener { /* NO - OP */ }

        nav_host_fragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->

                Log.d("DestenationId", "${destination.id} - ${destination.label.toString()}")
                when (destination.id) {
                    R.id.settingFragment, R.id.runFragment, R.id.statisticFragment -> {
                        Log.d("DestenationId", "Show - ${destination.label.toString()}")
                        bottomNavigationView.visibility = View.VISIBLE
                    }
                    else -> {

                        Log.d("DestenationId", "Gone - ${destination.label.toString()}")
                        bottomNavigationView.visibility = View.GONE
                    }
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
