/*
The MIT License (MIT)

Copyright (c) 2015 Sanketh P B

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.etas.jenkins.plugins.HeathviewPlugin;

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

	
	public HeathviewReleaseTask(){
	}

	@Override
	public void checkRoles(RoleChecker checker) throws SecurityException {
		checker.check(this, Roles.SLAVE);		
	}

	@Override
	public Boolean call() throws IOException {
		
		try {
			FilePath textFile = new FilePath(new File(filePath));
			String finalFileContent = "";
			String eol = System.getProperty("line.separator");
			
			if(textFile.exists()){
				listener.getLogger().println(String.format("File already exists at '%s'", filePath));
				if (beginOutput) 
					textFile.deleteContents();
				else
				 finalFileContent = textFile.readToString().concat(eol);
			}

			if (beginOutput) {
			finalFileContent = finalFileContent.concat("<?xml version=\"1.0\" encoding=\"us-ascii\"?>\n")
					.concat("<Heath>\n")
					.concat(String.format("<Release create='open' name='%s'>\n", buildName))
					.concat(String.format("<Patch create='open' name='%s'/>\n", buildName))
					.concat(String.format("<Order type='%s'>\n", patchOrder));
			} else {
				finalFileContent = finalFileContent.concat("</Order>\n</Release>\n</Heath>\n")
															.replaceAll("\n", System.lineSeparator());

			}
			
			listener.getLogger().println(String.format("File content is:\n %s", finalFileContent));
			textFile.write(finalFileContent, "UTF-8");
			
		} catch (Exception e) {

			listener.getLogger().println("Failed to create/update file. " + e.getMessage());
			e.printStackTrace(listener.getLogger());	
			return false;
		} 
		listener.getLogger().println("File successfully created/updated at "+ filePath);
		return true;
	}


}
