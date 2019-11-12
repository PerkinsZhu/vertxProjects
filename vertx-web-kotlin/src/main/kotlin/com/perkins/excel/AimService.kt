package com.perkins.excel

import com.perkins.common.PropertiesUtil
import com.perkins.excel.bean.AimBean
import com.perkins.excel.bean.OrignBean
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.streams.toList

class AimService : BaseService() {
    val path = "F:\\work\\360\\execlParse\\data\\最后需要的表-20190916-20191015淇毓考勤ALL (10.23).xlsx"
    //    val orignPath = "D:\\zhupingjing\\testFile\\kaoqin\\data\\source\\wework.xls"
    val orignPath = "D:\\zhupingjing\\testFile\\kaoqin\\data\\source\\wework.xls"
    val vacationPath = "D:\\zhupingjing\\testFile\\kaoqin\\data\\vacation.xls"


    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd");
    val nameFormat = SimpleDateFormat("yyyyMMddHHmmss");
    val startDate = simpleDateFormat.parse(PropertiesUtil.get("startDate"))
    val endDate = simpleDateFormat.parse(PropertiesUtil.get("endDate"))
    val specialList = mutableListOf<String>("创新业务部门", "贷后管理部", "反欺诈部", "IB客服部")
    val orignService = OrignService()
    val vacationService = VacationService()
    val scheduleService = ScheduleService()
    val userMappingService = UserMappingService()
    lateinit var orignBeanMap: Map<String?, List<OrignBean?>>
    lateinit var userMapping: Map<String, String?>


    fun doTask() {
        logger.info("dataPath:$dataPath")
        userMapping = userMappingService.loadFile()
        val list = loadFile()
        logger.info("aimBeanSize:${list.size}")
        val newList = updateAimBean(list)
        (dataPath + File.separator + "result-${nameFormat.format(Date())}.xlsx").also {
            writeToFile(newList, it)
        }
    }


    private fun loadFile(): List<AimBean?> {
        val file = getFile("result")
        return if (file.exists()) {
            vacationService.loadFile(File(vacationPath))
            orignBeanMap = orignService.loadFile(File(orignPath))
            logger.info("orignBeanSize:${orignBeanMap.size}")
            val transfer = { row: Row, keyMap: Map<String, Int>? ->
                AimBean.fromRow(row, keyMap)
            }
            loadFile(file, transfer)?.filter { !(it?.name?.isNullOrBlank() ?: true) }
        } else {
            logger.error("result.xls 文件不存在，系统退出")
            System.exit(-1)
            mutableListOf()
        }
    }

    private fun updateAimBean(list: List<AimBean?>): List<AimBean?> {
        return list.parallelStream().map {
            it?.let { aimBean ->
                val origiName = userMapping[aimBean.oneDepartment + "-" + aimBean.name] ?: aimBean.name
                val recordList = orignBeanMap[origiName]
                if (recordList == null) {
                    logger.error("未找到用户(${aimBean.name})的打卡数据")
                } else {
                    logger.info(aimBean.toString())
                    logger.info("${aimBean.name},dataSize : ${recordList.size}")
                    doUpdate(recordList, aimBean)
                }
                aimBean
            }
        }.toList()
    }

    private fun doUpdate(recordList: List<OrignBean?>, aimBean: AimBean) {
        val orignBeanMap = recordList.associateBy { it?.dateStr }
        val end = Calendar.getInstance()
        end.time = endDate
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        var n = 0
        while (calendar <= end) {
            val dataStr = simpleDateFormat.format(calendar.time)
            val orign = orignBeanMap[dataStr]
            when {
                specialList.contains(orign?.department) -> {
                    //TODO 处理四个部门的数据
                }
                else -> updateCommonUser(aimBean, orign)
            }
            if (n > 31) break
            calendar.add(Calendar.DATE, 1)
        }
    }

    private fun updateCommonUser(aimBean: AimBean, orign: OrignBean?) {
        orign?.let { orignBean ->
            val vacationBean = vacationService.vacation(aimBean, orignBean.dateStr)
            if (vacationBean?.isSick() == true) {
                aimBean.sickDayNum += 1 //请病假
            }
            if (vacationBean?.isThing() == true) {
                aimBean.eventDateNum += 1 //请事假
            }
            if (vacationBean?.isPay() == true) {
                aimBean.paidDateNum += 1 //带薪年假
            }
            if (orignBean.isLateOrEarly()) { //是否为迟到、早退
                aimBean.lateOrEarlyDateNum += 1
            }
            if (orignBean.signInTime.isNullOrBlank() && vacationBean?.isForenoonVacation(orignBean.dateStr) != true) { //上班忘打卡，且上午不再请假范围内
                aimBean.startNoPunchDateNum += 1
            }
            if (orignBean.signOutTime.isNullOrBlank() && vacationBean?.isAfternoonVacation(orignBean.dateStr) != true) { //下班忘打卡，且下午不再请假范围内
                aimBean.endNoPunchDateNum += 1
            }
            if (orignBean?.isLateReturn()) { // 是否为晚归
                aimBean.lateReturnDateNum += 1
            }
            if (orignBean?.isWeekendOvertime()) { // 是否为周末加班
                aimBean.weekendOvertimeDateNum += 1
            }
            if (orignBean?.isLegalHolidays()) { // 是否为法定节假日
                aimBean.legalHolidaysDateNum += 1
            }
            if (vacationBean?.isBusinessTravel() == true) {
                aimBean.businessTravel += 1 //出差
            }
            if (vacationBean?.isOut() == true) {
                aimBean.out += 1 //外出
            }
        }
    }

    private fun writeToFile(list: List<AimBean?>, newPath: String) {
        list.associateBy {
            it?.let { aim ->
                aim.oneDepartment + "-" + aim.name
            }
        }.let { map ->
            workbook?.first()?.forEach { row ->
                val name = keyMap?.get(AimBean.nameStr)?.let { index ->
                    row.getCell(index).stringCellValue
                }
                val oneDepartment = keyMap?.get(AimBean.oneDepartmentStr)?.let { index ->
                    row.getCell(index).stringCellValue
                }
                val key = "$oneDepartment-$name"
                map[key]?.let { aimBean ->
                    updateRow(row, aimBean)
                }
            }
        }
        excelTool.save(newPath, workbook)
    }

    private fun updateRow(row: Row, aimBean: AimBean) {
        setCell(row, AimBean.sickDayNumStr, aimBean.sickDayNum.toDouble())
        setCell(row, AimBean.eventDateNumStr, aimBean.eventDateNum.toDouble())
        setCell(row, AimBean.paidDateNumStr, aimBean.paidDateNum.toDouble())
        setCell(row, AimBean.lateOrEarlyDateNumStr, aimBean.lateOrEarlyDateNum.toDouble())
        setCell(row, AimBean.startNoPunchDateNumStr, aimBean.startNoPunchDateNum.toDouble())
        setCell(row, AimBean.endNoPunchDateNumStr, aimBean.endNoPunchDateNum.toDouble())
        setCell(row, AimBean.lateReturnDateNumStr, aimBean.lateReturnDateNum.toDouble())
        setCell(row, AimBean.weekendOvertimeDateNumStr, aimBean.weekendOvertimeDateNum.toDouble())
        setCell(row, AimBean.legalHolidaysDateNumStr, aimBean.legalHolidaysDateNum.toDouble())
        setCell(row, AimBean.middleShiftStr, aimBean.middleShift.toDouble())
        setCell(row, AimBean.B1Str, aimBean.B1.toDouble())
        setCell(row, AimBean.B2Str, aimBean.B2.toDouble())
        setCell(row, AimBean.businessTravelStr, aimBean.businessTravel.toDouble())
        setCell(row, AimBean.absenteeismStr, aimBean.absenteeism.toDouble())
        setCell(row, AimBean.outStr, aimBean.out.toDouble())
    }

    val setCell: (Row, String, Double) -> Unit = { row, key, value ->
        keyMap?.get(key)?.let { index ->
            row.getCell(index)?.setCellValue(value)
        }
    }
}