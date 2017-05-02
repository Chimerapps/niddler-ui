package com.icapps.niddler.ui.util

import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.util.*
import javax.swing.Icon
import javax.swing.UIManager

/**
 * @author Nicola Verbeeck
 * @date 02/05/2017.
 */
object UIUtil {

    val DECORATED_ROW_BG_COLOR: Color = Color(242, 245, 249)

    fun getDecoratedRowColor(): Color {
        return DECORATED_ROW_BG_COLOR
    }

    fun isUnderAquaLookAndFeel(): Boolean {
        return SystemInfo.isMac && UIManager.getLookAndFeel().name.contains("Mac OS X")
    }

    fun isUnderIntelliJLaF(): Boolean {
        return UIManager.getLookAndFeel().name.contains("IntelliJ")
    }

    fun isUnderAquaBasedLookAndFeel(): Boolean {
        return SystemInfo.isMac && (isUnderAquaLookAndFeel() || isUnderDarcula() || isUnderIntelliJLaF())
    }

    fun isUnderDarcula(): Boolean {
        return UIManager.getLookAndFeel().name.contains("Darcula")
    }

    fun isUnderNimbusLookAndFeel(): Boolean {
        return UIManager.getLookAndFeel().name.contains("Nimbus")
    }

    fun isUnderGTKLookAndFeel(): Boolean {
        return SystemInfo.isXWindow && UIManager.getLookAndFeel().name.contains("GTK")
    }

    fun getTreeNodeIcon(expanded: Boolean, selected: Boolean, focused: Boolean): Icon {
        val white = selected && focused || isUnderDarcula()
        val selectedIcon = getTreeSelectedExpandedIcon()
        val notSelectedIcon = getTreeExpandedIcon()
        val width = Math.max(selectedIcon.iconWidth, notSelectedIcon.iconWidth)
        val height = Math.max(selectedIcon.iconWidth, notSelectedIcon.iconWidth)
        return CenteredIcon(if (expanded) if (white) getTreeSelectedExpandedIcon() else getTreeExpandedIcon() else if (white) getTreeSelectedCollapsedIcon() else getTreeCollapsedIcon(), width, height, false)
    }

    fun getTreeSelectedCollapsedIcon(): Icon {
        return getTreeCollapsedIcon()
    }

    fun getTreeSelectedExpandedIcon(): Icon {
        return getTreeExpandedIcon()
    }


    fun getTreeCollapsedIcon(): Icon {
        return UIManager.getIcon("Tree.collapsedIcon")
    }

    fun getTreeExpandedIcon(): Icon {
        return UIManager.getIcon("Tree.expandedIcon")
    }

    fun getTreeSelectionBackground(): Color {
        if (isUnderNimbusLookAndFeel()) {
            var color: Color? = UIManager.getColor("Tree.selectionBackground")
            if (color != null) {
                return color
            }

            color = UIManager.getColor("nimbusSelectionBackground")
            if (color != null) {
                return color
            }
        }

        return UIManager.getColor("Tree.selectionBackground")
    }

    fun getTreeSelectionBackground(focused: Boolean): Color {
        return if (focused) getTreeSelectionBackground() else getTreeUnfocusedSelectionBackground()
    }

    fun getTreeTextBackground(): Color {
        return UIManager.getColor("Tree.textBackground")
    }

    fun getTreeUnfocusedSelectionBackground(): Color {
        val background = getTreeTextBackground()
        return Color(30, 30, 30)
    }

}

object SystemInfo {

    private val OS_VERSION = System.getProperty("os.version").toLowerCase(Locale.US)
    private var OS_NAME = System.getProperty("os.name")
    val _OS_NAME = OS_NAME.toLowerCase(Locale.US)
    val isWindows = _OS_NAME.startsWith("windows")
    val isOS2 = _OS_NAME.startsWith("os/2") || _OS_NAME.startsWith("os2")
    val isMac = _OS_NAME.startsWith("mac")
    val isLinux = _OS_NAME.startsWith("linux")
    val isFreeBSD = _OS_NAME.startsWith("freebsd")
    val isSolaris = _OS_NAME.startsWith("sunos")
    val isUnix = !isWindows && !isOS2
    val isXWindow = isUnix && !isMac
    val isFileSystemCaseSensitive = isUnix && !isMac || "true".equals(System.getProperty("idea.case.sensitive.fs"), ignoreCase = true)

}

class CenteredIcon @JvmOverloads constructor(private val myIcon: Icon, private val myWidth: Int = myIcon.iconWidth, private val myHight: Int = myIcon.iconHeight, private val myCenteredInComponent: Boolean = true) : Icon {

    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        val offsetX: Int
        val offsetY: Int
        if (this.myCenteredInComponent) {
            val size = c.size
            offsetX = size.width / 2 - this.myIcon.iconWidth / 2
            offsetY = size.height / 2 - this.myIcon.iconHeight / 2
        } else {
            offsetX = (this.myWidth - this.myIcon.iconWidth) / 2
            offsetY = (this.myHight - this.myIcon.iconHeight) / 2
        }

        this.myIcon.paintIcon(c, g, x + offsetX, y + offsetY)
    }

    override fun getIconWidth(): Int {
        return this.myWidth
    }

    override fun getIconHeight(): Int {
        return this.myHight
    }
}
