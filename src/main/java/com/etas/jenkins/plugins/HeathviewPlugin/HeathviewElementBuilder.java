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

public class HeathviewElementBuilder extends Builder  {

    public static final String FILE_NAME="${WORKSPACE}/HVPatch.XML";
    private final String elementName;
    private final boolean beginOutput;

    @DataBoundConstructor
    public HeathviewElementBuilder(boolean beginOutput, String elementName) {
        this.elementName =  elementName;   
        this.beginOutput = beginOutput;     
    }

    public String getElementName(){
    	return elementName;
    }

    public boolean getBeginOutput() {
        return beginOutput;
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    	boolean result = false;
    	try {
            HeathviewElementTask task = new HeathviewElementTask()
                                .withBeginOutput(beginOutput)
                                .withListener(listener)
                                .withPatchFilepath(build.getEnvironment(listener).expand(HeathviewPatchBuilder.FILE_NAME))
                                .withElementName(build.getEnvironment(listener).expand(elementName));

            result = launcher.getChannel().call(task);
		} catch (Exception e) {

			listener.getLogger().println("\nHEATHVIEW: Failed to invoke 'HeathviewElementTask': " + e.getMessage());
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
            super(HeathviewElementBuilder.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
		public HeathviewElementBuilder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			return req.bindJSON(HeathviewElementBuilder.class, formData);
        }
        
        @Override
        public String getDisplayName() {
            return "Heathview: Create Element header or footer section.";
        }        
    }
}

