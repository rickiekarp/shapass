package net.rickiekarp.shapass.view

import javafx.collections.FXCollections
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.Stage
import net.rickiekarp.shapass.core.util.crypt.CustomCoder
import net.rickiekarp.shapass.core.util.image.TextToImage
import net.rickiekarp.shapass.core.components.FoldableListCell
import net.rickiekarp.shapass.core.components.textfield.CustomTextField
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.debug.LogFileHandler
import net.rickiekarp.shapass.core.enums.AlphabetType
import net.rickiekarp.shapass.core.enums.CustomCoderType
import net.rickiekarp.shapass.core.enums.FontType
import net.rickiekarp.shapass.core.model.CustomCoderConfig
import net.rickiekarp.shapass.core.model.SettingEntry
import net.rickiekarp.shapass.core.provider.LocalizationProvider
import net.rickiekarp.shapass.core.ui.windowmanager.ImageLoader
import net.rickiekarp.shapass.core.ui.windowmanager.WindowScene
import net.rickiekarp.shapass.core.ui.windowmanager.WindowStage
import net.rickiekarp.shapass.core.view.MainScene
import net.rickiekarp.shapass.enum.TextCodingType
import net.rickiekarp.shapass.math.PerlinNoise2D
import java.util.*

class TextCoding(textCodingType: TextCodingType) {
    private var grid: GridPane? = null
    private var controls: HBox? = null
    private var codingType = textCodingType
    private val windowIdentifier = "textCoding"

    private lateinit var seedTextField: TextField
    private lateinit var inputTextArea: TextArea
    private lateinit var customCoderVersionBox: ComboBox<CustomCoderType>
    private lateinit var preserveWhiteSpacesCheckBox: CheckBox

    private lateinit var noiseImageView: ImageView

    private val defaultFontSize = 40
    private val defaultCoderType = CustomCoderType.V2

    private val coderConfig = CustomCoderConfig(
        defaultCoderType,
        "",
        mutableMapOf(
            AlphabetType.CYRILLIC to true,
            AlphabetType.LATIN to true,
            AlphabetType.GREEK to true,
        ),
        false
    )

    private var perlinNoiseGenerator =
        PerlinNoise2D(coderConfig.coderType.getDefaultNoiseConfig())

    init {
        create()
        onEnable()
    }

    private fun onEnable() {
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                if (MainScene.stageStack.getStageByIdentifier(windowIdentifier) == null) {
                    timer.cancel()
                    return
                }

                perlinNoiseGenerator.incrementTime()
                val perlinNoiseImage = perlinNoiseGenerator.getNoiseImage()
                val image: Image = SwingFXUtils.toFXImage(perlinNoiseImage, null)
                noiseImageView.image = image
            }
        }
        timer.schedule(task,0L, 25L)
    }

    private fun create() {
        val infoStage = Stage()
        infoStage.title = LocalizationProvider.getString(windowIdentifier)
        infoStage.icons.add(ImageLoader.getAppIconSmall())
        infoStage.isResizable = true
        infoStage.width = 900.0
        infoStage.height = 700.0

        val contentVbox = BorderPane()
        contentVbox.center = createLayout()

        val aboutWindow = WindowScene(WindowStage(windowIdentifier, infoStage), contentVbox, 1)

        infoStage.scene = aboutWindow
        infoStage.show()

        MainScene.stageStack.push(WindowStage(windowIdentifier, infoStage))
    }

    private val content: BorderPane
        get() {
            val borderpane = BorderPane()
            borderpane.style = "-fx-background-color: #1d1d1d;"

            val hBox = HBox()
            grid = GridPane()
            controls = HBox()

            grid!!.vgap = 8.0
            grid!!.hgap = 16.0
            grid!!.padding = Insets(20.0, 15.0, 20.0, 20.0)

            val title = Label(codingType.name)
            title.style = "-fx-font-size: 16pt;"
            GridPane.setHalignment(title, HPos.CENTER)
            GridPane.setConstraints(title, 0, 0)
            GridPane.setColumnSpan(title, 2)
            HBox.setHgrow(title, Priority.ALWAYS)
            grid!!.children.add(title)

            seedTextField = TextField()
            seedTextField.style = "-fx-font-size: 10pt;"
            seedTextField.promptText = LocalizationProvider.getString("seed")
            GridPane.setConstraints(seedTextField, 0, 1)
            GridPane.setColumnSpan(seedTextField, 2)
            seedTextField.prefWidth = 30.0
            grid!!.children.add(seedTextField)

            inputTextArea = TextArea()
            GridPane.setConstraints(inputTextArea, 0, 2)
            GridPane.setVgrow(inputTextArea, Priority.ALWAYS)
            inputTextArea.isWrapText = true
            grid!!.children.add(inputTextArea)

            val output = TextArea()
            output.isEditable = false
            output.isWrapText = true
            GridPane.setConstraints(output, 1, 2)
            GridPane.setVgrow(output, Priority.ALWAYS)
            grid!!.children.add(output)

            controls!!.padding = Insets(10.0, 7.0, 10.0, 7.0)
            controls!!.spacing = 10.0
            controls!!.alignment = Pos.CENTER

            when (codingType) {
                TextCodingType.ENCODE -> {
                    val codingButton = Button(LocalizationProvider.getString("encode"))
                    codingButton.setOnAction { _ ->
                        output.clear()
                        coderConfig.coderType = customCoderVersionBox.value
                        coderConfig.baseSeed = seedTextField.text
                        coderConfig.preserveWhiteSpaces = preserveWhiteSpacesCheckBox.isSelected
                        coderConfig.noiseGenerator = perlinNoiseGenerator
                        output.text = CustomCoder.encode(inputTextArea.text, coderConfig)
                    }
                    controls!!.children.add(codingButton)
                }
                TextCodingType.DECODE -> {
                    val codingButton = Button(LocalizationProvider.getString("decode"))
                    codingButton.setOnAction { _ ->
                        output.clear()
                        coderConfig.coderType = customCoderVersionBox.value
                        coderConfig.baseSeed = seedTextField.text
                        coderConfig.preserveWhiteSpaces = preserveWhiteSpacesCheckBox.isSelected
                        coderConfig.noiseGenerator = perlinNoiseGenerator
                        output.text = CustomCoder.decode(inputTextArea.text, coderConfig)
                    }
                    controls!!.children.add(codingButton)
                }
            }

            hBox.children.add(grid)
            hBox.alignment = Pos.CENTER

            borderpane.center = hBox
            borderpane.bottom = controls

            return borderpane
        }

    private fun createBox2(): VBox {
        val content = VBox()
        content.spacing = 5.0

        val option2Desc = Label(LocalizationProvider.getString("Setting.CharacterSet.Description"))
        option2Desc.isWrapText = true
        option2Desc.style = "-fx-font-size: 9pt;"
        option2Desc.maxWidth = 175.0

        val latin = CheckBox(LocalizationProvider.getString("latin"))
        latin.isSelected = true
        latin.setOnAction { _ ->
            coderConfig.characterSetConfig[AlphabetType.LATIN] = !coderConfig.characterSetConfig[AlphabetType.LATIN]!!
            LogFileHandler.logger.config("change_latin_option: " + !coderConfig.characterSetConfig[AlphabetType.LATIN]!! + " -> " + coderConfig.characterSetConfig[AlphabetType.LATIN])
        }

        val cyrillic = CheckBox(LocalizationProvider.getString("cyrillic"))
        cyrillic.isSelected = true
        cyrillic.setOnAction { _ ->
            coderConfig.characterSetConfig[AlphabetType.CYRILLIC] = !coderConfig.characterSetConfig[AlphabetType.CYRILLIC]!!
            LogFileHandler.logger.config("change_cyrillic_option: " + !coderConfig.characterSetConfig[AlphabetType.CYRILLIC]!! + " -> " + coderConfig.characterSetConfig[AlphabetType.CYRILLIC])
        }

        val greek = CheckBox(LocalizationProvider.getString("greek"))
        greek.isSelected = true
        greek.setOnAction { _ ->
            coderConfig.characterSetConfig[AlphabetType.GREEK] = !coderConfig.characterSetConfig[AlphabetType.GREEK]!!
            LogFileHandler.logger.config("change_greek_option: " + !coderConfig.characterSetConfig[AlphabetType.GREEK]!! + " -> " + coderConfig.characterSetConfig[AlphabetType.GREEK])
        }

        content.children.addAll(option2Desc, latin, cyrillic, greek)
        return content
    }

    private fun createBox3(): VBox {
        val content = VBox()
        content.spacing = 5.0

        val option2Desc = Label(LocalizationProvider.getString("Setting.ImageExport.Description"))
        option2Desc.isWrapText = true
        option2Desc.style = "-fx-font-size: 9pt;"
        option2Desc.maxWidth = 175.0

        val cBoxVersion = ComboBox<FontType>()
        cBoxVersion.items.addAll(FontType.entries.toTypedArray())
        cBoxVersion.selectionModel.select(0)
        cBoxVersion.minWidth = 100.0
        cBoxVersion.maxWidth = 150.0

        val fontSizeDesc = Label(LocalizationProvider.getString("Setting.ImageExport.FontSizeDesc"))
        fontSizeDesc.isWrapText = true
        fontSizeDesc.style = "-fx-font-size: 9pt;"
        fontSizeDesc.maxWidth = 175.0

        val fontSize = CustomTextField()
        fontSize.setRestrict("[0-9]")
        fontSize.text = defaultFontSize.toString()
        fontSize.minWidth = 100.0
        fontSize.maxWidth = 150.0

        val applyEncodingDesc = Label(LocalizationProvider.getString("Setting.ImageExport.ApplyEncodingDesc"))
        applyEncodingDesc.isWrapText = true
        applyEncodingDesc.style = "-fx-font-size: 9pt;"
        applyEncodingDesc.maxWidth = 175.0

        val applyEncoding = CheckBox(LocalizationProvider.getString("Setting.ImageExport.ApplyEncoding"))
        applyEncoding.isSelected = false

        val outputDesc = Label(LocalizationProvider.getString("Setting.ImageExport.SaveDescription"))
        outputDesc.isWrapText = true
        outputDesc.style = "-fx-font-size: 9pt;"
        outputDesc.maxWidth = 175.0

        val codingButton = Button(LocalizationProvider.getString("export"))
        codingButton.setOnAction { _ ->
            var inputText = inputTextArea.text
            if (applyEncoding.isSelected) {
                coderConfig.coderType = customCoderVersionBox.value
                coderConfig.baseSeed = seedTextField.text
                coderConfig.preserveWhiteSpaces = preserveWhiteSpacesCheckBox.isSelected
                coderConfig.noiseGenerator = perlinNoiseGenerator
                inputText = CustomCoder.encode(inputText, coderConfig)
            }

            if (inputText.isNotEmpty()) {
                var fontSizeAsInt = defaultFontSize
                if (fontSize.text.isNotEmpty()) {
                    fontSizeAsInt = fontSize.text.toInt()
                }

                TextToImage().saveToImage(inputText, fontSizeAsInt, cBoxVersion.value,System.getProperty("user.home")+"/export.png")
            }
        }

        content.children.addAll(
            option2Desc, cBoxVersion,
            fontSizeDesc, fontSize,
            applyEncodingDesc, applyEncoding,
            outputDesc, codingButton
        )
        return content
    }

    private fun createBox1(): VBox {
        val content = VBox()
        content.spacing = 5.0

        val option1Desc = Label(LocalizationProvider.getString("Setting.AlgorithmVersion.Description"))
        option1Desc.isWrapText = true
        option1Desc.style = "-fx-font-size: 9pt;"
        option1Desc.maxWidth = 175.0

        customCoderVersionBox = ComboBox<CustomCoderType>()
        customCoderVersionBox.items.addAll(CustomCoderType.entries.toTypedArray())
        customCoderVersionBox.selectionModel.select(coderConfig.coderType)
        customCoderVersionBox.minWidth = 100.0
        customCoderVersionBox.valueProperty().addListener { _, _, newValue ->
            perlinNoiseGenerator =
                PerlinNoise2D(newValue!!.getDefaultNoiseConfig())
            val perlinNoiseImage = perlinNoiseGenerator.getNoiseImage()
            val image: Image = SwingFXUtils.toFXImage(perlinNoiseImage, null)
            noiseImageView.image = image
        }

        val noiseImage = perlinNoiseGenerator.getNoiseImage()
        val image: Image = SwingFXUtils.toFXImage(noiseImage, null)
        noiseImageView = ImageView(image)
        noiseImageView.fitHeightProperty().setValue(35)
        noiseImageView.fitWidthProperty().setValue(35)

        val settingsLabel = Label(LocalizationProvider.getString("settings"))
        settingsLabel.isWrapText = true
        settingsLabel.style = "-fx-font-size: 9pt;"
        settingsLabel.maxWidth = 175.0

        preserveWhiteSpacesCheckBox = CheckBox(LocalizationProvider.getString("Setting.Algorithm.PreserveWhitespaces"))
        preserveWhiteSpacesCheckBox.isSelected = false

        val hBox = HBox()
        hBox.alignment = Pos.CENTER_LEFT
        hBox.spacing = 5.0
        hBox.children.addAll(customCoderVersionBox, noiseImageView)

        content.children.addAll(option1Desc, hBox, settingsLabel, preserveWhiteSpacesCheckBox)
        return content
    }

    private fun createLayout(): Node {
        val mainContent = BorderPane()

        val columnConstraints = ColumnConstraints()
        columnConstraints.isFillWidth = true
        columnConstraints.hgrow = Priority.ALWAYS

        val controls = AnchorPane()
        controls.minHeight = 50.0

        val settingsGrid = GridPane()
        settingsGrid.prefWidth = 200.0
        settingsGrid.vgap = 10.0
        settingsGrid.padding = Insets(5.0, 0.0, 0.0, 0.0)  //padding top, left, bottom, right
        settingsGrid.alignment = Pos.BASELINE_CENTER

        //SETTINGS LIST
        val list = ListView<SettingEntry>()
        GridPane.setConstraints(list, 0, 0)
        GridPane.setVgrow(list, Priority.ALWAYS)
        settingsGrid.children.add(list)

        val items = FXCollections.observableArrayList<SettingEntry>()
        items.add(SettingEntry("Setting.AlgorithmVersion.Title",false, createBox1()))
        items.add(SettingEntry("Setting.CharacterSet.Title",false, createBox2()))

        if (codingType == TextCodingType.ENCODE) {
            items.add(SettingEntry("Setting.ImageExport.Title",false, createBox3()))
        }

        list.items = items

        list.setCellFactory { FoldableListCell(list) }

        GridPane.setConstraints(settingsGrid, 1, 0)

        //add components to borderpane
        mainContent.center = content
        mainContent.right = settingsGrid

        //debug colors
        if (DebugHelper.isDebugVersion) {
            controls.style = "-fx-background-color: #336699;"
            settingsGrid.style = "-fx-background-color: #444444"
            settingsGrid.isGridLinesVisible = true
        } else {
            controls.style = "-fx-background-color: #1d1d1d;"
            settingsGrid.style = null
            settingsGrid.isGridLinesVisible = false
        }

        return mainContent
    }
}