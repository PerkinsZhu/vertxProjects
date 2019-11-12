package com.perkins.excel.bean

import com.perkins.excel.ExcelTool
import org.apache.poi.ss.usermodel.Row

class UserMapping constructor(
        var name: String? = null,//真实姓名
        var oneDepartment: String? = null,//一级部门
        var nickName: String? = null//打卡姓名
) {
    companion object {
        val nameStr = "真实姓名"
        val oneDepartmentStr = "一级部门"
        val nickNameStr = "打卡姓名"

        fun fromRow(row: Row?, keyMap: Map<String, Int>?): UserMapping? {
            val excelTool = ExcelTool()
            val getValue: (String) -> String? = { excelTool.getTextValue(it, keyMap, row) }
            return if (row == null || keyMap == null) {
                null
            } else {
                UserMapping(
                        getValue(nameStr),
                        getValue(oneDepartmentStr),
                        getValue(nickNameStr)
                )

            }
        }
    }

}