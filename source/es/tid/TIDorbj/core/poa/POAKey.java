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
package es.tid.TIDorbj.core.poa;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.MARSHAL;

import es.tid.TIDorbj.core.ObjectKey;
import es.tid.TIDorbj.core.cdr.CDR;
import es.tid.TIDorbj.core.cdr.CDRInputStream;
import es.tid.TIDorbj.core.cdr.Encapsulation;
import es.tid.TIDorbj.core.comm.giop.GIOPVersion;

public class POAKey extends ObjectKey
    implements Cloneable
{

    /**
     * Magic number: start of an POAKey
     */

    public final static byte MAGIC_START = 0x0;

    /**
     * Start encapsulated key size to verify if it is an POAKey
     */

    public final static int START_BUFFER_SIZE = 
        CDR.ULONG_SIZE  /* encapsulation size  */
        + CDR.BOOLEAN_SIZE  /* byte order */
        + (2 * CDR.OCTET_SIZE); /* and 2 magic number */

    /**
     * Sequence of poa names from the rootPOA to the final POA.
     */
    private String[] m_poas = null;

    /**
     * Rereference durability. If 0 the reference is persistent, otherwise the
     * reference is transient, and it lifespan is joined to its POA lifespan.
     */
    private long m_poa_id = 0L;

    /**
     * Object Id.
     */
    private OID m_oid = null;

    private String m_key_name = null;

    private POAKey(){
    	
    }
      
    
    public POAKey(org.omg.PortableServer.POA poa, long poa_id, OID oid) {
        m_poas = ((POAImpl) (poa)).getPath();
        m_poa_id = poa_id;
        m_oid = oid;
    }

    public POAKey(String[] poas, long poa_id, OID oid)
    {
        m_poas = poas;
        m_poa_id = poa_id;
        m_oid = oid;
    }
    

    public String getPOA(int poa_level)
    {
        return m_poas[poa_level];
    }

    public int numberOfPOAs()
    {
        return m_poas.length;
    }

    public OID getOID()
    {
        return m_oid;
    }

    public long getPOAId()
    {
        return m_poa_id;
    }

    protected void setOID(OID oid)
    {
        m_oid = oid;
    }

    public void write(es.tid.TIDorbj.core.cdr.CDROutputStream output)
    {
    	if ( super.getMarshaledKey() == null ){
    		
    		// create input stream for save the encapsualation
			CDRInputStream encapsulation_input;
            encapsulation_input = output.inputStreamAtThisPosition();
            
            if (m_poas == null) {
                throw new INTERNAL("Uncompleted POAKey");
            }
            
            // begin marshalling
            output.enterEncapsulation();

            // KEY is alwais 1.2

            GIOPVersion output_version = output.getVersion();
            output.setVersion(GIOPVersion.VERSION_1_2);

            // write magic start 2 bytes with 0 value
            output.write_octet(MAGIC_START);
            output.write_octet(MAGIC_START);

            // write string[] with poas path
            //   - number of poas in path
            output.write_ulong(m_poas.length);
            //   - each poa path entry
            for (int i = 0; i < m_poas.length; i++) {
                output.write_string(m_poas[i]);
            }

            // lifespan time
            output.write_longlong(m_poa_id);

            // oid
            byte[] oid_value = m_oid.toByteArray();
            if (oid_value == null) {
                throw new INTERNAL("Uncompleted POAKey");
            }
            output.write_ulong(oid_value.length);
            output.write_octet_array(oid_value, 0, oid_value.length);

            output.setVersion(output_version);
            
            // end marshalling
            output.exitEncapsulation();
            
            super.setMarshaledKey( encapsulation_input.readEncapsulation() );
            
            encapsulation_input = null;
            
    	} else {
    		super.write( output );
    	}

    }

    public void read(es.tid.TIDorbj.core.cdr.CDRInputStream input)
    {

        // KEY is alwais 1.2

        GIOPVersion input_version = input.getVersion();

        input.setVersion(GIOPVersion.VERSION_1_2);

        // check magic bytes

        byte magic_0 = input.read_octet();
        byte magic_1 = input.read_octet();

        if ((magic_0 != MAGIC_START) || (magic_1 != MAGIC_START))
            throw new MARSHAL("Not a TIDorb key");

        // read string[] with poas path

        int length = input.read_ulong();

        if (length < 0)
            throw new INV_OBJREF("Malformed POAKey(bad number of poas)");

        m_poas = new String[length];
        for (int i = 0; i < m_poas.length; i++)
            m_poas[i] = input.read_string();

        m_poa_id = input.read_longlong();

        // read byte[] with oid

        length = input.read_ulong();

        if (length == 0)
            throw new org.omg.CORBA.INV_OBJREF("Malformed POAKey");

        byte[] oid_value = new byte[length];
        input.read_octet_array(oid_value, 0, length);

        m_oid = new OID(oid_value);

        input.setVersion(input_version);
    }

    public boolean samePOA(POAKey other)
    {
        if (m_poa_id != other.m_poa_id)
            return false;
        if (m_poas.length != other.m_poas.length)
            return false;
        for (int i = 0; i < m_poas.length; i++) {
            if (!m_poas[i].equals(other.m_poas[i]))
                return false;
        }
        return true;
    }

    public boolean equals(POAKey other)
    {
        return m_oid.equals(other.m_oid) && samePOA(other);
    }

    public Object clone()
        throws CloneNotSupportedException
    {
        return super.clone();
    }

    public String getPOAPath()
    {
        StringBuffer str = new StringBuffer("/");
        if (m_poas != null) {
            for (int i = 0; i < m_poas.length; i++) {
                str.append(m_poas[i]).append("/");
            }
        }

        return str.toString();
    }
    
    public static POAKey createKey(Encapsulation key)
    {
        if(maybePOAKey(key)) {
            POAKey poaKey = new POAKey();
        	    poaKey.setMarshaledKey( key );
        	    poaKey.read( key.createInputStream() );
        	    return poaKey;
        } else {
            return null;
        }    
        
    }

    public static boolean 
    	maybePOAKey(es.tid.TIDorbj.core.cdr.Encapsulation encap)
    {
        int start = encap.getOffset();

        if (encap.getLength() < START_BUFFER_SIZE)
            return false;

        byte[] start_key_buffer = encap.getOctetSequence();

        return (start_key_buffer[start + START_BUFFER_SIZE - 1] == MAGIC_START)
            && (start_key_buffer[start + START_BUFFER_SIZE - 2] == MAGIC_START);
    }

    public synchronized String toString()
    {
        if (m_key_name == null) {

            StringBuffer str = new StringBuffer("ObjectKey[POA: /");

            if (m_poas != null) {
                for (int i = 0; i < m_poas.length; i++) {
                    str.append(m_poas[i]).append("/");
                }
            }

            if (m_poa_id == 0L)
                str.append("; Type: PERSISTENT ");
            else {
                str.append("; Type: TRANSIENT(POA Id: ");
                str.append(m_poa_id);
                str.append("); ");
            }

            if (m_oid != null)
                str.append(m_oid.toString());

            str.append(']');

            m_key_name = str.toString();
        }
        return m_key_name;

    }
}