package com.redmadrobot.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.CheckBox
import android.widget.EditText
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.helper.TextPresentationStrategy
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/**
 * Home screen for the sample app.
 *
 * @author taflanidi
 */
class MainActivity : AppCompatActivity() {

    private var masked = true
    private var hideWhenEmpty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupButtons()
//        setupPrefixSample()
//        setupSuffixSample()
    }

    private fun setupPrefixSample() {
        val editText = findViewById<EditText>(R.id.prefix_edit_text)
        val checkBox = findViewById<CheckBox>(R.id.prefix_check_box)
        val affineFormats = ArrayList<String>()
        affineFormats.add("8 ([000]) [000]-[00]-[00]")

        val listener = MaskedTextChangedListener.installOn(
                editText,
                "ЛСИ[0999999999];[00].[00]",
//                affineFormats,
//                AffinityCalculationStrategy.PREFIX,
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
//                        logValueListener(maskFilled, extractedValue, formattedValue)
                        checkBox.isChecked = maskFilled
                    }
                }
        )
        listener.textPresentationStrategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        editText.hint = listener.placeholder()
    }

    private fun setupSuffixSample() {
        val editText = findViewById<EditText>(R.id.suffix_edit_text)
        val checkBox = findViewById<CheckBox>(R.id.suffix_check_box)
        val affineFormats = ArrayList<String>()
//        affineFormats.add("+7 ([000]) [000]-[00]-[00]#[000]")

        val listener = MaskedTextChangedListener.installOn(
                editText,
                "ЛСИ[0999999999];[00].[00]",
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedText: String) {
//                        logValueListener(maskFilled, extractedValue, formattedText)
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

    private fun setupButtons() {
        switchMasked()
        btnMask.setOnClickListener {
            switchMasked()
        }
        btnClear.setOnClickListener {
            edtMask.clear()
        }
        btnHide.setOnClickListener {
            hideWhenEmpty = !hideWhenEmpty
            edtMask.setHideWhenEmpty(hideWhenEmpty)
            hide_check_box.isChecked = hideWhenEmpty
        }
    }

    private fun switchMasked() {
        masked = !masked
        if (masked) {
            edtMask.setMask("ЛСИ[0999999999];[00].[00]")
        } else {
            edtMask.setMask("")
        }
        mask_check_box.isChecked = masked
    }

}
