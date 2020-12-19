/*
 * Copyright (c) 2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 *
 * The table layout manager is based on JTable (copyright notice below).
 */

package org.violetlib.vaquaclient;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.plaf.ComponentUI;

import org.jetbrains.annotations.*;

/**
  Assemble a horizontal collection of buttons with the appropriate client properties to be displayed as a segmented
  control when using the Aqua or VAqua look and feel on macOS. In other circumstances, ordinary buttons will be used and
  separated horizontally by a specified distance. The displayed order depends upon the component orientation and is
  updated automatically.
*/

public class SegmentedControlBuilder
{
    public static final @NotNull String TEXTURED = "textured";
    public static final @NotNull String SEPARATED = "separated";
    public static final @NotNull String TEXTURED_SEPARATED = "texturedSeparated";
    public static final @NotNull String ROUND_RECT = "roundRect";
    public static final @NotNull String GRADIENT = "gradient";

    public static final @NotNull String LARGE = "large";
    public static final @NotNull String REGULAR = "regular";
    public static final @NotNull String SMALL = "small";
    public static final @NotNull String MINI = "mini";

    private final @NotNull String buttonType;
    private final boolean isExclusive;
    private final @Nullable String controlSize;
    private final @NotNull List<AbstractButton> buttons = new ArrayList<>();
    private boolean isClosed;
    private final int defaultSeparation;

    /**
      Create a builder of a segmented control. The builder can be used only once.
      @param style The segmented control style, or null to use a default style.
      The supported styles are:
      {@link #TEXTURED},
      {@link #SEPARATED},
      {@link #TEXTURED_SEPARATED},
      {@link #ROUND_RECT},
      and {@link #GRADIENT}.
      See the <a href="https://violetlib.org/vaqua/segmentedbuttons.html">VAqua documentation</a> for information on the
      these styles.
      @param isExclusive True to ensure that exactly one button is selected at any time; false to allow any number of
      buttons to be selected or to allow ordinary push buttons. When this parameter is true, only toggle buttons may
      be added to the control; otherwise, this parameter has no effect if there are fewer than two buttons.
      @param controlSize A specification of the control size. Available sizes are
      {@link #LARGE}, {@link #REGULAR}, {@link #SMALL}, and {@link #MINI}. The supported combinations of style and
      size depend upon the macOS release.
      If null, the regular size is used.
      @param defaultSeparation The horizontal separation between buttons to use when not using the Aqua or VAqua look
      and feel.
    */

    public SegmentedControlBuilder(@Nullable String style,
                                   boolean isExclusive,
                                   @Nullable String controlSize,
                                   int defaultSeparation)
    {
        this.buttonType = getButtonStyle(style);
        this.isExclusive = isExclusive;
        this.controlSize = controlSize != null ? controlSize : "regular";
        this.defaultSeparation = Math.max(0, defaultSeparation);
    }

    private @NotNull String getButtonStyle(@Nullable String controlStyle)
    {
        if (controlStyle == null) {
            return "segmented";
        }
        if (controlStyle.equals(TEXTURED)) {
            return "segmentedTextured";
        }
        if (controlStyle.equals(SEPARATED)) {
            return "segmentedSeparated";
        }
        if (controlStyle.equals(TEXTURED_SEPARATED)) {
            return "segmentedTexturedSeparated";
        }
        if (controlStyle.equals(ROUND_RECT)) {
            return "segmentedRoundRect";
        }
        if (controlStyle.equals(GRADIENT)) {
            return "segmentedGradient";
        }
        throw new IllegalArgumentException("Unrecognized segmented control style: " + controlStyle);
    }

    /**
      Add a push button to the collection. This operation is not supported if the control is exclusive.
    */

    public void add(@NotNull JButton b)
    {
        if (isExclusive) {
            throw new IllegalStateException("Only toggle buttons are supported in an exclusive control");
        }
        if (isClosed) {
            throw new IllegalStateException("Builder has been closed");
        }
        buttons.remove(b);
        buttons.add(b);
        b.putClientProperty("JButton.buttonType", buttonType);
        b.putClientProperty("JComponent.sizeVariant", controlSize);
    }

    /**
      Add a toggle button to the collection.
    */

    public void add(@NotNull JToggleButton b)
    {
        if (isClosed) {
            throw new IllegalStateException("Builder has been closed");
        }
        buttons.remove(b);
        buttons.add(b);
        b.putClientProperty("JButton.buttonType", buttonType);
        b.putClientProperty("JComponent.sizeVariant", controlSize);
    }

    /**
      Create and return the segmented control component.
    */

    public @NotNull JPanel build()
    {
        if (isClosed) {
            throw new IllegalStateException("Builder has been closed");
        }
        isClosed = true;

        return new SegmentedControl(buttons, isExclusive, defaultSeparation);
    }

    private static class SegmentedControl
      extends JPanel
    {
        private final @NotNull List<AbstractButton> buttons;
        private final @Nullable ButtonGroup group;
        private final int defaultSeparation;
        private boolean isSegmented;

        public SegmentedControl(@NotNull List<AbstractButton> buttons, boolean isExclusive, int defaultSeparation)
        {
            this.buttons = buttons;
            if (isExclusive && buttons.size() > 1) {
                group = new ButtonGroup();
                for (AbstractButton b : buttons) {
                    group.add(b);
                }
            } else {
                group = null;
            }

            this.defaultSeparation = defaultSeparation;
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            isSegmented = isSegmentedSupported();
            installButtons();
            addPropertyChangeListener("UI", this::updateForUI);
            addPropertyChangeListener("componentOrientation", this::updateForComponentOrientation);
            setOpaque(false);
        }

        private void updateForUI(@Nullable PropertyChangeEvent e)
        {
            boolean isSupported = isSegmentedSupported();
            if (isSupported != isSegmented) {
                isSegmented = isSupported;
                installButtons();
            }
        }

        private void updateForComponentOrientation(@Nullable PropertyChangeEvent e)
        {
            isSegmented = isSegmentedSupported();
            installButtons();
        }

        private void installButtons()
        {
            removeAll();

            List<AbstractButton> buttonsToAdd = buttons;

            if (!getComponentOrientation().isLeftToRight()) {
                buttonsToAdd = new ArrayList<>(buttonsToAdd);
                Collections.reverse(buttonsToAdd);
            }

            int count = buttonsToAdd.size();
            if (count == 1) {
                AbstractButton b = buttonsToAdd.get(0);
                b.putClientProperty("JButton.segmentPosition", "only");
            } else {
                for (int i = 0; i < count; i++) {
                    AbstractButton b = buttonsToAdd.get(i);
                    String position = "middle";
                    if (i == 0) {
                        position = "first";
                    } else if (i == count-1) {
                        position = "last";
                    }
                    b.putClientProperty("JButton.segmentPosition", position);
                }
            }

            if (group != null) {
                int selectedCount = getSelectedCount(buttonsToAdd);
                if (selectedCount == 0) {
                    buttonsToAdd.get(0).setSelected(true);
                }
            }

            boolean isFirst = true;
            for (AbstractButton b : buttonsToAdd) {
                if (!isFirst && !isSegmented) {
                    add(Box.createHorizontalStrut(defaultSeparation));
                }
                isFirst = false;
                add(b);
            }
            revalidate();
            repaint();
        }

        private int getSelectedCount(@NotNull List<AbstractButton> buttons)
        {
            int count = 0;
            for (AbstractButton b : buttons) {
                if (b.isSelected()) {
                    count++;
                }
            }
            return count;
        }

        private boolean isSegmentedSupported()
        {
            ComponentUI ui = getUI();
            if (ui != null) {
                String className = ui.getClass().getName();
                return className.startsWith("com.apple.laf.") || className.startsWith("org.violetlib.aqua.");
            }
            return false;
        }
    }
}
