package com.appclr8.quicktilt.screens.title

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.appclr8.quicktilt.R
import com.appclr8.quicktilt.databinding.TitleFragmentBinding
import com.appclr8.quicktilt.screens.game.ColourPickerFragment

class TitleFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding: TitleFragmentBinding = DataBindingUtil.inflate(
            inflater, R.layout.title_fragment, container, false)

        //Pop dialog to pick arrow colour
        binding.playGameButton.setOnClickListener {
            ColourPickerFragment().show(parentFragmentManager, "ColourPickerFragment")
        }
        return binding.root
    }
}