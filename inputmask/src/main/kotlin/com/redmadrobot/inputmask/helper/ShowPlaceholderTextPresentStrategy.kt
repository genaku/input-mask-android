package com.redmadrobot.inputmask.helper

import com.redmadrobot.inputmask.model.CaretString

class ShowPlaceholderTextPresentStrategy : ITextPresentationStrategy {

    override fun getTextToShow(storedText: String, mask: Mask?, text: String, autocomplete: Boolean, colorText: (String, Int) -> CharSequence): CharSequence {
        return addPlaceholderAndColor(storedText, mask, text, autocomplete, colorText)
    }

    private fun addPlaceholderAndColor(storedText: String, mask: Mask?, text: String, autocomplete: Boolean, colorText: (String, Int) -> CharSequence): CharSequence {
//        Log.d("getColoredText", "storedText: [$storedText]")
        val caretString = CaretString(storedText, storedText.length)
        val placeholder = mask?.placeholder(caretString, autocomplete) ?: ""
        val fullText = text + placeholder

//        Log.d("getColoredText", "text: [$text] [$fullText] place [$placeholder]")
        return colorText(fullText, text.length)
    }


    override fun prepareText(isDeletion: Boolean, storedText: String, text: CharSequence, cursorData: CursorData): String {
        return prepareTextWithPlaceholder(isDeletion, storedText, text, cursorData)
    }

    private fun prepareTextWithPlaceholder(isDeletion: Boolean, storedText: String, inputText: CharSequence, cursorData: CursorData): String {
        var result = storedText
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

            result = storedText.removeRange(start, end)
        } else {
            // here actually
            // before - number of chars deleted after cursorPosition
            // cursorPosition - current position
            // count - number of chars added after cursorPosition
            val start = Math.max(cursorData.cursorPosition, 0)
            val replacement = inputText.substring(start, start + cursorData.count)
            if (start < storedText.length) {
                val end = Math.min(storedText.length, start + cursorData.before)
                result = storedText.replaceRange(start, end, replacement)
            } else {
                result += replacement
            }
        }
        return result
    }

    override fun getCaretPosition(isDeletion: Boolean, cursorData: CursorData): Int {
        val correctPos = if (cursorData.cursorPosition > cursorData.caretPosition) cursorData.caretPosition else cursorData.cursorPosition
        return if (isDeletion) correctPos else correctPos + cursorData.count
    }
}