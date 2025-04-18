package net.rickiekarp.core.ui.anim

import javafx.animation.*
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.util.Duration
import net.rickiekarp.core.settings.Configuration

/**
 *
 */
object AnimationHandler {

    lateinit var xPosMenu: DoubleProperty
    private val SLIDE_DURATION = Duration.seconds(0.2)

    lateinit var menuBtnAnim: Animation
    lateinit var slideIn: Timeline
    lateinit var slideOut: Timeline
    var stackFadeIn: FadeTransition? = null
    var stackFadeOut: FadeTransition? = null

    val OFFSET_X = 0
    val OFFSET_Y = 0
    val WIDTH = 39
    var menuWidth: Double = 0.toDouble()

    /**
     * Creates slideIn / slideOut timeline to slide the node content in and out
     * @param stageWidth Width of the stage
     */
    fun addSlideHandlers(stageWidth: Double) {
        menuWidth = stageWidth * 0.35
        xPosMenu = SimpleDoubleProperty(-menuWidth)
        slideIn = Timeline(KeyFrame(SLIDE_DURATION, KeyValue(xPosMenu, 0)))
        slideOut = Timeline(KeyFrame(SLIDE_DURATION, KeyValue(xPosMenu, -menuWidth)))
    }

    /**
     * Starts the menu button animation
     * @param imageView Animated ImageView
     */
    fun createMenuBtnAnim(imageView: ImageView, HEIGHT: Int) {
        menuBtnAnim = SpriteAnimation(
                imageView, Duration.millis(1000.0),
                42, 7,
                OFFSET_X, OFFSET_Y,
                WIDTH, HEIGHT
        )
        menuBtnAnim.cycleCount = 1
    }

    /**
     * Translation effect in the settings stage
     * @param pane Pane which is translated
     * @param duration The duration of the translate effect
     * @param startVal The X position before the TranslateTransition
     * @param endVal The X position after the TranslateTransition
     */
    fun translate(pane: Any, duration: Int, startVal: Double, endVal: Double) {
        if (pane is Node) {
            val translateTransition = TranslateTransition(Duration.millis(duration.toDouble()), pane)
            translateTransition.fromX = startVal
            translateTransition.toX = endVal
            translateTransition.cycleCount = 1
            translateTransition.isAutoReverse = true
            translateTransition.play()
        }
    }

    /**
     * Fading effect.
     * @param pane Pane which is faded in/out
     * @param duration The duration of the fade effect
     * @param startVal The opacity value before the FadeTransition
     * @param endVal The final opacity value after the FadeTransition
     */
    fun fade(pane: Any, duration: Int, startVal: Double, endVal: Double): FadeTransition {
        val fadeTransition = FadeTransition(Duration.millis(duration.toDouble()), pane as Node)
        fadeTransition.fromValue = startVal
        fadeTransition.toValue = endVal
        fadeTransition.cycleCount = 1
        fadeTransition.isAutoReverse = true
        return fadeTransition
    }

    /**
     * Fading effect of the status label
     * @param label The affected label
     * @param type Type of the status (success|fail|neutral)
     * @param text Status text
     */
    fun statusFade(label: Label, type: String, text: String) {
        if (Configuration.animations) {
            val fadeTransition = FadeTransition(Duration.millis(700.0), label)
            fadeTransition.fromValue = 0.0
            fadeTransition.toValue = 1.0
            when (type) {
                "success" -> {
                    label.style = "-fx-text-fill: #55c4fe; -fx-opacity: 0;"
                    fadeTransition.cycleCount = 1
                }
                "fail" -> {
                    label.style = "-fx-text-fill: red; -fx-opacity: 0;"
                    fadeTransition.cycleCount = 1
                }
                "neutral" -> {
                    label.style = "-fx-text-fill: white; -fx-opacity: 0;"
                    fadeTransition.cycleCount = 1
                }
            }
            fadeTransition.isAutoReverse = true
            fadeTransition.stop()
            fadeTransition.play()
        } else {
            when (type) {
                "success" -> label.style = "-fx-text-fill: #55c4fe; -fx-opacity: 1;"
                "fail" -> label.style = "-fx-text-fill: red; -fx-opacity: 1;"
                "neutral" -> label.style = "-fx-text-fill: white; -fx-opacity: 1;"
            }
        }
        label.text = text
    }
}
