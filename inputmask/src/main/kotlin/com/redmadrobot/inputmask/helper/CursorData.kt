package com.redmadrobot.inputmask.helper

data class CursorData(
        val cursorPosition: Int,
        val before: Int,
        val count: Int,
        val caretPosition: Int,
        val autocomplete: Boolean
)