/* SystemLogger.java -- Classpath's system debugging logger.
   Copyright (C) 2005  Free Software Foundation, Inc.

This file is a part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under terms
of your choice, provided that you also meet, for each linked independent
module, the terms and conditions of the license of that module.  An
independent module is a module which is not derived from or based on
this library.  If you modify this library, you may extend this exception
to your version of the library, but you are not obligated to do so.  If
you do not wish to do so, delete this exception statement from your
version.  */


package gnu.classpath.debug;

import gnu.java.security.action.GetPropertyAction;

import java.security.AccessController;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SystemLogger extends Logger
{
  public static final SystemLogger SYSTEM = new SystemLogger();

  static
  {
    SYSTEM.setFilter (PreciseFilter.GLOBAL);
    String defaults = (String) AccessController.doPrivileged
      (new GetPropertyAction("gnu.classpath.debug.components"));

    if (defaults != null)
      {
        StringTokenizer tok = new StringTokenizer (defaults, ",");
        while (tok.hasMoreTokens ())
          {
            Component c = Component.forName (tok.nextToken ());
            if (c != null)
              PreciseFilter.GLOBAL.enable (c);
            SYSTEM.log (Level.INFO, "enabled: {0}", c);
          }
      }
  }

  /**
   * Fetch the system logger instance. The logger returned is meant for debug
   * and diagnostic logging for Classpath internals.
   *
   * @return The system logger.
   */
  public static SystemLogger getSystemLogger()
  {
    // XXX Check some permission here?
    return SYSTEM;
  }

  /**
   * Keep only one instance of the system logger.
   */
  private SystemLogger()
  {
    super("gnu.classpath", null);
  }
  
  /**
   * Variable-arguments log method.
   * 
   * @param level The level to log to.
   * @param format The format string.
   * @param args The arguments.
   */
  public void logv(Level level, String format, Object... args)
  {
    log(level, format, args);
  }
}
