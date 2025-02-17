package net.rickiekarp.core.components.button

import javafx.animation.*
import javafx.scene.control.Button
import javafx.scene.control.Skin
import javafx.scene.control.skin.ButtonSkin
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.util.Duration

class MaterialDesignButton(text: String) : Button(text) {
    private var circleRipple: Circle? = null
    private val rippleClip = Rectangle()
    private val rippleDuration = Duration.millis(250.0)
    private var lastRippleHeight = 0.0
    private var lastRippleWidth = 0.0
    private val rippleColor = Color(0.0, 0.0, 0.0, 0.61)

    init {

        styleClass.addAll("md-button")

        createRippleEffect()
    }

    override fun createDefaultSkin(): Skin<*> {
        val buttonSkin = ButtonSkin(this)
        this.children.add(0, circleRipple)
        return buttonSkin
    }

    private fun createRippleEffect() {
        circleRipple = Circle(0.1, rippleColor)
        circleRipple!!.opacity = 0.0
        //Optional: BoxBlur effect
        //circleRipple.setEffect(new BoxBlur(10, 3, 2));

        // Fade effect bit longer to show edges on the end
        val fadeTransition = FadeTransition(rippleDuration, circleRipple)
        fadeTransition.interpolator = Interpolator.EASE_OUT
        fadeTransition.fromValue = 1.0
        fadeTransition.toValue = 0.0

        val scaleRippleTimeline = Timeline()

        val parallelTransition = SequentialTransition()
        parallelTransition.children.addAll(
                scaleRippleTimeline,
                fadeTransition
        )

        parallelTransition.setOnFinished {
            circleRipple!!.opacity = 0.0
            circleRipple!!.radius = 0.1
        }

        this.addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
            parallelTransition.stop()
            parallelTransition.onFinished.handle(null)

            circleRipple!!.centerX = event.x
            circleRipple!!.centerY = event.y

            // Recalculate ripple size if size of button from last time was changed
            if (width != lastRippleWidth || height != lastRippleHeight) {
                lastRippleWidth = width
                lastRippleHeight = height

                rippleClip.width = lastRippleWidth
                rippleClip.height = lastRippleHeight

                try {
                    rippleClip.arcHeight = this.background.fills[0].radii.topLeftHorizontalRadius
                    rippleClip.arcWidth = this.background.fills[0].radii.topLeftHorizontalRadius
                    circleRipple!!.clip = rippleClip
                } catch (e: Exception) {

                }

                // Getting 45% of longest button's length, because we want edge of ripple effect always visible
                val circleRippleRadius = Math.max(height, width) * 0.45
                val keyValue = KeyValue(circleRipple!!.radiusProperty(), circleRippleRadius, Interpolator.EASE_OUT)
                val keyFrame = KeyFrame(rippleDuration, keyValue)
                scaleRippleTimeline.keyFrames.clear()
                scaleRippleTimeline.keyFrames.add(keyFrame)
            }

            parallelTransition.playFromStart()
        }
    }

    fun setRippleColor(color: Color) {
        circleRipple!!.fill = color
    }
}
