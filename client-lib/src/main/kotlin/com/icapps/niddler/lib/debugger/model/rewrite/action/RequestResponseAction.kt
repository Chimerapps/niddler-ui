package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse

interface RequestAction {
    fun apply(debugRequest: DebugRequest): DebugRequest
}

interface ResponseAction {
    fun apply(debugResponse: DebugResponse): DebugResponse
}