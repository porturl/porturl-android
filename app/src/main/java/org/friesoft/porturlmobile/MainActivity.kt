package org.friesoft.porturlmobile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import net.openid.appauth.TokenResponse
import org.friesoft.porturlmobile.model.Application
import org.friesoft.porturlmobile.network.ApplicationApi
import org.friesoft.porturlmobile.network.ServiceBuilder
import org.friesoft.porturlmobile.ui.SettingsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val AUTH_REQUEST_CODE = 1001
    private lateinit var adapter: ApplicationAdapter
    private var api: ApplicationApi? = null

    private lateinit var backendUrl: String
    private lateinit var oidcUrl: String
    private lateinit var oidcClientId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load settings securely
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            "settings_prefs", masterKey, this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        backendUrl = prefs.getString("backend_url", "") ?: ""
        oidcUrl = prefs.getString("oidc_url", "") ?: ""
        oidcClientId = prefs.getString("oidc_client_id", "") ?: ""

        if (backendUrl.isBlank() || oidcUrl.isBlank() || oidcClientId.isBlank()) {
            // Prompt user to enter settings
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val settingsBtn = findViewById<Button>(R.id.settingsButton)
        settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        val loginBtn = findViewById<Button>(R.id.loginButton)
        val addBtn = findViewById<Button>(R.id.addButton)
        val nameEdit = findViewById<EditText>(R.id.nameEdit)
        val urlEdit = findViewById<EditText>(R.id.urlEdit)
        val recycler = findViewById<RecyclerView>(R.id.recyclerView)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ApplicationAdapter(
            onDelete = { app -> deleteApplication(app) },
            onEdit = { app -> showEditDialog(app) }
        )
        recycler.adapter = adapter

        // Handle authentication
        val authManager = AuthManager(this, oidcUrl, oidcClientId)
        authManager.loadConfig(
            onReady = {
                loginBtn.setOnClickListener {
                    val authRequest = authManager.getAuthRequest()
                    authManager.performAuthRequest(this, authRequest, AUTH_REQUEST_CODE)
                }
                // Try auto-login with saved token
                authManager.getSavedToken()?.let { token ->
                    ServiceBuilder.setBaseUrl(backendUrl)
                    ServiceBuilder.setToken(token)
                    api = ServiceBuilder.buildService(this, ApplicationApi::class.java)
                    getApplications()
                }
            },
            onError = {
                Toast.makeText(this, "OIDC config error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        )

        addBtn.setOnClickListener {
            val name = nameEdit.text.toString()
            val url = urlEdit.text.toString()
            if (name.isNotBlank() && url.isNotBlank()) {
                createApplication(Application(name = name, url = url))
                nameEdit.text.clear()
                urlEdit.text.clear()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Load OIDC config again (should already be loaded, but just in case)
            val authManager = AuthManager(this, oidcUrl, oidcClientId)
            authManager.handleAuthResponse(data,
                onSuccess = { tokenResponse: TokenResponse ->
                    val token = tokenResponse.accessToken
                    if (token != null) {
                        authManager.saveToken(token)
                        ServiceBuilder.setBaseUrl(backendUrl)
                        ServiceBuilder.setToken(token)
                        api = ServiceBuilder.buildService(this, ApplicationApi::class.java)
                        getApplications()
                        Toast.makeText(this, "Authenticated!", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = {
                    Toast.makeText(this, "Auth failed", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun getApplications() {
        api?.getApplications()?.enqueue(object : Callback<List<Application>> {
            override fun onResponse(call: Call<List<Application>>, response: Response<List<Application>>) {
                if (response.isSuccessful) {
                    adapter.submitList(response.body() ?: emptyList())
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load applications", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Application>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "API error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createApplication(app: Application) {
        api?.createApplication(app)?.enqueue(object : Callback<Application> {
            override fun onResponse(call: Call<Application>, response: Response<Application>) {
                if (response.isSuccessful) getApplications()
                else Toast.makeText(this@MainActivity, "Create failed", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Application>, t: Throwable) {
                Toast.makeText(this@MainActivity, "API error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteApplication(app: Application) {
        app.id?.let {
            api?.deleteApplication(it)?.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) getApplications()
                    else Toast.makeText(this@MainActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "API error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showEditDialog(app: Application) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_application, null)
        val nameEdit = dialogView.findViewById<EditText>(R.id.editName)
        val urlEdit = dialogView.findViewById<EditText>(R.id.editUrl)
        nameEdit.setText(app.name)
        urlEdit.setText(app.url)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Application")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameEdit.text.toString()
                val newUrl = urlEdit.text.toString()
                if (newName.isNotBlank() && newUrl.isNotBlank()) {
                    val updated = app.copy(name = newName, url = newUrl)
                    updateApplication(updated)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun updateApplication(app: Application) {
        api?.updateApplication(app.id!!, app)?.enqueue(object : Callback<Application> {
            override fun onResponse(call: Call<Application>, response: Response<Application>) {
                if (response.isSuccessful) getApplications()
                else Toast.makeText(this@MainActivity, "Update failed", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Application>, t: Throwable) {
                Toast.makeText(this@MainActivity, "API error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}