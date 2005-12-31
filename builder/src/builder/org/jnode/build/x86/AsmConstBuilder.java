/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package org.jnode.build.x86;

import org.jnode.build.AbstractAsmConstBuilder;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.x86.VmX86Architecture32;
import org.jnode.vm.x86.VmX86Architecture64;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AsmConstBuilder extends AbstractAsmConstBuilder {

	private int bits = 32;

	private VmArchitecture arch;

	protected VmArchitecture getArchitecture() {
		if (arch == null) {
			switch (bits) {
			case 32:
				arch = new VmX86Architecture32();
				break;
			case 64:
				arch = new VmX86Architecture64();
				break;
			default:
				throw new IllegalArgumentException("Invalid bits " + bits);
			}
		}
		return arch;
	}

	public final int getBits() {
		return bits;
	}

	public final void setBits(int bits) {
		if ((bits != 32) && (bits != 64)) {
			throw new IllegalArgumentException("Invalid bits " + bits);
		}
		this.bits = bits;
	}
}
