package net.happygiraffe.jslint.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import net.happygiraffe.jslint.Issue;
import net.happygiraffe.jslint.JSLint;
import net.happygiraffe.jslint.Option;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * Run {@link JSLint} over a tree of files in order to pick holes in your
 * JavaScript. This task defaults to reading *.js files.
 *
 * <p>
 * Example build.xml usage:
 *
 * <pre>
 * &lt;taskdef name=&quot;jslint&quot; classname=&quot;net.happygiraffe.jslint.ant.JSLintTask&quot; /&gt;
 * &lt;jslint dir=&quot;web/js&quot; /&gt;
 * </pre>
 *
 * @author dom
 * @version $Id$
 * @see <a href="http://jslint.com/">jslint.com</a>
 */
public class JSLintTask extends MatchingTask {

    private File dir;

    private JSLint lint;

    /**
     * Specify a directory to scan for JavaScript problems.
     *
     * @param dir
     */
    public void setDir(File dir) {
        this.dir = dir;
    }

    /**
     * Create a new {@link JSLint} object. Set the default includes parameter to
     * <code>**<span>/</span>*.js</code>.
     */
    @Override
    public void init() throws BuildException {
        try {
            lint = new JSLint();
        } catch (IOException e) {
            throw new BuildException(e);
        }

        // Default to "*.js" anywhere in dir.
        setIncludes("**/*.js");
    }

    /**
     * Scan the specified directory for JavaScript files and lint them.
     */
    @Override
    public void execute() throws BuildException {
        if (dir == null)
            throw new BuildException("dir must be specified");

        DirectoryScanner ds = getDirectoryScanner(dir);
        for (String fileName : ds.getIncludedFiles()) {
            lintFile(fileName);
        }

        // Clear out for next time.
        setDir(null);
        lint.resetOptions();
    }

    private void lintFile(String fileName) {
        try {
            File file = new File(dir, fileName);
            log("check " + file, Project.MSG_DEBUG);
            // XXX We should allow specifying the encoding here.
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));
            List<Issue> issues = lint.lint(file.toString(), reader);
            if (issues.size() > 0) {
                for (Issue issue : issues) {
                    log(issue.toString());
                    log(issue.getEvidence());
                    log(spaces(issue.getCharacter()) + "^");
                }
            }
        } catch (FileNotFoundException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Set the options for running JSLint.
     */
    public void setOptions(String optionList) throws BuildException {
        for (String name : optionList.split("\\s*,\\s*")) {
            try {
                // The Option constants are upper case…
                lint.addOption(Option.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BuildException("Unknown option " + name);
            }
        }
    }

    private String spaces(int howmany) {
        StringBuffer sb = new StringBuffer(howmany);
        for (int i = 0; i < howmany; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
