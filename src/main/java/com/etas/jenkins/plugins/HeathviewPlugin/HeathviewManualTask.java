package com.etas.jenkins.plugins.heathviewplugin;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import jenkins.security.Roles;

import org.jenkinsci.remoting.RoleChecker;

public class HeathviewManualTask implements Serializable,Callable<Boolean,IOException> {

	private static final long serialVersionUID = 1L;
	private String filePath;
	private BuildListener listener;
	private String taskType;
	private String taskDetail;
	
	public HeathviewManualTask(){
	}

	public HeathviewManualTask withListener(BuildListener listener) {
		this.listener = listener;
		return this;
	}

	public HeathviewManualTask withFilepath(String filePath) {
		this.filePath = filePath;
		return this;
	}

	public HeathviewManualTask withManualTaskType(String taskType) {
		this.taskType = taskType;
		return this;
	}

	public HeathviewManualTask withTaskDetail(String taskDetail) {
		this.taskDetail = taskDetail;
		return this;
	}

	@Override
	public void checkRoles(RoleChecker checker) throws SecurityException {
		checker.check(this, Roles.SLAVE);		
	}

	@Override
	public Boolean call() throws IOException {
		listener.getLogger().println("\nHEATHVIEW: Beginning Manual task");
		try {
			FilePath textFile = new FilePath(new File(filePath));
			String finalFileContent = "";
			boolean fileExists = textFile.exists();
					
			if (fileExists) {
				finalFileContent = textFile.readToString()
					.concat(String.format("\t\t<%s task='%s'/>\n", taskType, taskDetail));
			} else {
				listener.getLogger().println("\nERROR: Cannot create Heathview Manual Task as there is no previous Release File section.");
				return false;
			}
			textFile.write(finalFileContent, "UTF-8");
			
		} catch (Exception e) {

			listener.getLogger().println("Failed to create/update file. " + e.getMessage());
			e.printStackTrace(listener.getLogger());	
			return false;
		} 
		return true;
	}


}
