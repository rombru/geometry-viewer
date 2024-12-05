package be.bruyere.romain.geometryviewer

import com.intellij.openapi.Disposable
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import java.net.URI
import java.net.URLConnection

private const val INDEX_HTML = "/index.html"
private const val INDEX_HTML_MIME_TYPE = "text/html"

class CefPartialLocalRequestHandler(
    private val myProtocol: String,
    private val myAuthority: String,
    private val disposable: Disposable
) : CefRequestHandlerAdapter() {
    private val fileNameMap = URLConnection.getFileNameMap();

    private val handler = object : CefResourceRequestHandlerAdapter() {
        override fun getResourceHandler(
            browser: CefBrowser?,
            frame: CefFrame?,
            request: CefRequest
        ): CefResourceHandler? {
            val url = URI.create(request.url).toURL()
            if (url.protocol != myProtocol || url.authority != myAuthority) {
                return null
            }
            var mimeType = fileNameMap.getContentTypeFor(request.url)
            var path = request.url.replace("http://localhost","")
            if (path == "/") {
                path = INDEX_HTML
                mimeType = INDEX_HTML_MIME_TYPE
            }
            return CustomCefStreamResourceHandler(
                javaClass.getResourceAsStream(path)!!,
                mimeType,
                disposable
            )
        }
    }

    override fun getResourceRequestHandler(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        isNavigation: Boolean,
        isDownload: Boolean,
        requestInitiator: String?,
        disableDefaultHandling: BoolRef?
    ): CefResourceRequestHandler {
        return handler
    }
}
