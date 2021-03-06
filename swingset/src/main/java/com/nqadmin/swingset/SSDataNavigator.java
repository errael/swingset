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
 *   Ernie R. Rael
 ******************************************************************************/
package com.nqadmin.swingset;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nqadmin.swingset.utils.SSEnums.Navigation;

// SSDataNavigator.java
//
// SwingSet - Open Toolkit For Making Swing Controls Database-Aware

/**
 * Component that can be used for data navigation. It provides buttons for
 * navigation, insertion, and deletion of records in a RowSet. The
 * modification of a RowSet can be prevented using the setModificaton()
 * method. Any changes made to the columns of a record will be updated whenever
 * there is a navigation.
 * <p>
 * For example if you are displaying three columns using the JTextField and the
 * user changes the text in the text fields then the columns will be updated to
 * the new values when the user navigates the RowSet. If the user wants to
 * revert the changes he made he can press the Undo button, however this must be
 * done before any navigation. Once navigation takes place changes can't be
 * reverted using Undo button (has to be done manually by the user).
 */
public class SSDataNavigator extends JPanel {

	private static class RowSetState {
		private boolean inserting;
	}

	// don't have to worry about concurrency, always EDT
	private static Map<RowSet,RowSetState> rowSetState = new WeakHashMap<>();

	private static RowSetState getRowSetState(RowSet rs) {
		return rowSetState.computeIfAbsent(rs, k -> new RowSetState());
	}

	private static void setInserting(RowSet rs, boolean flag) {
		if (rs != null) {
			getRowSetState(rs).inserting = flag;
		}
	}

	/**
	 * Find out if the specified RowSet is on the insert row.
	 * @param rs get state for this RowSet
	 * @return true if on the insert row
	 */
	public static boolean isInserting(RowSet rs) {
		return rs == null ? false : getRowSetState(rs).inserting;
	}

	/**
	 * Rowset Listener on the RowSet used by data navigator.
	 */
	protected class SSDBNavRowSetListener implements RowSetListener, Serializable {
		
		/**
		 * unique serial ID
		 */
		private static final long serialVersionUID = -6731728079603068264L;
		
		/**
		 * variables needed to consolidate multiple calls
		 */
		private int lastChange = 0;
		private int lastNotifiedChange = 0;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void cursorMoved(final RowSetEvent rse) {
			logger.trace("Rowset cursor moved.");
			performUpdates();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void rowChanged(final RowSetEvent rse) {
			logger.trace("Row changed.");
			performUpdates();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void rowSetChanged(final RowSetEvent rse) {
			logger.trace("Rowset changed.");
			// Update the record counts and navigator display following a navigation.
			try {
				logger.debug("Updating row count with last(), getRow(), and first().");
				rowSet.last();
				rowCount = rowSet.getRow();
				rowSet.first();
			} catch (final SQLException se) {
				logger.error("SQL Exception.", se);
			}
			performUpdates();

		}
		
		private void performUpdates() {
			lastChange++;
			logger.trace("performUpdates(): lastChange=" + lastChange
					+ ", lastNotifiedChange=" + lastNotifiedChange);
			
			// Delay execution of logic until all listener methods are called for current event
			// Based on: https://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
			SwingUtilities.invokeLater(() -> {
				if (lastNotifiedChange != lastChange) {
					lastNotifiedChange = lastChange;

					try {
						logger.debug("Calling updateNavigator().");
						updateNavigator();
					} catch (final SQLException se) {
						logger.error("SQL Exception.", se);
					}				
				}
			});
		}

	}

	/**
	 * Log4j Logger for component
	 */
	private static Logger logger = LogManager.getLogger();

	/**
	 * unique serial id
	 */
	private static final long serialVersionUID = 3129669039062103212L;

	/**
	 * Button to add a record to the RowSet.
	 */
	protected JButton addButton = new JButton();

	/**
	 * Navigator button dimensions.
	 */
	protected Dimension buttonSize = new Dimension(40, 20);

	/**
	 * Indicator to cause the navigator to skip the execute() function call on the
	 * specified RowSet. Must be false for MySQL (see FAQ).
	 */
	protected boolean callExecute = true;

	/**
	 * Button to commit screen changes to the RowSet.
	 */
	protected JButton commitButton = new JButton();

	/**
	 * Indicator to force confirmation of RowSet deletions.
	 */
	protected boolean confirmDeletes = true;

	/**
	 * Row number for current record in RowSet.
	 */
	protected int currentRow = 0;

	/**
	 * Container (frame or internal frame) which contains the navigator.
	 */
	protected SSDBNav dBNav = new SSDBNav(){
		private static final long serialVersionUID = 4690448389199879038L; // unique serial ID
	};

	/**
	 * Button to delete the current record in the RowSet.
	 */
	protected JButton deleteButton = new JButton();

	/**
	 * Indicator to allow/disallow deletions from the RowSet.
	 */
	protected boolean deletion = true;

	/**
	 * Button to navigate to the first record in the RowSet.
	 */
	protected JButton firstButton = new JButton();

	/**
	 * Indicator to allow/disallow insertions to the RowSet.
	 */
	protected boolean insertion = true;

	/**
	 * Button to navigate to the last record in the RowSet.
	 */
	protected JButton lastButton = new JButton();

	/**
	 * Label to display the total number of records in the RowSet.
	 */
	protected JLabel lblRowCount = new JLabel();

	/**
	 * Indicator to allow/disallow changes to the RowSet.
	 */
	protected boolean modification = true;

	/**
	 * SSDBComboBox used for navigation if applicable.
	 * <p>
	 * Allows Navigator to disable it when a row is inserted and enable it when that row is saved.
	 * <p>
	 * TODO Consider writing a PropertyChangeListener for onInsertRow instead.
	 */
	protected SSDBComboBox navCombo= null;

	/**
	 * Button to navigate to the next record in the RowSet.
	 */
	protected JButton nextButton = new JButton();

	/**
	 * Button to navigate to the previous record in the RowSet.
	 */
	protected JButton previousButton = new JButton();

	/**
	 * Button to refresh the screen based on any changes to the RowSet.
	 */
	protected JButton refreshButton = new JButton();

	/**
	 * Number of rows in RowSet. Set to zero if next() method returns false.
	 */
	protected int rowCount = 0;

	/**
	 * RowSet from which component will get/set values.
	 */
	protected RowSet rowSet = null;

	/**
	 * Listener on the RowSet used by data navigator.
	 */
	private final SSDBNavRowSetListener rowsetListener = new SSDBNavRowSetListener();
	
	/**
	 * Indicates if rowset listener is added (or removed)
	 */
	private boolean rowsetListenerAdded = false;

	/**
	 * Text field for viewing/changing the current record number.
	 */
	protected JTextField txtCurrentRow = new JTextField();

	/**
	 * Current record text field dimensions.
	 */
	protected Dimension txtFieldSize = new Dimension(65, 20);

	/**
	 * Button to revert screen changes based on the RowSet.
	 */
	protected JButton undoButton = new JButton();

	/**
	 * Creates a object of SSDataNavigator. Note: you have to set the RowSet
	 * before you can start using it.
	 */
	public SSDataNavigator() {
		this(null, null);
	}

	/**
	 * Constructs a SSDataNavigator for the given RowSet
	 *
	 * @param _rowSet The RowSet to which the SSDataNavigator has to be bound
	 */
	public SSDataNavigator(final RowSet _rowSet) {
		this(_rowSet, null);
	}

	/**
	 * Constructs the SSDataNavigator with the given RowSet and sets the size of
	 * the buttons on the navigator to the given size
	 *
	 * @param _rowSet   the RowSet to which the navigator is bound to
	 * @param _buttonSize the size to which the button on navigator have to be set
	 */
	public SSDataNavigator(final RowSet _rowSet, final Dimension _buttonSize) {
		if (_rowSet!=null) {
			setRowSet(_rowSet);
		}
		if (_buttonSize!=null) {
			buttonSize = _buttonSize;
		}
		addToolTips();
		createPanel();
		addNavListeners();
	}

	/**
	 * Adds the listeners for the navigator components.
	 */
	private void addNavListeners() {

		// WHEN THIS BUTTON IS PRESSED THE RECORD ON WHICH USER WAS WORKING IS SAVED
		// AND MOVES THE SSROWSET TO THE FIRST ROW
		// SINCE ROW SET IS IN FIRST ROW DISABLE PREVIOUS BUTTON AND ENABLE NEXT BUTTON
		firstButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				logger.debug("FIRST button clicked.");
				removeRowsetListener();
				try {
					if (modification) {
						if (!dBNav.allowUpdate()) {
							// UPDATE NOT ALLOWED SO DO NOTHING.
							// WE SHOULD NOT MOVE TO THE ROW AS THE USER HAS MADE CHANGES TO
							// TO THE ROW THAT SHOULD BE UNDONE.
							return;
						}
						rowSet.updateRow();
						dBNav.performPostUpdateOps();
					}
					rowSet.first();

					updateNavigator();
					
					dBNav.performNavigationOps(Navigation.First);
					
				} catch (final SQLException se) {
					logger.error("SQL Exception.", se);
					JOptionPane.showMessageDialog(SSDataNavigator.this,
							"Exception occured while updating row or moving the cursor.\n" + se.getMessage());
				}
				addRowsetListener();
			}
		});

		// WHEN BUTTON 2 IS PRESSED THE CURRENT RECORD IS SAVED AND SSROWSET IS MOVED TO PREVIOUS RECORD
		// CALLING PREVIOUS ON EMPTY SSROWSET IS ILLEGAL SO A CHECK IS PERFORMED
		previousButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				logger.debug("PREVIOUS button clicked.");
				removeRowsetListener();
				try {
					// if( rowSet.rowUpdated() )
					if (modification) {
						if (!dBNav.allowUpdate()) {
							// UPDATE NOT ALLOWED SO DO NOTHING.
							// WE SHOULD NOT MOVE TO THE ROW AS THE USER HAS MADE CHANGES TO
							// TO THE ROW THAT SHOULD BE UNDONE.
							return;
						}
						rowSet.updateRow();
						dBNav.performPostUpdateOps();
					}
					if ((rowSet.getRow() != 0) && !rowSet.previous()) {
						rowSet.first();
					}

					updateNavigator();

					dBNav.performNavigationOps(Navigation.Previous);
				} catch (final SQLException se) {
					logger.error("SQL Exception.", se);
					JOptionPane.showMessageDialog(SSDataNavigator.this,
							"Exception occured while updating row or moving the cursor.\n" + se.getMessage());
				}
				addRowsetListener();
			}
		});

		// WHEN BUTTON 3 PRESSED THE CURRENT RECORD IS SAVED AND THE SSROWSET IS
		// MOVED TO NEXT RECORD. IF THIS IS THE LAST RECORD THEN BUTTON 3 IS DISABLED
		// ALSO IF THE PREVIOUS BUTTON IS NOT ENABLED THEN IT IS ENABLED
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				logger.debug("NEXT button clicked.");
				removeRowsetListener();
				try {
					// if( rowSet.rowUpdated() )
					if (modification) {
						if (!dBNav.allowUpdate()) {
							// UPDATE NOT ALLOWED SO DO NOTHING.
							// WE SHOULD NOT MOVE TO THE ROW AS THE USER HAS MADE CHANGES TO
							// TO THE ROW THAT SHOULD BE UNDONE.
							return;
						}
						rowSet.updateRow();
						dBNav.performPostUpdateOps();
					}

					rowSet.next();

					updateNavigator();

					dBNav.performNavigationOps(Navigation.Next);

				} catch (final SQLException se) {
					logger.error("SQL Exception.", se);
					JOptionPane.showMessageDialog(SSDataNavigator.this,
							"Exception occured while updating row or moving the cursor.\n" + se.getMessage());
				}
				addRowsetListener();
			}
		});

		// BUTTON 4 ( "LAST" BUTTON ) CAUSED THE SSROWSET TO MOVE TO LAST RECORD.
		// BEFORE MOVING CURRENT RECORD IS SAVED
		// AFTER MOVING TO LAST RECORD THE NEXT BUTTON IS DIAABLED AND PREVIOUS BUTTON
		// ENABLED
		lastButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				logger.debug("LAST button clicked.");
				removeRowsetListener();
				try {
					// if( rowSet.rowUpdated() )
					if (modification) {
						if (!dBNav.allowUpdate()) {
							// UPDATE NOT ALLOWED SO DO NOTHING.
							// WE SHOULD NOT MOVE TO THE ROW AS THE USER HAS MADE CHANGES TO
							// TO THE ROW THAT SHOULD BE UNDONE.
							return;
						}
						rowSet.updateRow();
						dBNav.performPostUpdateOps();
					}
					rowSet.last();

					updateNavigator();

					dBNav.performNavigationOps(Navigation.Last);

				} catch (final SQLException se) {
					logger.error("SQL Exception.", se);
					JOptionPane.showMessageDialog(SSDataNavigator.this,
							"Exception occured while updating row or moving the cursor.\n" + se.getMessage());
				}
				addRowsetListener();
			}
		});

		// THIS BUTTON COMMITS ANY CHANGES TO THE DATABASE
		// IF YOU ARE ON THE INSERT ROW FOR
		// A NEW RECORD, IT INSERTS THE ROW AND MOVES TO THE NEWLY INSERTED ROW.
		// WHEN INSERT BUTTON IS PRESSED NAVIGATION WILL BE DISABLED SO THOSE HAVE TO BE
		// RE-ENABLED HERE
		commitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				logger.debug("COMMIT button clicked.");
				removeRowsetListener();
				try {
					if (isInserting(rowSet)) {
						// IF ON INSERT ROW ADD THE ROW.
						// CHECK IF THE ROW CAN BE INSERTED.
						if (!dBNav.allowInsertion()) {
							// WE DO NOTHING. THE ROWSET STAYS IN INSERT ROW. EITHER USER
							// HAS TO FIX THE DATA AND SAVE THE ROW OR CANCEL THE INSERTION.
							return;
						}
						rowSet.insertRow();
						setInserting(rowSet, false);
						dBNav.performPostInsertOps();
						
						// 2019-10-14: next bit of code seems odd. not sure why we're calling moveToCurrentRow() and last().
						//  In H2, this is leading to an incorrect total # of rows (e.g., have 5, insert a row, and it shows 6 of 7 until refreshed).
						//  Modified to just call last();
						
//						// INCREMENT THE ROW COUNT
//						SSDataNavigator.this.rowCount++;
//
//						// MOVE TO CURRENT ROW MOVES SSROWSET TO RECORD AT WHICH ADD WAS PRESSED.
//						// BUT IT NICE TO BE ON THE ADDED ROW WHICH IS THE LAST ONE IN THE SSROWSET.
//						// ALSO MOVE TO CURRENT ROW MOVES THE SSROWSET POSITION BUT DOES NOT TRIGGER
//						// ANY EVENT FOR THE LISTENERS AS A RESULT VALUES ON THE SCREEN WILL NOT
//						// DISPLAY THE CURRENT RECORD VALUES.
//						SSDataNavigator.this.rowSet.moveToCurrentRow();

						rowSet.last();

						rowCount = rowSet.getRow();

						updateNavigator();

						refreshButton.setEnabled(true);

						if (insertion) {
							addButton.setEnabled(true);
						}
						if (deletion) {
							deleteButton.setEnabled(true);
						}
					
					} else {
						// ELSE UPDATE THE PRESENT ROW VALUES.
						if (!dBNav.allowUpdate()) {
							// UPDATE NOT ALLOWED SO DO NOTHING.
							// WE SHOULD NOT MOVE TO THE ROW AS THE USER HAS MADE CHANGES TO
							// TO THE ROW THAT SHOULD BE UNDONE.
							return;
						}
						rowSet.updateRow();
						
						// 2020-11-24: Generally redundant, but force a refresh the screen with the 
						// values from the rowset. This will be most noticeable if you have
						// two fields bound to the same column.
						// 
						// rowSet.refreshRow() did not accomplish the intended result, but
						// navigating to the same row using absolute and the current record
						// number did.
						rowSet.absolute(rowSet.getRow());
						
						dBNav.performPostUpdateOps();
					}

				} catch (final SQLException se) {
					logger.error("SQL Exception.", se);
					JOptionPane.showMessageDialog(SSDataNavigator.this,
							"Exception occured while saving row.\n" + se.getMessage());
				}
				addRowsetListener();
			}
		});

		// THIS BUTTON IS USED TO CANCEL THE CHANGES MADE TO THE RECORD.
		// IT CAN ALSO BE USED TO CANCEL INSERT ROW.
		// SO THE BUTTONS DISABLED AT THE INSERT BUTTON EVENT HAVE TO BE ENABLED
		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				logger.debug("UNDO button clicked.");
				removeRowsetListener();
				try {
					// CALL MOVE TO CURRENT ROW IF ON INSERT ROW.
					if (isInserting(rowSet)) {
				rowSet.moveToCurrentRow();
					}
					// THIS FUNCTION IS NOT NEED IF ON INSERT ROW
					// BUT MOVETOINSERTROW WILL NOT TRIGGER ANY EVENT SO FOR THE SCREEN
					// TO UPDATE WE NEED TO TRIGGER SOME THING.
					// SINCE USER IS MOVED TO CURRENT ROW PRIOR TO INSERT IT IS SAFE TO
					// CALL CANCELROWUPDATE TO GET A TRIGGER
					rowSet.cancelRowUpdates();
					setInserting(rowSet, false);
					dBNav.performCancelOps();
					rowSet.refreshRow();

					updateNavigator();

					refreshButton.setEnabled(true);
					if (insertion) {
						addButton.setEnabled(true);
					}
					if (deletion) {
						deleteButton.setEnabled(true);
					}

				} catch (final SQLException se) {
					logger.error("SQL Exception.", se);
					JOptionPane.showMessageDialog(SSDataNavigator.this,
							"Exception occured while undoing changes.\n" + se.getMessage());
				}
				addRowsetListener();
			}
		});

		// REFETCH REFETCHES THE ROWS FROMS THE DATABASE AND MOVES THE CURSOR TO THE
		// FIRST
		// RECORD IF THERE ARE NO RECORDS NAVIGATION BUTTONS ARE DIABLED
		// EVEN IS THERE ARE RECORDS PREVIOUS BUTTON IS DISABLED BECAUSE THE SSROWSET IS
		// ON
		// THE FIRST ROW
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				logger.debug("REFRESH button clicked.");
				removeRowsetListener();
				try {
					if (callExecute) {
						rowSet.execute();

						if (!rowSet.next()) {
							// THERE ARE NO RECORDS IN THE ROWSET
							rowCount = 0;
						} else {
							// WE HAVE ROWS GET THE ROW COUNT AND MOVE BACK TO FIRST ROW
							rowSet.last();
							rowCount = rowSet.getRow();
							rowSet.first();
						}

						updateNavigator();
					}

					dBNav.performRefreshOps();

				} catch (final SQLException se) {
					logger.error("SQL Exception.", se);
					JOptionPane.showMessageDialog(SSDataNavigator.this,
							"Exception occured refreshing the data.\n" + se.getMessage());
				}
				addRowsetListener();
			}
		});

		// INSERT ROW BUTTON MOVES THE SSROWSET TO THE INSERT ROW POSITION
		// AT THIS TIME NAVIGATION HAS TO BE DISABLED
		// ONLY COMMIT AND CANCEL ARE ENABLED
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				logger.debug("ADD button clicked.");
				removeRowsetListener();
				try {

					rowSet.moveToInsertRow();
					setInserting(rowSet, true);
					if (navCombo!=null) {
						navCombo.setEnabled(false);
					}

					dBNav.performPreInsertOps();

					firstButton.setEnabled(false);
					previousButton.setEnabled(false);
					nextButton.setEnabled(false);
					lastButton.setEnabled(false);
					commitButton.setEnabled(true);
					undoButton.setEnabled(true);
					refreshButton.setEnabled(false);
					addButton.setEnabled(false);
					deleteButton.setEnabled(false);

				} catch (final SQLException se) {
					logger.error("SQL Exception.", se);
					JOptionPane.showMessageDialog(SSDataNavigator.this,
							"Exception occured while moving to insert row.\n" + se.getMessage());
				}
				addRowsetListener();
			}
		});

		// DELETES THE CURRENT ROW AND MOVES TO NEXT ROW
		// IF THE DELETED ROW IS THE LAST ROW THEN MOVES TO LAST ROW IN SSROWSET
		// AFTER THE DELETION IS MADE (THATS THE PREVIOUS ROW TO THE DELETED ROW)
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				logger.debug("DELETE button clicked.");
				removeRowsetListener();
				try {
					if (confirmDeletes) {
						final int answer = JOptionPane.showConfirmDialog(SSDataNavigator.this,
								"Are you sure you want to delete this record?", "Delete Present Record",
								JOptionPane.YES_NO_OPTION);
						if (answer != JOptionPane.YES_OPTION) {
							return;
						}
					}
					
					if (!dBNav.allowDeletion()) {
						return;
					}
					
					// CAPTURE CURRENT ROW PRE-DELETION
					final int tmpPosition = currentRow;
					
					// SET ANTICIPATED ROW COUNT POST-DELETION
					final int tmpSize = rowCount-1;
					
					// PERFORM ANY PRE DELETION OPS
					dBNav.performPreDeletionOps();
					
					// DELETE ROW FROM ROWSET
					rowSet.deleteRow();
					
					// PERFORM ANY POST DELETION OPS (WHICH MAY INVOLVE REQUERYING WHICH IS NEEDED FOR H2)
					dBNav.performPostDeletionOps();
					
					// UPDATE TOTAL ROW COUNT
					rowCount=tmpSize;
					
					// TRY TO NAVIGATE TO THE RECORD AFTER THE DELETED RECORD, OTHERWISE GO TO
					// WHATEVER IS THE LAST RECORD
					if ((tmpPosition <= rowCount) && (tmpPosition > 0)) {
						rowSet.absolute(tmpPosition);
					} else {
						rowSet.last();
					}
					
					// UPDATE THE STATUS OF THE NAVIGATOR
					updateNavigator();
					
				} catch (final SQLException se) {
					logger.error("SQL Exception.", se);
					JOptionPane.showMessageDialog(SSDataNavigator.this,
							"Exception occured while deleting row.\n" + se.getMessage());
				}
				addRowsetListener();
			}
		});

		// LISTENER FOR THE TEXT FIELD. USER CAN ENTER A ROW NUMBER IN THE TEXT
		// FIELD TO MOVE THE THE SPECIFIED ROW.
		// IF ITS NOT A NUMBER OR IF ITS NOT VALID FOR THE CURRENT SSROWSET
		// NOTHING HAPPENS.
		txtCurrentRow.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent ke) {
				logger.debug("Record number manually updated.");
				removeRowsetListener();
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						final int row = Integer.parseInt(txtCurrentRow.getText().trim());
						if ((row <= rowCount) && (row > 0)) {
							rowSet.absolute(row);
						}
					} catch (final Exception e) {
						// do nothing
					}
				}
				addRowsetListener();
			}
		});

	}
	
	/**
	 * Adds listener to the rowset
	 */
	private void addRowsetListener() {
		if (!rowsetListenerAdded) {
			rowSet.addRowSetListener(rowsetListener);
			rowsetListenerAdded = true;
		}
	}

	/**
	 * Method to add tooltips and button graphics (or text) to navigator components.
	 */
	protected void addToolTips() {

		try {
			final ClassLoader cl = this.getClass().getClassLoader();
			firstButton.setIcon(new ImageIcon(cl.getResource("images/first.gif")));
			previousButton.setIcon(new ImageIcon(cl.getResource("images/prev.gif")));
			nextButton.setIcon(new ImageIcon(cl.getResource("images/next.gif")));
			lastButton.setIcon(new ImageIcon(cl.getResource("images/last.gif")));
			commitButton.setIcon(new ImageIcon(cl.getResource("images/commit.gif")));
			undoButton.setIcon(new ImageIcon(cl.getResource("images/undo.gif")));
			refreshButton.setIcon(new ImageIcon(cl.getResource("images/refresh.gif")));
			addButton.setIcon(new ImageIcon(cl.getResource("images/add.gif")));
			deleteButton.setIcon(new ImageIcon(cl.getResource("images/delete.gif")));
		} catch (final Exception e) {
			firstButton.setText("<<");
			previousButton.setText("<");
			nextButton.setText(">");
			lastButton.setText(">>");
			commitButton.setText("Commit");
			undoButton.setText("Undo");
			refreshButton.setText("Refresh");
			addButton.setText("Add");
			deleteButton.setText("Delete");
			logger.warn("Unable to load images for navigator buttons.", e);
		}

		// SET TOOL TIPS FOR THE BUTTONS
		firstButton.setToolTipText("First");
		previousButton.setToolTipText("Previous");
		nextButton.setToolTipText("Next");
		lastButton.setToolTipText("Last");
		commitButton.setToolTipText("Commit");
		undoButton.setToolTipText("Undo");
		refreshButton.setToolTipText("Refresh");
		addButton.setToolTipText("Add Record");
		deleteButton.setToolTipText("Delete Record");

	} // end protected void addToolTips() {

	/**
	 * Returns true if the RowSet contains one or more rows, else false.
	 *
	 * @return return true if RowSet contains data else false.
	 */
	public boolean containsRows() {

		if (rowCount == 0) {
			return false;
		}

		return true;
	}

	/**
	 * Adds the navigator components to the navigator panel.
	 */
	protected void createPanel() {

		setButtonSizes();
		// SET THE BOX LAYOUT
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		// ADD BUTTONS TO THE PANEL
		add(firstButton);
		add(previousButton);
		add(txtCurrentRow);
		add(nextButton);
		add(lastButton);
		add(commitButton);
		add(undoButton);
		add(refreshButton);
		add(addButton);
		add(deleteButton);
		add(lblRowCount);
		// pack();
	}

	/**
	 * Calls the doClick on Add Button.
	 */
	public void doAddButtonClick() {
		addButton.doClick();
	}

	/**
	 * Calls the doClick on Commit Button.
	 */
	public void doCommitButtonClick() {
		commitButton.doClick();
	}

	/**
	 * Calls the doClick on Delete Button.
	 */
	public void doDeleteButtonClick() {
		deleteButton.doClick();
	}

	/**
	 * Calls the doClick on First Button.
	 */
	public void doFirstButtonClick() {
		firstButton.doClick();
	}

	/**
	 * Calls the doClick on Last Button.
	 */
	public void doLastButtonClick() {
		lastButton.doClick();
	}

	/**
	 * Calls the doClick on Next Button.
	 */
	public void doNextButtonClick() {
		nextButton.doClick();
	}

	/**
	 * Calls the doClick on Previous Button.
	 */
	public void doPreviousButtonClick() {
		previousButton.doClick();
	}

	/**
	 * Calls the doClick on Refresh Button.
	 */
	public void doRefreshButtonClick() {
		refreshButton.doClick();
	}

	/**
	 * Calls the doClick on Undo Button.
	 */
	public void doUndoButtonClick() {
		undoButton.doClick();
	}

	/**
	 * Returns the size of buttons on the data navigator.
	 *
	 * @return returns a Dimension object representing the size of each button on
	 *         the data navigator.
	 */
	public Dimension getButtonSize() {
		return buttonSize;
	}

	/**
	 * Indicates if the navigator will skip the execute function call on the
	 * underlying RowSet (needed for MySQL - see FAQ).
	 *
	 * @return value of execute() indicator
	 */
	public boolean getCallExecute() {
		return callExecute;
	}

	/**
	 * Returns true if deletions must be confirmed by user, else false.
	 *
	 * @return returns true if a confirmation dialog is displayed when the user
	 *         deletes a record, else false.
	 */
	public boolean getConfirmDeletes() {
		return confirmDeletes;
	}

	/**
	 * Returns any custom implementation of the SSDBNav interface, which is used
	 * when the insert button is pressed to perform custom actions.
	 *
	 * @return any custom implementation of the SSDBNav interface
	 */
	public SSDBNav getDBNav() {
		return dBNav;
	}

	/**
	 * Returns true if deletions are allowed, else false.
	 *
	 * @return returns true if deletions are allowed, else false.
	 */
	public boolean getDeletion() {
		return deletion;
	}

	/**
	 * Returns true if insertions are allowed, else false.
	 *
	 * @return returns true if insertions are allowed, else false.
	 */
	public boolean getInsertion() {
		return insertion;
	}

	/**
	 * Returns true if the user can modify the data in the RowSet, else false.
	 *
	 * @return returns true if the user modifications are written back to the
	 *         database, else false.
	 */
	public boolean getModification() {
		return modification;
	}

	/**
	 * @return the navCombo
	 */
	public SSDBComboBox getNavCombo() {
		return navCombo;
	}

	/**
	 * Returns the RowSet being used.
	 *
	 * @return returns the RowSet being used.
	 */
	public RowSet getRowSet() {
		return rowSet;
	}

//	/**
//	 * Returns the RowSet being used.
//	 *
//	 * @return returns the RowSet being used.
//	 *
//	 * @deprecated Use {@link #getRowSet()} instead.
//	 */
//	@Deprecated
//	public RowSet getSSRowSet() {
//		return rowSet;
//	}

	/**
	 * @return boolean indicating if the navigator is on an insert row
	 */
	public boolean isOnInsertRow() {
		return isInserting(rowSet);
	}
	
	/**
	 * Removes listener from the rowset
	 */
	private void removeRowsetListener() {
		if (rowsetListenerAdded) {
			rowSet.removeRowSetListener(rowsetListener);
			rowsetListenerAdded = false;
		}
	}

	/**
	 * Sets the preferredSize and the MinimumSize of the buttons to the specified
	 * size
	 *
	 * @param _buttonSize the required dimension of the buttons
	 */
	public void setButtonSize(final Dimension _buttonSize) {
		final Dimension oldValue = buttonSize;
		buttonSize = _buttonSize;
		firePropertyChange("buttonSize", oldValue, buttonSize);
		setButtonSizes();
	}

	/**
	 * Sets the dimensions for the navigator components.
	 */
	protected void setButtonSizes() {

		// SET THE PREFERRED SIZES
		firstButton.setPreferredSize(buttonSize);
		previousButton.setPreferredSize(buttonSize);
		nextButton.setPreferredSize(buttonSize);
		lastButton.setPreferredSize(buttonSize);
		commitButton.setPreferredSize(buttonSize);
		undoButton.setPreferredSize(buttonSize);
		refreshButton.setPreferredSize(buttonSize);
		addButton.setPreferredSize(buttonSize);
		deleteButton.setPreferredSize(buttonSize);
		txtCurrentRow.setPreferredSize(txtFieldSize);
		lblRowCount.setPreferredSize(txtFieldSize);
		lblRowCount.setHorizontalAlignment(SwingConstants.CENTER);

		// SET MINIMUM BUTTON SIZES
		firstButton.setMinimumSize(buttonSize);
		previousButton.setMinimumSize(buttonSize);
		nextButton.setMinimumSize(buttonSize);
		lastButton.setMinimumSize(buttonSize);
		commitButton.setMinimumSize(buttonSize);
		undoButton.setMinimumSize(buttonSize);
		refreshButton.setMinimumSize(buttonSize);
		addButton.setMinimumSize(buttonSize);
		deleteButton.setMinimumSize(buttonSize);
		txtCurrentRow.setMinimumSize(txtFieldSize);
		lblRowCount.setMinimumSize(txtFieldSize);
	}

	/**
	 * Method to cause the navigator to skip the execute() function call on the
	 * underlying RowSet. This is necessary for MySQL (see FAQ).
	 *
	 * @param _callExecute false if using MySQL database - otherwise true
	 */
	public void setCallExecute(final boolean _callExecute) {
		final boolean oldValue = callExecute;
		callExecute = _callExecute;
		firePropertyChange("callExecute", oldValue, callExecute);
	}

	/**
	 * Sets the confirm deletion indicator. If set to true, every time delete button
	 * is pressed, the navigator pops up a confirmation dialog to the user. Default
	 * value is true.
	 *
	 * @param _confirmDeletes indicates whether or not to confirm deletions
	 */
	public void setConfirmDeletes(final boolean _confirmDeletes) {
		final boolean oldValue = confirmDeletes;
		confirmDeletes = _confirmDeletes;
		firePropertyChange("confirmDeletes", oldValue, confirmDeletes);
	}

	/**
	 * Function that passes the implementation of the SSDBNav interface. This
	 * interface can be implemented by the developer to perform custom actions when
	 * the insert button is pressed
	 *
	 * @param _dBNav implementation of the SSDBNav interface
	 */
	public void setDBNav(final SSDBNav _dBNav) {
		final SSDBNav oldValue = dBNav;
		dBNav = _dBNav != null ? _dBNav : new SSDBNav(){
			private static final long serialVersionUID = -1655686725609007995L; // unique serial ID
		};
		firePropertyChange("dBNav", oldValue, dBNav);
	}

	/**
	 * Enables or disables the row deletion button. This method should be used if
	 * row deletions are not allowed. True by default.
	 *
	 * @param _deletion indicates whether or not to allow deletions
	 */
	public void setDeletion(final boolean _deletion) {
		final boolean oldValue = deletion;
		deletion = _deletion;
		firePropertyChange("deletion", oldValue, deletion);

		if (!deletion) {
			deleteButton.setEnabled(false);
		} else {
			deleteButton.setEnabled(true);
		}
	}

	/**
	 * This will make all the components in the navigator to either focusable
	 * components or non focusable components. Set to false if you don't want any of
	 * the buttons or text fields in the navigator to receive the focus else true.
	 * The default value is true.
	 *
	 * @param focusable - false if you don't want the navigator to receive focus
	 *                  else false.
	 */
	@Override
	public void setFocusable(final boolean focusable) {
		// MAKE THE BUTTONS NON FOCUSABLE IF REQUESTED
		firstButton.setFocusable(focusable);
		previousButton.setFocusable(focusable);
		nextButton.setFocusable(focusable);
		lastButton.setFocusable(focusable);
		commitButton.setFocusable(focusable);
		undoButton.setFocusable(focusable);
		refreshButton.setFocusable(focusable);
		addButton.setFocusable(focusable);
		deleteButton.setFocusable(focusable);
		txtCurrentRow.setFocusable(focusable);
	}

	/**
	 * Enables or disables the row insertion button. This method should be used if
	 * row insertions are not allowed. True by default.
	 *
	 * @param _insertion indicates whether or not to allow insertions
	 */
	public void setInsertion(final boolean _insertion) {
		final boolean oldValue = insertion;
		insertion = _insertion;
		firePropertyChange("insertion", oldValue, insertion);

		if (!insertion) {
			addButton.setEnabled(false);
		} else {
			addButton.setEnabled(true);
		}
	}

	/**
	 * Enables or disables the modification-related buttons on the SSDataNavigator.
	 * If the user can only navigate through the records with out making any changes
	 * set this to false. By default, the modification-related buttons are enabled.
	 *
	 * @param _modification indicates whether or not the modification-related
	 *                      buttons are enabled.
	 */
	public void setModification(final boolean _modification) {
		final boolean oldValue = modification;
		modification = _modification;
		firePropertyChange("modification", oldValue, modification);

		if (!modification) {
			commitButton.setEnabled(false);
			undoButton.setEnabled(false);
			addButton.setEnabled(false);
			deleteButton.setEnabled(false);
		} else {
			commitButton.setEnabled(true);
			undoButton.setEnabled(true);
			addButton.setEnabled(true);
			deleteButton.setEnabled(true);
		}
	}

	/**
	 * @param _navCombo the navCombo to set
	 */
	public void setNavCombo(final SSDBComboBox _navCombo) {
		navCombo = _navCombo;
	}

	/**
	 * Sets the RowSet for the navigator.
	 *
	 * @param _rowSet data source for navigator
	 */
	public void setRowSet(final RowSet _rowSet) {
		// RESET INSERT FLAG THIS IS NEED IF USERS LEFT THE LAST ROWSET IN INSERTION
		// MODE
		// WITH OUT SAVING THE RECORD OR UNDOING THE INSERTION
		setInserting(rowSet, false);

		// REMOVE ROWSET LISTENER
		if (rowSet != null) {
			removeRowsetListener();
		}

		final RowSet oldValue = rowSet;
		rowSet = _rowSet;
		firePropertyChange("rowSet", oldValue, rowSet);

		// SEE IF THERE ARE ANY ROWS IN THE GIVEN SSROWSET
		try {
			if (callExecute) {
				rowSet.execute();
			}

			if (!rowSet.next()) {
				rowCount = 0;
				currentRow = 0;
			} else {
				// IF THERE ARE ROWS GET THE ROW COUNT
				rowSet.last();
				rowCount = rowSet.getRow();
				rowSet.first();
				currentRow = rowSet.getRow();
			}
			// SET THE ROW COUNT AS LABEL
			lblRowCount.setText("of " + rowCount);
			txtCurrentRow.setText(String.valueOf(currentRow));

			
		} catch (final SQLException se) {
			logger.error("SQL Exception.", se);
		}
		
		// ADD ROWSET LISTENER
		addRowsetListener();

		// IF NO ROWS ARE PRESENT DISABLE NAVIGATION
		// ELSE ENABLE THEN ELSE IS USEFUL WHEN THE SSROWSET IS CHNAGED
		// IF THE INITIAL SSROWSET HAS ZERO ROWS NEXT IF THE USER SETS A NEW SSROWSET
		// THEN THE BUTTONS HAVE TO BE ENABLED
		if (rowCount == 0) {
			firstButton.setEnabled(false);
			previousButton.setEnabled(false);
			nextButton.setEnabled(false);
			lastButton.setEnabled(false);
		} else {
			firstButton.setEnabled(true);
			previousButton.setEnabled(true);
			nextButton.setEnabled(true);
			lastButton.setEnabled(true);
		}

		try {
			if (rowSet.isLast()) {
				nextButton.setEnabled(false);
				lastButton.setEnabled(false);
			}
			if (rowSet.isFirst()) {
				firstButton.setEnabled(false);
				previousButton.setEnabled(false);
			}

		} catch (final SQLException se) {
			logger.error("SQL Exception.", se);
		}

		// ENABLE OTHER BUTTONS IF NEED BE.

		// THIS IS NEEDED TO HANDLE USER LEAVING THE SCREEN IN AN INCONSISTENT
		// STATE EXAMPLE: USER CLICKS ADD BUTTON, THIS DISABLES ALL THE BUTTONS
		// EXCEPT COMMIT & UNDO. WITH OUT COMMITING OR UNDOING THE ADD USER
		// CLOSES THE SCREEN. NOW IF THE SCREEN IS OPENED WITH A NEW SSROWSET.
		// THE REFRESH, ADD & DELETE WILL BE DISABLED.
		// 2019-11-11: only enabling add/delete if this.modification==true
		refreshButton.setEnabled(true);
		if (insertion && modification) {
			addButton.setEnabled(true);
		}
		if (deletion && modification) {
			deleteButton.setEnabled(true);
		}

	}

//	/**
//	 * Sets the RowSet for the navigator.
//	 *
//	 * @param _rowSet data source for navigator
//	 *
//	 * @deprecated Use {@link #setRowSet(RowSet _rowset)} instead.
//	 */
//	@Deprecated
//	public void setSSRowSet(final RowSet _rowSet) {
//		setRowSet(_rowSet);
//	}

	/**
	 * Enables/disables navigation buttons as needed and updates the current row and row count
	 * numbers
	 * @throws SQLException 	SQLException
	 */
	protected void updateNavigator() throws SQLException {

		currentRow = rowSet.getRow();
		// SET THE ROW COUNT AS LABEL
		lblRowCount.setText("of " + rowCount);
		txtCurrentRow.setText(String.valueOf(currentRow));
		
//	    StringBuilder sb = new StringBuilder();
//	    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
//	        sb.append(element.toString());
//	        sb.append("\n");
//	    }

		logger.debug("Current Row: " + currentRow + ". Row Count: " + rowCount);
		//logger.debug("Stack trace:\n" + sb);

		// ENABLE OR DISABLE BUTTONS
		if (rowCount == 0) {
			firstButton.setEnabled(false);
			previousButton.setEnabled(false);
			nextButton.setEnabled(false);
			lastButton.setEnabled(false);
		} else {
			firstButton.setEnabled(true);
			previousButton.setEnabled(true);
			nextButton.setEnabled(true);
			lastButton.setEnabled(true);
		}

		try {
			if (rowSet.isLast()) {
				nextButton.setEnabled(false);
				lastButton.setEnabled(false);
			}
			if (rowSet.isFirst()) {
				firstButton.setEnabled(false);
				previousButton.setEnabled(false);
			}

		} catch (final SQLException se) {
			logger.error("SQL Exception.", se);
		}
	}

	/**
	 * Writes the present row back to the RowSet. This is done automatically when
	 * any navigation takes place, but can also be called manually.
	 *
	 * @return returns true if update succeeds else false.
	 */
	public boolean updatePresentRow() {
		if (isInserting(rowSet) || (currentRow > 0)) {
			logger.debug("Calling doCommitButtonClick().");
			doCommitButtonClick();
		}

		return true;
	}

} // end public class SSDataNavigator extends JPanel {

