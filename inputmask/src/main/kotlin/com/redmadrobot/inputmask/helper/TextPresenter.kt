package com.redmadrobot.inputmask.helper

import com.redmadrobot.inputmask.model.CaretString

/**
 * ### TextPresenter
 *
 * Process text for different text presentation model (with visible/hidden placeholder in dynamic)
 *
 * @author Gena Kuchergin
 **/
class TextPresenter(private val strategy: ITextPresentationStrategy) {

    private var storedText = ""
    private var mask: Mask? = null

    fun setText(text: CharSequence) {
        storedText = text.toString()
    }

    fun getText(): String {
        return storedText
    }

    fun getTextToShow(text: String, autocomplete: Boolean, colorText: (String, Int) -> CharSequence): CharSequence =
            strategy.getTextToShow(storedText, mask, text, autocomplete, colorText)

    fun getMaskResult(text: CharSequence, cursorData: CursorData, pickMask: (String, Int, Boolean) -> Mask): Mask.Result {
        val isDeletion = isDeletion(cursorData.before, cursorData.count)
        storedText = strategy.prepareText(isDeletion, storedText, text, cursorData)
        return pickMaskResult(cursorData, pickMask)
    }

    fun isDeletion(before: Int, count: Int): Boolean =
            (count == 0 && before > 0)

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
        return strategy.getCaretPosition(isDeletion, cursorData)
    }
}