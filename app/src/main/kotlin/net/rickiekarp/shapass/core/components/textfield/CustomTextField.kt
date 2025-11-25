package net.rickiekarp.shapass.core.components.textfield

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.TextField

/**
 * This class creates a basic TextField that can be configured in the following ways:
 * Restrict max text lenght, Restrict user input to certain characters e.g. [0-9]
 */
class CustomTextField : TextField() {

    private val maxLength = SimpleIntegerProperty(this, "maxLength", -1)
    private val restrict = SimpleStringProperty(this, "restrict")

    init {

        textProperty().addListener(object : ChangeListener<String> {

            private var ignore: Boolean = false

            override fun changed(observableValue: ObservableValue<out String>, s: String, s1: String?) {
                if (ignore || s1 == null)
                    return

                if (maxLength.get() > -1 && s1.length > maxLength.get()) {
                    ignore = true
                    text = s1.substring(0, maxLength.get())
                    ignore = false
                }

                if (restrict.get() != null && restrict.get() != "" && !s1.matches((restrict.get() + "*").toRegex())) {
                    ignore = true
                    text = s
                    ignore = false
                }
            }
        })
    }

    /**
     * Max TextField length property
     */
    fun maxLengthProperty(): IntegerProperty {
        return maxLength
    }

    /**
     * Gets Max TextField length
     */
    fun getMaxLength(): Int {
        return maxLength.get()
    }

    /**
     * Sets Max TextField length
     */
    fun setMaxLength(maxLength: Int) {
        this.maxLength.set(maxLength)
    }

    /**
     * Restrict property
     */
    fun restrictProperty(): StringProperty {
        return restrict
    }

    /**
     * Gets the expression character class that restricts user input
     */
    fun getRestrict(): String {
        return restrict.get()
    }

    /**
     * Sets the expression character class that restricts user input
     * Example: [0-9] only allows numeric values
     */
    fun setRestrict(restrict: String) {
        this.restrict.set(restrict)
    }

    fun getDoubleValue() : Double {
        return text.replace("−", "-").toDoubleOrNull() ?: 0.0
    }
}