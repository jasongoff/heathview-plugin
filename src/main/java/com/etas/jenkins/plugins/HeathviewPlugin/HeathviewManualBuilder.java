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

public class HeathviewManualBuilder extends Builder {
    public static final String FILE_NAME = "${WORKSPACE}/HVRelease.XML";
    private final String manualTaskType;
    private final String taskDetail;


    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HeathviewManualBuilder(String manualTaskType, String taskDetail) {
        this.manualTaskType = manualTaskType;
        this.taskDetail =  taskDetail;   
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getManualTaskType() {
        return manualTaskType;
    }
    
    public String getTaskDetail(){
    	return taskDetail;
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    	boolean result = false;
    	try {
            HeathviewManualTask task = new HeathviewManualTask()
                                .withManualTaskType(manualTaskType)
                                .withTaskDetail(build.getEnvironment(listener).expand(taskDetail))
                                .withListener(listener)
                                .withFilepath(build.getEnvironment(listener).expand(HeathviewReleaseBuilder.FILE_NAME));

            result = launcher.getChannel().call(task);
		} catch (Exception e) {

			listener.getLogger().println("HEATHVIEW: Failed to invoke 'HeathviewManualTask': " + e.getMessage());
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
            super(HeathviewManualBuilder.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
		public HeathviewManualBuilder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return req.bindJSON(HeathviewManualBuilder.class, formData);
        }
        
        @Override
        public String getDisplayName() {
            return "Heathview: Create Manual Task in Release.";
        }        
    }
}

