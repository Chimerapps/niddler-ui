package com.chimerapps.niddler.ui.util.localization

import com.chimerapps.niddler.ui.util.localization.Localization

enum class Tr(val key: String) {
    ActionClear("niddler.action.clear"),
    ActionClearDescription("niddler.action.clear.description"),
    ActionConfigureBreakpoints("niddler.action.configure.breakpoints"),
    ActionConfigureBreakpointsDescription("niddler.action.configure.breakpoints.description"),
    ActionConfigureRewrite("niddler.action.configure.rewrite"),
    ActionConfigureRewriteDescription("niddler.action.configure.rewrite.description"),
    ActionConnect("niddler.action.connect"),
    ActionConnectDebugger("niddler.action.connect.debugger"),
    ActionConnectDebuggerDescription("niddler.action.connect.debugger.description"),
    ActionConnectDescription("niddler.action.connect.description"),
    ActionDisconnect("niddler.action.disconnect"),
    ActionDisconnectDescription("niddler.action.disconnect.description"),
    ActionExport("niddler.action.export"),
    ActionExportDescription("niddler.action.export.description"),
    ActionNewSession("niddler.action.new.session"),
    ActionNewSessionDescription("niddler.action.new.session.description"),
    ActionScrollToEnd("niddler.action.scroll.to.end"),
    ActionScrollToEndDescription("niddler.action.scroll.to.end.description"),
    ActionViewLinked("niddler.action.view.linked"),
    ActionViewLinkedDescription("niddler.action.view.linked.description"),
    ActionViewTimeline("niddler.action.view.timeline"),
    ActionViewTimelineDescription("niddler.action.view.timeline.description");

    fun tr() : String {
        return Localization.getString(key)
    }
}