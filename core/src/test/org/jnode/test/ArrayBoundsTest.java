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
 
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ArrayBoundsTest {

	public static void main(String[] args) {
		test(args, -1, false);
		test(args, args.length, false);
		if (args.length > 0) {
			test(args, 0, true);
			test(args, args.length-1, true);
		}
	}
	
	private static void test(String[] arr, int index, boolean ok) {
		try {
			System.out.println(arr[index]);
			if (!ok) {
				throw new RuntimeException("Test should fail at index " + index);
			} else {
				System.out.println("Ok");
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			if (ok) {
				throw ex;
			} else {
				System.out.println("Ok: " + ex.getMessage());
			}
		}
	}
	
}
