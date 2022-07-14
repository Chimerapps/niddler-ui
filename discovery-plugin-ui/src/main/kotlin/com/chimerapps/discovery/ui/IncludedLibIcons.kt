package com.chimerapps.discovery.ui

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

private fun Any.loadIcon(path: String): Icon {
    return IconLoader.getIcon(path, javaClass)
}

object IncludedLibIcons {

    object Devices {
        val computer = loadIcon("/ic_device_computer.svg")
        val emulator = loadIcon("/ic_device_emulator.svg")
        val real = loadIcon("/ic_device_real.svg")
        val realApple = loadIcon("/ic_device_real_apple.svg")
        val realTizen = loadIcon("/ic_device_tizen.svg")
    }

    object Icons {
        val android = loadIcon("/ic_icon_android.svg")
        val apple = loadIcon("/ic_icon_apple.svg")
        val dart = loadIcon("/ic_icon_dart.svg")
        val flutter = loadIcon("/ic_icon_flutter.svg")
    }
}