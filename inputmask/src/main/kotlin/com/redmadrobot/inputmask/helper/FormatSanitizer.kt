package com.redmadrobot.inputmask.helper

import com.redmadrobot.inputmask.model.*
import java.util.*

/**
 * ### FormatSanitizer
 *
 * Sanitizes given ```formatString``` before it's compilation.
 *
 * @complexity ```O(2*floor(log(n)))```, and switches to ```O(n^2)``` for ```n < 20``` where
 * ```n = formatString.characters.count```
 *
 * @requires Format string to contain only flat groups of symbols in ```[]``` and ```{}``` brackets
 * without nested brackets, like ```[[000]99]```. Square bracket ```[]``` groups may contain mixed
 * types of symbols ("0" and "9" with "A" and "a" or "_" and "-"), which sanitizer will divide into
 * separate groups. Such that, ```[0000Aa]``` group will be divided in two groups: ```[0000]```
 * and ```[Aa]```.
 *
 * ```FormatSanitizer``` is used by ```Compiler``` before format string compilation.
 *
 * @author taflanidi
 */
class FormatSanitizer {

    /**
     * Sanitize ```formatString``` before compilation.
     *
     * In order to do so, sanitizer splits the string into groups of regular symbols, symbols in square
     * brackets [] and symbols in curly brackets {}. Then, characters in square brackets are sorted in
     * a way that mandatory symbols go before optional symbols. For instance,
     *
     * ```
     * a ([0909]) b
     * ```
     *
     * mask format is rearranged to
     *
     * ```
     * a ([0099]) b
     * ```
     *
     * Also, ellipsis in square brackets [] is always placed at the end.
     *
     * @complexity ```O(2*floor(log(n)))```, and switches to ```O(n^2)``` for ```n < 20``` where
     * ```n = formatString.characters.count```
     *
     * @requires Format string to contain only flat groups of symbols in ```[]``` and ```{}``` brackets
     * without nested brackets, like ```[[000]99]```. Square bracket ```[]``` groups may contain mixed
     * types of symbols ("0" and "9" with "A" and "a" or "_" and "-"), which sanitizer will divide into
     * separate groups. Such that, ```[0000Aa]``` group will be divided in two groups: ```[0000]```
     * and ```[Aa]```.
     *
     * @param formatString: mask format string.
     *
     * @returns Sanitized format string.
     *
     * @throws ```FormatError``` if ```formatString``` does not conform to the method requirements.
     */
    @Throws(Compiler.FormatError::class)
    fun sanitize(formatString: String): String {
        this.checkOpenBraces(formatString)

        val blocks: List<String> =
                this.divideBlocksWithMixedCharacters(this.getFormatBlocks(formatString))

        return this.sortFormatBlocks(blocks).joinToString("")
    }

    private fun getFormatBlocks(formatString: String): List<String> {
        val blocks: MutableList<String> = ArrayList()
        var currentBlock = ""
        var escape = false

        for (char in formatString.toCharArray()) {
            if (ESCAPE_CHAR == char) {
                if (!escape) {
                    escape = true
                    currentBlock += char
                    continue
                }
            }

            if ((MASK_CHARS_OPEN == char || FIXED_CHARS_OPEN == char) && !escape) {
                if (currentBlock.isNotEmpty()) {
                    blocks.add(currentBlock)
                }
                currentBlock = ""
            }

            currentBlock += char

            if ((MASK_CHARS_CLOSE == char || FIXED_CHARS_CLOSE == char) && !escape) {
                blocks.add(currentBlock)
                currentBlock = ""
            }

            escape = false
        }

        if (!currentBlock.isEmpty()) {
            blocks.add(currentBlock)
        }

        return blocks
    }

    private fun divideBlocksWithMixedCharacters(blocks: List<String>): List<String> {
        val resultingBlocks: MutableList<String> = ArrayList()

        for (block in blocks) {
            if (block.startsWith(MASK_CHARS_OPEN)) {
                var blockBuffer = ""
                for (blockCharacter in block) {
                    if (blockCharacter == MASK_CHARS_OPEN) {
                        blockBuffer += blockCharacter
                        continue
                    }

                    if (blockCharacter == MASK_CHARS_CLOSE && !blockBuffer.endsWith(ESCAPE_CHAR)) {
                        blockBuffer += blockCharacter
                        resultingBlocks.add(blockBuffer)
                        break
                    }

                    if (blockCharacter == '0' || blockCharacter == '9') {
                        if (blockBuffer.contains("A")
                                || blockBuffer.contains("a")
                                || blockBuffer.contains("-")
                                || blockBuffer.contains("_")) {
                            blockBuffer += MASK_CHARS_CLOSE
                            resultingBlocks.add(blockBuffer)
                            blockBuffer = "$MASK_CHARS_OPEN$blockCharacter"
                            continue
                        }
                    }

                    if (blockCharacter == 'A' || blockCharacter == 'a') {
                        if (blockBuffer.contains("0")
                                || blockBuffer.contains("9")
                                || blockBuffer.contains("-")
                                || blockBuffer.contains("_")) {
                            blockBuffer += MASK_CHARS_CLOSE
                            resultingBlocks.add(blockBuffer)
                            blockBuffer = "$MASK_CHARS_OPEN$blockCharacter"
                            continue
                        }
                    }

                    if (blockCharacter == '-' || blockCharacter == '_') {
                        if (blockBuffer.contains("0")
                                || blockBuffer.contains("9")
                                || blockBuffer.contains("A")
                                || blockBuffer.contains("a")) {
                            blockBuffer += MASK_CHARS_CLOSE
                            resultingBlocks.add(blockBuffer)
                            blockBuffer = "$MASK_CHARS_OPEN$blockCharacter"
                            continue
                        }
                    }

                    blockBuffer += blockCharacter
                }
            } else {
                resultingBlocks.add(block)
            }

        }

        return resultingBlocks
    }

    private fun sortFormatBlocks(blocks: List<String>): List<String> {
        val sortedBlocks: MutableList<String> = ArrayList()

        for (block in blocks) {
            var sortedBlock: String
            if (block.startsWith(MASK_CHARS_OPEN)) {
                if (block.contains("0") || block.contains("9")) {
                    sortedBlock = block.removeMaskBraces().sortChars().addMaskBraces()
                } else if (block.contains("a") || block.contains("A")) {
                    sortedBlock = block.removeMaskBraces().sortChars().addMaskBraces()
                } else {
                    sortedBlock = block.removeMaskBraces().replace("_", "A").replace("-", "a").sortChars().addMaskBraces()
                    sortedBlock = sortedBlock.replace("A", "_").replace("a", "-")
                }
            } else {
                sortedBlock = block
            }

            sortedBlocks.add(sortedBlock)
        }

        return sortedBlocks
    }

    private fun String.removeMaskBraces() =
            replace("$MASK_CHARS_OPEN", "").replace("$MASK_CHARS_CLOSE", "")

    private fun String.sortChars() =
            toCharArray().sorted().joinToString("")

    private fun checkOpenBraces(string: String) {
        var escape = false
        var squareBraceOpen = false
        var curlyBraceOpen = false

        for (char in string.toCharArray()) {
            if (ESCAPE_CHAR == char) {
                escape = !escape
                continue
            }

            if (MASK_CHARS_OPEN == char) {
                if (squareBraceOpen) {
                    throw Compiler.FormatError()
                }
                squareBraceOpen = true && !escape
            }

            if (MASK_CHARS_CLOSE == char && !escape) {
                squareBraceOpen = false
            }

            if (FIXED_CHARS_OPEN == char) {
                if (curlyBraceOpen) {
                    throw Compiler.FormatError()
                }
                curlyBraceOpen = true && !escape
            }

            if (FIXED_CHARS_CLOSE == char && !escape) {
                curlyBraceOpen = false
            }

            escape = false
        }
    }

}