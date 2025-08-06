package com.roadrelief.app.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionManager {

    fun arePermissionsGranted(context: Context, permissions: List<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun shouldShowRequestPermissionRationale(activity: Activity, permissions: List<String>): Boolean {
        return permissions.any { activity.shouldShowRequestPermissionRationale(it) }
    }
}
