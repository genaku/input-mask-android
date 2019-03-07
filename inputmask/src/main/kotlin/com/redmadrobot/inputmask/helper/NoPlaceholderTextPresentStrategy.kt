package com.redmadrobot.inputmask.helper

class NoPlaceholderTextPresentStrategy: ITextPresentationStrategy {

    override fun getTextToShow(storedText: String, mask: Mask?, text: String, autocomplete: Boolean, colorText: (String, Int) -> CharSequence): CharSequence {
        return text
    }

    override fun prepareText(isDeletion: Boolean, storedText: String, text: CharSequence, cursorData: CursorData): String {
        return text.toString()
    }

    override fun getCaretPosition(isDeletion: Boolean, cursorData: CursorData): Int {
        return if (isDeletion) cursorData.cursorPosition else cursorData.cursorPosition + cursorData.count
    }
}