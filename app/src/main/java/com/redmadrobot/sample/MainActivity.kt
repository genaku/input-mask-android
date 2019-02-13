package com.redmadrobot.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.CheckBox
import android.widget.EditText
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.helper.AffinityCalculationStrategy
import java.util.*

/**
 * Home screen for the sample app.
 *
 * @author taflanidi
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPrefixSample()
        setupSuffixSample()
    }

    private fun setupPrefixSample() {
        val editText = findViewById<EditText>(R.id.prefix_edit_text)
        val checkBox = findViewById<CheckBox>(R.id.prefix_check_box)
        val affineFormats = ArrayList<String>()
        affineFormats.add("8 ([000]) [000]-[00]-[00]")

        val listener = MaskedTextChangedListener.installOn(
                editText,
                "+7 ([000]) [000]-[00]-[00]",
                affineFormats,
                AffinityCalculationStrategy.PREFIX,
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                        logValueListener(maskFilled, extractedValue, formattedValue)
                        checkBox.isChecked = maskFilled
                    }
                }
        )

        editText.hint = listener.placeholder()
    }

    private fun setupSuffixSample() {
        val editText = findViewById<EditText>(R.id.suffix_edit_text)
        val checkBox = findViewById<CheckBox>(R.id.suffix_check_box)
        val affineFormats = ArrayList<String>()
        affineFormats.add("+7 ([000]) [000]-[00]-[00]#[000]")

        val listener = MaskedTextChangedListener.installOn(
                editText,
                "+7 ([000]) [000]-[00]-[00]",
                affineFormats,
                AffinityCalculationStrategy.WHOLE_STRING,
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedText: String) {
                        logValueListener(maskFilled, extractedValue, formattedText)
                        checkBox.isChecked = maskFilled
                    }
                }
        )

        editText.hint = listener.placeholder()
    }

    private fun logValueListener(maskFilled: Boolean, extractedValue: String, formattedText: String) {
        val className = MainActivity::class.java.simpleName
        Log.d(className, extractedValue)
        Log.d(className, maskFilled.toString())
        Log.d(className, formattedText)
    }

}
