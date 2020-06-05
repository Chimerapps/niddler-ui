package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import java.util.regex.Pattern

class ModifyStatusAction(rule: RewriteRule, private val wrongValueCallback: (newValue: String) -> Unit) : BaseValueMatcher(rule), ResponseAction {

    private companion object {
        private val STATUS_CODE_REGEX = Pattern.compile("(\\d+)\\s+(.*)")
    }

    override fun apply(debugResponse: DebugResponse): DebugResponse {
        if (!rule.matchResponse) return debugResponse

        val status = "${debugResponse.code} ${debugResponse.message}"
        val newStatus = createReplacement(status)

        val matcher = STATUS_CODE_REGEX.matcher(newStatus)
        if (!matcher.matches()) {
            wrongValueCallback(newStatus)
            return debugResponse
        }

        val code = matcher.group(1).toInt()
        val message = matcher.group(2).trim()

        return debugResponse.copy(code = code, message = message)
    }

}