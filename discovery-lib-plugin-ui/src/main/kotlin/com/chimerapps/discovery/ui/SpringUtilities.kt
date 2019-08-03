package com.chimerapps.discovery.ui

import java.awt.Component
import java.awt.Container
import javax.swing.Spring
import javax.swing.SpringLayout

/**
 * A 1.4 file that provides utility methods for
 * creating form- or grid-style layouts with SpringLayout.
 * These utilities are used by several programs, such as
 * SpringBox and SpringCompactGrid.
 */
object SpringUtilities {
    /**
     * A debugging utility that prints to stdout the component's
     * minimum, preferred, and maximum sizes.
     */
    fun printSizes(c: Component) {
        println("minimumSize = " + c.minimumSize)
        println("preferredSize = " + c.preferredSize)
        println("maximumSize = " + c.maximumSize)
    }

    /**
     * Aligns the first `rows` * `cols`
     * components of `parent` in
     * a grid. Each component is as big as the maximum
     * preferred width and height of the components.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    fun makeGrid(parent: Container,
                 rows: Int, cols: Int,
                 initialX: Int, initialY: Int,
                 xPad: Int, yPad: Int) {
        val layout: SpringLayout
        try {
            layout = parent.layout as SpringLayout
        } catch (exc: ClassCastException) {
            System.err.println("The first argument to makeGrid must use SpringLayout.")
            return
        }

        val xPadSpring = Spring.constant(xPad)
        val yPadSpring = Spring.constant(yPad)
        val initialXSpring = Spring.constant(initialX)
        val initialYSpring = Spring.constant(initialY)
        val max = rows * cols

        //Calculate Springs that are the max of the width/height so that all
        //cells have the same size.
        var maxWidthSpring = layout.getConstraints(parent.getComponent(0)).width
        var maxHeightSpring = layout.getConstraints(parent.getComponent(0)).height
        for (i in 1 until max) {
            val cons = layout.getConstraints(
                    parent.getComponent(i))

            maxWidthSpring = Spring.max(maxWidthSpring, cons.width)
            maxHeightSpring = Spring.max(maxHeightSpring, cons.height)
        }

        //Apply the new width/height Spring. This forces all the
        //components to have the same size.
        for (i in 0 until max) {
            val cons = layout.getConstraints(
                    parent.getComponent(i))

            cons.width = maxWidthSpring
            cons.height = maxHeightSpring
        }

        //Then adjust the x/y constraints of all the cells so that they
        //are aligned in a grid.
        var lastCons: SpringLayout.Constraints? = null
        var lastRowCons: SpringLayout.Constraints? = null
        for (i in 0 until max) {
            val cons = layout.getConstraints(
                    parent.getComponent(i))
            if (i % cols == 0) { //start of new row
                lastRowCons = lastCons
                cons.x = initialXSpring
            } else { //x position depends on previous component
                cons.x = Spring.sum(lastCons!!.getConstraint(SpringLayout.EAST),
                        xPadSpring)
            }

            if (i / cols == 0) { //first row
                cons.y = initialYSpring
            } else { //y position depends on previous row
                cons.y = Spring.sum(lastRowCons!!.getConstraint(SpringLayout.SOUTH),
                        yPadSpring)
            }
            lastCons = cons
        }

        //Set the parent's size.
        val pCons = layout.getConstraints(parent)
        pCons.setConstraint(SpringLayout.SOUTH,
                Spring.sum(
                        Spring.constant(yPad),
                        lastCons!!.getConstraint(SpringLayout.SOUTH)))
        pCons.setConstraint(SpringLayout.EAST,
                Spring.sum(
                        Spring.constant(xPad),
                        lastCons.getConstraint(SpringLayout.EAST)))
    }

    /* Used by makeCompactGrid. */
    private fun getConstraintsForCell(
            row: Int, col: Int,
            parent: Container,
            cols: Int): SpringLayout.Constraints {
        val layout = parent.layout as SpringLayout
        val c = parent.getComponent(row * cols + col)
        return layout.getConstraints(c)
    }

    /**
     * Aligns the first `rows` * `cols`
     * components of `parent` in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    fun makeCompactGrid(parent: Container,
                        rows: Int, cols: Int,
                        initialX: Int, initialY: Int,
                        xPad: Int, yPad: Int) {
        val layout: SpringLayout
        try {
            layout = parent.layout as SpringLayout
        } catch (exc: ClassCastException) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.")
            return
        }

        //Align all cells in each column and make them the same width.
        var x = Spring.constant(initialX)
        for (c in 0 until cols) {
            var width = Spring.constant(0)
            for (r in 0 until rows) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent, cols).width)
            }
            for (r in 0 until rows) {
                val constraints = getConstraintsForCell(r, c, parent, cols)
                constraints.x = x
                constraints.width = width
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)))
        }

        //Align all cells in each row and make them the same height.
        var y = Spring.constant(initialY)
        for (r in 0 until rows) {
            var height = Spring.constant(0)
            for (c in 0 until cols) {
                height = Spring.max(height,
                        getConstraintsForCell(r, c, parent, cols).height)
            }
            for (c in 0 until cols) {
                val constraints = getConstraintsForCell(r, c, parent, cols)
                constraints.y = y
                constraints.height = height
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)))
        }

        //Set the parent's size.
        val pCons = layout.getConstraints(parent)
        pCons.setConstraint(SpringLayout.SOUTH, y)
        pCons.setConstraint(SpringLayout.EAST, x)
    }
}