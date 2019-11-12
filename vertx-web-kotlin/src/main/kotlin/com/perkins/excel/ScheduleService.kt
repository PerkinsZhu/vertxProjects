package com.perkins.excel

import com.perkins.excel.bean.ScheduleBean
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class ScheduleService : BaseService() {

    fun loadFile(file: File): List<ScheduleBean?> {
        val transfer = { row: Row, keyMap: Map<String, Int>? ->
            ScheduleBean.fromRow(row, keyMap)
        }
        val list = when {
            file.name.startsWith("客服部主管排班表") -> loadCrmFile(file, transfer)
            file.name.startsWith("反欺诈部排班表") -> loadFQZFile(file, transfer)
            file.name.startsWith("创新业务部排班表") -> loadFQZFile(file, transfer)
            file.name.startsWith("贷后管理部排班表") -> loadDHGLFile(file, transfer)
            else -> loadFile(file, transfer)
        }
        return list?.filter { !(it?.name?.isNullOrBlank() ?: true) }
    }

    // 加载CRM 表格
    fun <T> loadCrmFile(file: File, transfer: (Row, Map<String, Int>?) -> T): List<T?> {
        val workbook = excelTool.readExcel(file)
        return workbook?.flatMap { sheet ->
            val rowCount = sheet.count()
            if (rowCount > 0) {
                val titleRow = sheet.getRow(1)
                val keyMap = getKeyMap(titleRow)
                logger.info("keyMap =$keyMap")
                sheet.mapIndexed { index, row ->
                    if (index > 1) {
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

    // 反欺诈排班表
    fun <T> loadFQZFile(file: File, transfer: (Row, Map<String, Int>?) -> T): List<T?> {
        val workbook = excelTool.readExcel(file)
        return workbook?.flatMap { sheet ->
            val rowCount = sheet.count()
            if (rowCount > 0) {
                val titleRow = sheet.getRow(0)
                val keyMap = getKeyMap(titleRow)
                logger.info("keyMap =$keyMap")
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

    var sdf = SimpleDateFormat("yyyy/MM")//日期格式化
    // 贷后管理部排班表
    fun <T> loadDHGLFile(file: File, transfer: (Row, Map<String, Int>?) -> T): List<T?> {
        val workbook = excelTool.readExcel(file)
        return workbook?.flatMap { sheet ->
            val rowCount = sheet.count()
            if (rowCount > 0) {
                val titleRow = sheet.getRow(1)
                val keyMap = getDHGLKeyMap(titleRow)
                logger.info("keyMap =$keyMap")
                sheet.mapIndexed { index, row ->
                    if (index > 1) {
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

    // 贷后管理的keyMap做特殊处理
    private fun getDHGLKeyMap(titleRow: Row?): Map<String, Int>? {
        return getKeyMap(titleRow)?.map { entry ->
            if (entry.key.length < 4 && entry.key.contains("日") && !entry.key.substringBeforeLast("日").trim().isNullOrBlank()) {
                val num = entry.key.substringBeforeLast("日").trim()
                val newKey = if (num.toInt() < 16) { //当前月
                    sdf.format(Date()) + "/$num"
                } else { //上一月
                    val c = Calendar.getInstance()
                    c.time = Date()
                    c.add(Calendar.MONTH, -1)
                    sdf.format(c.time) + "/$num"
                }
                Pair(newKey, entry.value)
            } else {
                Pair(entry.key, entry.value)
            }
        }?.associate { it }
    }
}