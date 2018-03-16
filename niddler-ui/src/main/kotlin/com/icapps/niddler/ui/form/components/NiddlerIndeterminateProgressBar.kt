package com.icapps.niddler.ui.form.components

import com.icapps.niddler.ui.hexToColor
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JPanel
import javax.swing.Timer

class NiddlerIndeterminateProgressBar : JPanel() {

    private val progressColor = "#4F8AE9".hexToColor()
    private val progressBarSize: Int = 25
    private var changeDirection: Boolean = false
    private var positionX = 0

    private val indeterminateTimer = Timer(20, object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            repaint()
        }
    })

    override fun paintComponent(graphics: Graphics?) {
        super.paintComponent(graphics)
        val g2d = graphics as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.color = progressColor
        if (changeDirection) {
            if (changeDirection) {
                if (positionX + 5 > 0) {
                    positionX -= 5
                } else {
                    changeDirection = false
                }
            }
        } else {
            if (positionX + 5 < (width - progressBarSize)) {
                positionX += 5
            } else {
                changeDirection = true
            }
        }
        g2d.fillRect(positionX, 0, progressBarSize, height)
        g2d.dispose()
    }

    fun start() {
        isVisible = true
        if (!indeterminateTimer.isRunning) {
            indeterminateTimer.start()
        }
    }

    fun stop() {
        isVisible = false
        if (indeterminateTimer.isRunning){
            indeterminateTimer.stop()
        }
    }
}