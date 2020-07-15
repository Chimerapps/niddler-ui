package com.icapps.niddler.lib.debugger.model.breakpoint

import com.icapps.niddler.lib.debugger.model.configuration.BaseDebuggerConfiguration
import com.icapps.niddler.lib.debugger.model.configuration.DebuggerLocationMatch

data class Breakpoint(override val active: Boolean,
                      override val name: String,
                      override val locations: List<DebuggerLocationMatch>,
                      val request: Boolean,
                      val response: Boolean,
                      val method: String?,
                      @Transient override val id: String) : BaseDebuggerConfiguration {

    val location : DebuggerLocationMatch
        get() = locations.first()
}