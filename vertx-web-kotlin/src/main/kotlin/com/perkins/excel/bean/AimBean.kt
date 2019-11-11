package com.perkins.excel.bean

import com.perkins.excel.ExcelTool
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFColor

class AimBean constructor(
        val no: String? = null,// 编号
        val location: String? = null,//分部
        val branch: String? = null,// 分公司
        val name: String? = null,//姓名
        val idNo: String? = null,//身份证
        val oneDepartment: String? = null,// 一级部门
        val state: String? = null, // 员工状态
        val hireDate: String? = null,//入职日期
        val leaveDate: String? = null,//离职日期
        val workDayNum: String? = null,//应出勤（天）
        val sickDayNum: String? = null,//病假（天）
        val eventDateNum: String? = null,//事假（天）
        val paidDateNum: String? = null,//带薪假期（天）
        val lateOrEarlyDateNum: String? = null,//迟到/早退（次数）
        val startNoPunchDateNum: String? = null,//上班忘打卡（次）
        val endNoPunchDateNum: String? = null,//下班忘打卡（次）
        val lateReturnDateNum: String? = null,//工作日晚归（天）
        val weekendOvertimeDateNum: String? = null,//周末加班（天数）
        val legalHolidaysDateNum: String? = null,//法定节假日加班（天数）
        val tip: String? = null,//备注
        val middleShift: String? = null,//班次二(反欺诈 50/一天) //TODO 需确认是不是反欺诈的中班排班
        val B1: String? = null,//班次三（客服F30一天）
        val B2: String? = null,//客服班B70/天
        val businessTravel: String? = null,//出差天数
        val mealSupplement: String? = null,//餐补天数
        val absenteeism: String? = null,//旷工
        val out: String? = null//外出
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
            val getValue: (String) -> String? = { str ->
                keyMap?.get(str)?.let { index ->

                    row?.getCell(index)?.cellStyle?.let {style ->
                        println(style.fillPattern)
                        style.fillBackgroundColorColor?.let{color->
                            val index = when(color){
                                is XSSFColor -> color.index
                                is HSSFColor -> color.index
                                else -> 64
                            }
                            println("color ->$index")
                        }
                        style.fillBackgroundColor?.let{index->
//                            println("color ->${IndexedColors.fromInt(index.toInt())}")
                            println("color ->$index")
                        }
                    }
                    excelTool.getOrNull(row?.getCell(index))?.toString()?.replace("\r\n", "")?.replace("\n", "")
                }
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
                        getValue(sickDayNumStr),
                        getValue(eventDateNumStr),
                        getValue(paidDateNumStr),
                        getValue(lateOrEarlyDateNumStr),
                        getValue(startNoPunchDateNumStr),
                        getValue(endNoPunchDateNumStr),
                        getValue(lateReturnDateNumStr),
                        getValue(weekendOvertimeDateNumStr),
                        getValue(legalHolidaysDateNumStr),
                        getValue(tipStr),
                        getValue(middleShiftStr),
                        getValue(B1Str),
                        getValue(B2Str),
                        getValue(businessTravelStr),
                        getValue(mealSupplementStr),
                        getValue(absenteeismStr),
                        getValue(outStr)
                )
            }
        }
    }

    override fun toString(): String {
        return "AimBean(no=$no, location=$location, branch=$branch, name=$name, idNo=$idNo, oneDepartment=$oneDepartment, state=$state, hireDate=$hireDate, leaveDate=$leaveDate, workDayNum=$workDayNum, sickDayNum=$sickDayNum, eventDateNum=$eventDateNum, paidDateNum=$paidDateNum, lateOrEarlyDateNum=$lateOrEarlyDateNum, startNoPunchDateNum=$startNoPunchDateNum, endNoPunchDateNum=$endNoPunchDateNum, lateReturnDateNum=$lateReturnDateNum, weekendOvertimeDateNum=$weekendOvertimeDateNum, legalHolidaysDateNum=$legalHolidaysDateNum, tip=$tip, middleShift=$middleShift, B1=$B1, B2=$B2, businessTravel=$businessTravel, mealSupplement=$mealSupplement, absenteeism=$absenteeism, out=$out)"
    }


}