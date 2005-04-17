/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.spi;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;

/**
 * Abstract class with common things in different FileSystem implementations
 *  
 * @author Fabien DUMINY
 */
public abstract class AbstractFileSystem implements FileSystem {

    private boolean readOnly;

    private final Device device;

    private final BlockDeviceAPI api;

    private boolean closed;
    
    private FSEntry rootEntry;

    // cache of FSFile (key: FSEntry)
    private HashMap<FSEntry, FSFile> files = new HashMap<FSEntry, FSFile>();
    
    // cache of FSDirectory (key: FSEntry)
    private HashMap<FSEntry, FSDirectory> directories = new HashMap<FSEntry, FSDirectory>();

    /**
     * Construct an AbstractFileSystem in specified readOnly mode
     */
    public AbstractFileSystem(Device device, boolean readOnly)
            throws FileSystemException {
        if (device == null) throw new IllegalArgumentException("null device!");

        this.device = device;

        try {
            api = (BlockDeviceAPI) device.getAPI(BlockDeviceAPI.class);
        } catch (ApiNotFoundException e) {
            throw new FileSystemException("Device is not a partition!", e);
        }
                
        this.closed = false;
        this.readOnly = readOnly;
    }

    /**
     * @see org.jnode.fs.FileSystem#getDevice()
     */
    final public Device getDevice() {
        return device;
    }

    /**
     * @see org.jnode.fs.FileSystem#getRootEntry()
     */
    public FSEntry getRootEntry() throws IOException
	{
    	log.debug("<<< BEGIN getRootEntry >>>");
    	if(isClosed())
    		throw new IOException("FileSystem is closed");
    		    	
    	if(rootEntry == null)
    	{
   			rootEntry = createRootEntry();
    	}
    	log.debug("<<< END getRootEntry >>>");
    	return rootEntry;
	}

    /**
     * @see org.jnode.fs.FileSystem#close()
     */
    public void close() throws IOException {
    	if(!isClosed())
    	{
	        // if readOnly, nothing to do
	        if (!isReadOnly()) {
	            flush();
	        }
	        
	        api.flush();
	        files.clear();
	        directories.clear();
	        
	        // these fields are final, can't nullify them
	        //device = null;
	        //api = null;
	        
	        rootEntry = null;
	        files = null;
	        directories = null;	        

	        closed = true;	    	        
    	}
    }

    /**
     * Save the content that have been altered but not saved in the Device
     * @throws IOException
     */
    public void flush() throws IOException
	{
    	log.debug("<<< BEGIN flush >>>");
    	flushFiles();
    	flushDirectories();    	
    	log.debug("<<< END flush >>>");
	}

    /**
     * @return Returns the api.
     */
    public final BlockDeviceAPI getApi() {
        return api;
    }

    /**
     * @return Returns the FSApi.
     */
    final public FSBlockDeviceAPI getFSApi() throws ApiNotFoundException {
    	return (FSBlockDeviceAPI) device.getAPI(FSBlockDeviceAPI.class);
    }

    /**
     * @return Returns the closed.
     */
    final public boolean isClosed() {
        return closed;
    }

    /**
     * @return Returns the readOnly.
     */
    final public boolean isReadOnly() {
        return readOnly;
    }
    
    final protected void setReadOnly(boolean readOnly) {
    	this.readOnly = readOnly;
    }
    
	/**
	 * Gets the file for the given entry.
	 * 
	 * @param entry
	 */
	final public synchronized FSFile getFile(FSEntry entry) throws IOException {
    	if(isClosed())
    		throw new IOException("FileSystem is closed");
    		
		FSFile file = (FSFile)files.get(entry);
		if (file == null) {
			file = createFile(entry);
			files.put(entry, file);
		}
		return file;
	}
	
	/**
	 * Abstract method to create a new FSFile from the entry
	 * @param entry
	 * @return
	 * @throws IOException
	 */
	protected abstract FSFile createFile(FSEntry entry) throws IOException;
	
	/**
	 * Flush all unsaved FSFile in our cache
	 * @throws IOException
	 */
	final private void flushFiles() throws IOException
	{
		log.info("flushing files ...");
		for (FSFile f : files.values()) {
			log.debug("flush: flushing file "+f);			
			f.flush();
		}		
	}

	/**
	 * Gets the file for the given entry.
	 * 
	 * @param entry
	 */
	final public synchronized FSDirectory getDirectory(FSEntry entry) throws IOException {
    	if(isClosed())
    		throw new IOException("FileSystem is closed");
    		
		FSDirectory dir = (FSDirectory)directories.get(entry);
		if (dir == null) {
			dir = createDirectory(entry);
			directories.put(entry, dir);
		}
		return dir;
	}

	/**
	 * Abstract method to create a new directory from the given entry
	 * @param entry
	 * @return
	 * @throws IOException
	 */
	protected abstract FSDirectory createDirectory(FSEntry entry) throws IOException;
	
	/**
	 * Flush all unsaved FSDirectory in our cache
	 * @throws IOException
	 */
	final private void flushDirectories() throws IOException
	{
		log.info("flushing directories ...");
		for (FSDirectory d : directories.values()) {
			log.debug("flush: flushing directory "+d);
			
			//TODO: uncomment this line
			//d.flush();
		}		
	}

    /**
     * Abstract method to create a new root entry
     * @return
     * @throws IOException
     */
    protected abstract FSEntry createRootEntry() throws IOException;

    static private final Logger log = Logger.getLogger(AbstractFileSystem.class);    
}
