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
package es.tid.TIDorbj.core.cdr;

import java.util.Vector;

/**
 * represents a marshaled octet IIOP stream thad can be splitted in chunks.
 * <P>
 * This buffer will exentialy used for store chunks received from a conection
 * for reading data. Also, the buffer can be used for marshal a octet stream. In
 * this case, the buffer can be monochunk or multichunk an growable or not.
 * <P>
 * The buffer can be transformed in a String or byte array for storing IIOP
 * data. The String and byte array formats allows regenerate the buffer.
 * 
 * @author Juan A. C&aacute;ceres
 * @version 1.0
 */
public class BufferCDR
{

    /**
     * Header size in the array format.
     */
    final static int ARRAY_HEADER_SIZE = 5;

    /**
     * Header size in the string format.
     */
    public final static int STRING_HEADER_SIZE = 14;

    /**
     * Vector that contains the buffer chunks.
     */
    protected Vector m_chunks;

    /**
     * The size of the new chunks.
     */

    protected int m_chunk_size;

    /**
     * Creates a new empty buffer monochunk for marshaling.
     */

    public BufferCDR(int block_size)
    {
        m_chunks = new Vector();
        m_chunk_size = block_size;
        addChunk(new ChunkCDR(m_chunk_size));
    }

    /**
     * Creates a buffer monochunk with the array.
     */

    public BufferCDR(byte[] buffer)
    {
        m_chunks = new Vector();
        m_chunk_size = buffer.length;
        addChunk(new ChunkCDR(buffer));
    }

    /**
     * Creates a buffer monochunk with the chunk.
     */

    public BufferCDR(ChunkCDR chunk)
    {
        m_chunks = new Vector();
        m_chunk_size = chunk.m_buffer.length;
        addChunk(chunk);
    }

    /**
     * Returns the number of chunks.
     */
    public int getNumChunks()
    {
        return m_chunks.size();
    }

    /**
     * @return <code>true</code> if it has an unique chunk, <code>false</code>
     *         otherwise.
     */
    public boolean isMonoChunk()
    {
        return (m_chunks.size() == 1);
    }

    /**
     * @return the chunk associated to de index, or <code>null</code> if not
     *         exits.
     */
    public ChunkCDR getChunk(int index)
    {
        if (index < m_chunks.size())
            return (ChunkCDR) m_chunks.elementAt(index);
        else
            return null;
    }

    /**
     * Prepare the buffer to be reused.
     */
    public void recycle()
    {
        int num_chunks = getNumChunks();
        for (int i = 0; i < num_chunks; i++)
            ((ChunkCDR) m_chunks.elementAt(i)).recycle();
    }

    /**
     * Insert a new chunk in the buffer.
     */
    public boolean addChunk(ChunkCDR chunk)
    {
        m_chunks.addElement(chunk);
        return true;
    }

    /**
     * Insert a new chunk in the buffer at a position less than the buffer size.
     */
    public boolean insertChunk(ChunkCDR chunk, int at)
    {
        if (at >= m_chunks.size())
            return false;
        m_chunks.insertElementAt(chunk, at);
        return true;
    }

    /**
     * Creates and inserts a new chunk ant the end of the buffer.
     * 
     * @return <code>true</code> if OK or <code>false</code> on error;
     */

    public boolean grow()
    {
        if (m_chunk_size > 0) {
            addChunk(new ChunkCDR(m_chunk_size));
            return true;
        } else
            return false;
    }

    public boolean grow_1_0()
    {
        if (m_chunk_size > 0) {

            if (m_chunks.size() != 1)
                return false;
            ChunkCDR chunk = getChunk(0);

            byte[] aux_buffer = new byte[chunk.m_buffer.length + m_chunk_size];

            System.arraycopy(aux_buffer, 0, chunk.m_buffer, 0,
                             chunk.m_buffer.length);

            chunk.setBuffer(aux_buffer);

            return true;

        } else
            return false;
    }

    public PointerCDR getPointer(int position)
    {
        //pre: position < available

        int num_chunk = 0;
        int chunk_available = 0;
        int available = 0;
        while (num_chunk < m_chunks.size()) {
            chunk_available = getChunk(num_chunk).getAvailable();
            if (position <= chunk_available + available - 1)
                break; // this is the position
            available += chunk_available;
            num_chunk++;
        }

        if (num_chunk >= m_chunks.size())
            return null;

        return new PointerCDR(this, num_chunk, position - available);
    }

    public int getAvailable()
    {
        int available = 0;
        int num_chunks = m_chunks.size();
        for (int i = 0; i < num_chunks; i++)
            available += getChunk(i).getAvailable();

        return available;
    }

    public int getNumAvailableChunks()
    {
        int available = 0;
        int num_chunks = m_chunks.size();
        for (int i = 0; i < num_chunks; i++) {
            if (getChunk(i).getAvailable() > 0)
                available++;
            else
                break;
        }

        return available;
    }

    /**
     * Compares the data of two <code>BufferCDR</code>
     * 
     * @return <code> true</code> if the data contained is equal.
     */
    public boolean equal(BufferCDR buff)
    {
        int available = getAvailable();
        if (available != buff.getAvailable())
            return false;
        int num_chunks = getNumAvailableChunks();

        if (num_chunks != buff.getNumAvailableChunks())
            return false;

        for (int i = 0; i < num_chunks; i++)
            if (getChunk(i).getAvailable() != buff.getChunk(i).getAvailable())
                return false;

        byte[] buffer_a, buffer_b;
        for (int i = 0; i < num_chunks; i++) {
            buffer_a = getChunk(i).getBuffer();
            buffer_b = buff.getChunk(i).getBuffer();
            int size = getChunk(i).getAvailable();
            for (int j = 0; j < size; j++)
                if (buffer_a[j] != buffer_b[j])
                    return false;
        }

        return true;

    }

}