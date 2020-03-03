package com.icapps.niddler.lib.debugger.model.rewrite.action

import com.icapps.niddler.lib.debugger.model.rewrite.RewriteRule
import com.icapps.niddler.lib.utils.makeQueryString
import com.icapps.niddler.lib.utils.newBuilder
import java.net.URI

abstract class BaseModifyQueryParameterAction(rule: RewriteRule) : BaseModifyMapAction(rule) {

    protected fun updateUrl(url: String, mutableQueries: Map<String, List<String>>): String {
        val uri = try {
            URI(url)
        } catch (e: Throwable) {
            return url
        }

        return uri.newBuilder().also { it.query = makeQueryString(mutableQueries) }.build().toString()
    }

}