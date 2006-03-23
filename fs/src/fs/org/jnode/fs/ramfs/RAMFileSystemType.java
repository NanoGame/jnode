package org.jnode.fs.ramfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.partitions.PartitionTableEntry;

public class RAMFileSystemType implements FileSystemType {

	private static final int DEFAULT_SIZE = 104857600;
	
	public static final String NAME = "RAMFS";
		
	public String getName() {
		return NAME;
	}

	public boolean supports(PartitionTableEntry pte, byte[] firstSector,
			FSBlockDeviceAPI devApi) {
		return false;
	}

	public FileSystem create(Device device, boolean readOnly)
			throws FileSystemException {
		
		if (!(device instanceof RAMFSDevice))
			throw new FileSystemException("Only NULL Device is supported");
		
		return new RAMFileSystem(device, readOnly, DEFAULT_SIZE);
	}

	public FileSystem format(Device device, Object specificOptions)
			throws FileSystemException {

		if (!(device instanceof RAMFSDevice))
			throw new FileSystemException("Only NULL Device is supported");

		// TODO read in specificOptions ... How todo that??

		return new RAMFileSystem(device, false, DEFAULT_SIZE);
	}

}
