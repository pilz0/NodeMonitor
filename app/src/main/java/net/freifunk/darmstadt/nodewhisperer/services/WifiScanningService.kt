package net.freifunk.darmstadt.nodewhisperer.services

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import net.freifunk.darmstadt.nodewhisperer.models.WifiScanRaw
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Enhanced WiFi scanning service that captures comprehensive raw WiFi data
 * beyond just Gluon nodes for better network analysis and debugging.
 */
class WifiScanningService : Service() {
    
    private val binder = LocalBinder()
    private lateinit var wifiManager: WifiManager
    private var isScanning = false
    private var isPaused = false
    private var scanInterval = 5000L // Default 5 seconds
    private var scanResultListener: ScanResultListener? = null
    private val lastScanResults = CopyOnWriteArrayList<WifiScanRaw>()
    private val handler = Handler(Looper.getMainLooper())
    private var scanRunnable: Runnable? = null
    private var receiverRegistered = false
    
    interface ScanResultListener {
        fun onScanResults(results: List<WifiScanRaw>)
        fun onScanError(error: String)
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): WifiScanningService = this@WifiScanningService
    }
    
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("WifiScanningService", "Scan results received")
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                handleScanSuccess()
            } else {
                handleScanFailure()
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        Log.d("WifiScanningService", "Service created")
    }
    
    override fun onBind(intent: Intent): IBinder {
        Log.d("WifiScanningService", "Service bound")
        return binder
    }
    
    override fun onUnbind(intent: Intent): Boolean {
        Log.d("WifiScanningService", "Service unbound")
        stopContinuousScanning()
        return false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("WifiScanningService", "Service destroyed")
        stopContinuousScanning()
        unregisterReceiver()
    }
    
    /**
     * Sets the listener for scan results
     */
    fun setListener(listener: ScanResultListener) {
        this.scanResultListener = listener
    }
    
    /**
     * Starts continuous WiFi scanning with specified interval
     */
    fun startContinuousScanning(intervalMs: Long = 5000): Boolean {
        if (!checkPermissions()) {
            scanResultListener?.onScanError("Missing required permissions")
            return false
        }
        
        if (!wifiManager.isWifiEnabled) {
            scanResultListener?.onScanError("WiFi is disabled")
            return false
        }
        
        this.scanInterval = intervalMs
        this.isScanning = true
        this.isPaused = false
        
        registerReceiver()
        scheduleNextScan()
        
        Log.d("WifiScanningService", "Started continuous scanning with ${intervalMs}ms interval")
        return true
    }
    
    /**
     * Stops continuous WiFi scanning
     */
    fun stopContinuousScanning() {
        isScanning = false
        isPaused = false
        
        scanRunnable?.let { handler.removeCallbacks(it) }
        scanRunnable = null
        
        unregisterReceiver()
        
        Log.d("WifiScanningService", "Stopped continuous scanning")
    }
    
    /**
     * Performs a single WiFi scan
     */
    fun performSingleScan(): Boolean {
        if (!checkPermissions()) {
            scanResultListener?.onScanError("Missing required permissions")
            return false
        }
        
        if (!wifiManager.isWifiEnabled) {
            scanResultListener?.onScanError("WiFi is disabled")
            return false
        }
        
        registerReceiver()
        return wifiManager.startScan()
    }
    
    /**
     * Pauses continuous scanning (keeps service alive but stops scans)
     */
    fun pauseScanning() {
        if (isScanning) {
            isPaused = true
            scanRunnable?.let { handler.removeCallbacks(it) }
            Log.d("WifiScanningService", "Scanning paused")
        }
    }
    
    /**
     * Resumes continuous scanning after being paused
     */
    fun resumeScanning() {
        if (isScanning && isPaused) {
            isPaused = false
            scheduleNextScan()
            Log.d("WifiScanningService", "Scanning resumed")
        }
    }
    
    /**
     * Checks if scanning is currently active
     */
    fun isScanning(): Boolean {
        return isScanning && !isPaused
    }
    
    /**
     * Gets the last scan results
     */
    fun getLastScanResults(): List<WifiScanRaw> {
        return lastScanResults.toList()
    }
    
    private fun scheduleNextScan() {
        if (!isScanning || isPaused) return
        
        scanRunnable = Runnable {
            if (isScanning && !isPaused) {
                Log.d("WifiScanningService", "Starting scheduled scan")
                if (!wifiManager.startScan()) {
                    Log.w("WifiScanningService", "Failed to start scan")
                    scheduleNextScan() // Try again
                }
            }
        }
        
        handler.postDelayed(scanRunnable!!, scanInterval)
    }
    
    private fun handleScanSuccess() {
        if (!checkPermissions()) {
            scanResultListener?.onScanError("Missing required permissions during scan")
            return
        }
        
        try {
            val scanResults = wifiManager.scanResults ?: emptyList()
            val rawResults = scanResults.map { WifiScanRaw.fromScanResult(it) }
            
            // Update cached results
            lastScanResults.clear()
            lastScanResults.addAll(rawResults)
            
            Log.d("WifiScanningService", "Scan successful: ${rawResults.size} networks found")
            scanResultListener?.onScanResults(rawResults)
            
            // Schedule next scan if continuous scanning is enabled
            if (isScanning && !isPaused) {
                scheduleNextScan()
            }
            
        } catch (e: Exception) {
            Log.e("WifiScanningService", "Error processing scan results", e)
            scanResultListener?.onScanError("Error processing scan results: ${e.message}")
            
            // Still schedule next scan to continue scanning
            if (isScanning && !isPaused) {
                scheduleNextScan()
            }
        }
    }
    
    private fun handleScanFailure() {
        Log.w("WifiScanningService", "Scan failed")
        scanResultListener?.onScanError("WiFi scan failed")
        
        // Schedule next scan even after failure
        if (isScanning && !isPaused) {
            scheduleNextScan()
        }
    }
    
    private fun registerReceiver() {
        if (!receiverRegistered) {
            val intentFilter = IntentFilter().apply {
                addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            }
            registerReceiver(wifiScanReceiver, intentFilter)
            receiverRegistered = true
            Log.d("WifiScanningService", "Broadcast receiver registered")
        }
    }
    
    private fun unregisterReceiver() {
        if (receiverRegistered) {
            try {
                unregisterReceiver(wifiScanReceiver)
                receiverRegistered = false
                Log.d("WifiScanningService", "Broadcast receiver unregistered")
            } catch (e: IllegalArgumentException) {
                Log.w("WifiScanningService", "Receiver was not registered", e)
            }
        }
    }
    
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}