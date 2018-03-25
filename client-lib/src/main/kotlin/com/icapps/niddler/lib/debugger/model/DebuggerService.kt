package com.icapps.niddler.lib.debugger.model

import com.google.gson.GsonBuilder
import com.icapps.niddler.lib.debugger.NiddlerDebuggerConnection
import java.util.*

/**
 * @author nicolaverbeeck
 */
class DebuggerService(private val connection: NiddlerDebuggerConnection) {

    private val gson = GsonBuilder()
            .disableHtmlEscaping()
            .create()

    fun addBlacklistItem(regex: String) {
        sendMessage(AddBlacklistMessage(regex))
    }

    fun removeBlacklistItem(regex: String) {
        sendMessage(RemoveBlacklistMessage(regex))
    }

    fun addDefaultRequestOverride(regex: String?, method: String?, request: DebugRequest, active: Boolean): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_DEFAULT_REQUEST_OVERRIDE,
                mergeToJson(gson, RegexPayload(regex),
                        MethodPayload(method),
                        ActionPayload(id),
                        ActivePayload(active),
                        request)))
        return id
    }

    fun addDefaultResponse(regex: String?, method: String?, response: DebugResponse, active: Boolean): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_DEFAULT_RESPONSE,
                mergeToJson(gson, RegexPayload(regex),
                        MethodPayload(method),
                        ActionPayload(id),
                        ActivePayload(active),
                        response)))
        return id
    }

    fun addRequestIntercept(regex: String?, method: String?, active: Boolean): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_REQUEST,
                mergeToJson(gson, RegexPayload(regex),
                        MethodPayload(method),
                        ActionPayload(id),
                        ActivePayload(active))))
        return id
    }

    fun addResponseIntercept(regex: String?, method: String?, responseCode: Int?, active: Boolean): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_RESPONSE,
                mergeToJson(gson, RegexPayload(regex),
                        MethodPayload(method),
                        ResponseCodePayload(responseCode),
                        ActionPayload(id),
                        ActivePayload(active))))
        return id
    }

    fun addRequestOverride(regex: String?, method: String?, active: Boolean): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_REQUEST_OVERRIDE,
                mergeToJson(gson, RegexPayload(regex),
                        MethodPayload(method),
                        ActionPayload(id),
                        ActivePayload(active))))
        return id
    }

    fun respondTo(niddlerMessageId: String, response: DebugResponse) {
        sendMessage(NiddlerDebugControlMessage(MESSAGE_DEBUG_REPLY,
                mergeToJson(gson, DebugReplyPayload(niddlerMessageId),
                        response)))
    }

    fun muteAction(id: String) {
        sendMessage(DeactivateActionMessage(id))
    }

    fun unmuteAction(id: String) {
        sendMessage(ActivateActionMessage(id))
    }

    fun removeRequestAction(id: String) {
        sendMessage(RemoveRequestActionMessage(id))
    }

    fun removeResponseAction(id: String) {
        sendMessage(RemoveResponseActionMessage(id))
    }

    fun removeRequestOverrideMethod(id: String) {
        sendMessage(RemoveRequestOverrideActionMessage(id))
    }

    fun updateDelays(delays: DebuggerDelays) {
        sendMessage(UpdateDelaysMessage(delays))
    }

    fun setAllActionsMuted(muted: Boolean) {
        if (muted)
            sendMessage(MuteActionsMessage())
        else
            sendMessage(UnmuteActionsMessage())
    }

    fun connect() {
        sendMessage(NiddlerRootMessage(MESSAGE_START_DEBUG))
    }

    fun disconnect() {
        sendMessage(NiddlerRootMessage(MESSAGE_END_DEBUG))
    }

    private fun sendMessage(msg: Any) {
        connection.sendMessage(gson.toJson(msg))
    }

    fun setActive(active: Boolean) {
        if (active)
            sendMessage(NiddlerDebugControlMessage(MESSAGE_ACTIVATE, payload = null))
        else
            sendMessage(NiddlerDebugControlMessage(MESSAGE_DEACTIVATE, payload = null))
    }

}
