/*
 * Copyright 1996-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.beans;

import java.lang.ref.Reference;

/**
 * A BeanDescriptor provides global information about a "bean",
 * including its Java class, its displayName, etc.
 * <p>
 * This is one of the kinds of descriptor returned by a BeanInfo object,
 * which also returns descriptors for properties, method, and events.
 */

public class BeanDescriptor extends FeatureDescriptor {

    private Reference beanClassRef;
    private Reference customizerClassRef;

    /**
     * Create a BeanDescriptor for a bean that doesn't have a customizer.
     *
     * @param beanClass  The Class object of the Java class that implements
     *		the bean.  For example sun.beans.OurButton.class.
     */
    public BeanDescriptor(Class<?> beanClass) {
	this(beanClass, null);
    }

    /**
     * Create a BeanDescriptor for a bean that has a customizer.
     *
     * @param beanClass  The Class object of the Java class that implements
     *		the bean.  For example sun.beans.OurButton.class.
     * @param customizerClass  The Class object of the Java class that implements
     *		the bean's Customizer.  For example sun.beans.OurButtonCustomizer.class.
     */
    public BeanDescriptor(Class<?> beanClass, Class<?> customizerClass) {
	beanClassRef = createReference(beanClass);
	customizerClassRef = createReference(customizerClass);

	String name = beanClass.getName();
	while (name.indexOf('.') >= 0) {
	    name = name.substring(name.indexOf('.')+1);
	}
	setName(name);
    }

    /**
     * Gets the bean's Class object.
     *
     * @return The Class object for the bean.
     */
    public Class<?> getBeanClass() {
	return (Class)getObject(beanClassRef);
    }

    /**
     * Gets the Class object for the bean's customizer.
     *
     * @return The Class object for the bean's customizer.  This may
     * be null if the bean doesn't have a customizer.
     */
    public Class<?> getCustomizerClass() {
	return (Class)getObject(customizerClassRef);
    }

    /*
     * Package-private dup constructor
     * This must isolate the new object from any changes to the old object.
     */
    BeanDescriptor(BeanDescriptor old) {
	super(old);
	beanClassRef = old.beanClassRef;
	customizerClassRef = old.customizerClassRef;
    }
}
