package net.rickiekarp.core.util.crypt

import javafx.scene.paint.Color

object ColorCoder {
    var colorArray = arrayOf(Color.color(0.0, 0.0, 1.0), //blue
            Color.color(0.0, 0.0, 0.0), //black
            Color.color(0.0, 1.0, 0.0), //green
            Color.color(1.0, 0.5, 0.0), //orange
            Color.color(1.0, 0.0, 0.0), //red
            Color.color(1.0, 1.0, 0.0), //yellow
            Color.color(0.6, 0.1, 0.9), //purple
            Color.color(0.0, 1.0, 1.0)      //cyan
    )
}