package com.icapps.niddler.ui.export.har

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream

/**
 * @author Nicola Verbeeck
 * @date 09/11/2017.
 */
class StreamingHarWriterTest {

    @Test
    fun testEmpty() {
        val out = ByteArrayOutputStream()

        val writer = StreamingHarWriter(out, creator = Creator(name = "Niddler", version = "1.0"))

        writer.close()
        val string = out.toString(Charsets.UTF_8.name())
        Assert.assertEquals("{\"log\":{\"version\":\"1.2\",\"creator\":{\"name\":\"Niddler\",\"version\":\"1.0\"},\"entries\":[]}}", string)
    }

    @Test
    fun testAdd() {
        val out = ByteArrayOutputStream()

        val writer = StreamingHarWriter(out, creator = Creator(name = "Niddler", version = "1.0"))

        val entry = Entry(startedDateTime = "2005-04-01T13:38:09-0800",
                time = 19200,
                request = Request(method = "POST", url = "http://www.test.com/bla", httpVersion = "HTTP/1.1", headers = emptyList(), queryString = emptyList(), postData = null),
                response = Response(status = 200, statusText = "OK", httpVersion = "HTTP/1.1", content = Content(13, "application/xml", "<>", null), headers = emptyList()),
                cache = Cache(),
                timings = Timings(send = 13, receive = 344, wait = 1213))

        writer.addEntry(entry)

        writer.close()
        val string = out.toString(Charsets.UTF_8.name())
        Assert.assertEquals("{\"log\":{\"version\":\"1.2\",\"creator\":{\"name\":\"Niddler\",\"version\":\"1.0\"},\"entries\":[{\"startedDateTime\":\"2005-04-01T13:38:09-0800\",\"time\":19200,\"request\":{\"method\":\"POST\",\"url\":\"http://www.test.com/bla\",\"httpVersion\":\"HTTP/1.1\",\"headers\":[],\"queryString\":[],\"cookies\":[],\"headersSize\":-1,\"bodySize\":-1},\"response\":{\"status\":200,\"statusText\":\"OK\",\"httpVersion\":\"HTTP/1.1\",\"content\":{\"size\":13,\"mimeType\":\"application/xml\",\"text\":\"\\u003c\\u003e\"},\"headers\":[],\"redirectURL\":\"\",\"cookies\":[],\"headersSize\":-1,\"bodySize\":-1},\"cache\":{},\"timings\":{\"send\":13,\"wait\":1213,\"receive\":344}}]}}", string)
    }
}