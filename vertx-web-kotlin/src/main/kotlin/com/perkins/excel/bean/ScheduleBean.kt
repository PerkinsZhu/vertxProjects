package com.perkins.excel.bean

import com.perkins.excel.ExcelTool
import org.apache.poi.ss.usermodel.Row

class ScheduleBean constructor(
        val name: String? = null,//姓名
        val data: Map<String, String?>? = null//排班状态
        //工作状态，包括：1、0.5、旷工、离职、事假、病假、加班、年假、产检假、中班、补班、
        // 换30休息、换徐圆中班、换班、调休、与9月5号换班、婚假、调休下午半天、10月补、半天年假、调班
) {
    companion object {
        val nameStr = "姓名"
        fun fromRow(row: Row?, keyMap: Map<String, Int>?): ScheduleBean? {
            val excelTool = ExcelTool()
            val getValue: (String) -> String? = { excelTool.getTextValue(it, keyMap, row) }
            return if (row == null || keyMap == null) {
                null
            } else {
                val data = keyMap.filter { it.key != nameStr && !it.key.isNullOrBlank() }.map { entry ->
                    Pair(entry.key, getValue(entry.key))
                }.associate { it }
                ScheduleBean(
                        getValue(nameStr),
                        data
                )

            }
        }
    }

    override fun toString(): String {
        return "ScheduleBean(name=$name, data=$data)"
    }

}