package com.icapps.niddler.lib.debugger.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener

class CombiningNiddlerDebugListener(private vararg val delegates: NiddlerDebugListener) : NiddlerDebugListener {

    override fun onRequestOverride(actionId: String, message: NiddlerMessage): DebugRequest? {
        delegates.forEach { delegate -> delegate.onRequestOverride(actionId, message)?.let { return it } }
        return null
    }

    override fun onRequestAction(actionId: String, requestId: String): DebugResponse? {
        delegates.forEach { delegate -> delegate.onRequestAction(actionId, requestId)?.let { return it } }
        return null
    }

    override fun onResponseAction(actionId: String, requestId: String, response: NiddlerMessage, request: NiddlerMessage?): DebugResponse? {
        delegates.forEach { delegate -> delegate.onResponseAction(actionId, requestId, response, request)?.let { return it } }
        return null
    }

}