package com.redmadrobot.inputmask.model

const val ESCAPE_CHAR = '\\'
const val MASK_CHARS_OPEN = '['
const val MASK_CHARS_CLOSE = ']'
const val FIXED_CHARS_OPEN = '{'
const val FIXED_CHARS_CLOSE = '}'

fun String.wrapWithMaskBraces() =
        "$MASK_CHARS_OPEN$this$MASK_CHARS_CLOSE"

fun String.addFixedBraces() =
        "$FIXED_CHARS_OPEN$this$FIXED_CHARS_CLOSE"
