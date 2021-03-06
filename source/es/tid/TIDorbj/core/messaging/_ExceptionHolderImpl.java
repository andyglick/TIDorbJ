/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDorbJ
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 453 $
* Date: $Date: 2010-04-27 16:52:41 +0200 (Tue, 27 Apr 2010) $
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

package es.tid.TIDorbj.core.messaging;

import java.io.Serializable;
import java.util.Properties;

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.Messaging._ExceptionHolder;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.*;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.UnknownUserException;
import org.omg.CORBA.UserException;

	
public class _ExceptionHolderImpl extends _ExceptionHolder 

{

    public _ExceptionHolderImpl() {
        is_system_exception = false;
        byte_order = false;
        marshaled_exception = new byte[0];
    }       
    
    public void read(es.tid.TIDorbj.core.cdr.CDRInputStream is) {

        is.fixStarting();

        // parsear el nombre de la excepcion	
        String name = is.read_string();
        is.rewind();

        // TODO: Leer el typecode
        org.omg.CORBA.StructMember[] members = new org.omg.CORBA.StructMember[0];
        org.omg.CORBA.TypeCode _type = 
            is.orb().create_exception_tc(name, "UserException", members);
        
        org.omg.CORBA.Any any = is.orb().create_any();
        any.read_value(is, _type);
	
        byte_order = is.getByteOrder();
	
        
        try {
            // Obtiene codec factory
            String[] args = { "", "" };
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass","es.tid.TIDorbj.core.TIDORB");

            org.omg.CORBA.Object obj = 
                org.omg.CORBA.ORB.init(args, props).resolve_initial_references("CodecFactory");
            CodecFactory codec_factory = CodecFactoryHelper.narrow(obj);
            
            // Crea codec
            Encoding encoding = new Encoding();
            encoding.format = ENCODING_CDR_ENCAPS.value;
            encoding.major_version = 1;
            encoding.minor_version = 2;
            Codec codec = codec_factory.create_codec(encoding);
            
            marshaled_exception = codec.encode(any);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
 
    public void raise_exception()
        throws org.omg.CORBA.UserException
    {
        org.omg.CORBA.Any any = null;
        try {

            // Obtiene codec factory
            String[] args = { "", "" };
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass","es.tid.TIDorbj.core.TIDORB");

            org.omg.CORBA.Object obj = 
                org.omg.CORBA.ORB.init(args,props).resolve_initial_references("CodecFactory");
            CodecFactory codec_factory = CodecFactoryHelper.narrow(obj);
            
            // Crea codec
            Encoding encoding = new Encoding();
            encoding.format = ENCODING_CDR_ENCAPS.value;
            encoding.major_version = 1;
            encoding.minor_version = 2;
            Codec codec = codec_factory.create_codec(encoding);

            any = codec.decode(marshaled_exception);

        } catch (FormatMismatch e) {
            e.printStackTrace();
            throw new MARSHAL();
        } catch (Exception e) {
            throw new MARSHAL();
        }

        
        // TODO: how throw UserException contained in Any??
        throw new UnknownUserException(any);
    }


    public void raise_exception_with_list(org.omg.CORBA.TypeCode[] exc_list)
        throws org.omg.CORBA.UserException
    {
        throw new NO_IMPLEMENT();
    }

   

}
