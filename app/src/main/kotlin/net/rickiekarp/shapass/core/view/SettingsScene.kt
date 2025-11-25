package net.rickiekarp.shapass.core.view

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import net.rickiekarp.shapass.core.AppContext
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.debug.ExceptionHandler
import net.rickiekarp.shapass.core.debug.LogFileHandler
import net.rickiekarp.shapass.core.model.SettingsList
import net.rickiekarp.shapass.core.net.update.FileDownloader
import net.rickiekarp.shapass.core.net.update.UpdateChecker
import net.rickiekarp.shapass.core.provider.LocalizationProvider
import net.rickiekarp.shapass.core.settings.Configuration
import net.rickiekarp.shapass.core.settings.LoadSave
import net.rickiekarp.shapass.core.ui.anim.AnimationHandler
import net.rickiekarp.shapass.core.ui.tray.ToolTrayIcon
import net.rickiekarp.shapass.core.ui.windowmanager.ImageLoader
import net.rickiekarp.shapass.core.ui.windowmanager.ThemeSelector
import net.rickiekarp.shapass.core.ui.windowmanager.WindowScene
import net.rickiekarp.shapass.core.ui.windowmanager.WindowStage
import java.io.File
import java.lang.reflect.Field
import java.net.MalformedURLException
import java.net.URI
import java.util.logging.Level

/**
 * The Settings Stage GUI.
 */
class SettingsScene {
    private var settingsWindow: WindowScene? = null
    private var controls: AnchorPane? = null
    private var tabPane: TabPane? = null
    private val tabName = arrayOf("general", "appearance", "advanced")
    private val tabVBox = arrayOfNulls<VBox>(tabName.size)
    private var tab1ContentGrid: ArrayList<GridPane> = ArrayList(3)
    private var tab2ContentGrid: ArrayList<GridPane> = ArrayList(2)
    private var tab3ContentGrid: ArrayList<GridPane> = ArrayList(1)
    private val localeData = FXCollections.observableArrayList("English", "Deutsch")
    private var otherTable: TableView<SettingsList>? = null
    private var listData: ObservableList<SettingsList>? = null
    private var logCBox: CheckBox? = null
    private var animateCBox: CheckBox? = null
    private var sysBorderCBox: CheckBox? = null
    private var langCB: ComboBox<String>? = null
    private var themeCB: ComboBox<String>? = null
    private var colBox: ComboBox<String>? = null
    private val updateChannel = FXCollections.observableArrayList("stable", "dev")

    private val cfgLayout: BorderPane
        get() {

            val cfgContent = BorderPane()

            controls = AnchorPane()

            tabPane = TabPane()
            tabPane!!.side = Configuration.tabPosition

            val tabs = arrayOfNulls<Tab>(tabName.size)
            for (i in tabName.indices) {
                tabs[i] = Tab()
                tabs[i]!!.setText(LocalizationProvider.getString(tabName[i]))
                tabs[i]!!.setClosable(false)

                tabVBox[i] = VBox()
                tabVBox[i]!!.setPadding(Insets(0.0, 0.0, 0.0, 0.0))
                tabs[i]!!.setContent(tabVBox[i])
            }
            tabPane!!.tabs.add(tabs[0])
            tabPane!!.tabs.add(tabs[1])
            for (i in 0 until 3) {
                val gridPane = GridPane()
                gridPane.padding = Insets(15.0, 5.0, 5.0, 15.0)
                gridPane.vgap = 5.0
                gridPane.hgap = 10.0
                tab1ContentGrid.add(gridPane)
            }
            GridPane.setConstraints(tab1ContentGrid[0], 0, 0)
            GridPane.setConstraints(tab1ContentGrid[1], 0, 1)
            GridPane.setConstraints(tab1ContentGrid[2], 0, 2)

            val appLanguage = Label()
            appLanguage.text = LocalizationProvider.getString("languageSelection")
            appLanguage.style = "-fx-font-size: 12pt;"
            GridPane.setConstraints(appLanguage, 0, 0)
            tab1ContentGrid[0].children.add(appLanguage)

            langCB = ComboBox(localeData)
            langCB!!.setValue(localeData[LocalizationProvider.currentLocale])
            GridPane.setConstraints(langCB, 0, 1)
            tab1ContentGrid[0].children.add(langCB)

            val setChange = Label(LocalizationProvider.getString("restartOnCfgChange"))
            setChange.isVisible = false
            setChange.style = "-fx-text-fill: red;"
            GridPane.setConstraints(setChange, 0, 2)
            tab1ContentGrid[0].children.add(setChange)

            val pgUpdate = Label()
            pgUpdate.text = LocalizationProvider.getString("progUpdate")
            pgUpdate.style = "-fx-font-size: 12pt;"
            GridPane.setConstraints(pgUpdate, 0, 0)
            tab1ContentGrid[1].children.add(pgUpdate)

            val updateChannelBox = ComboBox<String>()
            updateChannelBox.style = "-fx-font-size: 12pt;"
            GridPane.setConstraints(updateChannelBox, 1, 0)
            tab1ContentGrid[1].children.add(updateChannelBox)

            updateChannelBox.items.addAll("Stable", "Dev")
            updateChannelBox.selectionModel.select(Configuration.updateChannel)
            updateChannelBox.valueProperty().addListener { _, _, _ -> Configuration.updateChannel = updateChannelBox.selectionModel.selectedIndex }

            val chkAppUpdate = Label()
            chkAppUpdate.text = LocalizationProvider.getString("chkAppUpdate")
            GridPane.setConstraints(chkAppUpdate, 0, 1)
            tab1ContentGrid[1].children.add(chkAppUpdate)

            val appbox = HBox()
            appbox.minHeight = 40.0
            appbox.spacing = 20.0
            appbox.alignment = Pos.CENTER_LEFT
            GridPane.setConstraints(appbox, 1, 1)
            tab1ContentGrid[1].children.add(appbox)

            val btn_chkAppUpdate = Button()
            btn_chkAppUpdate.text = LocalizationProvider.getString("chkUpdate")

            val updateBarApp = ProgressBar(0.0)
            updateBarApp.progress = ProgressIndicator.INDETERMINATE_PROGRESS

            val updStatusApp = Label()

            val btn_downloadAppUpdate = Button(LocalizationProvider.getString("download"))

            val btn_installAppUpdate = Button(LocalizationProvider.getString("install"))

            if (UpdateChecker.isUpdAvailable) {
                updStatusApp.text = LocalizationProvider.getString("update_available")
                appbox.children.addAll(updStatusApp, btn_downloadAppUpdate)
                updateChannelBox.isDisable = true
            } else {
                appbox.children.add(btn_chkAppUpdate)
            }

            btn_chkAppUpdate.setOnAction { _ ->

                updateChannelBox.isDisable = true
                appbox.children.remove(btn_chkAppUpdate)
                appbox.children.add(updateBarApp)

                Thread {

                    val updatestatus = UpdateChecker().checkProgramUpdate()

                    Platform.runLater {
                        appbox.children.remove(updateBarApp)
                        appbox.children.add(updStatusApp)
                        when (updatestatus) {
                            0 -> updStatusApp.text = LocalizationProvider.getString("no_update")
                            1 -> {
                                updStatusApp.text = LocalizationProvider.getString("update_available")
                                appbox.children.add(btn_downloadAppUpdate)
                                MainScene.mainScene.windowScene!!.win.windowStage.stage.title = AppContext.context.applicationName + " - " + LocalizationProvider.getString("update_available")
                            }
                            2 -> updStatusApp.text = LocalizationProvider.getString("no_connection")
                            3 -> updStatusApp.text = LocalizationProvider.getString("error")
                        }
                    }
                }.start()
            }

            btn_downloadAppUpdate.setOnAction { _ ->
                appbox.children.remove(btn_downloadAppUpdate)
                appbox.children.remove(updStatusApp)
                appbox.children.add(updateBarApp)

                updStatusApp.text = ""
                appbox.children.add(updStatusApp)

                val fileDownloader: FileDownloader
                try {
                    fileDownloader = FileDownloader(URI.create(Configuration.host + "files/apps/" + AppContext.context.contextIdentifier + "/download/" + updateChannel[Configuration.updateChannel] + File.separator).toURL(), UpdateChecker.filesToDownload)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                    return@setOnAction
                }

                Thread {
                    while (fileDownloader.status == FileDownloader.DOWNLOADING) {
                        val progress = fileDownloader.progress.toDouble()
                        Platform.runLater {
                            updateBarApp.progress = progress
                            updStatusApp.text = LocalizationProvider.getString("dlRemaining") + " " + fileDownloader.downloadList.size
                        }

                        try {
                            Thread.sleep(100)
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                            break
                        }

                    }

                    Platform.runLater {
                        appbox.children.remove(updateBarApp)
                        updStatusApp.text = LocalizationProvider.getString("dlComplete")
                        appbox.children.add(btn_installAppUpdate)
                    }
                }.start()
            }

            btn_installAppUpdate.setOnAction { _ ->
                val stage = MessageDialog.installUpdateDialog(500, 220)
                stage.showAndWait()
            }

            val debug = Label(LocalizationProvider.getString("logging"))
            debug.style = "-fx-font-size: 12pt;"
            GridPane.setConstraints(debug, 0, 0)
            tab1ContentGrid[2].children.add(debug)

            logCBox = CheckBox(LocalizationProvider.getString("enableLog"))
            logCBox!!.isSelected = Configuration.logState
            GridPane.setConstraints(logCBox, 0, 1)
            tab1ContentGrid[2].children.add(logCBox)
            for (i in 0 until 2) {
                val gridPane = GridPane()
                gridPane.padding = Insets(15.0, 5.0, 5.0, 15.0)
                gridPane.vgap = 5.0
                gridPane.hgap = 15.0
                tab2ContentGrid.add(gridPane)
            }
            GridPane.setConstraints(tab2ContentGrid[0], 0, 0)
            GridPane.setConstraints(tab2ContentGrid[1], 0, 1)

            val uiText = Label(LocalizationProvider.getString("uiOptions"))
            appLanguage.style = "-fx-font-size: 12pt;"
            GridPane.setConstraints(uiText, 0, 0)
            tab2ContentGrid[0].children.add(uiText)

            val themeText = Label("Theme")
            appLanguage.style = "-fx-font-size: 12pt;"
            GridPane.setConstraints(themeText, 0, 1)
            tab2ContentGrid[0].children.add(themeText)

            themeCB = ComboBox()
            themeCB!!.items.add("Dark")
            themeCB!!.items.add("Light")
            themeCB!!.selectionModel.select(Configuration.themeState)
            themeCB!!.minWidth = 115.0
            GridPane.setConstraints(themeCB, 1, 1)
            tab2ContentGrid[0].children.add(themeCB)

            sysBorderCBox = CheckBox(LocalizationProvider.getString("sysDecorationEnable"))
            sysBorderCBox!!.isSelected = Configuration.useSystemBorders
            GridPane.setConstraints(sysBorderCBox, 2, 1)
            GridPane.setMargin(sysBorderCBox, Insets(0.0, 0.0, 0.0, 35.0))
            tab2ContentGrid[0].children.add(sysBorderCBox)

            val trayIconCBox = CheckBox(LocalizationProvider.getString("showTrayIcon"))
            trayIconCBox.isSelected = Configuration.showTrayIcon
            GridPane.setConstraints(trayIconCBox, 2, 2)
            GridPane.setMargin(trayIconCBox, Insets(0.0, 0.0, 0.0, 35.0))
            tab2ContentGrid[0].children.add(trayIconCBox)

            val colorSchemeLabel = Label()
            colorSchemeLabel.text = LocalizationProvider.getString("colorScheme")
            colorSchemeLabel.style = "-fx-font-size: 12pt;"
            GridPane.setConstraints(colorSchemeLabel, 0, 2)
            tab2ContentGrid[0].children.add(colorSchemeLabel)

            colBox = ComboBox()
            colBox!!.minWidth = 115.0
            GridPane.setConstraints(colBox, 1, 2)
            tab2ContentGrid[0].children.add(colBox)

            val colorList = ArrayList<String>()
            colorList.add(LocalizationProvider.getString("black"))
            colorList.add(LocalizationProvider.getString("gray"))
            colorList.add(LocalizationProvider.getString("white"))
            colorList.add(LocalizationProvider.getString("red"))
            colorList.add(LocalizationProvider.getString("orange"))
            colorList.add(LocalizationProvider.getString("yellow"))
            colorList.add(LocalizationProvider.getString("blue"))
            colorList.add(LocalizationProvider.getString("magenta"))
            colorList.add(LocalizationProvider.getString("purple"))
            colorList.add(LocalizationProvider.getString("green"))

            colBox!!.items.addAll(colorList)
            colBox!!.selectionModel.select(Configuration.colorScheme)

            val animateText = Label(LocalizationProvider.getString("effects"))
            animateText.style = "-fx-font-size: 12pt;"
            GridPane.setConstraints(animateText, 0, 0)
            tab2ContentGrid[1].children.add(animateText)

            animateCBox = CheckBox(LocalizationProvider.getString("animationEnable"))
            animateCBox!!.isSelected = Configuration.animations
            GridPane.setConstraints(animateCBox, 0, 1)
            tab2ContentGrid[1].children.add(animateCBox)
            for (i in 0 until 1) {
                val gridPane = GridPane()
                gridPane.padding = Insets(15.0, 10.0, 10.0, 15.0)
                gridPane.vgap = 5.0
                tab3ContentGrid.add(gridPane)
            }
            GridPane.setConstraints(tab3ContentGrid[0], 0, 0)


            val advSet = Label(LocalizationProvider.getString("advanced_desc"))
            advSet.style = "-fx-font-size: 12pt;"

            val reset = Button(LocalizationProvider.getString("reset"))
            reset.style = "-fx-font-size: 12pt;"

            val advTopAnchor = AnchorPane()
            advTopAnchor.children.addAll(advSet, reset)
            AnchorPane.setTopAnchor(advSet, 5.0)
            AnchorPane.setRightAnchor(reset, 0.0)
            GridPane.setConstraints(advTopAnchor, 0, 0)
            tab3ContentGrid[0].children.add(advTopAnchor)

            otherTable = TableView()
            otherTable!!.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
            otherTable!!.isEditable = true
            GridPane.setConstraints(otherTable, 0, 1)
            GridPane.setHgrow(otherTable, Priority.ALWAYS)
            tab3ContentGrid[0].children.add(otherTable)

            listData = getListData()
//            listData!!.addListener({ c -> println(c + " changed") } as ListChangeListener<SettingsList>)

            val settingName = TableColumn<SettingsList, String>(LocalizationProvider.getString("name"))
            val setting = TableColumn<SettingsList, String>(LocalizationProvider.getString("value"))
            val desc = TableColumn<SettingsList, String>(LocalizationProvider.getString("setting_desc"))

            settingName.setCellValueFactory(PropertyValueFactory("settingName"))
            setting.setCellValueFactory(PropertyValueFactory("setting"))
            desc.setCellValueFactory(PropertyValueFactory("desc"))

            setting.setCellFactory(TextFieldTableCell.forTableColumn())
            setting.setOnEditCommit { event ->
                checkSetting(event)
                LogFileHandler.logger.log(Level.CONFIG, event.rowValue.getSettingName() + ": " + event.oldValue + "->" + event.newValue)
            }
            otherTable!!.columns.addAll(settingName, setting, desc)
            otherTable!!.setItems(listData)
            val saveHBox = HBox(10.0)
            saveHBox.alignment = Pos.CENTER
            controls!!.children.add(saveHBox)

            val status = Label()
            saveHBox.children.add(status)

            val saveCfg = Button(LocalizationProvider.getString("saveCfg"))
            saveHBox.children.add(saveCfg)

            val advCBox = CheckBox(LocalizationProvider.getString("advanced"))
            controls!!.children.add(advCBox)

            controls!!.padding = Insets(12.0, 12.0, 12.0, 12.0)
            AnchorPane.setBottomAnchor(saveHBox, 0.0)
            AnchorPane.setRightAnchor(saveHBox, 0.0)
            AnchorPane.setBottomAnchor(advCBox, 5.0)
            AnchorPane.setLeftAnchor(advCBox, 5.0)

            controls!!.style = "-fx-background-color: #1d1d1d;"
            for (aTab1ContentGrid in tab1ContentGrid) {
                tabVBox[0]!!.getChildren().add(aTab1ContentGrid)
            }
            for (aTab2ContentGrid in tab2ContentGrid) {
                tabVBox[1]!!.getChildren().add(aTab2ContentGrid)
            }
            for (aTab3ContentGrid in tab3ContentGrid) {
                tabVBox[2]!!.getChildren().add(aTab3ContentGrid)
            }
            cfgContent.center = tabPane
            cfgContent.bottom = controls

            saveCfg.setOnAction { _ ->
                var shouldRestart = false
                if (updateChannelBox.selectionModel.selectedIndex != Configuration.updateChannel) {
                    LogFileHandler.logger.config("change_update_channel: " + Configuration.updateChannel + " -> " + updateChannelBox.selectionModel.selectedIndex)
                    ThemeSelector.changeColorScheme(updateChannelBox.selectionModel.selectedIndex)
                    Configuration.updateChannel = updateChannelBox.selectionModel.selectedIndex
                }
                if (themeCB!!.selectionModel.selectedIndex != Configuration.themeState) {
                    LogFileHandler.logger.config("change_theme: " + Configuration.themeState + " -> " + themeCB!!.selectionModel.selectedIndex)
                    Configuration.themeState = themeCB!!.selectionModel.selectedIndex
                    ThemeSelector.onThemeChange()
                }
                if (colBox!!.selectionModel.selectedIndex != Configuration.colorScheme) {
                    LogFileHandler.logger.config("change_color_scheme: " + Configuration.colorScheme + " -> " + colBox!!.selectionModel.selectedIndex)
                    ThemeSelector.changeColorScheme(colBox!!.selectionModel.selectedIndex)
                    Configuration.colorScheme = colBox!!.selectionModel.selectedIndex
                }
                if (animateCBox!!.isSelected != Configuration.animations) {
                    LogFileHandler.logger.config("change_window_animation: " + Configuration.animations + " -> " + animateCBox!!.isSelected)
                    Configuration.animations = animateCBox!!.isSelected
                }
                if (sysBorderCBox!!.isSelected != Configuration.useSystemBorders) {
                    LogFileHandler.logger.config("change_window_decoration: " + Configuration.useSystemBorders + " -> " + sysBorderCBox!!.isSelected)
                    Configuration.useSystemBorders = sysBorderCBox!!.isSelected
                    shouldRestart = true
                }
                if (trayIconCBox.isSelected != Configuration.showTrayIcon) {
                    LogFileHandler.logger.config("change_systray: " + Configuration.showTrayIcon + " -> " + trayIconCBox.isSelected)
                    if (trayIconCBox.isSelected) {
                        ToolTrayIcon.icon.addAppToTray()
                    } else {
                        ToolTrayIcon.icon.removeTrayIcon()
                    }
                    Configuration.showTrayIcon = trayIconCBox.isSelected
                }
                if (logCBox!!.isSelected != Configuration.logState) {
                    LogFileHandler.logger.config("change_log_state: " + Configuration.logState + " -> " + logCBox!!.isSelected)
                    LogFileHandler.onLogStateChange()
                    Configuration.logState = logCBox!!.isSelected
                }
                if (langCB!!.selectionModel.selectedIndex != Configuration.language) {
                    LogFileHandler.logger.config("change_program_language: " + Configuration.language + " -> " + langCB!!.selectionModel.selectedIndex)
                    Configuration.language = langCB!!.selectionModel.selectedIndex
                    shouldRestart = true
                    setChange.isVisible = true
                }
                if (shouldRestart) {
                    MessageDialog.restartDialog("restartApp_desc", 535, 230)
                }
                try {
                    Configuration.config.save()
                } catch (e1: Exception) {
                    if (DebugHelper.DEBUG) {
                        e1.printStackTrace()
                    } else {
                        ExceptionHandler(e1)
                    }
                }

                AnimationHandler.statusFade(status, "success", LocalizationProvider.getString("cfgSaved"))
            }

            tabPane!!.selectionModel.selectedItemProperty().addListener { _, _, arg2 ->

                if (Configuration.animations) {
                    if (arg2 === tabs[0]) {
                        for (aTab1ContentGrid in tab1ContentGrid) {
                            AnimationHandler.translate(aTab1ContentGrid, 150, -100.0, 0.0)
                            val fade = AnimationHandler.fade(aTab1ContentGrid, 150, 0.1, 1.0)
                            fade.play()
                        }
                    }

                    if (arg2 === tabs[1]) {
                        for (aTab2ContentGrid in tab2ContentGrid) {
                            AnimationHandler.translate(aTab2ContentGrid, 150, -100.0, 0.0)
                            val fade = AnimationHandler.fade(aTab2ContentGrid, 150, 0.1, 1.0)
                            fade.play()
                        }
                    }

                    if (arg2 === tabs[2]) {
                        for (aTab3ContentGrid in tab3ContentGrid) {
                            AnimationHandler.translate(aTab3ContentGrid, 150, -100.0, 0.0)
                            val fade = AnimationHandler.fade(aTab3ContentGrid, 150, 0.1, 1.0)
                            fade.play()
                        }
                    }
                }
            }

            advCBox.setOnAction { _ ->
                if (advCBox.isSelected) {
                    tabPane!!.tabs.add(tabs[2])
                } else {
                    tabPane!!.tabs.removeAt(2)
                }
            }

            reset.setOnAction { _ ->
                if (MessageDialog.confirmDialog("reset_desc", 535, 230)) {
                    Configuration.config.setDefaults()
                }
            }

            return cfgContent
        }

    init {
        val about = settingsScene
        if (about == null) {
            settingsScene = this
            create()
        } else {
            if (about.settingsWindow!!.win.windowStage.stage.isShowing) {
                about.settingsWindow!!.win.windowStage.stage.requestFocus()
            } else {
                settingsScene = this
                create()
            }
        }
    }

    private fun create() {
        val cfgStage = Stage()
        cfgStage.title = LocalizationProvider.getString("settings")
        cfgStage.icons.add(ImageLoader.getAppIconSmall())
        cfgStage.isResizable = true
        cfgStage.minWidth = 640.0
        cfgStage.minHeight = 480.0
        cfgStage.width = 720.0
        cfgStage.height = 570.0

        val contentVbox = BorderPane()

        val cfgNode = cfgLayout

        // The UI (Client Area) to display
        contentVbox.center = cfgNode
        VBox.setVgrow(cfgNode, Priority.ALWAYS)

        // The Window as a Scene
        settingsWindow = WindowScene(WindowStage("settings", cfgStage), contentVbox, 1)

        cfgStage.scene = settingsWindow
        cfgStage.show()

        debugCfg()

        LogFileHandler.logger.info("open.settings")
    }

    private fun debugCfg() {
        if (DebugHelper.isDebugVersion) {
            controls!!.style = "-fx-background-color: #444444;"
            for (i in tabName.indices) {
                tabVBox[i]!!.style = "-fx-background-color: blue;"
            }

            //tab 1
            for (aTab1ContentGrid in tab1ContentGrid) {
                aTab1ContentGrid.style = "-fx-background-color: gray;"
                aTab1ContentGrid.isGridLinesVisible = true
            }

            //tab 2
            for (aTab2ContentGrid in tab2ContentGrid) {
                aTab2ContentGrid.style = "-fx-background-color: gray;"
                aTab2ContentGrid.isGridLinesVisible = true
            }

            //tab 3
            for (aTab3ContentGrid in tab3ContentGrid) {
                aTab3ContentGrid.style = "-fx-background-color: gray;"
                aTab3ContentGrid.isGridLinesVisible = true
            }

            //tab 4
            for (aTab4ContentGrid in tab3ContentGrid) {
                aTab4ContentGrid.style = "-fx-background-color: gray;"
                aTab4ContentGrid.isGridLinesVisible = true
            }
        } else {
            controls!!.style = "-fx-background-color: #1d1d1d;"
            for (i in tabName.indices) {
                tabVBox[i]!!.style = null
            }

            //tab 1
            for (aTab1ContentGrid in tab1ContentGrid) {
                aTab1ContentGrid.style = null
                aTab1ContentGrid.isGridLinesVisible = false
            }

            //tab 2
            for (aTab2ContentGrid in tab2ContentGrid) {
                aTab2ContentGrid.style = null
                aTab2ContentGrid.isGridLinesVisible = false
            }

            //tab 3
            for (aTab3ContentGrid in tab3ContentGrid) {
                aTab3ContentGrid.style = null
                aTab3ContentGrid.isGridLinesVisible = false
            }
        }
    }

    /**
     * SettingsList Data Collection
     * @return All settings in an observable list
     */
    private fun getListData(): ObservableList<SettingsList> {
        return FXCollections.observableArrayList(
                SettingsList("language", Configuration.language.toString(), "set program language"),
                SettingsList("theme", Configuration.themeState.toString(), "set program theme"),
                SettingsList("logging", Configuration.logState.toString(), "set program logging"),
                SettingsList("decorationColor", ThemeSelector.getColorHexString(Configuration.decorationColor!!), "set window decoration color"),
                SettingsList("shadowColorFocused", ThemeSelector.getColorHexString(Configuration.shadowColorFocused!!), "set shadow color when window focused"),
                SettingsList("shadowColorNotFocused", ThemeSelector.getColorHexString(Configuration.shadowColorNotFocused!!), "set shadow color when window not focused"),
                SettingsList("tabPosition", Configuration.tabPosition!!.name, "set tab position in Settings window")
        )
    }

    /**
     * Checks which setting has changed and applies the new setting respectively.
     * @param event the CellEditEvent of the changed setting
     */
    private fun checkSetting(event: TableColumn.CellEditEvent<SettingsList, String>) {
        when (event.rowValue.getSettingName()) {
            "language" -> try {
                Configuration.language = Integer.parseInt(event.newValue)
                updateGui(Configuration::class.java.getDeclaredField("language"))
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NumberFormatException) {
                event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                event.tableView.refresh()
            }

            "theme" -> try {
                Configuration.themeState = Integer.parseInt(event.newValue)
                updateGui(Configuration::class.java.getDeclaredField("themeState"))
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NumberFormatException) {
                event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                event.tableView.refresh()
            }

            "logging" -> try {
                Configuration.logState = java.lang.Boolean.parseBoolean(event.newValue)
                updateGui(Configuration::class.java.getDeclaredField("logState"))
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NumberFormatException) {
                event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                event.tableView.refresh()
            }

            "decorationColor" -> if (!Configuration.useSystemBorders) {
                try {
                    ThemeSelector.changeDecorationColor(event.newValue)
                } catch (e1: IllegalArgumentException) {
                    MessageDialog(0, LocalizationProvider.getString("colorInvalid_errorDesc"), 400, 230)
                    event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                    event.tableView.refresh()
                }

            } else {
                event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                event.tableView.refresh()
            }
            "shadowColorFocused" -> if (!Configuration.useSystemBorders) {
                try {
                    ThemeSelector.changeWindowShadowColor(true, event.newValue)
                } catch (e1: IllegalArgumentException) {
                    MessageDialog(0, LocalizationProvider.getString("colorInvalid_errorDesc"), 400, 230)
                    event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                    event.tableView.refresh()
                }

            } else {
                event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                event.tableView.refresh()
            }
            "shadowColorNotFocused" -> if (!Configuration.useSystemBorders) {
                try {
                    ThemeSelector.changeWindowShadowColor(false, event.newValue)
                } catch (e1: IllegalArgumentException) {
                    MessageDialog(0, LocalizationProvider.getString("colorInvalid_errorDesc"), 400, 230)
                    event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                    event.tableView.refresh()
                }

            } else {
                event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                event.tableView.refresh()
            }
            "tabPosition" -> try {
                Configuration.tabPosition = Side.valueOf(event.newValue)
                tabPane!!.side = Configuration.tabPosition
            } catch (e1: IllegalArgumentException) {
                MessageDialog(0, LocalizationProvider.getString("tabPosition_errorDesc"), 400, 230)
                event.tableView.items[event.tablePosition.row].setSetting(event.oldValue)
                event.tableView.refresh()
            }

        }
    }

    @Throws(IllegalAccessException::class)
    private fun updateGui(f: Field) {
        when (f.name) {
            "language" -> if (!MessageDialog.restartDialog("restartApp_desc", 535, 230)) {
                Configuration.config.save()
            }
            "themeState" -> {
                ThemeSelector.onThemeChange()
                themeCB!!.selectionModel.select(Configuration.themeState)
            }
            "logState" -> {
                LogFileHandler.onLogStateChange()
                logCBox!!.isSelected = Configuration.logState
            }
            "colorScheme" -> {
                colBox!!.selectionModel.select(Configuration.colorScheme)
                ThemeSelector.changeColorScheme(Configuration.colorScheme)
            }
            "animations" -> animateCBox!!.isSelected = Configuration.animations
            "useSystemBorders" -> if (!MessageDialog.restartDialog("restartApp_desc", 535, 230)) {
                sysBorderCBox!!.isSelected = Configuration.useSystemBorders
                Configuration.config.save()
            }
            "decorationColor" -> {
                for (aListData3 in listData!!) {
                    if (aListData3.getSettingName() == f.name) {
                        aListData3.setSetting(ThemeSelector.getColorHexString(f.get(LoadSave::class.java) as Color))
                        break
                    }
                }
                ThemeSelector.changeDecorationColor(Configuration.decorationColor.toString())
            }
            "shadowColorFocused" -> {
                for (aListData2 in listData!!) {
                    if (aListData2.getSettingName() == f.name) {
                        aListData2.setSetting(ThemeSelector.getColorHexString(f.get(LoadSave::class.java) as Color))
                        break
                    }
                }
                ThemeSelector.changeWindowShadowColor(true, Configuration.shadowColorFocused.toString())
            }
            "shadowColorNotFocused" -> {
                for (aListData1 in listData!!) {
                    if (aListData1.getSettingName() == f.name) {
                        aListData1.setSetting(ThemeSelector.getColorHexString(f.get(LoadSave::class.java) as Color))
                        break
                    }
                }
                ThemeSelector.changeWindowShadowColor(false, Configuration.shadowColorNotFocused.toString())
            }
            "tabPosition" -> {
                for (aListData in listData!!) {
                    if (aListData.getSettingName() == f.name) {
                        aListData.setSetting(f.get(LoadSave::class.java).toString())
                        break
                    }
                }
                tabPane!!.side = Configuration.tabPosition
            }
            else -> println("nothing changed")
        }
    }

    fun updateGUI(objectlist: List<Field>) {
        for (f in objectlist) {
            try {
                updateGui(f)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

        }
        otherTable!!.refresh()
    }

    companion object {

        var settingsScene: SettingsScene? = null
    }
}