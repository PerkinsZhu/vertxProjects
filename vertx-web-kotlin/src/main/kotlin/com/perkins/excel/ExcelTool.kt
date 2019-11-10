package com.perkins.excel

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import java.io.*
import java.text.SimpleDateFormat
import java.text.DecimalFormat

class ExcelTool {

    val logger = LoggerFactory.getLogger(this.javaClass)

    fun readExcel(file: File): Workbook? {
        var wb: Workbook? = null
        if (file == null) {
            return null
        }
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(file)
            if (file.name.endsWith(".xls")) {
                wb = HSSFWorkbook(inputStream)
            } else if (file.name.endsWith(".xlsx")) {
                wb = XSSFWorkbook(inputStream)
            } else {
                wb = null
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return wb
    }

    fun <T> getOrElse(cell: Cell, default: T): T {
        val a = when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.BOOLEAN -> cell.booleanCellValue
            CellType.BLANK -> ""
            CellType.NUMERIC -> cell.numericCellValue
            else -> {
                logger.info("未知单元格类型：${cell.cellType}")
                cell.toString()
            }
        }

        return try {
            a as T
        } catch (e: Exception) {
            logger.info("不匹配的单元格类型：${cell.cellType},$cell")
            default
        }
    }

    var df = DecimalFormat("0")//格式化number String字符串
    var sdf = SimpleDateFormat("yyyy/MM/dd")//日期格式化

    fun getOrNull(cell: Cell?): Any? {
        return cell?.let { ce ->
            when (cell.cellType) {
                CellType.STRING -> ce.stringCellValue
                CellType.BOOLEAN -> ce.booleanCellValue
                CellType.BLANK -> ""
                CellType.NUMERIC -> {
                    val value = when {
                        DateUtil.isCellDateFormatted(cell) -> sdf.format(cell.dateCellValue)
                        "General" == cell.cellStyle.dataFormatString -> df.format(cell.numericCellValue)
                        else -> cell.richStringCellValue.string
                    }
                    value
                }
                else -> {
                    logger.info("未知单元格类型：${ce.cellType}")
                    ce.toString()
                }
            }
        }
    }

}