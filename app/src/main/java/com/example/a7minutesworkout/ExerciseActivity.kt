package com.example.a7minutesworkout

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a7minutesworkout.databinding.ActivityExerciseBinding
import com.example.a7minutesworkout.databinding.DialogCustomBackConfirmationBinding
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var binding: ActivityExerciseBinding? = null

    private var restTimerDuration: Long = 1
    private var restTimer: CountDownTimer? = null
    private var restProgress = 0

    private var exerciseTimerDuration: Long = 1
    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var mTextToSpeech: TextToSpeech? = null
    private var mMediaPLayer: MediaPlayer? = null

    private var mExerciseStatusAdapter: ExerciseStatusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarExercise)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        mTextToSpeech = TextToSpeech(this,this)
        exerciseList = Constants.defaultExerciseList()

        binding?.toolbarExercise?.setNavigationOnClickListener {
            customDialogForBackButton()
        }

        //For sound at rest start or exercise complete
        val soundURI = Uri.parse(
            "android.resource://com.example.a7minutesworkout/" + R.raw.press_start)
        mMediaPLayer = MediaPlayer.create(applicationContext, soundURI)
        mMediaPLayer?.isLooping = false

        setupRestView()
        setupExerciseStatusRecyclerView()
    }

    override fun onBackPressed() {
        customDialogForBackButton()
    }

    private fun customDialogForBackButton(){
        val customDialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)

        dialogBinding.btnYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }

        dialogBinding.btnNo.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun setupExerciseStatusRecyclerView() {
        binding?.rvExerciseStatus?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)

        mExerciseStatusAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter = mExerciseStatusAdapter
    }

    private fun setupRestView() {
        try{
            mMediaPLayer?.start()
        }catch (e: Exception){
            e.printStackTrace()
        }

        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.tvExerciseName?.visibility = View.INVISIBLE
        binding?.flExerciseView?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE
        binding?.tvUpcomingLabel?.visibility = View.VISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.VISIBLE

        if(restTimer != null){
            restTimer?.cancel()
            restProgress = 0
        }

        val upcomingExerciseName = exerciseList!![currentExercisePosition + 1].getName()
        binding?.tvUpcomingExerciseName?.text = upcomingExerciseName

        setRestProgressBar()
    }

    private fun setRestProgressBar() {
        binding?.progressBar?.progress = restProgress

        restTimer = object : CountDownTimer(restTimerDuration*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text = (10-restProgress).toString()
            }

            override fun onFinish() {
                Toast.makeText(
                    this@ExerciseActivity,
                    "Let's start the exercise.",
                    Toast.LENGTH_SHORT
                ).show()

                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                mExerciseStatusAdapter!!.notifyDataSetChanged()

                setupExerciseView()
            }
        }.start()
    }

    private fun setupExerciseView() {
        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.INVISIBLE
        binding?.tvExerciseName?.visibility = View.VISIBLE
        binding?.flExerciseView?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE
        binding?.tvUpcomingLabel?.visibility = View.INVISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.INVISIBLE

        if(exerciseTimer != null){
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        val exerciseImage = exerciseList!![currentExercisePosition].getImage()
        val exerciseName = exerciseList!![currentExercisePosition].getName()
        speakOut(exerciseName)
        binding?.ivImage?.setImageResource(exerciseImage)
        binding?.tvExerciseName?.text = exerciseName

        setExerciseProgressBar()
    }

    private fun setExerciseProgressBar() {
        binding?.progressBarExercise?.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(exerciseTimerDuration*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                binding?.progressBarExercise?.progress = 30 - exerciseProgress
                binding?.tvTimerExercise?.text = (30-exerciseProgress).toString()
            }

            override fun onFinish() {

                if(currentExercisePosition < exerciseList?.size!! -1){
                    exerciseList!![currentExercisePosition].setIsCompleted(true)
                    exerciseList!![currentExercisePosition].setIsSelected(false)
                    mExerciseStatusAdapter?.notifyDataSetChanged()
                    setupRestView()
                }else{
                    Toast.makeText(
                        this@ExerciseActivity,
                        "You have completed the workout.",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                    val intent = Intent(this@ExerciseActivity,FinishActivity::class.java)
                    startActivity(intent)
                }
            }
        }.start()
    }

    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            val result = mTextToSpeech?.setLanguage(Locale.US)

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TextToSpeech","The Language specified is not supported!")
            }
        }else{
            Log.e("TextToSpeech","Initialization Failed")
        }
    }

    private fun speakOut(text: String) {
        mTextToSpeech!!.speak(text,TextToSpeech.QUEUE_FLUSH, null,"")
    }

    override fun onDestroy() {
        super.onDestroy()
        if(restTimer != null){
            restTimer?.cancel()
            restProgress = 0
        }

        if(exerciseTimer != null){
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        if(mTextToSpeech != null){
            mTextToSpeech?.stop()
            mTextToSpeech = null
        }

        if(mMediaPLayer != null){
            mMediaPLayer?.stop()
        }
        binding = null
    }
}