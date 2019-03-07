package com.redmadrobot.sample

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.helper.TextPresentationStrategy

class DynMaskEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs), View.OnFocusChangeListener {

    private var maskListener: MaskedTextChangedListener? = null

    val complete: Boolean
        get() = maskListener?.complete ?: true

    init {
        onFocusChangeListener = this
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        Log.d("TAG", "onFocusChange")
        maskListener?.onFocusChange(v, hasFocus)
    }

    fun setValue(text: CharSequence) {
        if (maskListener == null) {
            setText(text)
        } else {
            maskListener?.setText(text.toString())
        }
    }

    fun getValue(): String {
        return maskListener?.getText() ?: editableText.toString()
    }

    fun setHideWhenEmpty(value: Boolean) {
        maskListener?.setHideWhenEmpty(value)
    }

    fun setMask(maskString: String?) {
        if (maskString.isNullOrEmpty()) {
            removeMaskListener()
        } else {
            removeMaskListener()
            maskListener = MaskedTextChangedListener(
                    primaryFormat = maskString,
                    field = this,
                    autocompleteEmpty = false
            ).apply {
                textPresentationStrategy = TextPresentationStrategy.SHOW_PLACEHOLDER
            }
            Log.d("TAG", "addTextChangedListener $maskListener")
            this.addTextChangedListener(maskListener)
        }
    }

    private fun removeMaskListener() {
        maskListener ?: return
        this.removeTextChangedListener(maskListener)
        Log.d("TAG", "removeTextChangedListener $maskListener")
        maskListener = null
    }

    fun setValueListener(listener: MaskedTextChangedListener.ValueListener) {
        maskListener?.valueListener = listener
    }
}