/* *****************************************************************************
 * Copyright (C) 2024, Prasanth R. Pasala, Brian E. Pangburn, & The Pangburn Group
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
 * ****************************************************************************/
/* *****************************************************************************
 * The conditions in the above copyright notice apply to this copyright notice.
 * Additions and modifications made by Ernie R. Rael are
 * copyright (C) 2024, Ernie R. Rael. All rights reserved.
 * ****************************************************************************/
package com.nqadmin.swingset.navigate;

import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.RowSet;

/**
 *
 * @author err
 */
public class RowSetState
{
	private boolean inserting;
	private NavigateActions navigateActions;

	// don't have to worry about concurrency, always EDT
	private static final Map<RowSet,RowSetState> rowSetState = new WeakHashMap<>();

	private static RowSetState getRowSetState(RowSet rs) {
		return rowSetState.computeIfAbsent(rs, k -> new RowSetState());
	}

	static void setInserting(RowSet rs, boolean flag) {
		if (rs != null) {
			getRowSetState(rs).inserting = flag;
		}
	}

	static void setDataNavigator(RowSet rs, NavigateActions navigator) {
		if (rs != null) {
			getRowSetState(rs).navigateActions = navigator;
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
	 * Find the data navigator for the specified RowSet.
	 * <p>
	 * Originally added to support SSComponentInterface.getSSDataNavigator(),
	 * see discussion #93,
	 * but may come in handy when implementing ActionMap interface.
	 * @param rs get information for this RowSet
	 * @return the associated data navigator
	 */
	public static NavigateActions getSSDataNavigator(RowSet rs) {
		return rs == null ? null : getRowSetState(rs).navigateActions;
	}
	
}
