package com.chimerapps.niddler.ui.util.ui

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

private fun Any.loadIcon(path: String): Icon {
    return IconLoader.getIcon(path, javaClass)
}

object IncludedIcons {

    object Types {
        val boolean = loadIcon("/ic_boolean.svg")
        val double = loadIcon("/ic_double.svg")
        val int = loadIcon("/ic_int.svg")
        val string = loadIcon("/ic_string.svg")
    }

    object Status {
        val connected = loadIcon("/ic_connected.svg")
        val disconnected = loadIcon("/ic_disconnected.svg")

        val incoming = loadIcon("/ic_down_wide.svg")
        val incoming_cached = loadIcon("/ic_down_wide_cache.svg")
        val outgoing = loadIcon("/ic_up_wide.svg")
        val logo = loadIcon("/niddler_logo.svg")
        val incoming_debugged = loadIcon("/ic_down_wide_debug.svg")
        val outgoing_debugged = loadIcon("/ic_up_wide_debug.svg")
    }

    object Action {
        val pretty = loadIcon("/ic_pretty.svg")
        val chronological = loadIcon("/ic_chronological_flat.svg")
        val localMapping = loadIcon("/ic_map_local.svg")
        val enableWifi = loadIcon("/ic_wifi_off.svg")
        val disableWifi = loadIcon("/ic_wifi_on.svg")
    }

}