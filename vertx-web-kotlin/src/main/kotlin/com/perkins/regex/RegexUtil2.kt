package com.perkins.regex

import java.util.regex.Pattern

class RegexUtil2 {

    // 查询文本内容中的移动手机号
    fun mobilePhoneRegex(data: String, action: (matchStr: String) -> Unit): RegexUtil2 {
        doRegex(RegexUtil.mobilePhone, data, action)
        return this
    }

    // 查询文本内容中的座机号
    fun phoneRegex(data: String, action: (matchStr: String) -> Unit): RegexUtil2 {
        doRegex(RegexUtil.phone, data, action)
        return this
    }

    // 查询文本内容中的邮箱
    fun mailRegex(data: String, action: (matchStr: String) -> Unit): RegexUtil2 {
        doRegex(RegexUtil.mail, data, action)
        return this
    }

    /**
     * 匹配身份账号
     * @param type 身份证号长度，15:15位长度身份证号，否则为18位身份证号长度
     */
    fun IDRegex(data: String, action: (matchStr: String) -> Unit, type: Int): RegexUtil2 {
        val id = when (type) {
            15 -> RegexUtil.id_15
            else -> RegexUtil.id_18
        }
        doRegex(id, data, action)
        return this
    }

    // 执行匹配任务
    private fun doRegex(regex: String, data: String, action: (matchStr: String) -> Unit) {
        val matcher = Pattern.compile(regex).matcher(data)
        while (matcher.find()) {
            action(matcher.group())
        }
    }
}