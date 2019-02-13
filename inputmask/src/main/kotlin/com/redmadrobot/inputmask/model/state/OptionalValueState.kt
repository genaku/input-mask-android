package com.redmadrobot.inputmask.model.state

import com.redmadrobot.inputmask.model.Next
import com.redmadrobot.inputmask.model.State
import com.redmadrobot.inputmask.model.addMaskBraces

/**
 * ### OptionalValueState
 *
 * Represents optional characters in square brackets [].
 *
 * Accepts any characters, but puts into the result string only the characters of own type
 * (see ```StateType```).
 *
 * Returns accepted characters of own type as an extracted value.
 *
 * @see ```OptionalValueState.StateType```
 *
 * @author taflanidi
 */
class OptionalValueState(child: State, val type: StateType) : State(child) {

    sealed class StateType {
        class Numeric : StateType()
        class Literal : StateType()
        class AlphaNumeric : StateType()
        class Custom(val character: Char, val characterSet: String) : StateType()
    }

    private fun accepts(character: Char): Boolean = when (this.type) {
        is StateType.Numeric -> character.isDigit()
        is StateType.Literal -> character.isLetter()
        is StateType.AlphaNumeric -> character.isLetterOrDigit()
        is StateType.Custom -> this.type.characterSet.contains(character)
    }

    override fun accept(character: Char): Next? {
        val acceptsCharacter = accepts(character)
        val acceptedCharacter = if (acceptsCharacter) character else null
        return Next(
                this.nextState(),
                acceptedCharacter,
                acceptsCharacter,
                acceptedCharacter
        )
    }

    override fun toString(): String = when (this.type) {
        is StateType.Literal -> "[a] -> "
        is StateType.Numeric -> "[9] -> "
        is StateType.AlphaNumeric -> "[-] -> "
        is StateType.Custom -> this.type.character.toString().addMaskBraces() + " -> "
    } + childString
}