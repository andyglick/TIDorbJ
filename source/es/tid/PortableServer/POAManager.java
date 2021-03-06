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
package es.tid.PortableServer;

/**
 * TIDorbj POAManager extended interface.
 * 
 * @autor Juan A. C&aacute;ceres
 * @version 1.0
 */
public interface POAManager
    extends org.omg.PortableServer.POAManager
{

    /**
     * Sets the mininum number of execution threads
     */
    void set_min_threads(int min_threads);

    /**
     * Sets the maximun number of execution threads
     */
    void set_max_threads(int max_threads);

    /**
     * Sets the maximun number of request queued in the POAManager
     */
    void set_max_queued_requests(int max_queued_requests);

    /**
     * Sets the maximun time an execution thread is inactive.
     */
    void set_starving_time(int millisecs);
    
    /**
     * Sets the queue order, see Messaging module in CORBA specification     
     */
    
    void set_queue_order(short order);

    /**
     * @return maximun number of request queued in the POAManager
     */
    int get_max_queued_requests();

    /**
     * @return the maximun number of execution threads
     */
    int get_max_threads();

    /**
     * @return the mininum number of execution threads
     */
    int get_min_threads();

    /**
     * @return the maximun time an execution thread is inactive.
     */
    int get_starving_time();

    /**
     * @return the state of the POA manager.
     */
    org.omg.PortableServer.POAManagerPackage.State get_state();
    
    
    /**
     * 
     * @return the queue order used defined as vaulue of the Messaging policy
     *    
     */
    
    short get_queue_order();
    
}