package com.redmadrobot.inputmask.helper

interface ITextPresentationStrategy {
    fun getTextToShow(storedText: String, mask: Mask?, text: String, autocomplete: Boolean, colorText: (String, Int) -> CharSequence): CharSequence
    fun prepareText(isDeletion: Boolean, storedText: String, text: CharSequence, cursorData: CursorData): String
    fun getCaretPosition(isDeletion: Boolean, cursorData: CursorData): Int
}