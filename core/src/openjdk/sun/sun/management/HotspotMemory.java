/*
 * Copyright 2003-2004 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.management;

import java.util.List;
import sun.management.counter.*;

/**
 * Implementation class of HotspotMemoryMBean interface.
 *
 * Internal, uncommitted management interface for Hotspot memory 
 * system.
 *
 */
class HotspotMemory
    implements HotspotMemoryMBean {

    private VMManagement jvm;

    /**
     * Constructor of HotspotRuntime class.
     */ 
    HotspotMemory(VMManagement vm) {
        jvm = vm;
    }

    // Performance counter support
    private static final String JAVA_GC    = "java.gc.";
    private static final String COM_SUN_GC = "com.sun.gc.";
    private static final String SUN_GC     = "sun.gc.";
    private static final String GC_COUNTER_NAME_PATTERN =
        JAVA_GC + "|" + COM_SUN_GC + "|" + SUN_GC;

    public java.util.List getInternalMemoryCounters() {
        return jvm.getInternalCounters(GC_COUNTER_NAME_PATTERN); 
    } 
}
