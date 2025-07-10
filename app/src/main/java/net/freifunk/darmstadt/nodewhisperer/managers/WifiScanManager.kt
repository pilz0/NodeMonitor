package net.freifunk.darmstadt.nodewhisperer.managers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import net.freifunk.darmstadt.nodewhisperer.models.WifiScanRaw
import net.freifunk.darmstadt.nodewhisperer.services.WifiScanningService

/**
 * Manager class that simplifies interaction with the WiFi scanning service.
 * Handles service binding/unbinding and provides a clean callback interface.
 */
class WifiScanManager(private val context: Context) {
    
    private var wifiScanningService: WifiScanningService? = null
    private var isBound = false
    private var scanResultListener: ScanResultListener? = null
    
    interface ScanResultListener {
        fun onScanResults(results: List<WifiScanRaw>)
        fun onScanError(error: String)
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("WifiScanManager", "Service connected")
            val binder = service as WifiScanningService.LocalBinder
            wifiScanningService = binder.getService()
            isBound = true
            
            // Register for scan results
            wifiScanningService?.setListener(object : WifiScanningService.ScanResultListener {
                override fun onScanResults(results: List<WifiScanRaw>) {
                    scanResultListener?.onScanResults(results)
                }
                
                override fun onScanError(error: String) {
                    scanResultListener?.onScanError(error)
                }
            })
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("WifiScanManager", "Service disconnected")
            wifiScanningService = null
            isBound = false
        }
    }
    
    /**
     * Binds to the WiFi scanning service
     */
    fun bindService(): Boolean {
        val intent = Intent(context, WifiScanningService::class.java)
        return context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    /**
     * Unbinds from the WiFi scanning service
     */
    fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
            wifiScanningService = null
        }
    }
    
    /**
     * Sets the listener for scan results
     */
    fun setScanResultListener(listener: ScanResultListener) {
        this.scanResultListener = listener
    }
    
    /**
     * Starts continuous WiFi scanning
     */
    fun startContinuousScanning(intervalMs: Long = 5000): Boolean {
        return wifiScanningService?.startContinuousScanning(intervalMs) ?: false
    }
    
    /**
     * Stops continuous WiFi scanning
     */
    fun stopContinuousScanning() {
        wifiScanningService?.stopContinuousScanning()
    }
    
    /**
     * Performs a single WiFi scan
     */
    fun performSingleScan(): Boolean {
        return wifiScanningService?.performSingleScan() ?: false
    }
    
    /**
     * Checks if scanning is currently active
     */
    fun isScanning(): Boolean {
        return wifiScanningService?.isScanning() ?: false
    }
    
    /**
     * Checks if the service is bound and ready
     */
    fun isServiceReady(): Boolean {
        return isBound && wifiScanningService != null
    }
    
    /**
     * Gets the last scan results if available
     */
    fun getLastScanResults(): List<WifiScanRaw> {
        return wifiScanningService?.getLastScanResults() ?: emptyList()
    }
    
    /**
     * Pauses scanning (keeps service bound but stops scans)
     */
    fun pauseScanning() {
        wifiScanningService?.pauseScanning()
    }
    
    /**
     * Resumes scanning after being paused
     */
    fun resumeScanning() {
        wifiScanningService?.resumeScanning()
    }
}