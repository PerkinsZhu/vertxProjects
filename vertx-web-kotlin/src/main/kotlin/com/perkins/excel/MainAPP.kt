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


class MainAPP:BaseService() {
    val path = "F:\\work\\360\\execlParse\\data\\最后需要的表-20190916-20191015淇毓考勤ALL (10.23).xlsx"
    val orignPath = "F:\\work\\360\\execlParse\\data\\原打卡表\\orign.xlsx"
    val vacationPath = "F:\\work\\360\\execlParse\\data\\vacation.xls"
    val orignService = OrignService()
    val vacationService = VacationService()
    @Test
    fun main() {
        val file = File(vacationPath)
//        orignService.loadFile(file).forEach { log(it.toString()) }
        vacationService.loadFile(file).forEach { log(it.toString()) }
    }


}