package com.cepda.firetv.launcherwrapper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast

object AppLauncher {

    private const val TAG = "AppLauncher"

    fun launchApp(context: Context, componentName: android.content.ComponentName) {
        try {
            val intent = Intent().apply {
                component = componentName
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pm = context.packageManager
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
            } else {
                throw PackageManager.NameNotFoundException("Activity not resolvable")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch app: {componentName.flattenToString()}", e)
            Toast.makeText(
                context,
                "No se pudo abrir la aplicacion",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}