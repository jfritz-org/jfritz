package de.moonflower.jfritz;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProgramConstants {
	// when changing this, don't forget to check the resource bundles!!
	public final static String PROGRAM_NAME = "JFritz"; //$NON-NLS-1$

	public final static String PROGRAM_VERSION = longStringVersion();

	// SVN should fill this out with the latest tag when it's checked out.

	private static final String APP_SVNURL_RAW = "$HeadURL: svn+ssh://user@host/svnroot/app/trunk/src/de.moonflower.jfritz.ProgramConstants.java $";
	private static final String APP_SVN_REVISION_RAW = "$Revision: 325 $";

	private static final Pattern SVNBRANCH_PAT = Pattern
			.compile("(branches|trunk|releases)\\/([\\w\\.\\-]+)\\/.*");
	private static final String APP_SVNTAIL = APP_SVNURL_RAW.replaceFirst(
			".*\\/svnroot\\/app\\/", "");

	private static final String APP_BRANCHTAG;
	private static final String APP_BRANCHTAG_NAME;
	private static final String APP_SVNREVISION = APP_SVN_REVISION_RAW
			.replaceAll("\\$Revision:\\s*", "").replaceAll("\\s*\\$", "");

	static {
		Matcher m = SVNBRANCH_PAT.matcher(APP_SVNTAIL);
		if (!m.matches()) {
			APP_BRANCHTAG = "[Broken SVN Info]";
			APP_BRANCHTAG_NAME = "[Broken SVN Info]";
		} else {
			APP_BRANCHTAG = m.group(1);
			if (APP_BRANCHTAG.equals("trunk")) {
				// this isn't necessary in this SO example, but it
				// is since we don't call it trunk in the real case
				APP_BRANCHTAG_NAME = "trunk";
			} else {
				APP_BRANCHTAG_NAME = m.group(2);
			}
		}
	}

	public static String tagOrBranchName() {
		return APP_BRANCHTAG_NAME;
	}

	/**
	 * Answers a formatter String descriptor for the app version.
	 *
	 * @return version string
	 */
	public static String longStringVersion() {
		return "app " + tagOrBranchName() + " (" + tagOrBranchName()
				+ ", svn revision=" + svnRevision() + ")";
	}

	public static String shortStringVersion() {
		return tagOrBranchName();
	}

	public static String svnVersion() {
		return APP_SVNURL_RAW;
	}

	public static String svnRevision() {
		return APP_SVNREVISION;
	}

	public static String svnBranchId() {
		return APP_BRANCHTAG + "/" + APP_BRANCHTAG_NAME;
	}

	public static final String banner() {
		StringBuilder sb = new StringBuilder();
		sb
				.append("\n----------------------------------------------------------------");
		sb.append("\nApplication -- ");
		sb.append(longStringVersion());
		sb
				.append("\n----------------------------------------------------------------\n");
		return sb.toString();
	}
}
