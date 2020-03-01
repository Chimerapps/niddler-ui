package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import java.util.regex.Pattern

class ModifyUrlAction(private val rule: RewriteRule) {

    init {
        if (rule.ruleType != RewriteType.URL) throw IllegalArgumentException("Rule is not a modify url rule")
    }

    fun apply(debugRequest: DebugRequest): DebugRequest {
        val url = debugRequest.url
        val toMatch = rule.matchValue?.trim()
        val replacement = rule.newValue?.trim()
        if (replacement.isNullOrEmpty()) return debugRequest

        when {
            toMatch == null -> return debugRequest.copy(url = replacement)
            rule.matchValueRegex -> {
                val regex = toMatch.toPattern(flags = if (!rule.caseSensitive) Pattern.CASE_INSENSITIVE else 0)
                val matcher = regex.matcher(url)
                if (rule.replaceType == ReplaceType.REPLACE_ALL) {
                    if (matcher.matches()) {
                        var actualReplacement: String = replacement
                        for (i in 1..matcher.groupCount()) {
                            actualReplacement = actualReplacement.replace("\$$i", matcher.group(i), ignoreCase = !rule.caseSensitive)
                        }
                        return debugRequest.copy(url = actualReplacement)
                    }
                } else {
                    if (matcher.find()) {
                        var actualReplacement: String = replacement
                        for (i in 1..matcher.groupCount()) {
                            actualReplacement = actualReplacement.replace("\$$i", matcher.group(i), ignoreCase = !rule.caseSensitive)
                        }
                        return debugRequest.copy(url = actualReplacement)
                    }
                }
            }
            rule.matchWholeValue -> return if (url.equals(toMatch, ignoreCase = !rule.caseSensitive)) debugRequest.copy(url = replacement) else debugRequest
            url.contains(toMatch, ignoreCase = !rule.caseSensitive) ->
                return if (rule.replaceType == ReplaceType.REPLACE_ALL)
                    debugRequest.copy(url = replacement)
                else
                    debugRequest.copy(url = url.replaceFirst(toMatch, replacement, ignoreCase = !rule.caseSensitive))
        }

        return debugRequest
    }

}