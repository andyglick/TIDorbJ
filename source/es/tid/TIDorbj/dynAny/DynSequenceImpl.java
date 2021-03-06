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
package es.tid.TIDorbj.dynAny;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_TYPECODE;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;

import es.tid.TIDorbj.core.TIDORB;
import es.tid.TIDorbj.core.cdr.CDRInputStream;
import es.tid.TIDorbj.core.typecode.TypeCodeFactory;
import es.tid.TIDorbj.core.typecode.TypeCodeMarshaler;

/**
 * DynSequence implementation.
 * 
 * @autor Juan A. C&aacute;ceres
 * @version 1.0
 */

public class DynSequenceImpl extends DynComposite
    implements org.omg.DynamicAny.DynSequence
{

    boolean m_bounded;

    int m_bound;

    /**
     * Empty Constructor for generate copies.
     */

    protected DynSequenceImpl(DynAnyFactoryImpl factory, TIDORB orb)
    {
        super(factory, orb);
    }

    /**
     * Constructor. Gets a TypeCode to create a new value. Warning: It assumes
     * that the TypeCode is tk_sequence.
     * 
     * @param any
     *            the any value.
     */

    protected DynSequenceImpl(DynAnyFactoryImpl factory, TIDORB orb,
                              TypeCode type, TypeCode real_type)
    {
        super(factory, orb, type, real_type);
        try {
            m_bound = real_type.length();
        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE();
        }

        m_bounded = (m_bound != 0);
        m_component_count = 0;
    }

    protected DynSequenceImpl(DynAnyFactoryImpl factory, TIDORB orb, Any any,
                              TypeCode real_type)
    {
        super(factory, orb, any, real_type);
        try {
            m_bound = real_type.length();
        }
        catch (BadKind bk) {
            throw new BAD_TYPECODE();
        }
        m_bounded = (m_bound != 0);

        extract_length();
    }

    protected void extract_length()
    {
        m_component_count = m_next_value.read_ulong();
        
        if(m_component_count > 0) {
            m_current_index = 0;
        }
        
        m_next_value.fixStarting();
        if ((m_bound != 0) && (m_component_count > m_bound))
            throw new MARSHAL("Invalid bounded sequence length");
    }

    public org.omg.DynamicAny.DynAny copy()
    {
        if (m_destroyed)
            throw new org.omg.CORBA.OBJECT_NOT_EXIST("DynAny destroyed.");

        DynSequenceImpl new_dyn = new DynSequenceImpl(m_factory, m_orb,
                                                      m_dyn_type, m_base_type);

        copyTo(new_dyn);

        new_dyn.m_bound = m_bound;

        new_dyn.m_bounded = m_bounded;

        return new_dyn;
    }

    // DynSequence Operations

    public int get_length()
    {
        if (m_destroyed)
            throw new org.omg.CORBA.OBJECT_NOT_EXIST("DynAny destroyed.");

        return component_count();
    }

    public void set_length(int len)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new org.omg.CORBA.OBJECT_NOT_EXIST("DynAny destroyed.");

        if (len < 0)
            throw new InvalidValue("Invalid Sequence length: " + len);

        if (len == m_component_count)
            return;

        if (len == 0) {
            reset();
        }

        if (m_bounded && (len > m_bound)) {
            throw new InvalidValue("Invalid Sequence length (" + len
                                   + "), bound is (" + m_bound + ")");
        }

        m_component_count = len;

        int actual_components = m_components.size();

        if (len < actual_components) { //destroy excedent
            m_complete_value = null;
            m_next_value = null;
            for (int i = actual_components - 1; i >= len; i--) {
                ((DynAny) (m_components.elementAt(i))).destroy();
                m_components.removeElementAt(i);
            }
        }
    }

    public org.omg.CORBA.Any[] get_elements()
    {
        return super.get_elements();
    }

    public void set_elements(org.omg.CORBA.Any[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        set_length(value.length);

        super.set_elements(value);
    }

    public void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        set_length(value.length);

        super.set_elements_as_dyn_any(value);
    }

    public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
    {
        return super.get_elements_as_dyn_any();
    }

    public void from_any(org.omg.CORBA.Any value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        super.from_any(value);

        extract_length();
    }

    public void _read(org.omg.CORBA.portable.InputStream is)
    {
        super._read(is);

        extract_length();
    }

    public void _write(org.omg.CORBA.portable.OutputStream os)
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST();

        if (os == null)
            throw new BAD_PARAM("Null OutputStream reference");

        if (m_complete_value != null) {// value marshaled
            CDRInputStream value_copy = m_complete_value.copy();
            value_copy.rewind();
            TypeCodeMarshaler.remarshalValue(m_base_type, value_copy, os);
        } else { // value in the dynAny structure

            os.write_long(m_component_count);

            super._write(os);
        }
    }

    protected TypeCode getComponentType(int position)
    {

        try {
            return m_base_type.content_type();
        }
        catch (BadKind bk) {
            /* unreachable */
            throw new org.omg.CORBA.BAD_TYPECODE();
        }

    }

    public void insert_boolean_seq(boolean[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_boolean)) {
            set_length(value.length);
            insert_boolean_members(value);

        } else {
            set_length(value.length);
            super.insert_boolean_seq(value);
        }
    }

    public void insert_octet_seq(byte[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_octet)) {
            set_length(value.length);
            insert_octet_members(value);
        } else {
            set_length(value.length);
            super.insert_octet_seq(value);
        }
    }

    public void insert_char_seq(char[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_char)) {
            set_length(value.length);
            insert_char_members(value);
        } else {
            set_length(value.length);
            super.insert_char_seq(value);
        }
    }

    public void insert_short_seq(short[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_short)) {
            set_length(value.length);
            insert_short_members(value);
        } else {
            set_length(value.length);
            super.insert_short_seq(value);
        }
    }

    public void insert_ushort_seq(short[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_ushort)) {
            set_length(value.length);
            insert_ushort_members(value);
        } else {
            set_length(value.length);
            super.insert_ushort_seq(value);
        }
    }

    public void insert_long_seq(int[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_long)) {
            set_length(value.length);
            insert_long_members(value);
        } else {
            set_length(value.length);
            super.insert_long_seq(value);
        }
    }

    public void insert_ulong_seq(int[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_ulong)) {
            set_length(value.length);
            insert_ulong_members(value);
        } else {
            set_length(value.length);
            super.insert_ulong_seq(value);
        }
    }

    public void insert_float_seq(float[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_float)) {
            set_length(value.length);
            insert_float_members(value);
        } else {
            set_length(value.length);
            super.insert_float_seq(value);
        }
    }

    public void insert_double_seq(double[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_double)) {
            set_length(value.length);
            insert_double_members(value);
        } else {
            set_length(value.length);
            super.insert_double_seq(value);
        }
    }

    public void insert_longlong_seq(long[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_longlong)) {
            set_length(value.length);
            insert_longlong_members(value);
        } else {
            set_length(value.length);
            super.insert_longlong_seq(value);
        }
    }

    public void insert_ulonglong_seq(long[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_ulonglong)) {
            set_length(value.length);
            insert_ulonglong_members(value);
        } else {
            set_length(value.length);
            super.insert_ulonglong_seq(value);
        }
    }

    public void insert_wchar_seq(char[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (value == null)
            throw new BAD_PARAM("null array reference");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_wchar)) {
            set_length(value.length);
            insert_wchar_members(value);
        } else {
            set_length(value.length);
            super.insert_wchar_seq(value);
        }
    }

    public boolean[] get_boolean_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_boolean)) {
            return super.get_boolean_members();
        } else {
            return super.get_boolean_seq();
        }
    }

    public byte[] get_octet_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_octet)) {
            return super.get_octet_members();
        } else {
            return super.get_octet_seq();
        }
    }

    public char[] get_char_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_char)) {
            return super.get_char_members();
        } else {
            return super.get_char_seq();
        }
    }

    public short[] get_short_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_short)) {
            return super.get_short_members();
        } else {
            return super.get_short_seq();
        }
    }

    public short[] get_ushort_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_ushort)) {
            return super.get_ushort_members();
        } else {
            return super.get_ushort_seq();
        }
    }

    public int[] get_long_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_long)) {
            return super.get_long_members();
        } else {
            return super.get_long_seq();
        }
    }

    public int[] get_ulong_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_ulong)) {
            return super.get_ulong_members();
        } else {
            return super.get_ulong_seq();
        }
    }

    public float[] get_float_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_float)) {
            return super.get_float_members();
        } else {
            return super.get_float_seq();
        }
    }

    public double[] get_double_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_double)) {
            return super.get_double_members();
        } else {
            return super.get_double_seq();
        }
    }

    public long[] get_longlong_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_longlong)) {
            return super.get_longlong_members();
        } else {
            return super.get_longlong_seq();
        }
    }

    public long[] get_ulonglong_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        typeMismatch();
        return null;
    }

    public char[] get_wchar_seq()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
        org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST("DynAny destroyed.");

        if (getComponentType(0).equivalent(TypeCodeFactory.tc_wchar)) {
            return super.get_wchar_members();
        } else {
            return super.get_wchar_seq();
        }
    }

    // Object methods

    protected final static String[] __ids = 
    	{ "IDL:omg.org/DynamicAny/DynAny:1.0", };

    public boolean _is_a(java.lang.String repositoryIdentifier)
    {
        if (repositoryIdentifier == null)
            throw new BAD_PARAM("Null string reference");

        if (repositoryIdentifier
            .equals("IDL:omg.org/DynamicAny/DynSequence:1.0"))
            return true;

        return super._is_a(repositoryIdentifier);
    }

}