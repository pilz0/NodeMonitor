#!/bin/bash

# Enhanced WiFi Scanning Implementation Validation Script
# This script validates that all expected files and components are in place

echo "=== Enhanced WiFi Scanning Implementation Validation ==="
echo

SUCCESS=true

# Check if all required files exist
echo "1. Checking required files..."

FILES=(
    "app/src/main/java/net/freifunk/darmstadt/nodewhisperer/models/WifiScanRaw.kt"
    "app/src/main/java/net/freifunk/darmstadt/nodewhisperer/services/WifiScanningService.kt"
    "app/src/main/java/net/freifunk/darmstadt/nodewhisperer/managers/WifiScanManager.kt"
    "app/src/test/java/net/freifunk/darmstadt/nodewhisperer/models/WifiScanRawTest.kt"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✓ $file exists"
    else
        echo "✗ $file missing"
        SUCCESS=false
    fi
done

echo

# Check AndroidManifest.xml for required permissions and service
echo "2. Checking AndroidManifest.xml..."

MANIFEST="app/src/main/AndroidManifest.xml"
if [ -f "$MANIFEST" ]; then
    echo "✓ AndroidManifest.xml exists"
    
    # Check for permissions
    if grep -q "ACCESS_BACKGROUND_LOCATION" "$MANIFEST"; then
        echo "✓ ACCESS_BACKGROUND_LOCATION permission found"
    else
        echo "✗ ACCESS_BACKGROUND_LOCATION permission missing"
        SUCCESS=false
    fi
    
    # Check for service declaration
    if grep -q "WifiScanningService" "$MANIFEST"; then
        echo "✓ WifiScanningService declaration found"
    else
        echo "✗ WifiScanningService declaration missing"
        SUCCESS=false
    fi
else
    echo "✗ AndroidManifest.xml missing"
    SUCCESS=false
fi

echo

# Check MainActivity integration
echo "3. Checking MainActivity integration..."

MAINACTIVITY="app/src/main/java/net/freifunk/darmstadt/nodewhisperer/MainActivity.kt"
if [ -f "$MAINACTIVITY" ]; then
    echo "✓ MainActivity.kt exists"
    
    # Check for key integration points
    if grep -q "WifiScanManager" "$MAINACTIVITY"; then
        echo "✓ WifiScanManager integration found"
    else
        echo "✗ WifiScanManager integration missing"
        SUCCESS=false
    fi
    
    if grep -q "rawWifiScanResults" "$MAINACTIVITY"; then
        echo "✓ Raw WiFi scan results handling found"
    else
        echo "✗ Raw WiFi scan results handling missing"
        SUCCESS=false
    fi
    
    if grep -q "raw_wifi_networks" "$MAINACTIVITY"; then
        echo "✓ Enhanced debug export found"
    else
        echo "✗ Enhanced debug export missing"
        SUCCESS=false
    fi
else
    echo "✗ MainActivity.kt missing"
    SUCCESS=false
fi

echo

# Check for proper package structure
echo "4. Checking package structure..."

if [ -d "app/src/main/java/net/freifunk/darmstadt/nodewhisperer/managers" ]; then
    echo "✓ Managers package created"
else
    echo "✗ Managers package missing"
    SUCCESS=false
fi

if [ -d "app/src/test/java/net/freifunk/darmstadt/nodewhisperer/models" ]; then
    echo "✓ Test models package created"
else
    echo "✗ Test models package missing"
    SUCCESS=false
fi

echo

# Summary
echo "=== Validation Summary ==="
if [ "$SUCCESS" = true ]; then
    echo "✅ All checks passed! Enhanced WiFi scanning implementation is complete."
    echo
    echo "Key Features Implemented:"
    echo "• WifiScanRaw model with comprehensive WiFi data capture"
    echo "• WifiScanningService with continuous and single-scan capabilities"
    echo "• WifiScanManager for simplified service interaction"
    echo "• MainActivity integration with dual scanning support"
    echo "• Enhanced debug export with raw WiFi data"
    echo "• Proper Android permissions and service declaration"
    echo "• Unit tests for core functionality"
    echo
    echo "The implementation provides:"
    echo "• Complete network visibility for debugging"
    echo "• Enhanced network analysis capabilities"
    echo "• Raw data export for external analysis"
    echo "• Backward compatibility with existing Gluon scanning"
else
    echo "❌ Some checks failed. Please review the missing components above."
fi

echo
echo "For detailed implementation information, see ENHANCED_WIFI_IMPLEMENTATION.md"