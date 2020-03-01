package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import java.util.regex.Pattern

open class BaseValueMatcher(protected val rule: RewriteRule) {

    protected fun createReplacement(originalValue: String): String {
        val toMatch = rule.matchValue?.trim()
        val replacement = rule.newValue?.trim()
        if (replacement.isNullOrEmpty()) return originalValue

        when {
            toMatch == null -> return replacement
            rule.matchValueRegex -> {
                val regex = toMatch.toPattern(flags = if (!rule.caseSensitive) Pattern.CASE_INSENSITIVE else 0)
                val matcher = regex.matcher(originalValue)
                if (rule.replaceType == ReplaceType.REPLACE_ALL) {
                    if (matcher.matches()) {
                        var actualReplacement: String = replacement
                        for (i in 1..matcher.groupCount()) {
                            actualReplacement = actualReplacement.replace("\$$i", matcher.group(i), ignoreCase = !rule.caseSensitive)
                        }
                        return actualReplacement
                    }
                } else {
                    if (matcher.find()) {
                        var actualReplacement: String = replacement
                        for (i in 1..matcher.groupCount()) {
                            actualReplacement = actualReplacement.replace("\$$i", matcher.group(i), ignoreCase = !rule.caseSensitive)
                        }
                        return actualReplacement
                    }
                }
            }
            rule.matchWholeValue -> return if (originalValue.equals(toMatch, ignoreCase = !rule.caseSensitive)) replacement else originalValue
            originalValue.contains(toMatch, ignoreCase = !rule.caseSensitive) ->
                return if (rule.replaceType == ReplaceType.REPLACE_ALL)
                    replacement
                else
                    originalValue.replaceFirst(toMatch, replacement, ignoreCase = !rule.caseSensitive)
        }
        return originalValue
    }

}