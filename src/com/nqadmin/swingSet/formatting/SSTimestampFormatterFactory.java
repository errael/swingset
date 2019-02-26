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

import java.io.Serializable;
import java.text.SimpleDateFormat;

import javax.swing.text.DateFormatter;


/**
 *
 * @author dags
 */
public class SSTimestampFormatterFactory extends javax.swing.text.DefaultFormatterFactory implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1206910593127977868L;

	/**
     * Creates an instance of SSTimestampFormatterFactory 
     * the display format used is dd/MM/yyyy hh:mm:ss SSS Z and the edit format is ddMMyyyyHHmmssSSSZ 
     */
    public SSTimestampFormatterFactory() {
        this.setDefaultFormatter(new DateFormatter(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS Z")));
        this.setNullFormatter(null);
        this.setEditFormatter(new DateFormatter(new SimpleDateFormat("ddMMyyyyHHmmssSSSZ")));
        this.setDisplayFormatter(new DateFormatter(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS Z")));
    }
}


/*
 * $Log$
 * Revision 1.2  2005/05/26 22:20:36  dags
 * SSField interface implemented
 *
 * Revision 1.1  2005/05/26 12:16:20  dags
 * initial release
 *
 */