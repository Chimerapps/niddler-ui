package com.chimerapps.niddler.ui.debugging.maplocal

import com.chimerapps.niddler.ui.debugging.rewrite.EditableTableModel
import com.chimerapps.niddler.ui.debugging.rewrite.PackingJBTable
import com.chimerapps.niddler.ui.debugging.rewrite.location.EditLocationDialog
import com.chimerapps.niddler.ui.model.ProjectConfig
import com.chimerapps.niddler.ui.util.ui.CheckBox
import com.chimerapps.niddler.ui.util.ui.setColumnFixedWidth
import com.icapps.niddler.lib.debugger.model.maplocal.MapLocalConfiguration
import com.icapps.niddler.lib.debugger.model.maplocal.MapLocalEntry
import com.icapps.niddler.lib.model.NiddlerMessageInfo
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.Window
import java.util.UUID
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.ListSelectionModel


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
class MapLocalDialog(parent: Window?, project: Project) : JDialog(parent, "MapLocal settings", ModalityType.APPLICATION_MODAL) {

    companion object {
        fun show(parent: Window?, project: Project): MapLocalConfiguration? {
            val dialog = MapLocalDialog(parent, project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

            dialog.isVisible = true
            return dialog.response
        }

        fun showAdd(parent: Window?, project: Project, message: NiddlerMessageInfo): MapLocalConfiguration? {
            val dialog = MapLocalDialog(parent, project)
            if (dialog.parent != null)
                dialog.setLocationRelativeTo(parent)

//            dialog.addNewRuleFor(message) TODO

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
//            onItemUpdated(item, copy) TODO
        }
    }, onRowDoubleClicked = { row, model ->
        val item = mappings.getOrNull(row) ?: return@PackingJBTable
        //TODO
//        val edited = EditLocationDialog.show(parentWindow, item.locations[row].location)
//            ?: return@PackingJBTable
//        val locationsCopy = item.locations.toMutableList()
//        locationsCopy[row] = locationsCopy[row].copy(location = edited)
//        val copy = item.copy(locations = locationsCopy)
//        (model as DefaultTableModel).setValueAt(edited.asString(), row, 1)
//        _currentItemInternal = copy
//        onItemUpdated(item, copy)
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
        //TODO
    }
    private val cancelButton = JButton("Cancel").also {
        buttonPanel.add(it)
        it.addActionListener {
            dispose()
        }
    }
    private val okButton = JButton("OK").also {
        buttonPanel.add(it)
//        it.addActionListener {
//            for (i in 0 until rules.size) {
//                rules[i] = rules[i].copy(active = masterPanel.isRewriteSetEnabled(rules[i]))
//            }
//            response = RewriteConfig(masterPanel.allEnabled, rules)
//            dispose()
//        }
    }
    private val addButton = JButton("Add").also {
        actionButtonPanel.add(it)
        it.addActionListener {
            val newLocation = EditLocalMappingDialog.show(this, null, project) ?: return@addActionListener
        }
    }
    private val removeButton = JButton("Remove").also {
        actionButtonPanel.add(it)
        //TODO
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
            mappingTable.isEnabled = config.enabled
//            masterPanel.addRewriteSets(config.sets, selectLast = false)
        }
    }
}

private fun List<MapLocalEntry>.createIds(): List<MapLocalEntry> {
    return map { it.copy(id = UUID.randomUUID().toString()) }
}