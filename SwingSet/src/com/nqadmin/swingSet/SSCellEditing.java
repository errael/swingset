/* $Id$
 *
 * Tab Spacing = 4
 *
 * Copyright (c) 2003, The Pangburn Company, Inc. and Prasanth R. Pasala
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
 
package com.nqadmin.swingSet;

/**
 * SSCellEditing.java
 *<p>
 * SwingSet - Open Toolkit For Making Swing Controls Database-Aware
 *<p><pre>
 *
 *	The SSCellEditing interface specifies methods the SSTableModel will use to
 *interrogate whether a cell can be edited or if the specified value can be allowed
 *for the cell.
 */
public interface SSCellEditing {
	
	/**
	 *	Returns true if the cell at row _row and at column _column is editable else
	 *false. 
	 *
	 *SSTableModel first looks in to uneditable columns, if the column is not
	 *in the uneditable columns list then this function is called (If SSCellEditing 
	 *is implemented).
	 *@param _row the row to which the cell belongs.
	 *@param _column the column to which the cell belongs.
	 *@return returns true is the cell is editable else false.
	 */
	public boolean isCellEditable(int _row, int _column);
	
	/**
	 *	This function is called when ever a update to a cell is done but before 
	 *the value is updated in the database.<BR>
	 *If the function returns false the update is cancelled, if it returns true
	 *the value will be updated in the database.<BR>
	 *
	 *@param _row the row in which update is taking place.<BR>
	 *@param _column the column at which update is taking palce.<BR>
	 *@param _oldValue the present value in the cell being edited.
	 *@param  _newValue the new value entered in the cell being edited.
	 *@return returns true if update should be made else false.
	 */
	public boolean cellUpdateRequested(int _row, int _column, Object _oldValue, Object _newValue); 

	
}




/*
 * $Log$
 */