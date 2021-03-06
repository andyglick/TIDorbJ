/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDorbJ
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 478 $
* Date: $Date: 2011-04-29 16:42:47 +0200 (Fri, 29 Apr 2011) $
* Last modified by: $Author: avega $
*
* (C) Copyright 2004 Telef�nica Investigaci�n y Desarrollo
*     S.A.Unipersonal (Telef�nica I+D)
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
package es.tid.TIDorbj.core.comm.iiop;

import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_ORB_TYPE;
import org.omg.IOP.TAG_POLICIES;
import org.omg.IOP.TAG_SSL_SEC_TRANS;
import org.omg.IOP.TAG_CSI_SEC_MECH_LIST;

import es.tid.TIDorbj.core.ConfORB;
import es.tid.TIDorbj.core.ObjectKey;
import es.tid.TIDorbj.core.cdr.CDRInputStream;
import es.tid.TIDorbj.core.cdr.CDROutputStream;
import es.tid.TIDorbj.core.comm.giop.GIOPVersion;
import es.tid.TIDorbj.core.iop.ORBComponent;
import es.tid.TIDorbj.core.comm.ssliop.SSLComponent;
import es.tid.TIDorbj.core.security.CSIComponent;
import es.tid.TIDorbj.core.iop.TaggedComponent;
import es.tid.TIDorbj.core.iop.TaggedComponentReader;
import es.tid.TIDorbj.core.iop.TaggedProfile;
import es.tid.TIDorbj.core.messaging.PoliciesComponent;
import es.tid.TIDorbj.core.poa.POAKey;
import es.tid.TIDorbj.core.policy.PolicyContext;

/**
 * IIOPProfile defined in the GIOP Module.
 * 
 * @autor Juan A. C&aacute;ceres
 * @version 1.0
 */
public class IIOPProfile extends TaggedProfile {

    private GIOPVersion m_version;

    private ListenPoint m_listen_point;

    private ObjectKey m_object_key;

    private TaggedComponent[] m_components;

    private CDRInputStream m_profile_data;

    public IIOPProfile()
    {
        super(TAG_INTERNET_IOP.value);

        m_version = null;
        m_listen_point = null;
        m_object_key = null;
        m_components = null;
        m_profile_data = null;
    }

    public IIOPProfile(GIOPVersion version, ListenPoint listen_point)
    {
        super(TAG_INTERNET_IOP.value);
        this.m_version = version;
        this.m_listen_point = listen_point;
        m_object_key = null;
        m_components = null;
        m_profile_data = null;
    }

    public IIOPProfile(GIOPVersion version, 
                       ListenPoint listen_point,
                       ObjectKey object_key, 
                       TaggedComponent[] tagged_components)
    {
        super(TAG_INTERNET_IOP.value);
        this.m_version = version;
        this.m_listen_point = listen_point;
        this.m_object_key = object_key;
        m_components = tagged_components;
        m_profile_data = null;
    }

    public GIOPVersion getVersion()
    {
        if (m_version == null)
            extractMembers();
        return m_version;

    }

    public ListenPoint getListenPoint()
    {
        if (m_listen_point == null)
            extractMembers();

        // Remove %interface from host string
        int pos = m_listen_point.m_host.indexOf("%");
        if (pos != -1) {
            String only_host_string;
            only_host_string = m_listen_point.m_host.substring(0,pos);
            m_listen_point.m_host = only_host_string;
        }

        return m_listen_point;
    }

    public ObjectKey getObjectKey()
    {
        if (m_object_key == null)
            extractMembers();
        return m_object_key;
    }

    public TaggedComponent[] getTaggedComponents()
    {
        if (m_components == null)
            extractMembers();
        return m_components;
    }

    public boolean equal( Object profile)
    {
        if (m_profile_data != null)
            extractMembers();
        
        IIOPProfile iiopProfile;
        if ( profile instanceof IIOPProfile ){
            iiopProfile = ( IIOPProfile )profile;
        } else {
            iiopProfile = null;
        }
        
        return iiopProfile != null &&
               getVersion()    .equal ( iiopProfile.getVersion()     ) && 
               getListenPoint().equals( iiopProfile.getListenPoint() ) &&
               getObjectKey()  .equal ( iiopProfile.getObjectKey()   );
    }

    public void partialRead( es.tid.TIDorbj.core.cdr.CDRInputStream input ) {
        m_profile_data = input.copy();
        input.skipEncapsulation();
    }

    synchronized protected void extractMembers() {
        if (m_version != null) // has the members
            return;

        if (m_profile_data == null)
            throw new org.omg.CORBA.INTERNAL("Empty Profile");

        m_profile_data.enterEncapsulation();

        m_version = GIOPVersion.read(m_profile_data);

        m_listen_point = ListenPoint.read(m_profile_data);

        m_object_key = new ObjectKey();
        m_object_key.read( m_profile_data );
        if (m_version.minor != 0) {
            int size = m_profile_data.read_ulong();
            if (size < 0) {
                throw new org.omg.CORBA.MARSHAL("Invalid component size");
            } else {
                m_components = new TaggedComponent[size];
                for (int i = 0; i < size; i++) {

                    TaggedComponent comp = 
                        TaggedComponentReader.read(m_profile_data);

                    m_components[i] = comp;

                    // Check tag 
                    
                    if (comp.m_tag == TAG_POLICIES.value) {
                        PoliciesComponent policies_component = (PoliciesComponent) comp;
                        //m_policies = policies_component.getPolicies();
                    }
                    if (comp.m_tag == TAG_SSL_SEC_TRANS.value) {
                        SSLComponent ssl_component = (SSLComponent) comp;
                        m_listen_point.m_ssl_port = ssl_component.getSSLPort();
                        // m_ssl = ssl_component.getSSL();
                    }
                    if (comp.m_tag == TAG_CSI_SEC_MECH_LIST.value) {
                        CSIComponent csi_component = (CSIComponent) comp;

                    }
                    
                }
            }
        }

        m_profile_data = null;
    }

    public void write(CDROutputStream out)
    {
        extractMembers();

        // write tag
        out.write_ulong(tag);

        // write profile data

        out.enterEncapsulation();

        m_version.write(out);
        ListenPoint.write(out, m_listen_point);
        m_object_key.write(out);

        if (m_version.minor != 0) {
            if (m_components == null)
                out.write_ulong(0);
            else {
                out.write_ulong(m_components.length);
                for (int i = 0; i < m_components.length; i++)
                    m_components[i].write(out);
            }
        }

        out.exitEncapsulation();
    }

    public String toString()
    {
        if (m_profile_data != null)
            extractMembers();

        boolean from_tidorb = false;

        if (m_components != null)
            for (int i = 0; i < m_components.length; i++)
                if (m_components[i].m_tag == TAG_ORB_TYPE.value) {
                    if (((ORBComponent) m_components[i]).m_orb_type
                        == ConfORB.ORB_TYPE.m_orb_type) {
                        from_tidorb = true;
                        break;
                    }
                }

        StringBuffer buffer = new StringBuffer();

        buffer.append("Profile: { ");
        buffer.append('\n');
        buffer.append('\t');
        buffer.append(getVersion().toString());
        buffer.append(", ");
        buffer.append('\n');
        buffer.append('\t');
        buffer.append(getListenPoint().toString());
        buffer.append(", ");

        ORBComponent orb_component = getORBComponent();
        if (orb_component != null) {
            buffer.append('\n');
            buffer.append('\t');
            buffer.append("TAG_ORB_TYPE: ");
            buffer.append(orb_component.toString());
        }

        PoliciesComponent policies_component = getPoliciesComponent();
        if (policies_component != null) {
            buffer.append('\n');
            buffer.append('\t');
            buffer.append("TAG_POLICIES: ");
            buffer.append(policies_component.toString());
        }


        SSLComponent ssl_component = getSSLComponent();
        if (ssl_component != null) {
            buffer.append('\n');
            buffer.append('\t');
            buffer.append("TAG_SSL_SEC_TRANS: ");
            buffer.append(ssl_component.toString());
        }

        CSIComponent csi_component = getCSIComponent();
        if (csi_component != null) {
            buffer.append('\n');
            buffer.append('\t');
            buffer.append("TAG_CSI_SEC_MECH_LIST: ");
            buffer.append(csi_component.toString());
        }
      
        buffer.append('\n');
        buffer.append('\t');

        if ( from_tidorb ) {
        	POAKey key;
        	try {
        		key = POAKey.createKey( getObjectKey().getMarshaledKey() );
        		buffer.append( key.toString() );
        	} catch ( Throwable th ) {
        		buffer.append( "ObjectKey[OPAQUE]" );
        	}
            
        } else {
            buffer.append("ObjectKey[OPAQUE]");
        }
        buffer.append('\n');
        buffer.append('}');

        return buffer.toString();
    }

    /**
     * @return
     */
    public PoliciesComponent getPoliciesComponent()
    {
        if (m_components != null) {
            for (int i = 0; i < m_components.length; i++)
                if (m_components[i].m_tag == TAG_POLICIES.value) {
                    return (PoliciesComponent) m_components[i];
                }
        }
    
        return null;
    }


    public ORBComponent getORBComponent()
    {
        if (m_components != null) {
            for (int i = 0; i < m_components.length; i++)
                if (m_components[i].m_tag == TAG_ORB_TYPE.value) {
                    return (ORBComponent) m_components[i];
                }
        }
    
        return null;
    }

    public SSLComponent getSSLComponent()
    {
        if (m_components != null) {
            for (int i = 0; i < m_components.length; i++)
                if (m_components[i].m_tag == TAG_SSL_SEC_TRANS.value) {
                    return (SSLComponent) m_components[i];
                }
        }
        return null;
    }


    public CSIComponent getCSIComponent()
    {
        if (m_components != null) {
            for (int i = 0; i < m_components.length; i++)
                if (m_components[i].m_tag == TAG_CSI_SEC_MECH_LIST.value) {
                    return (CSIComponent) m_components[i];
                }
        }
        return null;
    }


    public PolicyContext getPolicies()
    {
        PoliciesComponent policies_component = this.getPoliciesComponent();
        if (policies_component != null)
            return policies_component.getPolicies();
        else
            return null;
    }

}
