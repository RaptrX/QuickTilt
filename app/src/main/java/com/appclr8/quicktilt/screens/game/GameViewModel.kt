package com.appclr8.quicktilt.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 1000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

class GameViewModel : ViewModel() {

    //Stuff for timer
    companion object {
        // These represent different important times
        // This is the time when the phone will start buzzing each second
        private const val COUNTDOWN_PANIC_SECONDS = 10L
        // This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L
        //This is the time before game starts
        const val THREE_SECONDS = 3000L
        //This is the time for each round
        const val ROUND_SECONDS = 2500L
    }

    private lateinit var timer : CountDownTimer
    private val _timeToStart = MutableLiveData<String>()
    val timerToStart : LiveData<String> get() = _timeToStart

    // The current round time
    private val _intervalTimeStr = MutableLiveData<String>()
    val intervalTimeStr: LiveData<String> get() = _intervalTimeStr

    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz: LiveData<BuzzType> get() = _eventBuzz

    // The current direction
    private val _direction = MutableLiveData<String>()
    val direction : LiveData<String> get() = _direction

    // The current score
    private val _score = MutableLiveData<Int>()
    val score : LiveData<Int> get() = _score

    //The arrow colour
    private val _colour = MutableLiveData<Int>()
    val colour : LiveData<Int> get() = _colour

    private lateinit var directionList: MutableList<String>
    private lateinit var intervalList: MutableList<Long>

    private val _gameOver = MutableLiveData<Boolean>()
    val gameOver : LiveData<Boolean> get() = _gameOver

    private val _gameStarted = MutableLiveData<Boolean>()
    val gameStarted : LiveData<Boolean> get() = _gameStarted

    private val _roundStarted = MutableLiveData<Boolean>()
    val roundStarted : LiveData<Boolean> get() = _roundStarted

    private val _shouldCheckAnswer = MutableLiveData<Boolean>()
    private val shouldCheckAnswer : LiveData<Boolean> get() = _shouldCheckAnswer

    init {
        initializeValues()
    }

    fun startGame(){
        initializeValues()
        startTimer(THREE_SECONDS)
    }

    private fun initializeValues(){
        _gameOver.value = false
        _gameStarted.value = false
        _roundStarted.value = false
        _shouldCheckAnswer.value = false
        _score.value = 0
        resetGame()
    }

    private fun startTimer(time: Long){

        timer = object : CountDownTimer(time, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                if (gameStarted.value == false){
                    _timeToStart.value = ((millisUntilFinished / ONE_SECOND) +  1).toString()
                    _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                } else {
                    _intervalTimeStr.value = DateUtils.formatElapsedTime((millisUntilFinished + 1000L) / ONE_SECOND)
                }
            }

            override fun onFinish() {
                if (gameStarted.value == false){
                    _gameStarted.value = true
                    _score.value = 0

                }
                nextDirection()
            }
        }

        timer.start()
    }

    private fun resetGame() {
        directionList = mutableListOf("UP", "DOWN", "LEFT", "RIGHT", "UP", "DOWN", "LEFT", "RIGHT", "UP",
            "DOWN", "LEFT", "RIGHT"
        )

        directionList.shuffle()
        directionList.removeAt(0)
        directionList.removeAt(0)

        intervalList = mutableListOf(2000L, 3000L, 4000L, 5000L, 2000L, 3000L, 4000L, 5000L, 2000L,
            3000L, 4000L, 5000L
        )

        intervalList.shuffle()
        intervalList.removeAt(0)
        intervalList.removeAt(0)
    }

    private fun nextDirection() {
        //Select and remove an item from the list
        if (directionList.isEmpty()) {
            _eventBuzz.value = BuzzType.GAME_OVER
            timer.cancel()
            _gameOver.value = true
        } else {
            if (roundStarted.value == true){
                _roundStarted.value = false
                startTimer(intervalList.removeAt(0))
            } else {
                _roundStarted.value = true
                _direction.value = directionList.removeAt(0)
                startTimer(ROUND_SECONDS)
            }
        }
    }

    fun setColour(colour: Int){
        _colour.value = colour
    }

    fun onRight(){
        checkAnswer("RIGHT")
    }

    fun onLeft(){
        checkAnswer("LEFT")
    }

    fun onUp(){
        checkAnswer("UP")
    }

    fun onDown(){
        checkAnswer("DOWN")
    }

    private fun checkAnswer(selectedDirection: String) {
        if (shouldCheckAnswer.value == true){
            _shouldCheckAnswer.value = false
            if (_direction.value == selectedDirection && roundStarted.value == true) {
                onCorrect()
            } else {
                onIncorrect()
            }
        }
    }

    fun resetShouldCheckAnswer(){
        _shouldCheckAnswer.value = true
    }

    private fun onCorrect(){
        _score.value = _score.value?.plus(1)
        _eventBuzz.value = BuzzType.CORRECT
        timer.cancel()
        nextDirection()
    }

    private fun onIncorrect() {
        _score.value = _score.value?.minus(1)
        timer.cancel()
        nextDirection()
    }

    fun onGameOverComplete(){
        _gameOver.value = false
        _score.value = 0
    }

    fun onBuzzComplete() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}