package com.etas.jenkins.plugins.heathviewplugin;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class HeathviewReleaseBuilder extends Builder {
    public static final String FILE_NAME = "${WORKSPACE}/HVRelease.XML";
    private final String patchOrder;
    private final String buildName;
    private final boolean beginOutput;

    

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HeathviewReleaseBuilder(boolean beginOutput, String patchOrder, String buildName) {
        this.patchOrder = patchOrder;
        this.buildName =  buildName;   
        this.beginOutput = beginOutput;     
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String patchOrder() {
        return patchOrder;
    }
    
    public String getBuildName(){
    	return buildName;
    }

    public boolean getBeginOutput() {
        return beginOutput;
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    	boolean result = false;
    	try {
            HeathviewReleaseTask task = new HeathviewReleaseTask()
                                .withBeginOutput(beginOutput)
                                .withListener(listener)
                                .withFilepath(build.getEnvironment(listener).expand(HeathviewReleaseBuilder.FILE_NAME))
                                .withBuildName(build.getEnvironment(listener).expand(buildName))
                                .withPatchOrder(patchOrder);

            result = launcher.getChannel().call(task);
		} catch (Exception e) {

			listener.getLogger().println("HEATHVIEW: Failed to invoke 'HeathviewReleaseTask': " + e.getMessage());
			e.printStackTrace(listener.getLogger());
			return false;
		} 
    	
        return result;
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            super(HeathviewReleaseBuilder.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
		public HeathviewReleaseBuilder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return req.bindJSON(HeathviewReleaseBuilder.class, formData);
        }
        
        @Override
        public String getDisplayName() {
            return "Heathview: Create Release File header or footer XML section.";
        }        
    }
}

