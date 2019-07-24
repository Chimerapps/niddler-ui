package com.chimerapps.niddler.ui;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * From https://stackoverflow.com/questions/8076794/create-a-titled-border-with-the-title-as-a-jcheckbox
 * and https://www.javalobby.org//java/forums/t33048.html
 */
public class CheckboxBorder extends AbstractBorder {

    private final TitledBorder _parent;
    private final JCheckBox _checkBox;

    public CheckboxBorder(String title, boolean selected) {
        _parent = BorderFactory.createTitledBorder(title);
        _checkBox = new JCheckBox(title, selected);
        _checkBox.setHorizontalTextPosition(SwingConstants.LEFT);
    }

    public boolean isSelected() {
        return _checkBox.isSelected();
    }

    public void addActionListener(ActionListener listener) {
        _checkBox.addActionListener(listener);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Insets borderInsets = _parent.getBorderInsets(c);
        Insets insets = getBorderInsets(c);
        int temp = (insets.top - borderInsets.top) / 2;
        _parent.paintBorder(c, g, x, y + temp, width, height - temp);
        Dimension size = _checkBox.getPreferredSize();
        final Rectangle rectangle = new Rectangle(5, 0, size.width, size.height);

        final Container container = (Container) c;
        container.addMouseListener(new MouseAdapter() {
            private void dispatchEvent(MouseEvent me) {
                if (rectangle.contains(me.getX(), me.getY())) {
                    Point pt = me.getPoint();
                    pt.translate(-5, 0);
                    _checkBox.setBounds(rectangle);
                    _checkBox.dispatchEvent(new MouseEvent(_checkBox, me.getID(),
                            me.getWhen(), me.getModifiers(), pt.x, pt.y, me.getClickCount(), me.isPopupTrigger(), me.getButton()));
                    if (!_checkBox.isValid()) {
                        container.repaint();
                    }
                }
            }

            public void mousePressed(MouseEvent me) {
                dispatchEvent(me);
            }

            public void mouseReleased(MouseEvent me) {
                dispatchEvent(me);
            }
        });
        SwingUtilities.paintComponent(g, _checkBox, container, rectangle);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        Insets insets = _parent.getBorderInsets(c);
        insets.top = Math.max(insets.top, _checkBox.getPreferredSize().height);
        return insets;
    }
}