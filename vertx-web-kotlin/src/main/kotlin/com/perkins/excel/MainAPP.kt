package com.perkins.excel

import com.perkins.kotlintest.log
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Test
import org.slf4j.LoggerFactory
import org.apache.poi.ss.util.CellUtil.getRow
import java.io.*


class MainAPP : BaseService() {

    val orignService = OrignService()
    val vacationService = VacationService()
    val aimService = AimService()
    val scheduleService = ScheduleService()
    @Test
    fun main() {
//        val file = File(aimPath)
        //orignService.loadFile(file).forEach { log(it.toString()) }
        //vacationService.loadFile(file).forEach { log(it.toString()) }
//        val list = aimService.loadFile(file)
//        val list = scheduleService.loadFile(file)
//        list.forEach { log(it.toString()) }
        /*log(list.size.toString())
        list.forEach {
            logger.info(it.toString())
        }
        */
        aimService.doTask()

    }
}