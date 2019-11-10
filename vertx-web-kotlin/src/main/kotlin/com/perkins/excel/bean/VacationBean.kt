package com.perkins.excel.bean

import com.perkins.excel.ExcelTool
import org.apache.poi.ss.usermodel.Row

class VacationBean constructor(
        val no: String? = null, //序号
        val department: String? = null,//部门
        val name: String? = null, //姓名
        val startTimeStr: String? = null, //起始时间
        val endTimeStr: String? = null, //结束时间
        val count: String? = null,// 请假天数
        val type: String? = null,// 请假类型，字符串，文字描述
        val prove: String? = null, //有无证明
        val remarks: String? = null, //备注
        val cause: String? = null //事由
) {


    companion object {
        val noStr = "序号" //序号
        val departmentStr = "部门"//部门
        val nameStr = "姓名"//姓名
        val startTimeStrStr = "起始时间"//起始时间
        val endTimeStrStr = "结束时间"//结束时间
        val countStr = "请假天数"// 请假天数
        val typeStr = "请假类型"// 请假类型，字符串，文字描述
        val proveStr = "有无证明"//有无证明
        val remarksStr = "备注"//备注
        val causeStr = "事由"//事由

        fun fromRow(row: Row?, keyMap: Map<String, Int>?): VacationBean? {
            val excelTool = ExcelTool()
            val getValue: (String) -> String? = { str ->
                keyMap?.get(str)?.let { index ->
                    if(str == startTimeStrStr){
                        println("000")
                    }
                    excelTool.getOrNull(row?.getCell(index))?.toString()
                }
            }
            return if (row == null || keyMap == null) {
                null
            } else {
                VacationBean(
                        getValue(noStr),
                        getValue(departmentStr),
                        getValue(nameStr),
                        getValue(startTimeStrStr),
                        getValue(endTimeStrStr),
                        getValue(countStr),
                        getValue(typeStr),
                        getValue(proveStr),
                        getValue(remarksStr),
                        getValue(causeStr)
                )

            }
        }
    }

    override fun toString(): String {
        return "VacationBean(no=$no, department=$department, name=$name, startTimeStr=$startTimeStr, endTimeStr=$endTimeStr, count=$count, type=$type, prove=$prove, remarks=$remarks, cause=$cause)"
    }

}