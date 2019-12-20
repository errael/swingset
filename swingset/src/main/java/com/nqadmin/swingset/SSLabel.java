/*******************************************************************************
 * Copyright (C) 2003-2019, Prasanth R. Pasala, Brian E. Pangburn, & The Pangburn Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Contributors:
 *   Prasanth R. Pasala
 *   Brian E. Pangburn
 *   Diego Gil
 *   Man "Bee" Vo
 ******************************************************************************/


package com.nqadmin.swingset;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.nqadmin.swingset.datasources.SSRowSet;

/**
 * SSLabel.java
 * 
 * SwingSet - Open Toolkit For Making Swing Controls Database-Aware
 * 
 * Used to display database values in a read-only JLabel.
 */
public class SSLabel extends JLabel {

    /**
	 * unique serial id
	 */
	private static final long serialVersionUID = -5232780793538061537L;

	/**
     * Text field bound to the SSRowSet.
     */
    protected JTextField textField = new JTextField();

    /**
     * SSRowSet from which component will get/set values.
     */
    protected SSRowSet sSRowSet;

    /**
     * SSRowSet column to which the component will be bound.
     */
    protected String columnName = "";

    /**
     * Component listener.
     */
    protected final MyLabelTextListener labelTextListener = new MyLabelTextListener();

    /**
     * Bound text field document listener.
     */
    protected final MyTextFieldDocumentListener textFieldDocumentListener = new MyTextFieldDocumentListener();

    /**
     * Empty constructor needed for deserialization. Creates a SSLabel instance
     * with no image and with an empty string for the title.
     */
    public SSLabel() {
        super("<label text here>");
        init();
    }

    /**
     * Creates a SSLabel instance with the specified image.
     *
     * @param _image    specified image for label
     */
    public SSLabel(Icon _image) {
		super(_image);
        init();
    }

    /**
     * Creates a SSLabel instance with the specified image and horizontal alignment.
     *
     * @param _image    specified image for label
     * @param _horizontalAlignment	horizontal alignment
     */
    public SSLabel(Icon _image, int _horizontalAlignment) {
		super(_image, _horizontalAlignment);
        init();
    }

    /**
     * Creates a SSLabel instance with no image and binds it to the specified
     * SSRowSet column.
     *
     * @param _sSRowSet    datasource to be used.
     * @param _columnName    name of the column to which this label should be bound
     */
    public SSLabel(SSRowSet _sSRowSet, String _columnName) {
        this.sSRowSet = _sSRowSet;
        this.columnName = _columnName;
        init();
        bind();
    }

    /**
     * Sets the SSRowSet column name to which the component is bound.
     *
     * @param _columnName    column name in the SSRowSet to which the component
     *    is bound
     */
    public void setColumnName(String _columnName) {
        String oldValue = this.columnName;
        this.columnName = _columnName;
        firePropertyChange("columnName", oldValue, this.columnName);
        bind();
    }

    /**
     * Returns the SSRowSet column name to which the component is bound.
     *
     * @return column name to which the component is bound
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Sets the SSRowSet to which the component is bound.
     *
     * @param _sSRowSet    SSRowSet to which the component is bound
     */
    public void setSSRowSet(SSRowSet _sSRowSet) {
        SSRowSet oldValue = this.sSRowSet;
        this.sSRowSet = _sSRowSet;
        firePropertyChange("sSRowSet", oldValue, this.sSRowSet);
        bind();
    }

    /**
     * Returns the SSRowSet to which the component is bound.
     *
     * @return SSRowSet to which the component is bound
     */
    public SSRowSet getSSRowSet() {
        return this.sSRowSet;
    }

    /**
     * Sets the SSRowSet and column name to which the component is to be bound.
     *
     * @param _sSRowSet    datasource to be used.
     * @param _columnName    Name of the column to which this check box should be bound
     */
    public void bind(SSRowSet _sSRowSet, String _columnName) {
        SSRowSet oldValue = this.sSRowSet;
        this.sSRowSet = _sSRowSet;
        firePropertyChange("sSRowSet", oldValue, this.sSRowSet);

        String oldValue2 = this.columnName;
        this.columnName = _columnName;
        firePropertyChange("columnName", oldValue2, this.columnName);

        bind();
    }

    /**
     * Initialization code.
     */
    protected void init() {

        // SET PREFERRED DIMENSIONS
            setPreferredSize(new Dimension(200,20));
    }

    /**
     * Method for handling binding of component to a SSRowSet column.
     */
    protected void bind() {

        // CHECK FOR NULL COLUMN/ROWSET
            if (this.columnName==null || this.columnName.trim().equals("") || this.sSRowSet==null) {
                return;
            }

        // REMOVE LISTENERS TO PREVENT DUPLICATION
            removeListeners();

        // BIND THE TEXT FIELD TO THE SPECIFIED COLUMN
            this.textField.setDocument(new SSTextDocument(this.sSRowSet, this.columnName));

        // SET THE LABEL DISPLAY
            updateDisplay();

        // ADD BACK LISTENERS
            addListeners();

    }

    /**
     * Updates the value displayed in the component based on the SSRowSet column
     * binding.
     */
    protected void updateDisplay() {

        // SET THE LABEL BASED ON THE VALUE IN THE TEXT FIELD
            setText(this.textField.getText());

    } // end protected void updateDisplay() {

    /**
     * Adds listeners for component and bound text field (where applicable).
     */
    private void addListeners() {
        this.textField.getDocument().addDocumentListener(this.textFieldDocumentListener);
        addPropertyChangeListener("text", this.labelTextListener);
    }

    /**
     * Removes listeners for component and bound text field (where applicable).
     */
    private void removeListeners() {
        this.textField.getDocument().removeDocumentListener(this.textFieldDocumentListener);
        removePropertyChangeListener("text", this.labelTextListener);
    }

    /**
     * Listener(s) for the bound text field used to propigate values back to the
     * component's value.
     */
    protected class MyTextFieldDocumentListener implements DocumentListener, Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = -6911906045174819801L;

		@Override
		public void changedUpdate(DocumentEvent de) {
            removePropertyChangeListener("text", SSLabel.this.labelTextListener);

            updateDisplay();

            addPropertyChangeListener("text", SSLabel.this.labelTextListener);
        }

        // WHEN EVER THERE IS A CHANGE IN THE VALUE IN THE TEXT FIELD CHANGE THE LABEL
        // ACCORDINGLY.
        @Override
		public void insertUpdate(DocumentEvent de) {
            removePropertyChangeListener("text", SSLabel.this.labelTextListener);

            updateDisplay();

            addPropertyChangeListener("text", SSLabel.this.labelTextListener);
        }

        // IF A REMOVE UPDATE OCCURS ON THE TEXT FIELD CHECK THE CHANGE AND SET THE
        // CHECK BOX ACCORDINGLY.
        @Override
		public void removeUpdate(DocumentEvent de) {
            removePropertyChangeListener("text", SSLabel.this.labelTextListener);

            updateDisplay();

            addPropertyChangeListener("text", SSLabel.this.labelTextListener);
        }
    } // end protected class MyTextFieldDocumentListener implements DocumentListener, Serializable {

    /**
     * Listener(s) for the component's value used to propigate changes back to
     * bound text field.
     */
    protected class MyLabelTextListener implements PropertyChangeListener, Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 6786673052979566820L;

		@Override
		public void propertyChange(PropertyChangeEvent pce) {
            SSLabel.this.textField.getDocument().removeDocumentListener(SSLabel.this.textFieldDocumentListener);

            SSLabel.this.textField.setText(getText());

            SSLabel.this.textField.getDocument().addDocumentListener(SSLabel.this.textFieldDocumentListener);
        }

    } // end protected class MyLabelTextListener implements ChangeListener, Serializable {

} // end public class SSLabel extends JLabel {

/*
 * $Log$
 * Revision 1.16  2005/02/21 16:31:33  prasanth
 * In bind checking for empty columnName before binding the component.
 *
 * Revision 1.15  2005/02/13 15:38:20  yoda2
 * Removed redundant PropertyChangeListener and VetoableChangeListener class variables and methods from components with JComponent as an ancestor.
 *
 * Revision 1.14  2005/02/12 03:29:26  yoda2
 * Added bound properties (for beans).
 *
 * Revision 1.13  2005/02/11 22:59:46  yoda2
 * Imported PropertyVetoException and added some bound properties.
 *
 * Revision 1.12  2005/02/11 20:16:05  yoda2
 * Added infrastructure to support property & vetoable change listeners (for beans).
 *
 * Revision 1.11  2005/02/10 21:10:23  yoda2
 * Added default label text to empty constructor so that label will be visible in BDK.
 *
 * Revision 1.10  2005/02/10 20:13:03  yoda2
 * Setter/getter cleanup & method reordering for consistency.
 *
 * Revision 1.9  2005/02/10 03:46:47  yoda2
 * Replaced all setDisplay() methods & calls with updateDisplay() methods & calls to prevent any setter/getter confusion.
 *
 * Revision 1.8  2005/02/09 17:29:55  yoda2
 * JavaDoc cleanup.
 *
 * Revision 1.7  2005/02/07 20:36:38  yoda2
 * Made private listener data members final.
 *
 * Revision 1.6  2005/02/05 05:16:33  yoda2
 * API cleanup.
 *
 * Revision 1.5  2005/02/04 22:48:54  yoda2
 * API cleanup & updated Copyright info.
 *
 * Revision 1.4  2005/02/01 17:32:38  yoda2
 * API cleanup.
 *
 * Revision 1.3  2005/01/03 02:58:03  yoda2
 * Added appropriate super() calls to non-empty constructors.
 *
 * Revision 1.2  2005/01/02 18:33:48  yoda2
 * Added back empty constructor needed for deserialization along with other potentially useful constructors from parent classes.
 *
 * Revision 1.1  2005/01/01 05:05:47  yoda2
 * Adding preliminary SwingSet implementations for JLabel & JSlider.
 *
 */