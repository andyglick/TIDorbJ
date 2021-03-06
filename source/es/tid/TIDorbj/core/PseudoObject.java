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
package es.tid.TIDorbj.core;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.OBJECT_NOT_EXIST;

/**
 * Base class for all TIDorb's pseudobjects.
 * 
 * @author Juan A. C&aacute;ceres
 * @version 1.0
 */

public abstract class PseudoObject
    implements org.omg.CORBA.Object
{

    /**
     * If <code>true</code> the Object has been destroyed and it will throw a
     * <code>org.omg.CORBA.OBJECT_NOT_EXIST<code> exception.
     * Many Objects has the destroy method, this method will change 
     * this attribute.
     */

    protected boolean m_destroyed;

    /**
     * Object hashcode.
     */

    protected int m_hash_code;

    protected PseudoObject()
    {
        m_destroyed = false;
        m_hash_code = -1;
    }

    public boolean _is_a(java.lang.String repositoryIdentifier)
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST();

        if (repositoryIdentifier == null)
            throw new BAD_PARAM("Null string reference");

        return repositoryIdentifier.equals("IDL:omg.org/CORBA/Object:1.0");
    }

    public boolean _non_existent()
    {
        return m_destroyed;
    }

    public int _hash(int maximum)
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST();

        if (m_hash_code == -1) {
            m_hash_code = hashCode();
            while (m_hash_code > maximum)
                m_hash_code %= maximum;
        }

        return m_hash_code;
    }

    public boolean _is_equivalent(org.omg.CORBA.Object other)
    {
        if (other == null)
            throw new BAD_PARAM("Null Object reference");

        return (this == other);
    }

    public org.omg.CORBA.Object _duplicate()
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST();

        return this;
    }

    public void _release()
    {
        if (m_destroyed)
            throw new OBJECT_NOT_EXIST();
    }

    /**
     * @deprecated Deprecated by CORBA 2.3
     */

    public org.omg.CORBA.InterfaceDef _get_interface()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object _get_interface_def()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Request _request(java.lang.String operation)
    {
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    public org.omg.CORBA.Request 
    	_create_request(org.omg.CORBA.Context ctx,
    	                java.lang.String operation,
    	                org.omg.CORBA.NVList arg_list,
    	                org.omg.CORBA.NamedValue result)
    {
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    public org.omg.CORBA.Request 
    	_create_request(org.omg.CORBA.Context ctx,
    	                java.lang.String operation,
    	                org.omg.CORBA.NVList arg_list,
    	                org.omg.CORBA.NamedValue result,
    	                org.omg.CORBA.ExceptionList exclist,
    	                org.omg.CORBA.ContextList ctxlist)
    {
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    public org.omg.CORBA.Policy _get_policy(int policy_type)
    {
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    public org.omg.CORBA.DomainManager[] _get_domain_managers()
    {
        throw new org.omg.CORBA.BAD_OPERATION();
    }

    public org.omg.CORBA.Object 
    	_set_policy_override(org.omg.CORBA.Policy[] policies,
    	                     org.omg.CORBA.SetOverrideType set_add)
    {
        throw new org.omg.CORBA.BAD_OPERATION();
    }
}