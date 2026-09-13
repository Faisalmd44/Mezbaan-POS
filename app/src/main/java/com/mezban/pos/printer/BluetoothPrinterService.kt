package com.mezban.pos.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.mezban.pos.viewmodel.ReceiptData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object BluetoothPrinterService {
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        return adapter.bondedDevices.toList()
    }

    @SuppressLint("MissingPermission")
    suspend fun printReceipt(device: BluetoothDevice, receipt: ReceiptData): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            val outputStream = socket.outputStream

            outputStream.write(byteArrayOf(0x1B, 0x40)) // ESC @ (Initialize printer)
            outputStream.write(byteArrayOf(0x1B, 0x61, 0x01)) // Center align
            outputStream.write(byteArrayOf(0x1D, 0x21, 0x11)) // Double size
            outputStream.write("MEZBAN\n".toByteArray(Charsets.US_ASCII))
            outputStream.write(byteArrayOf(0x1D, 0x21, 0x00)) // Normal size
            outputStream.write("Fast Food & Burgers\n".toByteArray(Charsets.US_ASCII))
            outputStream.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))

            outputStream.write(byteArrayOf(0x1B, 0x61, 0x00)) // Left align
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            outputStream.write("Bill: ${receipt.billNumber}\n".toByteArray(Charsets.US_ASCII))
            outputStream.write("Date: ${sdf.format(Date(receipt.timestampMillis))}\n".toByteArray(Charsets.US_ASCII))
            outputStream.write("Staff: ${receipt.staffName}\n".toByteArray(Charsets.US_ASCII))
            outputStream.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))

            receipt.lines.forEach { line ->
                val name = if (line.name.length > 17) line.name.take(15) + ".." else line.name.padEnd(17)
                val qty = "x${line.qty}".padEnd(4)
                val amt = "Rs.${line.lineTotal.toInt()}".padStart(9)
                outputStream.write("$name$qty$amt\n".toByteArray(Charsets.US_ASCII))
            }

            outputStream.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))
            outputStream.write("Subtotal: Rs.${receipt.subtotal.toInt()}\n".toByteArray(Charsets.US_ASCII))
            if (receipt.discountAmount > 0) {
                outputStream.write("Discount: -Rs.${receipt.discountAmount.toInt()}\n".toByteArray(Charsets.US_ASCII))
            }
            outputStream.write("GST (5%): Rs.${receipt.taxAmount.toInt()}\n".toByteArray(Charsets.US_ASCII))
            outputStream.write(byteArrayOf(0x1B, 0x45, 0x01)) // Bold on
            outputStream.write("TOTAL:    Rs.${receipt.total.toInt()}\n".toByteArray(Charsets.US_ASCII))
            outputStream.write(byteArrayOf(0x1B, 0x45, 0x00)) // Bold off
            outputStream.write("Payment:  ${receipt.paymentMode.label}\n".toByteArray(Charsets.US_ASCII))
            outputStream.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))

            outputStream.write(byteArrayOf(0x1B, 0x61, 0x01)) // Center align
            outputStream.write("Thank you for visiting MEZBAN!\n\n\n\n".toByteArray(Charsets.US_ASCII))
            outputStream.write(byteArrayOf(0x1D, 0x56, 0x41, 0x10)) // Cut paper

            outputStream.flush()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }
}
