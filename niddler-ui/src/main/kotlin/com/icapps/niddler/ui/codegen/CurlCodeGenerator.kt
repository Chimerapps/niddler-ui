package com.icapps.niddler.ui.codegen

import com.icapps.niddler.lib.connection.model.NiddlerMessage
import com.icapps.niddler.ui.model.ParsedNiddlerMessage

/**
 * @author Nicola Verbeeck
 * @date 14/06/2017.
 */
class CurlCodeGenerator : CodeGenerator {

    companion object {
        private val COMMAND_NAME = "curl"
    }

    override fun generateRequestCode(request: ParsedNiddlerMessage): String {
        if (!request.isRequest)
            return "<not a request>"

        return when (request.method!!.toLowerCase()) {
            "get" -> generateGet(request.message)
            "delete" -> generateDelete(request.message)
            "head" -> generateHead(request.message)
            "post" -> generatePost(request)
            "put" -> generatePut(request)
            else -> "<request type not yet supported>"
        }
    }

    private fun generateHead(request: NiddlerMessage): String {
        return generateNoBodyRequest(request, "HEAD")
    }

    private fun generateGet(request: NiddlerMessage): String {
        return generateNoBodyRequest(request, "GET")
    }

    private fun generateDelete(request: NiddlerMessage): String {
        return generateNoBodyRequest(request, "DELETE")
    }

    private fun generatePost(request: ParsedNiddlerMessage): String {
        return generateBodyRequest(request, "POST")
    }

    private fun generatePut(request: ParsedNiddlerMessage): String {
        return generateBodyRequest(request, "PUT")
    }

    private fun addHeaders(builder: StringBuilder, request: NiddlerMessage) {
        val headers = request.headers ?: return

        headers.forEach { key, values ->
            builder.append("-H \"").append(key).append(": ").append(values.joinToString(",")).append("\" ")
        }
    }

    private fun generateNoBodyRequest(request: NiddlerMessage, method: String): String {
        val builder = StringBuilder(COMMAND_NAME)
        builder.append(" -i ")
        addHeaders(builder, request)
        builder.append("-X \"").append(method.toUpperCase()).append("\" ")
        builder.append(request.url!!)
        return builder.toString()
    }

    private fun generateBodyRequest(request: ParsedNiddlerMessage, method: String): String {
        val builder = StringBuilder(COMMAND_NAME)
        builder.append(" -i ")
        addHeaders(builder, request.message)
        builder.append("-X \"").append(method.toUpperCase()).append("\" ")

        val bytes = request.message.getBodyAsBytes
        if (bytes != null) {
            builder.append("--data-binary \"")
            builder.append(escape(String(bytes, 0, bytes.size)))
            builder.append("\" ")
        }

        builder.append(request.url!!)
        return builder.toString()
    }

    private fun escape(data: String): String {
        return data.replace("\"", "\\\"")
    }

}