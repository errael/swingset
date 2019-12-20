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

package com.nqadmin.swingset.formatting;

import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

/**
 * SSCuitField.java
 * 
 * SwingSet - Open Toolkit For Making Swing Controls Database-Aware
 * 
 * SSCuitField extends the SSFormattedTextField. This class provides an implementation 
 * of Argentina's Tax ID. Every taxpayer in Argentina must have this Government supplied 
 * ID. It is an 10 digits code plus one verifier digit. Display format is ##-########-#.
 */

public class SSCuitField extends SSFormattedTextField {
  
    /**
	 * unique serial id
	 */
	private static final long serialVersionUID = 6012580680828883089L;
	private Caret cuitCaret;
  
    /** 
     * Creates a new instance of SSCuitFieldField 
     */
    public SSCuitField() {
        this(new SSCuitFormatterFactory());
    }
    
    /** Creates a new instance of SSCuitFieldField with the specified formatter factory 
     * @param factory - formatter factory to be used
     */
    public SSCuitField(javax.swing.JFormattedTextField.AbstractFormatterFactory factory) {
        super(factory);
        
        this.cuitCaret = new DefaultCaret();
        this.cuitCaret.setBlinkRate(600);
        
        try {
            this.cuitCaret.setSelectionVisible(true);
        } catch(java.lang.NullPointerException np) {
        	// do nothing
        }
        

        this.setCaret(this.cuitCaret);
        
    }

    /* (non-Javadoc)
     * @see com.nqadmin.swingset.formatting.SSFormattedTextField#validateField(java.lang.Object)
     */
    @Override
	public boolean validateField(Object value) {

        boolean retValue;

        retValue = CheckCuit((String)value);
        
        return retValue;
    }

    /**
	 * Computes verifier digit and checks against supplied value.
	 *
	 * @param  cu    the CUIT value to be verified.
	 * @return true if CUIT is valid, else false
	 */
    public static boolean CheckCuit(final String cu)
    {
        
        String base = new String("54 32765432  ");
        String c1, c2;
        StringBuffer cuit;
        StringBuffer ctrl;
        
        int mo, ba, mr, i;
        
        ctrl = new StringBuffer(cu);
        cuit = new StringBuffer(cu);
        
        for (mo=0, i=0; i < 12; i++)
        {
            if (i==2 || i==11) continue;
            mo += (base.charAt(i) - '0') * (cuit.charAt(i) - '0');
        }
        
        mr = mo%11;
        
        if (mr==0)
            ba = 0;
        else
            if (mr==1)
            {
            return false;
            }
            else
                ba = 11 - mr;
        
        ctrl.setCharAt(12, (char)(ba + '0'));
        
        c1 = new String(cuit);
        c2 = new String(ctrl);
        
        if (c1.compareTo(c2) != 0)
        {
            return false;
        }
        return true;
        
    }
}

/*
 * $Log$
 * Revision 1.1  2005/06/08 02:26:02  dags
 * initial release
 * 
 */