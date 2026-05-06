package net.buildabrowser.ak4j;

public final class AK4J {
    
    private static final int MAJOR_VERSION = 0;
    private static final int MINOR_VERSION = 1;
    private static final int PATCH_VERSION = 0;

    public static int majorVersion() {
        return MAJOR_VERSION;
    }

    public static int minorVersion() {
        return MINOR_VERSION;
    }

    public static int patchVersion() {
        return PATCH_VERSION;
    }

    public static String versionString() {
        return majorVersion() + "." + minorVersion() + "." + patchVersion();
    }

    public static void init() {
        
    }

}
