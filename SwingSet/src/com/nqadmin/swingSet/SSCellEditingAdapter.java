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

import javax.swing.*;

/**
  * SSCellEditingAdapter.java
 *<p>
 * SwingSet - Open Toolkit For Making Swing Controls Database-Aware
 *<p><pre
 *
 *	This abstract adapter class is provided as convenience for creating SSCellEditing objects.
 *Extend this class to create a SSCellEditing implementation.
 *
 *This class defines empty functions so that you can define only functions that you want to.
 *Both isCellEditable and  cellUpdateRequested always return true.
 */
public abstract class SSCellEditingAdapter implements SSCellEditing {
	
	/**
	 *	This empty implementation always returns true.
	 *For description about the function look in SSCellEditing class.
	 */
	public boolean isCellEditable(int _row, int _column){
		return true;
	}
	
	/**
	 *	This empty implementation always returns true.
	 *For description about the function look in SSCellEditing class.
	 */
	public boolean cellUpdateRequested(int _row, int _column, Object _oldValue, Object _newValue){
		return true;
	}
}



/*
 * $Log$
 */