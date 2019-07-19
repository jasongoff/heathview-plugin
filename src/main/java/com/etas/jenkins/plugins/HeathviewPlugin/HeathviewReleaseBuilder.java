package com.etas.jenkins.plugins.HeathviewPlugin;
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
    		
    		String tempFilePath = "${WORKSPACE}/HVRelease.XML";
            
            HeathviewReleaseTask task = new HeathviewReleaseTask()
                                .withBeginOutput(beginOutput)
                                .withListener(listener)
                                .withFilepath(build.getEnvironment(listener).expand(tempFilePath))
                                .withBuildName(build.getEnvironment(listener).expand(buildName))
                                .withPatchOrder(build.getEnvironment(listener).expand(patchOrder));

            result = launcher.getChannel().call(task);
		} catch (Exception e) {

			listener.getLogger().println("Failed to invoke 'HeathviewReleaseTask': " + e.getMessage());
			e.printStackTrace(listener.getLogger());
			return false;
		} 
    	
        return result;
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }


    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Heathview: Create Release File header or footer XML section.";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
           
            save();
            return super.configure(req,formData);
        }
        
    }
}

