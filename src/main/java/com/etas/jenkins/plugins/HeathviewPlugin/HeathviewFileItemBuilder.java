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

public class HeathviewFileItemBuilder extends Builder  {

    public static final String FILE_NAME="${WORKSPACE}/HVPatch.XML";
    private final String source;
    private final String target;
    private final String type;

    @DataBoundConstructor
    public HeathviewFileItemBuilder(String source, String target, String type) {
        this.source = source; 
        this.target = target;
        this.type = type;     
    }

    public String getSource(){
    	return source;
    }

    public String getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    	boolean result = false;
    	try {
            HeathviewFileItemTask task = new HeathviewFileItemTask()
                                .withListener(listener)
                                .withPatchFilepath(build.getEnvironment(listener).expand(HeathviewPatchBuilder.FILE_NAME))
                                .withSource(build.getEnvironment(listener).expand(source))
                                .withTarget(build.getEnvironment(listener).expand(target))
                                .withType(build.getEnvironment(listener).expand(type));

            result = launcher.getChannel().call(task);
		} catch (Exception e) {

			listener.getLogger().println("\nHEATHVIEW: Failed to invoke 'HeathviewFileItemTask': " + e.getMessage());
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
            super(HeathviewFileItemBuilder.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
		public HeathviewFileItemBuilder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return req.bindJSON(HeathviewFileItemBuilder.class, formData);
        }
        
        @Override
        public String getDisplayName() {
            return "Heathview: Create FileItem section.";
        }        
    }
}

