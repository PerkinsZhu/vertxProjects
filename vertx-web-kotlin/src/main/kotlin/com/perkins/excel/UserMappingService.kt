package com.perkins.excel

import com.perkins.excel.bean.UserMapping
import org.apache.poi.ss.usermodel.Row

class UserMappingService : BaseService() {

    fun loadFile(): Map<String, String?> {
        val file = getFile("mapping")
        return if (file.exists()) {
            logger.info("未设置名称映射文件")
            val transfer = { row: Row, keyMap: Map<String, Int>? ->
                UserMapping.fromRow(row, keyMap)
            }
            loadFile(file, transfer).filter {
                !(it?.name?.isNullOrBlank() ?: true)
            }.map { bean ->
                Pair(bean?.oneDepartment + "-" + bean?.name, bean?.name)
            }.associate {
                it
            }
        } else {
            mapOf()
        }
    }
}