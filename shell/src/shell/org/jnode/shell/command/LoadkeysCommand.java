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
 
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.KeyboardInterpreterFactory;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author Marc DENTY
 * @version Feb 2004
 * @since 0.15
 */
public class LoadkeysCommand {

    static final Argument COUNTRY = new Argument("country", "country parameter");

    static final Argument LANGUAGE = new Argument("language", "language parameter");

    static final Argument VARIANT = new Argument("variant", "variant parameter");

    static final Parameter PARAM_COUNTRY = new Parameter(COUNTRY);

    static final Parameter PARAM_LANGUAGE = new Parameter(LANGUAGE, Parameter.OPTIONAL);

    static final Parameter PARAM_VARIANT = new Parameter(VARIANT, Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info(
            "loadkeys",
            new Syntax[] { 
            	new Syntax("Display the current keyboard layout"),
            	new Syntax("change the current keyboard layout\n\tExample : loadkeys FR fr",
                    PARAM_COUNTRY, PARAM_LANGUAGE, PARAM_VARIANT)
				});

    public static void main(String[] args) throws Exception {
        new LoadkeysCommand().execute(args, System.in, System.out, System.err);
    }

    /**
     * Execute this command
     */
    protected void execute(String[] args, InputStream in, PrintStream out,
            PrintStream err) throws Exception {
        final Collection<Device> kbDevs = DeviceUtils
                .getDevicesByAPI(KeyboardAPI.class);

        ParsedArguments cmdLine = HELP_INFO.parse(args);

        for (Device kb : kbDevs) {
            final KeyboardAPI api = kb.getAPI(KeyboardAPI.class);

            if (!PARAM_COUNTRY.isSatisfied()) {
                out.println("layout currently loaded : "
                        + api.getKbInterpreter().getClass().getName());
            } else {
                final String country = COUNTRY.getValue(cmdLine);
                String language = LANGUAGE.getValue(cmdLine);
                String variant = VARIANT.getValue(cmdLine);

                //TODO add more validation for country and language
                if(language != null &&
                        !language.equals(language.toLowerCase()) &&
                        variant == null){

                    variant = language;
                    language = null;
                }

                final KeyboardInterpreter kbInt = KeyboardInterpreterFactory
                        .getKeyboardInterpreter(country, language, variant);
                if (kbInt != null) {
                	api.setKbInterpreter(kbInt);
                } else {
                	out.println("Not found");
                }
            }
        }
        out.println(" Done.");
    }
}
