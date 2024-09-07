package be.bruyere.romain.geometryviewer

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import java.net.URL

private typealias CefResourceProvider = () -> CefResourceHandler?

class CefPartialLocalRequestHandler(
    private val myProtocol: String,
    private val myAuthority: String
) : CefRequestHandlerAdapter() {
    private val myResources: MutableMap<String, CefResourceProvider> = HashMap()

    private val handler = object : CefResourceRequestHandlerAdapter() {
        override fun getResourceHandler(
            browser: CefBrowser?,
            frame: CefFrame?,
            request: CefRequest
        ): CefResourceHandler? {
            val url = URL(request.url)
            if (!url.protocol.equals(myProtocol) || !url.authority.equals(myAuthority)) {
                return null
            }
            return myResources[url.path]?.let { it() }
        }
    }

    fun addResource(resourcePath: String, resourceProvider: CefResourceProvider) {
        myResources[resourcePath] = resourceProvider
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
