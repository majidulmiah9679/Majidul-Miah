package com.example.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build

data class DeviceTelemetry(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val networkType: String,
    val isConnected: Boolean,
    val availableRamMb: Long,
    val totalRamMb: Long,
    val deviceModel: String,
    val androidVersion: String
) {
    fun summary(): String {
        val chargeStatus = if (isCharging) "Charging" else "Discharging"
        return "Power: $batteryPercent% ($chargeStatus), Network: $networkType, RAM: ${availableRamMb}MB/${totalRamMb}MB available, Unit: $deviceModel (Android $androidVersion)"
    }
}

object JarvisDiagnostics {

    fun getTelemetry(context: Context): DeviceTelemetry {
        // Battery
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Connectivity
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connMgr?.activeNetwork
        val caps = connMgr?.getNetworkCapabilities(activeNetwork)
        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val networkType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi (High Bandwidth)"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular LTE/5G"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Direct Ethernet"
            !isConnected -> "Offline (Local Only)"
            else -> "Active Relay"
        }

        // Memory
        val actMgr = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actMgr?.getMemoryInfo(memInfo)
        val availRam = memInfo.availMem / (1024 * 1024)
        val totalRam = memInfo.totalMem / (1024 * 1024)

        return DeviceTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            networkType = networkType,
            isConnected = isConnected,
            availableRamMb = availRam,
            totalRamMb = totalRam,
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            androidVersion = Build.VERSION.RELEASE
        )
    }
}
