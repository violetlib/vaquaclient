/*
 * Copyright (c) 2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.vaquaclient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.PanelUI;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
  A container that supports use of a specific appearance, which may be different than the appearance used by its
  ancestors.
*/

public class JAppearancePanel
  extends JPanel
{
    private static final String uiClassID = "AppearancePanelUI";

    private boolean paintingAppearanceBackground;

    @Override
    public void updateUI()
    {
        super.updateUI();
    }

    @Override
    public @NotNull String getUIClassID()
    {
        if (UIManager.get(uiClassID) != null) {
            return uiClassID;
        }
        return super.getUIClassID();
    }

    @Override
    protected boolean isPaintingOrigin()
    {
        return true;
    }

    @Override
    public void paint(Graphics g)
    {
        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }

        paintingAppearanceBackground = true;
        Object o = getClientProperty("Aqua.appearanceName");
        if (o instanceof String) {
            String appearanceName = (String) o;
            PanelUI ui = getUI();
            try {
                Consumer<Color> r = (color) -> paintWithWindowBackground(g, color);
                Method m = ui.getClass().getMethod("paint", JComponent.class, String.class, Consumer.class);
                m.invoke(ui, this, appearanceName, r);
                return;
            } catch (Exception ignore) {
            }
        }

        paintingAppearanceBackground = false;
        basicPaint(g);
    }

    private void paintWithWindowBackground(@NotNull Graphics g, @Nullable Color color)
    {
        boolean wasOpaque = isOpaque();
        if (color != null) {
            setOpaque(false);  // avoid painting the component background
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            paintingAppearanceBackground = false;
        }
        basicPaint(g);
        setOpaque(wasOpaque);
    }

    private void basicPaint(@NotNull Graphics g)
    {
        super.paint(g);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        if (!paintingAppearanceBackground) {
            super.paintComponent(g);
        }
    }
}
