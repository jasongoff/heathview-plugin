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

public class HeathviewPatchBuilder extends Builder  {

    public static final String FILE_NAME="${WORKSPACE}/HVPatch.XML";
    private final String buildName;
    private final boolean beginOutput;

    @DataBoundConstructor
    public HeathviewPatchBuilder(boolean beginOutput, String buildName) {
        this.buildName =  buildName;   
        this.beginOutput = beginOutput;     
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
            HeathviewPatchTask task = new HeathviewPatchTask()
                                .withBeginOutput(beginOutput)
                                .withListener(listener)
                                .withPatchFilepath(build.getEnvironment(listener).expand(HeathviewPatchBuilder.FILE_NAME))
                                .withReleaseFilepath(build.getEnvironment(listener).expand(HeathviewReleaseBuilder.FILE_NAME))
                                .withBuildName(build.getEnvironment(listener).expand(buildName));

            result = launcher.getChannel().call(task);
		} catch (Exception e) {

			listener.getLogger().println("\nHEATHVIEW: Failed to invoke 'HeathviewPatchTask': " + e.getMessage());
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
            super(HeathviewPatchBuilder.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
		public HeathviewPatchBuilder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return req.bindJSON(HeathviewPatchBuilder.class, formData);
        }
        
        @Override
        public String getDisplayName() {
            return "Heathview: Create Patch File header or footer XML section.";
        }        
    }
}

