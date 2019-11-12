package com.perkins.excel

import com.perkins.excel.bean.AimBean
import com.perkins.excel.bean.OrignBean
import com.perkins.excel.bean.VacationBean
import org.apache.poi.ss.usermodel.Row
import java.io.File

class VacationService : BaseService() {
    private lateinit var list: List<VacationBean?>;
    private lateinit var map: Map<String?, List<VacationBean?>>

    fun loadFile(file: File): List<VacationBean?> {
        val transfer = { row: Row, keyMap: Map<String, Int>? ->
            VacationBean.fromRow(row, keyMap)
        }
        list = super.loadFile(file, transfer)?.filter { !(it?.name?.isNullOrBlank() ?: true) }
        map = list.groupBy { it -> it?.let { it.department + "-" + it.name } }
        return list
    }

    fun vacation(aimBean: AimBean, dateStr: String?): VacationBean? {
        return map[aimBean.oneDepartment + "-" + aimBean.name]?.first { it?.startTimeStr == dateStr }
    }

}