package com.mezban.pos.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.mezban.pos.R
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
    suspend fun printReceipt(context: Context, device: BluetoothDevice, receipt: ReceiptData): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            val out = socket.outputStream

            // Initialize printer & align center
            out.write(byteArrayOf(0x1B, 0x40))
            out.write(byteArrayOf(0x1B, 0x61, 0x01))

            // Print Mezbaan Logo if exists
            try {
                val logoBmp = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
                if (logoBmp != null) {
                    val scaled = Bitmap.createScaledBitmap(logoBmp, 384, (384f * logoBmp.height / logoBmp.width).toInt(), true)
                    val rasterBytes = decodeBitmapToEscPos(scaled)
                    out.write(rasterBytes)
                    out.write(byteArrayOf(0x0A))
                }
            } catch (_: Exception) {}

            // Header Tagline
            out.write("Freshly Crafted, Honestly Served.\n".toByteArray(Charsets.US_ASCII))
            out.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))

            // Details Left Aligned
            out.write(byteArrayOf(0x1B, 0x61, 0x00))
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            out.write("Bill: ${receipt.billNumber}\n".toByteArray(Charsets.US_ASCII))
            out.write("Date: ${sdf.format(Date(receipt.timestampMillis))}\n".toByteArray(Charsets.US_ASCII))
            out.write("Staff: ${receipt.staffName}\n".toByteArray(Charsets.US_ASCII))
            out.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))

            receipt.lines.forEach { line ->
                val name = if (line.name.length > 17) line.name.take(15) + ".." else line.name.padEnd(17)
                val qty = "x${line.qty}".padEnd(4)
                val amt = "Rs.${line.lineTotal.toInt()}".padStart(9)
                out.write("$name$qty$amt\n".toByteArray(Charsets.US_ASCII))
            }

            out.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))
            out.write("Subtotal: Rs.${receipt.subtotal.toInt()}\n".toByteArray(Charsets.US_ASCII))
            if (receipt.discountAmount > 0) {
                out.write("Discount: -Rs.${receipt.discountAmount.toInt()}\n".toByteArray(Charsets.US_ASCII))
            }
            out.write("GST (5%): Rs.${receipt.taxAmount.toInt()}\n".toByteArray(Charsets.US_ASCII))
            out.write(byteArrayOf(0x1B, 0x45, 0x01)) // Bold
            out.write("TOTAL:    Rs.${receipt.total.toInt()}\n".toByteArray(Charsets.US_ASCII))
            out.write(byteArrayOf(0x1B, 0x45, 0x00))
            out.write("Payment:  ${receipt.paymentMode.label}\n".toByteArray(Charsets.US_ASCII))
            out.write("--------------------------------\n".toByteArray(Charsets.US_ASCII))

            out.write(byteArrayOf(0x1B, 0x61, 0x01))
            out.write("Thank you for visiting MEZBAAN!\n\n\n\n".toByteArray(Charsets.US_ASCII))
            out.write(byteArrayOf(0x1D, 0x56, 0x41, 0x10)) // Feed & Cut

            out.flush()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    private fun decodeBitmapToEscPos(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8
        val data = ByteArray(8 + widthBytes * height)

        data[0] = 0x1D
        data[1] = 0x76
        data[2] = 0x30
        data[3] = 0x00
        data[4] = (widthBytes and 0xFF).toByte()
        data[5] = ((widthBytes shr 8) and 0xFF).toByte()
        data[6] = (height and 0xFF).toByte()
        data[7] = ((height shr 8) and 0xFF).toByte()

        var index = 8
        for (y in 0 until height) {
            for (x in 0 until widthBytes) {
                var b = 0
                for (bit in 0..7) {
                    val px = x * 8 + bit
                    if (px < width) {
                        val pixel = bitmap.getPixel(px, y)
                        val r = Color.red(pixel)
                        val g = Color.green(pixel)
                        val blue = Color.blue(pixel)
                        val luminance = (0.299 * r + 0.587 * g + 0.114 * blue).toInt()
                        if (luminance < 140) { // Black pixel on white receipt paper
                            b = b or (1 shl (7 - bit))
                        }
                    }
                }
                data[index++] = b.toByte()
            }
        }
        return data
    }
}
