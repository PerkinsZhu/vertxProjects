package com.perkins.excel

import com.perkins.excel.bean.OrignBean
import org.apache.poi.ss.usermodel.Row
import java.io.File

class OrignService : BaseService() {

    fun loadFile(file: File): List<OrignBean?> {
        val transfer = { row: Row, keyMap: Map<String, Int>? ->
            OrignBean.fromRow(row, keyMap)
        }
        return loadFile(file, transfer)?.filter { !(it?.name?.isNullOrBlank() ?: true) }
    }


}