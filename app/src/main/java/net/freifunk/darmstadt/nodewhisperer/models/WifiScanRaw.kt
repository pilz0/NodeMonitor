package net.freifunk.darmstadt.nodewhisperer.models

import android.net.wifi.ScanResult
import android.os.Build
import java.util.Date

/**
 * Enhanced WiFi scan data class that captures comprehensive raw WiFi information
 * beyond just Gluon nodes for better network analysis and debugging.
 */
data class WifiScanRaw(
    val ssid: String,
    val bssid: String,
    val capabilities: String,
    val level: Int, // Signal strength in dBm
    val frequency: Int, // Frequency in MHz
    val timestamp: Date,
    
    // Additional Android API Level 23+ fields
    val centerFreq0: Int = 0,
    val centerFreq1: Int = 0, 
    val channelWidth: Int = 0,
    
    // Additional Android API Level 29+ fields  
    val is80211mcResponder: Boolean = false,
    val isPasspointNetwork: Boolean = false,
    val operatorFriendlyName: String? = null,
    val venueName: String? = null,
    
    // Raw information elements for detailed analysis
    val informationElements: List<InformationElementRaw> = emptyList()
) {
    
    companion object {
        /**
         * Creates a WifiScanRaw instance from Android's ScanResult
         */
        fun fromScanResult(scanResult: ScanResult): WifiScanRaw {
            val informationElements = scanResult.informationElements.map { ie ->
                val bytes = ByteArray(ie.bytes.remaining())
                ie.bytes.get(bytes)
                InformationElementRaw(ie.id, bytes)
            }
            
            return WifiScanRaw(
                ssid = scanResult.SSID ?: "",
                bssid = scanResult.BSSID ?: "",
                capabilities = scanResult.capabilities ?: "",
                level = scanResult.level,
                frequency = scanResult.frequency,
                timestamp = Date(scanResult.timestamp / 1000), // Convert microseconds to milliseconds
                centerFreq0 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) scanResult.centerFreq0 else 0,
                centerFreq1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) scanResult.centerFreq1 else 0,
                channelWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) scanResult.channelWidth else 0,
                is80211mcResponder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) scanResult.is80211mcResponder() else false,
                isPasspointNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) scanResult.isPasspointNetwork else false,
                operatorFriendlyName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) scanResult.operatorFriendlyName?.toString() else null,
                venueName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) scanResult.venueName?.toString() else null,
                informationElements = informationElements
            )
        }
    }
    
    /**
     * Gets the channel number from frequency
     */
    fun getChannel(): Int {
        return when {
            frequency in 2412..2484 -> (frequency - 2412) / 5 + 1
            frequency in 5170..5825 -> (frequency - 5000) / 5
            frequency in 5925..7125 -> (frequency - 5950) / 5 + 1 // 6GHz band
            else -> -1
        }
    }
    
    /**
     * Gets the WiFi band (2.4GHz, 5GHz, 6GHz)
     */
    fun getBand(): String {
        return when {
            frequency in 2412..2484 -> "2.4GHz"
            frequency in 5170..5825 -> "5GHz" 
            frequency in 5925..7125 -> "6GHz"
            else -> "Unknown"
        }
    }
    
    /**
     * Checks if this is likely a Gluon/Freifunk node based on SSID patterns
     */
    fun isLikelyGluonNode(): Boolean {
        return ssid.contains("freifunk", ignoreCase = true) || 
               ssid.contains("ffda", ignoreCase = true) ||
               ssid.matches(Regex("^[a-f0-9]{12}$")) // MAC address pattern for mesh nodes
    }
    
    /**
     * Gets security type from capabilities string
     */
    fun getSecurityType(): String {
        return when {
            capabilities.contains("WPA3") -> "WPA3"
            capabilities.contains("WPA2") -> "WPA2" 
            capabilities.contains("WPA") -> "WPA"
            capabilities.contains("WEP") -> "WEP"
            else -> "Open"
        }
    }
    
    /**
     * Converts to debug string format for export
     */
    fun toDebugString(): String {
        return buildString {
            appendLine("SSID: $ssid")
            appendLine("BSSID: $bssid")
            appendLine("Signal: ${level}dBm")
            appendLine("Frequency: ${frequency}MHz (Channel ${getChannel()}, ${getBand()})")
            appendLine("Security: ${getSecurityType()}")
            appendLine("Capabilities: $capabilities")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                appendLine("Channel Width: $channelWidth")
                appendLine("Center Freq 0: $centerFreq0")
                appendLine("Center Freq 1: $centerFreq1")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appendLine("802.11mc Responder: $is80211mcResponder")
                appendLine("Passpoint Network: $isPasspointNetwork")
                operatorFriendlyName?.let { appendLine("Operator: $it") }
                venueName?.let { appendLine("Venue: $it") }
            }
            appendLine("Timestamp: $timestamp")
            appendLine("Likely Gluon Node: ${isLikelyGluonNode()}")
            appendLine("Information Elements: ${informationElements.size}")
        }
    }
}

/**
 * Raw representation of 802.11 Information Elements
 */
data class InformationElementRaw(
    val id: Int,
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InformationElementRaw

        if (id != other.id) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + bytes.contentHashCode()
        return result
    }
    
    fun toHexString(): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}