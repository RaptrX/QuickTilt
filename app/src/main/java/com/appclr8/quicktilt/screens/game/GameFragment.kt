package com.appclr8.quicktilt.screens.game

import android.content.Context
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.appclr8.quicktilt.R
import com.appclr8.quicktilt.databinding.GameFragmentBinding

class GameFragment : Fragment(), SensorEventListener {

    private lateinit var binding: GameFragmentBinding
    private val viewModel: GameViewModel by activityViewModels()

    private var sensorManager: SensorManager? = null

    private var gravityRot: FloatArray? = null //for gravity rotational data
    private var magneticRot: FloatArray? = null //for magnetic rotational data

    private var accels = FloatArray(3)
    private var mags = FloatArray(3)
    private var values = FloatArray(3)

    private var pitch = 0f
    private var roll = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.game_fragment,
            container,
            false
        )

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?

        binding.gameViewModel = viewModel
        binding.lifecycleOwner = this

        showArrow()

        startGame()

        startRound()

        endGame()

        initiateBuzz()

        return binding.root

    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()

        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { gameRotation ->
            sensorManager?.registerListener(this, gameRotation, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { gameRotation ->
            sensorManager?.registerListener(this, gameRotation, SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    private fun initiateBuzz() {
        viewModel.eventBuzz.observe(viewLifecycleOwner, { buzzType ->
            if (buzzType != GameViewModel.BuzzType.NO_BUZZ) {
                buzz(buzzType.pattern)
                viewModel.onBuzzComplete()
            }
        })
    }

    private fun endGame() {
        viewModel.gameOver.observe(viewLifecycleOwner, { gameState ->
            if (gameState) {
                val action = GameFragmentDirections.actionGameToScore(viewModel.score.value ?: 0)
                NavHostFragment.findNavController(this).navigate(action)
                viewModel.onGameOverComplete()
            }
        })
    }

    private fun startRound() {
        viewModel.roundStarted.observe(viewLifecycleOwner, { roundStarted ->
            if (roundStarted) {
                binding.timerText.setTextColor(resources.getColor(R.color.mediumGrey))
            } else {
                binding.directionArrow.setImageResource(R.drawable.shuffle)
                binding.timerText.setTextColor(resources.getColor(R.color.red))
            }
        })
    }

    private fun showArrow() {
        viewModel.direction.observe(viewLifecycleOwner, { direction ->
            when (direction) {
                "UP" -> {
                    binding.directionArrow.setImageResource(R.drawable.arrow_upward)
                }
                "DOWN" -> {
                    binding.directionArrow.setImageResource(R.drawable.arrow_downward)
                }
                "LEFT" -> {
                    binding.directionArrow.setImageResource(R.drawable.arrow_left)
                }
                "RIGHT" -> {
                    binding.directionArrow.setImageResource(R.drawable.arrow_right)
                }
            }
        })
    }

    private fun startGame() {

        viewModel.colour.observe(viewLifecycleOwner, { colour ->
            binding.directionArrow.imageTintList =
                ColorStateList.valueOf(resources.getColor(colour))
        })

        viewModel.gameStarted.observe(viewLifecycleOwner, { gameStarted ->
            if (!gameStarted) {
                binding.startTimerText.visibility = View.VISIBLE
                binding.directionArrow.visibility = View.GONE
            } else {
                binding.startTimerText.visibility = View.GONE
                binding.directionArrow.visibility = View.VISIBLE
                Toast.makeText(context, "Game has Begun!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun buzz(pattern: LongArray) {
        val buzzer = activity?.getSystemService<Vibrator>()

        buzzer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                //deprecated in API 26
                buzzer.vibrate(pattern, -1)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

        when (event!!.sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> mags = event.values.clone()
            Sensor.TYPE_ACCELEROMETER -> accels = event.values.clone()
        }

        gravityRot = FloatArray(9)
        magneticRot = FloatArray(9)
        SensorManager.getRotationMatrix(gravityRot, magneticRot, accels, mags)
        // Correct if screen is in Landscape
        val outR = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            gravityRot,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            outR
        )
        SensorManager.getOrientation(outR, values)
        pitch = values[1] * 57.2957795f
        roll = values[2] * 57.2957795f

        when {
            roll > -40 && roll < 0 -> {
                //Right
                viewModel.onRight()
            }
            roll < -140 && roll > -180 -> {
                //left
                viewModel.onLeft()
            }
            pitch > 50 && pitch < 90 -> {
                //up
                viewModel.onUp()
            }
            pitch < -50 && pitch > -90 -> {
                //down
                viewModel.onDown()
            }
            roll < -70 && roll > -110 && pitch < 20 && pitch > -20 -> {
                viewModel.resetShouldCheckAnswer()
            }
        }

        /*
        start position -> roll = -90, pitch = 0
        right -> roll = 0
        left -> roll = -180
        up -> pitch = 90
        down -> pitch = -90
        */
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}