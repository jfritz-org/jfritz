package de.moonflower.jfritz.utils;

import java.io.File;
import java.io.FilenameFilter;
/**
 *  @author Bastian Schaefer
 */
public class StartEndFilenameFilter implements FilenameFilter {
	String ext;
	String start;
	public StartEndFilenameFilter(String start,String ext) {
		this.ext = "." + ext;
		this.start = start;
	}
	public boolean accept(File dir, String name) {
		return (name.startsWith(start) && name.endsWith(ext));
	}
}