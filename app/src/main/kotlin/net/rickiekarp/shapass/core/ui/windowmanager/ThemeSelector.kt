package net.rickiekarp.core.ui.windowmanager

import javafx.scene.Scene
import javafx.scene.paint.Color
import net.rickiekarp.core.settings.Configuration
import net.rickiekarp.core.view.MainScene

object ThemeSelector {

    val DARK_THEME_CSS = "themes/DarkTheme.css"
    private val LIGHT_THEME_CSS = "themes/LightTheme.css"

    /**
     * Sets the theme according to the current themeState.
     * @param scene The scene
     */
    fun setTheme(scene: Scene, loader: ClassLoader) {
        when (Configuration.themeState) {
            0 -> {
                val darkStyle = loader.getResource(DARK_THEME_CSS)
                if (!scene.stylesheets.contains(DARK_THEME_CSS)) {
                    scene.stylesheets.clear()
                }
                scene.stylesheets.add(darkStyle!!.toString())
            }
            1 -> {
                val lightStyle = loader.getResource(LIGHT_THEME_CSS)
                if (!scene.stylesheets.contains(LIGHT_THEME_CSS)) {
                    scene.stylesheets.clear()
                }
                scene.stylesheets.add(lightStyle!!.toString())
            }
        }
    }

    /**
     * If the theme is changed, all active stages are updated.
     */
    fun onThemeChange() {
        println("fixme: onThemeChange()")
        //        if (MainScene.mainScene.getWindowScene() != null) { setTheme(MainScene.mainScene.getWindowScene().getWin().getWindowStage().getStage().getScene()); }
        //        if (SettingsScene.settingsScene != null) { setTheme(SettingsScene.settingsScene.getSettingsWindow().getWin().getWindowStage().getStage().getScene()); }
        ////        if (AboutScene.aboutScene != null) { setTheme(AboutScene.aboutScene.getAboutWindow().getWin().getWindowStage().getScene()); }
        //        if (CommandsScene.commandsScene != null) { setTheme(CommandsScene.commandsScene.getCommandsWindow().getWin().getScene()); }
    }


    /**
     * Changes the color scheme of the application
     * @param schemeIdx The scheme index
     */
    fun changeColorScheme(schemeIdx: Int) {
        when (schemeIdx) {
            0 -> {
                Window.colorTheme = "darkgray"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            1 -> {
                Window.colorTheme = "gray"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            2 -> {
                Window.colorTheme = "black"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            3 -> {
                Window.colorTheme = "red"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            4 -> {
                Window.colorTheme = "orange"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            5 -> {
                Window.colorTheme = "yellow"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            6 -> {
                Window.colorTheme = "blue"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            7 -> {
                Window.colorTheme = "magenta"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            8 -> {
                Window.colorTheme = "purple"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            9 -> {
                Window.colorTheme = "green"
                MainScene.mainScene.windowScene!!.win.clientArea.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
            else -> {
                Window.colorTheme = "darkgray"
                MainScene.mainScene.windowScene!!.win.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
            }
        }

        for (i in 0 until MainScene.mainScene.sceneViewStack.size) {
            MainScene.mainScene.sceneViewStack[i].style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
        }

        MainScene.mainScene.windowScene!!.win.style = "-fx-accent: " + Window.colorTheme + ";" + "-fx-focus-color: " + Window.colorTheme + ";"
    }

    /**
     * If the theme is changed, all active stages are updated.
     */
    fun changeWindowShadowColor(focus: Boolean, value: String) {
        if (focus) {
            Configuration.shadowColorFocused = Color.valueOf(value)
            Window.dsFocused.color = Configuration.shadowColorFocused
        } else {
            Configuration.shadowColorNotFocused = Color.valueOf(value)
            Window.dsNotFocused.color = Configuration.shadowColorNotFocused
        }
    }

    /**
     * If the theme is changed, all active stages are updated.
     */
    fun changeDecorationColor(newValue: String) {
        println("fixme: onThemeChange()")
        Configuration.decorationColor = Color.valueOf(newValue)
        if (MainScene.mainScene.windowScene != null) {
            MainScene.mainScene.windowScene!!.win.setDecorationColor()
        }
        //        if (SettingsScene.Companion.getSettingsScene() != null) { SettingsScene.Companion.getSettingsScene().getSettingsWindow().getWin().setDecorationColor(); }
        //        if (AboutScene.aboutScene != null) { AboutScene.aboutScene.getAboutWindow().getWin().setDecorationColor(); }
        //        if (CommandsScene.Companion.getCommandsScene() != null) { CommandsScene.Companion.getCommandsScene().getCommandsWindow().getWin().setDecorationColor(); }
    }

    /**
     * Converts a color code to hex format.
     * Example: 0x1d1d1dff -> #1d1d1d
     */
    fun getColorHexString(color: Color): String {
        val green = (color.green * 255).toInt()
        var greenString = Integer.toHexString(green)
        if (greenString.length == 1) {
            greenString += "0"
        } //append a '0' if string length is 1

        val red = (color.red * 255).toInt()
        var redString = Integer.toHexString(red)
        if (redString.length == 1) {
            redString += "0"
        } //append a '0' if string length is 1

        val blue = (color.blue * 255).toInt()
        var blueString = Integer.toHexString(blue)
        if (blueString.length == 1) {
            blueString += "0"
        } //append a '0' if string length is 1

        return "#$redString$greenString$blueString"
    }
}
