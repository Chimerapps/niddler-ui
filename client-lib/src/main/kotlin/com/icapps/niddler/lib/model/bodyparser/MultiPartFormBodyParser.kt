package com.icapps.niddler.lib.model.bodyparser

import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyParser
import org.apache.http.entity.ContentType
import org.synchronoss.cloud.nio.multipart.BlockingIOAdapter
import org.synchronoss.cloud.nio.multipart.Multipart
import org.synchronoss.cloud.nio.multipart.MultipartContext
import java.io.ByteArrayInputStream

class MultiPartFormBodyParser(private val contentType: ContentType) : BodyParser<Map<String, ByteArray>> {

    override fun parse(bodyFormat: BodyFormat, bytes: ByteArray): Map<String, ByteArray>? {

        val multipart = Multipart.multipart(MultipartContext(contentType.toString(), bytes.size, bodyFormat.encoding ?: "UTF-8"))
        multipart.forBlockingIO(ByteArrayInputStream(bytes)).use { iterator ->
            while (iterator.hasNext()) {
                val token = iterator.next()
                when (token.type!!) {
                    BlockingIOAdapter.ParserToken.Type.PART -> {
                        token as BlockingIOAdapter.Part
                        val headers = token.headers
                        val body = token.partBody.readBytes()
                        //TODO
                    }
                    BlockingIOAdapter.ParserToken.Type.NESTED_START -> {
                        TODO()
                    }
                    BlockingIOAdapter.ParserToken.Type.NESTED_END -> {
                        TODO()
                    }
                }
            }
        }

        return null
    }

}
