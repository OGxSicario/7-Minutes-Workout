package com.example.a7minutesworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.a7minutesworkout.databinding.ActivityBmiBinding
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round

class BMIAcitvity : AppCompatActivity() {

    companion object{
        private const val METRIC_UNITS_VIEW = "METRIC_UNITS_VIEW"
        private const val US_UNITS_VIEW = "US_UNITS_VIEW"
    }
    private var currentVisibleView: String = METRIC_UNITS_VIEW
    private var binding: ActivityBmiBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBmiBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarBmiActivity)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "CALCULATE BMI"
        }

        binding?.toolbarBmiActivity?.setNavigationOnClickListener {
            onBackPressed()
        }

        makeMetricUnitsViewVisible()

        binding?.rgUNits?.setOnCheckedChangeListener{ _, checkedId: Int ->
            if(checkedId == R.id.rbMetricUnits){
                makeMetricUnitsViewVisible()
            }
            else{
                makeUSUnitsViewVisible()
            }
        }

        binding?.btnCalculateUnits?.setOnClickListener {
            calculateUnits()
        }

    }

    private fun makeMetricUnitsViewVisible() {
        currentVisibleView = METRIC_UNITS_VIEW
        binding?.tilMetricUnitWeight?.visibility = View.VISIBLE
        binding?.tilMetricUnitHeight?.visibility = View.VISIBLE
        binding?.tilUSUnitWeight?.visibility = View.GONE
        binding?.tilMetricUnitHeightFeet?.visibility = View.GONE
        binding?.tilMetricUnitHeightInch?.visibility = View.GONE

        binding?.etMetricUnitWeight?.text!!.clear()
        binding?.etMetricUnitHeight?.text!!.clear()

        binding?.llDisplayBMIResult?.visibility = View.INVISIBLE
    }

    private fun makeUSUnitsViewVisible() {
        currentVisibleView = US_UNITS_VIEW
        binding?.tilMetricUnitWeight?.visibility = View.INVISIBLE
        binding?.tilMetricUnitHeight?.visibility = View.INVISIBLE
        binding?.tilUSUnitWeight?.visibility = View.VISIBLE
        binding?.tilMetricUnitHeightFeet?.visibility = View.VISIBLE
        binding?.tilMetricUnitHeightInch?.visibility = View.VISIBLE

        binding?.etUSUnitWeight?.text!!.clear()
        binding?.etMetricUnitHeightFeet?.text!!.clear()
        binding?.etMetricUnitHeightInch?.text!!.clear()

        binding?.llDisplayBMIResult?.visibility = View.INVISIBLE
    }

    private fun displayBMIResult(bmi: Float) {

        val bmiLabel : String
        val bmiDescription : String

        if (bmi.compareTo(15f) <= 0) {
            bmiLabel = "Very severely underweight"
            bmiDescription = "Oops! You really need to take better care of yourself! Eat more!"
        } else if (bmi.compareTo(15f) > 0 && bmi.compareTo(16f) <= 0
        ) {
            bmiLabel = "Severely underweight"
            bmiDescription = "Oops!You really need to take better care of yourself! Eat more!"
        } else if (bmi.compareTo(16f) > 0 && bmi.compareTo(18.5f) <= 0
        ) {
            bmiLabel = "Underweight"
            bmiDescription = "Oops! You really need to take better care of yourself! Eat more!"
        } else if (bmi.compareTo(18.5f) > 0 && bmi.compareTo(25f) <= 0
        ) {
            bmiLabel = "Normal"
            bmiDescription = "Congratulations! You are in a good shape!"
        } else if ( bmi.compareTo(25f) > 0 && bmi.compareTo(30f) <= 0) {
            bmiLabel = "Overweight"
            bmiDescription = "Oops! You really need to take care of your yourself! Workout maybe!"
        } else if (bmi.compareTo(30f) > 0 && bmi.compareTo(35f) <= 0
        ) {
            bmiLabel = "Obese Class | (Moderately obese)"
            bmiDescription = "Oops! You really need to take care of your yourself! Workout maybe!"
        } else if (bmi.compareTo(35f) > 0 && bmi.compareTo(40f) <= 0
        ) {
            bmiLabel = "Obese Class || (Severely obese)"
            bmiDescription = "OMG! You are in a very dangerous condition! Act now!"
        } else {
            bmiLabel = "Obese Class ||| (Very Severely obese)"
            bmiDescription = "OMG! You are in a very dangerous condition! Act now!"
        }

        val bmiValue  =BigDecimal(bmi.toDouble())
            .setScale(2, RoundingMode.HALF_EVEN).toString()

        binding?.llDisplayBMIResult?.visibility = View.VISIBLE
        binding?.tvBMIValue?.text = bmiValue
        binding?.tvBMIType?.text = bmiLabel
        binding?.tvBMIDescription?.text = bmiDescription

    }

    private fun validateMetricUnits(): Boolean {
        var isValid = true

        if(binding?.etMetricUnitWeight?.text.toString().isEmpty()) {
            isValid = false
        }else if ( binding?.etMetricUnitHeight?.text.toString().isEmpty()) {
            isValid = false
        }

        return isValid
    }

    private fun calculateUnits() {
        if(currentVisibleView == METRIC_UNITS_VIEW) {
            if(validateMetricUnits()) {
                val heightValue: Float = binding?.etMetricUnitHeight?.text.toString().toFloat() / 100
                val weightValue: Float = binding?.etMetricUnitWeight?.text.toString().toFloat()

                val bmi = weightValue / (heightValue * heightValue)

                displayBMIResult(bmi)

            } else {
                Toast.makeText(
                    this@BMIAcitvity,
                    "Please enter valid values",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        else{
            if(validateUSUnits()) {
                val heightValueFeet: String = binding?.etMetricUnitHeightFeet?.text.toString()
                val heightValueInch: String = binding?.etMetricUnitHeightInch?.text.toString()
                val weightValuePound: Float = binding?.etUSUnitWeight?.text.toString().toFloat()

                val heightValue = heightValueInch.toFloat() + heightValueFeet.toFloat() * 12


                val bmi = 703 * (weightValuePound / (heightValue * heightValue))

                displayBMIResult(bmi)
            } else {
                Toast.makeText(
                    this@BMIAcitvity,
                    "Please enter valid values",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateUSUnits(): Boolean {
        var isValid = true

        when {
            binding?.etUSUnitWeight?.text.toString().isEmpty() -> {
                isValid = false
            }
            binding?.etMetricUnitHeightFeet?.text.toString().isEmpty() ->{
                isValid = false
            }
            binding?.etMetricUnitHeightInch?.text.toString().isEmpty() -> {
                isValid = false
            }
        }

        return isValid
    }
}