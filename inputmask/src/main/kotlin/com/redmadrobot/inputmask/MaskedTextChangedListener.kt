package com.redmadrobot.inputmask

import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import com.redmadrobot.inputmask.helper.AffinityCalculationStrategy
import com.redmadrobot.inputmask.helper.Mask
import com.redmadrobot.inputmask.model.CaretString
import com.redmadrobot.inputmask.model.Notation
import java.lang.ref.WeakReference
import java.util.*

/**
 * TextWatcher implementation.
 *
 * TextWatcher implementation, which applies masking to the user input, picking the most suitable mask for the text.
 *
 * Might be used as a decorator, which forwards TextWatcher calls to its own listener.
 */
open class MaskedTextChangedListener(
        var primaryFormat: String,
        var affineFormats: List<String> = emptyList(),
        var customNotations: List<Notation> = emptyList(),
        var affinityCalculationStrategy: AffinityCalculationStrategy = AffinityCalculationStrategy.WHOLE_STRING,
        var autocomplete: Boolean = true,
        field: EditText,
        var listener: TextWatcher? = null,
        var valueListener: ValueListener? = null
) : TextWatcher, View.OnFocusChangeListener {

    interface ValueListener {
        fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String)
    }

    private val primaryMask: Mask
        get() = Mask.getOrCreate(primaryFormat, customNotations)

    private var afterText: String = ""
    private var caretPosition: Int = 0

    private val field: WeakReference<EditText> = WeakReference(field)

    var showPlaceholder = false

    /**
     * Convenience constructor.
     */
    constructor(format: String, field: EditText) :
            this(format, field, null)

    /**
     * Convenience constructor.
     */
    constructor(format: String, field: EditText, valueListener: ValueListener?) :
            this(format, field, null, valueListener)

    /**
     * Convenience constructor.
     */
    constructor(format: String, field: EditText, listener: TextWatcher?, valueListener: ValueListener?) :
            this(format, true, field, listener, valueListener)

    /**
     * Convenience constructor.
     */
    constructor(format: String, autocomplete: Boolean, field: EditText, listener: TextWatcher?, valueListener: ValueListener?) :
            this(format, emptyList(), emptyList(), AffinityCalculationStrategy.WHOLE_STRING, autocomplete, field, listener, valueListener)

    /**
     * Convenience constructor.
     */
    constructor(primaryFormat: String, affineFormats: List<String>, field: EditText) :
            this(primaryFormat, affineFormats, field, null)

    /**
     * Convenience constructor.
     */
    constructor(primaryFormat: String, affineFormats: List<String>, field: EditText, valueListener: ValueListener?) :
            this(primaryFormat, affineFormats, field, null, valueListener)

    /**
     * Convenience constructor.
     */
    constructor(primaryFormat: String, affineFormats: List<String>, field: EditText, listener: TextWatcher?, valueListener: ValueListener?) :
            this(primaryFormat, affineFormats, true, field, listener, valueListener)

    /**
     * Convenience constructor.
     */
    constructor(primaryFormat: String, affineFormats: List<String>, autocomplete: Boolean, field: EditText, listener: TextWatcher?, valueListener: ValueListener?) :
            this(primaryFormat, affineFormats, AffinityCalculationStrategy.WHOLE_STRING, autocomplete, field, listener, valueListener)

    /**
     * Convenience constructor.
     */
    constructor(primaryFormat: String, affineFormats: List<String>, affinityCalculationStrategy: AffinityCalculationStrategy, autocomplete: Boolean, field: EditText, listener: TextWatcher?, valueListener: ValueListener?) :
            this(primaryFormat, affineFormats, emptyList(), affinityCalculationStrategy, autocomplete, field, listener, valueListener)

    /**
     * Set text and apply formatting.
     * @param text - text; might be plain, might already have some formatting.
     */
    open fun setText(text: String): Mask.Result? = field.get()?.let {
        val result = setText(text, it)
        afterText = result.formattedText.string
        caretPosition = result.formattedText.caretPosition
        valueListener?.onTextChanged(result.complete, result.extractedValue, afterText)
        return result
    }

    /**
     * Set text and apply formatting.
     * @param text - text; might be plain, might already have some formatting;
     * @param field - a field where to put formatted text.
     */
    open fun setText(text: String, field: EditText): Mask.Result {
        val result = pickMask(text, text.length, autocomplete).apply(
                CaretString(text, text.length),
                autocomplete
        )
        field.setText(result.formattedText.string)
        field.setSelection(result.formattedText.caretPosition)
        return result
    }

    /**
     * Generate placeholder.
     *
     * @return Placeholder string.
     */
    fun placeholder(): String = primaryMask.placeholder()

    /**
     * Minimal length of the text inside the field to fill all mandatory characters in the mask.
     *
     * @return Minimal satisfying count of characters inside the text field.
     */
    fun acceptableTextLength(): Int = primaryMask.acceptableTextLength()

    /**
     *  Maximal length of the text inside the field.
     *
     *  @return Total available count of mandatory and optional characters inside the text field.
     */
    fun totalTextLength(): Int = primaryMask.totalTextLength()

    /**
     * Minimal length of the extracted value with all mandatory characters filled.\
     *
     * @return Minimal satisfying count of characters in extracted value.
     */
    fun acceptableValueLength(): Int = primaryMask.acceptableValueLength()

    /**
     * Maximal length of the extracted value.
     *
     * @return Total available count of mandatory and optional characters for extracted value.
     */
    fun totalValueLength(): Int = primaryMask.totalValueLength()

    override fun afterTextChanged(edit: Editable?) {
        field.get()?.removeTextChangedListener(this)
        val newText = if (showPlaceholder) getColoredText(afterText) else afterText
        edit?.replace(0, edit.length, newText)
        if (showPlaceholder && caretPosition > newText.length) {
            caretPosition = newText.length
        }
        field.get()?.setSelection(caretPosition)
        field.get()?.addTextChangedListener(this)
        listener?.afterTextChanged(edit)
    }

    private fun getColoredText(text: String): CharSequence {
        val fullText = if (text.length >= placeholder().length) {
            text
        } else {
            text + placeholder().substring(Math.min(text.length, placeholder().length - 1))
        }

        Log.d("TAG", "text: [$text] [$fullText] place [${placeholder()}]")
        return SpannableString(fullText).apply {
            val color = field.get()?.currentHintTextColor
            if (color != null) {
                setSpan(ForegroundColorSpan(color),
                        text.length,
                        fullText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        listener?.beforeTextChanged(s, start, count, after)
    }

    private var lastPos = 0;

    override fun onTextChanged(text: CharSequence, cursorPosition: Int, before: Int, count: Int) {
        val isDeletion: Boolean = before > 0 && count == 0

        val changeStart = cursorPosition - if (isDeletion) 0 else before
        val changeEnd = changeStart + count

        val oldEnd = lastPos

        Log.d("TAG", "cur: $cursorPosition oldEnd: $oldEnd change [$changeStart-$changeEnd] text: [$text]")

        val correctPos = if (cursorPosition > caretPosition) caretPosition else cursorPosition

        var textToProcess = text.toString()
        if (showPlaceholder) {
            textToProcess = if (changeStart > caretPosition) {
                text.substring(0, oldEnd) + text.substring(changeStart, changeEnd)
            } else {
                text.substring(0, changeEnd)
            }
        }

        val newCaretPosition = if (showPlaceholder) {
            if (isDeletion) correctPos else correctPos + count
        } else {
            if (isDeletion) cursorPosition else cursorPosition + count
        }

        val result: Mask.Result =
                pickMask(textToProcess, newCaretPosition, autocomplete && !isDeletion).apply(
                        CaretString(textToProcess, newCaretPosition),
                        autocomplete && !isDeletion
                )
        afterText = result.formattedText.string
        caretPosition = if (isDeletion) cursorPosition else result.formattedText.caretPosition
        lastPos = caretPosition
        Log.d("TAG", "cur: $cursorPosition before: $before count: $count textToProcess: [$textToProcess] afterText: [$afterText] newCur: $caretPosition")
        valueListener?.onTextChanged(result.complete, result.extractedValue, afterText)
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (autocomplete && hasFocus) {
            val text: String = if (field.get()?.text!!.isEmpty()) {
                ""
            } else {
                field.get()?.text.toString()
            }

            val result: Mask.Result =
                    pickMask(text, text.length, autocomplete).apply(
                            CaretString(text, text.length),
                            autocomplete
                    )

            afterText = result.formattedText.string
            caretPosition = result.formattedText.caretPosition
            Log.d("TAG", "focus change: [$afterText] cur=$caretPosition")
            field.get()?.setText(afterText)
            field.get()?.setSelection(result.formattedText.caretPosition)
            valueListener?.onTextChanged(result.complete, result.extractedValue, afterText)
        }
    }

    private fun pickMask(
            text: String,
            caretPosition: Int,
            autocomplete: Boolean
    ): Mask {
        if (affineFormats.isEmpty()) return primaryMask

        data class MaskAffinity(val mask: Mask, val affinity: Int)

        val primaryAffinity: Int = calculateAffinity(primaryMask, text, caretPosition, autocomplete)

        val masksAndAffinities: MutableList<MaskAffinity> = ArrayList()
        for (format in affineFormats) {
            val mask: Mask = Mask.getOrCreate(format, customNotations)
            val affinity: Int = calculateAffinity(mask, text, caretPosition, autocomplete)
            masksAndAffinities.add(MaskAffinity(mask, affinity))
        }

        masksAndAffinities.sortByDescending { it.affinity }

        var insertIndex: Int = -1

        for ((index, maskAffinity) in masksAndAffinities.withIndex()) {
            if (primaryAffinity >= maskAffinity.affinity) {
                insertIndex = index
                break
            }
        }

        if (insertIndex >= 0) {
            masksAndAffinities.add(insertIndex, MaskAffinity(primaryMask, primaryAffinity))
        } else {
            masksAndAffinities.add(MaskAffinity(primaryMask, primaryAffinity))
        }

        return masksAndAffinities.first().mask
    }

    private fun calculateAffinity(
            mask: Mask,
            text: String,
            caretPosition: Int,
            autocomplete: Boolean
    ): Int = affinityCalculationStrategy.calculateAffinityOfMask(
            mask,
            CaretString(text, caretPosition),
            autocomplete
    )

    companion object {
        /**
         * Create a `MaskedTextChangedListener` instance and assign it as a field's
         * `TextWatcher` and `onFocusChangeListener`.
         */
        @JvmStatic
        fun installOn(
                editText: EditText,
                primaryFormat: String,
                valueListener: ValueListener? = null
        ): MaskedTextChangedListener = installOn(
                editText,
                primaryFormat,
                emptyList(),
                AffinityCalculationStrategy.WHOLE_STRING,
                valueListener
        )

        /**
         * Create a `MaskedTextChangedListener` instance and assign it as a field's
         * `TextWatcher` and `onFocusChangeListener`.
         */
        @JvmStatic
        fun installOn(
                editText: EditText,
                primaryFormat: String,
                affineFormats: List<String> = emptyList(),
                affinityCalculationStrategy: AffinityCalculationStrategy = AffinityCalculationStrategy.WHOLE_STRING,
                valueListener: ValueListener? = null
        ): MaskedTextChangedListener = installOn(
                editText,
                primaryFormat,
                affineFormats,
                emptyList(),
                affinityCalculationStrategy,
                true,
                null,
                valueListener
        )

        /**
         * Create a `MaskedTextChangedListener` instance and assign it as a field's
         * `TextWatcher` and `onFocusChangeListener`.
         */
        @JvmStatic
        fun installOn(
                editText: EditText,
                primaryFormat: String,
                affineFormats: List<String> = emptyList(),
                customNotations: List<Notation> = emptyList(),
                affinityCalculationStrategy: AffinityCalculationStrategy = AffinityCalculationStrategy.WHOLE_STRING,
                autocomplete: Boolean = true,
                listener: TextWatcher? = null,
                valueListener: ValueListener? = null
        ) = MaskedTextChangedListener(
                primaryFormat,
                affineFormats,
                customNotations,
                affinityCalculationStrategy,
                autocomplete,
                editText,
                listener,
                valueListener
        ).apply {
            editText.addTextChangedListener(this)
            editText.onFocusChangeListener = this
        }
    }

}
