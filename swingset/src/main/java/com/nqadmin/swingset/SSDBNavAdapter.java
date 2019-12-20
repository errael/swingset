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

/**
 * SSDBNavAdapter.java
 * 
 * SwingSet - Open Toolkit For Making Swing Controls Database-Aware
 * 
 * Abstract class that provides empty implementations of all the methods for the
 * SSDBNav interface.
 *
 * This class is provided for convenience. so that users wishing to write their
 * own SSDBNav implementations can just extend the abstract class and override
 * the desired methods.
 * 
 * @deprecated Starting in 2.3.0+ use {@link SSDBNav} instead.
 * 
 */
@Deprecated
public class SSDBNavAdapter implements SSDBNav {

	/**
	 * unique serial id
	 */
	private static final long serialVersionUID = -6951967166432878580L;

// All logic in this class has been moved to default methods in the interface class.

} // end public class SSDBNavAdapter implements SSDBNav, Serializable {

/*
 * $Log$ Revision 1.11 2005/11/02 17:17:26 prasanth Added empty implementations
 * for allowUpdate & performPostUpdateOps.
 *
 * Revision 1.10 2005/05/03 15:22:28 prasanth Added default implementations for
 * new functions allowInsertion & allowDeletion.
 *
 * Revision 1.9 2005/02/09 17:21:21 yoda2 JavaDoc cleanup.
 *
 * Revision 1.8 2005/02/04 22:48:53 yoda2 API cleanup & updated Copyright info.
 *
 * Revision 1.7 2004/11/11 14:45:48 yoda2 Using TextPad, converted all tabs to
 * "soft" tabs comprised of four actual spaces.
 *
 * Revision 1.6 2004/08/10 22:06:59 yoda2 Added/edited JavaDoc, made code layout
 * more uniform across classes, made various small coding improvements suggested
 * by PMD.
 *
 * Revision 1.5 2004/08/02 15:22:51 prasanth Implements Serializable.
 *
 * Revision 1.4 2004/03/08 16:43:37 prasanth Updated copy right year.
 *
 * Revision 1.3 2003/11/26 21:22:11 prasanth Added function performCancelOps().
 *
 * Revision 1.2 2003/09/25 14:27:45 yoda2 Removed unused Import statements and
 * added preformatting tags to JavaDoc descriptions.
 *
 * Revision 1.1.1.1 2003/09/25 13:56:43 yoda2 Initial CVS import for SwingSet.
 *
 */