/*
 * $Id$
 */
package org.jnode.shell.command.driver.console;

import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Set;

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;

/**
 * @author vali
 */
public class ConsoleCommand {

    private static Parameter ListParameter = new Parameter(new Argument("-l",
            "list all registered consoles", false), Parameter.OPTIONAL);

    private static Parameter NewUserConsoleParameter = new Parameter(
            new Argument("-n", "create new Shell Console", false),
            Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info(
            "console",
            new Syntax[] { new Syntax("Console administration",
                    new Parameter[] { ListParameter, NewUserConsoleParameter }), });

    /**
     * Displays the system date
     * 
     * @param args
     *            No arguments.
     */
    public static void main(String[] args) throws NameNotFoundException,
            ShellException {

        final ConsoleManager conMgr = (ConsoleManager) InitialNaming
                .lookup(ConsoleManager.NAME);
        
        if (args.length > 0) {
            if (args[0].equals("-l")) {
                final Set consoleNames = conMgr.getConsoleNames();
                System.out.println("Nr. of registered consoles: "
                        + consoleNames.size());
                for (Iterator i = consoleNames.iterator(); i.hasNext();) {
                    final String name = (String)i.next();
                    final Console console = conMgr.getConsole(name);
                    System.out.println("      - " + name + " ACCEL:" + KeyEvent.getKeyText(console.getAcceleratorKeyCode()));
                }
            } else if (args[0].equals("-n")) {
                final TextConsole console = (TextConsole) conMgr
                        .createConsole(null, ConsoleManager.CreateOptions.TEXT
                                | ConsoleManager.CreateOptions.SCROLLABLE);
                CommandShell commandShell = new CommandShell(console);
                new Thread(commandShell).start();

                ShellUtils.getShellManager().registerShell(commandShell);
                System.out.println("Console created with name:"
                        + console.getConsoleName());
            }
        } else {
            System.out.println("test RawTextConsole");
            final TextConsole console = (TextConsole) conMgr
                    .createConsole(null, ConsoleManager.CreateOptions.TEXT);
            conMgr.registerConsole(console);
            conMgr.focus(console);
            console.clear();
        }
    }
}