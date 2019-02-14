package com.redmadrobot.inputmask.helper

import org.junit.Assert
import org.junit.Test

class TextPresenterTest {

    @Test
    fun a() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ")
        strategy.updateStoredText("ЛСИ10000000000;00.00", 3, 0, 1)
        Assert.assertEquals("ЛСИ1", strategy.getText())
    }

    @Test
    fun deleteLastChar() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ3456789012;45.78")
        strategy.updateStoredText("ЛСИ3456789012;45.7", 18, 1, 0)
        Assert.assertEquals("ЛСИ3456789012;45.7", strategy.getText())
    }

    @Test
    fun deleteBeforeLastChar() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ3456789012;45.7")
        strategy.updateStoredText("ЛСИ3456789012;45.0", 17, 1, 0)
        Assert.assertEquals("ЛСИ3456789012;45.", strategy.getText())
    }

    @Test
    fun delete2chars() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ3456789012;45.78")
        strategy.updateStoredText("ЛСИ34567890;45.78", 11, 2, 0)
        Assert.assertEquals("ЛСИ34567890;45.78", strategy.getText())
    }

    @Test
    fun delete4thChar() {
        val strategy = TextPresentationStrategy.SHOW_PLACEHOLDER

        strategy.setText("ЛСИ3456789012;45.78")
        strategy.updateStoredText("ЛСИ456789012;45.78", 3, 1, 0)
        Assert.assertEquals("ЛСИ456789012;45.78", strategy.getText())
    }

}