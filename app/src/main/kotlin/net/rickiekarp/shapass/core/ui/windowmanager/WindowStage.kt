package net.rickiekarp.core.ui.windowmanager

import javafx.stage.Stage

class WindowStage {
    var stage: Stage
        private set
    var identifier: String? = null
        private set

    constructor(identifier: String) {
        this.stage = Stage()
        this.identifier = identifier
    }

    constructor(identifier: String, stage: Stage) {
        this.stage = stage
        this.identifier = identifier
    }

    override fun toString(): String {
        return "$identifier - $stage"
    }
}
