package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.rewrite.ReplaceType
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule

interface BaseAddParameterAction {

    fun modifyMap(rule: RewriteRule, newKey: String, newValue: String, matchResult: MapMatchResult, map: Map<String, List<String>>?): Map<String, List<String>> {
        if (!matchResult.matches) return map ?: emptyMap()

        val mutableMap = map?.toMutableMap() ?: mutableMapOf()
        mutableMap[newKey] = if (rule.replaceType == ReplaceType.REPLACE_ALL)
            listOf(newValue)
        else
            (mutableMap[newKey]?.toMutableList() ?: mutableListOf()).also { it.add(newValue) }

        return mutableMap
    }

}