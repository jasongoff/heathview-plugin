package com.etas.jenkins.plugins.heathviewplugin;

import hudson.AbortException;
import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Plugin;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.ArgumentListBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.annotation.CheckForNull;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

public class HeathviewExeBuilder extends Builder implements SimpleBuildStep {

    private final String exeName;
    private String cmdLineArgs;
    private boolean failBuild = true;
    
    @DataBoundConstructor
    public HeathviewExeBuilder(String exeName, String cmdLineArgs, boolean failBuild) {
        this.exeName = exeName;
        this.cmdLineArgs = cmdLineArgs;
        this.failBuild = failBuild;
    }

    public String getExeName() {
        return exeName;
    }

    @CheckForNull
    public String getCmdLineArgs() {
        return cmdLineArgs;
    }

    public boolean getFailBuild() {
        return failBuild;
    }

    public HeathviewExeInstallation getInstallation() {
        if (exeName == null) {
            return null;
        }
        for (HeathviewExeInstallation i : DESCRIPTOR.getInstallations()) {
            if (exeName.equals(i.getName())) {
                return i;
            }
        }
        return null;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener tl) throws InterruptedException, IOException {
        //public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        ArrayList<String> args = new ArrayList<String>();
        EnvVars env = null;
        HeathviewExeInstallation installation = getInstallation();
        if (installation == null) {
            throw new AbortException("ExeInstallation not found.");
        }
        installation = installation.forNode(HeathviewExeInstallation.workspaceToNode(workspace), tl);

        if (run instanceof AbstractBuild) {
            env = run.getEnvironment(tl);
            installation = installation.forEnvironment(env);
        }

        // exe path.
        String exePath = getExePath(installation, launcher, tl);
        if (isNullOrSpace(exePath)) {
            throw new AbortException("Exe path is blank.");
        }
        args.add(exePath);

        // Default Arguments
        if (!isNullOrSpace(installation.getDefaultArgs())) {
            args.addAll(getArguments(run, workspace, tl, installation.getDefaultArgs()));
        }

        // Manual Command Line String
        if (!isNullOrSpace(cmdLineArgs)) {
            args.addAll(getArguments(run, workspace, tl, cmdLineArgs));
        }

        // exe run.
        exec(args, run, launcher, tl, env, workspace);
    }

    private String getExePath(HeathviewExeInstallation installation, Launcher launcher, TaskListener tl) throws InterruptedException, IOException {
        String pathToExe = installation.getHome();
        FilePath exec = new FilePath(launcher.getChannel(), pathToExe);

        try {
            if (!exec.exists()) {
                tl.fatalError(pathToExe + " doesn't exist");
                return null;
            }
        } catch (IOException e) {
            tl.fatalError("Failed checking for existence of " + pathToExe);
            return null;
        }

        tl.getLogger().println("Path To exe: " + pathToExe);
        return appendQuote(pathToExe);
    }

    private List<String> getArguments(Run<?, ?> run, hudson.FilePath workspace, TaskListener tl, String values) throws InterruptedException, IOException {
        ArrayList<String> args = new ArrayList<String>();
        StringTokenizer valuesToknzr = new StringTokenizer(values, " \t\r\n");

        while (valuesToknzr.hasMoreTokens()) {
            String value = valuesToknzr.nextToken();
            if (run instanceof AbstractBuild) {
                Plugin p = Jenkins.getInstance().getPlugin("token-macro");
                if (null != p && p.getWrapper().isActive()) {
                    try {
                        value = TokenMacro.expandAll(run, workspace, tl, value);
                    } catch (MacroEvaluationException ex) {
                        tl.error("TokenMacro was unable to evaluate: " + value + " " + ex.getMessage());
                    }
                } else {
                    EnvVars envVars = run.getEnvironment(tl);
                    value = envVars.expand(value);
                }
            }
            if (!isNullOrSpace(value)) {
                args.add(value);
            }
        }
        return args;
    }

    private void exec(List<String> args, Run<?, ?> run, Launcher launcher, TaskListener tl, EnvVars env, FilePath workspace) throws InterruptedException, IOException {
        ArgumentListBuilder cmdExecArgs = new ArgumentListBuilder();
        FilePath tmpDir = null;

        if (!launcher.isUnix()) {
            tmpDir = workspace.createTextTempFile("exe_runner_", ".bat", concatString(args), false);
            cmdExecArgs.add("cmd.exe", "/C", tmpDir.getRemote(), "&&", "exit", "%ERRORLEVEL%");
        } else {
            for (String arg : args) {
                cmdExecArgs.add(arg);
            }
        }

        tl.getLogger().println("Executing : " + cmdExecArgs.toStringWithQuote());

        try {
            int r;
            if (run instanceof AbstractBuild) {
                r = launcher.launch().cmds(cmdExecArgs).envs(env).stdout(tl).pwd(workspace).join();
            }
            else{
                r = launcher.launch().cmds(cmdExecArgs).stdout(tl).pwd(workspace).join();   //env vars arent available in pipeline
            }

            if (failBuild) {
                if (r != 0) {
                    throw new AbortException("Exited with code: " + r);
                }
            } else {
                if (r != 0) {
                    tl.getLogger().println("Exe exited with code: " + r);
                    run.setResult(Result.UNSTABLE);
                }
            }
        } finally {
            if (tmpDir != null) {
                tmpDir.delete();
            }
        }
    }

    @Override
    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @Symbol("runexe")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public static final boolean DEFAULTFAILBUILD = true;
        @CopyOnWrite
        private volatile HeathviewExeInstallation[] installations = new HeathviewExeInstallation[0];

        public DescriptorImpl() {
            super(HeathviewExeBuilder.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Heathview: Run HVCMD to load the release.";
        }

        public HeathviewExeInstallation[] getInstallations() {
            return installations;
        }

        public void setInstallations(HeathviewExeInstallation... installations) {
            this.installations = installations;
            save();
        }

        /**
         * Obtains the {@link ExeInstallation.DescriptorImpl} instance.
         */
        public HeathviewExeInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(HeathviewExeInstallation.DescriptorImpl.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

  public static String appendQuote(String value) {
      return String.format("\"%s\"", value);
  }

  /**
   * Null or Space
   * @param value
   * @return
   */
  public static boolean isNullOrSpace(String value) {
      return (value == null || value.trim().length() == 0);
  }

  /**
   *
   * @param args
   * @return
   */
  public static String concatString(List<String> args) {
      StringBuilder buf = new StringBuilder();
      for (String arg : args) {
          if(buf.length() > 0)  buf.append(' ');
          buf.append(arg);
      }
      return buf.toString();
  }
}