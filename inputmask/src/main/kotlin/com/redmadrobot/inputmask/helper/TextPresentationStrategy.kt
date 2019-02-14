package com.redmadrobot.inputmask.helper

import android.util.Log

enum class TextPresentationStrategy {

    NO_PLACEHOLDER,

    SHOW_PLACEHOLDER;

    private var storedText = ""

    private var cursorPosition = 0

    fun setText(text: CharSequence) {
        storedText = text.toString()
    }

    fun updateMaskedText(text: String) {
        Log.d("updateMaskedText", text)
        storedText = text
    }

    fun getText(): String {
        return storedText
    }

    fun updateStoredText(text: CharSequence, cursorPosition: Int, before: Int, count: Int) {
        when (this) {
            NO_PLACEHOLDER -> storedText = text.toString()
            SHOW_PLACEHOLDER -> prepareTextWithPlaceholder(text, cursorPosition, before, count)
        }
    }

    fun getCaretPosition(cursorPosition: Int, before: Int, count: Int, caretPosition: Int): Int {
        val isDeletion = before > 0 && count == 0

        return when (this) {
            NO_PLACEHOLDER -> if (isDeletion) cursorPosition else cursorPosition + count
            SHOW_PLACEHOLDER -> {
                val correctPos = if (cursorPosition > caretPosition) caretPosition else cursorPosition
                if (isDeletion) correctPos else correctPos + count
            }
        }
    }

    private fun prepareTextWithPlaceholder(text: CharSequence, cursorPosition: Int, before: Int, count: Int) {
//        Log.d("prepare", "input [$text], $cursorPosition, $before, $count")
        val isDeletion = before > 0 && count == 0

        if (isDeletion) {
            // here
            // before - actually number of chars deleted
            // cursorPosition - actually position after delete
            val lastPos = storedText.length - 1
            var start = cursorPosition
            if (cursorPosition > lastPos) {
                start = lastPos - before
            }
            var end = start + before

            start = Math.max(0, start)
            end = Math.min(Math.max(0, end), storedText.length)

            storedText = storedText.removeRange(start, end)
        } else {
            val curStart = Math.max(cursorPosition - before, 0)
            val replacement = text.substring(curStart, curStart + count)
            val start = Math.max(cursorPosition - before, 0)
            if (start < storedText.length) {
                val end = Math.min(storedText.length, start + count)
//            Log.d("prepare", "stored: [$storedText] ($start-$end) {$substr}")
                storedText = storedText.replaceRange(start, end, replacement)
            } else {
                storedText += replacement
            }
        }
    }

}