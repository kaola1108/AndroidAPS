package info.nightscout.androidaps.plugins.pump.omnipod.dash.driver.comm.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import info.nightscout.androidaps.logging.AAPSLogger
import info.nightscout.androidaps.logging.LTag
import info.nightscout.androidaps.plugins.pump.omnipod.dash.driver.comm.exceptions.ScanFailException
import info.nightscout.androidaps.plugins.pump.omnipod.dash.driver.comm.exceptions.ScanFailFoundTooManyException
import info.nightscout.androidaps.plugins.pump.omnipod.dash.driver.comm.exceptions.ScanFailNotFoundException
import java.util.*

class PodScanner(private val logger: AAPSLogger, private val bluetoothAdapter: BluetoothAdapter) {

    @Throws(InterruptedException::class, ScanFailException::class)
    fun scanForPod(serviceUUID: String?, podID: Long): BleDiscoveredDevice {
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(serviceUUID))
            .build()
        val scanSettings = ScanSettings.Builder()
            .setLegacy(false)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val scanCollector = ScanCollector(logger, podID)
        logger.debug(LTag.PUMPBTCOMM, "Scanning with filters: $filter settings$scanSettings")
        scanner.startScan(Arrays.asList(filter), scanSettings, scanCollector)
        Thread.sleep(SCAN_DURATION_MS.toLong())
        scanner.flushPendingScanResults(scanCollector)
        scanner.stopScan(scanCollector)
        val collected = scanCollector.collect()
        if (collected.isEmpty()) {
            throw ScanFailNotFoundException()
        } else if (collected.size > 1) {
            throw ScanFailFoundTooManyException(collected)
        }
        return collected[0]
    }

    companion object {

        const val SCAN_FOR_SERVICE_UUID = "00004024-0000-1000-8000-00805F9B34FB"
        const val POD_ID_NOT_ACTIVATED = 0xFFFFFFFFL
        private const val SCAN_DURATION_MS = 5000
    }
}