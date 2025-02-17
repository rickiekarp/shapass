package net.rickiekarp.core.ui.anim

import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView
import javafx.util.Duration
import kotlin.math.floor
import kotlin.math.min

class SpriteAnimation(
        private val imageView: ImageView,
        duration: Duration,
        private val count: Int, private val columns: Int,
        private val offsetX: Int, private val offsetY: Int,
        private val width: Int, private val height: Int) : Transition() {

    private var lastIndex: Int = 0

    init {
        cycleDuration = duration
        interpolator = Interpolator.LINEAR
    }

    override fun interpolate(k: Double) {
        val index = min(floor(k * count).toInt(), count - 1)
        if (index != lastIndex) {
            val x = index % columns * width + offsetX
            val y = index / columns * height + offsetY
            imageView.viewport = Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
            lastIndex = index
        }
    }
}