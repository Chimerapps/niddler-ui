package com.chimerapps.niddler.ui.debugging.maplocal

import com.chimerapps.niddler.ui.debugging.rewrite.EditableTableModel
import com.chimerapps.niddler.ui.debugging.rewrite.PackingJBTable
import com.chimerapps.niddler.ui.debugging.rewrite.createRewriteLocationFor
import com.chimerapps.niddler.ui.model.ProjectConfig
import com.chimerapps.niddler.ui.util.ui.CheckBox
import com.chimerapps.niddler.ui.util.ui.NotificationUtil
import com.chimerapps.niddler.ui.util.ui.chooseSaveFile
import com.chimerapps.niddler.ui.util.ui.setColumnFixedWidth
import com.icapps.niddler.lib.debugger.model.maplocal.MapLocalConfiguration
import com.icapps.niddler.lib.debugger.model.maplocal.MapLocalEntry
import com.icapps.niddler.lib.debugger.model.maplocal.MapLocalExporter
import com.icapps.niddler.lib.debugger.model.maplocal.VariableFileResolver
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.Window
import java.io.File
import java.util.UUID
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel


fun JComponent.padding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0): JComponent {
    border = BorderFactory.createEmptyBorder(top, left, bottom, right)
    return this
}

val JComponent.leftJustify: JComponent
    get() {
        val b = Box.createHorizontalBox()
        b.add(this)
        b.add(Box.createHorizontalGlue())
        return b
    }

/**
 * @author Nicola Verbeeck
 */
class MapLocalDialog(
    parent: Window?,
    private val fileResolver: ProjectFileResolver,
    private val project: Project,
) : JDialog(parent, "MapLocal settings", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, project: Project): MapLocalConfiguration? {
            val dialog = MapLocalDialog(parent, ProjectFileResolver(project), project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            return dialog.response
        }

        fun showAdd(parent: Window?, project: Project, message: NiddlerMessageInfo): MapLocalConfiguration? {
            val dialog = MapLocalDialog(parent, ProjectFileResolver(project), project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.addNewRuleFor(message)

            dialog.isVisible = true
            return dialog.response
        }

    }

    var response: MapLocalConfiguration? = null
        private set

    private val mappings = mutableListOf<MapLocalEntry>()

    private val rootContainer = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.Y_AXIS)

        it.add(JBLabel("Use local files to serve remote locations").leftJustify.padding(top = 16, left = 10))
    }

    private val enableAllToggle = CheckBox("Enable Map Local", ::allEnabled).also {
        rootContainer.add(it.leftJustify.padding(top = 10, left = 10, bottom = 16))
    }

    private val mappingTable: PackingJBTable = PackingJBTable(EditableTableModel() { value, row, col ->
        if (col == 0) {
            val item = mappings.getOrNull(row) ?: return@EditableTableModel
            mappings[row] = item.copy(enabled = value == true)
        }
    }, onRowDoubleClicked = { row, model ->
        val item = mappings.getOrNull(row) ?: return@PackingJBTable
        val edited = EditLocalMappingDialog.show(this, item.copy(destination = fileResolver.resolveFile(item.destination)), project)
            ?: return@PackingJBTable
        mappings[row] = edited
        (model as DefaultTableModel).setValueAt(edited.location.asString(), row, 1)
        (model as DefaultTableModel).setValueAt(edited.destination, row, 2)
    }).also {
        val model = it.model as EditableTableModel
        model.addColumn("", java.lang.Boolean::class.java)
        model.addColumn("Location", String::class.java)
        model.addColumn("Local Path", String::class.java)

        it.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        it.rowSelectionAllowed = true
        it.columnSelectionAllowed = false
        it.selectionModel.addListSelectionListener { _ ->
            val hasSelection = it.selectedRowCount != 0
            removeButton.isEnabled = hasSelection
        }

        it.setColumnFixedWidth(0, 30)

        rootContainer.add(JBScrollPane(it).also { scroller -> scroller.border = BorderFactory.createLineBorder(Color.GRAY) })
    }

    private val actionButtonPanel = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.X_AXIS)
        rootContainer.add(it.padding(top = 4, left = 10, right = 10, bottom = 4))
    }

    private val buttonPanel = JPanel().also {
        it.layout = BoxLayout(it, BoxLayout.X_AXIS)
        rootContainer.add(it.padding(bottom = 12, left = 10, right = 10))
    }
    private val importButton = JButton("Import").also {
        buttonPanel.add(it)
        //TODO
    }
    private val exportButton = JButton("Export").also {
        buttonPanel.add(it.padding(left = 10))
        buttonPanel.add(Box.createHorizontalGlue())
        it.addActionListener { showExportDialog() }
    }
    private val cancelButton = JButton("Cancel").also {
        buttonPanel.add(it)
        it.addActionListener {
            dispose()
        }
    }
    private val okButton = JButton("OK").also {
        buttonPanel.add(it)
        it.addActionListener {
            val finalMappings = mappings
            response = MapLocalConfiguration(allEnabled, fileResolver.unResolveAll(finalMappings))
            dispose()
        }
    }
    private val addButton = JButton("Add").also {
        actionButtonPanel.add(it)
        it.addActionListener {
            val newMapping = EditLocalMappingDialog.show(this, null, project) ?: return@addActionListener

            mappings.add(newMapping)
            (mappingTable.model as DefaultTableModel).addRow(arrayOf(true, newMapping.location.asString(), fileResolver.unResolve(newMapping.destination)))
//            onItemUpdated(item, copy)
        }
    }
    private val removeButton = JButton("Remove").also {
        actionButtonPanel.add(it)
        it.addActionListener { _ ->
            mappingTable.selectedRows.sortedDescending().forEach { index ->
                mappings.removeAt(index)
                (mappingTable.model as DefaultTableModel).removeRow(index)
            }
            it.isEnabled = false
        }
    }
    private val upButton = JButton("Up").also {
        actionButtonPanel.add(it)
        //TODO
    }
    private val downButton = JButton("Down").also {
        actionButtonPanel.add(it)
        //TODO
    }

    var allEnabled: Boolean = false
        set(value) {
            field = value
            enableAllToggle.isSelected = value

            mappingTable.isEnabled = value

            val selectedIndices = mappingTable.selectedRowCount
            val enableExportAndRemove = value && selectedIndices > 0
            exportButton.isEnabled = enableExportAndRemove
            removeButton.isEnabled = enableExportAndRemove
            addButton.isEnabled = value
        }

    init {
        contentPane = rootContainer
        rootPane.defaultButton = okButton

        val screenSize = Toolkit.getDefaultToolkit().screenSize

        val minSize = Dimension(screenSize.width / 3, screenSize.height / 3)
        minimumSize = minSize
        size = minSize

        ProjectConfig.load<MapLocalConfiguration>(project, ProjectConfig.CONFIG_MAPLOCAL)?.let {
            val config = it.copy(mappings = it.mappings.createIds())
            mappings.addAll(it.mappings.createIds())
            mappingTable.isEnabled = config.enabled
            addButton.isEnabled = config.enabled
            removeButton.isEnabled = false
            allEnabled = it.enabled

            mappings.forEach { entry ->
                (mappingTable.model as DefaultTableModel).addRow(arrayOf(entry.enabled, entry.location.asString(), fileResolver.unResolve(entry.destination)))
            }
        }
    }

    private fun addNewRuleFor(message: NiddlerMessageInfo) {
        val entry = MapLocalEntry(
            enabled = true,
            location = createRewriteLocationFor(message),
            caseSensitive = true,
            destination = "",
            id = UUID.randomUUID().toString()
        )

        mappings.add(entry)
        (mappingTable.model as DefaultTableModel).addRow(arrayOf(entry.enabled, entry.location.asString(), fileResolver.unResolve(entry.destination)))
    }

    private fun showExportDialog() {
        val items = mappingTable.selectedRows.map { mappings[it] }
        if (items.isEmpty()) return

        var file = chooseSaveFile("Export to", ".xml") ?: return
        if (file.extension.isEmpty()) {
            file = File(file.absolutePath + ".xml")
        }
        file.outputStream().use { MapLocalExporter().export(MapLocalConfiguration(allEnabled, items), fileResolver, it) }
        NotificationUtil.info("Rule export complete", "<html>Rule export completed to <a href=\"file://${file.absolutePath}\">${file.name}</a></html>", project)
    }

}

private fun List<MapLocalEntry>.createIds(): List<MapLocalEntry> {
    return map { it.copy(id = UUID.randomUUID().toString()) }
}

class ProjectFileResolver(project: Project) : VariableFileResolver(linkedMapOf()) {

    init {
        project.guessProjectDir()?.path?.let { path ->
            mapping["projectDir"] = path.cleanPath()
        }
        System.getProperty("user.home")?.let {
            mapping["user.home"] = it.cleanPath()
        }
    }

    fun unResolve(toUnResolve: String): String {
        var variable = toUnResolve
        mapping.forEach { (key, value) ->
            variable = replacePrefix(variable, variableName = key, prefix = value)
        }
        return variable
    }

    fun unResolveAll(mappings: MutableList<MapLocalEntry>): List<MapLocalEntry> {
        return mappings.map { it.copy(destination = unResolve(it.destination), id = UUID.randomUUID().toString()) }
    }

}

private fun String.cleanPath(): String {
    val path = File(this).absolutePath
    if (path.endsWith(File.pathSeparatorChar)) {
        return path.substring(1, path.length - 1)
    }
    return path
}
