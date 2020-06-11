package com.icapps.niddler.lib.debugger.model.rewrite

import com.icapps.niddler.lib.debugger.model.configuration.BaseDebuggerConfiguration
import com.icapps.niddler.lib.debugger.model.configuration.DebuggerLocationMatch
import java.util.regex.Pattern

enum class RewriteType(val charlesCode: Int) {
    ADD_HEADER(1),
    MODIFY_HEADER(3),
    REMOVE_HEADER(2),
    HOST(4),
    PATH(5),
    URL(6),
    ADD_QUERY_PARAM(8),
    MODIFY_QUERY_PARAM(9),
    REMOVE_QUERY_PARAM(10),
    RESPONSE_STATUS(11),
    BODY(7);

    companion object {
        fun fromCharlesCode(code: Int): RewriteType? {
            return RewriteType.values().find { it.charlesCode == code }
        }
    }
}

enum class ReplaceType(val charlesCode: Int) {
    REPLACE_ALL(2),
    REPLACE_FIRST(1);

    companion object {
        fun fromCharlesCode(code: Int): ReplaceType? {
            return ReplaceType.values().find { it.charlesCode == code }
        }
    }
}

data class RewriteSet(override val active: Boolean,
                      override val name: String,
                      override val locations: List<DebuggerLocationMatch>,
                      val rules: List<RewriteRule>,
                      @Transient override val id: String) : BaseDebuggerConfiguration {

    fun matchesUrl(url: String): Boolean {
        return locations.any { Regex(it.location.asRegex()).matches(url) }
    }
}

data class RewriteRule(val active: Boolean,
                       val ruleType: RewriteType,
                       val matchHeaderRegex: Boolean,
                       val matchValueRegex: Boolean,
                       val matchRequest: Boolean,
                       val matchResponse: Boolean,
                       val newHeaderRegex: Boolean,
                       val newValueRegex: Boolean,
                       val matchWholeValue: Boolean,
                       val caseSensitive: Boolean,
                       val replaceType: ReplaceType,
                       val matchHeader: String?,
                       val matchValue: String?,
                       val newHeader: String?,
                       val newValue: String?) {

    fun actionAsString(): String {
        return buildString {
            var needsArrow = false
            if (!matchHeader.isNullOrBlank()) {
                append(matchHeader).append(':')
                needsArrow = true
            }
            if (!matchValue.isNullOrBlank()) {
                append(matchValue)
                needsArrow = true
            }
            if (needsArrow) append(" -> ")

            if (!newHeader.isNullOrBlank()) append(newHeader).append(':')
            if (!newValue.isNullOrBlank()) append(newValue)
        }
    }
}

private fun escapeSafeRegex(value: String): String {
    return Pattern.compile("[{}()\\[\\].+*?^$\\\\|]").matcher(value).replaceAll("\\\\$0")
}