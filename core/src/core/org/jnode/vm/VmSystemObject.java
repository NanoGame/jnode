/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2004 JNode.org
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
package org.jnode.vm;

import java.util.StringTokenizer;

public abstract class VmSystemObject implements BootableObject {
	/**
	 * Mangle an identifier into a ASCII C name
	 * @param s
	 * @return String
	 */
	public static String mangle(String s) {
		final StringBuffer res = new StringBuffer();
		final char[] src = s.toCharArray();
		final int cnt = s.length();
		for (int i = 0; i < cnt; i++) {
			final char ch = src[i];
			if (((ch >= 'a') && (ch <= 'z'))
				|| ((ch >= 'A') && (ch <= 'Z'))
				|| ((ch >= '0') && (ch <= '9'))) {
				res.append(ch);
			} else {
				res.append(Integer.toHexString(ch));
			}
		}
		return res.toString();
	}

	/**
	 * Mangle a classname into a ASCII C name
	 * @param s
	 * @return String
	 */
	public static String mangleClassName(String s) {
		s = s.replace('/', '.'); 
		final StringTokenizer tok = new StringTokenizer(s, ".");
		final StringBuffer res = new StringBuffer();
		int q = tok.countTokens();
		res.append('Q');
		res.append(q);
		while (tok.hasMoreTokens()) {
			String v = tok.nextToken();
			res.append(v.length());
			res.append(v);
		}
		return res.toString();
	}
	
	/**
	 * Verify this object, just before it is written to the boot image during
	 * the build process.
	 */
	public void verifyBeforeEmit() {}
	
	/**
	 * This method is called in the build process to get extra information
	 * on this object. This extra information is added to the listing file.
	 * @return String
	 */
	public String getExtraInfo() {
		return null;
	}
}
