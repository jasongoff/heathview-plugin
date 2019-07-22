package com.etas.jenkins.plugins.heathviewplugin;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

public final class HeathviewExeInstallation extends ToolInstallation implements NodeSpecific<HeathviewExeInstallation>, EnvironmentSpecific<HeathviewExeInstallation> {

  private static final long serialVersionUID = 1L;
    private transient String pathToExe;

    private final String defaultArgs;

    @DataBoundConstructor
    public HeathviewExeInstallation(String name, String home, String defaultArgs) {
        super(name, home, null);
        this.defaultArgs = Util.fixEmptyAndTrim(defaultArgs);
    }

    @Override
    public HeathviewExeInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new HeathviewExeInstallation(getName(), translateFor(node, log), this.defaultArgs);
    }

    @Override
    public HeathviewExeInstallation forEnvironment(EnvVars environment) {
        return new HeathviewExeInstallation(this.getName(), environment.expand(getHome()), this.defaultArgs);
    }

    @Override
    protected Object readResolve() {
        if (this.pathToExe != null) {
            return new HeathviewExeInstallation(this.getName(), this.pathToExe, this.defaultArgs);
        }
        return this;
    }

    public String getDefaultArgs() {
        return this.defaultArgs;
    }

    public static Node workspaceToNode(FilePath workspace) {
        Jenkins j = Jenkins.getInstance();
        if (workspace != null && workspace.isRemote()) {
            for (Computer c : j.getComputers()) {
                if (c.getChannel() == workspace.getChannel()) {
                    Node n = c.getNode();
                    if (n != null) {
                        return n;
                    }
                }
            }
        }
        return j;
    }
    
    @Extension
    public static class DescriptorImpl extends ToolDescriptor<HeathviewExeInstallation> {

        @Override
        public String getDisplayName() {
            return "Heathview";
        }

        @Override
        public HeathviewExeInstallation[] getInstallations() {
            return Jenkins.getInstance().getDescriptorByType(HeathviewExeBuilder.DescriptorImpl.class).getInstallations();
        }

        @Override
        public void setInstallations(HeathviewExeInstallation... installations) {
            Jenkins.getInstance().getDescriptorByType(HeathviewExeBuilder.DescriptorImpl.class).setInstallations(installations);
        }

    }
}