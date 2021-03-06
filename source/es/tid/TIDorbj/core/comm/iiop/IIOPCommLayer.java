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

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.Object;

import es.tid.TIDorbj.core.ConfORB;
import es.tid.TIDorbj.core.ObjectKey;
import es.tid.TIDorbj.core.TIDORB;
import es.tid.TIDorbj.core.cdr.CDROutputStream;
import es.tid.TIDorbj.core.comm.ForwardRequest;
import es.tid.TIDorbj.core.comm.giop.GIOPVersion;
import es.tid.TIDorbj.core.comm.giop.BiDirServiceContext;
import es.tid.TIDorbj.core.comm.giop.ServiceContextList;
import es.tid.TIDorbj.core.comm.ssliop.SSLIOPCommLayer;
import es.tid.TIDorbj.core.comm.ssliop.SSLIOPCommunicationLayer;
import es.tid.TIDorbj.core.comm.ssliop.SSLIOPCommunicationLayerPropertiesInfo;
import es.tid.TIDorbj.core.iop.IOR;
import es.tid.TIDorbj.core.iop.TaggedComponent;
import es.tid.TIDorbj.core.iop.TaggedProfile;
import es.tid.TIDorbj.core.poa.POAKey;
import es.tid.TIDorbj.core.policy.PolicyContext;
import es.tid.TIDorbj.util.Trace;

/**
 * IIOP Communications layer. It will manage the request for a remote object
 * using the IIOP Protocol.
 * 
 * @autor Juan A. C&aacute;ceres
 * @version 1.0
 */

public class IIOPCommLayer extends es.tid.TIDorbj.core.comm.iiop.CommunicationLayer
{
    /**
     * The ORB has been destroyed.
     */

    public boolean m_destroyed;

    /**
     * Server Socket listening thread.
     */
    protected ServerListener m_server_listener;

    /**
     * Conection manager.
     */
    protected IIOPConnectionManager m_connection_manager;

    /**
     * Service context that contains the information for activating the
     * bidirectional service.
     */
    protected ServiceContextList m_bidirectional_service;

    protected int max_recover_count;
    protected int recover_time;
    protected boolean reliable_oneway;
    protected GIOPVersion giopVersion;
    
    //TODO: remove ORB's from here if possible, and get CommunicationLayer 
    public IIOPCommLayer(TIDORB orb)
    {
        // crear la conexion servidora y lanzar threads de escucha
        super(orb);
        m_destroyed = false;
        m_connection_manager = new IIOPConnectionManager(orb, this);
        m_bidirectional_service = null;
        m_server_listener = null;
        
        max_recover_count = orb.getCommunicationManager().getLayerById(
                IIOPCommunicationLayer.ID
            ).getPropertyInfo(
                IIOPCommunicationLayerPropertiesInfo.MAX_COMM_RECOVERING_TRIES
            ).getInt();
        
        recover_time = orb.getCommunicationManager().getLayerById(
                IIOPCommunicationLayer.ID
            ).getPropertyInfo(
                IIOPCommunicationLayerPropertiesInfo.COMM_RECOVERING_TIME
            ).getInt();
        
        reliable_oneway = orb.getCommunicationManager().getLayerById(
                IIOPCommunicationLayer.ID
            ).getPropertyInfo(
                IIOPCommunicationLayerPropertiesInfo.RELIABLE_ONEWAY
            ).getBoolean(); 

        giopVersion =
            GIOPVersion.fromString(
                m_orb.getCommunicationManager().getLayerById( IIOPCommunicationLayer.ID )
                .getPropertyInfo( IIOPCommunicationLayerPropertiesInfo.GIOP_VERSION )
                .getString()
            ); 

    }

    public IIOPCommLayer(TIDORB orb, SSLIOPCommLayer ssliop_layer)
    {
        // crear la conexion servidora y lanzar threads de escucha
        super(orb);
        m_destroyed = false;
        m_connection_manager = new IIOPConnectionManager(orb, ssliop_layer);
        m_bidirectional_service = null;
        m_server_listener = null;
        
        max_recover_count = orb.getCommunicationManager().getLayerById(
                SSLIOPCommunicationLayer.ID
            ).getPropertyInfo(
                SSLIOPCommunicationLayerPropertiesInfo.MAX_COMM_RECOVERING_TRIES
            ).getInt();
        
        recover_time = orb.getCommunicationManager().getLayerById(
                SSLIOPCommunicationLayer.ID
            ).getPropertyInfo(
                SSLIOPCommunicationLayerPropertiesInfo.COMM_RECOVERING_TIME
            ).getInt();
        
        reliable_oneway = orb.getCommunicationManager().getLayerById(
                SSLIOPCommunicationLayer.ID
            ).getPropertyInfo(
                SSLIOPCommunicationLayerPropertiesInfo.RELIABLE_ONEWAY
            ).getBoolean(); 

        giopVersion =
            GIOPVersion.fromString(
                m_orb.getCommunicationManager().getLayerById( IIOPCommunicationLayer.ID )
                .getPropertyInfo( IIOPCommunicationLayerPropertiesInfo.GIOP_VERSION )
                .getString()
            ); 

    }

    public boolean isLocal( IIOPIOR ior)
    {
        if (m_server_listener == null) // no object adaptor initialized
            return false;
        
        // Check matching between IOR ListenPoint and server_listener ListenPoints
        java.util.Vector listen_points = m_server_listener.getListenPoints();

        int i = 0;

        while (i < listen_points.size() ) {
            if ( ((ListenPoint)listen_points.elementAt(i)).equals(ior.profileIIOP()
                                                                   .getListenPoint()))
                return true;
            i++;
        }
        return false;
    }

    public boolean hasServerListener()
    {
        return (m_server_listener != null);
    }

    /**
     * Sends a request (with response) allocating an active connection with the
     * corresponding server referenced by its target address.
     * <p>
     * If necessary, tries to do a recovery loop.
     * 
     * @param request
     *            the CORBA request.
     */
    protected void sendRequest(es.tid.TIDorbj.core.RequestImpl request, IIOPIOR ior)
        throws ForwardRequest
    {

        PolicyContext policy_context = request.getPolicyContext();
        
       
        //  PolicyContext policy_context = request
        IIOPProfile profile = ior.profileIIOP();

        if (profile == null) {
            throw new org.omg.CORBA.INTERNAL( "Can not get IIOP Profile." );
        }

        IIOPConnection conn;
        int recover_count = max_recover_count;

        // recovering loop

        while (true) {

            try {
                conn = m_connection_manager.getClientConnection(
                	profile.getListenPoint(),
					policy_context
				);
            }
            catch (org.omg.CORBA.COMM_FAILURE ce) {

                recover_count--;

                if (recover_count <= 0) {
                    if (m_orb.m_trace != null) {
                        String[] msg =
                            {"Can not recover the communication any more: ",
                             ce.toString() };

                        m_orb.printTrace(Trace.DEBUG, msg);
                    }

                    throw ce;
                }

                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.DEBUG,
                                     "CORBA::COMM_FAILURE -> Communication "
                                     + "recovered, waiting "
                                     + recover_time
                                     + " milliseconds.");
                }

                try {
                    Thread.sleep( recover_time );
                }
                catch (InterruptedException e) {}

                continue;
            }

            try {
                conn.sendRequest(request, ior);
                return;
            }
            catch (RECOVERABLE_COMM_FAILURE rcf) {

                recover_count--;

                if (recover_count <= 0) {
                    if (m_orb.m_trace != null) {
                        String[] msg =
                        	{
                        	 "Can not recover the communication any more: ",
                        	 rcf.m_comm_failure.toString()
                        	 };

                        m_orb.printTrace(Trace.DEBUG, msg);
                    }

                    throw rcf.m_comm_failure;
                }

                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.DEBUG,
                                     "CORBA::COMM_FAILURE -> Communication " 
                                     + "recovered, waiting "
                                     + recover_time
                                     + " milliseconds.");
                }	

                try {
                    Thread.sleep( recover_time );
                }
                catch (InterruptedException e) {}

            }
            catch (RuntimeException re) {
                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.ERROR,
                                     "Exception in remote invocation", re);
                }
                throw re;
            }
        }
    }

    /**
     * Sends a request using the IIOP protocol.
     * 
     * @param request
     *            the CORBA request.
     */
    public void request(es.tid.TIDorbj.core.RequestImpl request,
                        IIOPIOR ior)
        throws ForwardRequest
    {
        request.withResponse(true);
        sendRequest(request, ior);
    }

    /**
     * Sends a oneway request using the IIOP protocol allocating an active
     * IIOPConnection with the server referenced in the request target address.
     * 
     * @param request
     *            the CORBA request.
     */
    public void onewayRequest(es.tid.TIDorbj.core.RequestImpl request,
                              IIOPIOR ior)
    {
        if ( reliable_oneway ) {
            try {
                ReliableOnewayThread th = 
                    new ReliableOnewayThread(this, request, ior);
                th.start();
                return;
            }
            catch (Throwable thw) {
                throw new NO_RESOURCES("Can't create thread: "
                                       + thw.toString());
            }
        } else {

            try {

                request.withResponse(false);

                IIOPProfile profile = ior.profileIIOP();

                if (profile == null)
                    throw new INTERNAL("Can not get IIOP Profile.");

                IIOPConnection conn;

                conn = 
                    m_connection_manager
                    .getClientConnection(profile.getListenPoint(),
                                         request.getPolicyContext());

                conn.sendOnewayRequestAsync(request, ior);

            }
            catch (Throwable th) {
                if (m_orb.m_trace != null)
                    m_orb.printTrace(Trace.DEBUG,
                                     "Exception in oneway remote invocation",
                                     th);
            }
        }
    }

    /**
     * Sends a asynchronous request using the IIOP protocol allocating an active 
     * connection with the corresponding server referenced by its target address.
     * <p>
     * If necessary, tries to do a recovery loop.
     * 
     * @param request
     *            the CORBA request.
     */
    public void asyncRequest(es.tid.TIDorbj.core.RequestImpl request, IIOPIOR ior) //*opc1*, Object ami_handler)
        throws ForwardRequest
    {

        request.withResponse(true);
        
        PolicyContext policy_context = request.getPolicyContext();
        
        //  PolicyContext policy_context = request
        IIOPProfile profile = ior.profileIIOP();

        if (profile == null) {
            throw new org.omg.CORBA.INTERNAL( "Can not get IIOP Profile." );
        }

        IIOPConnection conn;
        int recover_count = max_recover_count;

        // recovering loop

        while (true) {

            try {
                conn = m_connection_manager.getClientConnection(
                	profile.getListenPoint(),
					policy_context
				);
            }
            catch (org.omg.CORBA.COMM_FAILURE ce) {

                recover_count--;

                if (recover_count <= 0) {
                    if (m_orb.m_trace != null) {
                        String[] msg =
                            {"Can not recover the communication any more: ",
                             ce.toString() };

                        m_orb.printTrace(Trace.DEBUG, msg);
                    }

                    throw ce;
                }

                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.DEBUG,
                                     "CORBA::COMM_FAILURE -> Communication "
                                     + "recovered, waiting "
                                     + recover_time
                                     + " milliseconds.");
                }

                try {
                    Thread.sleep( recover_time );
                }
                catch (InterruptedException e) {}

                continue;
            }

            try {
                conn.sendAsyncRequest(request, ior); //*opc1*, ami_handler);
                return;
            }
            catch (RECOVERABLE_COMM_FAILURE rcf) {

                recover_count--;

                if (recover_count <= 0) {
                    if (m_orb.m_trace != null) {
                        String[] msg =
                        	{
                        	 "Can not recover the communication any more: ",
                        	 rcf.m_comm_failure.toString()
                        	 };

                        m_orb.printTrace(Trace.DEBUG, msg);
                    }

                    throw rcf.m_comm_failure;
                }

                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.DEBUG,
                                     "CORBA::COMM_FAILURE -> Communication " 
                                     + "recovered, waiting "
                                     + recover_time
                                     + " milliseconds.");
                }	

                try {
                    Thread.sleep( recover_time );
                }
                catch (InterruptedException e) {}

            }
            catch (RuntimeException re) {
                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.ERROR,
                                     "Exception in remote invocation", re);
                }
                throw re;
            }
        }
    }

    
    /**
     * Sends a oneway request using the IIOP protocol allocating an active
     * IIOPConnection with the server referenced in the request target address.
     * 
     * @param request
     *            the CORBA request.
     */
    public void reliableOnewayRun(
            es.tid.TIDorbj.core.RequestImpl request,
            IIOPIOR ior ) {
        try {

            PolicyContext policy_context =
                request.getPolicyContext();

            request.reliableOneway(true);

            request.withResponse(false);

            IIOPProfile profile = ior.profileIIOP();

            if (profile == null)
                throw new org.omg.CORBA.INTERNAL("Can not get IIOP Profile.");

            IIOPConnection conn;
            int recover_count = max_recover_count;

            // recovering loop
        
            while (true) {

                try {

                    conn =
                        m_connection_manager
                        .getClientConnection(profile.getListenPoint(),
                                             policy_context);

                    conn.sendOnewayRequestSync(request, ior);

                    return;
                }
                catch (RECOVERABLE_COMM_FAILURE rcf) {

                    recover_count--;

                    if (recover_count <= 0)
                        throw rcf.m_comm_failure;

                    if (m_orb.m_trace != null) {
                        m_orb.printTrace(Trace.DEBUG,
                                         "CORBA::COMM_FAILURE -> Communication"
                                         +" recovered, waiting "
                                         + this.recover_time
                                         + " milliseconds.");
                    }	

                    try {
                        Thread.sleep( this.recover_time );
                    }
                    catch (InterruptedException e) {}

                }
                catch (ForwardRequest fr) {
                    
                    recover_count--;

                    if (m_orb.m_trace != null) {
                        String[] msg = { "Communication forwarded: ",
                                        fr.forward_reference.toString() };
                        m_orb.printTrace(Trace.DEBUG, msg);
                    }
                }
            }
        }
        catch (Throwable e) {
            if (m_orb.m_trace != null)
                m_orb.printTrace(
                    Trace.DEBUG,
                    "Exception in reliable remote oneway invocation :",
                    e);
        }
    }

    /**
     * Sends a object existence request.
     * 
     * @param ior
     *            the object IOR.
     */
    public boolean objectExists(IIOPIOR ior,
                                PolicyContext policy_context)
        throws ForwardRequest
    {

        IIOPProfile profile = ior.profileIIOP();

        if (profile == null)
            throw new org.omg.CORBA.INTERNAL("Can not get IIOP Profile.");

        IIOPConnection conn;
        int recover_count = max_recover_count;

        // recovering loop

        while (true) {

            try {
                conn = 
                    m_connection_manager.getClientConnection(
                        profile.getListenPoint(),
                        policy_context);
            }
            catch (org.omg.CORBA.COMM_FAILURE ce) {

                recover_count--;

                if (recover_count <= 0) {
                    if (m_orb.m_trace != null) {
                        String[] msg = 
                        	{
                        	 "Can not recover the communication any more: ",
                        	 ce.toString()
                        	};

                        m_orb.printTrace(Trace.DEBUG, msg);
                    }

                    throw ce;
                }

                if (m_orb.m_trace != null) {
                    m_orb.printTrace(
                                     Trace.DEBUG,
                                     "CORBA::COMM_FAILURE -> Communication " 
                                     + "recovered, waiting "
                                     + this.recover_time
                                     + " milliseconds.");
                }

                try {
                    Thread.sleep( this.recover_time );
                }
                catch (InterruptedException e) {}

                continue;
            }

            try {

                return conn.sendLocateRequest(ior, policy_context);

            }
            catch (RECOVERABLE_COMM_FAILURE rcf) {

                recover_count--;

                if (recover_count <= 0) {
                    if (m_orb.m_trace != null)
                        m_orb.printTrace(
                            Trace.DEBUG,
                            "Can not recover the communication any more: ",
                            rcf.m_comm_failure);

                    throw rcf.m_comm_failure;
                }

                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.DEBUG,
                                     "CORBA::COMM_FAILURE -> Communication " 
                                     + "recovered, waiting "
                                     + this.recover_time
                                     + " milliseconds.");
                }

                try {
                    Thread.sleep( this.recover_time );
                }
                catch (InterruptedException e) {}

            }
            catch (ForwardRequest fr) {
                throw fr;
            }
            catch (RuntimeException re) {
                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.ERROR,
                                     "Exception in remote invocation", re);
                }
                throw re;
            }
        }
    }

    public void prepareRequest(es.tid.TIDorbj.core.StreamRequestImpl request)
    {
        org.omg.CORBA.portable.ObjectImpl obj = request.getTarget();
        es.tid.TIDorbj.core.comm.CommunicationDelegate delegate =
            (es.tid.TIDorbj.core.comm.CommunicationDelegate) obj
                                                                                          ._get_delegate();
        es.tid.TIDorbj.core.iop.IOR ior = delegate.getReference();

        IIOPIOR iiopIOR;
        if ( ior instanceof IIOPIOR ){
            iiopIOR = ( IIOPIOR )ior;
        } else {
            throw new org.omg.CORBA.INTERNAL("Not an IIOP IOR.");
        }
        
        PolicyContext policy_context =
            delegate.createRequestPolicyContext();
        
        IIOPProfile profile;
        profile = iiopIOR.profileIIOP();

        if (profile == null) {
            throw new org.omg.CORBA.INTERNAL("Can not get IIOP Profile.");
        }

        IIOPConnection conn = null;
        int recover_count = max_recover_count;

        while (conn == null) {

            try {
                conn = 
                    m_connection_manager.getClientConnection(
                        profile.getListenPoint(),
                        policy_context);

            }
            catch (org.omg.CORBA.COMM_FAILURE ce) {

                recover_count--;

                if (recover_count <= 0) {
                    if (m_orb.m_trace != null) {
                        String[] msg =
                        	{
                        	 "Can not recover the communication any more: ",
                             ce.toString()
                            };

                        m_orb.printTrace(Trace.DEBUG, msg);
                    }

                    throw ce;
                }

                if (m_orb.m_trace != null) {
                    m_orb.printTrace(
                                     Trace.DEBUG,
                                     "CORBA::COMM_FAILURE -> Communication " 
                                     + "recovered, waiting "
                                     + this.recover_time
                                     + " milliseconds.");
                }

                try {
                    Thread.sleep( this.recover_time );
                }
                catch (InterruptedException e) {}

                continue;
            }
        }

        conn.prepareRequest(request, iiopIOR);

    }

    public org.omg.CORBA.portable.InputStream request(
            IIOPIOR ior,
            CDROutputStream stream,
            PolicyContext policy_context
        ) throws ForwardRequest,
                 org.omg.CORBA.portable.ApplicationException,
                 org.omg.CORBA.portable.RemarshalException {

        IIOPProfile profile = ior.profileIIOP();

        if (profile == null)
            throw new org.omg.CORBA.INTERNAL("Can not get IIOP Profile.");

        IIOPConnection conn = null;
        int recover_count = max_recover_count;

        while (true) {
            try {

                conn = 
                    m_connection_manager.getClientConnection(
                        profile.getListenPoint(),
                        policy_context);

            }
            catch (org.omg.CORBA.COMM_FAILURE ce) {

                recover_count--;

                if (recover_count <= 0) {
                    if (m_orb.m_trace != null) {
                        String[] msg = {
                        	 "Can not recover the communication any more: ",
                        	 ce.toString()
                        	};

                        m_orb.printTrace(Trace.DEBUG, msg);
                    }

                    throw ce;
                }

                if (m_orb.m_trace != null) {
                    m_orb.printTrace(
                       Trace.DEBUG,
                       "CORBA::COMM_FAILURE->Communication recovered, waiting "
                       + this.recover_time
                       + " milliseconds.");
                }

                try {
                    Thread.sleep( this.recover_time );
                }
                catch (InterruptedException e) {}

                continue;
            }

            try {

                return conn.sendRequest(ior, stream, policy_context);

            }
            catch (RECOVERABLE_COMM_FAILURE rcf) {

                recover_count--;

                if (recover_count <= 0) {
                    if (m_orb.m_trace != null)
                        m_orb.printTrace(
                           Trace.DEBUG,
                           "Can not recover the communication any more: ",
                           rcf.m_comm_failure);

                    throw rcf.m_comm_failure;
                }

                if (m_orb.m_trace != null) {
                    m_orb.printTrace(
                        Trace.DEBUG,
                        "CORBA::COMM_FAILURE->Communication recovered, waiting "
                        + this.recover_time
                        + " milliseconds.");
                }

                try {
                    Thread.sleep( this.recover_time );
                }
                catch (InterruptedException e) {}

            }
            catch (ForwardRequest fr) {
                throw fr;
            }
            catch (RuntimeException re) {
                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.ERROR,
                                     "Exception in remote invocation", re);
                }
                throw re;
            }
        }
    }

    /**
     * ORB Server IIOPConnection part shutdown.
     */
    public synchronized void shutdown()
    {
        try {
            if (m_server_listener != null) {
                if (m_orb.m_trace != null) {
                    m_orb.printTrace(Trace.DEBUG,
                                     "Shutdown IIOPCommLayer .ServerListener");
                }
                m_server_listener.shutdown();
                m_server_listener = null;
            }
        }
        catch (Throwable e) {}
    }

    /**
     * IIOP Layer close.
     */
    public synchronized void destroy()
    {
        if (!m_destroyed) {

            if (m_orb.m_trace != null) {
                m_orb.printTrace(Trace.DEBUG, "Destroying IIOPCommLayer");
            }

            try {
                if (m_server_listener != null) {
                    m_server_listener.shutdown();
                    m_server_listener = null;
                }
            }
            catch (Throwable e) {}

            try {
                if (m_connection_manager != null) {
                    m_connection_manager.destroy();
                    m_connection_manager = null;
                }
            }
            catch (Throwable e) {}

            m_bidirectional_service = null;

            m_destroyed = true;
        }
    }

    public synchronized void initServerListener()
    {
        if (m_server_listener == null) {
            m_server_listener = new ServerListener(m_connection_manager);
            m_server_listener.setDaemon(false);
            try {

                m_server_listener.start();

            }
            catch (Throwable thw) {
                m_server_listener = null;
                throw new org.omg.CORBA.NO_RESOURCES("Can't create thread: "
                                                     + thw.toString());
            }
        }
    }

    public synchronized IOR createIOR(String id,
                                      POAKey key, 
                                      TaggedComponent[] extraComponents)
    {
        if (m_server_listener == null)
            throw new org.omg.CORBA.INTERNAL("ServerListener not initialized");

        // return m_server_listener.createIOR(id, key, components);

        TaggedProfile[] profiles = new TaggedProfile[1];

        TaggedComponent[] components = null;
        
        if(extraComponents != null) {
            components = new TaggedComponent[extraComponents.length + 1];
            System.arraycopy(extraComponents, 0, components, 0, extraComponents.length);
            components[extraComponents.length] = ConfORB.ORB_TYPE;
        } else {
            components = new TaggedComponent[1];
            components[0] = ConfORB.ORB_TYPE;
        }
        
        java.util.Vector listen_points = m_server_listener.getListenPoints();

        int i = 0;
        while (i < listen_points.size() ) {
            profiles[i] = new IIOPProfile(
        	this.giopVersion, 
                (ListenPoint)listen_points.elementAt(i),
                key,
                components
		);
            i++;
        }
        return new IIOPIOR(id, profiles);


    }
    
    public synchronized IOR createIOR(String id, 
                                      ObjectKey key, 
                                      TaggedComponent[] extraComponents)
    {
        if (m_server_listener == null)
            throw new org.omg.CORBA.INTERNAL("ServerListener not initialized");

        // return m_server_listener.createIOR(id, key, components);




        TaggedComponent[] components = null;
        
        if(extraComponents != null) {
            components = new TaggedComponent[extraComponents.length + 1];
            System.arraycopy(extraComponents, 0, components, 0, extraComponents.length);
            components[extraComponents.length] = ConfORB.ORB_TYPE;
        } else {
            components = new TaggedComponent[1];
            components[0] = ConfORB.ORB_TYPE;
        }

        java.util.Vector listen_points = m_server_listener.getListenPoints();
        TaggedProfile[] profiles = new TaggedProfile[listen_points.size()];
        int i = 0;

        while (i < listen_points.size() ) {
            profiles[i] = new IIOPProfile(
                 this.giopVersion, 
                 //m_server_listener.getListenPoint(),
                 (ListenPoint)listen_points.elementAt(i),
                 key,
                 components
		);
            i++;
        }
        return new IIOPIOR(id, profiles);

    }
    

    public synchronized ServiceContextList getBidirectionalService()
    {
        if (m_destroyed)
            throw new org.omg.CORBA.BAD_INV_ORDER("ORB is destroying");

        if (m_bidirectional_service == null) {
            if (m_server_listener == null)
                throw new INTERNAL("Trying to create a bidirectional context " 
                                   + "without ListenPoint");

            java.util.Vector listen_points = m_server_listener.getListenPoints();

            BiDirServiceContext bidir_context = new BiDirServiceContext(listen_points.size());

            for (int i = 0; i < listen_points.size(); i++) {
              bidir_context.m_listen_points[i] = 
                  (ListenPoint)listen_points.elementAt(i);
            }

            m_bidirectional_service = new ServiceContextList(1);
            m_bidirectional_service.m_components[0] = bidir_context;

        }

        return m_bidirectional_service;
    }
}
