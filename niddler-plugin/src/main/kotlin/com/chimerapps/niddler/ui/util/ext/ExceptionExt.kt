package com.chimerapps.niddler.ui.util.ext

import java.io.PrintWriter
import java.io.StringWriter

val Throwable.stackTraceString: String
    get() {
        val stringWriter = StringWriter()
        this.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()
    }