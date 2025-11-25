package net.rickiekarp.shapass.core.util.image

import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.text.Font
import net.rickiekarp.shapass.core.enums.FontType
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class TextToImage {
    private val imageWidth = 512
    private val imageHeight = 512
    private val outFormat = "png"

    fun saveToImage(text: String, fontSize: Int, fontType: FontType, outFile: String) {
        run {
            val image = textToImage(text, fontSize, fontType)
            saveToFile(image, outFile)
        }
    }

    private fun saveToFile(image: Image, outPath: String) {
        val outputFile = File(outPath)
        val bImage = SwingFXUtils.fromFXImage(image, null)
        try {
            ImageIO.write(bImage, outFormat, outputFile)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun textToImage(text: String, fontSize: Int, fontType: FontType): Image {
        val label = Label(text)
        label.setMinSize(imageWidth.toDouble(), imageHeight.toDouble())
        label.setMaxSize(imageWidth.toDouble(), imageHeight.toDouble())
        label.setPrefSize(imageWidth.toDouble(), imageHeight.toDouble())
        label.style = "-fx-background-color:#191919; -fx-text-fill:white;"
        label.isWrapText = true
        label.alignment = Pos.CENTER
        label.padding = Insets(50.0, 50.0, 50.0, 50.0)

        val url = this.javaClass.classLoader.getResource(fontType.getFilePath())
        if (url != null) {
            val font = Font.loadFont(url.toExternalForm(), fontSize.toDouble())
            label.font = font
        }

        val scene = Scene(Group(label))
        val img = WritableImage(imageWidth, imageHeight)
        scene.snapshot(img)
        return img
    }
}