package com.perkins.excel

import com.perkins.excel.bean.OrignBean
import com.perkins.excel.bean.VacationBean
import org.apache.poi.ss.usermodel.Row
import java.io.File

class VacationService : BaseService() {
    fun loadFile(file: File): List<VacationBean?> {
        val transfer = { row: Row, keyMap: Map<String, Int>? ->
            VacationBean.fromRow(row, keyMap)
        }
        return super.loadFile(file, transfer)?.filter { !(it?.name?.isNullOrBlank() ?: true) }
    }

}