package be.bruyere.romain.geometryviewer

import com.intellij.openapi.ui.Messages
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.KeyStroke

private const val PREFERRED_WIDTH = 600
private const val PREFERRED_HEIGHT = 600
private const val TITLE = "Geometry Viewer"

object GeometryViewerFrame {

    private var jFrame: JFrame? = null;
    private var jbCefBrowser: JBCefBrowser? = null;

    /**
     * Shows the Geometry Viewer frame with the given parameters.
     * @param name The name of the geometry.
     * @param wkt The Well-Known Text (WKT) representation of the geometry.
     * @param srid The Spatial Reference System Identifier (SRID) of the geometry.
     */
    fun show(name: String, wkt: String, srid: Int? = null) {
        if (JBCefApp.isSupported()) {
            val js = createJS(name, wkt, srid);

            if (jFrame == null) {
                createBrowserAndExecuteJavascript(js)
            } else {
                val browser = jbCefBrowser?.cefBrowser
                executeJavascript(browser, js)
                jFrame?.toFront()
            }
        } else {
            Messages.showWarningDialog(
                "JCEF is not supported in your environment.",
                "Warning"
            )
        }
    }

    /**
     * Creates the JavaScript script to add the geometry to the viewer.
     * @param name The name of the geometry.
     * @param wkt The Well-Known Text (WKT) representation of the geometry.
     * @param srid The Spatial Reference System Identifier (SRID) of the geometry.
     * @return The JavaScript string to add the geometry.
     */
    private fun createJS(name: String, wkt: String, srid: Int?): String {
        return if(srid == null || srid == 0) {
            "window.addGeometry('$name','$wkt');"
        } else {
            "window.addGeometry('$name','$wkt',$srid);"
        }
    }

    /**
     * Executes the given JavaScript script in the specified browser.
     * @param browser The browser in which to execute the JavaScript.
     * @param js The JavaScript string to execute.
     */
    private fun executeJavascript(browser: CefBrowser?, js: String) {
        browser?.executeJavaScript(js, browser.url, 0)
    }

    /**
     * Creates the browser and executes the given JavaScript.
     * @param js The JavaScript string to execute.
     */
    private fun createBrowserAndExecuteJavascript(js: String) {
        val frame = JFrame(TITLE)
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)

        val browser = JBCefBrowser.createBuilder()
            .setEnableOpenDevToolsMenuItem(true)
            .setOffScreenRendering(false)
            .build()
        val requestHandler = CefPartialLocalRequestHandler(UrlUtils.DEFAULT_PROTOCOL, UrlUtils.DEFAULT_AUTHORITY, browser)
        browser.jbCefClient.addRequestHandler(requestHandler, browser.cefBrowser)
        browser.loadURL(UrlUtils.getHomeUrl())

        frame.contentPane.add(browser.component, BorderLayout.CENTER)
        frame.setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true

        addDisplayHandlerForCursor(browser)
        addLoadHandlerForJs(browser, js)
        addOpenDevToolsListener(frame, browser)
        frame.addWindowListener(windowClosingListener())

        jFrame = frame
        jbCefBrowser = browser
    }

    /**
     * Adds a load handler to differ the JavaScript execution when the page load ends.
     * @param browser The browser to which the load handler is added.
     * @param js The JavaScript string to execute.
     */
    private fun addLoadHandlerForJs(browser: JBCefBrowser, js: String) {
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (frame?.isMain == true) {
                    executeJavascript(browser, js)
                }
            }
        }, browser.cefBrowser)
    }

    /**
     * Adds a display handler to change the cursor based on the browser's cursor type.
     * @param browser The browser to which the display handler is added.
     */
    private fun addDisplayHandlerForCursor(browser: JBCefBrowser) {
        browser.jbCefClient.addDisplayHandler(object: CefDisplayHandlerAdapter() {
            override fun onCursorChange(browser: CefBrowser?, cursorType: Int): Boolean {
                browser?.uiComponent?.cursor = Cursor.getPredefinedCursor(cursorType)
                return false
            }
        }, browser.cefBrowser)
    }

    /**
     * Adds a listener to open the developer tools when F12 is pressed.
     * @param frame The frame to which the listener is added.
     * @param browser The browser for which the developer tools are opened.
     */
    private fun addOpenDevToolsListener(frame: JFrame, browser: JBCefBrowser) {
        val actionKey = "openDevtools";
        val inputMap = frame.rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        val actionMap = frame.rootPane.actionMap
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), actionKey)
        actionMap.put(actionKey, object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) = browser.openDevtools()
        })
    }

    private fun windowClosingListener(): WindowAdapter = object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            closeBrowser()
        }
    }

    fun closeBrowser() {
        jbCefBrowser?.dispose()
        jbCefBrowser = null
        jFrame = null
    }


}