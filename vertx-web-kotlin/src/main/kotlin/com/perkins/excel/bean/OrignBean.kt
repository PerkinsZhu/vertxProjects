package com.perkins.excel.bean

import com.perkins.common.PropertiesUtil
import com.perkins.excel.ExcelTool
import org.apache.poi.ss.usermodel.Row
import java.text.SimpleDateFormat
import java.util.*

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

        val legalHolidays = PropertiesUtil.get("legalHolidays").split(";")

        fun fromRow(row: Row?, keyMap: Map<String, Int>?): OrignBean? {
            val excelTool = ExcelTool()
            val getValue: (String) -> String? = { excelTool.getTextValue(it, keyMap, row) }
            return if (row == null || keyMap == null) {
                null
            } else {
                OrignBean(
                        getValue(attendanceNumberStr),
                        getValue(nameStr),
                        formatDateStr(getValue(dateStrStr)),
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

        private fun formatDateStr(value: String?): String? {
            return value?.let { str ->
                val year = str.substringBefore("/")
                val month = str.substringAfter("/").substringBefore("/")
                val day = str.substringAfterLast("/")
                return "$year/" + (if (month.length == 1) {
                    "0$month"
                } else {
                    month
                }) + "/" + (if (day.length == 1) {
                    "0$day"
                } else {
                    day
                })
            }
        }
    }

    override fun toString(): String {
        return "OrignBean(attendanceNumber=$attendanceNumber, name=$name, dateStr=$dateStr, timeInterval=$timeInterval, workStartTime=$workStartTime, workEndTime=$workEndTime, signInTime=$signInTime, signOutTime=$signOutTime, attendanceTime=$attendanceTime, department=$department, floor=$floor)"
    }

    val dataFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
    val sdf = SimpleDateFormat("yyyy/MM/dd")

    fun isLateOrEarly(): Boolean {
        // 10:00之后上班为迟到
        // 18:00之前下班为早退
        return !isWeekend() && this.signInTime?.let { it.maxThan("10:00") } == true || this.signOutTime?.let { !(it.maxThan("18:00") && it != "18:00") } == true
    }

    //是否为晚归
    fun isLateReturn(): Boolean {
        //TODO 周末要考虑晚归吗？
        return this.signOutTime?.maxThan("22:00") ?: false
    }

    //时间比较大小
    private fun String.maxThan(aim: String): Boolean {
        return this.let {
            if (it.isNullOrBlank()) {
                false
            } else {
                val c1 = Calendar.getInstance()
                c1.time = dataFormat.parse("2019/09/02 $it")
                val c2 = Calendar.getInstance()
                c2.time = dataFormat.parse("2019/09/02 $aim")
                c1 > c2
            }
        }
    }

    //是否是周末
    fun isWeekend(): Boolean {
        return this.dateStr?.let {
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(it)
            (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
        } == true
    }

    fun isWeekendOvertime(): Boolean {
        //FIXME 什么情况下算是周末加班？只要有打卡记录就算是吗？
        return isWeekend() && false
    }

    //是否为法定节假日
    fun isLegalHolidays(): Boolean {
        //FIXME 什么情况才算是法定节假日加班？
        return legalHolidays.contains(this.dateStr) && false
    }
}
