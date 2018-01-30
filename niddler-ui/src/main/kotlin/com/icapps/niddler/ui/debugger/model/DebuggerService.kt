package com.icapps.niddler.ui.debugger.model

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.icapps.niddler.ui.NiddlerDebuggerConnection
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

    fun addDefaultRequestOverride(regex: String, request: DebugRequest): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_DEFAULT_REQUEST_OVERRIDE,
                merge(RegexPayload(regex), ActionPayload(id), request)))
        return id
    }

    fun addDefaultResponse(regex: String, response: DebugResponse): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_DEFAULT_RESPONSE,
                merge(RegexPayload(regex), ActionPayload(id), response)))
        return id
    }

    fun addRequestIntercept(regex: String): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_REQUEST,
                merge(RegexPayload(regex), ActionPayload(id))))
        return id
    }

    fun addResponseIntercept(regex: String): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_RESPONSE,
                merge(RegexPayload(regex), ActionPayload(id))))
        return id
    }

    fun addRequestOverride(regex: String): String {
        val id = UUID.randomUUID().toString()
        sendMessage(NiddlerDebugControlMessage(MESSAGE_ADD_REQUEST_OVERRIDE,
                merge(RegexPayload(regex), ActionPayload(id))))
        return id
    }

    fun respondTo(niddlerMessageId: String, response: DebugResponse) {
        sendMessage(NiddlerDebugControlMessage(MESSAGE_DEBUG_REPLY,
                merge(DebugReplyPayload(niddlerMessageId), response)))
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

    private fun sendMessage(msg: NiddlerDebugControlMessage) {
        connection.sendMessage(gson.toJson(msg))
    }

    private fun merge(vararg messages: Any): JsonElement {
        return messages.map { gson.toJsonTree(it) }.reduce { leftRaw, rightRaw ->
            val left = leftRaw.asJsonObject
            val right = rightRaw.asJsonObject
            right.forEach { name, value ->
                if (!left.has(name))
                    left.add(name, value)
            }
            left
        }
    }

}

private inline fun JsonObject.forEach(block: (String, JsonElement) -> Unit) {
    entrySet().forEach { block(it.key, it.value) }
}