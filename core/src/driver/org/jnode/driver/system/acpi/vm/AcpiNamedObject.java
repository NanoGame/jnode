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
 
package org.jnode.driver.system.acpi.vm;

import java.io.PrintWriter;


/**
 * AcpiNamedObject.
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class AcpiNamedObject extends AcpiObject {

	private String name;

	public AcpiNamedObject() {
		this.name = null;
	}

	public AcpiNamedObject(String name) {
		this.name = name;
		this.putInSameNameSpace(NameSpace.currentNameSpace);
	}

	public AcpiNamedObject(NameSpace space, String name) {
		super(space);
		this.name = name;
		this.putInSameNameSpace(space);
	}

	public String getName() {
		return name;
	}
	
	protected void setName(String name) {
		this.name = name;
	}

	public void dump(PrintWriter out, String prefix) {
		out.println(toString(prefix));
	}

	public String toString(String prefix) {
		String className = this.getClass().getName();
		String n = className.substring(className.lastIndexOf(".") + 1);
		return prefix + n + ": " + this.name;
	}

}
