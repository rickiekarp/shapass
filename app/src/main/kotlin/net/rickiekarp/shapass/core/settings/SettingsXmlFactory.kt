package net.rickiekarp.shapass.core.settings

import javafx.scene.paint.Color
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.debug.ExceptionHandler
import net.rickiekarp.shapass.core.ui.windowmanager.ThemeSelector
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * This class handles reading and writing of the config.xml file.
 */
class SettingsXmlFactory {

    /**
     * Saves the current state of the program to the config.xml file.
     */
    fun createConfigXML() {
        try {
            val docFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = docFactory.newDocumentBuilder()
            val doc = dBuilder.newDocument()
            doc.xmlVersion = "1.1"

            // root element
            val rootElement = doc.createElement("settings")
            doc.appendChild(rootElement)

            // create all elements
            for (f in LoadSave::class.java.declaredFields) {
                createElement(doc, f.name, getFieldValueString(f))
            }
        } catch (e: Exception) {
            if (DebugHelper.DEBUG) {
                e.printStackTrace()
            } else {
                ExceptionHandler(e)
            }
        }

    }

    /**
     * Creates an xml element.
     * @param doc  the config.xml Document
     * @param name the value of the element
     */
    private fun createElement(doc: Document, name: String, value: Any?) {
        val root = doc.documentElement
        val element = doc.createElement("entry")
        element.setAttribute("key", name)
        if (value != null) {
            element.appendChild(doc.createTextNode(value.toString()))
        }

        root.appendChild(element)

        // write content into xml file
        try {
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            val source = DOMSource(doc)
            val result = StreamResult(Configuration.config.configDirFile.toString() + File.separator + Configuration.config.configFileName)
            transformer.transform(source, result)
        } catch (e: TransformerException) {
            if (DebugHelper.DEBUG) {
                e.printStackTrace()
            } else {
                ExceptionHandler(e)
            }
        }

    }

    private fun getFieldValueString(f: Field): Any? {
        return try {
            if (f.type == Color::class) {
                ThemeSelector.getColorHexString(Color.valueOf(f.get(LoadSave::class).toString()))
            } else {
                f.isAccessible = true //work around issue where fields are private (AppConfiguration)
                f.get(LoadSave::class)
            }
        } catch (e: IllegalAccessException) {
            if (DebugHelper.DEBUG) {
                e.printStackTrace()
            } else {
                ExceptionHandler(e)
            }
            null
        }

    }

    fun getElementValue(key: String, clazz: Class<*>): String? {
        val configFile = File(Configuration.config.configDirFile.toString() + File.separator + Configuration.config.configFileName)
        try {
            val docFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = docFactory.newDocumentBuilder()
            val doc = dBuilder.parse(configFile)
            doc.documentElement.normalize()

            try {
                checkXmlNode(doc, clazz.getDeclaredField(key))
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }

            val nList = doc.getElementsByTagName("entry")

            for (i in 0 until nList.length) {
                if (nList.item(i).attributes.getNamedItem("key").nodeValue == key && !nList.item(i).textContent.isEmpty()) {
                    return nList.item(i).textContent
                }
            }
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun setElementValue(key: String, value: String) {
        try {
            val docFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = docFactory.newDocumentBuilder()
            val doc = dBuilder.parse(File(Configuration.config.configDirFile.toString() + File.separator + Configuration.config.configFileName))

            doc.documentElement.normalize()

            val nList = doc.getElementsByTagName("entry")

            for (i in 0 until nList.length) {
                if (nList.item(i).attributes.getNamedItem("key").nodeValue == key) {
                    nList.item(i).textContent = value
                    break
                }
            }

            // write content into xml file
            try {
                val transformerFactory = TransformerFactory.newInstance()
                val transformer = transformerFactory.newTransformer()
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                val source = DOMSource(doc)
                val result = StreamResult(Configuration.config.configDirFile.toString() + File.separator + Configuration.config.configFileName)
                transformer.transform(source, result)
            } catch (e1: TransformerException) {
                if (DebugHelper.DEBUG) {
                    e1.printStackTrace()
                } else {
                    ExceptionHandler(e1)
                }
            }

        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * Checks if all xml nodes exist. If a node does not exist it is created.
     * @param doc The xml Document
     */
    private fun checkXmlNode(doc: Document, f: Field) {
        val nList = doc.getElementsByTagName("entry")
        var insertNewKey = true
        for (i in 0 until nList.length) {
            if (nList.item(i).attributes.getNamedItem("key").nodeValue == f.name) {
                insertNewKey = false
                break
            }
        }
        if (insertNewKey) {
            try {
                createElement(doc, f.name, getFieldValueString(LoadSave::class.java.getDeclaredField(f.name)))
            } catch (e: NoSuchFieldException) {
                createElement(doc, f.name, getFieldValueString(f))
            }
        }
    }

    /**
     * Checks if all xml nodes exist. If a node does not exist it is created.
     * @param doc The xml Document
     */
    @Deprecated("")
    private fun checkXmlNodes(doc: Document) {
        val fields = LoadSave::class.java.declaredFields
        for (i in fields.indices) {
            if (doc.getElementsByTagName("entry").item(i) == null) {
                createElement(doc, fields[i].name, getFieldValueString(fields[i]))
            }
        }
    }

    /**
     * Returns a NodeList of a given node name
     * @param nodeName Node name
     * @return Node List of the given node name
     */
    fun getNodeList(nodeName: String): NodeList? {
        val configFile = File(Configuration.config.configDirFile.toString() + File.separator + Configuration.config.configFileName)
        val docFactory = DocumentBuilderFactory.newInstance()
        try {
            val dBuilder = docFactory.newDocumentBuilder()
            val doc = dBuilder.parse(configFile)
            doc.documentElement.normalize()
            return doc.getElementsByTagName(nodeName)
        } catch (e: SAXException) {
            if (DebugHelper.DEBUG) {
                e.printStackTrace()
            } else {
                ExceptionHandler(e)
            }
            return null
        } catch (e: ParserConfigurationException) {
            if (DebugHelper.DEBUG) {
                e.printStackTrace()
            } else {
                ExceptionHandler(e)
            }
            return null
        } catch (e: IOException) {
            if (DebugHelper.DEBUG) {
                e.printStackTrace()
            } else {
                ExceptionHandler(e)
            }
            return null
        }

    }
}