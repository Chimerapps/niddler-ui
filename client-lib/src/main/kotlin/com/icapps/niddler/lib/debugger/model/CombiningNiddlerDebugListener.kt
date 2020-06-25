package com.icapps.niddler.lib.debugger.model

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener

class CombiningNiddlerDebugListener(vararg val delegates: NiddlerDebugListener) : NiddlerDebugListener {

    override fun onRequestOverride(message: NiddlerMessage): DebugRequest? {
        delegates.forEach { delegate -> delegate.onRequestOverride(message)?.let { return it } }
        return null
    }

    override fun onRequestAction(requestId: String): DebugResponse? {
        delegates.forEach { delegate -> delegate.onRequestAction(requestId)?.let { return it } }
        return null
    }

    override fun onResponseAction(requestId: String, response: NiddlerMessage, request: NiddlerMessage?): DebugResponse? {
        delegates.forEach { delegate -> delegate.onResponseAction(requestId, response, request)?.let { return it } }
        return null
    }

}