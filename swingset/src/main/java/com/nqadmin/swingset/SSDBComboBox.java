/*******************************************************************************
 * Copyright (C) 2003-2020, Prasanth R. Pasala, Brian E. Pangburn, & The Pangburn Group
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.JComboBox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nqadmin.swingset.datasources.SSConnection;
import com.nqadmin.swingset.utils.SSCommon;
import com.nqadmin.swingset.utils.SSComponentInterface;
import com.nqadmin.swingset.utils.SSListItem;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

// SSDBComboBox.java
//
// SwingSet - Open Toolkit For Making Swing Controls Database-Aware

/**
 * Similar to the SSComboBox, but used when both the 'bound' values and the
 * 'display' values are pulled from a database table. Generally the bound value
 * represents a foreign key to another table, and the combobox needs to display
 * a list of one (or more) columns from the other table.
 * <p>
 * Note, if changing both a sSRowSet and column name consider using the bind()
 * method rather than individual setSSRowSet() and setColumName() calls.
 * <p>
 * e.g.
 * <p>
 * Consider two tables: 1. part_data (part_id, part_name, ...) 2. shipment_data
 * (shipment_id, part_id, quantity, ...)
 * <p>
 * Assume you would like to develop a screen for the shipment table and you want
 * to have a screen with a combobox where the user can choose a part and a
 * textbox where the user can specify a quantity.
 * <p>
 * In the combobox you would want to display the part name rather than part_id
 * so that it is easier for the user to choose. At the same time you want to
 * store the id of the part chosen by the user in the shipment table.
 * <pre>
 * {@code
 * SSConnection connection = null;
 * SSJdbcRowSetImpl sSRowSet = null;
 * SSDataNavigator navigator = null;
 * SSDBComboBox combo = null;
 *
 * try {
 *
 * // CREATE A DATABASE CONNECTION OBJECT
 * SSConnection connection = new SSConnection(........);
 *
 * // CREATE AN INSTANCE OF SSJDBCROWSETIMPL
 * SSJdbcRowsetImpl sSRowSet = new SSJdbcRowsetImpl(connection);
 * sSRowSet.setCommand("SELECT * FROM shipment_data;");
 *
 * // DATA NAVIGATOR CALLS THE EXECUTE AND NEXT FUNCTIONS ON THE SSROWSET.
 * // IF YOU ARE NOT USING THE DATA NAVIGATOR YOU HAVE TO INCLUDE THOSE.
 * // sSRowSet.execute();
 * // sSRowSet.next();
 * SSDataNavigator navigator = new SSDataNavigator(sSRowSet);
 *
 * // QUERY FOR THE COMBOBOX.
 * String query = "SELECT * FROM part_data;";
 *
 * // CREATE AN INSTANCE OF THE SSDBCOMBOBOX WITH THE CONNECTION OBJECT
 * // QUERY AND COLUMN NAMES
 * combo = new SSDBComboBox(connection,query,"part_id","part_name");
 *
 * // THIS BASICALLY SPECIFIES THE COLUMN AND THE SSROWSET WHERE UPDATES HAVE
 * // TO BE MADE.
 * combo.bind(sSRowSet,"part_id");
 * combo.execute();
 *
 * // CREATE A TEXTFIELD
 * JTextField myText = new JTextField();
 * myText.setDocument(new SSTextDocument(sSRowSet, "quantity");
 *
 * } catch(Exception e) {
 *	// EXCEPTION HANDLER HERE...
 * }
 *
 *
 * // ADD THE SSDBCOMBOBOX TO THE JFRAME
 * getContentPane().add(combo.getComboBox());
 *
 * // ADD THE JTEXTFIELD TO THE JFRAME
 * getContentPane().add(myText);
 * }
 * </pre>
 */

public class SSDBComboBox extends JComboBox<SSListItem> implements SSComponentInterface {

	/**
	 * Listener(s) for the component's value used to propagate changes back to bound
	 * database column.
	 */
	protected class SSDBComboBoxListener implements ActionListener, Serializable {

		/**
		 * unique serial ID
		 */
		private static final long serialVersionUID = 5078725576768393489L;

		@Override
		public void actionPerformed(final ActionEvent ae) {

			removeSSRowSetListener();

			final int index = getSelectedIndex();

			if (index == -1) {
				logger.debug(getColumnForLog() + ": SSDBComboListener.actionPerformed setting bound column to  null.");
				setBoundColumnText(null);
			} else {
				logger.debug(getColumnForLog() + ": SSDBComboListener.actionPerformed setting bound column to " + getSelectedValue() + ".");
				setBoundColumnText(String.valueOf(getSelectedValue()));

			}

			addSSRowSetListener();
		}
	}


	/**
	 * Log4j Logger for component
	 */
	private static Logger logger = LogManager.getLogger();

	/**
	 * Value to represent that no item has been selected in the combo box.
	 */
	public static final int NON_SELECTED = (int) ((Math.pow(2, 32) - 1) / (-2));


	/**
	 * unique serial id
	 */
	private static final long serialVersionUID = -4203338788107410027L;

//	/**
//	 * Model to be used for holding and filtering data in combo box.
//	 */
//	 protected SelectorComboBoxModel selectorCBM = new SelectorComboBoxModel();

//    /**
//     * Text field bound to the SSRowSet.
//     * Bee changed to public
//     */
//    public JTextField textField = new JTextField();

//    /**
//     * Database connection used to execute queries for combo population.
//     */
//    protected SSConnection sSConnection = null;

	/**
	 * Indicates if GlazedList autocompletion has already been installed
	 */
	private boolean autoCompleteInstalled = false;

	/**
	 * Format for any date columns displayed in combo box.
	 */
	protected String dateFormat = "MM/dd/yyyy";

	/**
	 * The database column used to populate the first visible column of the combo
	 * box.
	 */
	protected String displayColumnName = "";

	/**
	 * Map of string/value pairings for the ComboBox (generally the text to be
	 * display (SSListItem) and its corresponding primary key)
	 */
	protected EventList<SSListItem> eventList;

	/**
	 * counter for # times that execute() method is called - for testing
	 */
	// TODO remove this
	protected int executeCount = 0;

	/**
	 * boolean value for activating or disabling the filter
	 * <p>
	 * Appears to determine if GlazedList is used for filtering or original
	 * keystroke listener/filter.
	 */
	protected boolean filterSwitch = true;

	/**
	 * Underlying database table primary key values corresponding to text displayed.
	 * <p>
	 * Note that mappings for the SSDBComboBox are Longs whereas in SSComboBox they
	 * are Integers.
	 */
	protected ArrayList<Long> mappings = null;

	/**
	 * Options to be displayed in the combobox (based on a query).
	 */
	protected ArrayList<String> options = null;

	/**
	 * The column name whose value is written back to the database when the user
	 * chooses an item in the combo box. This is generally the PK of the table to
	 * which a foreign key is mapped.
	 */
	protected String primaryKeyColumnName = "";

	/**
	 * True if inside a call to updateDisplay()
	 */
	//protected volatile boolean inUpdateDisplay = false;

//    /**
//     * Number of items in the combo box.
//     */
//    protected int numberOfItems = 0;

//    /**
//     * SSRowSet from which component will get/set values.
//     */
//    protected SSRowSet sSRowSet;

//    /**
//     * SSRowSet column to which the component will be bound.
//     */
//    protected String columnName = "";

//    /**
//     * Component listener.
//     */
//    private final MyComboListener cmbListener = new MyComboListener();

//    /**
//     * Bound text field document listener.
//     */
//    protected final MyTextFieldDocumentListener textFieldDocumentListener = new MyTextFieldDocumentListener();

//    /**
//     * Keystroke-based item selection listener.
//     */
//    protected final MyKeyListener myKeyListener = new MyKeyListener();
//
//    /**
//     * Listener for PopupMenu
//     */
//    protected MyPopupMenuListener myPopupMenuListener = new MyPopupMenuListener();
//
//    /**
//     * Listener for Filter
//     */
//    protected FilterFocusListener filterFocusListener = new FilterFocusListener();

	/**
	 * String typed by user into combobox
	 */
	protected String priorEditorText = "";

	/**
	 * Query used to populate combo box.
	 */
	protected String query = "";

	/**
	 * The database column used to populate the second (optional) visible column of
	 * the combo box.
	 */
	protected String secondDisplayColumnName = null;

	/**
	 * SSListItem currently selected in combobox. Needed because GlazedList can cause getSelectedIndex()
	 * to return -1 (while editing) or 0 (after selection is made from list subset)
	 */
	private SSListItem selectedItem = null;

	/**
	 * Alphanumeric separator used to separate values in multi-column comboboxes.
	 * <p>
	 * Changing from " - " to " | " for 2020 rewrite.
	 */
	protected String separator = " - ";

	/**
	 * Boolean to indicated that a call to setSelectedItem() is in progress.
	 */
	private boolean settingSelectedItem = false;

	/**
	 * Common fields shared across SwingSet components
	 */
	protected SSCommon ssCommon;

	/**
	 * Component listener.
	 */
	protected final SSDBComboBoxListener ssDBComboBoxListener = new SSDBComboBoxListener();

	/**
	 * Creates an object of the SSDBComboBox.
	 */
	public SSDBComboBox() {
		super();
		setSSCommon(new SSCommon(this));
		// SSCommon constructor calls init()
	}

	/**
	 * Constructs a SSDBComboBox with the given parameters.
	 *
	 * @param _ssConnection         database connection to be used.
	 * @param _query                query to be used to retrieve the values from the
	 *                              database.
	 * @param _primaryKeyColumnName column name whose value has to be stored.
	 * @param _displayColumnName    column name whose values are displayed in the
	 *                              combo box.
	 */
	public SSDBComboBox(final SSConnection _ssConnection, final String _query, final String _primaryKeyColumnName,
			final String _displayColumnName) {
		super();
		setSSCommon(new SSCommon(this));
		// SSCommon constructor calls init()
		setSSConnection(_ssConnection);
		// this.sSConnection = _sSConnection;
		setQuery(_query);
		setPrimaryKeyColumnName(_primaryKeyColumnName);
		setDisplayColumnName(_displayColumnName);
		// init();
	}

	/**
	 * Adds an item to the existing list of items in the combo box.
	 *
	 * @param _displayText text that should be displayed in the combobox
	 * @param _primaryKey  primary key value corresponding the the display text
	 */
	public void addItem(final String _displayText, final long _primaryKey) {

		// LOCK EVENT LIST
		eventList.getReadWriteLock().writeLock().lock();

		// INITIALIZE LISTS IF NULL
		if (eventList == null) {
			eventList = new BasicEventList<>();
		}
		if (mappings == null) {
			mappings = new ArrayList<Long>();
		}
		if (options == null) {
			options = new ArrayList<String>();
		}

		try {

			// create new list item
			final SSListItem listItem = new SSListItem(_primaryKey, _displayText);

			// add to lists
			eventList.add(listItem);
			mappings.add(listItem.getPrimaryKey());
			options.add(listItem.getListItem());

		} catch (final Exception e) {
			logger.error(getColumnForLog() + ": Exception.", e);
		} finally {
			eventList.getReadWriteLock().writeLock().unlock();
		}

// TODO Determine if any change is needed to actually add item to combobox.

	}

	/**
	 * Adds any necessary listeners for the current SwingSet component. These will
	 * trigger changes in the underlying RowSet column.
	 */
	@Override
	public void addSSComponentListener() {
		addActionListener(ssDBComboBoxListener);

	}

	/**
	 * Adds an item to the existing list of items in the combo box.
	 *
	 * @param _name  name that should be displayed in the combo
	 * @param _value value corresponding the the name
	 */
	@Deprecated
	public void addStringItem(final String _name, final String _value) {
		addItem(_name, Long.valueOf(_value));

	}

	/**
	 * Method to allow Developer to add functionality when SwingSet component is
	 * instantiated.
	 * <p>
	 * It will actually be called from SSCommon.init() once the SSCommon data member
	 * is instantiated.
	 */
	@Override
	public void customInit() {
		// SET PREFERRED DIMENSIONS
// TODO not sure SwingSet should be setting component dimensions
		setPreferredSize(new Dimension(200, 20));
// TODO This was added during SwingSet rewrite 4/2020. Need to confirm it doesn't break anything.
		setEditable(false); // GlazedList overrides this and sets it to true
	}

	/**
	 * Removes an item from the combobox and underlying lists based on the record
	 * primary key provided.
	 * <p>
	 * If more than one item is present in the combo for that value the first one is
	 * changed.
	 *
	 * @param _primaryKey primary key value for the item that should be removed
	 *
	 * @return returns true on successful deletion otherwise returns false.
	 */
	public boolean deleteItem(final long _primaryKey) {

		boolean result = false;

		if (eventList != null) {

			// LOCK EVENT LIST
			eventList.getReadWriteLock().writeLock().lock();

			try {

				// GET INDEX FOR mappings and options
				final int index = mappings.indexOf(_primaryKey);

				// PROCEED IF INDEX WAS FOUND
				if (index != -1) {
					options.remove(index);
					mappings.remove(index);
// TODO Confirm that eventList is not reordered by GlazedLists code.
					eventList.remove(index);
					result = true;

				}

			} catch (final Exception e) {
				logger.error(getColumnForLog() + ": Exception.", e);
			} finally {
				eventList.getReadWriteLock().writeLock().unlock();
			}

		}

		return result;

// TODO Determine if any change is needed to actually remove item from combobox.

	}

	/**
	 * Removes the display text provided from the combobox and removes any
	 * corresponding list items.
	 * <p>
	 * If more than one item is present in the combo for the specified value, only
	 * the first one is removed.
	 *
	 * @param _displayText value of the item to be deleted.
	 *
	 * @return returns true on successful deletion otherwise returns false.
	 */
	public boolean deleteStringItem(final String _displayText) {

		boolean result = false;

		if (options != null) {
			final int index = options.indexOf(_displayText);
			result = deleteItem(mappings.get(index));
		}

		return result;

	}

	/**
	 * Executes the query specified with setQuery(), populates combobox, and turns on AutoCompleteSupport
	 * <p>
	 * @throws Exception exception that occurs querying data or turning on AutoComplete
	 */
	public void execute() throws Exception {

		//System.out.println(getBoundColumnName() + " - " + "SSDBComboBox.execute() - setting execute count: " + executeCount++);
		// (re)query data
		queryData();

// Only install AutoCompleteSupport once.
// See https://stackoverflow.com/questions/15210771/autocomplete-with-glazedlists for info on modifying lists.
// See https://javadoc.io/doc/com.glazedlists/glazedlists/latest/ca/odell/glazedlists/swing/AutoCompleteSupport.html
// We would like to call autoComplete.setStrict(true), but it is not currently compatible with TextMatcherEditor.CONTAINS, which is the more important feature.
// Note that installing AutoComplete support makes the ComboBox editable.
// Should already in the event dispatch thread so don't use invokeAndWait()
		if (!autoCompleteInstalled) {
			final AutoCompleteSupport<SSListItem> autoComplete = AutoCompleteSupport.install(this, eventList);
			autoComplete.setFilterMode(TextMatcherEditor.CONTAINS);
			autoCompleteInstalled = true;
		}

		// autoComplete.setStrict(true);

// since the list was likely blank when the component was bound we need to update the component again so it can get the text from the list
// we don't want to do this if the component is unbound as with an SSDBComboBox used for navigation.
		if (getSSRowSet() != null) {
			updateSSComponent();
		}
	}

//    /**
//     * Sets the new SSRowSet for the combo box.
//     *
//     * @param _sSRowSet  SSRowSet to which the combo has to update values.
//     */
//    public void setSSRowSet(SSRowSet _sSRowSet) {
//        SSRowSet oldValue = this.sSRowSet;
//        this.sSRowSet = _sSRowSet;
//        firePropertyChange("sSRowSet", oldValue, this.sSRowSet);
//        bind();
//    }

//    /**
//     * Returns the SSRowSet being used to get the values.
//     *
//     * @return returns the SSRowSet being used.
//     */
//    public SSRowSet getSSRowSet() {
//        return this.sSRowSet;
//    }

//    /**
//     * Sets the connection object to be used.
//     *
//     * @param _sSConnection    connection object used for database.
//     */
//    public void setSSConnection(SSConnection _sSConnection) {
//        SSConnection oldValue = this.sSConnection;
//        this.sSConnection = _sSConnection;
//        firePropertyChange("sSConnection", oldValue, this.sSConnection);
//        bind();
//    }

//    /**
//     * Returns connection object used to get values from database.
//     *
//     * @return returns a SSConnection object.
//     */
//    public SSConnection getSSConnection() {
//        return this.sSConnection;
//    }

	/**
	 * Returns the pattern in which dates have to be displayed
	 *
	 * @return returns the pattern in which dates have to be displayed
	 */
	public String getDateFormat() {
		return dateFormat;
	}

//	/**
//	 * Adds listeners for Component, RowSet, Keyboard, and PopupMenu
//	 */
//	public void addListeners() {
//		SSComponentInterface.super.addListeners();
//		// addKeyListener(this.myKeyListener);
//		// addPopupMenuListener(this.myPopupMenuListener);
//	}

//    /**
//     * Sets the column name for the combo box
//     *
//     * @param _columnName   name of column
//     */
//    public void setColumnName(String _columnName) {
//        String oldValue = this.columnName;
//        this.columnName = _columnName;
//        firePropertyChange("columnName", oldValue, this.columnName);
//        bind();
//    }

//    /**
//     * Returns the column name to which the combo is bound.
//     *
//     * @return returns the column name to which to combo box is bound.
//     */
//    public String getColumnName() {
//        return this.columnName;
//    }

	/**
	 * Returns the column name whose values are displayed in the combo box.
	 *
	 * @return returns the name of the column used to get values for combo box
	 *         items.
	 */
	public String getDisplayColumnName() {
		return displayColumnName;
	}

	/**
	 * @return the eventList
	 */
	public EventList<SSListItem> getEventList() {
		return eventList;
	}

	/**
	 * Provides the initial number of items in the list underlying the CombobBox
	 * <p>
	 * NOTE: There does not appear to be any code that sets this value so marking as
	 * Deprecated.
	 *
	 * @return the initial number of items in the combobox list
	 */
	@Deprecated
	public int getInitialNumberOfItems() {
		// it appears code was never written to set this value so Depreciated and
		// returning 0
		// TODO Remove completely from future release.

		logger.warn(getColumnForLog() + ": This method was never properly implemented so it has been Deprecated and just returns 0. \n" + Thread.currentThread().getStackTrace());
		return 0;
	}

	/**
	 * @return the mappings
	 */
	public ArrayList<Long> getMappings() {
		return mappings;
	}

	/**
	 * Returns the number of items present in the combo box.
	 * <p>
	 * This is a read-only bean property.
	 *
	 * @return returns the number of items present in the combo box.
	 */
	@Deprecated
	public int getNumberOfItems() {
// TODO Determine where/how this is/was used.
		int result = 0;

		if (eventList != null) {
			result = eventList.size();
		}

		return result;
	}

	/**
	 * @return the options
	 */
	public ArrayList<String> getOptions() {
		return options;
	}

	/**
	 * @return the primaryKeyColumnName
	 */
	public String getPrimaryKeyColumnName() {
		return primaryKeyColumnName;
	}

	/**
	 * Returns the query used to retrieve values from database for the combo box.
	 *
	 * @return returns the query used.
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Returns the second column name whose values are also displayed in the combo
	 * box.
	 *
	 * @return returns the name of the column used to get values for combo box
	 *         items. returns NULL if the second display column is not provided.
	 */
	public String getSecondDisplayColumnName() {
		return secondDisplayColumnName;
	}

	/**
	 * Returns the text displayed in the combobox.
	 * <p>
	 * Currently not a bean property since there is no associated variable.
	 *
	 * @return value corresponding to the selected item in the combo. return null if
	 *         no item is selected.
	 */
	public String getSelectedStringValue() {

		String result = null;

		final SSListItem currentItem = (SSListItem)getSelectedItem();

		if (currentItem!=null) {
			result = currentItem.getListItem();
		}


		return result;

//        int index = getSelectedIndex();
//
//        if (index == -1) {
//            return null;
//        }
//
//        return ""+this.selectorCBM.getSelectedBoundData(index);
	} // public String getSelectedStringValue() {
//
//    /**
//     * Map of string/value pairings for the ComboBox.
//     */
//    public Map<String, Long> itemMap;
//
//    /**
//     * Executes the query and adds items to the combo box based on the values
//     * retrieved from the database.
//     * @throws SQLException 	SQLException
//     * @throws Exception 	Exception
//     */
//    public void execute() throws SQLException, Exception {
//        // TURN OFF LISTENERS
//            removeListeners();
//
//            if (this.query.equals("")) {
//                throw new Exception("Query is empty");
//            }
//        	this.selectorCBM.setQuery(this.query);
//        	this.selectorCBM.setDateFormat(this.dateFormat);
//        	this.selectorCBM.setPrimaryKeyColumn(this.primaryKeyColumnName);
//    		this.selectorCBM.setDisplayColumn(this.displayColumnName);
//    		this.selectorCBM.setSecondDisplayColumn(this.secondDisplayColumnName);
//    		this.selectorCBM.setSeparator(this.seperator);
//    		this.selectorCBM.setSSConnection(this.sSConnection);
//    		this.selectorCBM.refresh();
//    		setModel(this.selectorCBM);
//    		this.itemMap = this.selectorCBM.itemMap;
//    		this.numberOfItems = this.selectorCBM.getSize();
//
//    		// UPDATE DISPLAY WILL ADD THE LISTENERS AT THE END SO NO NEED TO ADD IT AGAIN.
//            updateDisplay();
//
//            //ADD THE LISTENERS BACK
//            addListeners();
//
//    }

	/**
	 * Returns the underlying database record primary key value corresponding to the
	 * currently selected item in the combobox.
	 * <p>
	 * Currently not a bean property since there is no associated variable.
	 *
	 * @return returns the value associated with the selected item OR -1 if nothing
	 *         is selected.
	 */
	public long getSelectedValue() {
// TODO Consider overriding getSelectedIndex() to account for GlazedList impact
		logger.debug(getColumnForLog() + ": Call to getSelectedValue().");

		Long result;

		// 2020-10-03_BP: getSelectedValue() seems to be the root of problems with filtered/glazed lists.
		// When filtering is taking place, getSelectedIndex() returns -1

		// Determine if the call to getSelectedValue() is happening during a call to setSelectedItem()
		if (settingSelectedItem) {
			if (selectedItem == null) {
				result = (long) NON_SELECTED;
			} else {
				result = selectedItem.getPrimaryKey();
			}
		} else {
		// Existing code not impacted by GlazedList dynamically impacting the list.
			if (getSelectedIndex() == -1) {
				result = (long) NON_SELECTED;
			} else {
				result = mappings.get(getSelectedIndex());

			}

		}

		// If anything above returned null, change to NON_SELECTED.
		if (result==null) {
			result = (long) NON_SELECTED;
		}

		return result;
	}

	/**
	 * Returns the separator used when multiple columns are displayed
	 *
	 * @return separator used.
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * Returns the separator used when multiple columns are displayed
	 * <p>
	 * Deprecated for misspelling.
	 *
	 * @return separator used.
	 */
	@Deprecated
	public String getSeperator() {
		return separator;
	}

	/**
	 * Returns the ssCommon data member for the current Swingset component.
	 *
	 * @return shared/common SwingSet component data and methods
	 */
	@Override
	public SSCommon getSSCommon() {
		return ssCommon;
	}

//    /**
//     * Sets the currently selected value
//     *
//     * Currently not a bean property since there is no associated variable.
//     *
//     * @param _value    value to set as currently selected.
//     */
//    public void setSelectedValue(long _value) {
//        this.textField.setText(String.valueOf(_value));
//    }

	/**
	 * Converts the database column value into string. Only date columns are
	 * formated as specified by dateFormat variable all other column types are
	 * retrieved as strings
	 *
	 * @param _rs         ResultSet containing database column to convert to string
	 * @param _columnName database column to convert to string
	 * @return string value of database column
	 */
	protected String getStringValue(final ResultSet _rs, final String _columnName) {
		String strValue = "";
		try {
			final int type = _rs.getMetaData().getColumnType(_rs.findColumn(_columnName));
			switch (type) {
			case Types.DATE:
				final SimpleDateFormat myDateFormat = new SimpleDateFormat(dateFormat);
				strValue = myDateFormat.format(_rs.getDate(_columnName));
				break;
			default:
				strValue = _rs.getString(_columnName);
				break;
			}
			if (strValue == null) {
				strValue = "";
			}
		} catch (final SQLException se) {
			logger.error(getColumnForLog() + ": SQL Exception.", se);
		}
		return strValue;

	}

//    /**
//     * Returns the bound key value of the currently selected item.
//     *
//     * Currently not a bean property since there is no associated variable.
//     *
//     * @return value corresponding to the selected item in the combo.
//     *     return -1 if no item is selected.
//     */
//    public long getSelectedValue() {
//
//// TODO revisit returning -1 if nothing is selected as that could be a legitimate bound pk value (unlikely)
//
//    	long returnValue = -1;
//
//        int index = getSelectedIndex();
//
//        if (index == -1) {
//            // NOTHING TO DO return -1;
//        } else {
//        	returnValue = comboMap.
//        }
//
//        returnValue
//		long returnVal =  Long.valueOf(this.selectorCBM.getSelectedBoundData(index).toString());
//
//
//        return returnVal;
//
//    }

	/**
	 * Populates the list model with the data by fetching it from the database.
	 */
	private void queryData() {

		if (eventList != null) {
// TODO look at .dispose() vs .clear()
			logger.trace(getColumnForLog() + ": Clearing eventList.");
			eventList.clear();
		} else {
			eventList = new BasicEventList<>();
		}
		if (mappings != null) {
			mappings.clear();
		} else {
			mappings = new ArrayList<Long>();
		}
		if (options != null) {
			options.clear();
		} else {
			options = new ArrayList<String>();
		}

		eventList.getReadWriteLock().writeLock().lock();

		Long primaryKey = null;
		String firstColumnString = null;
		String secondColumnString = null;
		SSListItem listItem = null;
		ResultSet rs = null;

		// this.data.getReadWriteLock().writeLock().lock();
		try {
			logger.debug(getColumnForLog() + ": Nulls allowed? " + getAllowNull());
			// 2020-07-24: adding support for a nullable first item if nulls are supported
			// 2020-10-02: For a SSDBComboBox used as a navigator, we don't want a null first item. Look at getBoundColumnName().
			if (getAllowNull() && (getBoundColumnName()!=null)) {
				listItem = new SSListItem(null, "");
				logger.debug(getColumnForLog() + ": Adding blank list item - " + listItem);
				eventList.add(listItem);
				mappings.add(listItem.getPrimaryKey());
				options.add(listItem.getListItem());
			}

			final Statement statement = ssCommon.getSSConnection().getConnection().createStatement();
			rs = statement.executeQuery(getQuery());

			logger.debug(getColumnForLog() + ": Query - " + getQuery());

			while (rs.next()) {
				// extract primary key
				primaryKey = rs.getLong(getPrimaryKeyColumnName());

				// extract first column string
				// getStringValue() takes care of formatting dates
				firstColumnString = getStringValue(rs, displayColumnName);
				logger.trace(getColumnForLog() + ": First column to display - " + firstColumnString);

				// extract second column string, if applicable
				// getStringValue() takes care of formatting dates
				secondColumnString = null;
				if ((secondDisplayColumnName != null) && !secondDisplayColumnName.equals("")) {
					secondColumnString = rs.getString(secondDisplayColumnName);
					if (secondColumnString.equals("")) {
						secondColumnString = null;
					}
					logger.trace(getColumnForLog() + ": Second column to display - " + secondColumnString);
				}

				// build eventList item
				if (secondColumnString != null) {
					listItem = new SSListItem(primaryKey, firstColumnString + separator + secondColumnString);
				} else {
					listItem = new SSListItem(primaryKey, firstColumnString);
				}

				// add to lists
				eventList.add(listItem);
				mappings.add(listItem.getPrimaryKey());
				options.add(listItem.getListItem());

			}
			rs.close();

		} catch (final SQLException se) {
			logger.error(getColumnForLog() + ": SQL Exception.", se);
		} catch (final java.lang.NullPointerException npe) {
			logger.error(getColumnForLog() + ": Null Pointer Exception.", npe);
		} finally {
			eventList.getReadWriteLock().writeLock().unlock();
		}
	}

	/**
	 * Removes any necessary listeners for the current SwingSet component. These
	 * will trigger changes in the underlying RowSet column.
	 */
	@Override
	public void removeSSComponentListener() {
		removeActionListener(ssDBComboBoxListener);

	}

	/**
	 * When a display column is of type date you can choose the format in which it
	 * has to be displayed. For the pattern refer SimpleDateFormat in java.text
	 * package.
	 *
	 * @param _dateFormat pattern in which dates have to be displayed
	 */
	public void setDateFormat(final String _dateFormat) {
		final String oldValue = dateFormat;
		dateFormat = _dateFormat;
		firePropertyChange("dateFormat", oldValue, dateFormat);
	}

	/**
	 * Sets the column name whose values have to be displayed in combo box.
	 *
	 * @param _displayColumnName column name whose values have to be displayed.
	 */
	public void setDisplayColumnName(final String _displayColumnName) {
		final String oldValue = displayColumnName;
		displayColumnName = _displayColumnName;
		firePropertyChange("displayColumnName", oldValue, displayColumnName);
	}

//	/**
//	 * Adds listeners for Component, RowSet, Keyboard, and PopupMenu
//	 */
//	public void removeListeners() {
//		SSComponentInterface.super.removeListeners();
//		// removeKeyListener(this.myKeyListener);
//		// removePopupMenuListener(this.myPopupMenuListener);
//	}

//    /**
//     * Sets the SSRowSet and column name to which the component is to be bound.
//     *
//     * @param _sSRowSet    datasource to be used.
//     * @param _columnName    Name of the column to which this check box should be bound
//     */
//    public void bind(SSRowSet _sSRowSet, String _columnName) {
//        SSRowSet oldValue = this.sSRowSet;
//        this.sSRowSet = _sSRowSet;
//        firePropertyChange("sSRowSet", oldValue, this.sSRowSet);
//
//        String oldValue2 = this.columnName;
//        this.columnName = _columnName;
//        firePropertyChange("columnName", oldValue2, this.columnName);
//
//        bind();
//    }

	/**
	 * @param eventList the eventList to set
	 */
	public void setEventList(final EventList<SSListItem> eventList) {
		this.eventList = eventList;
	}

	/**
	 * Method that sets the combo box to be filterable.
	 * <p>
	 * GlazedList filtering is now fully integrated so this no longer serves a
	 * purpose.
	 *
	 * @param _filter boolean to turn filtering on or off
	 */
	@Deprecated
	public void setFilterable(final boolean _filter) {
		// TODO remove this method in future release
		filterSwitch = _filter;
		logger.warn(getColumnForLog() + ": This method has been Deprecated because GlazedList filtering is now fully integrated.\n" + Thread.currentThread().getStackTrace());
	}

	/**
	 * @param mappings the mappings to set
	 */
	public void setMappings(final ArrayList<Long> mappings) {
		this.mappings = mappings;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(final ArrayList<String> options) {
		this.options = options;
	}

	/**
	 * Sets the database table primary column name.
	 *
	 * @param _primaryKeyColumnName name of primary key column
	 */
	public void setPrimaryKeyColumnName(final String _primaryKeyColumnName) {
		final String oldValue = primaryKeyColumnName;
		primaryKeyColumnName = _primaryKeyColumnName;
		firePropertyChange("primaryKeyColumnName", oldValue, primaryKeyColumnName);
	}

	/**
	 * Sets the query used to display items in the combo box.
	 *
	 * @param _query query to be used to get values from database (to display combo
	 *               box items)
	 */
	public void setQuery(final String _query) {
		final String oldValue = query;
		query = _query;
		firePropertyChange("query", oldValue, query);
	}

//    /**
//     * Initialization code.
//     */
//    protected void init() {
//       // // TRANSFER FOCUS TO NEXT ELEMENT WHEN ENTER KEY IS PRESSED
//        //Set<AWTKeyStroke> forwardKeys    = getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
//        //Set<AWTKeyStroke> newForwardKeys = new HashSet<AWTKeyStroke>(forwardKeys);
//        //newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
//        //newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, java.awt.event.InputEvent.SHIFT_MASK ));
//        //setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newForwardKeys);
//
//        // SET PREFERRED DIMENSIONS
//            setPreferredSize(new Dimension(200,20));
//    }

//    /**
//     * Method for handling binding of component to a SSRowSet column.
//     */
//    protected void bind() {
//
//        // CHECK FOR NULL COLUMN/ROWSET
//            if (this.columnName==null || this.columnName.trim().equals("") || this.sSRowSet==null) {
//                return;
//            }
//
//        // REMOVE LISTENERS TO PREVENT DUPLICATION
//            removeListeners();
//            try {
//
//	        // BIND THE TEXT FIELD TO THE SPECIFIED COLUMN
//	            this.textField.setDocument(new SSTextDocument(this.sSRowSet, this.columnName));
//
//	        // SET THE COMBO BOX ITEM DISPLAYED
//	            updateDisplay();
//
//            }finally {
//            	// ADD BACK LISTENERS
//            	addListeners();
//            }
//    }

//    /**
//     * Updates the value displayed in the component based on the SSRowSet column
//     * binding.
//     */
//	protected void updateDisplay() {
//
//    	// THIS WILL MAKE SURE COMBO BOX ACTION LISTENER DOESN'T DO ANY THING EVEN IF IT GETS CALLED
//    	this.inUpdateDisplay = true;
//    	try {
//	        // GET THE VALUE FROM TEXT FIELD
//	        String text = this.textField.getText().trim();
//
//	        if (!text.equals("") && this.itemMap != null && this.itemMap.get(text) != null ) {
//	            //long valueInText = Long.parseLong(text);
//	            // GET THE INDEX WHERE THIS VALUE IS IN THE VECTOR.
//	        	//long longIndex = this.itemMap.get(text);
//	        	Long index = this.itemMap.get(text);
//	            //int index = (int) longIndex;
//	            if (index != getSelectedIndex()) {
//	                setSelectedIndex(index.intValue());
//	                updateUI();
//	            }
//	        }
//	        else {
//	            setSelectedIndex(-1);
//	            updateUI();
//	        }
//    	}finally {
//    		this.inUpdateDisplay = false;
//    	}
//    }

	/**
	 * Sets the second display name. If more than one column have to displayed then
	 * use this. For the parts example given above. If you have a part description
	 * in part table. Then you can display both part name and part description.
	 *
	 * @param _secondDisplayColumnName column name whose values have to be displayed
	 *                                 in the combo in addition to the first column
	 *                                 name.
	 */
	public void setSecondDisplayColumnName(final String _secondDisplayColumnName) {
		final String oldValue = secondDisplayColumnName;
		secondDisplayColumnName = _secondDisplayColumnName;
		firePropertyChange("secondDisplayColumnName", oldValue, secondDisplayColumnName);
	}

	/**
	 * Sets the currently selected value. This is called when the user clicks on an
	 * item or when they type in the combo's textfield.
	 * <p>
	 * Currently not a bean property since there is no associated variable.
	 *
	 * @param _value value to set as currently selected.
	 */
	@Override
	public void setSelectedItem(final Object _value) {

// TODO Need to deal with null on focus lost event. SSDBComboListener.actionPerformed setting bound column to  null when focus lost.
// TODO Could add back logic to revert typed text to restore matches.

// INTERCEPTING GLAZEDLISTS CALLS TO setSelectedItem() SO THAT WE CAN PREVENT IT FROM TRYING TO SET VALUES NOT IN THE LIST

		settingSelectedItem = true;

		try {

// NOTE THAT CALLING setSelectedIndex(-1) IN THIS METHOD CAUSES  CYCLE HERE BECAUSE setSelectedIndex() CALLS setSelectedItem()
		logger.debug(getColumnForLog() + ": Selected Item=" + _value);

//		logger.debug(getColumnForLog() + ": Selected Index BEFORE hidePopup()=" + getSelectedIndex());
//
//		int possibleMatches = getItemCount();
//		logger.debug(getColumnForLog() + ": Possible matches BEFORE hidePopup() - " + possibleMatches);
//
//		//hidePopup();
//
//		possibleMatches = getItemCount();
//		logger.debug(getColumnForLog() + ": Possible matches AFTER hidePopup() - " + possibleMatches);
//		logger.debug(getColumnForLog() + ": Selected Index AFTER hidePopup()=" + getSelectedIndex());

		// Extract selected item
			selectedItem = (SSListItem) _value;

		// Call to super.setSelectedItem() triggers SSDBComboListener.actionPerformed, which calls getSelectedValue(), which calls getSelectedIndex(), which returns -1 while still in the editor
		// and returns 0 after focus is lost.
		//
		// Calling hidePopup() restores the list, but messes up the GlazedList filtering.
		//
		// 2020-10-03_BP: Updated getSelectedValue() to properly return the primary key rather than using getSelectedIndex() during a call to this method.
		// Only try to update item for a valid list item.
			if (selectedItem!=null) {
				super.setSelectedItem(_value);
				logger.debug(getColumnForLog() + ": Selected Index AFTER setSelectedItem()=" + getSelectedIndex());
			} else {
				logger.debug(getColumnForLog() + ": No matching list item found so not updating. Current editor text is '" + getEditor().getItem().toString() + "'.");
			}



		} finally {
			settingSelectedItem = false;
			selectedItem = null;
		}

		return;
//		// DECLARATIONS
//		String currentEditorText = "";
//		int possibleMatches;
//		SSListItem selectedItem;
//
//		// WE COULD BE HERE DUE TO:
//		// 1. MOUSE CLICK ON AN ITEM
//		// 2. KEYBASED NAVIGATION
//		// 3. USER TYPING SEQUENTIALLY:
//		// THIS MAY TRIGGER MATCHING ITEMS, OR MAY NOT MATCH ANY SUBSTRINGS SO WE DELETE
//		// THE LAST CHARACTER
//		// 4. USER DOING SOMETHING UNEXPECTED LIKE INSERTING CHARACTERS, DELETING ALL
//		// TEXT, ETC.
//		// THIS MAY TRIGGER MATCHING ITEMS, OR MAY NOT MATCH ANY SUBSTRINGS SO WE REVERT
//		// TO THE LAST STRING AVAILABLE
//		// IF NOT MATCH, COULD ALSO REVERT TO EMPTY STRING
//
//		// GET LATEST TEXT TYPED BY USER
//
//		if (getEditor().getItem() != null) {
//			currentEditorText = getEditor().getItem().toString();
//		}
//
//		selectedItem = (SSListItem) _value;
//
//		// FOUR OUTCOMES:
//		// 1. _value is null, but selectedItem is not null, indicating a match (so null
//		// is a valid choice)
//		// 2. _value is null and selectedItem is null, indicating no match
//		// 3. neither _value nor selectedItem are null, indicating a match
//		// 4. _value is not null, but selectedItem is null, indicating no match (have to
//		// revert text)
//
//		if (selectedItem != null) {
//			// OUTCOME 1 & 3 ABOVE, MAKE CALL TO SUPER AND MOVE ALONG
//			// Display contents of selectedItem for debugging
//			logger.debug(getColumnForLog() + ": PK=" + selectedItem.getPrimaryKey() + ", Item=" + selectedItem.getListItem());
//			logger.debug(getColumnForLog() + ": Prior text was '" + priorEditorText + "'. Current text is '" + currentEditorText + "'.");
//
//			// We have to be VERY careful with calls to setSelectedItem() because it will
//			// set the value based on the index of any SUBSET list returned by GlazedList,
//			// not the full list
//			//
//			// Calling hidePopup() clears the subset list so that the subsequent
//			// call to setSelectedItem works as intended.
//
//			possibleMatches = getItemCount();
//			logger.debug(getColumnForLog() + ": Possible matches BEFORE hidePopup() - " + possibleMatches);
//
//			hidePopup();
//
//			possibleMatches = getItemCount();
//			logger.debug(getColumnForLog() + ": Possible matches AFTER hidePopup() - " + possibleMatches);
//
//			// Call to parent method.
//			// Don't call setSelectedIndex() as this causes a cycle
//			// setSelectedIndex()->setSelectedItem().
//			logger.debug(getColumnForLog() + ": Calling super.setSelectedItem(" + selectedItem + ")");
//			super.setSelectedItem(selectedItem);
//
//			// Update editor text
//			currentEditorText = selectedItem.getListItem();
//			getEditor().setItem(currentEditorText);
//			updateUI();
//
//			logger.debug(getColumnForLog() + ": Prior text was '" + priorEditorText + "'. Current text is '" + currentEditorText + "'.");
//
//			// update priorEditorText
//			priorEditorText = currentEditorText;
//
//		} else if (_value == null) {
//			// OUTCOME 2 ABOVE
//			// setSelectedItem() was called with null, but there is no match (so null is not a valid selection in the list)
//			// There may be partial matches from GlazedList.
//			logger.debug(getColumnForLog() + ": Method called with null. Prior text was '" + priorEditorText + "'. Current text is '" + currentEditorText + "'.");
//
//			// Determine if there are partial matches on the popup list due to user typing.
//			possibleMatches = getItemCount();
//			logger.debug(getColumnForLog() + ": Possible matches - " + possibleMatches);
//
//			if (possibleMatches > 0) {
//				// update the latestTypedText, but don't make a call to super.setSelectedItem(). No change to bound value.
//				priorEditorText = currentEditorText;
//			} else {
//// 2020-08-03: if user types "x" and it is not a choice we land here
//// on call to updateUI(), focus is lost and list items revert to 6 for "ss_db_combo_box" column in swingset_tests.sql
//// if "x" is typed a 2nd time, the popup does not become visible again and there are zero items in the list before and after the call
//// to setItem() and/or to updateUI()
//
//
//// This could also be the result of the first call to execute() where nothing has been typed and the popup is not visible.
//// This will throw a 'java.awt.IllegalComponentStateException' exception when showPopup() is called.
//				//if (!this.isVisible()) {
//				if (currentEditorText.isEmpty()) {
//					logger.debug(getColumnForLog() + ": Method called with null, but nothing has been typed. This occurs during screen initialization.");
//					super.setSelectedItem(selectedItem);
//					// 2020-10-03_BP: Probably need to update priorEditorText here
//					priorEditorText = currentEditorText;
//				} else {
//					logger.debug(getColumnForLog() + ": Reverting to prior typed text.");
//					getEditor().setItem(priorEditorText);
//					// IMPORTANT: The particular order here of showPopup() and then updateUI() seems to restore the
//					// underlying GlazedList to all of the items. Reversing this order breaks things. Calling hidePopup() does not work.
//					showPopup();
//					updateUI(); // This refreshes the characters displayed. Display does not update without call to updateUI();
//								// updateUI() triggers focus lost
//					possibleMatches = getItemCount();
//
//					logger.debug(getColumnForLog() + ": Possible matches AFTER reverting text - " + possibleMatches);
//				}
//			}
//
//		} else {
//			// OUTCOME 4 ABOVE
//			// generally not expecting this outcome
//			// revert to prior string and don't select anything
//			logger.warn(getColumnForLog() + ": Method called with " + _value + ", but there is no match. Prior text was '" + priorEditorText + "'. Current text is '" + currentEditorText + "'.");
//
//			// TODO Throw an exception here? May be the result of a coding error.
//			getEditor().setItem(priorEditorText);
//			currentEditorText = priorEditorText;
//			updateUI(); // This refreshes the characters displayed.
//		}

	}

	/**
	 * Sets the currently selected value
	 * <p>
	 * Currently not a bean property since there is no associated variable.
	 *
	 * @param _value value to set as currently selected.
	 */
	public void setSelectedStringValue(final String _value) {

		// ONLY NEED TO PROCEED IF THERE IS A CHANGE
		// TODO consider firing a property change
		if (_value != getSelectedItem()) {

			// IF OPTIONS ARE NON-NULL THEN LOCATE THE SEQUENTIAL INDEX AT WHICH THE
			// SPECIFIED TEXT IS STORED
			if (options != null) {
				final int index = options.indexOf(_value);

				if (index == -1) {
					logger.warn(getColumnForLog() + ": Could not find a corresponding item in combobox for display text of " + _value + ". Setting index to -1 (blank).");
				}

				setSelectedIndex(index);
				//updateUI();
			}

		}

	}

//    /**
//     * @author mvo
//     * Listener for the combobox's popup menu which resets the combobox's list to the original data if it is invisible.
//     */
//    protected class MyPopupMenuListener implements PopupMenuListener{
//    	ActionListener[] saveActionListeners = new ActionListener[getActionListeners().length];
//    	boolean saveSwitch = true;
//
//		public void addAllActionListeners(){
//    		for (ActionListener al: this.saveActionListeners) addActionListener(al);
//			fireActionEvent();
//		}
//
//		public void removeAllActionListeners(){
//			if (getActionListeners().length != 0){
//				for (ActionListener al : getActionListeners()){
//					removeActionListener(al);
//				}
//			}
//		}
//
//		@Override
//		public void popupMenuCanceled(PopupMenuEvent e) {
//    		if (isEditable()){
//    			hidePopup();
//    		}
//    	}
//
//		@Override
//		public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
//
//			//if the popup was open before filtering, return as to not add action listeners
//			if(SSDBComboBox.this.myKeyListener.openPopupFilter){
//				SSDBComboBox.this.myKeyListener.openPopupFilter = false;
//				return;
//			}
//			//when menu closes, change out textfield to re-insert original items before filtering.
//			if (SSDBComboBox.this.selectorCBM != null) SSDBComboBox.this.selectorCBM.setFilterEdit(new JTextField());
//			//set editable to false if value is clicked while filtering and set text field to selected item.
//			if (isEditable()){
//				setEditable(false);
//				SSDBComboBox.this.textField.setText(""+getSelectedFilteredValue());
//			}
//			addAllActionListeners();
//			requestFocus();
//		}
//
//		@Override
//		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//			if (this.saveSwitch){
//				this.saveActionListeners = getActionListeners();
//    			this.saveSwitch = false;
//    			// moving this line out of if block
//    			//removeAllActionListeners();
//			}
//			removeAllActionListeners();
//		}
//    }

//    /**
//     * Listener(s) for the bound text field used to propigate values back to the
//     * component's value.
//     */
//    protected class MyTextFieldDocumentListener implements DocumentListener {
//    	@Override
//		public void changedUpdate(DocumentEvent de) {
//    		// DON'T MOVE THE REMOVE ADD LISTENER CALLS TO UPDATEDISPLAY
//    		// AS UPDATE DISPLAY IS CALLED IN OTHER PLACES WHICH REMOVE AND ADD LISTENERS
//    		// THIS WOULD MAKE THE ADDLISTENER CALL TWICE SO THERE WILL BE TWO LISTENERS
//    		// AND A CALL TO REMOVE LISTENER IS NOT GOOD ENOUGH
//    		removeListeners();
//       		updateDisplay();
//       		addListeners();
//        }
//
//        @Override
//		public void insertUpdate(DocumentEvent de) {
//        	removeListeners();
//       		updateDisplay();
//       		addListeners();
//        }
//
//        @Override
//		public void removeUpdate(DocumentEvent de) {
//        	removeListeners();
//       		updateDisplay();
//       		addListeners();
//        }
//
//    } // end protected class MyTextFieldDocumentListener implements DocumentListener {

//    /**
//     * @author mvo
//     * Listener for JTextField "filterText" which allows for navigation of filtered items in combo box.
//     */
//    protected class FilterKeyListener extends KeyAdapter {
//    	String savePrimary;
//
//    	@Override
//		public void keyPressed(KeyEvent ke){
//
//    		if(ke.getKeyCode() == KeyEvent.VK_ESCAPE){
//    			setEditable(false);
//    			hidePopup();
//				setSelectedIndex(SSDBComboBox.this.myKeyListener.saveIndex);
//				SSDBComboBox.this.textField.setText(""+getSelectedFilteredValue());
//    			return;
//    		}
//    	}
//    	@Override
//		public void keyReleased(KeyEvent ke){
//    		//tab will traverse to next component
//    		if (ke.getKeyCode() == KeyEvent.VK_TAB){
//    			setEditable(false);
//				hidePopup();
//				transferFocus();
//    		}
//    		//turn off filter if arrows are pressed and keep focus on combo box
//    		else if (ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_UP){
//    			setEditable(false);
//    			showPopup();
//    			requestFocus();
//			}
//    		else if (0 == getItemCount()){
//    			repaint();
//    			showPopup();
//    		}
//    		else {
//	    		this.savePrimary = getSelectedFilteredValue();
//
//	    		//if the combo box has less items than the popup's row length, refresh the popup box.
//	    		if(getItemCount() < 9 || ke.getKeyCode() == KeyEvent.VK_BACK_SPACE)
//	    		{	SSDBComboBox.this.myKeyListener.openPopupFilter = true;
//	    			hidePopup();
//	    		}
//	    		showPopup();
//    		}
//    	}
//    }

//    /**
//     * Gets the selected value of the selected item in the filtered list.
//     * @return a String corresponding to the currently selected value in the SSDBCombobBox
//     */
//    public String getSelectedFilteredValue() {
//    	if(getSelectedIndex() < 0) setSelectedIndex(0);
//    	//gets the primary value of the selected index of the filtered list.
//    	Object selectedValue = this.selectorCBM.getSelectedBoundData(getSelectedIndex());
//    	if(selectedValue == null)
//    		return null;
//    	return selectedValue.toString();
//    }

//    /**
//     * Listener for focus in filter text field.
//     */
//    protected class FilterFocusListener extends FocusAdapter{
//		@Override
//		public void focusGained(FocusEvent fe){
//			showPopup();
//		}
//	}

//    /**
//     * Listener for keystroke-based, string matching, combo box navigation.
//     */
//    protected class MyKeyListener extends KeyAdapter {
//    	private JTextField filterText;
//    	private FilterKeyListener filterKeyListener = new FilterKeyListener();
//    	boolean openPopupFilter = false;
//    	int saveIndex;
//    	@Override
//		public void keyPressed(KeyEvent ke){
//    		this.saveIndex = getSelectedIndex();
//    		if (SSDBComboBox.this.myPopupMenuListener.saveSwitch) {
//    			SSDBComboBox.this.myPopupMenuListener.saveActionListeners = getActionListeners();
//    			SSDBComboBox.this.myPopupMenuListener.saveSwitch = false;
//    		}
//    		SSDBComboBox.this.myPopupMenuListener.removeAllActionListeners();
//    	}
//    	@Override
//		public void keyReleased(KeyEvent ke){
//    		//reset the list and set the text field to the selected item's primary key
//    		if (ke.getKeyCode() == KeyEvent.VK_ENTER){
//
//    			//if enter is pressed inside of the filter textfield and no item is selected
//    			//pick the last item selected item and set it as the current selected item
//    			if (-1 == getSelectedIndex()){
//    				setSelectedItem(null);
//    				SSDBComboBox.this.textField.setText(this.filterKeyListener.savePrimary);
//    			}
//    			return;
//    		}
//
//    		if (ke.getKeyCode() == KeyEvent.VK_DOWN ||  ke.getKeyCode() == KeyEvent.VK_UP || !SSDBComboBox.this.filterSwitch) return;
//
//    		//take the first key pressed, set combo box to editable, turn on filter, and set the text field to that saved key
//    		if (ke.getKeyCode() >= KeyEvent.VK_A & ke.getKeyCode() <= KeyEvent.VK_BACK_SLASH 					||
//    			ke.getKeyCode() >= KeyEvent.VK_COMMA & ke.getKeyCode() <= KeyEvent.VK_9 	  					||
//    			ke.getKeyCode() >= KeyEvent.VK_OPEN_BRACKET & ke.getKeyCode() <= KeyEvent.VK_CLOSE_BRACKET		||
//    			ke.getKeyCode() == KeyEvent.VK_PLUS																||
//    			ke.getKeyCode() == KeyEvent.VK_QUOTE) {
//    			// if the popup is open, close it and do not add listeners
//        		if (isPopupVisible()){
//        			this.openPopupFilter = true;
//        			hidePopup();
//        		}
//    			setEditable(true);
//    			this.filterText = (JTextField) getEditor().getEditorComponent();
//    			SSDBComboBox.this.selectorCBM.setFilterEdit(this.filterText);
//    			this.filterText.setText(""+ke.getKeyChar());
//    			this.filterText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
//
//    			// SINCE WE HAVE TO ADD THE LISTENER IN COMBO KEY LISTENER
//    			// MAKE SURE WE ARE NOT ADDING IT MULTIPLE TIMES.
//    			// EASY WAY TO DO IT IS TO REMOVE IT AND THEN ADD IT.
//       			this.filterText.removeKeyListener(this.filterKeyListener);
//       			this.filterText.removeFocusListener(SSDBComboBox.this.filterFocusListener);
//       			this.filterText.addKeyListener(this.filterKeyListener);
//       			this.filterText.addFocusListener(SSDBComboBox.this.filterFocusListener);
//
//        	}
//    	}
//    }

	/**
	 * Sets the value stored in the component.
	 * <p>
	 * If called from updateSSComponent() from a RowSet change then the Component
	 * listener should already be turned off. Otherwise we want it on so the
	 * ultimate call to setSelectedIndex() will trigger an update the to RowSet.
	 * <p>
	 * The mappings ArrayList will be null until execute is called so
	 * <p>
	 * Currently not a bean property since there is no associated variable.
	 *
	 * @param _value database record primary key value to assign to combobox
	 */
	public void setSelectedValue(final long _value) {

		// ONLY NEED TO PROCEED IF THERE IS A CHANGE
		// TODO consider firing a property change
		// TODO what happens if user tries to pass null or if getSelectedValue() is null?

		// 2020-08-03: Removing conditional as this could be called when consecutive records
		// have the same value and we want to make sure to update the editor Strings

			// IF MAPPINGS ARE SPECIFIED THEN LOCATE THE SEQUENTIAL INDEX AT WHICH THE
			// SPECIFIED CODE IS STORED
			if (mappings != null) {
				final int index = mappings.indexOf(_value);

				if (index == -1) {
					logger.warn(getColumnForLog() + ": Could not find a corresponding item in combobox for value of " + _value + ". Setting index to -1 (blank).");
				}

				logger.trace(getColumnForLog() + ": eventList - " + eventList.toString());
				logger.trace(getColumnForLog() + ": options - " + options.toString());
				logger.trace(getColumnForLog() + ": mappings - " + mappings.toString());

				setSelectedIndex(index);
			} else {
				logger.warn(getColumnForLog() + ": No mappings available for current component. No value set by setSelectedValue().");
			}

	}



//    /**
//     * Listener(s) for the component's value used to propagate changes back to
//     * bound text field.
//     */
//    protected class MyComboListener implements ActionListener {
//    	@Override
//		public void actionPerformed(ActionEvent ae) {
//    		// IF WE ARE UPDATING THE DISPLAY DON'T DO ANY THING.
//    		if(SSDBComboBox.this.inUpdateDisplay) {
//    			return;
//    		}
//
//        	//dont fire an action if the size of the filtered model is not the same as the initial item size
//        	if (SSDBComboBox.this.selectorCBM != null){
//        		if (SSDBComboBox.this.selectorCBM.getSize() != SSDBComboBox.this.selectorCBM.data.size()) return;
//        	}
//
//        	SSDBComboBox.this.textField.getDocument().removeDocumentListener(SSDBComboBox.this.textFieldDocumentListener);
//
//        	try {
//	            // GET THE INDEX CORRESPONDING TO THE SELECTED TEXT IN COMBO
//	            int index = getSelectedIndex();
//	            // IF THE USER WANTS TO REMOVE COMPLETELY THE VALUE IN THE FIELD HE CHOOSES
//	            // THE EMPTY STRING IN COMBO THEN THE TEXT FIELD IS SET TO EMPTY STRING
//	            if (index != -1) {
//	                try {
//	                    String textFieldText = SSDBComboBox.this.textField.getText();
//	                    String textPK= SSDBComboBox.this.selectorCBM.getSelectedBoundData(index).toString();
//	                    if (!textFieldText.equals(textPK)) {
//	                        SSDBComboBox.this.textField.setText(textPK);
//	                    }
//	                    // IF THE LONG VALUE CORRESPONDING TO THE SELECTED TEXT OF COMBO NOT EQUAL
//	                    // TO THAT IN THE TEXT FIELD THEN CHANGE THE TEXT IN THE TEXT FIELD TO THAT VALUE
//	                    // IF ITS THE SAME LEAVE IT AS IS
//	                } catch(NullPointerException npe) {
//	                	npe.printStackTrace();
//	                } catch(NumberFormatException nfe) {
//	                	nfe.printStackTrace();
//	                }
//	            }
//	            else {
//	                SSDBComboBox.this.textField.setText("");
//	            }
//
//        	}finally {
//        		SSDBComboBox.this.textField.getDocument().addDocumentListener(SSDBComboBox.this.textFieldDocumentListener);
//        	}
//
//            // WHEN SET SELECTED INDEX IS CALLED SET SELECTED ITEM WILL BE CALLED ON THE MODEL AND THIS FUNCTION
//            // IS SUPPOSED TO FIRE A EVENT TO CHANGE THE TEXT BUT IT WILL CAUSE ISSUES IN OUR IMPLEMENTATION
//            // BUT WE WILL GET ACTION EVENT SO REPAIT TO REFLECT THE CHANGE IN COMBO SELECTION
//            repaint();
//        }
//
//    } // protected class MyComboListener implements ActionListener {

	/**
	 * Set the separator to be used when multiple columns are displayed
	 *
	 * @param _separator separator to be used.
	 */
	public void setSeparator(final String _separator) {
		final String oldValue = separator;
		separator = _separator;
		firePropertyChange("separator", oldValue, separator);
	}

	/**
	 * Set the separator to be used when multiple columns are displayed
	 * <p>
	 * Deprecated for misspelling.
	 *
	 * @param _separator separator to be used.
	 */
	@Deprecated
	public void setSeperator(final String _separator) {
		setSeparator(_separator);
	}

	/**
	 * Sets the SSCommon data member for the current Swingset Component.
	 *
	 * @param _ssCommon shared/common SwingSet component data and methods
	 */
	@Override
	public void setSSCommon(final SSCommon _ssCommon) {
		ssCommon = _ssCommon;

	}

	/**
	 * Updates an item available in the combobox and associated lists.
	 * <p>
	 * If more than one item is present in the combo for that value, only the first
	 * one is changed.
	 * <p>
	 * NOTE: To retain changes made to current SSRowSet call updateRow before
	 * calling the updateItem on SSDBComboBox. (Only if you are using the
	 * SSDBComboBox and SSDataNavigator for navigation in the screen. If you are not
	 * using the SSDBComboBox for navigation then no need to call updateRow on the
	 * SSRowSet. Also if you are using only SSDBComboBox for navigation you need not
	 * call the updateRow.)
	 *
	 * @param _primaryKey         primary key value corresponding the the display
	 *                            text to be updated
	 * @param _updatedDisplayText text that should be updated in the combobox
	 *
	 * @return returns true if update is successful otherwise returns false.
	 */
	public boolean updateItem(final long _primaryKey, final String _updatedDisplayText) {

		boolean result = false;

		if (eventList != null) {

			// LOCK EVENT LIST
			eventList.getReadWriteLock().writeLock().lock();

			try {

				// GET INDEX FOR mappings and options
				final int index = mappings.indexOf(_primaryKey);

				// PROCEED IF INDEX WAS FOUND
				if (index != -1) {
					options.set(index, _updatedDisplayText);
					// mappings.remove(index);
// TODO Confirm that eventList is not reordered by GlazedLists code.
					eventList.get(index).setListItem(_updatedDisplayText);
					result = true;

				}

// TODO may need to call repaint()

			} catch (final Exception e) {
				logger.error(getColumnForLog() + ": Exception.", e);
			} finally {
				eventList.getReadWriteLock().writeLock().unlock();
			}

		}

		return result;
	}

	/**
	 * Updates the value stored and displayed in the SwingSet component based on
	 * getBoundColumnText()
	 * <p>
	 * Call to this method should be coming from SSCommon and should already have
	 * the Component listener removed
	 */
	@Override
	public void updateSSComponent() {
		// TODO Modify this class similar to updateSSComponent() in SSFormattedTextField and only allow JDBC types that convert to Long or Integer
		try {
			// 2020-10-05_BP: If initialization is taking place then there won't be any mappings so don't try to update anything yet.
			if (eventList==null) {
				return;
			}

			// If the user was on this component and the GlazedList had a subset of items, then
			// navigating resulting in a call to updateSSComponent()->setSelectedValue() may try to do a lookup based on
			// the GlazedList subset and generate:
			// Exception in thread "AWT-EventQueue-0" java.lang.IllegalArgumentException: setSelectedIndex: X out of bounds
			//int possibleMatches = getItemCount();
			//logger.debug(getColumnForLog() + ": Possible matches BEFORE setPopupVisible(false);: "+ possibleMatches);

			//this.setPopupVisible(false);
			//updateUI();

			//possibleMatches = getItemCount();
			//logger.debug(getColumnForLog() + ": Possible matches AFTER setPopupVisible(false);: "+ possibleMatches);

			// THIS SHOULD BE CALLED AS A RESULT OF SOME ACTION ON THE ROWSET SO RESET THE EDITOR STRINGS BEFORE DOING ANYTHING ELSE
			priorEditorText = "";
			getEditor().setItem(priorEditorText);


			// Combobox primary key column data queried from the database will generally be of data type long.
			// The bound column text should generally be a long integer as well, but trimming to be safe.
			// TODO Consider starting with a Long and passing directly to setSelectedValue(primaryKey). Modify setSelectedValue to accept a Long vs long.
			final String text = getBoundColumnText();

			logger.debug(getColumnForLog() + ": getBoundColumnText() - " + text);

			// GET THE BOUND VALUE STORED IN THE ROWSET
			//if (text != null && !(text.equals(""))) {
			if ((text != null) && !text.isEmpty()) {

				final long primaryKey = Long.parseLong(text);

				logger.debug(getColumnForLog() + ": Calling setSelectedValue(" + primaryKey + ").");

				setSelectedValue(primaryKey);

			} else {
				logger.debug(getColumnForLog() + ": Calling setSelectedIndex(-1).");

				setSelectedIndex(-1);
				//updateUI();
			}

			// TODO Consider commenting this out for performance.
			String editorString = null;
			if (getEditor().getItem() != null) {
				editorString = getEditor().getItem().toString();
			}
			logger.debug(getColumnForLog() + ": Combo editor string: " + editorString);

		} catch (final NumberFormatException nfe) {
			logger.error(getColumnForLog() + ": Number Format Exception.", nfe);
		}
	}

	/**
	 * Updates the string thats being displayed.
	 * <p>
	 * If more than one item is present in the combo for that value the first one is
	 * changed.
	 *
	 * @param _existingDisplayText existing display text to be updated
	 * @param _updatedDisplayText  text that should be updated in the combobox
	 *
	 * @return returns true if successful otherwise returns false.
	 */
	public boolean updateStringItem(final String _existingDisplayText, final String _updatedDisplayText) {

		boolean result = false;

		if (options != null) {
			final int index = options.indexOf(_existingDisplayText);
			result = updateItem(mappings.get(index), _updatedDisplayText);
		}

		return result;
	}

} // end public class SSDBComboBox
