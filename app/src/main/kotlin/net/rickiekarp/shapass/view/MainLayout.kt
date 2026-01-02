package net.rickiekarp.shapass.view

import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.BorderPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import net.rickiekarp.shapass.core.AppContext
import net.rickiekarp.shapass.core.components.textfield.CustomTextField
import net.rickiekarp.shapass.core.components.textfield.CustomTextFieldSkin
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.provider.LocalizationProvider
import net.rickiekarp.shapass.core.util.ClipboardUtil
import net.rickiekarp.shapass.core.view.AboutScene
import net.rickiekarp.shapass.core.view.MainScene
import net.rickiekarp.shapass.core.view.layout.AppLayout
import net.rickiekarp.shapass.enum.TextCodingType
import net.rickiekarp.shapass.libloader.GoLibTransformer

class MainLayout : AppLayout {
    private var isHmac = false
    private var isComplex = true

    private lateinit var mainGrid: GridPane
    private lateinit var sentenceTF: CustomTextField

    private val mainLayout: Node
        get() {
            val mainContent = BorderPane()

            mainGrid = GridPane()
            mainGrid.padding = Insets(5.0, 3.0, 0.0, 3.0)

            val encryptButtons = HBox()
            encryptButtons.spacing = 7.0
            encryptButtons.alignment = Pos.CENTER_LEFT

            val controls = HBox()
            controls.padding = Insets(3.0, 3.0, 3.0, 7.0)
            controls.spacing = 2.0

            val status = Label(AppContext.context.applicationName)
            status.style = "-fx-font-size: 9pt;"
            controls.children.add(status)
            val column1 = ColumnConstraints()
            column1.percentWidth = 15.0
            val column2 = ColumnConstraints()
            column2.percentWidth = 21.0
            val column3 = ColumnConstraints()
            column3.percentWidth = 21.0
            val column4 = ColumnConstraints()
            column4.percentWidth = 21.0
            val column5 = ColumnConstraints()
            column5.percentWidth = 21.0
            mainGrid.columnConstraints.addAll(column1, column2, column3, column4, column5)

            mainGrid.hgap = 5.0
            mainGrid.vgap = 5.0
            val sentenceLabel = Label(LocalizationProvider.getString("u_sentence"))
            sentenceLabel.style = "-fx-font-size: 9pt;"
            GridPane.setConstraints(sentenceLabel, 0, 0)
            GridPane.setHalignment(sentenceLabel, HPos.CENTER)
            mainGrid.children.add(sentenceLabel)
            sentenceLabel.tooltip = Tooltip(LocalizationProvider.getString("type_sentence_tip"))

            sentenceTF = CustomTextField()
            sentenceTF.tooltip = Tooltip(LocalizationProvider.getString("type_sentence_tip"))
            val sentenceTextFieldSkin = CustomTextFieldSkin(sentenceTF)
            sentenceTextFieldSkin.shouldMask = true
            sentenceTF.skin = sentenceTextFieldSkin
            GridPane.setConstraints(sentenceTF, 1, 0)
            GridPane.setColumnSpan(sentenceTF, 5)
            mainGrid.children.add(sentenceTF)

            val viewMode = CheckBox(LocalizationProvider.getString("vs"))
            viewMode.style = "-fx-font-size: 9pt;"
            viewMode.tooltip = Tooltip(LocalizationProvider.getString("vs_tip"))
            GridPane.setConstraints(viewMode, 3, 1)
            GridPane.setHalignment(viewMode, HPos.LEFT)
            mainGrid.children.add(viewMode)

            val hmacMode = CheckBox(LocalizationProvider.getString("hm"))
            hmacMode.style = "-fx-font-size: 8pt;"
            hmacMode.tooltip = Tooltip(LocalizationProvider.getString("hmac_tip"))
            hmacMode.isSelected = isHmac
            GridPane.setConstraints(hmacMode, 1, 1)
            mainGrid.children.add(hmacMode)

            val complexMode = CheckBox(LocalizationProvider.getString("comp"))
            complexMode.style = "-fx-font-size: 8pt;"
            complexMode.tooltip = Tooltip(LocalizationProvider.getString("specialMode_tip"))
            complexMode.isSelected = isComplex
            GridPane.setConstraints(complexMode, 2, 1)
            mainGrid.children.add(complexMode)

            val sha256Btn = Button(LocalizationProvider.getString("sha256_label"))
            sha256Btn.style = "-fx-font-size: 10pt;"
            sha256Btn.tooltip = Tooltip(LocalizationProvider.getString("a_40_char_tip"))
            sha256Btn.minWidth = 103.0
            encryptButtons.children.add(sha256Btn)

            val sha512Btn = Button(LocalizationProvider.getString("sha512_label"))
            sha512Btn.style = "-fx-font-size: 10pt;"
            sha512Btn.tooltip = Tooltip(LocalizationProvider.getString("a_28_char_tip"))
            sha512Btn.minWidth = 103.0
            encryptButtons.children.add(sha512Btn)

            val customBtn = Button(LocalizationProvider.getString("custom_label"))
            customBtn.style = "-fx-font-size: 10pt;"
            customBtn.tooltip = Tooltip(LocalizationProvider.getString("a_60_char_tip"))
            customBtn.minWidth = 103.0
            encryptButtons.children.add(customBtn)

            mainGrid.children.add(encryptButtons)
            GridPane.setConstraints(encryptButtons, 1, 2)
            GridPane.setColumnSpan(encryptButtons, 4)


            mainContent.center = mainGrid
            mainContent.bottom = controls

            sha256Btn.setOnAction {
                calcSha256()
                status.text = LocalizationProvider.getString("sha256_password_copied")
            }

            sha512Btn.setOnAction {
                calcSha512()
                status.text = LocalizationProvider.getString("sha512_password_copied")
            }

            customBtn.setOnAction {
                calcCustom()
                status.text = LocalizationProvider.getString("custom_password_copied")
            }

            viewMode.selectedProperty().addListener { _, _, newVal ->
                sentenceTextFieldSkin.shouldMask = !newVal!!
                sentenceTF.text = sentenceTF.text
            }

            hmacMode.selectedProperty().addListener { _, _, newVal ->
                isHmac = newVal
                if (newVal) {
                    status.text = LocalizationProvider.getString("hmac_on")
                } else {
                    status.text = LocalizationProvider.getString("hmac_off")
                }
            }

            complexMode.selectedProperty().addListener { _, _, newVal ->
                isComplex = newVal
                if (newVal) {
                    status.text = LocalizationProvider.getString("comp_on")
                } else {
                    status.text = LocalizationProvider.getString("comp_off")
                }
            }

            if (DebugHelper.DEBUG) {
                mainGrid.isGridLinesVisible = true
                mainGrid.style = "-fx-background-color: gray"
                encryptButtons.style = "-fx-background-color: #A36699;"
                controls.style = "-fx-background-color: #336699;"
            }

            setupEncryptDecryptButtons()

            val helpBtn = Button(LocalizationProvider.getString("help_label"))
            helpBtn.styleClass.add("decoration-button-other")
            helpBtn.tooltip = Tooltip(
                LocalizationProvider.getString("help_tip") + " " + AppContext.context.applicationName
            )
            helpBtn.setOnAction { AboutScene() }

            MainScene.mainScene.windowScene!!.win.titleBarButtonBox.children.addAll(helpBtn)
            return mainContent
        }

    private fun setupEncryptDecryptButtons() {
        val decodeButton = Button(LocalizationProvider.getString("decode_label_short"))
        decodeButton.style = "-fx-font-size: 9pt;"
        decodeButton.tooltip = Tooltip(LocalizationProvider.getString("decode_tip"))
        GridPane.setConstraints(decodeButton, 0, 1)
        GridPane.setHalignment(decodeButton, HPos.CENTER)
        mainGrid.children.add(decodeButton)
        decodeButton.setOnAction { TextCoding(TextCodingType.DECODE) }

        val encodeButton = Button(LocalizationProvider.getString("encode_label_short"))
        encodeButton.style = "-fx-font-size: 9pt;"
        encodeButton.tooltip = Tooltip(LocalizationProvider.getString("encode_tip"))
        GridPane.setConstraints(encodeButton, 0, 2)
        GridPane.setHalignment(encodeButton, HPos.CENTER)
        mainGrid.children.add(encodeButton)
        encodeButton.setOnAction { TextCoding(TextCodingType.ENCODE) }
    }

    private fun calcSha256() {
        val input = sentenceTF.text
        val result: String = if (isHmac) {
            val secret = GoLibTransformer.VelesLib.CalculateHashMD5(sentenceTF.text)
            GoLibTransformer.VelesLib.CalculateHashSha3256HMAC(input, secret)
        } else {
            GoLibTransformer.VelesLib.CalculateHashSha3256(input)
        }
        copyToClipboard(result)
    }

    private fun calcSha512() {
        val input = sentenceTF.text
        val result: String = if (isHmac) {
            val secret = GoLibTransformer.VelesLib.CalculateHashMD5(sentenceTF.text)
            GoLibTransformer.VelesLib.CalculateHashSha3512HMAC(input, secret)
        } else {
            GoLibTransformer.VelesLib.CalculateHashSha3512(input)
        }
        copyToClipboard(result)
    }

    private fun calcCustom() {
        val result = GoLibTransformer.VelesLib.CalculateHashCustom(
            sentenceTF.text,
            64,
            15,     // CYRILLIC, GREEK, LATIN, SPECIAL_CHARACTERS
            1        // COMPLEX_MODE = ON
        )
        copyToClipboard(result)
    }

    private fun copyToClipboard(data: String) {
        ClipboardUtil.setStringToClipboard(data)
    }

    override val layout: Node
        get() = mainLayout

    override fun postInit() {
        sentenceTF.requestFocus()
    }
}
