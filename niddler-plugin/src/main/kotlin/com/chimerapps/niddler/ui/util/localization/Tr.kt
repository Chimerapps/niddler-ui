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
    ActionExportFilterAll("niddler.action.export.filter.all"),
    ActionExportFilterCurrentView("niddler.action.export.filter.current.view"),
    ActionExportFilterMessage("niddler.action.export.filter.message"),
    ActionExportFilterTitle("niddler.action.export.filter.title"),
    ActionExportSuccessMessage("niddler.action.export.success.message"),
    ActionExportSuccessTitle("niddler.action.export.success.title"),
    ActionExportTitle("niddler.action.export.title"),
    ActionNewSession("niddler.action.new.session"),
    ActionNewSessionDescription("niddler.action.new.session.description"),
    ActionScrollToEnd("niddler.action.scroll.to.end"),
    ActionScrollToEndDescription("niddler.action.scroll.to.end.description"),
    ActionViewLinked("niddler.action.view.linked"),
    ActionViewLinkedDescription("niddler.action.view.linked.description"),
    ActionViewTimeline("niddler.action.view.timeline"),
    ActionViewTimelineDescription("niddler.action.view.timeline.description"),
    DebuggerActionAdd("niddler.debugger.action.add"),
    DebuggerActionExport("niddler.debugger.action.export"),
    DebuggerActionExportSuccessMessage("niddler.debugger.action.export.success.message"),
    DebuggerActionExportSuccessTitle("niddler.debugger.action.export.success.title"),
    DebuggerActionExportTitle("niddler.debugger.action.export.title"),
    DebuggerActionImport("niddler.debugger.action.import"),
    DebuggerActionImportFailedDescription("niddler.debugger.action.import.failed.description"),
    DebuggerActionImportFailedTitle("niddler.debugger.action.import.failed.title"),
    DebuggerActionImportTitle("niddler.debugger.action.import.title"),
    DebuggerActionRemove("niddler.debugger.action.remove"),
    DebuggerToggleEnable("niddler.debugger.toggle.enable"),
    RewriteReplaceFailedMessage("niddler.rewrite.replace.failed.message"),
    RewriteReplaceFailedTitle("niddler.rewrite.replace.failed.title"),
    ViewSession("niddler.view.session"),
    ViewStartingAdb("niddler.view.starting.adb"),
    ViewXmlActionCopyTree("niddler.view.xml.action.copy.tree"),
    ViewXmlActionCopyValue("niddler.view.xml.action.copy.value");

    fun tr() : String {
        return Localization.getString(key)
    }
}