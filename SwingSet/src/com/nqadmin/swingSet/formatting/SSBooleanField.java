/* $Id$
 *
 * Tab Spacing = 4
 *
 * Copyright (c) 2004, The Pangburn Company, Inc, Prasanth R. Pasala and
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

import com.nqadmin.swingSet.datasources.SSRowSet;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.sql.RowSetListener;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author dags
 */
public class SSBooleanField extends JCheckBox implements RowSetListener {
    
    private java.awt.Color std_color = null;
    private String columnName = null;
    private int colType = -99;
    private SSRowSet rowset = null;
    
    /** Creates a new instance of SSBooleanField */
    public SSBooleanField() {
        super();
        
        Set forwardKeys    = getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set newForwardKeys = new HashSet(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, java.awt.event.InputEvent.SHIFT_MASK ));
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newForwardKeys);
        
        Set backwardKeys    = getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        Set newBackwardKeys = new HashSet(backwardKeys);
        newBackwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_UP, java.awt.event.InputEvent.SHIFT_MASK ));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,newBackwardKeys);
        
        this.setInputVerifier(new internalVerifier());
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
        bind(rowset, columnName);
    }
    
    public void setRowSet(SSRowSet rowset) {
        this.rowset = rowset;
        bind(rowset, columnName);
    }
        
    private void DbToFm() {
        
        try {
            
            switch(colType) {
                
                case java.sql.Types.BIT://-7
                    this.setSelected(rowset.getBoolean(columnName));
                    break;
                    
                case java.sql.Types.BOOLEAN://16
                    this.setSelected(rowset.getBoolean(columnName));
                    break;
                    
                case java.sql.Types.INTEGER://4
                case java.sql.Types.BIGINT://-5
                case java.sql.Types.SMALLINT://5
                case java.sql.Types.TINYINT://-6
                    if (rowset.getInt(columnName) == 1)
                        this.setSelected(true);
                    else
                        this.setSelected(false);
                    break;
                    
                default:
                    break;
            }
        } catch (java.sql.SQLException sqe) {
            System.out.println("Error in DbToFm() = " + sqe);
        }
    }
    
    public void bind(com.nqadmin.swingSet.datasources.SSRowSet rowset, String columnName) {
        this.columnName = columnName;
        this.rowset = rowset;
     
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
    
    public void rowSetChanged(javax.sql.RowSetEvent event) {
        
    }
    
    public void rowChanged(javax.sql.RowSetEvent event) {
        
    }
    
    public void cursorMoved(javax.sql.RowSetEvent event) {
        DbToFm();
    }
    
    /**
     * This method should implements validation AND, most important for our purposes
     * implements actual rowset fields updates.
     *
     */
    
    class internalVerifier extends InputVerifier {
        
        public boolean verify(JComponent input) {
            
            Boolean aux = null;
            boolean passed = true;
            
            SSBooleanField tf = (SSBooleanField) input;
            aux = tf.isSelected();
            
            System.out.println("inputVerifier():");
            
            if (aux == null) {
                passed = false;
            }
            
            if (passed == true) {
                
                setBackground(java.awt.Color.WHITE);
                
                try {
                    rowset.removeRowSetListener(tf);
                    
                    switch(colType) {
                        
                        case java.sql.Types.BIT://-7
                            rowset.updateBoolean(columnName, aux);
                            break;
                            
                        case java.sql.Types.BOOLEAN://16
                            rowset.updateBoolean(columnName, aux);
                            break;
                            
                        case java.sql.Types.INTEGER:    //4
                        case java.sql.Types.BIGINT:     //-5
                        case java.sql.Types.SMALLINT:   //5
                        case java.sql.Types.TINYINT:    //-6
                            if (aux == true) {
                                rowset.updateInt(columnName, 1);
                            } else {
                                rowset.updateInt(columnName, 0);
                            }
                            break;
                            
                        default:
                            break;
                    }
                    rowset.addRowSetListener(tf);
                } catch (java.sql.SQLException se) {
                    System.out.println("---> SQLException -----------> " + se);
                } catch(java.lang.NullPointerException np) {
                    System.out.println("---> NullPointerException ---> " + np);
                }
                return true;
            } else {
                /*
                 * Validation fails.
                 *
                 */
                
                setBackground(java.awt.Color.RED);
                return false;
            }
        }
    }
}

/*
 * $Log$
 * Revision 1.3  2004/12/13 20:50:16  dags
 * Fix package name
 *
 * Revision 1.2  2004/12/13 18:46:13  prasanth
 * Added License.
 *
 */
