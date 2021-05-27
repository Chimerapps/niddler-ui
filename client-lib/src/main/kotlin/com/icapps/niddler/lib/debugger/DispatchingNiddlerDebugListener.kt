package com.icapps.niddler.lib.debugger

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse

/**
 * @author Nicola Verbeeck
 */
class DispatchingNiddlerDebugListener : NiddlerDebugListener {

    private val delegates = mutableListOf<NiddlerDebugListener>()

    fun addDelegate(delegate: NiddlerDebugListener) {
        synchronized(delegates) {
            delegates += delegate
        }
    }

    override fun onRequestOverride(message: NiddlerMessage): DebugRequest? {
        synchronized(delegates) {
            for (delegate in delegates) {
                delegate.onRequestOverride(message)?.let { return it }
            }
        }
        return null
    }

    override fun onRequestAction(requestId: String, request: NiddlerMessage?): DebugResponse? {
        synchronized(delegates) {
            for (delegate in delegates) {
                delegate.onRequestAction(requestId, request)?.let { return it }
            }
        }
        return null
    }

    override fun onResponseAction(requestId: String, response: NiddlerMessage, request: NiddlerMessage?): DebugResponse? {
        synchronized(delegates) {
            for (delegate in delegates) {
                delegate.onResponseAction(requestId, response, request)?.let { return it }
            }
        }
        return null
    }
}