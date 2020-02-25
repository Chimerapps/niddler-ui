package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType

open class BaseModifyHeaderAction(protected val rule: RewriteRule) {

    fun applies(originalHeaders: Map<String, List<String>>?): AppliesResult {
        val newHeaderKey = rule.newHeader?.toLowerCase()
        val newHeaderValue = rule.newValue
        val matchHeaderKey = rule.matchHeader?.toLowerCase()
        val matchHeaderValue = rule.matchValue

        if (rule.ruleType == RewriteType.ADD_HEADER || rule.ruleType == RewriteType.MODIFY_HEADER) {
            if (newHeaderKey.isNullOrBlank() || newHeaderValue.isNullOrBlank()) return AppliesResult(false)
        } else {
            if (matchHeaderKey.isNullOrBlank() && matchHeaderValue.isNullOrBlank()) return AppliesResult(false)
        }

        val matchedHeaderByKey = if (!matchHeaderKey.isNullOrBlank()) {
            if (originalHeaders == null || originalHeaders.isEmpty()) return AppliesResult(matches = false)

            if (!rule.matchHeaderRegex) {
                matchHeaderKey to (originalHeaders[matchHeaderKey] ?: return AppliesResult(matches = false))
            } else {
                val key = originalHeaders.keys.find { it.matches(Regex(matchHeaderKey)) } ?: return AppliesResult(matches = false)
                key to originalHeaders.getValue(key)
            }
        } else null

        //No need to check further
        if (matchHeaderValue.isNullOrBlank()) return AppliesResult(matches = true, matchedHeader = matchedHeaderByKey?.first)
        if (originalHeaders == null || originalHeaders.isEmpty()) return AppliesResult(false)

        if (matchedHeaderByKey != null) {
            return valueMatches(matchHeaderValue, matchedHeaderByKey.second, matchedHeaderByKey.first) ?: return AppliesResult(false)
        }
        originalHeaders.entries.forEach { (key, value) ->
            valueMatches(matchHeaderValue, value, key)?.let { return it }
        }
        return AppliesResult(false)
    }

    private fun valueMatches(matchHeaderValue: String, second: List<String>, key: String): AppliesResult? {
        if (second.isEmpty()) return null

        if (!rule.matchValueRegex) {
            if (rule.matchWholeValue) {
                if (second.size > 1) return null
                return AppliesResult(matchHeaderValue.equals(second[0], ignoreCase = !rule.caseSensitive), key, matchHeaderValue)
            } else {
                val matched = second.find { it.contains(matchHeaderValue, ignoreCase = !rule.caseSensitive) }
                return AppliesResult(matched != null, key, matched)
            }
        }
        val regex = if (rule.caseSensitive) Regex(matchHeaderValue) else Regex(matchHeaderValue, RegexOption.IGNORE_CASE)
        if (rule.matchWholeValue) {
            if (second.size > 1) return null
            return AppliesResult(regex.matches(second[0]), key, second[0])
        }

        val matched = second.find { regex.containsMatchIn(it) }
        return AppliesResult(matched != null, key, matched)
    }

}

data class AppliesResult(val matches: Boolean, val matchedHeader: String? = null, val matchedValue: String? = null)