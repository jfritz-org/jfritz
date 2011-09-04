package de.moonflower.jfritz.ant.version;

import org.apache.tools.ant.Project;

import de.moonflower.jfritz.constants.ProgramConstants;

public class GetJFritzVersion {
	Project project;

	public void setProject(Project proj) {
        project = proj;
    }

	public void execute() {
		project.setProperty("version", ProgramConstants.PROGRAM_VERSION);
	}

}
