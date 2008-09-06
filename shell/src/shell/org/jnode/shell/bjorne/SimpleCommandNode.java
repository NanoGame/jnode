/**
 * 
 */
package org.jnode.shell.bjorne;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellFailureException;
import org.jnode.shell.io.CommandIO;

public class SimpleCommandNode extends CommandNode {

    private BjorneToken[] assignments;

    private final BjorneToken[] words;

    public SimpleCommandNode(int nodeType, BjorneToken[] words) {
        super(nodeType);
        this.words = words;
    }

    public void setAssignments(BjorneToken[] assignments) {
        this.assignments = assignments;
    }

    public BjorneToken[] getWords() {
        return words;
    }

    public BjorneToken[] getAssignments() {
        return assignments;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SimpleCommand{").append(super.toString());
        if (assignments != null) {
            sb.append(",assignments=");
            appendArray(sb, assignments);
        }
        if (words != null) {
            sb.append(",words=");
            appendArray(sb, words);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) throws ShellException {
        BjorneContext.StreamHolder[] holders = null;
        int rc;
        try {
            BjorneToken[] words = getWords();
            if (words.length == 0) {
                // No command to run: assignments are done in the shell's context
                context.performAssignments(assignments);
                // Surprisingly, we still need to perform the redirections
                context = new BjorneContext(context);
                context.evaluateRedirections(getRedirects());
                rc = 0;
            } else {
                // Assignments and redirections are done in the command's context
                context = new BjorneContext(context);
                context.performAssignments(assignments);
                holders = context.evaluateRedirections(getRedirects());
                CommandLine command = context.expandAndSplit(words);
                CommandIO[] streams = new CommandIO[holders.length];
                for (int i = 0; i < streams.length; i++) {
                    streams[i] = holders[i].stream;
                }
                if ((getFlags() & BjorneInterpreter.FLAG_ASYNC) != 0) {
                    throw new ShellFailureException(
                            "asynchronous execution (&) not implemented yet");
                } else {
                    rc = context.execute(command, streams);
                }
            }
        } finally {
            if (holders != null) {
                for (BjorneContext.StreamHolder holder : holders) {
                    holder.close();
                }
            }
        }
        if ((getFlags() & BjorneInterpreter.FLAG_BANG) != 0) {
            rc = (rc == 0) ? -1 : 0;
        }
        return rc;
    }
}
