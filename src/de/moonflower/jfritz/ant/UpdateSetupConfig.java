package de.moonflower.jfritz.ant;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Used file format
 *
#define AppName "JFritz"
#define AppVersion "0.7.2.23"
;AppType can be "Beta-Setup" or "Release-Setup"
#define AppType "Beta-Setup"
**/

public class UpdateSetupConfig {
	String appVersion = "";
	String path = "";
	String appName = "";
	String appType = "";

	public void setFile(String path)
	{
		this.path = path;
	}

	public void setAppName(String appName)
	{
		this.appName = appName;
	}

	public void setAppVersion(String version) {
		this.appVersion = version;
    }

	public void setAppType(String appType)
	{
		this.appType = appType;
	}

	public void execute() {
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(path));
			out.write("#define AppName \"" + appName + "\"\n");
			out.write("#define AppVersion \"" + appVersion + "\"\n");
			out.write(";AppType can be \"Beta-Setup\" or \"Release-Setup\"\n");
			out.write("#define AppType \"" + appType + "\"\n");
			out.close();
			} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
