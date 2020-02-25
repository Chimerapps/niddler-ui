package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType

open class BaseModifyHeaderAction(protected val rule: RewriteRule) {

    companion object {
        val HAS_REPLACEMENT_REGEX = Regex(".*$\\d+.*")
    }

    fun matches(originalHeaders: Map<String, List<String>>?): HeaderMatchResult {
        val matchHeaderKey = rule.matchHeader?.toLowerCase()
        val matchHeaderValue = rule.matchValue

        return when (rule.ruleType) {
            RewriteType.ADD_HEADER -> matchAddHeader(matchHeaderKey, matchHeaderValue, originalHeaders)
            RewriteType.MODIFY_HEADER -> matchModifyHeader(matchHeaderKey, matchHeaderValue, originalHeaders)
            RewriteType.REMOVE_HEADER -> matchRemoveHeader(matchHeaderKey, matchHeaderValue, originalHeaders)
            else -> throw IllegalArgumentException("Can only be used with modify header rules")
        }
    }

    private fun matchAddHeader(matchHeaderKey: String?, matchHeaderValue: String?, originalHeaders: Map<String, List<String>>?): HeaderMatchResult {
        if (matchHeaderKey.isNullOrBlank() && matchHeaderValue.isNullOrBlank()) {
            return HeaderMatchResult(matches = true)
        } else if (originalHeaders.isNullOrEmpty()) {
            return HeaderMatchResult(matches = false)
        }

        return matchHeaders(matchHeaderKey, matchHeaderValue, originalHeaders)
    }

    private fun matchRemoveHeader(matchHeaderKey: String?, matchHeaderValue: String?, originalHeaders: Map<String, List<String>>?): HeaderMatchResult {
        if ((matchHeaderKey.isNullOrBlank() && matchHeaderValue.isNullOrBlank()) || originalHeaders.isNullOrEmpty()) {
            return HeaderMatchResult(matches = false)
        }

        return matchHeaders(matchHeaderKey, matchHeaderValue, originalHeaders)
    }

    private fun matchModifyHeader(matchHeaderKey: String?, matchHeaderValue: String?, originalHeaders: Map<String, List<String>>?): HeaderMatchResult {
        if ((matchHeaderKey.isNullOrBlank() && matchHeaderValue.isNullOrBlank()) || originalHeaders.isNullOrEmpty()) {
            return HeaderMatchResult(matches = false)
        }

        return matchHeaders(matchHeaderKey, matchHeaderValue, originalHeaders)
    }

    private fun matchHeaders(matchHeaderKey: String?, matchHeaderValue: String?, originalHeaders: Map<String, List<String>>): HeaderMatchResult {
        val matchedHeaderKey = if (matchHeaderKey != null) {
            val result = matchOnHeader(matchHeaderKey, originalHeaders)
            if (!result.matches)
                return result
            result.matchedHeader
        } else null

        if (matchHeaderValue.isNullOrBlank()) {
            if (matchedHeaderKey == null) return HeaderMatchResult(matches = true) //Can't happen due to check in beginning
            return HeaderMatchResult(matches = true, matchedHeader = matchedHeaderKey)
        }

        return matchHeaderValue(matchedHeaderKey, matchHeaderValue, originalHeaders)
    }

    private fun matchHeaderValue(matchedHeaderKey: String?, matchHeaderValue: String, originalHeaders: Map<String, List<String>>): HeaderMatchResult {
        if (matchedHeaderKey != null) {
            return valueMatches(matchHeaderValue, originalHeaders.getValue(matchedHeaderKey), matchedHeaderKey) ?: return HeaderMatchResult(matches = false)
        }
        originalHeaders.entries.forEach { (key, value) ->
            valueMatches(matchHeaderValue, value, key)?.let { return it }
        }
        return HeaderMatchResult(matches = false)
    }

    private fun matchOnHeader(matchHeaderKey: String, originalHeaders: Map<String, List<String>>?): HeaderMatchResult {
        if (originalHeaders == null || originalHeaders.isEmpty()) return HeaderMatchResult(matches = false)

        val key = if (!rule.matchHeaderRegex) {
            matchHeaderKey
        } else {
            originalHeaders.keys.find { it.matches(Regex(matchHeaderKey)) } ?: return HeaderMatchResult(matches = false)
        }
        if (!originalHeaders.containsKey(key)) return HeaderMatchResult(matches = false)

        return HeaderMatchResult(matches = true, matchedHeader = key)
    }

    private fun valueMatches(matchHeaderValue: String, second: List<String>, key: String): HeaderMatchResult? {
        if (second.isEmpty()) return null

        if (!rule.matchValueRegex) {
            if (rule.matchWholeValue) {
                if (second.size > 1) return null
                return HeaderMatchResult(matchHeaderValue.equals(second[0], ignoreCase = !rule.caseSensitive), key, matchHeaderValue)
            } else {
                val matched = second.find { it.contains(matchHeaderValue, ignoreCase = !rule.caseSensitive) }
                return HeaderMatchResult(matched != null, key, matched)
            }
        }
        val regex = if (rule.caseSensitive) Regex(matchHeaderValue) else Regex(matchHeaderValue, RegexOption.IGNORE_CASE)
        if (rule.matchWholeValue) {
            if (second.size > 1) return null
            return HeaderMatchResult(regex.matches(second[0]), key, second[0])
        }

        val matched = second.find { regex.containsMatchIn(it) }
        return HeaderMatchResult(matched != null, key, matched)
    }

}

data class HeaderMatchResult(val matches: Boolean, val matchedHeader: String? = null, val matchedValue: String? = null)