package com.perkins.util

import java.io.File

object FileUtil {

    fun createFile(path: String) {
        val file = File(path)
        file.deleteOnExit()
        if (file.exists()) {
            file.createNewFile()
        }
    }

}