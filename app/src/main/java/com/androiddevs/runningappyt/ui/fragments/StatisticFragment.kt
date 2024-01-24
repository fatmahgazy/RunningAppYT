package com.androiddevs.runningappyt.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.ui.viewModels.MainViewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticFragment : Fragment() {

    private val viewModel : MainViewModels by viewModels()
}
