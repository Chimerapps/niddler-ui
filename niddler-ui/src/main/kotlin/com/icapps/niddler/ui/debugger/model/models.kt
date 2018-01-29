package com.icapps.niddler.ui.debugger.model


/**
 * @author nicolaverbeeck
 */
open class NiddlerClientMessage(val type: String)

internal const val CONTROL_DEBUG = "controlDebug"
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

open class NiddlerDebugControlMessage(val controlType: String,
                                      val payload: Any?)
    : NiddlerClientMessage(CONTROL_DEBUG)

open class RegexPayload(val regex: String)

open class ActionPayload(val id: String)

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

data class DebugReplyPayload(val messageId: String)

data class DebugResponse(val code: Int,
                         val message: String,
                         val headers: Map<String, String>?,
                         val encodedBody: String?,
                         val bodyMimeType: String?)

data class DebuggerDelays(val preBlacklist: Long?, val postBlacklist: Long?)