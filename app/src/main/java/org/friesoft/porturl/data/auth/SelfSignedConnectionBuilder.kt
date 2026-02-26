package org.friesoft.porturl.data.auth

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import net.openid.appauth.connectivity.ConnectionBuilder
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.HostnameVerifier

/**
 * A connection builder that explicitly trusts our local self-signed Root CA.
 * This bypasses the system's Network Security Configuration and manually 
 * initializes an SSLContext with the provided Root CA.
 */
@SuppressLint("DiscouragedApi")
class SelfSignedConnectionBuilder(context: Context) : ConnectionBuilder {
    private var sslContext: SSLContext? = null
    private var hostnameVerifier: HostnameVerifier? = null

    init {
        Log.d("SelfSignedConnBuilder", "Initializing robust SSLContext")
        val resId = context.resources.getIdentifier("local_ca", "raw", context.packageName)
        if (resId != 0) {
            try {
                val cf = CertificateFactory.getInstance("X.509")
                val caInput: InputStream = context.resources.openRawResource(resId)
                val ca: X509Certificate = caInput.use {
                    cf.generateCertificate(it) as X509Certificate
                }
                Log.d("SelfSignedConnBuilder", "Loaded CA Subject: ${ca.subjectDN}")

                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                    load(null, null)
                    setCertificateEntry("ca", ca)
                }

                val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
                val tmf = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
                    init(keyStore)
                }

                sslContext = SSLContext.getInstance("TLS").apply {
                    init(null, tmf.trustManagers, null)
                }
                hostnameVerifier = HostnameVerifier { hostname, _ ->
                    hostname == "localhost" || hostname == "10.0.2.2" || hostname == "127.0.0.1"
                }
                Log.d("SelfSignedConnBuilder", "SSLContext successfully initialized with custom TrustManager")
            } catch (e: Exception) {
                Log.e("SelfSignedConnBuilder", "Failed to initialize SSLContext", e)
            }
        } else {
            Log.d("SelfSignedConnBuilder", "Resource R.raw.local_ca not found, using default SSL settings")
        }
    }

    override fun openConnection(uri: Uri): HttpURLConnection {
        Log.d("SelfSignedConnBuilder", "Opening connection to: $uri")
        val url = URL(uri.toString())
        val connection = url.openConnection() as HttpURLConnection
        
        connection.connectTimeout = TimeUnit.SECONDS.toMillis(15).toInt()
        connection.readTimeout = TimeUnit.SECONDS.toMillis(10).toInt()
        connection.instanceFollowRedirects = false

        if (connection is HttpsURLConnection && sslContext != null) {
            val host = uri.host
            if (host == "localhost" || host == "10.0.2.2" || host == "127.0.0.1") {
                connection.sslSocketFactory = sslContext!!.socketFactory
                connection.hostnameVerifier = hostnameVerifier!!
                Log.d("SelfSignedConnBuilder", "Applied manual SSLSocketFactory and HostnameVerifier for localhost")
            } else {
                Log.d("SelfSignedConnBuilder", "Using default SSL settings for host: $host")
            }
        }

        return connection
    }
}
