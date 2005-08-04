/* BasicTableUI.java --
   Copyright (C) 2004 Free Software Foundation, Inc.

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
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class BasicTableUI
  extends TableUI
{
  public static ComponentUI createUI(JComponent comp) 
  {
    return new BasicTableUI();
  }

  protected FocusListener focusListener;  
  protected KeyListener keyListener;   
  protected MouseInputListener	mouseInputListener;   
  protected CellRendererPane rendererPane;   
  protected JTable table;

  /** The normal cell border. */
  Border cellBorder;

  /** The cell border for selected/highlighted cells. */
  Border highlightCellBorder;

  class FocusHandler implements FocusListener
  {
    public void focusGained(FocusEvent e) 
    {
    }
    public void focusLost(FocusEvent e) 
    {
    }
  }

  class KeyHandler implements KeyListener
  {

    /**
     * A helper method for the keyPressed event.  Used because the actions
     * for TAB, SHIFT-TAB, ENTER, and SHIFT-ENTER are very similar.
     *
     * Selects the next (previous if SHIFT pressed) column for TAB, or row for
     * ENTER from within the currently selected cells.
     *
     * @param firstModel the ListSelectionModel for columns (TAB) or
     * rows (ENTER)
     * @param firstMin the first selected index in firstModel
     * @param firstMax the last selected index in firstModel
     * @param secondModel the ListSelectionModel for rows (TAB) or 
     * columns (ENTER)
     * @param secondMin the first selected index in secondModel
     * @param secondMax the last selected index in secondModel
     * @param reverse true if shift was held for the event
     * @param eventIsTab true if TAB was pressed, false if ENTER pressed
     */
    void advanceMultipleSelection (ListSelectionModel firstModel, int firstMin,
                                   int firstMax, ListSelectionModel secondModel, 
                                   int secondMin, int secondMax, boolean reverse,
                                   boolean eventIsTab)
    {
      // If eventIsTab, all the "firsts" correspond to columns, otherwise, to rows
      // "seconds" correspond to the opposite
      int firstLead = firstModel.getLeadSelectionIndex();
      int secondLead = secondModel.getLeadSelectionIndex();
      int numFirsts = eventIsTab ? 
        table.getModel().getColumnCount() : table.getModel().getRowCount();
      int numSeconds = eventIsTab ? 
        table.getModel().getRowCount() : table.getModel().getColumnCount();

      // check if we have to wrap the "firsts" around, going to the other side
      if ((firstLead == firstMax && !reverse) || 
          (reverse && firstLead == firstMin))
        {
          firstModel.addSelectionInterval(reverse ? firstMax : firstMin, 
                                          reverse ? firstMax : firstMin);
          
          // check if we have to wrap the "seconds"
          if ((secondLead == secondMax && !reverse) || 
              (reverse && secondLead == secondMin))
            secondModel.addSelectionInterval(reverse ? secondMax : secondMin, 
                                             reverse ? secondMax : secondMin);

          // if we're not wrapping the seconds, we have to find out where we
          // are within the secondModel and advance to the next cell (or 
          // go back to the previous cell if reverse == true)
          else
            {
              int[] secondsSelected;
              if (eventIsTab && table.getRowSelectionAllowed() || 
                  !eventIsTab && table.getColumnSelectionAllowed())
                secondsSelected = eventIsTab ? 
                  table.getSelectedRows() : table.getSelectedColumns();
              else
                {
                  // if row selection is not allowed, then the entire column gets
                  // selected when you click on it, so consider ALL rows selected
                  secondsSelected = new int[numSeconds];
                  for (int i = 0; i < numSeconds; i++)
                  secondsSelected[i] = i;
                }

              // and now find the "next" index within the model
              int secondIndex = reverse ? secondsSelected.length - 1 : 0;
              if (!reverse)
                while (secondsSelected[secondIndex] <= secondLead)
                  secondIndex++;
              else
                while (secondsSelected[secondIndex] >= secondLead)
                  secondIndex--;
              
              // and select it - updating the lead selection index
              secondModel.addSelectionInterval(secondsSelected[secondIndex], 
                                               secondsSelected[secondIndex]);
            }
        }
      // We didn't have to wrap the firsts, so just find the "next" first
      // and select it, we don't have to change "seconds"
      else
        {
          int[] firstsSelected;
          if (eventIsTab && table.getColumnSelectionAllowed() || 
              !eventIsTab && table.getRowSelectionAllowed())
            firstsSelected = eventIsTab ? 
              table.getSelectedColumns() : table.getSelectedRows();
          else
            {
              // if selection not allowed, consider ALL firsts to be selected
              firstsSelected = new int[numFirsts];
              for (int i = 0; i < numFirsts; i++)
                firstsSelected[i] = i;
            }
          int firstIndex = reverse ? firstsSelected.length - 1 : 0;
          if (!reverse)
            while (firstsSelected[firstIndex] <= firstLead)
              firstIndex++;
          else 
            while (firstsSelected[firstIndex] >= firstLead)
              firstIndex--;
          firstModel.addSelectionInterval(firstsSelected[firstIndex], 
                                          firstsSelected[firstIndex]);
          secondModel.addSelectionInterval(secondLead, secondLead);
        }
    }
    
    /** 
     * A helper method for the keyPressed event. Used because the actions
     * for TAB, SHIFT-TAB, ENTER, and SHIFT-ENTER are very similar.
     *
     * Selects the next (previous if SHIFT pressed) column (TAB) or row (ENTER)
     * in the table, changing the current selection.  All cells in the table
     * are eligible, not just the ones that are currently selected.
     * @param firstModel the ListSelectionModel for columns (TAB) or rows
     * (ENTER)
     * @param firstMax the last index in firstModel
     * @param secondModel the ListSelectionModel for rows (TAB) or columns
     * (ENTER)
     * @param secondMax the last index in secondModel
     * @param reverse true if SHIFT was pressed for the event
     */

    void advanceSingleSelection (ListSelectionModel firstModel, int firstMax, 
                                 ListSelectionModel secondModel, int secondMax, 
                                 boolean reverse)
    {
      // for TABs, "first" corresponds to columns and "seconds" to rows.
      // the opposite is true for ENTERs
      int firstLead = firstModel.getLeadSelectionIndex();
      int secondLead = secondModel.getLeadSelectionIndex();
      
      // if we are going backwards subtract 2 because we later add 1
      // for a net change of -1
      if (reverse && (firstLead == 0))
        {
          // check if we have to wrap around
          if (secondLead == 0)
            secondLead += secondMax + 1;
          secondLead -= 2;
        }
      
      // do we have to wrap the "seconds"?
      if (reverse && (firstLead == 0) || !reverse && (firstLead == firstMax))
        secondModel.setSelectionInterval((secondLead + 1)%(secondMax + 1), 
                                         (secondLead + 1)%(secondMax + 1));
      // if not, just reselect the current lead
      else
        secondModel.setSelectionInterval(secondLead, secondLead);
      
      // if we are going backwards, subtract 2  because we add 1 later
      // for net change of -1
      if (reverse)
        {
          // check for wraparound
          if (firstLead == 0)
            firstLead += firstMax + 1;
          firstLead -= 2;
        }
      // select the next "first"
      firstModel.setSelectionInterval ((firstLead + 1)%(firstMax + 1), 
                                       (firstLead + 1)%(firstMax + 1));
    }

    public void keyPressed(KeyEvent evt) 
    {
      ListSelectionModel rowModel = table.getSelectionModel();
      ListSelectionModel colModel = table.getColumnModel().getSelectionModel();

      int rowLead = rowModel.getLeadSelectionIndex();
      int rowMax = table.getModel().getRowCount() - 1;

      int colLead = colModel.getLeadSelectionIndex();
      int colMax = table.getModel().getColumnCount() - 1;

      if ((evt.getKeyCode() == KeyEvent.VK_DOWN)
          || (evt.getKeyCode() == KeyEvent.VK_KP_DOWN))
        {
          if (evt.getModifiers() == 0)
            {
              
              table.clearSelection();
              rowModel.setSelectionInterval(Math.min(rowLead + 1, rowMax),
                                            Math.min(rowLead + 1, rowMax));
              colModel.setSelectionInterval(colLead,colLead);
            }
          else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            {
              rowModel.setLeadSelectionIndex(Math.min(rowLead + 1, rowMax));
              colModel.setLeadSelectionIndex(colLead);
            }
        }
      else if ((evt.getKeyCode() == KeyEvent.VK_UP)
               || (evt.getKeyCode() == KeyEvent.VK_KP_UP))
        {
          if (evt.getModifiers() == 0)
            {
              table.clearSelection();
              rowModel.setSelectionInterval(Math.max(rowLead - 1, 0),
                                            Math.max(rowLead - 1, 0));
              colModel.setSelectionInterval(colLead,colLead);
            }
          else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            {
              rowModel.setLeadSelectionIndex(Math.max(rowLead - 1, 0));
              colModel.setLeadSelectionIndex(colLead);
            }
        }
      else if ((evt.getKeyCode() == KeyEvent.VK_LEFT)
               || (evt.getKeyCode() == KeyEvent.VK_KP_LEFT))
        {
          if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            {
              colModel.setLeadSelectionIndex(Math.max(colLead - 1, 0));
              rowModel.setLeadSelectionIndex(rowLead);
            }
          else if (evt.getModifiers() == 0)
            {
              table.clearSelection();
              rowModel.setSelectionInterval(rowLead,rowLead);
              colModel.setSelectionInterval(Math.max(colLead - 1, 0),
                                            Math.max(colLead - 1, 0));
            }
        }
      else if ((evt.getKeyCode() == KeyEvent.VK_RIGHT)
               || (evt.getKeyCode() == KeyEvent.VK_KP_RIGHT))
        {
          if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            {
              colModel.setLeadSelectionIndex(Math.min(colLead + 1, colMax));
              rowModel.setLeadSelectionIndex(rowLead);
            }
          else if (evt.getModifiers() == 0)
            {
              table.clearSelection();
              rowModel.setSelectionInterval(rowLead,rowLead);
              colModel.setSelectionInterval(Math.min(colLead + 1, colMax),
                                            Math.min(colLead + 1, colMax));
            }
        }
      else if (evt.getKeyCode() == KeyEvent.VK_END)
        {
          if (evt.getModifiers() == (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK))
            {
              rowModel.setLeadSelectionIndex(rowMax);
              colModel.setLeadSelectionIndex(colLead);
            }
          else if (evt.getModifiers() == InputEvent.CTRL_MASK)
            {
              table.clearSelection();
              rowModel.setSelectionInterval(rowMax,rowMax);
              colModel.setSelectionInterval(colLead, colLead);
            }
          else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            {
              colModel.setLeadSelectionIndex(colMax);
              rowModel.setLeadSelectionIndex(rowLead);
            }
          else if (evt.getModifiers() == 0)
            {
              table.clearSelection();
              rowModel.setSelectionInterval(rowLead, rowLead);
              colModel.setSelectionInterval(colMax, colMax);
            }
         }
      else if (evt.getKeyCode() == KeyEvent.VK_HOME)
        {
          if (evt.getModifiers() == 
              (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK))
            {
              rowModel.setLeadSelectionIndex(0);
              colModel.setLeadSelectionIndex(colLead);
            }
          else if (evt.getModifiers() == InputEvent.CTRL_MASK)
            {
              table.clearSelection();
              rowModel.setSelectionInterval(0,0);
              colModel.setSelectionInterval(colLead, colLead);
    }
          else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
            {
              colModel.setLeadSelectionIndex(0);
              rowModel.setLeadSelectionIndex(rowLead);
            }
          else if (evt.getModifiers() == 0)
            {
              table.clearSelection();
              rowModel.setSelectionInterval(rowLead, rowLead);
              colModel.setSelectionInterval(0, 0);
            }
        }
      else if (evt.getKeyCode() == KeyEvent.VK_F2)
        {
          // FIXME: Implement "start editing"
        }
      else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP)
        {
          int target;
          if (!evt.isControlDown())
            {
              if (rowLead == getFirstVisibleRowIndex())
                target = Math.max
                  (0, rowLead - (getLastVisibleRowIndex() - 
                                      getFirstVisibleRowIndex() + 1));
              else
                target = getFirstVisibleRowIndex();
              
              if (evt.getModifiers() == 0)
                {
                  rowModel.setSelectionInterval(target, target);
                  colModel.setSelectionInterval(colLead, colLead);
                }
              else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
                {
                  rowModel.setLeadSelectionIndex(target);
                  colModel.setLeadSelectionIndex(colLead);
                }
            }
          else
            {
              if (colLead == getFirstVisibleColumnIndex())
                target = Math.max
                  (0, colLead - (getLastVisibleColumnIndex() -
                                      getFirstVisibleColumnIndex() + 1));
              else
                target = getFirstVisibleColumnIndex();
              
              if (evt.getModifiers() == InputEvent.CTRL_MASK)
                {
                  colModel.setSelectionInterval(target, target);
                  rowModel.setSelectionInterval(rowLead, rowLead);
                }
              else if (evt.getModifiers() == 
                       (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK))
                {
                  colModel.setLeadSelectionIndex(target);
                  rowModel.setLeadSelectionIndex(rowLead);
                }
            }
        }
      else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
        {
          int target;
          if (!evt.isControlDown())
            {
              if (rowLead == getLastVisibleRowIndex())
                target = Math.min
                  (rowMax, rowLead + (getLastVisibleRowIndex() - 
                                      getFirstVisibleRowIndex() + 1));
              else
                target = getLastVisibleRowIndex();
              
              if (evt.getModifiers() == 0)
                {
                  rowModel.setSelectionInterval(target, target);
                  colModel.setSelectionInterval(colLead, colLead);
                }
              else if (evt.getModifiers() == InputEvent.SHIFT_MASK)
                {
                  rowModel.setLeadSelectionIndex(target);
                  colModel.setLeadSelectionIndex(colLead);
                }
            }
          else
            {
              if (colLead == getLastVisibleColumnIndex())
                target = Math.min
                  (colMax, colLead + (getLastVisibleColumnIndex() -
                                      getFirstVisibleColumnIndex() + 1));
              else
                target = getLastVisibleColumnIndex();
              
              if (evt.getModifiers() == InputEvent.CTRL_MASK)
                {
                  colModel.setSelectionInterval(target, target);
                  rowModel.setSelectionInterval(rowLead, rowLead);
        }
              else if (evt.getModifiers() == 
                       (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK))
        {
                  colModel.setLeadSelectionIndex(target);
                  rowModel.setLeadSelectionIndex(rowLead);
                }
            }
        }
      else if (evt.getKeyCode() == KeyEvent.VK_TAB
               || evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          // If modifers other than SHIFT are pressed, do nothing
          if (evt.getModifiers() != 0 && evt.getModifiers() !=
              InputEvent.SHIFT_MASK)
            return;
          
          // If nothing is selected, select the first cell in the table
          if (table.getSelectedRowCount() == 0 && 
              table.getSelectedColumnCount() == 0)
            {
              rowModel.setSelectionInterval(0, 0);
              colModel.setSelectionInterval(0, 0);
              return;
            }
          
          // If the lead selection index isn't selected (ie a remove operation
          // happened, then set the lead to the first selected cell in the
          // table
          if (!table.isCellSelected(rowLead, colLead))
            {
              rowModel.addSelectionInterval(rowModel.getMinSelectionIndex(), 
                                            rowModel.getMinSelectionIndex());
              colModel.addSelectionInterval(colModel.getMinSelectionIndex(), 
                                            colModel.getMinSelectionIndex());
              return;
            }

          // multRowsSelected and multColsSelected tell us if multiple rows or
          // columns are selected, respectively
          boolean multRowsSelected, multColsSelected;
          multRowsSelected = (table.getSelectedRowCount() > 1) ||
            (!table.getRowSelectionAllowed() && 
             table.getSelectedColumnCount() > 0);
          multColsSelected = (table.getSelectedColumnCount() > 1) ||
            (!table.getColumnSelectionAllowed() && 
             table.getSelectedRowCount() > 0);
          
          // If there is just one selection, select the next cell, and wrap
          // when you get to the edges of the table.
          if (!multColsSelected || !multRowsSelected)
            {
              if (evt.getKeyCode() == KeyEvent.VK_TAB)
                advanceSingleSelection(colModel, colMax, rowModel, rowMax, 
                                       (evt.getModifiers() == 
                                        InputEvent.SHIFT_MASK));
              else
                advanceSingleSelection(rowModel, rowMax, colModel, colMax, 
                                       (evt.getModifiers() == 
                                        InputEvent.SHIFT_MASK));
              return;
            }


          // rowMinSelected and rowMaxSelected are the minimum and maximum
          // values respectively of selected cells in the row selection model
          // Similarly for colMinSelected and colMaxSelected.
          int rowMaxSelected = table.getRowSelectionAllowed() ? 
            rowModel.getMaxSelectionIndex() : table.getModel().getRowCount() - 1;
          int rowMinSelected = table.getRowSelectionAllowed() ? 
            rowModel.getMinSelectionIndex() : 0; 
          int colMaxSelected = table.getColumnSelectionAllowed() ? 
            colModel.getMaxSelectionIndex() : 
            table.getModel().getColumnCount() - 1;
          int colMinSelected = table.getColumnSelectionAllowed() ? 
            colModel.getMinSelectionIndex() : 0;
              
          // If there are multiple rows and columns selected, select the next
          // cell and wrap at the edges of the selection.  
          if (evt.getKeyCode() == KeyEvent.VK_TAB)
            advanceMultipleSelection(colModel, colMinSelected, colMaxSelected, 
                                     rowModel, rowMinSelected, rowMaxSelected, 
                                     (evt.getModifiers() == 
                                      InputEvent.SHIFT_MASK), true);
          else
            advanceMultipleSelection(rowModel, rowMinSelected, rowMaxSelected, 
                                     colModel, colMinSelected, colMaxSelected, 
                                     (evt.getModifiers() == 
                                      InputEvent.SHIFT_MASK), false);
          
          table.repaint();
        }
      else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
          // FIXME: implement "cancel"
        }
      else if ((evt.getKeyCode() == KeyEvent.VK_A || evt.getKeyCode()
                == KeyEvent.VK_SLASH) && (evt.getModifiers() == 
                                          InputEvent.CTRL_MASK))
        {
          table.selectAll();
        }
      else if (evt.getKeyCode() == KeyEvent.VK_BACK_SLASH
               && (evt.getModifiers() == InputEvent.CTRL_MASK))
        {
          table.clearSelection();
        }
      else if (evt.getKeyCode() == KeyEvent.VK_SPACE 
               && (evt.getModifiers() == InputEvent.CTRL_MASK))
        {
          table.changeSelection(rowLead, colLead, true, false);
        }
      table.scrollRectToVisible
        (table.getCellRect(rowModel.getLeadSelectionIndex(), 
                           colModel.getLeadSelectionIndex(), false));
    }

    public void keyReleased(KeyEvent e) 
    {
    }

    public void keyTyped(KeyEvent e) 
    {
    }

    /**
     * Returns the column index of the first visible column.
     *
     */
    int getFirstVisibleColumnIndex()
    {
      ComponentOrientation or = table.getComponentOrientation();
      Rectangle r = table.getVisibleRect();
      if (!or.isLeftToRight())
        r.translate((int) r.getWidth() - 1, 0);
      return table.columnAtPoint(r.getLocation());
    }
    
    /**
     * Returns the column index of the last visible column.
     *
     */
    int getLastVisibleColumnIndex()
    {
      ComponentOrientation or = table.getComponentOrientation();
      Rectangle r = table.getVisibleRect();
      if (or.isLeftToRight())
        r.translate((int) r.getWidth() - 1, 0);
      return table.columnAtPoint(r.getLocation());      
    }
    
    /**
     * Returns the row index of the first visible row.
     *
     */
    int getFirstVisibleRowIndex()
    {
      ComponentOrientation or = table.getComponentOrientation();
      Rectangle r = table.getVisibleRect();
      if (!or.isLeftToRight())
        r.translate((int) r.getWidth() - 1, 0);
      return table.rowAtPoint(r.getLocation());
    }
    
    /**
     * Returns the row index of the last visible row.
     *
     */
    int getLastVisibleRowIndex()
    {
      ComponentOrientation or = table.getComponentOrientation();
      Rectangle r = table.getVisibleRect();
      r.translate(0, (int) r.getHeight() - 1);
      if (or.isLeftToRight())
        r.translate((int) r.getWidth() - 1, 0);
      // The next if makes sure that we don't return -1 simply because
      // there is white space at the bottom of the table (ie, the display
      // area is larger than the table)
      if (table.rowAtPoint(r.getLocation()) == -1)
        {
          if (getFirstVisibleRowIndex() == -1)
            return -1;
          else
            return table.getModel().getRowCount() - 1;
        }
      return table.rowAtPoint(r.getLocation());
    }
  }

  class MouseInputHandler implements MouseInputListener
  {
    Point begin, curr;

    private void updateSelection(boolean controlPressed)
    {
      // Update the rows
          int lo_row = table.rowAtPoint(begin);
          int hi_row  = table.rowAtPoint(curr);
          ListSelectionModel rowModel = table.getSelectionModel();
          if (lo_row != -1 && hi_row != -1)
            {
              if (controlPressed && rowModel.getSelectionMode() 
                  != ListSelectionModel.SINGLE_SELECTION)
                rowModel.addSelectionInterval(lo_row, hi_row);
              else
            rowModel.setSelectionInterval(lo_row, hi_row);
        }

      // Update the columns
          int lo_col = table.columnAtPoint(begin);
          int hi_col = table.columnAtPoint(curr);
          ListSelectionModel colModel = table.getColumnModel().
            getSelectionModel();
          if (lo_col != -1 && hi_col != -1)
            {
              if (controlPressed && colModel.getSelectionMode() != 
                  ListSelectionModel.SINGLE_SELECTION)
                colModel.addSelectionInterval(lo_col, hi_col);
              else
            colModel.setSelectionInterval(lo_col, hi_col);
        }
    }

    public void mouseClicked(MouseEvent e) 
    {
    }
    public void mouseDragged(MouseEvent e) 
    {
      curr = new Point(e.getX(), e.getY());
      updateSelection(e.isControlDown());      
    }
    public void mouseEntered(MouseEvent e) 
    {
    }
    public void mouseExited(MouseEvent e) 
    {
    }
    public void mouseMoved(MouseEvent e) 
    {
    }
    public void mousePressed(MouseEvent e) 
    {
      begin = new Point(e.getX(), e.getY());
      curr = new Point(e.getX(), e.getY());
      //if control is pressed and the cell is already selected, deselect it
      if (e.isControlDown() && table.
          isCellSelected(table.rowAtPoint(begin),table.columnAtPoint(begin)))
        {                                       
          table.getSelectionModel().
            removeSelectionInterval(table.rowAtPoint(begin), 
                                    table.rowAtPoint(begin));
          table.getColumnModel().getSelectionModel().
            removeSelectionInterval(table.columnAtPoint(begin), 
                                    table.columnAtPoint(begin));
        }
      else
      updateSelection(e.isControlDown());
      
    }
    public void mouseReleased(MouseEvent e) 
    {
      begin = null;
      curr = null;
    }
  }

  protected FocusListener createFocusListener() 
  {
    return new FocusHandler();
  }
  protected KeyListener createKeyListener() 
  {
    return new KeyHandler();
  }
  protected MouseInputListener createMouseInputListener() 
  {
    return new MouseInputHandler();
  }

  public Dimension getMaximumSize(JComponent comp) 
  {
    return getPreferredSize(comp);
  }

  public Dimension getMinimumSize(JComponent comp) 
  {
    return getPreferredSize(comp);
  }

  public Dimension getPreferredSize(JComponent comp) 
  {
    int width = table.getColumnModel().getTotalColumnWidth();
    int height = table.getRowCount() * table.getRowHeight();
    return new Dimension(width, height);
  }

  protected void installDefaults() 
  {
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    table.setFont(defaults.getFont("Table.font"));
    table.setGridColor(defaults.getColor("Table.gridColor"));
    table.setForeground(defaults.getColor("Table.foreground"));
    table.setBackground(defaults.getColor("Table.background"));
    table.setSelectionForeground(defaults.getColor("Table.selectionForeground"));
    table.setSelectionBackground(defaults.getColor("Table.selectionBackground"));
    table.setOpaque(true);

    highlightCellBorder = defaults.getBorder("Table.focusCellHighlightBorder");
    cellBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
  }
  protected void installKeyboardActions() 
  {
  }

  protected void installListeners() 
  {
    table.addFocusListener(focusListener);  
    table.addKeyListener(keyListener);
    table.addMouseListener(mouseInputListener);    
    table.addMouseMotionListener(mouseInputListener);
  }

  protected void uninstallDefaults() 
  {
    // TODO: this method used to do the following which is not
    // quite right (at least it breaks apps that run fine with the
    // JDK):
    //
    // table.setFont(null);
    // table.setGridColor(null);
    // table.setForeground(null);
    // table.setBackground(null);
    // table.setSelectionForeground(null);
    // table.setSelectionBackground(null);
    //
    // This would leave the component in a corrupt state, which is
    // not acceptable. A possible solution would be to have component
    // level defaults installed, that get overridden by the UI defaults
    // and get restored in this method. I am not quite sure about this
    // though. / Roman Kennke
  }

  protected void uninstallKeyboardActions() 
  {
  }

  protected void uninstallListeners() 
  {
    table.removeFocusListener(focusListener);  
    table.removeKeyListener(keyListener);
    table.removeMouseListener(mouseInputListener);    
    table.removeMouseMotionListener(mouseInputListener);
  }

  public void installUI(JComponent comp) 
  {
    table = (JTable)comp;
    focusListener = createFocusListener();  
    keyListener = createKeyListener();
    mouseInputListener = createMouseInputListener();
    installDefaults();
    installKeyboardActions();
    installListeners();
  }

  public void uninstallUI(JComponent c) 
  {
    uninstallListeners();
    uninstallKeyboardActions();
    uninstallDefaults();    
  }

  public void paint(Graphics gfx, JComponent ignored) 
  {
    int ncols = table.getColumnCount();
    int nrows = table.getRowCount();
    if (nrows == 0 || ncols == 0)
      return;

    Rectangle clip = gfx.getClipBounds();
    TableColumnModel cols = table.getColumnModel();

    int height = table.getRowHeight();
    int x0 = 0, y0 = 0;
    int x = x0;
    int y = y0;

    Dimension gap = table.getIntercellSpacing();
    int ymax = clip.y + clip.height;
    int xmax = clip.x + clip.width;

    // paint the cell contents
    for (int c = 0; c < ncols && x < xmax; ++c)
      {
        y = y0;
        TableColumn col = cols.getColumn(c);
        int width = col.getWidth();
        int modelCol = col.getModelIndex();

        for (int r = 0; r < nrows && y < ymax; ++r)
          {
            Rectangle bounds = new Rectangle(x, y, width, height);
              if (bounds.intersects(clip))
              {
                TableCellRenderer rend = table.getCellRenderer(r, c);
                Component comp = table.prepareRenderer(rend, r, c);
                gfx.translate(x, y);
                comp.setBounds(new Rectangle(0, 0, width, height));
                // Set correct border on cell renderer.
                // Only the lead selection cell gets a border
                if (comp instanceof JComponent)
                  {
                    if (table.getSelectionModel().getLeadSelectionIndex() == r
                        && table.getColumnModel().getSelectionModel().
                        getLeadSelectionIndex() == c)
                      ((JComponent) comp).setBorder(highlightCellBorder);
                    else
                      ((JComponent) comp).setBorder(cellBorder);
                  }
                comp.paint(gfx);
                gfx.translate(-x, -y);
              }
              y += height;
              if (gap != null)
                y += gap.height;
          }
        x += width;
        if (gap != null)
          x += gap.width;
      }

    // tighten up the x and y max bounds
    ymax = y;
    xmax = x;

    Color grid = table.getGridColor();    

    // paint vertical grid lines    
    if (grid != null && table.getShowVerticalLines())
      {    
        x = x0;
        Color save = gfx.getColor();
        gfx.setColor(grid);
        boolean paintedLine = false;
        for (int c = 0; c < ncols && x < xmax; ++c)
          {
            x += cols.getColumn(c).getWidth();;
            if (gap != null)
              x += gap.width;
            gfx.drawLine(x, y0, x, ymax);
            paintedLine = true;
          }
        gfx.setColor(save);
      }

    // paint horizontal grid lines    
    if (grid != null && table.getShowHorizontalLines())
      {    
        y = y0;
        Color save = gfx.getColor();
        gfx.setColor(grid);
        boolean paintedLine = false;
        for (int r = 0; r < nrows && y < ymax; ++r)
          {
            y += height;
            if (gap != null)
              y += gap.height;
            gfx.drawLine(x0, y, xmax, y);
            paintedLine = true;
          }
        gfx.setColor(save);
      }

  }
}
