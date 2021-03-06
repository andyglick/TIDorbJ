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

package es.tid.TIDorbj.core.comm;

/**
 * <p>CommunicationLayer properties for making a connection. The
 * <code>PropertyInfo</code> could be used by automatic code generation ides to
 * interact with <code>CommunicationManager</code> and retrieve each registered
 * layer's available properties.
 * 
 * @author Juan Pablo Rojas
 */

public class PropertyInfo {

    /**
     * Constructs a <code>CommunicationLayerPropertyInfo</code> object with a name and value;
	 * other members default to their initial values.
     *
     * @param name the name of the property
     * @param value the current value, which may be null
     */
    public PropertyInfo(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * The name of the property.
     */
    private String name;
    public void setName( String name ){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    

    /**
     * A brief description of the property, which may be null.
     */
    private String description = null;
    public void setDescription( String description ){
        this.description = description;
    }
    public String getDescription(){
        return this.description;
    }

    /**
     * The <code>required</code> field is <code>true</code> if a value must be 
	 * supplied for this property and <code>false</code> otherwise.
     */
    private boolean required = false;
    public void setRequired( boolean value ){
        this.required = value;
    }
    public boolean isRequired(){
        return this.required;
    }

    /**
     * The <code>value</code> field specifies the current value of 
	 * the property, based on a combination of the information
	 * supplied to the method.
     */
    public String value = null;
    public void setValue( String value ){
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }
    public String getString(){
        return this.value;
    }
    public int getInt(){
        if ( this.value != null ) {
            return Integer.parseInt( this.value );
        } else {
            throw new NullPointerException( this.name + " is null." );
        }
    }
    public boolean getBoolean(){
        if ( this.value != null ){
            return Boolean.valueOf( this.value ).booleanValue();
        } else {
            throw new NullPointerException( this.name + " is null." );
        }
    }
    public byte[] getBytes(){
        if ( this.value != null ){
            return this.value.getBytes();
        } else {
            throw new NullPointerException( this.name + " is null." );
        }
    }

    /**
     * An array of possible values if the value for the field 
	 * <code>DriverPropertyInfo.value</code> may be selected
	 * from a particular set of values; otherwise null.
     */
    private String[] choices = null;
    public void setChoices( String[] choices ){
        this.choices = choices;
    }
    public String[] getChoices(){
        return this.choices;
    }
    
    
}
