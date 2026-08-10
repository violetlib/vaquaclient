/*
 * Copyright (c) 2020-2023 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 *
 * The table layout manager is based on JTable (copyright notice below).
 */

/*
 * Copyright (c) 1997, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.violetlib.vaquaclient;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jetbrains.annotations.*;

/**
  An extended JTable that knows about VAqua's inset view mode and adjusts the table margins accordingly. Note that
  when margins are installed, it may be necessary to increase the preferred size of the table. See the
  {@link #marginChanged} method.
*/

public class JTableWithMargins
  extends JTable
{
    public static final String INSET_VIEW_MARGIN_KEY = "Aqua.insetViewMargin";
    public static final String INSET_VIEW_VERTICAL_MARGIN_KEY = "Aqua.insetViewVerticalMargin";

    private final TableLayoutManager layoutManager = new TableLayoutManager(this);
    private final MyPropertyChangeListener propertyChangeListener = new MyPropertyChangeListener();

    private int margin;
    private int verticalMargin;

    public JTableWithMargins()
    {
        initialize();
    }

    public JTableWithMargins(TableModel dm)
    {
        super(dm);
        initialize();
    }

    public JTableWithMargins(TableModel dm, TableColumnModel cm)
    {
        super(dm, cm);
        initialize();
    }

    public JTableWithMargins(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
    {
        super(dm, cm, sm);
        initialize();
    }

    public JTableWithMargins(int numRows, int numColumns)
    {
        super(numRows, numColumns);
        initialize();
    }

    public JTableWithMargins(Vector<? extends Vector> rowData, Vector<?> columnNames)
    {
        super(rowData, columnNames);
        initialize();
    }

    public JTableWithMargins(@NotNull Object[][] rowData, @NotNull Object[] columnNames)
    {
        super(rowData, columnNames);
        initialize();
    }

    private void initialize()
    {
        addPropertyChangeListener(propertyChangeListener);
        updateMargins();
    }

    private void updateMargins()
    {
        int margin = getSpecifiedMargin();
        int verticalMargin = getSpecifiedVerticalMargin();
        if (margin != this.margin || verticalMargin != this.verticalMargin) {
            installMargin(margin, verticalMargin);
        }
    }

    private void installMargin(int m, int vm)
    {
        margin = m;
        verticalMargin = vm;
        revalidate();
        repaint();
        JTableHeader header = getTableHeader();
        if (header != null) {
            header.revalidate();
            header.repaint();
        }
        marginChanged(margin, verticalMargin);
    }

    protected void marginChanged(int margin, int verticalMargin)
    {
    }

    @Override
    public @NotNull Dimension getMinimumSize()
    {
        Dimension d = super.getMinimumSize();
        if (isMinimumSizeSet()) {
            return d;
        }
        return fixSize(d);
    }

    @Override
    public @NotNull Dimension getPreferredSize()
    {
        Dimension d = super.getPreferredSize();
        if (isPreferredSizeSet()) {
            return d;
        }
        return fixSize(d);
    }

    @Override
    public @NotNull Dimension getMaximumSize()
    {
        Dimension d = super.getMaximumSize();
        if (isMaximumSizeSet()) {
            return d;
        }
        return fixSize(d);
    }

    private @NotNull Dimension fixSize(@NotNull Dimension d)
    {
        int specifiedMargin = getSpecifiedMargin();
        int specifiedVerticalMargin = getSpecifiedVerticalMargin();
        if (specifiedMargin == 0 && specifiedVerticalMargin == 0) {
            return d;
        }
        // VAqua will return the correct width but only when it installs the margins.
        if (isVAquaInsetView()) {
            return d;
        }
        return new Dimension(d.width + 2 * specifiedMargin, d.height + 2 * specifiedVerticalMargin);
    }

    private int getSpecifiedMargin()
    {
        Object o = getClientProperty(INSET_VIEW_MARGIN_KEY);
        return Math.max(0, o instanceof Integer ? (Integer) o : 0);
    }

    private int getSpecifiedVerticalMargin()
    {
        Object o = getClientProperty(INSET_VIEW_VERTICAL_MARGIN_KEY);
        return Math.max(0, o instanceof Integer ? (Integer) o : 0);
    }

    private boolean isVAquaInsetView()
    {
        Object o = getClientProperty("JTable.viewStyle");
        if (!"inset".equals(o)) {
            return false;
        }
        String s = System.getProperty("os.version");
        return s.startsWith("10.16") || !s.startsWith("10.");
    }

    @Override
    public void doLayout()
    {
        layoutManager.doLayout();
    }

    @Override
    public void sizeColumnsToFit(int resizingColumn)
    {
        layoutManager.sizeColumnsToFit(resizingColumn);
    }

    @Override
    public int columnAtPoint(@NotNull Point point)
    {
        if (margin > 0 || verticalMargin > 0) {
            point = new Point(point.x - margin, point.y - verticalMargin);
        }
        return super.columnAtPoint(point);
    }

    @Override
    public int rowAtPoint(@NotNull Point point)
    {
        if (margin > 0 || verticalMargin > 0) {
            point = new Point(point.x - margin, point.y - verticalMargin);
        }
        return super.rowAtPoint(point);
    }

    @Override
    public @NotNull Rectangle getCellRect(int row, int column, boolean includeSpacing)
    {
        Rectangle r = super.getCellRect(row, column, includeSpacing);
        if ((margin > 0 || verticalMargin > 0) && columnModel.getColumnCount() > 0) {
            r.x += margin;
            r.y += verticalMargin;
        }
        return r;
    }

    @Override
    protected JTableHeader createDefaultTableHeader()
    {
        return new TableHeaderWithMargins(columnModel);
    }

    private class MyPropertyChangeListener
      implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            String name = evt.getPropertyName();
            if (name == null || name.equals(INSET_VIEW_MARGIN_KEY) || name.equals(INSET_VIEW_VERTICAL_MARGIN_KEY)) {
                updateMargins();
            }
        }
    }

    /**
      The layout manager assigns column widths.
    */

    private static class TableLayoutManager
    {
        private final JTableWithMargins table;

        public TableLayoutManager(JTableWithMargins table)
        {
            this.table = table;
        }

        public void doLayout()
        {
            TableColumn resizingColumn = getResizingColumn();
            if (resizingColumn == null) {
                setWidthsFromPreferredWidths(false);
            }
            else {
                int columnIndex = viewIndexForColumn(resizingColumn);
                int delta = getAvailableWidth() - table.getColumnModel().getTotalColumnWidth();
                accommodateDelta(columnIndex, delta);
                delta = getAvailableWidth() - table.getColumnModel().getTotalColumnWidth();
                if (delta != 0) {
                    resizingColumn.setWidth(resizingColumn.getWidth() + delta);
                }
                setWidthsFromPreferredWidths(true);
            }
        }

        public void sizeColumnsToFit(int resizingColumn)
        {
            if (resizingColumn == -1) {
                setWidthsFromPreferredWidths(false);
            } else {
                if (table.getAutoResizeMode() == AUTO_RESIZE_OFF) {
                    TableColumn aColumn = table.getColumnModel().getColumn(resizingColumn);
                    aColumn.setPreferredWidth(aColumn.getWidth());
                } else {
                    int delta = getAvailableWidth() - table.getColumnModel().getTotalColumnWidth();
                    accommodateDelta(resizingColumn, delta);
                    setWidthsFromPreferredWidths(true);
                }
            }
        }

        public int getAvailableWidth()
        {
            return table.getWidth() - 2 * table.margin;
        }

        public int getPreferredAvailableWidth()
        {
            return table.getPreferredSize().width - 2 * table.margin;
        }

        private TableColumn getResizingColumn()
        {
            JTableHeader tableHeader = table.getTableHeader();
            return (tableHeader == null) ? null
                     : tableHeader.getResizingColumn();
        }

        private void setWidthsFromPreferredWidths(final boolean inverse)
        {
            int totalWidth     = getAvailableWidth();
            int totalPreferred = getPreferredAvailableWidth();
            int target = !inverse ? totalWidth : totalPreferred;

            final TableColumnModel cm = table.getColumnModel();
            Resizable3 r = new Resizable3() {
                public int  getElementCount()      { return cm.getColumnCount(); }
                public int  getLowerBoundAt(int i) { return cm.getColumn(i).getMinWidth(); }
                public int  getUpperBoundAt(int i) { return cm.getColumn(i).getMaxWidth(); }
                public int  getMidPointAt(int i)  {
                    if (!inverse) {
                        return cm.getColumn(i).getPreferredWidth();
                    }
                    else {
                        return cm.getColumn(i).getWidth();
                    }
                }
                public void setSizeAt(int s, int i) {
                    if (!inverse) {
                        cm.getColumn(i).setWidth(s);
                    }
                    else {
                        cm.getColumn(i).setPreferredWidth(s);
                    }
                }
            };

            adjustSizes(target, r, inverse);
        }

        // Distribute delta over columns, as indicated by the autoresize mode.
        private void accommodateDelta(int resizingColumnIndex, int delta)
        {
            int columnCount = table.getColumnCount();
            int from = resizingColumnIndex;
            int to;

            // Use the mode to determine how to absorb the changes.
            switch(table.getAutoResizeMode()) {
                case AUTO_RESIZE_NEXT_COLUMN:
                    from = from + 1;
                    to = Math.min(from + 1, columnCount); break;
                case AUTO_RESIZE_SUBSEQUENT_COLUMNS:
                    from = from + 1;
                    to = columnCount; break;
                case AUTO_RESIZE_LAST_COLUMN:
                    from = columnCount - 1;
                    to = from + 1; break;
                case AUTO_RESIZE_ALL_COLUMNS:
                    from = 0;
                    to = columnCount; break;
                default:
                    return;
            }

            final int start = from;
            final int end = to;
            final TableColumnModel cm = table.getColumnModel();
            Resizable3 r = new Resizable3() {
                public int  getElementCount()       { return end-start; }
                public int  getLowerBoundAt(int i)  { return cm.getColumn(i+start).getMinWidth(); }
                public int  getUpperBoundAt(int i)  { return cm.getColumn(i+start).getMaxWidth(); }
                public int  getMidPointAt(int i)    { return cm.getColumn(i+start).getWidth(); }
                public void setSizeAt(int s, int i) {        cm.getColumn(i+start).setWidth(s); }
            };

            int totalWidth = 0;
            for(int i = from; i < to; i++) {
                TableColumn aColumn = table.getColumnModel().getColumn(i);
                int input = aColumn.getWidth();
                totalWidth = totalWidth + input;
            }

            adjustSizes(totalWidth + delta, r, false);
        }

        private interface Resizable2
        {
            int getElementCount();
            int getLowerBoundAt(int i);
            int getUpperBoundAt(int i);
            void setSizeAt(int newSize, int i);
        }

        private interface Resizable3 extends Resizable2
        {
            int getMidPointAt(int i);
        }

        private void adjustSizes(long target, final Resizable3 r, boolean inverse)
        {
            int N = r.getElementCount();
            long totalPreferred = 0;
            for(int i = 0; i < N; i++) {
                totalPreferred += r.getMidPointAt(i);
            }

            // Code change here. If preferred sizes match the target, should not increase or decrease.
            if (target == totalPreferred) {
                for (int i = 0; i < N; i++) {
                    int preferred = r.getMidPointAt(i);
                    r.setSizeAt(preferred, i);
                }
                return;
            }

            Resizable2 s;
            if ((target < totalPreferred) == !inverse) {
                s = new Resizable2() {
                    public int  getElementCount()      { return r.getElementCount(); }
                    public int  getLowerBoundAt(int i) { return r.getLowerBoundAt(i); }
                    public int  getUpperBoundAt(int i) { return r.getMidPointAt(i); }
                    public void setSizeAt(int newSize, int i) { r.setSizeAt(newSize, i); }

                };
            }
            else {
                s = new Resizable2() {
                    public int  getElementCount()      { return r.getElementCount(); }
                    public int  getLowerBoundAt(int i) { return r.getMidPointAt(i); }
                    public int  getUpperBoundAt(int i) { return r.getUpperBoundAt(i); }
                    public void setSizeAt(int newSize, int i) { r.setSizeAt(newSize, i); }

                };
            }
            adjustSizes(target, s, !inverse);
        }

        private void adjustSizes(long target, Resizable2 r, boolean limitToRange)
        {
            long totalLowerBound = 0;
            long totalUpperBound = 0;
            for(int i = 0; i < r.getElementCount(); i++) {
                totalLowerBound += r.getLowerBoundAt(i);
                totalUpperBound += r.getUpperBoundAt(i);
            }

            if (limitToRange) {
                target = Math.min(Math.max(totalLowerBound, target), totalUpperBound);
            }

            for(int i = 0; i < r.getElementCount(); i++) {
                int lowerBound = r.getLowerBoundAt(i);
                int upperBound = r.getUpperBoundAt(i);
                // Check for zero. This happens when the distribution of the delta
                // finishes early due to a series of "fixed" entries at the end.
                // In this case, lowerBound == upperBound, for all subsequent terms.
                int newSize;
                if (totalLowerBound == totalUpperBound) {
                    newSize = lowerBound;
                }
                else {
                    double f = (double)(target - totalLowerBound)/(totalUpperBound - totalLowerBound);
                    newSize = (int)Math.round(lowerBound+f*(upperBound - lowerBound));
                    // We'd need to round manually in an all integer version.
                    // size[i] = (int)(((totalUpperBound - target) * lowerBound +
                    //     (target - totalLowerBound) * upperBound)/(totalUpperBound-totalLowerBound));
                }
                r.setSizeAt(newSize, i);
                target -= newSize;
                totalLowerBound -= lowerBound;
                totalUpperBound -= upperBound;
            }
        }

        private int viewIndexForColumn(TableColumn aColumn)
        {
            TableColumnModel cm = table.getColumnModel();
            for (int column = 0; column < cm.getColumnCount(); column++) {
                if (cm.getColumn(column) == aColumn) {
                    return column;
                }
            }
            return -1;
        }
    }

    public static class TableHeaderWithMargins
      extends JTableHeader
    {
        public TableHeaderWithMargins(@NotNull TableColumnModel cm)
        {
            super(cm);
        }

        @Override
        public int columnAtPoint(Point point)
        {
            int margin = getLeftMargin();
            if (margin > 0) {
                point = new Point(point.x - margin, point.y);
            }
            return super.columnAtPoint(point);
        }

        @Override
        public Rectangle getHeaderRect(int column)
        {
            Rectangle r = super.getHeaderRect(column);
            int lastColumn = columnModel.getColumnCount() - 1;
            if (lastColumn >= 0) {
                int margin = getLeftMargin();
                r.x += margin;
            }
            return r;
        }

        private int getLeftMargin()
        {
            if (table instanceof JTableWithMargins) {
                JTableWithMargins t = (JTableWithMargins) table;
                return t.margin;
            }
            return 0;
        }
    }
}
