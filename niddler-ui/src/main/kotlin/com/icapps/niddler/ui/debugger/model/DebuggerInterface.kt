package com.icapps.niddler.ui.debugger.model

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose

/**
 * @author nicolaverbeeck
 */
interface DebuggerInterface {

    fun activate()

    fun deactivate()

    fun updateBlacklist(active: Iterable<String>)

    fun updateDefaultResponses(items: Iterable<LocalRequestIntercept>)

    fun mute()

    fun unmute()

    fun updateDelays(delays: DebuggerDelays?)

    fun debugDelays(): DebuggerDelays?

    fun connect()

    fun disconnect()
}

interface BaseAction {
    var id: String
    var repeatCount: Int?
    var active: Boolean

    fun updateJson(json: JsonObject) {
        json.addProperty("id", id)
        json.addProperty("active", active)
        json.addProperty("repeatCount", repeatCount)
    }

}

interface BaseMatcher : BaseAction {
    var regex: String?
    var matchMethod: String?

    override fun updateJson(json: JsonObject) {
        super.updateJson(json)
        json.addProperty("regex", regex)
        json.addProperty("matchMethod", matchMethod)
    }
}

interface ResponseMatcher : BaseMatcher {
    var responseCode: Int?

    override fun updateJson(json: JsonObject) {
        super.updateJson(json)
        json.addProperty("responseCode", responseCode)
    }
}

data class LocalRequestOverride(@Expose override var id: String = "",
                                @Expose override var active: Boolean = false,
                                @Expose override var regex: String? = null,
                                @Expose override var matchMethod: String? = null,
                                @Expose override var repeatCount: Int? = null,
                                @Expose var debugRequest: DebugRequest? = null) : BaseMatcher {
    override fun toString(): String {
        return regex ?: matchMethod ?: ""
    }
}

data class LocalRequestIntercept(@Expose override var id: String = "",
                                 @Expose override var active: Boolean = false,
                                 @Expose override var regex: String? = null,
                                 @Expose override var matchMethod: String? = null,
                                 @Expose override var repeatCount: Int? = null,
                                 @Expose var debugResponse: DebugResponse? = null) : BaseMatcher {

    fun toServerJson(gson: Gson): JsonObject {
        val json = JsonObject()
        updateJson(json)

        json.merge(gson, debugResponse)

        return json
    }

}

data class LocalResponseIntercept(@Expose override var id: String = "",
                                  @Expose override var active: Boolean = false,
                                  @Expose override var regex: String? = null,
                                  @Expose override var matchMethod: String? = null,
                                  @Expose override var repeatCount: Int? = null,
                                  @Expose override var responseCode: Int? = null,
                                  @Expose var response: DebugResponse? = null) : ResponseMatcher