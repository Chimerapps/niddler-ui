package com.chimerapps.discovery.ui

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

private fun Any.loadIcon(path: String): Icon {
    return IconLoader.getIcon(path, javaClass)
}

object IncludedLibIcons {

    private val supportsSvg = ApplicationInfo.getInstance().build.baselineVersion >= 182 //2018.2
    private val hasSvgExtension = if (supportsSvg) ".svg" else ".png"

    object Devices {
        val computer = loadIcon("/ic_device_computer$hasSvgExtension")
        val emulator = loadIcon("/ic_device_emulator$hasSvgExtension")
        val real = loadIcon("/ic_device_real$hasSvgExtension")
        val realApple = loadIcon("/ic_device_real_apple$hasSvgExtension")
    }
}