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
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import es.tid.TIDorbj.core.cdr.CDRInputStream;
import es.tid.TIDorbj.core.cdr.CDROutputStream;

/**
 * The <code>ArrayTypeCode</code> class represents a <code>TypeCode</code>
 * object which is associated with an IDL array.
 * 
 * @autor Juan A. Ca&acute;ceres
 * @version 1.0
 */

public class ArrayTypeCode extends TypeCodeImpl
{

    protected TypeCode _element_type;

    protected int _length;

    public ArrayTypeCode()
    {
        super(TCKind.tk_array);
        _element_type = null;
        _length = 0;
    }

    public ArrayTypeCode(TypeCode element_type, int length)
    {
        super(TCKind.tk_array);
        _element_type = element_type;
        _length = length;
    }

    public boolean equal(org.omg.CORBA.TypeCode tc)
    {
        if (!super.equal(tc))
            return false;
        try {
            return (_element_type.equal(tc.content_type()) 
                   && (_length == tc.length()));
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
            return false;
        }
    }

    public int length()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        return _length;
    }

    public org.omg.CORBA.TypeCode content_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        return _element_type;
    }

    //TIDORB operations

    public boolean isSimple()
    {
        return false;
    }

    public static void skip_params(es.tid.TIDorbj.core.cdr.CDRInputStream input)
    {
        int length = input.read_ulong();
        input.skip(length);
    }

    public void partialUnmarshal(es.tid.TIDorbj.core.cdr.CDRInputStream input)
    {

        input.enterEncapsulation();

        _element_type = input.read_TypeCode();
        _length = input.read_ulong();

        input.exitEncapsulation();
    }

    /**
     * Skips the value asociated to the TypeCode. This operation is used by the
     * TIDorb's Any implementation an the subclass <code>skip_value()</code>
     * operations.
     * 
     * @param input
     *            must be alwais a reference to a CDRInputStream object.
     */

    public static boolean skip_value(TypeCode type, CDRInputStream input)
    {
        try {
            return TypeCodeMarshaler.skipValueArray(type.content_type(), input,
                                                    type.length());
        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE(" Fault in Array type operation: "
                                   + bk.toString());
        }
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
     * @pre the <code>TypeCode</code> must be an array type
     */

    public static void marshal(TypeCode type, CDROutputStream output)
    {
        try {
            // write TCKind
            output.write_long(type.kind().value());

            // copy the output for insert the encapsulation length later

            CDROutputStream encapsulation = output.copy();

            // marshalling of params

            output.enterEncapsulation();

            output.write_TypeCode(type.content_type());
            output.write_ulong(type.length());

            output.exitEncapsulation();

        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE(" Fault in Array type operation: "
                                   + bk.toString());
        }
    }

    /**
     * Copies and remarshals the given typecode value marshaled in an
     * InputStream to a <code>es.tid.TIDorbj.core.CDRInputStream</code>. This
     * method will alwais be invoked by this stream.
     * 
     * @param type
     *            the value <code>TypeCode</code>
     * @param input
     *            the <code>InputStream</code> where the value is marshaled
     * @param output
     *            the <code>es.tid.TIDorbj.core.CDRInputStream</code>
     * @pre the <code>TypeCode</code> must be an array type
     */

    public static void remarshal_value(TypeCode type, InputStream input,
                                       OutputStream output)
    {
        try {
            int length = type.length();
            for (int i = 0; i < length; i++)
                TypeCodeMarshaler.remarshalValue(type.content_type(), input,
                                                 output);
        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE(" Fault in Array type operation: "
                                   + bk.toString());
        }
    }

    /**
     * Compares two InputStream marshaled values of a given TypeCode to a
     * <code>es.tid.TIDorbj.core.CDRInputStream</code>. This method will
     * alwais be invoked by this stream.
     * 
     * @param type
     *            the value <code>TypeCode</code>
     * @param input_a
     *            the <code>InputStream</code> where one value is marshaled
     * @param input_b
     *            the <code>InputStream</code> where the value value is
     *            marshaled
     * @pre <code>type</code> must be an array type.
     */

    public static boolean values_equal(org.omg.CORBA.TypeCode type,
                                       InputStream input_a, InputStream input_b)
    {
        try {
            int length = type.length();

            for (int i = 0; i < length; i++)
                if (!TypeCodeMarshaler.valuesEqual(type.content_type(),
                                                   input_a, input_b))
                    return false;
        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE(" Fault in Array type operation: "
                                   + bk.toString());
        }
        return true;
    }

    /**
     * Dumps the description of a given TypeCode.
     * 
     * @param type
     *            the <code>TypeCode</code>
     * @param output
     *            the output stream where the TypeCode will be dumped
     * @pre <code>type</code> must be an array type.
     */

    public static void dump(org.omg.CORBA.TypeCode type,
                            java.io.PrintWriter output)
        throws java.io.IOException
    {
        try {
            output.print("[TYPECODE]{");
            TypeCodeDumper.dump(type.content_type(), output);
            output.print('[');
            output.print(type.length());
            output.print("]}");
        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE("Array type expected to invoke length()" 
                                   + "and content_type().");
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
     * @pre the typecode must be an array type
     * @return <code>true</code> if if has been possible dump the value.
     */

    public static boolean dump_value(TypeCode type, InputStream input,
                                     java.io.PrintWriter output)
        throws java.io.IOException
    {
        try {
            int length = type.length();
            org.omg.CORBA.TypeCode elements_type = type.content_type();
            output.print("[VALUE]{");
            for (int i = 0; i < length; i++) {
                output.print('(');
                output.print(i);
                output.print(") ");
                if (!TypeCodeDumper.dumpValue(elements_type, input, output))
                    return false;
                output.print(" | ");
            }

            output.print(" END_ARRAY-}");
        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE("Array type expected to invoke length()" +
                                   "and content_type().");
        }

        return true;
    }

}