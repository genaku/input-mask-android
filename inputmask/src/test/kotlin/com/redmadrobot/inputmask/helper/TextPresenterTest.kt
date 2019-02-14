package com.redmadrobot.inputmask.helper

import org.junit.Assert
import org.junit.Test

class TextPresenterTest {

    @Test
    fun addFirstDigit() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ")
        strategy.prepareText("ЛСИ10000000000;00.00", CursorData(3, 0, 1, 0, true))
        Assert.assertEquals("ЛСИ1", strategy.getText())
    }

    @Test
    fun deleteLastChar() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ3456789012;45.78")
        strategy.prepareText("ЛСИ3456789012;45.7",  CursorData(18, 1, 0, 0, true))
        Assert.assertEquals("ЛСИ3456789012;45.7", strategy.getText())
    }

    @Test
    fun deleteBeforeLastChar() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ3456789012;45.7")
        strategy.prepareText("ЛСИ3456789012;45.0",  CursorData(17, 1, 0, 0, true))
        Assert.assertEquals("ЛСИ3456789012;45.", strategy.getText())
    }

    @Test
    fun delete2chars() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ3456789012;45.78")
        strategy.prepareText("ЛСИ34567890;45.78",  CursorData(11, 2, 0, 0, true))
        Assert.assertEquals("ЛСИ34567890;45.78", strategy.getText())
    }

    @Test
    fun delete4thChar() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ3456789012;45.78")
        strategy.prepareText("ЛСИ456789012;45.78",  CursorData(3, 1, 0, 0, true))
        Assert.assertEquals("ЛСИ456789012;45.78", strategy.getText())
    }

    @Test
    fun replaceInMiddle() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER
        strategy.setText("ЛСИ1234567890;00.00")
        strategy.prepareText("ЛСИ120567890;00.00",  CursorData(5, 2, 1, 0, true))
        Assert.assertEquals("ЛСИ120567890;00.00", strategy.getText())
    }

}