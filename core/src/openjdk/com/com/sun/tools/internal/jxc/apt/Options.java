/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.internal.jxc.apt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sun.tools.internal.xjc.BadCommandLineException;

/**
 * This stores the invocation configuration for
 * SchemaGenerator
 *
 * @author Bhakti Mehta
 */
public class Options  {

    // honor CLASSPATH environment variable, but it will be overrided by -cp
    public String classpath = System.getenv("CLASSPATH");

    public File targetDir = null;

    public final List<String> arguments = new ArrayList<String>();

    public void parseArguments(String[] args) throws BadCommandLineException {
        for (int i = 0 ; i <args.length; i++) {
            if (args[i].charAt(0)== '-') {
                int j = parseArgument(args,i);
                if(j==0)
                    throw new BadCommandLineException(
                            Messages.UNRECOGNIZED_PARAMETER.format(args[i]));
                i += j;
            } else {
                arguments.add(args[i]);
            }
        }
    }

    private int parseArgument( String[] args, int i ) throws BadCommandLineException {
        if (args[i].equals("-d")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                        (Messages.NO_FILE_SPECIFIED.format()));
            targetDir = new File(args[++i]);
            if( !targetDir.exists() )
                throw new BadCommandLineException(
                        Messages.NON_EXISTENT_FILE.format(targetDir));
            return 1;
        }

        if (args[i].equals("-cp") || args[i].equals("-classpath")) {
            if (i == args.length - 1)
                throw new BadCommandLineException(
                        (Messages.NO_CLASSPATH_SPECIFIED.format()));
            classpath = args[++i];
            
            return 1;
        }

        return 0;

    }


}


