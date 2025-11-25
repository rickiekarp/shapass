package net.rickiekarp.shapass.core.ui.windowmanager

import net.rickiekarp.shapass.core.debug.LogFileHandler
import java.util.*

class WindowStageStack : Stack<WindowStage>() {
    val sceneViewStack: WindowStack = WindowStack()

    override fun push(item: WindowStage): WindowStage {
        addElement(item)
        LogFileHandler.logger.info("Push $item - new size: $size")
        return item
    }

    override fun pop(): WindowStage {
        LogFileHandler.logger.info("pop -> " + this.peek())
        return super.pop()
    }

    fun pop(stageIdentifier: String): WindowStage? {
        for (i in 0 until size) {
            if (get(i).identifier == stageIdentifier) {
                return pop()
            }
        }
        LogFileHandler.logger.info("Element with identifier $stageIdentifier could not be found!")
        return null
    }

    fun getStageByIdentifier(stageIdentifier: String): WindowStage? {
        for (windowStage in this) {
            if (windowStage.identifier == stageIdentifier) {
                return windowStage
            }
        }
        return null
    }
}
