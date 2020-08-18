package com.chimerapps.niddler.ui.util.localization

enum class Tr(val key: String) {
    ActionClear("niddler.action.clear"), //Clear local
    ActionClearDescription("niddler.action.clear.description"), //Remove locally cached messages
    ActionConfigureBreakpoints("niddler.action.configure.breakpoints"), //Configure breakpoints
    ActionConfigureBreakpointsDescription("niddler.action.configure.breakpoints.description"), //Configure breakpoints
    ActionConfigureRewrite("niddler.action.configure.rewrite"), //Configure rewrite rules
    ActionConfigureRewriteDescription("niddler.action.configure.rewrite.description"), //Configure rewrite rules
    ActionConnect("niddler.action.connect"), //Connect
    ActionConnectDebugger("niddler.action.connect.debugger"), //Connect with debugger
    ActionConnectDebuggerDescription("niddler.action.connect.debugger.description"), //Connect to niddler server using the debugger
    ActionConnectDescription("niddler.action.connect.description"), //Connect to niddler server
    ActionDisconnect("niddler.action.disconnect"), //Disconnect
    ActionDisconnectDescription("niddler.action.disconnect.description"), //Disconnect from niddler server
    ActionExport("niddler.action.export"), //Export
    ActionExportDescription("niddler.action.export.description"), //Export messages to HAR
    ActionExportFilterAll("niddler.action.export.filter.all"), //All
    ActionExportFilterCurrentView("niddler.action.export.filter.current.view"), //Current view
    ActionExportFilterMessage("niddler.action.export.filter.message"), //A filter is active.\nDo you wish to export only the items matching the filter?
    ActionExportFilterTitle("niddler.action.export.filter.title"), //Export options
    ActionExportSuccessMessage("niddler.action.export.success.message"), //Export completed to
    ActionExportSuccessTitle("niddler.action.export.success.title"), //Save complete
    ActionExportTitle("niddler.action.export.title"), //Save export to
    ActionNewSession("niddler.action.new.session"), //New session
    ActionNewSessionDescription("niddler.action.new.session.description"), //Start a new session
    ActionScrollToEnd("niddler.action.scroll.to.end"), //Scroll to the end
    ActionScrollToEndDescription("niddler.action.scroll.to.end.description"), //Keep the view scrolled to the end
    ActionViewLinked("niddler.action.view.linked"), //Linked
    ActionViewLinkedDescription("niddler.action.view.linked.description"), //View request and responses grouped together
    ActionViewTimeline("niddler.action.view.timeline"), //Timeline
    ActionViewTimelineDescription("niddler.action.view.timeline.description"), //View in chronological order
    BodyActionHintSaveBody("niddler.body.action.hint.save.body"), //Save body
    BodyButtonPretty("niddler.body.button.pretty"), //Pretty
    BodyButtonRaw("niddler.body.button.raw"), //Raw
    BodyButtonStructured("niddler.body.button.structured"), //Structured
    BodySaveSuccessMessage("niddler.body.save.success.message"), //Export completed to
    BodySaveSuccessTitle("niddler.body.save.success.title"), //Save complete
    BodySaveTitle("niddler.body.save.title"), //Save to
    BreakpointsConfigureCancel("niddler.breakpoints.configure.cancel"), //Cancel
    BreakpointsConfigureOk("niddler.breakpoints.configure.ok"), //OK
    BreakpointsConfigureTitle("niddler.breakpoints.configure.title"), //Breakpoint settings
    ClipboardActionDialogSaveTitle("niddler.clipboard.action.dialog.save.title"), //Save data to
    DebuggerActionAdd("niddler.debugger.action.add"), //Add
    DebuggerActionExport("niddler.debugger.action.export"), //Export
    DebuggerActionExportSuccessMessage("niddler.debugger.action.export.success.message"), //Breakpoint export completed to
    DebuggerActionExportSuccessTitle("niddler.debugger.action.export.success.title"), //Breakpoint export complete
    DebuggerActionExportTitle("niddler.debugger.action.export.title"), //Export to
    DebuggerActionImport("niddler.debugger.action.import"), //Import
    DebuggerActionImportFailedDescription("niddler.debugger.action.import.failed.description"), //Failed to parse file
    DebuggerActionImportFailedTitle("niddler.debugger.action.import.failed.title"), //Failed to import
    DebuggerActionImportTitle("niddler.debugger.action.import.title"), //Select file
    DebuggerActionRemove("niddler.debugger.action.remove"), //Remove
    DebuggerToggleEnable("niddler.debugger.toggle.enable"), //Enable
    EditLocationAction("niddler.edit.location.action"), //Action:
    EditLocationDialogCancel("niddler.edit.location.dialog.cancel"), //Cancel
    EditLocationDialogOk("niddler.edit.location.dialog.ok"), //OK
    EditLocationDialogTitle("niddler.edit.location.dialog.title"), //Edit Location
    EditLocationHost("niddler.edit.location.host"), //Host:
    EditLocationMatchDescription("niddler.edit.location.match.description"), //Empty fields match all values. Wildcards * and ? may be used.
    EditLocationPath("niddler.edit.location.path"), //Path:
    EditLocationPort("niddler.edit.location.port"), //Port:
    EditLocationProtocol("niddler.edit.location.protocol"), //Protocol:
    EditLocationQuery("niddler.edit.location.query"), //Query:
    EditRewriteDialogActionAppendHeader("niddler.edit.rewrite.dialog.action.append.header"), //Add header
    EditRewriteDialogActionAppendQueryParameter("niddler.edit.rewrite.dialog.action.append.query.parameter"), //Add query parameter
    EditRewriteDialogActionModifyBody("niddler.edit.rewrite.dialog.action.modify.body"), //Body
    EditRewriteDialogActionModifyHeader("niddler.edit.rewrite.dialog.action.modify.header"), //Modify header
    EditRewriteDialogActionModifyHost("niddler.edit.rewrite.dialog.action.modify.host"), //Host
    EditRewriteDialogActionModifyPath("niddler.edit.rewrite.dialog.action.modify.path"), //Path
    EditRewriteDialogActionModifyQueryParameter("niddler.edit.rewrite.dialog.action.modify.query.parameter"), //Modify query parameter
    EditRewriteDialogActionModifyResponseStatus("niddler.edit.rewrite.dialog.action.modify.response.status"), //Response status
    EditRewriteDialogActionModifyUrl("niddler.edit.rewrite.dialog.action.modify.url"), //Url
    EditRewriteDialogActionRemoveHeader("niddler.edit.rewrite.dialog.action.remove.header"), //Remove header
    EditRewriteDialogActionRemoveQueryParameter("niddler.edit.rewrite.dialog.action.remove.query.parameter"), //Remove query parameter
    EditRewriteDialogTitle("niddler.edit.rewrite.dialog.title"), //Edit Rewrite Rule
    EditRewriteUiCancel("niddler.edit.rewrite.ui.cancel"), //Cancel
    EditRewriteUiCaseSensitive("niddler.edit.rewrite.ui.case.sensitive"), //Case sensitive
    EditRewriteUiMatchDescription("niddler.edit.rewrite.ui.match.description"), //Enter text to match or leave blank to match all
    EditRewriteUiMatchEntireValue("niddler.edit.rewrite.ui.match.entire.value"), //Match whole value
    EditRewriteUiMatchName("niddler.edit.rewrite.ui.match.name"), //Name:
    EditRewriteUiMatchNameRegex("niddler.edit.rewrite.ui.match.name.regex"), //Regex
    EditRewriteUiMatchValueRegex("niddler.edit.rewrite.ui.match.value.regex"), //Regex
    EditRewriteUiOk("niddler.edit.rewrite.ui.ok"), //OK
    EditRewriteUiPanelMatch("niddler.edit.rewrite.ui.panel.match"), //Match
    EditRewriteUiPanelReplace("niddler.edit.rewrite.ui.panel.replace"), //Replace
    EditRewriteUiPanelWhere("niddler.edit.rewrite.ui.panel.where"), //Where
    EditRewriteUiReplaceAll("niddler.edit.rewrite.ui.replace.all"), //Replace all
    EditRewriteUiReplaceDescription("niddler.edit.rewrite.ui.replace.description"), //Enter new values or leave blank for no change. If using regex matches you may enter references to groups, eg. $1
    EditRewriteUiReplaceFirst("niddler.edit.rewrite.ui.replace.first"), //Replace first
    EditRewriteUiReplaceName("niddler.edit.rewrite.ui.replace.name"), //Name:
    EditRewriteUiReplaceValue("niddler.edit.rewrite.ui.replace.value"), //Value:
    EditRewriteUiRequest("niddler.edit.rewrite.ui.request"), //Request
    EditRewriteUiResponse("niddler.edit.rewrite.ui.response"), //Response
    EditRewriteUiType("niddler.edit.rewrite.ui.type"), //Type:
    EditRewriteUiValue("niddler.edit.rewrite.ui.value"), //Value:
    PreferenceLabelPerProject("niddler.preference.label.per.project"), //Per project
    PreferencesBrowseAdbDescription("niddler.preferences.browse.adb.description"), //Path to adb
    PreferencesBrowseAdbTitle("niddler.preferences.browse.adb.title"), //Niddler - adb
    PreferencesBrowseIdeviceDescription("niddler.preferences.browse.idevice.description"), //Path to imobiledevice folders
    PreferencesBrowseIdeviceTitle("niddler.preferences.browse.idevice.title"), //Niddler - imobiledevice
    PreferencesButtonTestConfiguration("niddler.preferences.button.test.configuration"), //Test configuration
    PreferencesOptionConnectDebugger("niddler.preferences.option.connect.debugger"), //Connect using the debugger
    PreferencesOptionPathToAdb("niddler.preferences.option.path.to.adb"), //Path to adb:
    PreferencesOptionPathToIdevice("niddler.preferences.option.path.to.idevice"), //Path to idevice binaries:
    PreferencesOptionReconnect("niddler.preferences.option.reconnect"), //Automatically connect niddler when running
    PreferencesOptionReuseConnection("niddler.preferences.option.reuse.connection"), //Re-use session when automatically connecting
    PreferencesTestMessageAdbFoundAt("niddler.preferences.test.message.adb.found.at"), //ADB defined at path: %s
    PreferencesTestMessageAdbNotFound("niddler.preferences.test.message.adb.not.found"), //Path to ADB not found
    PreferencesTestMessageAdbOk("niddler.preferences.test.message.adb.ok"), //ADB path seems ok
    PreferencesTestMessageCheckingAdb("niddler.preferences.test.message.checking.adb"), //Checking adb command
    PreferencesTestMessageErrorAdbNotExecutable("niddler.preferences.test.message.error.adb.not.executable"), //ERROR - ADB file not executable
    PreferencesTestMessageErrorAdbNotFound("niddler.preferences.test.message.error.adb.not.found"), //ERROR - ADB file not found
    PreferencesTestMessageErrorCommunicationFailed("niddler.preferences.test.message.error.communication.failed"), //ERROR - Failed to communicate with adb
    PreferencesTestMessageErrorFileNotExecutable("niddler.preferences.test.message.error.file.not.executable"), //ERROR - %s file not executable
    PreferencesTestMessageErrorFileNotFound("niddler.preferences.test.message.error.file.not.found"), //ERROR - %s file not found
    PreferencesTestMessageErrorIdeviceNotDirectory("niddler.preferences.test.message.error.idevice.not.directory"), //ERROR - iMobileDevice path is not a directory
    PreferencesTestMessageErrorIdeviceNotFound("niddler.preferences.test.message.error.idevice.not.found"), //ERROR - iMobileDevice folder not found
    PreferencesTestMessageErrorPathIsDir("niddler.preferences.test.message.error.path.is.dir"), //ERROR - Specified path is a directory
    PreferencesTestMessageFileOk("niddler.preferences.test.message.file.ok"), //%s seems ok
    PreferencesTestMessageFoundDevicesCount("niddler.preferences.test.message.found.devices.count"), //ADB devices returns: %d device(s)
    PreferencesTestMessageIdevicePath("niddler.preferences.test.message.idevice.path"), //iMobileDevice folder defined at path: %s
    PreferencesTestMessageListingAdbDevices("niddler.preferences.test.message.listing.adb.devices"), //Listing devices
    PreferencesTestMessageStartingAdb("niddler.preferences.test.message.starting.adb"), //Starting adb server
    PreferencesTestMessageTestingAdbTitle("niddler.preferences.test.message.testing.adb.title"), //Testing ADB\n=======================================
    PreferencesTestMessageTestingIdeviceTitle("niddler.preferences.test.message.testing.idevice.title"), //\nTesting iDevice\n=======================================
    RewriteConfigureDetailAction("niddler.rewrite.configure.detail.action"), //Action
    RewriteConfigureDetailActionAppendHeader("niddler.rewrite.configure.detail.action.append.header"), //Append header
    RewriteConfigureDetailActionHost("niddler.rewrite.configure.detail.action.host"), //Host
    RewriteConfigureDetailActionModifyHeader("niddler.rewrite.configure.detail.action.modify.header"), //Modify header
    RewriteConfigureDetailActionRemoveHeader("niddler.rewrite.configure.detail.action.remove.header"), //Remove header
    RewriteConfigureDetailAppendQuery("niddler.rewrite.configure.detail.append.query"), //Append query
    RewriteConfigureDetailBody("niddler.rewrite.configure.detail.body"), //Body
    RewriteConfigureDetailLocation("niddler.rewrite.configure.detail.location"), //Location
    RewriteConfigureDetailLocationAdd("niddler.rewrite.configure.detail.location.add"), //Add
    RewriteConfigureDetailLocationRemove("niddler.rewrite.configure.detail.location.remove"), //Remove
    RewriteConfigureDetailModifyQuery("niddler.rewrite.configure.detail.modify.query"), //Modify query
    RewriteConfigureDetailName("niddler.rewrite.configure.detail.name"), //Name:
    RewriteConfigureDetailPath("niddler.rewrite.configure.detail.path"), //Path
    RewriteConfigureDetailRemoveQuery("niddler.rewrite.configure.detail.remove.query"), //Remove query
    RewriteConfigureDetailRuleAdd("niddler.rewrite.configure.detail.rule.add"), //Add
    RewriteConfigureDetailRuleDown("niddler.rewrite.configure.detail.rule.down"), //Down
    RewriteConfigureDetailRuleRemove("niddler.rewrite.configure.detail.rule.remove"), //Remove
    RewriteConfigureDetailRuleUp("niddler.rewrite.configure.detail.rule.up"), //Up
    RewriteConfigureDetailStatus("niddler.rewrite.configure.detail.status"), //Status
    RewriteConfigureDetailType("niddler.rewrite.configure.detail.type"), //Type
    RewriteConfigureDetailUrl("niddler.rewrite.configure.detail.url"), //URL
    RewriteDialogCancel("niddler.rewrite.dialog.cancel"), //Cancel
    RewriteDialogDefaultName("niddler.rewrite.dialog.default.name"), //No name
    RewriteDialogOk("niddler.rewrite.dialog.ok"), //OK
    RewriteDialogTitle("niddler.rewrite.dialog.title"), //Rewrite settings
    RewriteReplaceFailedMessage("niddler.rewrite.replace.failed.message"), //Status code replacement failed, new value is not a valid HTTP status line. Required format: '\d+\s+.*'. Got:
    RewriteReplaceFailedTitle("niddler.rewrite.replace.failed.title"), //Replacement failed
    StatusConnected("niddler.status.connected"), //Connected
    StatusConnectedTo("niddler.status.connected.to"), //to
    StatusDisconnected("niddler.status.disconnected"), //Disconnected
    ViewActionCopyKey("niddler.view.action.copy.key"), //Copy key
    ViewActionCopyValue("niddler.view.action.copy.value"), //Copy value
    ViewDetailActionCopy("niddler.view.detail.action.copy"), //Copy
    ViewDetailActionCopyKeyAndValue("niddler.view.detail.action.copy.key.and.value"), //Copy key and value
    ViewDetailDecodedUrl("niddler.view.detail.decoded.url"), //Decoded URL
    ViewDetailExecutionTime("niddler.view.detail.execution.time"), //Execution time
    ViewDetailExecutionTimeMilliseconds("niddler.view.detail.execution.time.milliseconds"), //msec
    ViewDetailExecutionTimeUnknown("niddler.view.detail.execution.time.unknown"), //Unknown
    ViewDetailMethod("niddler.view.detail.method"), //Method
    ViewDetailSectionContext("niddler.view.detail.section.context"), //Context
    ViewDetailSectionGeneral("niddler.view.detail.section.general"), //General
    ViewDetailSectionHeaders("niddler.view.detail.section.headers"), //Headers
    ViewDetailSectionStacktrace("niddler.view.detail.section.stacktrace"), //Stacktrace
    ViewDetailStatus("niddler.view.detail.status"), //Status
    ViewDetailTimestamp("niddler.view.detail.timestamp"), //Timestamp
    ViewDetailUrl("niddler.view.detail.url"), //URL
    ViewJsonActionCopyJson("niddler.view.json.action.copy.json"), //Copy Json
    ViewSession("niddler.view.session"), //Session
    ViewStartingAdb("niddler.view.starting.adb"), //Starting adb
    ViewTimelineActionAddRequestRewriteRule("niddler.view.timeline.action.add.request.rewrite.rule"), //Add request rewrite rule
    ViewTimelineActionCopyBody("niddler.view.timeline.action.copy.body"), //Copy body
    ViewTimelineActionCopyUrl("niddler.view.timeline.action.copy.url"), //Copy URL
    ViewTimelineActionDialogConfigureBaseUrl("niddler.view.timeline.action.dialog.configure.base.url"), //Hide base url
    ViewTimelineActionExportCurlRequest("niddler.view.timeline.action.export.curl.request"), //Export cUrl request
    ViewTimelineActionHideBaseUrls("niddler.view.timeline.action.hide.base.urls"), //Hide base urls
    ViewTimelineActionShowBaseUrls("niddler.view.timeline.action.show.base.urls"), //Show base urls
    ViewTimelineActionViewRequest("niddler.view.timeline.action.view.request"), //View request
    ViewTimelineActionViewResponse("niddler.view.timeline.action.view.response"), //View response
    ViewTimelineHeaderDirection("niddler.view.timeline.header.direction"), //Up/Down
    ViewTimelineHeaderFormat("niddler.view.timeline.header.format"), //Format
    ViewTimelineHeaderMethod("niddler.view.timeline.header.method"), //Method
    ViewTimelineHeaderStatus("niddler.view.timeline.header.status"), //Status
    ViewTimelineHeaderTimestamp("niddler.view.timeline.header.timestamp"), //Timestamp
    ViewTimelineHeaderUrl("niddler.view.timeline.header.url"), //Url
    ViewXmlActionCopyTree("niddler.view.xml.action.copy.tree"), //Copy XML tree
    ViewXmlActionCopyValue("niddler.view.xml.action.copy.value"); //Copy value

    fun tr(vararg arguments: Any) : String {
        val raw = Localization.getString(key)
        if (arguments.isEmpty()) return raw
        return String.format(raw, *arguments)
    }
}