package be.bruyere.romain.geometryviewer

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


private const val PREFERRED_WIDTH = 600
private const val PREFERRED_HEIGHT = 600

private const val UNIQUE_SIZE_KEY = "#be.bruyere.romain.geometryviewer.ViewerDialog"

class GeometryViewerDialog(wkt: String) : DialogWrapper(true), Disposable {
    private val jbCefBrowser: JBCefBrowser?

    init {
        if (JBCefApp.isSupported()) {
            jbCefBrowser = JBCefBrowser()
            val requestHandler = CefPartialLocalRequestHandler("http", "localhost")
            addResource(requestHandler, "/leaflet.css", "text/css")
            addResource(requestHandler, "/images/layers.png", "image/png")
            addResource(requestHandler, "/images/layers-2x.png", "image/png")
            addResource(requestHandler, "/images/marker-icon.png", "image/png")
            addResource(requestHandler, "/images/marker-icon-2x.png", "image/png")
            addResource(requestHandler, "/images/marker-shadow.png", "image/png")
            jbCefBrowser.loadHTML(buildHtml(wkt))
            jbCefBrowser.jbCefClient.addRequestHandler(requestHandler, jbCefBrowser.cefBrowser)
        } else {
            jbCefBrowser = null
        }

        init()
        isModal = false
        title = "View on a map"
    }

    private fun addResource(requestHandler: CefPartialLocalRequestHandler, resourcePath: String, mimeType: String) {
        requestHandler.addResource(resourcePath) {
            CefStreamResourceHandler(
                javaClass.getResourceAsStream(resourcePath)!!,
                mimeType,
                this
            )
        }
    }

    /**
     * Fill viewer.html with the content of scripts and the WKT geometry.
     */
    fun buildHtml(wkt: String): String {
        val leafletJS: String =
            javaClass.getResource("/leaflet.js")?.readText()
                ?: throw GeometryViewerException("The file leaflet.js is not found")
        val wellknownJS: String =
            javaClass.getResource("/wellknown.js")?.readText()
                ?: throw GeometryViewerException("The file wellknown.js is not found")
        val viewerHtml: String =
            javaClass.getResource("/viewer.html")?.readText()
                ?: throw GeometryViewerException("The file viewer.html is not found")

        return viewerHtml
            .replace("<!-- LEAFLET_JS -->", leafletJS)
            .replace("<!-- WELLKNOWN -->", wellknownJS)
            .replace("<!-- WKT -->", wkt)
    }

    /**
     * Creates the center panel of the dialog, with the browser engine inside.
     */
    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        if (jbCefBrowser != null) {
            panel.add(jbCefBrowser.component, BorderLayout.CENTER)
        } else {
            val errorLabel = JLabel("JBCefBrowser is not supported on this platform.")
            panel.add(errorLabel, BorderLayout.CENTER)
        }
        return panel
    }

    /**
     * Adds a close button to the dialog.
     */
    override fun createActions(): Array<out Action> {
        val closeAction: Action = object : DialogWrapperAction("Close") {
            override fun doAction(e: ActionEvent) {
                close(OK_EXIT_CODE)
            }
        }

        return arrayOf(closeAction)
    }

    override fun dispose() {
        jbCefBrowser?.dispose()
        super.dispose()
    }

    public override fun getDimensionServiceKey(): String {
        return UNIQUE_SIZE_KEY
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT)
    }

    override fun getInitialSize(): Dimension {
        return preferredSize
    }
}