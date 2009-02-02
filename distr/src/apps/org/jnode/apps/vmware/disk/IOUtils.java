/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.apps.vmware.disk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.extent.Access;
import org.jnode.apps.vmware.disk.extent.ExtentType;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.IOHandler;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.apps.vmware.disk.handler.simple.SimpleExtentFactory;
import org.jnode.apps.vmware.disk.handler.sparse.SparseExtentFactory;
import org.jnode.apps.vmware.disk.handler.sparse.SparseExtentHeader;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class IOUtils {
    private static final Logger LOG = Logger.getLogger(IOUtils.class);

    private static final String COMMENT = "#";
    private static final String EQUAL = "=";

    private static final ExtentFactory[] FACTORIES = {
        new SparseExtentFactory(), 
        new SimpleExtentFactory(), 
    };

    public static final int INT_SIZE = 4;
    public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    public static class KeyValue {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "KeyValue[key:" + key + ", value:" + value + "]";
        }

        public void setNull() {
            setKey(null);
            setValue(null);
        }

        public boolean isNull() {
            return (key == null) && (value == null);
        }
    }

    public static String readLine(BufferedReader reader) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null) {
            LOG.debug("line=" + line);

            line = line.trim();
            if (!line.isEmpty() && !line.startsWith(COMMENT)) {
                return line;
            }
        }

        LOG.debug("no more lines");
        return null;
    }

    public static String removeQuotes(String value) {
        // remove enclosing '"'
        return (value == null) ? null : value.substring(1, value.length() - 1);
    }

    public static KeyValue readValue(BufferedReader reader, KeyValue keyValue, String wantedKey,
            boolean removeQuotes) throws IOException {
        keyValue = readValue(readLine(reader), keyValue, wantedKey);
        if (keyValue.isNull()) {
            return keyValue;
        }

        if (wantedKey != null) {
            while (keyValue.getValue() == null) {
                keyValue = readValue(readLine(reader), keyValue, wantedKey);
                if (keyValue.isNull()) {
                    return keyValue;
                }
            }
        }

        keyValue.setValue(removeQuotes ? removeQuotes(keyValue.getValue()) : keyValue.getValue());

        return keyValue;
    }

    private static KeyValue readValue(String line, KeyValue keyValue, String wantedKey)
        throws IOException {
        keyValue = (keyValue == null) ? new KeyValue() : keyValue;

        keyValue.setNull();

        if (line == null) {
            return keyValue;
        }

        int idx = line.indexOf(EQUAL);
        if (idx < 0) {
            LOG.debug("err2: tried to read key " + wantedKey + ", line=" + line);
            return keyValue;
        }

        keyValue.setKey(line.substring(0, idx).trim());
        keyValue.setValue(line.substring(idx + 1).trim());
        LOG.debug("readValue: line=" + line + " idx=" + idx + " -> KeyValue=" + keyValue);

        if ((wantedKey != null) && !keyValue.getKey().equals(wantedKey)) {
            LOG.debug("readValue: KeyValue=" + keyValue);
            LOG.fatal("************");
            throw new IOException("excepted key(" + wantedKey + ") not found (actual:" +
                    keyValue.getKey() + ")");
        }

        return keyValue;
    }

    public static FileDescriptor readFileDescriptor(File file)
        throws IOException, UnsupportedFormatException {
        FileDescriptor fileDescriptor = null;

        for (ExtentFactory f : FACTORIES) {
            try {
                LOG.debug("trying with factory " + f.getClass().getName());
                FileDescriptor fd = f.createFileDescriptor(file);

                // we have found the factory for that format
                fileDescriptor = fd;

                break;
            } catch (UnsupportedFormatException e) {
                // ignore, we will try with the next factory
                LOG.debug(f.getClass().getName() + ":" + file + " not supported. reason: " +
                        e.getMessage());
            }
        }

        if (fileDescriptor == null) {
            throw new UnsupportedFormatException("format not supported for file " + file);
        }

        LOG.info("descriptor for " + file.getName() + " is " + fileDescriptor.getClass().getName());

        return fileDescriptor;
    }

    public static ExtentDeclaration createExtentDeclaration(File mainFile, String fileName,
            Access access, long sizeInSectors, ExtentType extentType, long offset) {
        final File extentFile = IOUtils.getExtentFile(mainFile, fileName);
        final boolean isMainExtent = extentFile.getName().equals(mainFile.getName());
        return new ExtentDeclaration(access, sizeInSectors, extentType, fileName, extentFile,
                offset, isMainExtent);
    }

    public static File getExtentFile(File mainFile, String extentFileName) {
        String path = mainFile.getParentFile().getAbsolutePath();
        return new File(path, extentFileName);
    }

    public static Map<String, String> readValuesMap(String lastLine, BufferedReader br,
            boolean removeQuotes, String... requiredKeys) throws IOException {
        Map<String, String> values = new HashMap<String, String>();

        KeyValue keyValue = IOUtils.readValue(lastLine, null, null);
        if (keyValue.getValue() == null) {
            keyValue = IOUtils.readValue(br, keyValue, null, removeQuotes);
        }
        values.put(keyValue.getKey(), keyValue.getValue());

        while ((keyValue = IOUtils.readValue(br, keyValue, null, removeQuotes)).getValue() != null) {
            values.put(keyValue.getKey(), keyValue.getValue());
        }

        // check required keys
        boolean error = false;
        StringBuilder sb = new StringBuilder("required keys not found : ");
        for (String reqKey : requiredKeys) {
            if (!values.keySet().contains(reqKey)) {
                error = true;
                sb.append(reqKey).append(',');
            }
        }
        if (error) {
            throw new IOException(sb.toString());
        }

        return values;
    }

    public static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    public static ByteBuffer allocate(int capacity) {
        ByteBuffer bb = ByteBuffer.allocate(capacity);
        bb.order(BYTE_ORDER);
        return bb;
    }

    public static ByteBuffer getSectorsByteBuffer(RandomAccessFile raf, int firstSector,
            int nbSectors) throws IOException {
        IOUtils.positionSector(raf.getChannel(), firstSector);
        return IOUtils.getByteBuffer(raf, nbSectors * IOHandler.SECTOR_SIZE);
    }

    public static ByteBuffer getByteBuffer(RandomAccessFile raf, int size) throws IOException {
        FileChannel ch = raf.getChannel();

        // int capacity = Math.min(size, (int) (raf.length() - ch.position()));
        // if(capacity == 0)
        // {
        // throw new IOException("empty file");
        // }
        //
        if ((ch.position() + size) > ch.size()) {
            // TODO fix the bug
            LOG.fatal("getByteBuffer: FATAL: size too big. size=" + size + " position=" +
                    ch.position() + " channel.size=" + ch.size());
            size = (int) (ch.size() - ch.position());
        }

        LOG.debug("getByteBuffer: pos=" + ch.position() + " size=" + size + " channel.size=" +
                ch.size());
        ByteBuffer bb = ch.map(MapMode.READ_ONLY, ch.position(), size);
        bb.order(BYTE_ORDER);

        if (LOG.isDebugEnabled()) {
            LOG.debug("bb=" + bb.toString() + " content=" + bb.duplicate().asCharBuffer());
        }

        return bb;
    }

    public static void positionSector(FileChannel channel, long sector) throws IOException {
        channel.position(sector * IOHandler.SECTOR_SIZE);
        LOG.debug("positionSector(sector=" + sector + ") -> " + channel.position());
    }

    public static boolean isPowerOf2(long value) {
        long val = 1;
        if (val == value) {
            return true;
        }

        for (int i = 0; i < 64; i++) {
            val <<= 1;
            if (val == value) {
                return true;
            }
        }

        return false;
    }

    public static void computeGrainTableCoverage(SparseExtentHeader header) {
        header.setGrainTableCoverage(header.getNumGTEsPerGT() * header.getGrainSize());
    }
}
