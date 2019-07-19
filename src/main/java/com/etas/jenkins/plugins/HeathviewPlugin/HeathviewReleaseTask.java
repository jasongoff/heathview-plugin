package com.etas.jenkins.plugins.heathviewplugin;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import jenkins.security.Roles;

import org.jenkinsci.remoting.RoleChecker;

public class HeathviewReleaseTask implements Serializable,Callable<Boolean,IOException> {

	private static final long serialVersionUID = 1L;
	private String filePath;
	private String patchOrder;
	private BuildListener listener;
	private String buildName;
	private boolean beginOutput;
	
	public HeathviewReleaseTask(){
	}

	public HeathviewReleaseTask withListener(BuildListener listener) {
		this.listener = listener;
		return this;
	}

	public HeathviewReleaseTask withPatchOrder(String patchOrder) {
		this.patchOrder = patchOrder;
		return this;
	}

	public HeathviewReleaseTask withFilepath(String filePath) {
		this.filePath = filePath;
		return this;
	}

	public HeathviewReleaseTask withBuildName(String buildName) {
		this.buildName = buildName;
		return this;
	}

	public HeathviewReleaseTask withBeginOutput(boolean beginOutput) {
		this.beginOutput = beginOutput;
		return this;
	}

	@Override
	public void checkRoles(RoleChecker checker) throws SecurityException {
		checker.check(this, Roles.SLAVE);		
	}

	@Override
	public Boolean call() throws IOException {
		listener.getLogger().println("\nHEATHVIEW: Beginning Release File task");
		try {
			FilePath textFile = new FilePath(new File(filePath));
			String finalFileContent = "";
			String eol = System.getProperty("line.separator");
			boolean fileExists = textFile.exists();
					
			if (beginOutput) {
				if (fileExists) textFile.deleteContents();
				finalFileContent = finalFileContent.concat("<?xml version=\"1.0\" encoding=\"us-ascii\"?>\n")
						.concat("<Heath>\n")
						.concat(String.format("<Release create='open' name='%s'>\n", buildName))
						.concat(String.format("<Order type='%s'>\n</Order>\n", patchOrder));
			} else {
				if (fileExists) {
					finalFileContent = textFile.readToString().concat(eol);
					finalFileContent = finalFileContent.concat("</Release>\n</Heath>\n")
							.replaceAll("\n", System.lineSeparator());
				} else {
					listener.getLogger().println("\nERROR: Cannot close Heathview Release File as there is no corresponding opening section or the file does not exist.");
					return false;
				}

			}			
			listener.getLogger().println(String.format("File content is:\n %s", finalFileContent));
			textFile.write(finalFileContent, "UTF-8");
			
		} catch (Exception e) {

			listener.getLogger().println("Failed to create/update file. " + e.getMessage());
			e.printStackTrace(listener.getLogger());	
			return false;
		} 
		listener.getLogger().println("HEATHVIEW: Release File successfully created/updated at "+ filePath);
		return true;
	}


}
