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
package es.tid.TIDorbj.util;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class CircularTraceFile extends Writer
{

    /**
     * Base name for trace files
     */
    private String m_name;

    /**
     * Current trace file index
     */
    private int m_current = 0;

    /**
     * Current file descriptor
     */
    private File m_current_file = null;

    /**
     * Maximun size, in bytes, for a circular trace file, by default 100 K
     */
    private long m_size = 102400;

    /**
     * Circular file list size, by default 5
     */
    private int m_list_length = 5;

    /**
     * File descriptors for trace
     */
    private File[] m_files_list = null;

    /**
     * Output writer for trace
     */
    private FileWriter m_fw = null;

    public CircularTraceFile(int length, long size, String name)
        throws java.io.IOException
    {
        if (length != 0) //si vale cero -> valor por defecto
            m_list_length = length;
        if (size != 0) //si vale cero -> valor por defecto
            m_size = size;
        m_name = name;
        m_files_list = new File[m_list_length];
        try {
            getInitFile();
        }
        catch (java.io.IOException ioex) {
            throw ioex;
        }
    } //constructor

    /* implementación de los metodos abstractos del writer */
    public void close()
    {
        for (int i = 0; i < m_list_length; i++)
            m_files_list[i] = null;
    } //close

    public void flush()
        throws java.io.IOException
    {
        try {
            m_fw.flush();
        }
        catch (java.io.IOException ioex) {
            throw ioex;
        }
    } //flush

    public void write(char[] cbuf, int off, int len)
    {
        try {
            m_fw.write(cbuf, off, len);
            m_fw.flush();
            if (m_current_file.length() >= m_size)
                nextFile();
        }
        catch (java.io.IOException ioex) {}
    } //write

    /* fin implementacion de los metodos abstractos del writer */

    // devuelve el siguiente fichero de la lista, es decir, sobre el
    // que se deben seguir escribiendo las trazas
    private void nextFile()
        throws java.io.IOException
    {
        try {
            m_fw.close();
            m_current = (m_current + 1) % m_list_length;

            if (m_files_list[m_current] == null)
                //primera vez que se usa un fichero de la lista
                m_files_list[m_current] = new File(m_name + m_current);
            else
            //el fichero se reutiliza --> debo vaciarlo (lo borro y lo creo de
            // nuevo)
            {
                ((File) m_files_list[m_current]).delete();
                m_files_list[m_current] = new File(m_name + m_current);
                (m_files_list[m_current]).createNewFile();
            }
            m_current_file = (File) m_files_list[m_current];
            m_fw = new FileWriter(m_current_file.getPath(), true);
        }
        catch (java.io.IOException ioex) {
            throw ioex;
        }
    } //nextFile

    //elige como fichero a utilizar inicialmente el más antiguo
    private void getInitFile()
        throws java.io.IOException
    {
        int i = 0;
        File aux;
        File selected = new File(m_name + 0);
        int use = 0; //indice del vector de ficheros

        try {
            while (i < m_list_length) {
                aux = new File(m_name + i);
                if (aux.exists()) {
                    m_files_list[i] = aux;
                    if (aux.lastModified() < selected.lastModified()) {
                        //selected es mas actual
                        selected = aux;
                        use = i;
                    }
                }
                i++;
            }
            m_current = use;
            // En _files_list tenemos los nombres de los ficheros que existen
            // Si el que vamos a usar no esta en la lista, es porque no existe
            if (m_files_list[use] == null) // Lo apuntamos
                m_files_list[use] = selected;
            else
            // El fichero a usar ya existe, lo limpiamos.
            {
                ((File) m_files_list[m_current]).delete();
                m_files_list[m_current] = new File(m_name + m_current);
                (m_files_list[m_current]).createNewFile();
            }
            m_current_file = (File) m_files_list[use];
            m_fw = new FileWriter(m_current_file.getPath(), true);
        }
        catch (java.io.IOException ioex) {
            throw ioex;
        }
    } //getInitFile

} //class CircularTraceFile
