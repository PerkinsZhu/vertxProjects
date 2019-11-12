package com.perkins.excel

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.slf4j.LoggerFactory
import java.io.File

open class BaseService {
    val logger = LoggerFactory.getLogger(this.javaClass)
    val excelTool = ExcelTool()
    var workbook: Workbook? = null
    var keyMap: Map<String, Int>? = null

    //    val basePath = System.getProperty("user.dir")
    val basePath = "D:\\zhupingjing\\testFile\\kaoqin"
    val dataPath = basePath + File.separator + "data"


    fun log(str: String) {
        logger.info(str)
    }

    fun <T> loadFile(file: File, transfer: (Row, Map<String, Int>?) -> T): List<T?> {
        workbook = excelTool.readExcel(file)
        return workbook?.flatMap { sheet ->
            val rowCount = sheet.count()
            if (rowCount > 0) {
                val titleRow = sheet.first()
                keyMap = getKeyMap(titleRow)
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

    protected fun getKeyMap(titleRow: Row?): Map<String, Int>? {
        return titleRow?.let { row ->
            row.mapIndexed { index, cell ->
                val value = excelTool.getOrNull(cell)?.toString()?.replace("\r\n", "")?.replace("\n", "")?.replace(" ", "")
                Pair(value.toString(), index)
            }.associate {
                it
            }
        }
    }

    protected fun getFile(fileName: String): File {
        val file1 = File(dataPath + File.separator + "$fileName.xlsx")
        val file2 = File(dataPath + File.separator + "$fileName.xls")
        return if (file1.exists()) {
            file1
        } else {
            file2
        }
    }
}