package com.redmadrobot.inputmask.helper

import com.redmadrobot.inputmask.model.CaretString

/**
 * ### TextPresentationStrategy
 *
 * Process text for different text presentation model (with visible/hidden placeholder in dynamic)
 *
 * @author Gena Kuchergin
 **/
enum class TextPresentationStrategy {
    NO_PLACEHOLDER,
    SHOW_PLACEHOLDER;

    private var storedText = ""
    private var mask: Mask? = null

    fun setText(text: CharSequence) {
        storedText = text.toString()
    }

    fun getText(): String {
        return storedText
    }

    fun getTextToShow(text: String, autocomplete: Boolean, colorText: (String, Int) -> CharSequence): CharSequence =
            when (this) {
                NO_PLACEHOLDER -> text
                SHOW_PLACEHOLDER -> addPlaceholderAndColor(text, autocomplete, colorText)
            }

    private fun addPlaceholderAndColor(text: String, autocomplete: Boolean, colorText: (String, Int) -> CharSequence): CharSequence {
//        Log.d("getColoredText", "storedText: [$storedText]")
        val caretString = CaretString(storedText, storedText.length)
        val placeholder = mask?.placeholder(caretString, autocomplete) ?: ""
        val fullText = text + placeholder

//        Log.d("getColoredText", "text: [$text] [$fullText] place [$placeholder]")
        return colorText(fullText, text.length)
    }

    fun getMaskResult(text: CharSequence, cursorData: CursorData, pickMask: (String, Int, Boolean) -> Mask): Mask.Result {
        prepareText(text, cursorData)
        return pickMaskResult(cursorData, pickMask)
    }

    fun prepareText(text: CharSequence, cursorData: CursorData) {
        when (this) {
            NO_PLACEHOLDER -> {
                storedText = text.toString()
            }
            SHOW_PLACEHOLDER -> {
                prepareTextWithPlaceholder(text, cursorData)
            }
        }
    }

    private fun prepareTextWithPlaceholder(inputText: CharSequence, cursorData: CursorData) {
        val isDeletion = isDeletion(cursorData.before, cursorData.count)

        if (isDeletion) {
            // here actually
            // before - number of deleted chars
            // cursorPosition - position after delete
            val lastPos = storedText.length - 1
            var start = cursorData.cursorPosition
            if (cursorData.cursorPosition > lastPos) {
                start = lastPos - cursorData.before
            }
            var end = start + cursorData.before

            start = Math.max(0, start)
            end = Math.min(Math.max(0, end), storedText.length)

            storedText = storedText.removeRange(start, end)
        } else {
            // here actually
            // before - number of chars deleted after cursorPosition
            // cursorPosition - current position
            // count - number of chars added after cursorPosition
            val start = Math.max(cursorData.cursorPosition, 0)
            val replacement = inputText.substring(start, start + cursorData.count)
            if (start < storedText.length) {
                val end = Math.min(storedText.length, start + cursorData.before)
                storedText = storedText.replaceRange(start, end, replacement)
            } else {
                storedText += replacement
            }
        }
    }

    private fun pickMaskResult(cursorData: CursorData, pickMask: (String, Int, Boolean) -> Mask): Mask.Result {
        val isDeletion: Boolean = isDeletion(cursorData.before, cursorData.count)
        val needAutocomplete = cursorData.autocomplete && !isDeletion
        val adjustedCaretPosition = getCaretPosition(cursorData)
        mask = pickMask(storedText, adjustedCaretPosition, needAutocomplete)
        val result = mask!!.apply(
                text = CaretString(storedText, adjustedCaretPosition),
                autocomplete = needAutocomplete
        )
        storedText = result.formattedText.string
        return result
    }

    private fun getCaretPosition(cursorData: CursorData): Int {
        val isDeletion = isDeletion(cursorData.before, cursorData.count)

        return when (this) {
            NO_PLACEHOLDER -> if (isDeletion) cursorData.cursorPosition else cursorData.cursorPosition + cursorData.count
            SHOW_PLACEHOLDER -> {
                val correctPos = if (cursorData.cursorPosition > cursorData.caretPosition) cursorData.caretPosition else cursorData.cursorPosition
                if (isDeletion) correctPos else correctPos + cursorData.count
            }
        }
    }

    fun isDeletion(before: Int, count: Int): Boolean =
            (count == 0 && before > 0)

}