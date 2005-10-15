/* BasicListUI.java --
   Copyright (C) 2002, 2004, 2005 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ListUI;

/**
 * The Basic Look and Feel UI delegate for the 
 * JList.
 */
public class BasicListUI extends ListUI
{

  /**
   * A helper class which listens for {@link ComponentEvent}s from
   * the JList.
   */
  private class ComponentHandler extends ComponentAdapter {

    /**
     * Called when the component is hidden. Invalidates the internal
     * layout.
     */
    public void componentResized(ComponentEvent ev) {
      BasicListUI.this.damageLayout();
    }
  }

  /**
   * A helper class which listens for {@link FocusEvent}s
   * from the JList.
   */
  public class FocusHandler implements FocusListener
  {
    /**
     * Called when the JList acquires focus.
     *
     * @param e The FocusEvent representing focus acquisition
     */
    public void focusGained(FocusEvent e)
    {
      repaintCellFocus();
    }

    /**
     * Called when the JList loses focus.
     *
     * @param e The FocusEvent representing focus loss
     */
    public void focusLost(FocusEvent e)
    {
      repaintCellFocus();
    }

    /**
     * Helper method to repaint the focused cell's 
     * lost or acquired focus state.
     */
    protected void repaintCellFocus()
    {
    }
  }

  /**
   * A helper class which listens for {@link ListDataEvent}s generated by
   * the {@link JList}'s {@link ListModel}.
   *
   * @see javax.swing.JList#getModel()
   */
  public class ListDataHandler implements ListDataListener
  {
    /**
     * Called when a general change has happened in the model which cannot
     * be represented in terms of a simple addition or deletion.
     *
     * @param e The event representing the change
     */
    public void contentsChanged(ListDataEvent e)
    {
      BasicListUI.this.damageLayout();
    }

    /**
     * Called when an interval of objects has been added to the model.
     *
     * @param e The event representing the addition
     */
    public void intervalAdded(ListDataEvent e)
    {
      BasicListUI.this.damageLayout();
    }

    /**
     * Called when an inteval of objects has been removed from the model.
     *
     * @param e The event representing the removal
     */
    public void intervalRemoved(ListDataEvent e)
    {
      BasicListUI.this.damageLayout();
    }
  }

  /**
   * A helper class which listens for {@link ListSelectionEvent}s
   * from the {@link JList}'s {@link ListSelectionModel}.
   */
  public class ListSelectionHandler implements ListSelectionListener
  {
    /**
     * Called when the list selection changes.  
     *
     * @param e The event representing the change
     */
    public void valueChanged(ListSelectionEvent e)
    {
    }
  }


  /**
   * A helper class which listens for {@link KeyEvents}s 
   * from the {@link JList}.
   */
  // FIXME: This should be handled somehow by the L&F key bindings.
  private class KeyHandler extends KeyAdapter
  {
    public KeyHandler()
    {
    }
    
    public void keyPressed( KeyEvent evt ) 
    {
      int lead = BasicListUI.this.list.getLeadSelectionIndex();
      int max = BasicListUI.this.list.getModel().getSize() - 1;
      // Do nothing if list is empty
      if (max == -1)
        return;

      // Process the key event.  Bindings can be found in
      // javax.swing.plaf.basic.BasicLookAndFeel.java
      if ((evt.getKeyCode() == KeyEvent.VK_DOWN)
          || (evt.getKeyCode() == KeyEvent.VK_KP_DOWN))
        {
          if (evt.getModifiers() == 0)
            {
              BasicListUI.this.list.clearSelection();
              BasicListUI.this.list.setSelectedIndex(Math.min(lead+1,max));
            }
          else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            selectNextIndex();
        }
      else if ((evt.getKeyCode() == KeyEvent.VK_UP)
               || (evt.getKeyCode() == KeyEvent.VK_KP_UP))
        {
          if (evt.getModifiers() == 0)
            {
              BasicListUI.this.list.clearSelection();
              BasicListUI.this.list.setSelectedIndex(Math.max(lead-1,0));
            }
          else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            selectPreviousIndex();
        }
      else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP)
        {
          int target;
          if (lead == BasicListUI.this.list.getFirstVisibleIndex())
            {
              target = Math.max 
                (0, lead - (BasicListUI.this.list.getLastVisibleIndex() - 
                             BasicListUI.this.list.getFirstVisibleIndex() + 1));
            }
          else
            {
              target = BasicListUI.this.list.getFirstVisibleIndex();
            }
          if (evt.getModifiers() == 0)
            BasicListUI.this.list.setSelectedIndex(target);
          else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            BasicListUI.this.list.getSelectionModel().
              setLeadSelectionIndex(target);
        }
      else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
        {
          int target;
          if (lead == BasicListUI.this.list.getLastVisibleIndex())
            {
              target = Math.min
                (max, lead + (BasicListUI.this.list.getLastVisibleIndex() -
                              BasicListUI.this.list.getFirstVisibleIndex() + 1));
            }
          else
            {
              target = BasicListUI.this.list.getLastVisibleIndex();
            }
          if (evt.getModifiers() == 0)
            BasicListUI.this.list.setSelectedIndex(target);
          else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            BasicListUI.this.list.getSelectionModel().
              setLeadSelectionIndex(target);
        }
      else if (evt.getKeyCode() == KeyEvent.VK_BACK_SLASH
               && (evt.getModifiers() == InputEvent.CTRL_MASK))
        {
            BasicListUI.this.list.clearSelection();
        }
      else if ((evt.getKeyCode() == KeyEvent.VK_HOME)
               || evt.getKeyCode() == KeyEvent.VK_END)
        {
          if (evt.getModifiers() != 0 && 
              evt.getModifiers() != InputEvent.SHIFT_MASK)
            return;
          // index is either 0 for HOME, or last cell for END
          int index = (evt.getKeyCode() == KeyEvent.VK_HOME) ? 0 : max;
          
          if (!evt.isShiftDown() ||(BasicListUI.this.list.getSelectionMode() 
                                    == ListSelectionModel.SINGLE_SELECTION))
            BasicListUI.this.list.setSelectedIndex(index);
          else if (BasicListUI.this.list.getSelectionMode() == 
                   ListSelectionModel.SINGLE_INTERVAL_SELECTION)
            BasicListUI.this.list.setSelectionInterval
              (BasicListUI.this.list.getAnchorSelectionIndex(), index);
          else
            BasicListUI.this.list.getSelectionModel().
              setLeadSelectionIndex(index);
        }
      else if ((evt.getKeyCode() == KeyEvent.VK_A || evt.getKeyCode()
                == KeyEvent.VK_SLASH) && (evt.getModifiers() == 
                                          InputEvent.CTRL_MASK))
        {
          BasicListUI.this.list.setSelectionInterval(0, max);
          // this next line is to restore the lead selection index to the old
          // position, because select-all should not change the lead index
          BasicListUI.this.list.addSelectionInterval(lead, lead);
        }
      else if (evt.getKeyCode() == KeyEvent.VK_SPACE && 
               (evt.getModifiers() == InputEvent.CTRL_MASK))
        {
          BasicListUI.this.list.getSelectionModel().
            setLeadSelectionIndex(Math.min(lead+1,max));
        }

      BasicListUI.this.list.ensureIndexIsVisible
        (BasicListUI.this.list.getLeadSelectionIndex());
    }
  }
  
  /**
   * A helper class which listens for {@link MouseEvent}s 
   * from the {@link JList}.
   */
  public class MouseInputHandler implements MouseInputListener
  {
    /**
     * Called when a mouse button press/release cycle completes
     * on the {@link JList}
     *
     * @param event The event representing the mouse click
     */
    public void mouseClicked(MouseEvent event)
    {
      Point click = event.getPoint();
      int index = BasicListUI.this.locationToIndex(list, click);
      if (index == -1)
        return;
      if (event.isShiftDown())
        {
          if (BasicListUI.this.list.getSelectionMode() == 
              ListSelectionModel.SINGLE_SELECTION)
            BasicListUI.this.list.setSelectedIndex(index);
          else if (BasicListUI.this.list.getSelectionMode() == 
                   ListSelectionModel.SINGLE_INTERVAL_SELECTION)
            // COMPAT: the IBM VM is compatible with the following line of code.
            // However, compliance with Sun's VM would correspond to replacing 
            // getAnchorSelectionIndex() with getLeadSelectionIndex().This is 
            // both unnatural and contradictory to the way they handle other 
            // similar UI interactions.
            BasicListUI.this.list.setSelectionInterval
              (BasicListUI.this.list.getAnchorSelectionIndex(), index);
          else
            // COMPAT: both Sun and IBM are compatible instead with:
            // BasicListUI.this.list.setSelectionInterval
            //     (BasicListUI.this.list.getLeadSelectionIndex(),index);
            // Note that for IBM this is contradictory to what they did in 
            // the above situation for SINGLE_INTERVAL_SELECTION.  
            // The most natural thing to do is the following:
            BasicListUI.this.list.getSelectionModel().
              setLeadSelectionIndex(index);
        }
      else if (event.isControlDown())
        {
          if (BasicListUI.this.list.getSelectionMode() == 
              ListSelectionModel.SINGLE_SELECTION)
            BasicListUI.this.list.setSelectedIndex(index);
          else if (BasicListUI.this.list.isSelectedIndex(index))
            BasicListUI.this.list.removeSelectionInterval(index,index);
          else
            BasicListUI.this.list.addSelectionInterval(index,index);
        }
      else
        BasicListUI.this.list.setSelectedIndex(index);
      
      BasicListUI.this.list.ensureIndexIsVisible
        (BasicListUI.this.list.getLeadSelectionIndex());
    }

    /**
     * Called when a mouse button is pressed down on the
     * {@link JList}.
     *
     * @param event The event representing the mouse press
     */
    public void mousePressed(MouseEvent event)
    {
    }

    /**
     * Called when a mouse button is released on
     * the {@link JList}
     *
     * @param event The event representing the mouse press
     */
    public void mouseReleased(MouseEvent event)
    {
    }

    /**
     * Called when the mouse pointer enters the area bounded
     * by the {@link JList}
     *
     * @param event The event representing the mouse entry
     */
    public void mouseEntered(MouseEvent event)
    {
    }

    /**
     * Called when the mouse pointer leaves the area bounded
     * by the {@link JList}
     *
     * @param event The event representing the mouse exit
     */
    public void mouseExited(MouseEvent event)
    {
    }

    /**
     * Called when the mouse pointer moves over the area bounded
     * by the {@link JList} while a button is held down.
     *
     * @param event The event representing the mouse drag
     */
    public void mouseDragged(MouseEvent event)
    {
    }

    /**
     * Called when the mouse pointer moves over the area bounded
     * by the {@link JList}.
     *
     * @param event The event representing the mouse move
     */
    public void mouseMoved(MouseEvent event)
    {
    }
  }

  /**
   * Helper class which listens to {@link PropertyChangeEvent}s
   * from the {@link JList}.
   */
  public class PropertyChangeHandler implements PropertyChangeListener
  {
    /**
     * Called when the {@link JList} changes one of its bound properties.
     *
     * @param e The event representing the property change
     */
    public void propertyChange(PropertyChangeEvent e)
    {
      if (e.getSource() == BasicListUI.this.list)
        {
          if (e.getOldValue() != null && e.getOldValue() instanceof ListModel)
            ((ListModel) e.getOldValue()).removeListDataListener(BasicListUI.this.listDataListener);

          if (e.getNewValue() != null && e.getNewValue() instanceof ListModel)
            ((ListModel) e.getNewValue()).addListDataListener(BasicListUI.this.listDataListener);
        }
      // Update the updateLayoutStateNeeded flag.
      if (e.getPropertyName().equals("model"))
        updateLayoutStateNeeded += modelChanged;
      else if (e.getPropertyName().equals("selectionModel"))
        updateLayoutStateNeeded += selectionModelChanged;
      else if (e.getPropertyName().equals("font"))
        updateLayoutStateNeeded += fontChanged;
      else if (e.getPropertyName().equals("fixedCellWidth"))
        updateLayoutStateNeeded += fixedCellWidthChanged;
      else if (e.getPropertyName().equals("fixedCellHeight"))
        updateLayoutStateNeeded += fixedCellHeightChanged;
      else if (e.getPropertyName().equals("prototypeCellValue"))
        updateLayoutStateNeeded += prototypeCellValueChanged;
      else if (e.getPropertyName().equals("cellRenderer"))
        updateLayoutStateNeeded += cellRendererChanged;

      BasicListUI.this.damageLayout();
    }
  }

  /**
   * A constant to indicate that the model has changed.
   */
  protected static final int modelChanged = 1;

  /**
   * A constant to indicate that the selection model has changed.
   */
  protected static final int selectionModelChanged = 2;

  /**
   * A constant to indicate that the font has changed.
   */
  protected static final int fontChanged = 4;

  /**
   * A constant to indicate that the fixedCellWidth has changed.
   */
  protected static final int fixedCellWidthChanged = 8;

  /**
   * A constant to indicate that the fixedCellHeight has changed.
   */
  protected static final int fixedCellHeightChanged = 16;

  /**
   * A constant to indicate that the prototypeCellValue has changed.
   */
  protected static final int prototypeCellValueChanged = 32;

  /**
   * A constant to indicate that the cellRenderer has changed.
   */
  protected static final int cellRendererChanged = 64;

  /**
   * Creates a new BasicListUI for the component.
   *
   * @param c The component to create a UI for
   *
   * @return A new UI
   */
  public static ComponentUI createUI(final JComponent c)
  {
    return new BasicListUI();
  }

  /** The current focus listener. */
  protected FocusListener focusListener;

  /** The data listener listening to the model. */
  protected ListDataListener listDataListener;

  /** The selection listener listening to the selection model. */
  protected ListSelectionListener listSelectionListener;

  /** The mouse listener listening to the list. */
  protected MouseInputListener mouseInputListener;

  /** The key listener listening to the list */
  private KeyHandler keyListener;

  /** The property change listener listening to the list. */
  protected PropertyChangeListener propertyChangeListener;


  /** The component listener that receives notification for resizing the
   * JList component.*/
  private ComponentListener componentListener;

  /** Saved reference to the list this UI was created for. */
  protected JList list;

  /** The height of a single cell in the list. */
  protected int cellHeight;

  /** The width of a single cell in the list. */
  protected int cellWidth;

  /** 
   * An array of varying heights of cells in the list, in cases where each
   * cell might have a different height.
   */
  protected int[] cellHeights;

  /**
   * A bitmask that indicates which properties of the JList have changed.
   * When nonzero, indicates that the UI class is out of
   * date with respect to the underlying list, and must recalculate the
   * list layout before painting or performing size calculations.
   *
   * @see #modelChanged
   * @see #selectionModelChanged
   * @see #fontChanged
   * @see #fixedCellWidthChanged
   * @see #fixedCellHeightChanged
   * @see #prototypeCellValueChanged
   * @see #cellRendererChanged
   */
  protected int updateLayoutStateNeeded;

  /**
   * The {@link CellRendererPane} that is used for painting.
   */
  protected CellRendererPane rendererPane;

  /**
   * Calculate the height of a particular row. If there is a fixed {@link
   * #cellHeight}, return it; otherwise return the specific row height
   * requested from the {@link #cellHeights} array. If the requested row
   * is invalid, return <code>-1</code>.
   *
   * @param row The row to get the height of
   *
   * @return The height, in pixels, of the specified row
   */
  protected int getRowHeight(int row)
  {
    if (row < 0 || row >= cellHeights.length)
      return -1;
    else if (cellHeight != -1)
      return cellHeight;
    else
      return cellHeights[row];
  }

  /**
   * Calculate the bounds of a particular cell, considering the upper left
   * corner of the list as the origin position <code>(0,0)</code>.
   *
   * @param l Ignored; calculates over <code>this.list</code>
   * @param index1 The first row to include in the bounds
   * @param index2 The last row to incude in the bounds
   *
   * @return A rectangle encompassing the range of rows between 
   * <code>index1</code> and <code>index2</code> inclusive
   */
  public Rectangle getCellBounds(JList l, int index1, int index2)
  {
    maybeUpdateLayoutState();

    if (l != list || cellWidth == -1)
      return null;

    int minIndex = Math.min(index1, index2);
    int maxIndex = Math.max(index1, index2);
    Point loc = indexToLocation(list, minIndex);
    Rectangle bounds = new Rectangle(loc.x, loc.y, cellWidth,
                                     getRowHeight(minIndex));

    for (int i = minIndex + 1; i <= maxIndex; i++)
      {
        Point hiLoc = indexToLocation(list, i);
        Rectangle hibounds = new Rectangle(hiLoc.x, hiLoc.y, cellWidth,
                                       getRowHeight(i));
        bounds = bounds.union(hibounds);
      }

    return bounds;
  }

  /**
   * Calculate the Y coordinate of the upper edge of a particular row,
   * considering the Y coordinate <code>0</code> to occur at the top of the
   * list.
   *
   * @param row The row to calculate the Y coordinate of
   *
   * @return The Y coordinate of the specified row, or <code>-1</code> if
   * the specified row number is invalid
   */
  protected int convertRowToY(int row)
  {
    int y = 0;
    for (int i = 0; i < row; ++i)
      {
        int h = getRowHeight(i);
        if (h == -1)
          return -1;
        y += h;
      }
    return y;
  }

  /**
   * Calculate the row number containing a particular Y coordinate,
   * considering the Y coodrinate <code>0</code> to occur at the top of the
   * list.
   *
   * @param y0 The Y coordinate to calculate the row number for
   *
   * @return The row number containing the specified Y value, or <code>-1</code>
   * if the specified Y coordinate is invalid
   */
  protected int convertYToRow(int y0)
  {
    for (int row = 0; row < cellHeights.length; ++row)
      {
        int h = getRowHeight(row);

        if (y0 < h)
          return row;
        y0 -= h;
      }
    return -1;
  }

  /**
   * Recomputes the {@link #cellHeights}, {@link #cellHeight}, and {@link
   * #cellWidth} properties by examining the variouis properties of the
   * {@link JList}.
   */
  protected void updateLayoutState()
  {
    int nrows = list.getModel().getSize();
    cellHeight = -1;
    cellWidth = -1;
    if (cellHeights == null || cellHeights.length != nrows)
      cellHeights = new int[nrows];
    if (list.getFixedCellHeight() == -1 || list.getFixedCellWidth() == -1)
      {
        ListCellRenderer rend = list.getCellRenderer();
        for (int i = 0; i < nrows; ++i)
          {
            Component flyweight = rend.getListCellRendererComponent(list,
                                                                    list.getModel()
                                                                        .getElementAt(i),
                                                                    0, false,
                                                                    false);
            Dimension dim = flyweight.getPreferredSize();
            cellHeights[i] = dim.height;
            // compute average cell height (little hack here)
            cellHeight = (cellHeight * i + cellHeights[i]) / (i + 1);
            cellWidth = Math.max(cellWidth, dim.width);
            if (list.getLayoutOrientation() == JList.VERTICAL)
                cellWidth = Math.max(cellWidth, list.getSize().width);
          }
      }
    else
      {
        cellHeight = list.getFixedCellHeight();
        cellWidth = list.getFixedCellWidth();
      }
  }

  /**
   * Marks the current layout as damaged and requests revalidation from the
   * JList.
   * This is package-private to avoid an accessor method.
   *
   * @see #updateLayoutStateNeeded
   */
  void damageLayout()
  {
    updateLayoutStateNeeded = 1;
  }

  /**
   * Calls {@link #updateLayoutState} if {@link #updateLayoutStateNeeded}
   * is nonzero, then resets {@link #updateLayoutStateNeeded} to zero.
   */
  protected void maybeUpdateLayoutState()
  {
    if (updateLayoutStateNeeded != 0)
      {
        updateLayoutState();
        updateLayoutStateNeeded = 0;
      }
  }

  /**
   * Creates a new BasicListUI object.
   */
  public BasicListUI()
  {
    updateLayoutStateNeeded = 1;
    rendererPane = new CellRendererPane();
  }

  /**
   * Installs various default settings (mostly colors) from the {@link
   * UIDefaults} into the {@link JList}
   *
   * @see #uninstallDefaults
   */
  protected void installDefaults()
  {
    LookAndFeel.installColorsAndFont(list, "List.background",
                                     "List.foreground", "List.font");
    list.setSelectionForeground(UIManager.getColor("List.selectionForeground"));
    list.setSelectionBackground(UIManager.getColor("List.selectionBackground"));
    list.setOpaque(true);
  }

  /**
   * Resets to <code>null</code> those defaults which were installed in 
   * {@link #installDefaults}
   */
  protected void uninstallDefaults()
  {
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    list.setForeground(null);
    list.setBackground(null);
    list.setSelectionForeground(null);
    list.setSelectionBackground(null);
  }

  /**
   * Attaches all the listeners we have in the UI class to the {@link
   * JList}, its model and its selection model.
   *
   * @see #uninstallListeners
   */
  protected void installListeners()
  {
    if (focusListener == null)
      focusListener = createFocusListener();
    list.addFocusListener(focusListener);
    if (listDataListener == null)
      listDataListener = createListDataListener();
    list.getModel().addListDataListener(listDataListener);
    if (listSelectionListener == null)
      listSelectionListener = createListSelectionListener();
    list.addListSelectionListener(listSelectionListener);
    if (mouseInputListener == null)
      mouseInputListener = createMouseInputListener();
    list.addMouseListener(mouseInputListener);
    list.addMouseMotionListener(mouseInputListener);
    if (propertyChangeListener == null)
      propertyChangeListener = createPropertyChangeListener();
    list.addPropertyChangeListener(propertyChangeListener);

    // FIXME: Are these two really needed? At least they are not documented.
    keyListener = new KeyHandler();
    list.addComponentListener(componentListener);
    componentListener = new ComponentHandler();
    list.addKeyListener(keyListener);
  }

  /**
   * Detaches all the listeners we attached in {@link #installListeners}.
   */
  protected void uninstallListeners()
  {
    list.removeFocusListener(focusListener);
    list.getModel().removeListDataListener(listDataListener);
    list.removeListSelectionListener(listSelectionListener);
    list.removeMouseListener(mouseInputListener);
    list.removeKeyListener(keyListener);
    list.removeMouseMotionListener(mouseInputListener);
    list.removePropertyChangeListener(propertyChangeListener);
  }

  /**
   * Installs keyboard actions for this UI in the {@link JList}.
   */
  protected void installKeyboardActions()
  {
  }

  /**
   * Uninstalls keyboard actions for this UI in the {@link JList}.
   */
  protected void uninstallKeyboardActions()
  {
  }

  /**
   * Installs the various aspects of the UI in the {@link JList}. In
   * particular, calls {@link #installDefaults}, {@link #installListeners}
   * and {@link #installKeyboardActions}. Also saves a reference to the
   * provided component, cast to a {@link JList}.
   *
   * @param c The {@link JList} to install the UI into
   */
  public void installUI(final JComponent c)
  {
    super.installUI(c);
    list = (JList) c;
    installDefaults();
    installListeners();
    installKeyboardActions();
    maybeUpdateLayoutState();
  }

  /**
   * Uninstalls all the aspects of the UI which were installed in {@link
   * #installUI}. When finished uninstalling, drops the saved reference to
   * the {@link JList}.
   *
   * @param c Ignored; the UI is uninstalled from the {@link JList}
   * reference saved during the call to {@link #installUI}
   */
  public void uninstallUI(final JComponent c)
  {
    uninstallKeyboardActions();
    uninstallListeners();
    uninstallDefaults();
    list = null;
  }

  /**
   * Gets the size this list would prefer to assume. This is calculated by
   * calling {@link #getCellBounds} over the entire list.
   *
   * @param c Ignored; uses the saved {@link JList} reference 
   *
   * @return DOCUMENT ME!
   */
  public Dimension getPreferredSize(JComponent c)
  {
    int size = list.getModel().getSize();
    if (size == 0)
      return new Dimension(0, 0);
    int visibleRows = list.getVisibleRowCount();
    int layoutOrientation = list.getLayoutOrientation();
    Rectangle bounds = getCellBounds(list, 0, list.getModel().getSize() - 1);
    Dimension retVal = bounds.getSize();
    Component parent = list.getParent();
    if ((visibleRows == -1) && (parent instanceof JViewport))
      {
        JViewport viewport = (JViewport) parent;

        if (layoutOrientation == JList.HORIZONTAL_WRAP)
          {
            int h = viewport.getSize().height;
            int cellsPerCol = h / cellHeight;
            int w = size / cellsPerCol * cellWidth;
            retVal = new Dimension(w, h);
          }
        else if (layoutOrientation == JList.VERTICAL_WRAP)
          {
            int w = viewport.getSize().width;
            int cellsPerRow = Math.max(w / cellWidth, 1);
            int h = size / cellsPerRow * cellHeight;
            retVal = new Dimension(w, h);
          }
      }
    return retVal;
  }

  /**
   * Paints the packground of the list using the background color
   * of the specified component.
   *
   * @param g The graphics context to paint in
   * @param c The component to paint the background of
   */
  private void paintBackground(Graphics g, JComponent c)
  {
    Dimension size = getPreferredSize(c);
    Color save = g.getColor();
    g.setColor(c.getBackground());
    g.fillRect(0, 0, size.width, size.height);
    g.setColor(save);
  }

  /**
   * Paints a single cell in the list.
   *
   * @param g The graphics context to paint in
   * @param row The row number to paint
   * @param bounds The bounds of the cell to paint, assuming a coordinate
   * system beginning at <code>(0,0)</code> in the upper left corner of the
   * list
   * @param rend A cell renderer to paint with
   * @param data The data to provide to the cell renderer
   * @param sel A selection model to provide to the cell renderer
   * @param lead The lead selection index of the list
   */
  protected void paintCell(Graphics g, int row, Rectangle bounds,
                 ListCellRenderer rend, ListModel data,
                 ListSelectionModel sel, int lead)
  {
    boolean isSel = list.isSelectedIndex(row);
    boolean hasFocus = (list.getLeadSelectionIndex() == row) && BasicListUI.this.list.hasFocus();
    Component comp = rend.getListCellRendererComponent(list,
                                                       data.getElementAt(row),
                                                       0, isSel, hasFocus);
    //comp.setBounds(new Rectangle(0, 0, bounds.width, bounds.height));
    //comp.paint(g);
    rendererPane.paintComponent(g, comp, list, bounds);
  }

  /**
   * Paints the list by calling {@link #paintBackground} and then repeatedly
   * calling {@link #paintCell} for each visible cell in the list.
   *
   * @param g The graphics context to paint with
   * @param c Ignored; uses the saved {@link JList} reference 
   */
  public void paint(Graphics g, JComponent c)
  {
    int nrows = list.getModel().getSize();
    if (nrows == 0)
      return;

    maybeUpdateLayoutState();
    ListCellRenderer render = list.getCellRenderer();
    ListModel model = list.getModel();
    ListSelectionModel sel = list.getSelectionModel();
    int lead = sel.getLeadSelectionIndex();
    Rectangle clip = g.getClipBounds();
    paintBackground(g, list);

    for (int row = 0; row < nrows; ++row)
      {
        Rectangle bounds = getCellBounds(list, row, row);
        if (bounds.intersects(clip))
          paintCell(g, row, bounds, render, model, sel, lead);
      }
  }

  /**
   * Computes the index of a list cell given a point within the list.
   *
   * @param list the list which on which the computation is based on
   * @param location the coordinates
   *
   * @return the index of the list item that is located at the given
   *         coordinates or <code>null</code> if the location is invalid
   */
  public int locationToIndex(JList list, Point location)
  {
    int layoutOrientation = list.getLayoutOrientation();
    int index = -1;
    switch (layoutOrientation)
      {
      case JList.VERTICAL:
        index = convertYToRow(location.y);
        break;
      case JList.HORIZONTAL_WRAP:
        // determine visible rows and cells per row
        int visibleRows = list.getVisibleRowCount();
        int cellsPerRow = -1;
        int numberOfItems = list.getModel().getSize();
        Dimension listDim = list.getSize();
        if (visibleRows <= 0)
          {
            try
              {
                cellsPerRow = listDim.width / cellWidth;
              }
            catch (ArithmeticException ex)
              {
                cellsPerRow = 1;
              }
          }
        else
          {
            cellsPerRow = numberOfItems / visibleRows + 1;
          }

        // determine index for the given location
        int cellsPerColumn = numberOfItems / cellsPerRow + 1;
        int gridX = Math.min(location.x / cellWidth, cellsPerRow - 1);
        int gridY = Math.min(location.y / cellHeight, cellsPerColumn);
        index = gridX + gridY * cellsPerRow;
        break;
      case JList.VERTICAL_WRAP:
        // determine visible rows and cells per column
        int visibleRows2 = list.getVisibleRowCount();
        if (visibleRows2 <= 0)
          {
            Dimension listDim2 = list.getSize();
            visibleRows2 = listDim2.height / cellHeight;
          }
        int numberOfItems2 = list.getModel().getSize();
        int cellsPerRow2 = numberOfItems2 / visibleRows2 + 1;

        Dimension listDim2 = list.getSize();
        int gridX2 = Math.min(location.x / cellWidth, cellsPerRow2 - 1);
        int gridY2 = Math.min(location.y / cellHeight, visibleRows2);
        index = gridY2 + gridX2 * visibleRows2;
        break;
      }
    return index;
  }

  public Point indexToLocation(JList list, int index)
  {
    int layoutOrientation = list.getLayoutOrientation();
    Point loc = null;
    switch (layoutOrientation)
      {
      case JList.VERTICAL:
        loc = new Point(0, convertRowToY(index));
        break;
      case JList.HORIZONTAL_WRAP:
        // determine visible rows and cells per row
        int visibleRows = list.getVisibleRowCount();
        int numberOfCellsPerRow = -1;
        if (visibleRows <= 0)
          {
            Dimension listDim = list.getSize();
            numberOfCellsPerRow = Math.max(listDim.width / cellWidth, 1);
          }
        else
          {
            int numberOfItems = list.getModel().getSize();
            numberOfCellsPerRow = numberOfItems / visibleRows + 1;
          }
        // compute coordinates inside the grid
        int gridX = index % numberOfCellsPerRow;
        int gridY = index / numberOfCellsPerRow;
        int locX = gridX * cellWidth;
        int locY = gridY * cellHeight;
        loc = new Point(locX, locY);
        break;
      case JList.VERTICAL_WRAP:
        // determine visible rows and cells per column
        int visibleRows2 = list.getVisibleRowCount();
        if (visibleRows2 <= 0)
          {
            Dimension listDim2 = list.getSize();
            visibleRows2 = listDim2.height / cellHeight;
          }
        // compute coordinates inside the grid
        if (visibleRows2 > 0)
          {
            int gridY2 = index % visibleRows2;
            int gridX2 = index / visibleRows2;
            int locX2 = gridX2 * cellWidth;
            int locY2 = gridY2 * cellHeight;
            loc = new Point(locX2, locY2);
          }
        else
          loc = new Point(0, convertRowToY(index));
        break;
      }
    return loc;
  }

  /**
   * Creates and returns the focus listener for this UI.
   *
   * @return the focus listener for this UI
   */
  protected FocusListener createFocusListener()
  {
    return new FocusHandler();
  }

  /**
   * Creates and returns the list data listener for this UI.
   *
   * @return the list data listener for this UI
   */
  protected ListDataListener createListDataListener()
  {
    return new ListDataHandler();
  }

  /**
   * Creates and returns the list selection listener for this UI.
   *
   * @return the list selection listener for this UI
   */
  protected ListSelectionListener createListSelectionListener()
  {
    return new ListSelectionHandler();
  }

  /**
   * Creates and returns the mouse input listener for this UI.
   *
   * @return the mouse input listener for this UI
   */
  protected MouseInputListener createMouseInputListener()
  {
    return new MouseInputHandler();
  }

  /**
   * Creates and returns the property change listener for this UI.
   *
   * @return the property change listener for this UI
   */
  protected PropertyChangeListener createPropertyChangeListener()
  {
    return new PropertyChangeHandler();
  }

  /**
   * Selects the next list item and force it to be visible.
   */
  protected void selectNextIndex()
  {
    int index = list.getSelectedIndex();
    index++;
    list.setSelectedIndex(index);
    list.ensureIndexIsVisible(index);
  }

  /**
   * Selects the previous list item and force it to be visible.
   */
  protected void selectPreviousIndex()
  {
    int index = list.getSelectedIndex();
    index--;
    list.setSelectedIndex(index);
    list.ensureIndexIsVisible(index);
  }
}
