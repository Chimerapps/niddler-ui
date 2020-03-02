package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule

interface BaseModifyParameterAction {

    //TODO regex

    fun modifyMap(rule: RewriteRule, match: MapMatchResult, map: Map<String, List<String>>?): Map<String, List<String>>? {
        var newHeader = rule.newHeader?.toLowerCase()

        val matchedHeader = match.matchedHeader ?: newHeader ?: return map
        if (newHeader == null) newHeader = matchedHeader

        val matchedValue = match.matchedValue ?: rule.newValue ?: return map
        if (map == null) {
            return mapOf(matchedHeader to listOf(matchedValue))
        }

        val mutableHeaders = map.toMutableMap()
        if (matchedHeader != newHeader) {
            mutableHeaders[newHeader] = mutableHeaders.remove(matchedHeader) ?: return map
        }

        val originalValues = mutableHeaders[newHeader]?.toMutableList() ?: mutableListOf()

        if (rule.replaceType == ReplaceType.REPLACE_ALL) {
            if (rule.newValue != null) {
                originalValues.clear()
                originalValues.add(rule.newValue)
            }
        } else if (rule.newValue != null) {
            originalValues.remove(matchedValue)
            originalValues.add(rule.newValue)
        }
        mutableHeaders[newHeader] = originalValues

        return mutableHeaders
    }

}