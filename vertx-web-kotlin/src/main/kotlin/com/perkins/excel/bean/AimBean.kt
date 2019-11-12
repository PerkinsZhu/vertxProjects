package com.perkins.excel.bean

import com.perkins.excel.ExcelTool
import org.apache.poi.ss.usermodel.Row

class AimBean constructor(
        val no: String? = null,// 编号
        val location: String? = null,//分部
        val branch: String? = null,// 分公司
        val name: String? = null,//姓名
        var idNo: String? = null,//身份证
        var oneDepartment: String? = null,// 一级部门
        var state: String? = null, // 员工状态
        var hireDate: String? = null,//入职日期
        var leaveDate: String? = null,//离职日期
        var workDayNum: String? = null,//应出勤（天）
        var sickDayNum: Int = 0,//病假（天）
        var eventDateNum: Int = 0,//事假（天）
        var paidDateNum: Int = 0,//带薪假期（天）
        var lateOrEarlyDateNum: Int = 0,//迟到/早退（次数）
        var startNoPunchDateNum: Int = 0,//上班忘打卡（次）
        var endNoPunchDateNum: Int = 0,//下班忘打卡（次）
        var lateReturnDateNum: Int = 0,//工作日晚归（天）
        var weekendOvertimeDateNum: Int = 0,//周末加班（天数）
        var legalHolidaysDateNum: Int = 0,//法定节假日加班（天数）
        var tip: String? = null,//备注
        var middleShift: Int = 0,//班次二(反欺诈 50/一天) //TODO 需确认是不是反欺诈的中班排班
        var B1: Int = 0,//班次三（客服F30一天）
        var B2: Int = 0,//客服班B70/天
        var businessTravel: Int = 0,//出差天数
        var mealSupplement: String? = null,//餐补天数
        var absenteeism: Int = 0,//旷工
        var out: Int = 0//外出
) {
    companion object {
        val noStr = "编号"// 编号
        val locationStr = "分部"//分部
        val branchStr = "分公司"// 分公司
        val nameStr = "姓名"//姓名
        val idNoStr = "身份证"//身份证
        val oneDepartmentStr = "一级部门"// 一级部门
        val stateStr = "员工状态"// 员工状态
        val hireDateStr = "入职日期"//入职日期
        val leaveDateStr = "离职日期"//离职日期
        val workDayNumStr = "应出勤（天）"//应出勤（天）
        val sickDayNumStr = "病假（天）"//病假（天）
        val eventDateNumStr = "事假（天）"//事假（天）
        val paidDateNumStr = "带薪假期（天）"//带薪假期（天）
        val lateOrEarlyDateNumStr = "迟到/早退（次数）"//迟到/早退（次数）
        val startNoPunchDateNumStr = "上班忘打卡（次）"//上班忘打卡（次）
        val endNoPunchDateNumStr = "下班忘打卡（次）"//下班忘打卡（次）
        val lateReturnDateNumStr = "工作日晚归（天）"//工作日晚归（天）
        val weekendOvertimeDateNumStr = "周末加班（天数）"//周末加班（天数）
        val legalHolidaysDateNumStr = "法定节假日加班（天数）"//法定节假日加班（天数）
        val tipStr = "备注"//备注
        val middleShiftStr = "班次二(反欺诈 50/一天)"//班次二(反欺诈 50/一天) //TODO 需确认是不是反欺诈的中班排班
        val B1Str = "班次三（客服F30一天）"//班次三（客服F30一天）
        val B2Str = "客服班B70/天"//客服班B70/天
        val businessTravelStr = "出差天数"//出差天数
        val mealSupplementStr = "餐补天数"//餐补天数
        val absenteeismStr = "旷工"//旷工
        val outStr = "外出"//外出

        fun fromRow(row: Row?, keyMap: Map<String, Int>?): AimBean? {
            val excelTool = ExcelTool()
            val getValue: (String) -> String? = { excelTool.getTextValue(it, keyMap, row) }
            val getNum: (String) -> Int = {
                excelTool.getTextValue(it, keyMap, row)?.let { item ->
                    if (!item.isNullOrBlank()) {
                        try {
                            item.toInt()
                        } catch (e: Exception) {
                            0
                        }
                    } else {
                        0
                    }
                } ?: 0
            }

            return if (row == null || keyMap == null) {
                null
            } else {
                AimBean(
                        getValue(noStr),
                        getValue(locationStr),
                        getValue(branchStr),
                        getValue(nameStr),
                        getValue(idNoStr),
                        getValue(oneDepartmentStr),
                        getValue(stateStr),
                        getValue(hireDateStr),
                        getValue(leaveDateStr),
                        getValue(workDayNumStr),
                        getNum(sickDayNumStr),
                        getNum(eventDateNumStr),
                        getNum(paidDateNumStr),
                        getNum(lateOrEarlyDateNumStr),
                        getNum(startNoPunchDateNumStr),
                        getNum(endNoPunchDateNumStr),
                        getNum(lateReturnDateNumStr),
                        getNum(weekendOvertimeDateNumStr),
                        getNum(legalHolidaysDateNumStr),
                        getValue(tipStr),
                        getNum(middleShiftStr),
                        getNum(B1Str),
                        getNum(B2Str),
                        getNum(businessTravelStr),
                        getValue(mealSupplementStr),
                        getNum(absenteeismStr),
                        getNum(outStr)
                )
            }
        }
    }

    override fun toString(): String {
        return "AimBean(no=$no, location=$location, branch=$branch, name=$name, idNo=$idNo, oneDepartment=$oneDepartment, state=$state, hireDate=$hireDate, leaveDate=$leaveDate, workDayNum=$workDayNum, sickDayNum=$sickDayNum, eventDateNum=$eventDateNum, paidDateNum=$paidDateNum, lateOrEarlyDateNum=$lateOrEarlyDateNum, startNoPunchDateNum=$startNoPunchDateNum, endNoPunchDateNum=$endNoPunchDateNum, lateReturnDateNum=$lateReturnDateNum, weekendOvertimeDateNum=$weekendOvertimeDateNum, legalHolidaysDateNum=$legalHolidaysDateNum, tip=$tip, middleShift=$middleShift, B1=$B1, B2=$B2, businessTravel=$businessTravel, mealSupplement=$mealSupplement, absenteeism=$absenteeism, out=$out)"
    }


}