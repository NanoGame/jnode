/*
 * $Id$
 */
package org.jnode.ant.taskdefs.classpath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.types.FileSet;


/**
 * Task used to compare the latest classpath version against the latest
 * jnode version of classpath.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CompareTask extends Task {

    private BaseDirs vmDirs;
    private BaseDirs classpathDirs;
    private File destDir;
    private String vmSpecificTag = "@vm-specific";
    private String classpathBugfixTag = "@classpath-bugfix";
    
    private static final int NO_CHANGE = 1;
    private static final int NEEDS_MERGE = 2;
    private static final int VM_SPECIFIC = 3;
    private static final int CLASSPATH_BUGFIX = 4;
    
    
    public void execute() {
        if (destDir == null) {
            throw new BuildException("The destdir attribute must be set");
        }
        final Map vmFiles = scanJavaFiles(vmDirs);
        final Map classpathFiles = scanJavaFiles(classpathDirs);
        final TreeSet allFiles = new TreeSet();
        allFiles.addAll(vmFiles.keySet());
        allFiles.addAll(classpathFiles.keySet());
        
        try {
            destDir.mkdirs();
            final File outFile = new File(destDir, "classpath-compare.html");
            final PrintWriter out = new PrintWriter(new FileWriter(outFile));
            reportHeader(out);
            int missingInCp = 0;
            int missingInVm = 0;
            int needsMerge = 0;
            int diffVmSpecific = 0;
            int diffClasspathBugfix = 0;
            
            for (Iterator i = allFiles.iterator(); i.hasNext(); ) {
                final String name = (String)i.next();
                final JavaFile cpFile = (JavaFile)classpathFiles.get(name);
                final JavaFile vmFile = (JavaFile)vmFiles.get(name);
                
                if (!vmFiles.containsKey(name)) {
                    reportMissing(out, cpFile.getClassName(), "classpath");
                    missingInCp++;
                } else if (!classpathFiles.containsKey(name)) {
                    reportMissing(out, vmFile.getClassName(), "vm");
                    missingInVm++;
                } else {                    
                    final String diffFileName = vmFile.getClassName() + ".diff";
                    int rc = runDiff(vmFile, cpFile, diffFileName);
                    switch (rc) {
                    case NO_CHANGE: break;
                    case NEEDS_MERGE:
                        reportNeedsMerge(out, vmFile.getClassName(), diffFileName);
                        needsMerge++;
                        //diffPackages.put(vmFile.getPackageName(), )
                        break;
                    case VM_SPECIFIC:
                        reportDiffVmSpecific(out, vmFile.getClassName(), diffFileName);
                        diffVmSpecific++;
                        break;
                    case CLASSPATH_BUGFIX:
                        reportDiffClasspathBugfix(out, vmFile.getClassName(), diffFileName);
                        diffClasspathBugfix++;
                        break;
                    default:
                        throw new RuntimeException("Invalid rc " + rc);
                    }
                    // Let's compare them
                }
            }
            // Summary
            out.println("</table><p/><a name='summary'/><h2>Summary</h2>");
            if (missingInCp > 0) {
            	out.println("Found " + missingInCp + " files missing in classpath</br>");
            	log("Found " + missingInCp + " files missing in classpath");
            }
            if (missingInVm > 0) {
            	out.println("Found " + missingInVm + " files missing in vm<br/>");
            	log("Found " + missingInVm + " files missing in vm");
            }
            if (needsMerge > 0) {
            	out.println("Found " + needsMerge + " files that needs merging<br/>");
            	log("Found " + needsMerge + " files that needs merging");
			}            
            if (diffVmSpecific > 0) {
            	out.println("Found " + diffVmSpecific + " VM specific differences<br/>");
            	log("Found " + diffVmSpecific + " VM specific differences");
			}            
            if (diffClasspathBugfix > 0) {
            	out.println("Found " + diffClasspathBugfix + " local classpath bugfixes<br/>");
            	log("Found " + diffClasspathBugfix + " local classpath bugfixes");
			}            
            
            reportFooter(out);
            out.flush();
            out.close();
        } catch (IOException ex) {
            throw new BuildException(ex);
        } catch (InterruptedException ex) {
            throw new BuildException(ex);
        }
    }
    
    protected int runDiff(JavaFile vmFile, JavaFile cpFile, String diffFileName) throws IOException, InterruptedException {
        final String[] cmd = {
              "diff",
              "-b", // Ignore white space change
              "-au", 
              "-I", ".*$" + "Id:.*$.*", // Avoid cvs keyword expansion in this string
              vmFile.getFileName(),
              cpFile.getFile().getAbsolutePath()
        };
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        final PumpStreamHandler streamHandler = new PumpStreamHandler(out, err);
        final Execute exe = new Execute(streamHandler);
        exe.setCommandline(cmd);
        exe.setWorkingDirectory(vmFile.getBaseDir());
        final int rc = exe.execute();
        if (rc != 0) {
            File diffFile = new File(destDir, diffFileName);
            FileOutputStream os = new FileOutputStream(diffFile);
            try {
                final byte[] diff = out.toByteArray();
                os.write(diff);
                os.flush();
                
                final String diffStr = new String(diff);
                if (diffStr.indexOf(vmSpecificTag) >= 0) {
                    return VM_SPECIFIC;
                } else if (diffStr.indexOf(classpathBugfixTag) >= 0) {
                    return CLASSPATH_BUGFIX;
                } else {
                    return NEEDS_MERGE;
                }
            } finally {
                os.close();
            }
        } else {
            return NO_CHANGE;
        }
    }
    
    
    protected void reportHeader(PrintWriter out) {
        out.println("<html>");
        out.println("<title>Classpath compare</title>");
        out.println("<style type='text/css'>");
        out.println(".classpath-only   { background-color: #FFFFAA; }");
        out.println(".vm-only          { background-color: #CCCCFF; }");
        out.println(".needsmerge       { background-color: #FF9090; }");
        out.println(".vm-specific      { background-color: #22FF22; }");
        out.println(".classpath-bugfix { background-color: #CCFFCC; }");
        out.println("</style>");
        out.println("<body>");
        out.println("<h1>Classpath compare results</h1>");
        out.println("Created at " + new Date());
        out.println("<table border='1' width='100%' style='border: solid 1'>");        
        out.println("<tr>");
        out.println("<th align='left'>Class</th>");
        out.println("<th align='left'>Merge status</th>");
        out.println("</tr>");
    }
    
    protected void reportMissing(PrintWriter out, String fname, String existsIn) {
        out.println("<tr class='" + existsIn + "-only'>");
        out.println("<td>" + fname + "</td>");
        out.println("<td>Exists only in " + existsIn + "</td>");
        out.println("</tr>");
    }
    
    protected void reportNeedsMerge(PrintWriter out, String fname, String diffFileName) {
        out.println("<tr class='needsmerge'>");
        out.println("<td>" + fname + "</td>");
        out.println("<td><a href='" + diffFileName + "'>Diff</a></td>");
        out.println("</tr>");
    }
    
    protected void reportDiffVmSpecific(PrintWriter out, String fname, String diffFileName) {
        out.println("<tr class='vm-specific'>");
        out.println("<td>" + fname + "</td>");
        out.println("<td>VM specific change. (<a href='" + diffFileName + "'>diff</a>)</td>");
        out.println("</tr>");
    }
    
    protected void reportDiffClasspathBugfix(PrintWriter out, String fname, String diffFileName) {
        out.println("<tr class='classpath-bugfix'>");
        out.println("<td>" + fname + "</td>");
        out.println("<td>Local classpath bugfix. (<a href='" + diffFileName + "'>diff</a>)</td>");
        out.println("</tr>");
    }
    
    protected void reportFooter(PrintWriter out) {
        out.println("</body></html>");
    }
    
    
    public BaseDirs createVmsources() {
        if (vmDirs == null) {
            vmDirs = new BaseDirs();
        }
        return vmDirs;
    }
    
    public BaseDirs createClasspathsources() {
        if (classpathDirs == null) {
            classpathDirs = new BaseDirs();
        }
        return classpathDirs;
    }
    
    private Map scanJavaFiles(BaseDirs baseDirs) {
        TreeMap map = new TreeMap();
        for (Iterator i = baseDirs.getFileSets().iterator(); i.hasNext(); ) {
            final FileSet fs = (FileSet)i.next();

            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            final String[] fNames = ds.getIncludedFiles();
            for (int j = 0; j < fNames.length; j++) {
                final String fName = fNames[j];
                map.put(fName, new JavaFile(ds.getBasedir(), fName));
            }
        }
        return map;
    }
    
    public static class BaseDirs {
        
        private final ArrayList fileSets = new ArrayList();

        public List getFileSets() {
            return fileSets;
        }
        
        public void addFileset(FileSet fs) {
            fileSets.add(fs);
        }
    }    
    
    /**
     * @return Returns the destDir.
     */
    public final File getDestDir() {
        return this.destDir;
    }
    /**
     * @param destDir The destDir to set.
     */
    public final void setDestDir(File destDir) {
        this.destDir = destDir;
    }
    
    /**
     * @param classpathBugfixTag The classpathBugfixTag to set.
     */
    public final void setClasspathBugfixTag(String classpathBugfixTag) {
        this.classpathBugfixTag = classpathBugfixTag;
    }
    /**
     * @param vmSpecificTag The vmSpecificTag to set.
     */
    public final void setVmSpecificTag(String vmSpecificTag) {
        this.vmSpecificTag = vmSpecificTag;
    }
}
