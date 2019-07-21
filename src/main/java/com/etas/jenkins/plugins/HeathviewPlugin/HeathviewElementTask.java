package com.etas.jenkins.plugins.heathviewplugin;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import jenkins.security.Roles;

import org.jenkinsci.remoting.RoleChecker;

public class HeathviewElementTask implements Serializable,Callable<Boolean,IOException> {

	private static final long serialVersionUID = 1L;
	private String patchFilePath;
	private BuildListener listener;
	private String elementName;
	private boolean beginOutput;
	
	public HeathviewElementTask(){
	}

	public HeathviewElementTask withListener(BuildListener listener) {
		this.listener = listener;
		return this;
	}

	public HeathviewElementTask withPatchFilepath(String filePath) {
		this.patchFilePath = filePath;
		return this;
	}

	public HeathviewElementTask withElementName(String elementName) {
		this.elementName = elementName;
		return this;
	}

	public HeathviewElementTask withBeginOutput(boolean beginOutput) {
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
			String finalFileContent = "";
			String eol = System.getProperty("line.separator");
			boolean patchFileExists = patchFile.exists();
					
			if (!patchFileExists) {
				listener.getLogger().println("\nERROR: Cannot create Heathview Element as there is no preceding Create Heathview Patch File section.");
				return false;
			}

			finalFileContent = patchFile.readToString().concat(eol);
			if (beginOutput) {
				finalFileContent = finalFileContent.concat(String.format("\t\t<Element name='%s'>\n", elementName));
			} else {
				finalFileContent = finalFileContent.concat("\t\t</Element>\n");
			}			
			patchFile.write(finalFileContent, "UTF-8");
		} catch (Exception e) {
			listener.getLogger().println("Failed to create/update file. " + e.getMessage());
			e.printStackTrace(listener.getLogger());	
			return false;
		} 
		listener.getLogger().println("HEATHVIEW: Element successfully added at "+ patchFilePath);
		return true;
	}


}
