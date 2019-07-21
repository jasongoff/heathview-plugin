package com.etas.jenkins.plugins.heathviewplugin;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import jenkins.security.Roles;

import org.jenkinsci.remoting.RoleChecker;

public class HeathviewPatchTask implements Serializable,Callable<Boolean,IOException> {

	private static final long serialVersionUID = 1L;
	private String patchFilePath;
	private String releaseFilePath;
	private BuildListener listener;
	private String buildName;
	private boolean beginOutput;
	
	public HeathviewPatchTask(){
	}

	public HeathviewPatchTask withListener(BuildListener listener) {
		this.listener = listener;
		return this;
	}

	public HeathviewPatchTask withPatchFilepath(String filePath) {
		this.patchFilePath = filePath;
		return this;
	}

	public HeathviewPatchTask withReleaseFilepath(String filePath) {
		this.releaseFilePath = filePath;
		return this;
	}

	public HeathviewPatchTask withBuildName(String buildName) {
		this.buildName = buildName;
		return this;
	}

	public HeathviewPatchTask withBeginOutput(boolean beginOutput) {
		this.beginOutput = beginOutput;
		return this;
	}

	@Override
	public void checkRoles(RoleChecker checker) throws SecurityException {
		checker.check(this, Roles.SLAVE);		
	}

	@Override
	public Boolean call() throws IOException {
		listener.getLogger().println("\nHEATHVIEW: Beginning Patch File task");
		try {
			FilePath patchFile = new FilePath(new File(patchFilePath));
			FilePath releaseFile = new FilePath(new File(releaseFilePath));
			String finalFileContent = "";
			String releaseFileContent = "";
			boolean patchFileExists = patchFile.exists();
			boolean releaseFileExists = releaseFile.exists();
					
			if (releaseFileExists) {
				releaseFileContent = releaseFile.readToString();
			} else {
				listener.getLogger().println("\nERROR: Cannot create Heathview Patch file as there is no preceding Create Heathview Release File section.");
				return false;
			}

			if (beginOutput) {
				if (patchFileExists) patchFile.deleteContents();
				finalFileContent = finalFileContent.concat("<?xml version=\"1.0\" encoding=\"us-ascii\"?>\n")
						.concat("<Heath>\n")
						.concat(String.format("\t<Patch create='open' name='%s'>\n", buildName));

				releaseFileContent = releaseFileContent.concat(String.format("\t\t<Patch create='open' name='%s'/>\n", buildName));
			} else {
				if (patchFileExists) {
					finalFileContent = patchFile.readToString();
					finalFileContent = finalFileContent.concat("\t</Patch>\n</Heath>\n").replaceAll("\n", System.lineSeparator());
				} else {
					listener.getLogger().println("\nERROR: Cannot close Heathview Patch File as there is no corresponding opening section or the file does not exist.");
					return false;
				}
			}			
			patchFile.write(finalFileContent, "UTF-8");
			releaseFile.write(releaseFileContent, "UTF-8");
			
		} catch (Exception e) {

			listener.getLogger().println("Failed to create/update file. " + e.getMessage());
			e.printStackTrace(listener.getLogger());	
			return false;
		} 
		return true;
	}


}
