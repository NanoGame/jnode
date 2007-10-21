/*
 * $Id: FTPFSDevice.java 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.fs.nfs.nfs2;

import org.jnode.driver.Device;

/**
 * @author Andrei Dore
 */
public class NFS2Device extends Device {
    private String host;
    private String remoteDirectory;
    private int uid;
    private int gid;

    public NFS2Device(String host, String remoteDirectory) {
        this(host, remoteDirectory, -1, -1);

    }

    public NFS2Device(String host, String remoteDirectory, int uid, int gid) {
        super(null, "nfs2-(" + host + "," + remoteDirectory + "," + uid + "," + gid + ")");
        this.host = host;
        this.remoteDirectory = remoteDirectory;

        this.uid = uid;
        this.gid = gid;

    }

    public String getHost() {
        return host;
    }

    public String getRemoteDirectory() {
        return remoteDirectory;
    }

    public int getUid() {
        return uid;
    }

    public int getGid() {
        return gid;
    }

}
