package net.freifunk.darmstadt.nodewhisperer.models

import org.junit.Test
import org.junit.Assert.*
import java.util.Date

/**
 * Unit tests for WifiScanRaw model functionality.
 * Tests the core logic for WiFi data processing and utility methods.
 */
class WifiScanRawTest {

    @Test
    fun getChannel_2_4GHz_isCorrect() {
        val wifiScanRaw = WifiScanRaw(
            ssid = "TestNetwork",
            bssid = "00:11:22:33:44:55",
            capabilities = "WPA2",
            level = -50,
            frequency = 2412, // Channel 1
            timestamp = Date()
        )
        assertEquals(1, wifiScanRaw.getChannel())
        
        val wifiScanRaw6 = wifiScanRaw.copy(frequency = 2437) // Channel 6
        assertEquals(6, wifiScanRaw6.getChannel())
    }

    @Test
    fun getChannel_5GHz_isCorrect() {
        val wifiScanRaw = WifiScanRaw(
            ssid = "TestNetwork5G",
            bssid = "00:11:22:33:44:55",
            capabilities = "WPA2",
            level = -60,
            frequency = 5180, // Channel 36
            timestamp = Date()
        )
        assertEquals(36, wifiScanRaw.getChannel())
    }

    @Test
    fun getBand_isCorrect() {
        val wifi2_4 = WifiScanRaw(
            ssid = "Test2.4",
            bssid = "00:11:22:33:44:55",
            capabilities = "WPA2",
            level = -50,
            frequency = 2412,
            timestamp = Date()
        )
        assertEquals("2.4GHz", wifi2_4.getBand())

        val wifi5 = wifi2_4.copy(frequency = 5180)
        assertEquals("5GHz", wifi5.getBand())

        val wifi6 = wifi2_4.copy(frequency = 5955)
        assertEquals("6GHz", wifi6.getBand())

        val wifiUnknown = wifi2_4.copy(frequency = 1000)
        assertEquals("Unknown", wifiUnknown.getBand())
    }

    @Test
    fun isLikelyGluonNode_freifunkNetwork_returnsTrue() {
        val freifunkNetwork = WifiScanRaw(
            ssid = "freifunk-darmstadt",
            bssid = "00:11:22:33:44:55",
            capabilities = "Open",
            level = -50,
            frequency = 2412,
            timestamp = Date()
        )
        assertTrue(freifunkNetwork.isLikelyGluonNode())
    }

    @Test
    fun isLikelyGluonNode_ffdaNetwork_returnsTrue() {
        val ffdaNetwork = WifiScanRaw(
            ssid = "ffda-mesh",
            bssid = "00:11:22:33:44:55",
            capabilities = "Open",
            level = -50,
            frequency = 2412,
            timestamp = Date()
        )
        assertTrue(ffdaNetwork.isLikelyGluonNode())
    }

    @Test
    fun isLikelyGluonNode_macAddressPattern_returnsTrue() {
        val macPattern = WifiScanRaw(
            ssid = "aabbccddeeff",
            bssid = "00:11:22:33:44:55",
            capabilities = "Open",
            level = -50,
            frequency = 2412,
            timestamp = Date()
        )
        assertTrue(macPattern.isLikelyGluonNode())
    }

    @Test
    fun isLikelyGluonNode_regularNetwork_returnsFalse() {
        val regularNetwork = WifiScanRaw(
            ssid = "HomeWiFi",
            bssid = "00:11:22:33:44:55",
            capabilities = "WPA2",
            level = -50,
            frequency = 2412,
            timestamp = Date()
        )
        assertFalse(regularNetwork.isLikelyGluonNode())
    }

    @Test
    fun getSecurityType_isCorrect() {
        val openNetwork = WifiScanRaw(
            ssid = "OpenWiFi",
            bssid = "00:11:22:33:44:55",
            capabilities = "",
            level = -50,
            frequency = 2412,
            timestamp = Date()
        )
        assertEquals("Open", openNetwork.getSecurityType())

        val wpa2Network = openNetwork.copy(capabilities = "WPA2-PSK")
        assertEquals("WPA2", wpa2Network.getSecurityType())

        val wpa3Network = openNetwork.copy(capabilities = "WPA3-SAE")
        assertEquals("WPA3", wpa3Network.getSecurityType())

        val wepNetwork = openNetwork.copy(capabilities = "WEP")
        assertEquals("WEP", wepNetwork.getSecurityType())
    }

    @Test
    fun toDebugString_containsAllMainInfo() {
        val testNetwork = WifiScanRaw(
            ssid = "TestNetwork",
            bssid = "00:11:22:33:44:55",
            capabilities = "WPA2-PSK",
            level = -65,
            frequency = 2437, // Channel 6
            timestamp = Date()
        )
        
        val debugString = testNetwork.toDebugString()
        
        // Check that debug string contains all important information
        assertTrue("Debug string should contain SSID", debugString.contains("SSID: TestNetwork"))
        assertTrue("Debug string should contain BSSID", debugString.contains("BSSID: 00:11:22:33:44:55"))
        assertTrue("Debug string should contain signal", debugString.contains("Signal: -65dBm"))
        assertTrue("Debug string should contain frequency", debugString.contains("Frequency: 2437MHz"))
        assertTrue("Debug string should contain channel", debugString.contains("Channel 6"))
        assertTrue("Debug string should contain band", debugString.contains("2.4GHz"))
        assertTrue("Debug string should contain security", debugString.contains("Security: WPA2"))
        assertTrue("Debug string should contain Gluon detection", debugString.contains("Likely Gluon Node: false"))
    }

    @Test
    fun informationElementRaw_equalsAndHashCode() {
        val ie1 = InformationElementRaw(1, byteArrayOf(0x01, 0x02, 0x03))
        val ie2 = InformationElementRaw(1, byteArrayOf(0x01, 0x02, 0x03))
        val ie3 = InformationElementRaw(2, byteArrayOf(0x01, 0x02, 0x03))
        
        assertEquals(ie1, ie2)
        assertEquals(ie1.hashCode(), ie2.hashCode())
        assertNotEquals(ie1, ie3)
    }

    @Test
    fun informationElementRaw_toHexString() {
        val ie = InformationElementRaw(1, byteArrayOf(0x01, 0x02, 0xff.toByte()))
        assertEquals("0102ff", ie.toHexString())
    }
}