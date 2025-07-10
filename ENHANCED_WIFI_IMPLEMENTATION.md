# Enhanced WiFi Scanning Service Implementation

This implementation adds comprehensive WiFi scanning capabilities with raw data export to the NodeMonitor application. The enhancement provides detailed network analysis and debugging capabilities for Freifunk network monitoring while maintaining compatibility with existing Gluon node scanning.

## Implementation Overview

### 1. Enhanced WiFi Data Model (`WifiScanRaw.kt`)

**Location:** `app/src/main/java/net/freifunk/darmstadt/nodewhisperer/models/WifiScanRaw.kt`

A comprehensive data class that captures all available WiFi scan information:
- **Basic WiFi Data:** SSID, BSSID, signal strength, frequency, capabilities, timestamp
- **Android API 23+ Fields:** Center frequencies, channel width
- **Android API 29+ Fields:** 802.11mc responder capability, Passpoint network status, operator friendly name, venue name
- **Raw Information Elements:** Complete 802.11 information elements for detailed analysis

**Key Features:**
- Conversion utilities from Android's `ScanResult`
- Channel and band calculation methods (`getChannel()`, `getBand()`)
- Gluon node detection heuristics (`isLikelyGluonNode()`)
- Security type parsing (`getSecurityType()`)
- Debug string formatting for export (`toDebugString()`)
- Android version compatibility handling

### 2. Enhanced WiFi Scanning Service (`WifiScanningService.kt`)

**Location:** `app/src/main/java/net/freifunk/darmstadt/nodewhisperer/services/WifiScanningService.kt`

A comprehensive Service implementation that provides:
- **Service Architecture:** Proper Android Service with LocalBinder for lifecycle management
- **Scanning Modes:** Continuous scanning with configurable intervals and single-scan capability
- **BroadcastReceiver Pattern:** Efficient scan result handling using WiFi manager callbacks
- **Permission Management:** Proper handling of location and WiFi permissions
- **Lifecycle Management:** Start/stop/pause/resume functionality with thread safety
- **Error Handling:** Comprehensive error reporting and recovery mechanisms

**Key Methods:**
- `startContinuousScanning(intervalMs)` - Start periodic scans
- `performSingleScan()` - Trigger one-time scan
- `pauseScanning()` / `resumeScanning()` - Lifecycle management
- `getLastScanResults()` - Access cached results

### 3. WiFi Scan Manager (`WifiScanManager.kt`)

**Location:** `app/src/main/java/net/freifunk/darmstadt/nodewhisperer/managers/WifiScanManager.kt`

A manager class that simplifies service interaction:
- **Service Binding:** Automatic binding/unbinding to WifiScanningService
- **Clean Interface:** Simplified callback interface for scan results
- **Lifecycle Management:** Proper service lifecycle handling
- **State Management:** Tracking of service readiness and scanning state

**Usage Example:**
```kotlin
val wifiScanManager = WifiScanManager(context)
wifiScanManager.bindService()
wifiScanManager.setScanResultListener(object : WifiScanManager.ScanResultListener {
    override fun onScanResults(results: List<WifiScanRaw>) {
        // Handle scan results
    }
    override fun onScanError(error: String) {
        // Handle errors
    }
})
wifiScanManager.startContinuousScanning(5000) // 5 second intervals
```

### 4. MainActivity Integration

**Enhanced Features:**
- **Dual Scanning:** Runs enhanced raw WiFi scanning alongside existing Gluon node scanning
- **Raw Data Collection:** Stores comprehensive WiFi data for export
- **Enhanced Debug Export:** `generateRawDebugInfo()` now includes detailed raw WiFi data
- **Permission Handling:** Added support for Android 10+ background location permission
- **Lifecycle Integration:** Proper start/stop/pause/resume of enhanced scanning

**Debug Export Enhancement:**
The debug export now includes:
- Total count of raw WiFi networks discovered
- Detailed information for each network (SSID, BSSID, signal, frequency, channel, band, security)
- Android version-specific fields when available
- Gluon node detection results

### 5. Android Manifest Updates

**New Permissions:**
- `ACCESS_BACKGROUND_LOCATION` - Required for Android 10+ background scanning
- Reorganized existing permissions for clarity

**Service Declaration:**
- Properly declared `WifiScanningService` as internal service

## Usage and Benefits

### For Network Analysis
- **Complete Visibility:** See all WiFi networks, not just Gluon nodes
- **Detailed Information:** Signal strength, frequency, channel, security details
- **Raw Data Export:** Export comprehensive data for external analysis tools
- **Debugging Support:** Enhanced debug information for troubleshooting

### For Freifunk Network Monitoring
- **Gluon Node Detection:** Automatic identification of likely Gluon nodes in raw data
- **Network Coverage Analysis:** Complete picture of WiFi environment
- **Signal Analysis:** Detailed signal strength and frequency information
- **Interference Detection:** Identify competing networks and interference sources

### Integration Benefits
- **Backward Compatibility:** Existing UI and Gluon scanning functionality unchanged
- **Seamless Operation:** Enhanced scanning runs alongside existing features
- **Minimal Resource Impact:** Efficient scanning with configurable intervals
- **Proper Lifecycle:** Battery-efficient implementation with pause/resume

## Technical Implementation Details

### Android Version Compatibility
The implementation handles different Android API levels gracefully:
- **API 23+:** Channel width and center frequency information
- **API 29+:** 802.11mc responder and Passpoint network detection
- **Older Versions:** Graceful degradation with default values

### Permission Handling
- **Runtime Permissions:** Proper request handling for location and WiFi permissions
- **Background Location:** Android 10+ background location permission support
- **Graceful Degradation:** Continues operation with available permissions

### Battery Efficiency
- **Configurable Intervals:** Adjustable scan frequency to balance data freshness and battery usage
- **Pause/Resume:** Automatic pausing during app lifecycle changes
- **Error Recovery:** Robust error handling to prevent resource leaks

### Thread Safety
- **CopyOnWriteArrayList:** Thread-safe result storage
- **Handler-based Scheduling:** Main thread scheduling for UI updates
- **Service Lifecycle:** Proper cleanup on service destruction

## Testing

### Unit Tests
**Location:** `app/src/test/java/net/freifunk/darmstadt/nodewhisperer/models/WifiScanRawTest.kt`

Comprehensive unit tests for the WifiScanRaw model covering:
- Channel calculation for 2.4GHz, 5GHz, and 6GHz bands
- Band detection logic
- Gluon node detection heuristics
- Security type parsing
- Debug string formatting
- Information element handling

### Integration Testing
The implementation integrates seamlessly with existing functionality:
- Gluon node scanning continues to work as before
- Enhanced raw data is collected in parallel
- Debug export includes both Gluon nodes and raw WiFi data
- UI remains unchanged for user experience consistency

## Future Enhancements

Potential areas for future improvement:
1. **Historical Data Storage:** Persistent storage of scan results over time
2. **Statistical Analysis:** Network availability and signal strength trends
3. **Map Integration:** Geographic mapping of WiFi networks
4. **Advanced Filtering:** User-configurable filtering of scan results
5. **Export Formats:** Additional export formats (JSON, CSV, XML)

## Conclusion

This implementation significantly enhances the NodeMonitor application's network analysis capabilities while maintaining full backward compatibility. The modular design allows for easy extension and maintenance, while the comprehensive raw data export provides powerful debugging and analysis capabilities for Freifunk network monitoring.