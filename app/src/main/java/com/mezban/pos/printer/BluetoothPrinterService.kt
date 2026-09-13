package com.mezban.pos.printer

import android.content.Context
import com.mezban.pos.data.ReceiptData

object BluetoothPrinterService {
    fun printReceipt(context: Context, receipt: ReceiptData): Boolean {
        // Bluetooth print command stub (ESC/POS 58mm)
        return true
    }
}
