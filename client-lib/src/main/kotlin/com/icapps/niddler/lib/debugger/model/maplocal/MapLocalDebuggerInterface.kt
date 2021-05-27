package com.icapps.niddler.lib.debugger.model.maplocal

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.lib.connection.protocol.NiddlerDebugListener
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.DebuggerService
import com.icapps.niddler.lib.debugger.model.rewrite.RewriteLocationMatch
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.bodyparser.BinaryBodyParser
import com.icapps.niddler.lib.model.classifier.BodyClassifierResult
import com.icapps.niddler.lib.model.classifier.GuessingBodyParser
import java.io.File
import java.net.URL
import java.util.Base64
import java.util.UUID

class MapLocalDebuggerInterface(private val debuggerService: DebuggerService) {

    private val rulesetMap = mutableMapOf<String, MapLocalReference>()

    fun addLocalMapping(mapLocalEntry: MapLocalEntry): String {
        val id = UUID.randomUUID().toString()
        val ids = createLocationInterceptor(RewriteLocationMatch(mapLocalEntry.location, mapLocalEntry.enabled))

        rulesetMap[id] = MapLocalReference(mapLocalEntry, ids)

        return id
    }

    fun removeRuleSet(token: String) {
        val reference = rulesetMap.remove(token) ?: return
        debuggerService.removeRequestAction(reference.remoteId)
    }

    private fun createLocationInterceptor(rewriteLocationMatch: RewriteLocationMatch): String {
        val active = rewriteLocationMatch.enabled
        val regex = rewriteLocationMatch.location.asRegex()
        return debuggerService.addRequestIntercept(regex, method = null, active = active)
    }

    fun clearRuleSets() {
        rulesetMap.forEach { (_, ruleSetReference) ->
            debuggerService.removeRequestAction(ruleSetReference.remoteId)
        }
    }

}

class MapLocalDebugListener(private val resolver: FileResolver) : NiddlerDebugListener {

    private var mapLocalMappings: List<MapLocalEntry> = emptyList()

    fun updateMapLocal(mappings: List<MapLocalEntry>) {
        synchronized(this) {
            mapLocalMappings = mappings
        }
    }

    override fun onRequestOverride(message: NiddlerMessage): DebugRequest? {
        return null
    }

    override fun onRequestAction(requestId: String, request: NiddlerMessage?): DebugResponse? {
        val url = request?.url ?: return null

        synchronized(this) {
            mapLocalMappings.forEach { ruleSet ->
                if (!ruleSet.enabled) return@forEach
                if (ruleSet.matchesUrl(url)) {
                    val file = File(resolver.resolveFile(ruleSet.destination))
                    if (!file.exists()) {
                        //TODO notify
                        return@forEach
                    }
                    if (!file.isDirectory) {
                        return makeResponseFromFile(file)
                    } else {
                        val path = URL(url).path
                        val child = File(file, path)
                        if (!child.exists()) {
                            //TODO notify
                            return@forEach
                        }
                        return makeResponseFromFile(child)
                    }
                }
            }
        }
        return null
    }

    private fun makeResponseFromFile(file: File): DebugResponse {
        val bytes = file.readBytes()
        val mime = GuessingBodyParser(
            BodyClassifierResult(BodyFormat(BodyFormatType.FORMAT_BINARY, null, null), BinaryBodyParser()),
            bytes
        ).determineBodyType()?.rawType
            ?: determineMimeFromExtension(file) ?: "application/octet-stream"
        return DebugResponse(
            code = 200,
            message = "OK",
            headers = mapOf("content-type" to listOf(mime)),
            encodedBody = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes),
            bodyMimeType = mime
        )
    }

    private fun determineMimeFromExtension(file: File): String? {
        return when (file.extension) {
            "json" -> "application/json"
            "pdf" -> "application/pdf"
            "webp" -> "image/webp"
            "txt" -> "text/plain"
            else -> null
        }
    }

    override fun onResponseAction(requestId: String, response: NiddlerMessage, request: NiddlerMessage?): DebugResponse? {
        return null
    }

}

private data class MapLocalReference(val mapLocalEntry: MapLocalEntry, val remoteId: String)