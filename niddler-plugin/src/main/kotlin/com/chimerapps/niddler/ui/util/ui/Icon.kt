package com.chimerapps.niddler.ui.util.ui

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

private fun Any.loadIcon(path: String): Icon {
    return IconLoader.getIcon(path, javaClass)
}

object IncludedIcons {

    val supportsSvg = ApplicationInfo.getInstance().build.baselineVersion >= 182 //2018.2
    private val isFlat = supportsSvg //also 2018.2
    private val hasSvgExtension = if (supportsSvg) ".svg" else ".png"

    object Types {
        val boolean = loadIcon("/ic_boolean$hasSvgExtension")
        val double = loadIcon("/ic_double$hasSvgExtension")
        val int = loadIcon("/ic_int$hasSvgExtension")
        val string = loadIcon("/ic_string$hasSvgExtension")
    }

    object Status {
        val connected = loadIcon("/ic_connected$hasSvgExtension")
        val disconnected = loadIcon("/ic_disconnected$hasSvgExtension")

        val incoming = loadIcon("/ic_down_wide$hasSvgExtension")
        val incoming_cached = loadIcon("/ic_down_wide_cache$hasSvgExtension")
        val outgoing = loadIcon("/ic_up_wide$hasSvgExtension")
        val logo = loadIcon("/niddler_logo.png")
        val incoming_debugged = loadIcon("/ic_down_wide_debug$hasSvgExtension")
        val outgoing_debugged = loadIcon("/ic_up_wide_debug$hasSvgExtension")
    }

    object Action {
        val pretty = loadIcon("/ic_pretty$hasSvgExtension")
        val chronological = if (isFlat)
            loadIcon("/ic_chronological_flat$hasSvgExtension")
        else
            loadIcon("/ic_chronological_no_flat$hasSvgExtension")
    }

}