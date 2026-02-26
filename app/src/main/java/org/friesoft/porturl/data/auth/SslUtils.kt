package org.friesoft.porturl.data.auth

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object SslUtils {
    
    @android.annotation.SuppressLint("DiscouragedApi")
    fun applySelfSignedTrust(context: Context, builder: OkHttpClient.Builder) {
        val resId = context.resources.getIdentifier("local_ca", "raw", context.packageName)
        if (resId == 0) {
            Log.d("SslUtils", "Resource R.raw.local_ca not found, skipping self-signed trust configuration.")
            return
        }

        try {
            val cf = CertificateFactory.getInstance("X.509")
            val caInput: InputStream = context.resources.openRawResource(resId)
            val ca: X509Certificate = caInput.use {
                cf.generateCertificate(it) as X509Certificate
            }

            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(null, null)
                setCertificateEntry("ca", ca)
            }

            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
                init(keyStore)
            }

            val trustManagers = tmf.trustManagers
            val x509TrustManager = trustManagers.first { it is X509TrustManager } as X509TrustManager

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, trustManagers, null)
            }

            builder.sslSocketFactory(sslContext.socketFactory, x509TrustManager)
            builder.hostnameVerifier(HostnameVerifier { hostname, _ ->
                hostname == "localhost" || hostname == "10.0.2.2" || hostname == "127.0.0.1"
            })
            
            Log.d("SslUtils", "Successfully applied self-signed trust for localhost")
        } catch (e: Exception) {
            Log.e("SslUtils", "Failed to apply self-signed trust", e)
        }
    }
}
