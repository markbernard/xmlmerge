package xml.web.task;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import xml.web.MergeWebXml;

/**
 * ANT task to merge 2 web.xml files
 * 
 */
public class MergeWebXmlTask extends Task {
    private String srcfile1;
    private String srcfile2;
    private String destfile;
    private String conflictfile;

    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        if(srcfile1 == null) {
            throw new BuildException("srcfile1 attribute is required");
        }
        if(srcfile2 == null) {
            throw new BuildException("srcfile2 attribute is required");
        }
        if(destfile == null) {
            throw new BuildException("destfile attribute is required");
        }
        try {
            MergeWebXml mergeWebXml = new MergeWebXml(srcfile1, srcfile2, destfile, conflictfile);
            mergeWebXml.runJob();
        }
        catch (Exception e) {
            throw new BuildException(e.getMessage()+ " Unable to merge " + srcfile1 + " and " + srcfile2 + " into " + destfile + " with conflict file " + conflictfile, e);
        }
    }

    /**
     * @return the srcfile1
     */
    public String getSrcfile1() {
        return srcfile1;
    }

    /**
     * @param srcfile1 the srcfile1 to set
     */
    public void setSrcfile1(String srcfile1) {
        this.srcfile1 = srcfile1;
    }

    /**
     * @return the srcfile2
     */
    public String getSrcfile2() {
        return srcfile2;
    }

    /**
     * @param srcfile2 the srcfile2 to set
     */
    public void setSrcfile2(String srcfile2) {
        this.srcfile2 = srcfile2;
    }

    /**
     * @return the destfile
     */
    public String getDestfile() {
        return destfile;
    }

    /**
     * @param destfile the destfile to set
     */
    public void setDestfile(String destfile) {
        this.destfile = destfile;
    }

    /**
     * @return the conflictfile
     */
    public String getConflictfile() {
        return conflictfile;
    }

    /**
     * @param conflictfile the conflictfile to set
     */
    public void setConflictfile(String conflictfile) {
        this.conflictfile = conflictfile;
    }

}
