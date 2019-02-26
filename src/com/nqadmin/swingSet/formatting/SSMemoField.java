/* $Id$
 *
 * Tab Spacing = 4
 *
 * Copyright (c) 2004-2006, The Pangburn Company, Prasanth R. Pasala and
 * Diego Gil
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.  Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution.  The names of its contributors may not be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.nqadmin.swingSet.formatting;

import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import javax.sql.RowSetListener;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.nqadmin.swingSet.SSDataNavigator;
import com.nqadmin.swingSet.datasources.SSRowSet;

/**
 *
 * @author dags
 */
public class SSMemoField extends JTextArea implements RowSetListener, KeyListener, FocusListener {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7984808092295218942L;
	private java.awt.Color std_color = null;
    private String columnName = null;
    private int colType = -99;
    private SSRowSet rowset = null;
    private SSDataNavigator navigator = null;
    
    /** 
     * Creates a new instance of SSBooleanField 
     */
    public SSMemoField() {
        super();
        
        this.setLineWrap(true);
        this.setWrapStyleWord(true);
        
        Set<AWTKeyStroke> forwardKeys    = getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newForwardKeys = new HashSet<AWTKeyStroke>(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, java.awt.event.InputEvent.SHIFT_MASK ));
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newForwardKeys);
        
        Set<AWTKeyStroke> backwardKeys    = getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newBackwardKeys = new HashSet<AWTKeyStroke>(backwardKeys);
        newBackwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_UP, java.awt.event.InputEvent.SHIFT_MASK ));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,newBackwardKeys);
        
        addKeyListener(this);
        addFocusListener(this);
        this.setInputVerifier(new internalVerifier());
    }
    
    /**
     * Returns the column name to which the component is bound to
     * @return - returns the column name to which the component is bound to
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Sets the column name to which the component should be bound to
     * @param columnName - column name to which the component will be bound to
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
        bind();
    }

    /**
     * Sets the SSRowSet object to be used to get/set the value of the bound column
     * @param rowset - SSRowSet object to be used to get/set the value of the bound column
     * @deprecated
     * @see #setSSRowSet(SSRowSet)
     */
    public void setRowSet(SSRowSet rowset) {
        setSSRowSet(rowset);
    }
    
    /**
     * SSRowSet object being used to get/set the bound column value
     * @return - returns the SSRowSet object being used to get/set the bound column value
     * @deprecated
     * @see #getSSRowSet()
     **/
    public SSRowSet getRowSet() {
        return getSSRowSet();
    }
    
    /**
     * Sets the SSRowSet object to be used to get/set the value of the bound column
     * @param rowset - SSRowSet object to be used to get/set the value of the bound column
     */
    public void setSSRowSet(SSRowSet rowset) {
        this.rowset = rowset;
        bind();
    }
    
    /**
     * SSRowSet object being used to get/set the bound column value
     * @return - returns the SSRowSet object being used to get/set the bound column value
     */
      public SSRowSet getSSRowSet() {
        return this.rowset;
    }
    
	/**
	 * Sets the SSDataNavigator being used to navigate the SSRowSet
	 * This is needed only if you want to include the function keys as short cuts to perform operations on the DataNavigator
	 * like saving the current row/ undo changes/ delete current row.
	 * <font color=red>The functionality for this is not yet finalized so try to avoid using this </font>
	 * @param navigator - SSDataNavigator being used to navigate the SSRowSet   
	 * @deprecated
	 * @see #setSSDataNavigator(SSDataNavigator) 
	 */
    public void setNavigator(SSDataNavigator navigator) {
        this.setSSDataNavigator(navigator);
    }
    
    /**
     * Returns the SSDataNavigator object being used.
     * @return returns the SSDataNavigator object being used.
     * @deprecated
     * @see #getSSDataNavigator()
     **/
    public SSDataNavigator getNavigator() {
        return this.getSSDataNavigator();
    }
    
    /**
     * Sets the SSDataNavigator being used to navigate the SSRowSet
     * This is needed only if you want to include the function keys as short cuts to perform operations on the DataNavigator
     * like saving the current row/ undo changes/ delete current row.
     * <font color=red>The functionality for this is not yet finalized so try to avoid using this </font>
     * @param navigator - SSDataNavigator being used to navigate the SSRowSet
     */
     public void setSSDataNavigator(SSDataNavigator navigator) {
        this.navigator = navigator;
        setSSRowSet(navigator.getSSRowSet());
        bind();
    }
    
     /**
      * Returns the SSDataNavigator object being used.
      * @return returns the SSDataNavigator object being used.
      */
     public SSDataNavigator getSSDataNavigator() {
        return this.navigator;
    }
     
    private void DbToFm() {
        
        try {
            
            if (rowset.getRow() == 0) return;
            
            switch(colType) {
                
                case java.sql.Types.VARCHAR://-7
                case java.sql.Types.LONGVARCHAR://-7
                case java.sql.Types.CHAR://-7
                    this.setText(rowset.getString(columnName));
                    break;
                    
                default:
                    break;
            }
        } catch (java.sql.SQLException sqe) {
            System.out.println("Error in DbToFm() = " + sqe);
            this.setText("");
        }
    }
    
    /**
     * Sets the SSRowSet and column name to which the component is to be bound.
     *
     * @param _sSRowSet    datasource to be used.
     * @param _columnName  Name of the column to which this check box should be bound
     */
    public void bind(SSRowSet _sSRowSet, String _columnName) {
        rowset = _sSRowSet;
        columnName = _columnName;
        bind();
    }

    private void bind() {
        
        if (this.columnName == null) return;
        if (this.rowset  == null) return;
        
        try {
            colType = rowset.getColumnType(columnName);
        } catch(java.sql.SQLException sqe) {
            System.out.println("bind error = " + sqe);
        }
        rowset.addRowSetListener(this);
        DbToFm();
    }
    
    /* (non-Javadoc)
     * @see javax.sql.RowSetListener#rowSetChanged(javax.sql.RowSetEvent)
     */
    public void rowSetChanged(javax.sql.RowSetEvent event) {
        
    }
    
    /* (non-Javadoc)
     * @see javax.sql.RowSetListener#rowChanged(javax.sql.RowSetEvent)
     */
    public void rowChanged(javax.sql.RowSetEvent event) {
        
    }
    
    /* (non-Javadoc)
     * @see javax.sql.RowSetListener#cursorMoved(javax.sql.RowSetEvent)
     */
    public void cursorMoved(javax.sql.RowSetEvent event) {
        DbToFm();
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
    }
    
    /**
     *  Catch severals keys, to implement some forms functionality (To be done).
     */
    public void keyPressed(KeyEvent e) {
        
        if (e.getKeyCode() == KeyEvent.VK_F1) {
            //showHelper(e);
        }
        
        if (e.getKeyCode() == KeyEvent.VK_F2) {
        }
        
        if (e.getKeyCode() == KeyEvent.VK_F3) {
            System.out.println("F3 ");
            //calculator = new javax.swing.JPopupMenu();
            //calculator.add(new com.nqadmin.swingSet.formatting.utils.JCalculator());
            //JFormattedTextField ob = (JFormattedTextField)(e.getSource());
            //java.awt.Dimension d = ob.getSize();
            //calculator.show(ob, 0, d.height);
            
            //((Component)e.getSource()).transferFocus();
        }
        
        if (e.getKeyCode() == KeyEvent.VK_F4) {
            System.out.println("F4 ");
            //((Component)e.getSource()).transferFocus();
        }
        
        if (e.getKeyCode() == KeyEvent.VK_F5) {
            System.out.println("F5 = PROCESS");
            if (navigator.updatePresentRow()==true) {
                System.out.println("Update Sucessfully");
            }
        }
        
        if (e.getKeyCode() == KeyEvent.VK_F6) {
            System.out.println("F6 = DELETE");
            navigator.doDeleteButtonClick();
        }
        
        if (e.getKeyCode() == KeyEvent.VK_F8) {
            System.out.println("F8 ");
            //((Component)e.getSource()).transferFocus();
            navigator.doUndoButtonClick();
        }
        
        if (e.getKeyCode() == KeyEvent.VK_END) {
            System.out.println("END ");
            //((Component)e.getSource()).transferFocus();
        }
        
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            System.out.println("DELETE ");
            //((Component)e.getSource()).transferFocus();
        }
        
        if (e.getKeyCode() == KeyEvent.VK_HOME) {
            System.out.println("HOME ");
            //((Component)e.getSource()).transferFocus();
        }
        
    }
    
    
    /* (non-Javadoc)
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    public void focusLost(FocusEvent e) {
        /**
         * some code to highlight the component with the focus
         *
         */
        setBackground(std_color);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    public void focusGained(FocusEvent e) {
        
        /**
         * some code to highlight the component with the focus
         *
         */
        java.awt.Color col = new java.awt.Color(204,255,255);
        std_color = getBackground();
        setBackground(col);
        
        
        /**
         * This is a bug workaround
         * see : http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4740914
         *
         */
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                selectAll();
            }
        });
    }
    
    /**
     * This method should implements validation AND, most important for our purposes
     * implements actual rowset fields updates.
     *
     */
    
    class internalVerifier extends InputVerifier {
        
        public boolean verify(JComponent input) {
            
            String aux = null;
            boolean passed = true;
            
            SSMemoField tf = (SSMemoField) input;
            aux = tf.getText();
            
            if (passed == true) {
                
                setBackground(java.awt.Color.WHITE);
             
                // if not linked to a db field, returns.
                if (columnName == null || rowset == null) return true;

                try {
                    rowset.removeRowSetListener(tf);
                    
                    switch(colType) {
                        
                        case java.sql.Types.VARCHAR://-7
                        case java.sql.Types.LONGVARCHAR://-7
                        case java.sql.Types.CHAR://-7
                            rowset.updateString(columnName, aux);
                            break;
                            
                        default:
                            break;
                    }
                    rowset.addRowSetListener(tf);
                } catch (java.sql.SQLException se) {
                    System.out.println("SSMemoField ---> SQLException -----------> " + se);
                    tf.setText("");
                } catch(java.lang.NullPointerException np) {
                    System.out.println("SSMemoField ---> NullPointerException ---> " + np);
                    tf.setText("");
                }
                return true;
            } 
            /*
             * Validation fails.
             *
             */
            
            setBackground(java.awt.Color.RED);
            return false;            
        }
    }
}

/*
* $Log$
* Revision 1.11  2006/04/27 22:02:45  prasanth
* Added/updated java doc
*
*/