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

import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.net.InetAddress;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.NO_RESPONSE;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.Object;
import org.omg.CORBA.Request;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.Messaging.ReplyHandler;
import org.omg.Messaging.ReplyHandlerHelper;
import org.omg.Messaging._ReplyHandlerStub;
import org.omg.Compression.COMPRESSORID_NONE;
import org.omg.Compression.CompressorIdLevel;
import org.omg.Compression.COMPRESSORID_ZLIB;

import es.tid.TIDorbj.core.AnyImpl;
import es.tid.TIDorbj.core.NVListImpl;
import es.tid.TIDorbj.core.NamedValueImpl;
import es.tid.TIDorbj.core.ORBComponent;
import es.tid.TIDorbj.core.ObjectImpl;
import es.tid.TIDorbj.core.ObjectKey;
import es.tid.TIDorbj.core.RequestImpl;
import es.tid.TIDorbj.core.ServerRequestImpl;
import es.tid.TIDorbj.core.StreamRequestImpl;
import es.tid.TIDorbj.core.cdr.BufferCDR;
import es.tid.TIDorbj.core.cdr.CDRInputStream;
import es.tid.TIDorbj.core.cdr.ChunkCDR;
import es.tid.TIDorbj.core.comm.Connection;
import es.tid.TIDorbj.core.comm.CommunicationManager;
import es.tid.TIDorbj.core.comm.ForwardRequest;
import es.tid.TIDorbj.core.comm.giop.AddressingDisposition;
import es.tid.TIDorbj.core.comm.giop.BiDirServiceContext;
import es.tid.TIDorbj.core.comm.giop.GIOPCancelRequestMessage;
import es.tid.TIDorbj.core.comm.giop.GIOPFragmentMessage;
import es.tid.TIDorbj.core.comm.giop.GIOPFragmentedMessage;
import es.tid.TIDorbj.core.comm.giop.GIOPHeader;
import es.tid.TIDorbj.core.comm.giop.GIOPLocateReplyMessage;
import es.tid.TIDorbj.core.comm.giop.GIOPLocateRequestMessage;
import es.tid.TIDorbj.core.comm.giop.GIOPMessage;
import es.tid.TIDorbj.core.comm.giop.GIOPReplyMessage;
import es.tid.TIDorbj.core.comm.giop.GIOPRequestMessage;
import es.tid.TIDorbj.core.comm.giop.GIOPVersion;
import es.tid.TIDorbj.core.comm.giop.InvocationPoliciesContext;
import es.tid.TIDorbj.core.comm.giop.LocateReplyStatusType;
import es.tid.TIDorbj.core.comm.giop.MessageFactory;
import es.tid.TIDorbj.core.comm.giop.MsgType;
import es.tid.TIDorbj.core.comm.giop.ReplyStatusType;
import es.tid.TIDorbj.core.comm.giop.RequestId;
import es.tid.TIDorbj.core.comm.giop.ServiceContextList;
import es.tid.TIDorbj.core.comm.giop.ZIOPMessage;

import es.tid.TIDorbj.core.comm.ssliop.SSLIOPCommunicationLayer;
import es.tid.TIDorbj.core.comm.ssliop.SSLIOPCommunicationLayerPropertiesInfo;

import es.tid.TIDorbj.core.ziop.ZIOP;

import es.tid.TIDorbj.core.iop.IOR;
import es.tid.TIDorbj.core.messaging.AMILock;
import es.tid.TIDorbj.core.messaging.AMILockEnumeration;
import es.tid.TIDorbj.core.messaging.AMILockList;
import es.tid.TIDorbj.core.messaging.QoS;
import es.tid.TIDorbj.core.policy.PolicyContext;
import es.tid.TIDorbj.core.util.Counter;
import es.tid.TIDorbj.core.util.FullUseTableException;
import es.tid.TIDorbj.core.util.RemovableObject;
import es.tid.TIDorbj.core.util.UseTable;
import es.tid.TIDorbj.util.Trace;

/**
 * TIDorb IIOP IIOPConnection: sends and receives IIOP Messages under the GIOP
 * protocol.
 * 
 * <p>
 * Copyright 2000 Telef&oacute;nica I+D. Printed in Spain (Europe). All Rights
 * Reserved.
 * 
 * @autor Juan A. C&aacute;ceres
 * @version 1.0
 */

public class IIOPConnection extends Connection {

    /**
     * IIOPConnection Socket.
     */
    java.net.Socket m_socket;

    /**
     * IIOPConnection Socket InputStream.
     */
    public java.io.InputStream m_socket_in;

    /**
     * IIOPConnection Socket OutputStream.
     */
    public java.io.OutputStream m_socket_out;


    private boolean tcpNoDelay;
    private int     soTimeout;
    private int     soLinger;
    private int     socketWriteTimeout;


    private IIOPConnection(IIOPConnectionManager mngr, java.net.Socket socket)
    {
        super(mngr);

        m_manager = mngr;


        m_addresses = new Hashtable();
        m_bidirectional_service = null;
        m_send_bidirectional_service = false;

        tcpNoDelay = m_orb.getCommunicationManager().getLayerById( IIOPCommunicationLayer.ID )
            .getPropertyInfo( IIOPCommunicationLayerPropertiesInfo.TCP_NODELAY )
            .getBoolean();
        
        soTimeout = m_orb.getCommunicationManager().getLayerById( IIOPCommunicationLayer.ID )
            .getPropertyInfo( IIOPCommunicationLayerPropertiesInfo.SOCKET_TIMEOUT )
            .getInt();

        soLinger = m_orb.getCommunicationManager().getLayerById( IIOPCommunicationLayer.ID )
            .getPropertyInfo( IIOPCommunicationLayerPropertiesInfo.SOCKET_LINGER )
            .getInt();
        
        socketWriteTimeout = 
            m_orb.getCommunicationManager().getLayerById( IIOPCommunicationLayer.ID )
            .getPropertyInfo( IIOPCommunicationLayerPropertiesInfo.SOCKET_WRITE_TIMEOUT )
            .getInt(); 
        
        try {
            this.m_socket = socket;

            my_port = socket.getLocalPort();

            socket.setTcpNoDelay( tcpNoDelay );
            socket.setSoTimeout( soTimeout );
            
            socket.setSoLinger( soLinger != -1, soLinger );
            
            m_socket_in = socket.getInputStream();
            m_socket_out = socket.getOutputStream();
        }
        catch (java.io.IOException ioe) {
            throw new OBJECT_NOT_EXIST(ioe.toString(),
                                       0,
                                       CompletionStatus.COMPLETED_NO);
        }

        m_write_monitor = new ConnectionWriteMonitor( socketWriteTimeout );
    }

    /**
     * IIOPConnection hash code.
     */
    public int hashCode() {
        // return my_port;
        return m_socket.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof IIOPConnection &&
                ( ( IIOPConnection )obj ).my_port == my_port;
    }

    public String toString() {
        if (m_str == null) {
            if (m_mode == CLIENT_MODE) {
                m_str = "Client IIOPConnection at " + m_socket.toString();
            } else if (m_mode == SERVER_MODE) {
                m_str = "Server IIOPConnection at " + m_socket.toString();
            } else {
                m_str = "Bidirectional IIOPConnection at " + m_socket.toString();
            }
        }
        return m_str;
    }

    /**
     * Creates a new connection in SERVER_MODE.
     * 
     * @param mngr
     *            the manager
     * @param sock
     *            <code>Socket</code> created in the <code>accept</code>
     *            method in a <code>ServerSocket</code> object.
     */
    public static IIOPConnection serverConnection(IIOPConnectionManager mngr,
                                              java.net.Socket sock) {
        IIOPConnection conn = new IIOPConnection(mngr, sock);
        conn.m_mode = SERVER_MODE;
        conn.init();

        if (conn.m_orb.m_trace != null) {
            conn.m_orb.printTrace(Trace.DEBUG, "Creating " + conn.toString());
        }

        return conn;
    }

    /**
     * Creates a new connection in CLIENT_MODE.
     */

    public static IIOPConnection clientConnection(IIOPConnectionManager mngr,
                                              ListenPoint listen_point)
    {
        try {

            // create the socket
            Socket socket =
                new Socket(listen_point.m_host, listen_point.m_port);
            IIOPConnection conn = new IIOPConnection(mngr, socket);

            conn.m_mode = CLIENT_MODE;

            conn.m_initial_point = listen_point;

            conn.addListenPoint(listen_point);

            conn.init();

            return conn;

        }
        catch (java.io.IOException ioe) {
            throw new TRANSIENT(ioe.toString(), 0,
                                   CompletionStatus.COMPLETED_NO);
        }
        catch (java.lang.SecurityException sce) {
            throw new NO_PERMISSION(sce.toString(), 0,
                                    CompletionStatus.COMPLETED_NO);
        }
    }

    protected void close()
    {
        try {
            m_socket.close();
        }
        catch (IOException ioe) {}

        if (m_orb.m_trace != null)
            m_orb.printTrace(Trace.DEEP_DEBUG,
                             toString()
                             + " client socket closed");

        // notify the writers blocked in write_monitor

        m_write_monitor.setException(m_state.getError());

        // notify the locks

        LockEnumeration enumer = m_lock_list.elements();
        Lock lock;

        while (enumer.hasMoreElements()) {
            lock = enumer.next();
            synchronized (lock) {
                lock.cancelWaiting();
            }
        }

        AMILockEnumeration ami_enum = m_ami_lock_list.elements();
        AMILock ami_lock;

        while (ami_enum.hasMoreElements()) {
            ami_lock = ami_enum.next();
            synchronized (ami_lock) {
                ami_lock.notify();
            }
        }

        if (m_addresses != null) {
            m_addresses.clear();
        }

        // clear buffers

        Enumeration msgs = m_uncompleted_messages.elements();

        while (msgs.hasMoreElements()) {
            m_uncompleted_messages.remove(msgs.nextElement());
        }

    }

    protected void dispatchRequest(GIOPLocateRequestMessage message) {
        
        ObjectKey obj_key; 
        obj_key = message.extractAddress().getObjectKey();

        IIOPLocateResponseHandler responseHandler;
        responseHandler = new IIOPLocateResponseHandler( 
             this,
             message.getHeader().getVersion(),
             message.getRequestId(),
             obj_key
        );
        
        responseHandler.setSerial(m_orb.getRequestCounter().next());
        
        m_requests_in_POA.inc();
        CommunicationManager.getInstance( this.m_orb )
            .getRequestDispatcher().dispatchRequest( responseHandler );
        
    }
    
    protected void dispatchRequest(GIOPRequestMessage message) {

        ServerRequestImpl serverRequest; 
        serverRequest = message.extractRequest(m_orb);
        
        
        String ssl_key_store_file = new String("");
        String ssl_key_store_passwd = new String("");

        if (m_orb.getCommunicationManager().getLayerById( SSLIOPCommunicationLayer.ID )
            != null) {
        ssl_key_store_file =
            m_orb.getCommunicationManager().getLayerById( SSLIOPCommunicationLayer.ID )
                .getPropertyInfo( SSLIOPCommunicationLayerPropertiesInfo.ssl_key_store_file_name )
                .getValue();

        ssl_key_store_passwd =
            m_orb.getCommunicationManager().getLayerById( SSLIOPCommunicationLayer.ID )
                .getPropertyInfo( SSLIOPCommunicationLayerPropertiesInfo.ssl_key_store_passwd_name )
                .getValue();
        }

        // Check SSL: all TCP request are not dispached
        if ( (!ssl_key_store_file.equals("")) &&
             (!ssl_key_store_passwd.equals("")) ) {
            serverRequest.setSystemException(
                  new NO_PERMISSION("SSL target can not dispatch TCP requests ") );
            sendReply(serverRequest, null);
            //delete request;
            return;
        }


        ObjectKey objectKey; 
        objectKey = serverRequest.getTarget().getObjectKey();

        IIOPResponseHandler responseHandler;
        responseHandler = new IIOPResponseHandler(
            this, 
            message.getHeader().getVersion(),
            serverRequest, 
            objectKey,
            message.getRequestInvocationPolicies()
           
        );
        
        responseHandler.setSerial(m_orb.getRequestCounter().next());

        /*
         * TODO: ask juan: in previous code, when reference has been located,
         * if it's not local just returns... which should be the
         * refactoring mapping since the POAKey is resolved outside,
         * under ExecThread, and there's "no way?" to acces the object
         * reference to check if either it's local or not
         *   Previous code 
             if (!obj_impl._is_local()) {
                // oneway request to a non local object: DISCARD
                return;
             }
         * */

        if (serverRequest.withResponse()) {
            m_requests_in_POA.inc();
        }
        CommunicationManager.getInstance( this.m_orb )
            .getRequestDispatcher().dispatchRequest( responseHandler );
    }
  

    public void read(byte[] buffer, int offset, int length)
    {

        try {
            int numReadNow = 0;

            while (length > 0) {

                numReadNow = m_socket_in.read(buffer, offset, length);

                if (numReadNow < 0) {
                    throw new COMM_FAILURE("Broken IIOPConnection", 0,
                                           CompletionStatus.COMPLETED_NO);
                }

                offset += numReadNow;
                length -= numReadNow;

            }

        }
        catch (IOException ioe) {
            COMM_FAILURE connection_error = 
                new COMM_FAILURE(ioe.toString(),
                                 0,
                                 CompletionStatus.COMPLETED_NO);

            closeByBrokenConnection(connection_error);

            throw connection_error;
        }
    }

    protected void write(byte[] buffer)
    {
        try {

            m_socket_out.write(buffer, 0, buffer.length);
            m_socket_out.flush();
        }
        catch (IOException ioe) {
            COMM_FAILURE connection_error =
                new COMM_FAILURE(ioe.toString(),
                                 0,
                                 CompletionStatus.COMPLETED_NO);
            closeByBrokenConnection(connection_error);

            throw connection_error;
        }
    }

    protected void write(byte[] buffer, int offset, int length)
    {
        try {
            m_socket_out.write(buffer, offset, length);
            m_socket_out.flush();
        }
        catch (IOException ioe) {
            COMM_FAILURE connection_error = 
                new COMM_FAILURE(ioe.toString(),
                                 0,
                                 CompletionStatus.COMPLETED_NO);
            closeByBrokenConnection(connection_error);

            throw connection_error;
        }
    }


}
