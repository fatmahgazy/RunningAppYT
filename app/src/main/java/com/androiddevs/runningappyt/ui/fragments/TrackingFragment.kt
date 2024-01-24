package com.androiddevs.runningappyt.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.databinding.FragmentTrackingBinding
import com.androiddevs.runningappyt.other.Constants.ACTION_PAUSE_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningappyt.other.Constants.ACTION_STOP_SERVICE
import com.androiddevs.runningappyt.other.Constants.MAP_ZOOM
import com.androiddevs.runningappyt.other.Constants.POLYLINE_COLOR
import com.androiddevs.runningappyt.other.Constants.POLYLINE_WIDTH
import com.androiddevs.runningappyt.other.TrackingUtility
import com.androiddevs.runningappyt.services.TrackingService
import com.androiddevs.runningappyt.services.polyline
import com.androiddevs.runningappyt.ui.viewModels.MainViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import org.codeforegypt.runningappyt.db.Run
import kotlin.math.round
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController


@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!


    private val viewModel : MainViewModels by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<polyline>()

    private var map: GoogleMap? = null

    private  var curTimeInMillis = 0L

    private var menu: Menu? =null

    private var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true )
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)

        binding.btnToggleRun.setOnClickListener {
            toggleRun()

        }
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

       binding.mapView.getMapAsync {
            map = it
           addAllPOlylines()
        }
        subscribeToObserve()
    }

    private fun subscribeToObserve(){
        TrackingService.isTracking.observe(viewLifecycleOwner , Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner , Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis ,true)
            tvTimer.text =  formattedTime
        })
    }

    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)

        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (curTimeInMillis > 0L )
        {
            this.menu?.getItem(0)?.isVisible = true

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    @SuppressLint("SuspiciousIndentation")
    private fun showCancelTrackingDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext() , R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel run and delete all it is data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                stopRun()

            }
            .setNegativeButton("No") {
                dialogInterface, i ->
                dialogInterface.cancel()
            }
            .create()
            dialog.show()
    }

    private  fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }
    private fun updateTracking(isTracking: Boolean){
        this.isTracking =isTracking
        if (!isTracking){
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        }else{
            binding.btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = View.GONE
        }


    }

    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPOlylines(){
        for (polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)

        }
    }

    private  fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints){
            for (pos in polyline){
                bounds.include(pos)
            }
        }
        try {
            map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds.build(),
                    binding.mapView.width,
                    binding.mapView.height,
                    (binding.mapView.height * 0.05f).toInt()
                )
            )
        } catch (e: Exception) {
            Log.e("TrackingIssueLog", "Exception: ${e.message}")
        }
    }

    private fun endRunAndSaveToDb(){
        map?.snapshot {bmp ->

            var distanceInMeters = 0
            for (polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimesTamp = java.util.Calendar.getInstance().timeInMillis
            val caloriesBurned= ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp, dateTimesTamp, avgSpeed, distanceInMeters , curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Saved Successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()


        }
    }
    private fun addLatestPolyline(){
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1){

            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions =PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)

        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(),TrackingService::class.java).also{
            it.action =action
            requireContext().startService(it)

    }


    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView?.onSaveInstanceState(outState)
    }

}