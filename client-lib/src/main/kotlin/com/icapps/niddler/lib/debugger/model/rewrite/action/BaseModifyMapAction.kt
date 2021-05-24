package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteType
import java.util.Locale

open class BaseModifyMapAction(protected val rule: RewriteRule) {

    fun matches(originalValues: Map<String, List<String>>?): MapMatchResult {
        val matchHeaderKey = rule.matchHeader?.lowercase(Locale.getDefault())
        val matchHeaderValue = rule.matchValue

        return when (rule.ruleType) {
            RewriteType.ADD_HEADER, RewriteType.ADD_QUERY_PARAM -> matchAddToMap(matchHeaderKey, matchHeaderValue, originalValues)
            RewriteType.MODIFY_HEADER, RewriteType.MODIFY_QUERY_PARAM -> matchModifyMap(matchHeaderKey, matchHeaderValue, originalValues)
            RewriteType.REMOVE_HEADER, RewriteType.REMOVE_QUERY_PARAM -> matchRemoveFromMap(matchHeaderKey, matchHeaderValue, originalValues)
            else -> throw IllegalArgumentException("Can only be used with modify header/query rules")
        }
    }

    private fun matchAddToMap(matchHeaderKey: String?, matchHeaderValue: String?, originalHeaders: Map<String, List<String>>?): MapMatchResult {
        if (matchHeaderKey.isNullOrBlank() && matchHeaderValue.isNullOrBlank()) {
            return MapMatchResult(matches = true)
        } else if (originalHeaders.isNullOrEmpty()) {
            return MapMatchResult(matches = false)
        }

        return matchHeaders(matchHeaderKey, matchHeaderValue, originalHeaders)
    }

    private fun matchRemoveFromMap(matchHeaderKey: String?, matchHeaderValue: String?, originalHeaders: Map<String, List<String>>?): MapMatchResult {
        if ((matchHeaderKey.isNullOrBlank() && matchHeaderValue.isNullOrBlank()) || originalHeaders.isNullOrEmpty()) {
            return MapMatchResult(matches = false)
        }

        return matchHeaders(matchHeaderKey, matchHeaderValue, originalHeaders)
    }

    private fun matchModifyMap(matchHeaderKey: String?, matchHeaderValue: String?, originalHeaders: Map<String, List<String>>?): MapMatchResult {
        if ((matchHeaderKey.isNullOrBlank() && matchHeaderValue.isNullOrBlank()) || originalHeaders.isNullOrEmpty()) {
            return MapMatchResult(matches = false)
        }

        return matchHeaders(matchHeaderKey, matchHeaderValue, originalHeaders)
    }

    private fun matchHeaders(matchHeaderKey: String?, matchHeaderValue: String?, originalHeaders: Map<String, List<String>>): MapMatchResult {
        val matchedHeaderKey = if (matchHeaderKey != null) {
            val result = matchOnHeader(matchHeaderKey, originalHeaders)
            if (!result.matches)
                return result
            result.matchedHeader
        } else null

        if (matchHeaderValue.isNullOrBlank()) {
            if (matchedHeaderKey == null) return MapMatchResult(matches = true) //Can't happen due to check in beginning
            return MapMatchResult(matches = true, matchedHeader = matchedHeaderKey)
        }

        return matchHeaderValue(matchedHeaderKey, matchHeaderValue, originalHeaders)
    }

    private fun matchHeaderValue(matchedHeaderKey: String?, matchHeaderValue: String, originalHeaders: Map<String, List<String>>): MapMatchResult {
        if (matchedHeaderKey != null) {
            return valueMatches(matchHeaderValue, originalHeaders.getValue(matchedHeaderKey), matchedHeaderKey) ?: return MapMatchResult(matches = false)
        }
        originalHeaders.entries.forEach { (key, value) ->
            val match = valueMatches(matchHeaderValue, value, key)
            if (match?.matches == true) return match
        }
        return MapMatchResult(matches = false)
    }

    private fun matchOnHeader(matchHeaderKey: String, originalHeaders: Map<String, List<String>>?): MapMatchResult {
        if (originalHeaders == null || originalHeaders.isEmpty()) return MapMatchResult(matches = false)

        val key = if (!rule.matchHeaderRegex) {
            matchHeaderKey
        } else {
            originalHeaders.keys.find { it.matches(Regex(matchHeaderKey)) } ?: return MapMatchResult(matches = false)
        }
        if (!originalHeaders.containsKey(key)) return MapMatchResult(matches = false)

        return MapMatchResult(matches = true, matchedHeader = key)
    }

    private fun valueMatches(matchHeaderValue: String, values: List<String>, key: String): MapMatchResult? {
        if (values.isEmpty()) return null

        if (!rule.matchValueRegex) {
            if (rule.matchWholeValue) {
                if (values.size > 1) return null
                return MapMatchResult(matchHeaderValue.equals(values[0], ignoreCase = !rule.caseSensitive), key, matchHeaderValue)
            } else {
                val matched = values.find { it.contains(matchHeaderValue, ignoreCase = !rule.caseSensitive) }
                return MapMatchResult(matched != null, key, matched)
            }
        }
        val regex = if (rule.caseSensitive) Regex(matchHeaderValue) else Regex(matchHeaderValue, RegexOption.IGNORE_CASE)
        if (rule.matchWholeValue) {
            if (values.size > 1) return null
            return MapMatchResult(regex.matches(values[0]), key, values[0])
        }

        val matched = values.find { regex.containsMatchIn(it) }
        return MapMatchResult(matched != null, key, matched)
    }

}

data class MapMatchResult(val matches: Boolean, val matchedHeader: String? = null, val matchedValue: String? = null)