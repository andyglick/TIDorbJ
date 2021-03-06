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
* Last modified by: $Author $
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
package es.tid.TIDorbj.core.comm.giop;


import org.omg.IOP.INVOCATION_POLICIES;

import es.tid.TIDorbj.core.policy.PolicyContext;

public class InvocationPoliciesContext extends ServiceContext
{

    public PolicyContext m_policies;
    
    protected InvocationPoliciesContext()
    {
        super(INVOCATION_POLICIES.value);
        m_policies = null;
        
    }
    
    public PolicyContext getPolicies()
    {
        return m_policies;
    }

    public InvocationPoliciesContext(PolicyContext policies)
    {
        super(INVOCATION_POLICIES.value);
        this.m_policies = policies ;
    }

    public void partialRead(es.tid.TIDorbj.core.cdr.CDRInputStream input)
    {
        m_policies = new PolicyContext(null);
        
        input.enterEncapsulation();
        
        m_policies.partialRead(input);
        
        input.exitEncapsulation();        
        
    }

    public void write(es.tid.TIDorbj.core.cdr.CDROutputStream output)
    {

        output.write_ulong(m_context_id);

        output.enterEncapsulation();

        if(m_policies == null) {
            output.write_ulong(0);
        } else {
            m_policies.write(output);
        }

        output.exitEncapsulation();
    }

}
