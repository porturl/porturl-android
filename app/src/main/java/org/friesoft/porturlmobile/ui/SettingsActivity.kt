package org.friesoft.porturlmobile.ui

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.friesoft.porturlmobile.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backendEdit = findViewById<EditText>(R.id.backendEdit)
        val oidcEdit = findViewById<EditText>(R.id.oidcEdit)
        val clientEdit = findViewById<EditText>(R.id.clientEdit)
        val saveBtn = findViewById<Button>(R.id.saveButton)

        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            "settings_prefs", masterKey, this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        backendEdit.setText(prefs.getString("backend_url", ""))
        oidcEdit.setText(prefs.getString("oidc_url", ""))
        clientEdit.setText(prefs.getString("oidc_client_id", ""))

        saveBtn.setOnClickListener {
            prefs.edit()
                .putString("backend_url", backendEdit.text.toString())
                .putString("oidc_url", oidcEdit.text.toString())
                .putString("oidc_client_id", clientEdit.text.toString())
                .apply()
            finish()
        }
    }
}