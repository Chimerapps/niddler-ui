package com.icapps.niddler.ui.debugger.model

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose


/**
 * @author nicolaverbeeck
 */
open class NiddlerClientMessage(val type: String)

internal const val MESSAGE_START_DEBUG = "startDebug"
internal const val MESSAGE_END_DEBUG = "endDebug"
internal const val CONTROL_DEBUG = "controlDebug"
internal const val MESSAGE_ACTIVATE = "activate"
internal const val MESSAGE_DEACTIVATE = "deactivate"
internal const val MESSAGE_MUTE_ACTIONS = "muteActions"
internal const val MESSAGE_UNMUTE_ACTIONS = "unmuteActions"
internal const val MESSAGE_ADD_BLACKLIST = "addBlacklist"
internal const val MESSAGE_REMOVE_BLACKLIST = "removeBlacklist"
internal const val MESSAGE_ADD_DEFAULT_RESPONSE = "addDefaultResponse"
internal const val MESSAGE_DEBUG_REPLY = "debugReply"
internal const val MESSAGE_ADD_REQUEST = "addRequest"
internal const val MESSAGE_REMOVE_REQUEST = "removeRequest"
internal const val MESSAGE_ADD_RESPONSE = "addResponse"
internal const val MESSAGE_REMOVE_RESPONSE = "removeResponse"
internal const val MESSAGE_ACTIVATE_ACTION = "activateAction"
internal const val MESSAGE_DEACTIVATE_ACTION = "deactivateAction"
internal const val MESSAGE_DELAYS = "updateDelays"
internal const val MESSAGE_ADD_DEFAULT_REQUEST_OVERRIDE = "addDefaultRequestOverride"
internal const val MESSAGE_ADD_REQUEST_OVERRIDE = "addRequestOverride"
internal const val MESSAGE_REMOVE_REQUEST_OVERRIDE = "removeRequestOverride"

open class NiddlerDebugControlMessage(val controlType: String,
                                      val payload: Any?)
    : NiddlerClientMessage(CONTROL_DEBUG)

class NiddlerRootMessage(val type: String)

open class RegexPayload(val regex: String?)

open class MethodPayload(val matchMethod: String?)

open class ResponseCodePayload(val responseCode: Int?)

open class ActionPayload(val id: String)

open class ActivePayload(val active: Boolean)

class AddBlacklistMessage(regex: String)
    : NiddlerDebugControlMessage(MESSAGE_ADD_BLACKLIST, RegexPayload(regex))

class RemoveBlacklistMessage(regex: String)
    : NiddlerDebugControlMessage(MESSAGE_REMOVE_BLACKLIST, RegexPayload(regex))

class MuteActionsMessage
    : NiddlerDebugControlMessage(MESSAGE_MUTE_ACTIONS, null)

class UnmuteActionsMessage
    : NiddlerDebugControlMessage(MESSAGE_UNMUTE_ACTIONS, null)

class UpdateDelaysMessage(delays: DebuggerDelays)
    : NiddlerDebugControlMessage(MESSAGE_DELAYS, delays)

class DeactivateActionMessage(id: String)
    : NiddlerDebugControlMessage(MESSAGE_DEACTIVATE_ACTION, ActionPayload(id))

class ActivateActionMessage(id: String)
    : NiddlerDebugControlMessage(MESSAGE_ACTIVATE_ACTION, ActionPayload(id))

class RemoveRequestActionMessage(id: String)
    : NiddlerDebugControlMessage(MESSAGE_REMOVE_REQUEST, ActionPayload(id))

class RemoveResponseActionMessage(id: String)
    : NiddlerDebugControlMessage(MESSAGE_REMOVE_RESPONSE, ActionPayload(id))

class RemoveRequestOverrideActionMessage(id: String)
    : NiddlerDebugControlMessage(MESSAGE_REMOVE_REQUEST_OVERRIDE, ActionPayload(id))

data class DebugReplyPayload(val messageId: String)

abstract class DebugMessage(
        @Expose var headers: Map<String, String>?,
        @Expose var encodedBody: String?,
        @Expose var bodyMimeType: String?)

class DebugResponse(@Expose val code: Int,
                    @Expose val message: String,
                    headers: Map<String, String>?,
                    encodedBody: String?,
                    bodyMimeType: String?) : DebugMessage(headers, encodedBody, bodyMimeType)

class DebugRequest(@Expose var url: String = "",
                   @Expose var method: String = "",
                   headers: Map<String, String>? = null,
                   encodedBody: String? = null,
                   bodyMimeType: String? = null) : DebugMessage(headers, encodedBody, bodyMimeType) {
    override fun toString(): String {
        return url
    }
}

data class DebuggerDelays(@Expose val preBlacklist: Long?,
                          @Expose val postBlacklist: Long?,
                          @Expose val timePerCall: Long?)

internal fun mergeToJson(gson: Gson, vararg messages: Any): JsonElement {
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

internal fun JsonObject.merge(gson: Gson, toAdd: Any?): JsonObject {
    if (toAdd == null)
        return this

    val toAddJson = gson.toJsonTree(toAdd).asJsonObject
    toAddJson.forEach { name, value ->
        if (!has(name))
            add(name, value)
    }
    return this
}

private inline fun JsonObject.forEach(block: (String, JsonElement) -> Unit) {
    entrySet().forEach { block(it.key, it.value) }
}