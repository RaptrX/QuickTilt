package com.appclr8.quicktilt.screens.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.appclr8.quicktilt.R
import com.appclr8.quicktilt.databinding.ColourPickerFragmentBinding
import com.appclr8.quicktilt.screens.title.TitleFragmentDirections

class ColourPickerFragment  : DialogFragment() {

    private lateinit var binding: ColourPickerFragmentBinding

    private val viewModel : GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.colour_picker_fragment, container, false)

        binding.blueButton.setOnClickListener {
            setColourAndStartGame(R.color.miami_blue)
        }

        binding.greenButton.setOnClickListener {
            setColourAndStartGame(R.color.green)
        }

        binding.yellowButton.setOnClickListener {
            setColourAndStartGame(R.color.yellow)
        }

        binding.orangeButton.setOnClickListener {
            setColourAndStartGame(R.color.orange)
        }

        return binding.root
    }

    private fun setColourAndStartGame(colour:Int){
        viewModel.setColour(colour)
        dismiss()
        findNavController().navigate(TitleFragmentDirections.actionTitleToGame())
        viewModel.startGame()
    }

}