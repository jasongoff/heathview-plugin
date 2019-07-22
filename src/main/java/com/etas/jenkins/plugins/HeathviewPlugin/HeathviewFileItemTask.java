package com.etas.jenkins.plugins.heathviewplugin;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import jenkins.security.Roles;

import org.jenkinsci.remoting.RoleChecker;

public class HeathviewFileItemTask implements Serializable,Callable<Boolean,IOException> {

	private static final long serialVersionUID = 1L;
	private String patchFilePath;
	private BuildListener listener;
	private String source;
	private String target;
	private String type;
	
	public HeathviewFileItemTask(){
	}

	public HeathviewFileItemTask withListener(BuildListener listener) {
		this.listener = listener;
		return this;
	}

	public HeathviewFileItemTask withPatchFilepath(String filePath) {
		this.patchFilePath = filePath;
		return this;
	}

	public HeathviewFileItemTask withSource(String source) {
		this.source = source;
		return this;
	}
	public HeathviewFileItemTask withTarget(String target) {
		this.target = target;
		return this;
	}
	public HeathviewFileItemTask withType(String type) {
		this.type = type;
		return this;
	}

	@Override
	public void checkRoles(RoleChecker checker) throws SecurityException {
		checker.check(this, Roles.SLAVE);		
	}

	@Override
	public Boolean call() throws IOException {
		listener.getLogger().println("\nHEATHVIEW: Beginning FileItem task");
		try {
			FilePath patchFile = new FilePath(new File(patchFilePath));
			String finalFileContent = "";
			boolean patchFileExists = patchFile.exists();
					
			if (!patchFileExists) {
				listener.getLogger().println("\nERROR: Cannot create Heathview FileItem as there is no preceding Create Heathview Patch File section.");
				return false;
			}

			finalFileContent = patchFile.readToString();
			finalFileContent = finalFileContent.concat(String.format("\t\t\t<FileItem source='%s' target='%s' type='%s' />\n", source, target, type));

			patchFile.write(finalFileContent, "UTF-8");
		} catch (Exception e) {
			listener.getLogger().println("Failed to create/update file. " + e.getMessage());
			e.printStackTrace(listener.getLogger());	
			return false;
		} 
		return true;
	}


}
