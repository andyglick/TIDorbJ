/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDorbJ
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 395 $
* Date: $Date: 2009-05-27 16:10:32 +0200 (Wed, 27 May 2009) $
* Last modified by: $Author: avega $
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

class ORBServices
{
    public final static String ROOT_POA_ID = "RootPOA";

    public final static int ROOT_POA = 0;

    public final static String POA_CURRENT_ID = "POACurrent";

    public final static int POA_CURRENT = 1;

    public final static String INTERFACE_REPOSITORY_ID = "InterfaceRepository";

    public final static int INTERFACE_REPOSITORY = 2;

    public final static String NAME_SERVICE_ID = "NameService";

    public final static int NAME_SERVICE = 3;

    public final static String TRADING_SERVICE_ID = "TradingService";

    public final static int TRADING_SERVICE = 4;

    public final static String SECURITY_CURRENT_ID = "SecurityCurrent";

    public final static int SECURITY_CURRENT = 5;

    public final static String TRANSACTION_CURRENT_ID = "TransactionCurrent";

    public final static int TRANSACTION_CURRENT = 6;

    public final static String DYN_ANY_FACTORY_ID = "DynAnyFactory";

    public final static int DYN_ANY_FACTORY = 7;

    public final static String ORB_POLICY_MANAGER_ID = "ORBPolicyManager";

    public final static int ORB_POLICY_MANAGER = 8;

    public final static String POLICY_CURRENT_ID = "PolicyCurrent";

    public final static int POLICY_CURRENT = 9;

    public final static String NOTIFICATION_SERVICE_ID = "NotificationService";

    public final static int NOTIFICATION_SERVICE = 10;

    public final static String TYPED_NOTIFICATION_SERVICE_ID = 
        "TypedNotificationService";

    public final static int TYPED_NOTIFICATION_SERVICE = 11;

    public final static String CODEC_FACTORY_ID = "CodecFactory";

    public final static int CODEC_FACTORY = 12;

    public final static String PI_CURRENT_ID = "PICurrent";

    public final static int PI_CURRENT = 13;
    
    public final static String RT_ORB_ID = "RTORB";

    public final static int RT_ORB = 14;

    public final static String COMPRESSION_MANAGER_ID = "CompressionManager";

    public final static int COMPRESSION_MANAGER = 15;
    
   

    private TIDORB m_orb;

    private java.util.Hashtable m_corba_services_ids;

    private java.util.Hashtable m_service_table;

    private final static Integer NULL_SERVICE = new Integer(0);

    public ORBServices(TIDORB orb)
    {
        m_orb = orb;

        m_corba_services_ids = new java.util.Hashtable();

        m_corba_services_ids.put(ROOT_POA_ID, new Integer(ROOT_POA));
        m_corba_services_ids.put(POA_CURRENT_ID, new Integer(POA_CURRENT));
        m_corba_services_ids.put(INTERFACE_REPOSITORY_ID,
                                 new Integer(INTERFACE_REPOSITORY));
        m_corba_services_ids.put(NAME_SERVICE_ID, new Integer(NAME_SERVICE));
        m_corba_services_ids.put(TRADING_SERVICE_ID,
                                 new Integer(TRADING_SERVICE));
        m_corba_services_ids.put(SECURITY_CURRENT_ID,
                                 new Integer(SECURITY_CURRENT));
        m_corba_services_ids.put(TRANSACTION_CURRENT_ID,
                                 new Integer(TRANSACTION_CURRENT));
        m_corba_services_ids.put(DYN_ANY_FACTORY_ID,
                                 new Integer(DYN_ANY_FACTORY));
        m_corba_services_ids.put(ORB_POLICY_MANAGER_ID,
                                 new Integer(ORB_POLICY_MANAGER));
        m_corba_services_ids.put(POLICY_CURRENT_ID, 
                                 new Integer(POLICY_CURRENT));
        m_corba_services_ids.put(NOTIFICATION_SERVICE_ID,
                                 new Integer(NOTIFICATION_SERVICE));
        m_corba_services_ids.put(TYPED_NOTIFICATION_SERVICE_ID,
                                 new Integer(TYPED_NOTIFICATION_SERVICE));
        m_corba_services_ids.put(CODEC_FACTORY_ID, new Integer(CODEC_FACTORY));
        m_corba_services_ids.put(PI_CURRENT_ID, new Integer(PI_CURRENT));
        m_corba_services_ids.put(RT_ORB_ID, new Integer(RT_ORB));
        m_corba_services_ids.put(COMPRESSION_MANAGER_ID, new Integer(COMPRESSION_MANAGER));

        m_service_table = new java.util.Hashtable();

        m_service_table.put(ROOT_POA_ID, NULL_SERVICE);
        m_service_table.put(POA_CURRENT_ID, NULL_SERVICE);
        m_service_table.put(INTERFACE_REPOSITORY_ID, NULL_SERVICE);
        m_service_table.put(NAME_SERVICE_ID, NULL_SERVICE);
        m_service_table.put(TRADING_SERVICE_ID, NULL_SERVICE);
        m_service_table.put(SECURITY_CURRENT_ID, NULL_SERVICE);
        m_service_table.put(TRANSACTION_CURRENT_ID, NULL_SERVICE);
        m_service_table.put(DYN_ANY_FACTORY_ID, NULL_SERVICE);
        m_service_table.put(ORB_POLICY_MANAGER_ID, NULL_SERVICE);
        m_service_table.put(POLICY_CURRENT_ID, NULL_SERVICE);
        m_service_table.put(NOTIFICATION_SERVICE_ID, NULL_SERVICE);
        m_service_table.put(TYPED_NOTIFICATION_SERVICE_ID, NULL_SERVICE);
        m_service_table.put(CODEC_FACTORY_ID, NULL_SERVICE);
        m_service_table.put(PI_CURRENT_ID, NULL_SERVICE);
        m_service_table.put(RT_ORB_ID, NULL_SERVICE);
        m_service_table.put(COMPRESSION_MANAGER_ID, NULL_SERVICE);

    }

    public synchronized void destroy()
    {
        m_service_table.clear();
        m_service_table = null;

        m_corba_services_ids.clear();
        m_corba_services_ids = null;
    }

    public synchronized org.omg.CORBA.Object resolveService(String object_name)
        throws org.omg.CORBA.ORBPackage.InvalidName
    {
        java.lang.Object table_value = m_service_table.get(object_name);

        if (table_value == null) {
            if (m_orb.m_conf.default_initial_reference == null)
                throw new org.omg.CORBA.BAD_PARAM(object_name);

            return m_orb.string_to_object(
                       m_orb.m_conf.default_initial_reference
                       + object_name);
        }

        org.omg.CORBA.Object obj = null;

        if (table_value instanceof org.omg.CORBA.Object)
            return (org.omg.CORBA.Object) table_value;

        if ((table_value instanceof Integer)
            && (((Integer) table_value) != NULL_SERVICE)) {
        	//TODO: recomment this
        	//known services are registered as NULL_SERVICE to
        	//mark them as known initially, and instantiated when
        	//requested... if a service is registered with neither
        	//its ior nor the null service, will be an error
            throw new org.omg.CORBA.INTERNAL();
        }

        Integer orb_service_val = 
            (Integer) m_corba_services_ids.get(object_name);
        
        switch (orb_service_val.intValue())
        {
            case ROOT_POA:
                obj = m_orb.initPOA();
                m_service_table.put(ROOT_POA_ID, obj);
                return obj;
            case POA_CURRENT:
                obj = m_orb.initPOACurrent();
                m_service_table.put(POA_CURRENT_ID, obj);
                return obj;
            case DYN_ANY_FACTORY:
                obj = m_orb.initDynAnyFactory();
                m_service_table.put(DYN_ANY_FACTORY_ID, obj);

                return obj;
            case CODEC_FACTORY:
                obj = m_orb.initCodecFactory();
                m_service_table.put(CODEC_FACTORY_ID, obj);
                return obj;
            case ORB_POLICY_MANAGER:
                obj = m_orb.initPolicyManager();
                m_service_table.put(ORB_POLICY_MANAGER_ID, obj);
                return obj;
            case POLICY_CURRENT:
                obj = m_orb.initPolicyCurrent();
                m_service_table.put(POLICY_CURRENT_ID, obj);
                return obj;
            case COMPRESSION_MANAGER:
                obj = m_orb.initCompressionManager();
                m_service_table.put(COMPRESSION_MANAGER_ID, obj);
                return obj;
            case RT_ORB:                               
            case INTERFACE_REPOSITORY:
            case NAME_SERVICE:
            case TRADING_SERVICE:
            case SECURITY_CURRENT:
            case TRANSACTION_CURRENT:
            case NOTIFICATION_SERVICE:
            case TYPED_NOTIFICATION_SERVICE:
            case PI_CURRENT:
                throw new org.omg.CORBA.BAD_OPERATION("Service not available");
            default:
                throw new org.omg.CORBA.INTERNAL();
        }
    }

    public synchronized void registerInitialReference(String object_name,
                                                      org.omg.CORBA.Object obj)
        throws org.omg.CORBA.ORBPackage.InvalidName
    {

        if (object_name == null)
            throw new BAD_PARAM("Null String reference",
                                0,
                                org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        if (obj == null)
            throw new BAD_PARAM("Null Object reference",
                                24,
                                org.omg.CORBA.CompletionStatus.COMPLETED_NO);

        if (m_corba_services_ids.containsKey(object_name))
            throw new org.omg.CORBA.ORBPackage.InvalidName(object_name);

        m_service_table.put(object_name, obj);
    }

    public synchronized String[] listInitialServices()
    {
        java.util.Vector list = new java.util.Vector();

        java.util.Enumeration e = m_service_table.keys();

        while (e.hasMoreElements()) {
            list.addElement(e.nextElement());
        }

        int ids_size = list.size();

        String[] ids = new String[ids_size];

        for (int i = 0; i < ids_size; i++)
            ids[i] = (String) list.elementAt(i);

        return ids;
    }

    protected synchronized void setService(String object_name,
                                           org.omg.CORBA.Object obj)
    {
        if (object_name == null)
            throw new BAD_PARAM( "Null String reference",
                                 0,
                                 org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        if (obj == null)
            throw new BAD_PARAM("Null Object reference",
                                24,
                                org.omg.CORBA.CompletionStatus.COMPLETED_NO);

        m_service_table.put(object_name, obj);
    }

    protected synchronized void removeInitialReference(String object_name)
        throws org.omg.CORBA.ORBPackage.InvalidName
    {
        if (object_name == null)
            throw new BAD_PARAM("Null String reference",
                                0,
                                org.omg.CORBA.CompletionStatus.COMPLETED_NO);

        if (m_service_table.containsKey(object_name))
            m_service_table.remove(object_name);
        else
            throw new org.omg.CORBA.ORBPackage.InvalidName(object_name);
    }
}
