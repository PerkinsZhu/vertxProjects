package com.perkins.excel

import org.apache.poi.ss.usermodel.Row
import org.slf4j.LoggerFactory
import java.io.File

open class BaseService {
    val logger = LoggerFactory.getLogger(this.javaClass)
    val excelTool = ExcelTool()

    fun log(str: String) {
        logger.info(str)
    }

    fun <T> loadFile(file: File, transfer: (Row, Map<String, Int>?) -> T): List<T?> {
        val workbook = excelTool.readExcel(file)
        return workbook?.flatMap { sheet ->
            val rowCount = sheet.count()
            if (rowCount > 0) {
                val coloumNum = sheet.getRow(0).physicalNumberOfCells
                log("-->rowCount:$rowCount")
                log("-->coloumNum:$coloumNum")
                val titleRow = sheet.first()
                val keyMap = getKeyMap(titleRow)
                keyMap?.forEach { log(it.toString()) }
                sheet.mapIndexed { index, row ->
                    if (index > 0) {
                        transfer(row, keyMap)
                    } else {
                        null
                    }
                }
            } else {
                mutableListOf()
            }
        } ?: mutableListOf()
    }

    private fun getKeyMap(titleRow: Row?): Map<String, Int>? {
        return titleRow?.let { row ->
            row.mapIndexed { index, cell ->
                Pair(cell.stringCellValue.replace("\r\n", "").replace("\n", ""), index)
            }.associate {
                it
            }
        }
    }
}