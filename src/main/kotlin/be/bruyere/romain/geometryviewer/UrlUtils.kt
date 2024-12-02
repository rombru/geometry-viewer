package be.bruyere.romain.geometryviewer

object UrlUtils {
    const val DEFAULT_PROTOCOL = "http"
    const val DEFAULT_AUTHORITY = "localhost"

    /**
     * Returns the default protocol and authority as a string.
     * Example: "http://localhost"
     */
    fun getDefaultBaseUrl(): String {
        return "$DEFAULT_PROTOCOL://$DEFAULT_AUTHORITY"
    }

    /**
     * Returns the URL of the home page.
     * Example: "http://localhost/"
     */
    fun getHomeUrl(): String {
        return "${getDefaultBaseUrl()}/"
    }
}