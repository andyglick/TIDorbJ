/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDorbJ
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 2 $
* Date: $Date: 2005-12-19 08:58:21 +0100 (Mon, 19 Dec 2005) $
* Last modified by: $Author: caceres $
*
* (C) Copyright 2004 Telefónica Investigación y Desarrollo
*     S.A.Unipersonal (Telefónica I+D)
*
* Info about members and contributors of the MORFEO project
* is available at:
*
*   http://www.morfeo-project.org/TIDorbJ/CREDITS
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*
* If you want to use this software an plan to distribute a
* proprietary application in any way, and you are not licensing and
* distributing your source code under GPL, you probably need to
* purchase a commercial license of the product.  More info about
* licensing options is available at:
*
*   http://www.morfeo-project.org/TIDorbJ/Licensing
*/    
package es.tid.TIDorbj.core.typecode;

import org.omg.CORBA.BAD_TYPECODE;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.portable.InputStream;

import es.tid.TIDorbj.core.cdr.CDROutputStream;
import es.tid.TIDorbj.core.cdr.PointerCDR;

/**
 * The <code>RecursiveTypeCode</code> class represents a <code>TypeCode</code>
 * object which is associated with an IDL recursion in a typecode definition.
 * 
 * @autor Juan A. Ca&acute;ceres
 * @version 1.0
 */

public class RecursiveTypeCode extends TypeCodeImpl
{

    protected String m_repository_id;

    public RecursiveTypeCode()
    {
        m_repository_id = "";
    }

    public RecursiveTypeCode(String id)
    {
        //it is not a real typecode!!!!
        m_repository_id = id;
    }

    public boolean equal(org.omg.CORBA.TypeCode tc)
    {
        if (tc instanceof RecursiveTypeCode)
            return m_repository_id.equals(
                ((RecursiveTypeCode) tc).m_repository_id);
        else
            return false;
    }

    //TIDORB operations

    public void partialUnmarshal(es.tid.TIDorbj.core.cdr.CDRInputStream input)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Marshal the given typecode in a
     * <code>es.tid.TIDorbj.core.CDRInputStream</code>. This method will
     * alwais be invoked by this stream via the <code>TypeCodeMarshaler</code>.
     * 
     * @param type
     *            the <code>TypeCode</code>
     * @param output
     *            the <code>es.tid.TIDorbj.core.CDRInputStream</code>
     * @pre the <code>TypeCode</code> must be a recursisve type
     */

    public static void marshal(TypeCode type, CDROutputStream output)
    {
        try {
            PointerCDR previous_position = null;

            previous_position = output.getContextCDR()
                                      .lookupPosition(type.id());

            if (previous_position == null)
                throw new MARSHAL("Invalid Recursive TypeCode: " + type.id()
                                  + " is not yet marshaled.");

            output.writeIndirection(previous_position);

        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE("Fault in recursive operations:"
                                   + bk.toString());
        }

    }

    /**
     * Dumps the description of a given TypeCode.
     * 
     * @param type
     *            the <code>TypeCode</code>
     * @param output
     *            the output stream where the TypeCode will be dumped
     * @pre <code>type</code> must be an enum type.
     */
    public static void dump(TypeCode type, java.io.PrintWriter output)
        throws java.io.IOException
    {
        try {
            output.print("[TYPECODE]{obj_reference:");
            ComplexTypeCode.dumpParams(type.content_type(), output);
            output.print('}');
        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE("Fault in recursive operation:"
                                   + bk.toString());
        }

    }

    /**
     * Dumps the description of a the marshaled value of a given TypeCode.
     * 
     * @param type
     *            the <code>TypeCode</code>
     * @param input
     *            the input stream where the value is marshaled
     * @param output
     *            the output stream where the value will be dumped
     * @return <code>true</code> if if has been possible dump the value.
     */

    public static boolean dumpValue(TypeCode type, InputStream input,
                                    java.io.PrintWriter output)
        throws java.io.IOException
    {
        try {
            output.print("Indirection to " + type.id() 
                         + "[CAN'T PROCESS MORE.");
            return false;
        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE("Fault in recursive operation:"
                                   + bk.toString());
        }
    }

}