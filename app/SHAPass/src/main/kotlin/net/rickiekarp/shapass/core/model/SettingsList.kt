package net.rickiekarp.core.model

import javafx.beans.property.SimpleStringProperty

class SettingsList(settingName: String, setting: String, desc: String) {
    private val settingName: SimpleStringProperty = SimpleStringProperty(settingName)
    private val setting: SimpleStringProperty = SimpleStringProperty(setting)
    private val desc: SimpleStringProperty = SimpleStringProperty(desc)

    fun getSettingName(): String {
        return settingName.get()
    }

    fun setSettingName(fName: String) {
        settingName.set(fName)
    }

    fun getSetting(): String {
        return setting.get()
    }

    fun setSetting(fName: String) {
        setting.set(fName)
    }

    fun getDesc(): String {
        return desc.get()
    }

    fun setDesc(fName: String) {
        desc.set(fName)
    }
}
