package net.rickiekarp.core.util

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent

object ClipboardUtil {

    /**
     * Copies the given string to the clipboard
     * @param stringContent String to copy
     */
    fun setStringToClipboard(stringContent: String) {
        val content = ClipboardContent()
        content.putString(stringContent)
        Clipboard.getSystemClipboard().setContent(content)
    }
}
