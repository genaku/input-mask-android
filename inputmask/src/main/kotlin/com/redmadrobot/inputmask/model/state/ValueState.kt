package com.redmadrobot.inputmask.model.state

import com.redmadrobot.inputmask.model.Next
import com.redmadrobot.inputmask.model.State
import com.redmadrobot.inputmask.model.addMaskBraces

/**
 * ### ValueState
 *
 * Represents mandatory characters in square brackets [].
 *
 * Accepts only characters of own type (see ```StateType```). Puts accepted characters into the
 * result string.
 *
 * Returns accepted characters as an extracted value.
 *
 * @see ```ValueState.StateType```
 *
 * @author taflanidi
 */
class ValueState : State {

    /**
     * ### StateType
     *
     * ```Numeric``` stands for [9] characters
     * ```Literal``` stands for [a] characters
     * ```AlphaNumeric``` stands for [-] characters
     * ```Ellipsis``` stands for […] characters
     */
    sealed class StateType {
        class Numeric : StateType()
        class Literal : StateType()
        class AlphaNumeric : StateType()
        class Ellipsis(val inheritedType: StateType) : StateType()
        class Custom(val character: Char, val characterSet: String) : StateType()
    }

    val type: StateType

    /**
     * Constructor for elliptical ```ValueState```
     */
    constructor(inheritedType: StateType) : super(null) {
        type = StateType.Ellipsis(inheritedType)
    }

    constructor(child: State?, type: StateType) : super(child) {
        this.type = type
    }

    private fun accepts(character: Char): Boolean = when (type) {
        is StateType.Numeric -> character.isDigit()
        is StateType.Literal -> character.isLetter()
        is StateType.AlphaNumeric -> character.isLetterOrDigit()
        is StateType.Ellipsis -> when (type.inheritedType) {
            is StateType.Numeric -> character.isDigit()
            is StateType.Literal -> character.isLetter()
            is StateType.AlphaNumeric -> character.isLetterOrDigit()
            else -> false
        }
        is StateType.Custom -> type.characterSet.contains(character)
    }

    override fun accept(character: Char): Next? =
            if (accepts(character))
                Next(
                        nextState(),
                        character,
                        true,
                        character
                )
            else
                null

    val isElliptical: Boolean
        get() = type is StateType.Ellipsis

    override fun nextState(): State =
            if (type is StateType.Ellipsis)
                this
            else
                super.nextState()

    override fun toString(): String = when (type) {
        is StateType.Literal -> "[A] -> "
        is StateType.Numeric -> "[0] -> "
        is StateType.AlphaNumeric -> "[_] -> "
        is StateType.Ellipsis -> "[…] -> "
        is StateType.Custom -> type.character.toString().addMaskBraces() + " -> "
    } + childString

}