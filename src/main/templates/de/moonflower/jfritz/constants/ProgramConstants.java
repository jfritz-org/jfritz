package de.moonflower.jfritz.constants;

public final class ProgramConstants {
    public static final String PROGRAM_NAME = "${capitalizedName}";
    public static final String PROGRAM_VERSION_LONG = "${app.majorVersion}.${app.minorVersion}.${app.patchVersion}.${git.shortRevision}";
    public static final String REVISION = "${git.shortRevision}";
    public static final String BUILD_DATE = "${buildDateTime}";

    public static final String MAJOR = "${app.majorVersion}";
    public static final String MINOR = "${app.minorVersion}";
    public static final String PATCH = "${app.patchVersion}";
    public static final String VERSION_POSTFIX = "${app.version.postfix}";

    public static final String PROGRAM_VERSION = MAJOR + "." + MINOR + "." + PATCH;
}
