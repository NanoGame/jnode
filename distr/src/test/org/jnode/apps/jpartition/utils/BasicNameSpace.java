/**
 * 
 */
package org.jnode.apps.jpartition.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnode.naming.AbstractNameSpace;

public final class BasicNameSpace extends AbstractNameSpace {
    protected final Map<Class<?>, Object> namespace = new HashMap<Class<?>, Object>();

    public <T> void bind(Class<T> name, T service)
        throws NamingException, NameAlreadyBoundException {
        namespace.put(name, service);
    }

    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<T> name) throws NameNotFoundException {
        return (T) namespace.get(name);
    }

    public Set<Class<?>> nameSet() {
        return namespace.keySet();
    }

    public void unbind(Class<?> name) {
        namespace.remove(name);
    }
}
