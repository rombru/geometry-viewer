package be.bruyere.romain.geometryviewer

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import org.cef.callback.CefCallback
import org.cef.handler.CefResourceHandler
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.io.IOException
import java.io.InputStream

class CustomCefStreamResourceHandler(private val myStream: InputStream, private val myMimeType: String, parent: Disposable): CefResourceHandler, Disposable {
    init {
        Disposer.register(parent, this)
    }

    override fun processRequest(request: CefRequest, callback: CefCallback): Boolean {
        callback.Continue()
        return true
    }

    override fun getResponseHeaders(response: CefResponse, responseLength: IntRef, redirectUrl: StringRef) {
        response.mimeType = myMimeType
        response.status = 200
        response.setHeaderByName("Access-Control-Allow-Origin", "*", true)
    }

    override fun readResponse(dataOut: ByteArray, bytesToRead: Int, bytesRead: IntRef, callback: CefCallback): Boolean {
        try {
            bytesRead.set(myStream.read(dataOut, 0, bytesToRead))
            if (bytesRead.get() != -1) {
                return true
            }
        }
        catch (_: IOException) {
            callback.cancel()
        }
        bytesRead.set(0)
        Disposer.dispose(this)
        return false
    }

    override fun cancel() {
        Disposer.dispose(this)
    }

    override fun dispose() {
        try {
            myStream.close()
        }
        catch (e: IOException) {
            Logger.getInstance(CustomCefStreamResourceHandler::class.java).warn("Failed to close the stream", e)
        }
    }
}