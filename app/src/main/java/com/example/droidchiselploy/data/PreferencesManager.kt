package com.example.droidchiselploy.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "droidchiselploy_prefs",
        Context.MODE_PRIVATE
    )

    fun saveChiselCommand(command: String) {
        prefs.edit { putString("chisel_command", command) }
    }

    fun getChiselCommand(): String {
        return prefs.getString("chisel_command", "client ...") ?: "client ..."
    }

}

