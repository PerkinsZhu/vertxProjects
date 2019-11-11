package com.perkins.excel

import com.perkins.excel.bean.AimBean
import org.apache.poi.ss.usermodel.Row
import java.io.File

class AimService : BaseService() {
    fun loadFile(file: File): List<AimBean?> {
        val transfer = { row: Row, keyMap: Map<String, Int>? ->
            AimBean.fromRow(row, keyMap)
        }
        return loadFile(file, transfer)?.filter { !(it?.name?.isNullOrBlank() ?: true) }
    }

}