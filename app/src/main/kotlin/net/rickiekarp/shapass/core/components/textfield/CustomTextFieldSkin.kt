package net.rickiekarp.shapass.core.components.textfield

import javafx.scene.control.skin.TextFieldSkin

class CustomTextFieldSkin(textField: CustomTextField) : TextFieldSkin(textField) {

    var shouldMask = true

    override fun maskText(txt: String): String {
        if (!shouldMask)
            return txt

        val n = skinnable.length
        val stringBuilder = StringBuilder(n)
        for (i in 0 until n) {
            val bullet = '\u2022'
            stringBuilder.append(bullet)
        }

        return stringBuilder.toString()
    }
}