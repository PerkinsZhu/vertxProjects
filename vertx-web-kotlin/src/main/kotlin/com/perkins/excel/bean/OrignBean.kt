package com.perkins.excel.bean

import com.perkins.excel.ExcelTool
import org.apache.poi.ss.usermodel.Row

class OrignBean constructor(
        var attendanceNumber: String? = null, //考勤号码
        val name: String? = null, //姓名
        val dateStr: String? = null, //日期字符串. yyyy/MM/dd
        val timeInterval: String? = null,//对应时段
        val workStartTime: String? = null, // 上班时间
        val workEndTime: String? = null, //下班时间
        val signInTime: String? = null, //签到时间
        val signOutTime: String? = null,//签退时间
        val attendanceTime: String? = null,//出勤时间
        val department: String? = null,//部门
        val floor: String? = null//楼层
) {


    companion object {
        val attendanceNumberStr = "考勤号码"
        val nameStr = "姓名"
        val dateStrStr = "日期"
        val timeIntervalStr = "对应时段"
        val workStartTimeStr = "上班时间"
        val workEndTimeStr = "下班时间"
        val signInTimeStr = "签到时间"
        val signOutTimeStr = "签退时间"
        val attendanceTimeStr = "出勤时间"
        val departmentStr = "部门"
        val floorStr = "楼层"

        fun fromRow(row: Row?, keyMap: Map<String, Int>?): OrignBean? {
            val excelTool = ExcelTool()
            val getValue: (String) -> String? = { str ->
                keyMap?.get(str)?.let { index ->
                    excelTool.getOrNull(row?.getCell(index))?.toString()
                }
            }
            return if (row == null || keyMap == null) {
                null
            } else {
                OrignBean(
                        getValue(attendanceNumberStr),
                        getValue(nameStr),
                        getValue(dateStrStr),
                        getValue(timeIntervalStr),
                        getValue(workStartTimeStr),
                        getValue(workEndTimeStr),
                        getValue(signInTimeStr),
                        getValue(signOutTimeStr),
                        getValue(attendanceTimeStr),
                        getValue(departmentStr),
                        getValue(floorStr)
                )

            }
        }
    }

    override fun toString(): String {
        return "OrignBean(attendanceNumber=$attendanceNumber, name=$name, dateStr=$dateStr, timeInterval=$timeInterval, workStartTime=$workStartTime, workEndTime=$workEndTime, signInTime=$signInTime, signOutTime=$signOutTime, attendanceTime=$attendanceTime, department=$department, floor=$floor)"
    }
}
