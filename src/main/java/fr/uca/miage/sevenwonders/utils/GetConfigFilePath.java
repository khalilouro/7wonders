package fr.uca.miage.sevenwonders.utils;

import java.nio.file.*;

public class GetConfigFilePath {

    private static final String CONFIG_FILE_NAME = "config.toml";
    private static final String CONFIG_DIR_NAME = "7w";
    private static final String XDG_CONFIG_HOME = "XDG_CONFIG_HOME";
    private static final String APPDATA = "APPDATA";

    private static Path getConfigPathBase(String os) {
        String configDir;
        String userHome = System.getProperty("user.home");

        if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            String xdgConfigHome = System.getenv(XDG_CONFIG_HOME);
            configDir = (xdgConfigHome != null && !xdgConfigHome.isEmpty())
                    ? xdgConfigHome
                    : Paths.get(userHome, ".config").toString();
        } else if (os.contains("win")) {
            String appData = System.getenv(APPDATA);
            configDir = (appData != null && !appData.isEmpty())
                    ? appData
                    : Paths.get(userHome, "AppData", "Roaming").toString();
        } else {
            configDir = Paths.get(userHome, ".config").toString();
        }

        return Paths.get(configDir, CONFIG_DIR_NAME);
    }

    public static Path getConfigFilePath() {
        String os = System.getProperty("os.name").toLowerCase();
        Path configDirPath = getConfigPathBase(os);
        return Paths.get(configDirPath.toString(), CONFIG_FILE_NAME);
    }

    public static Path getConfigDirPath() {
        String os = System.getProperty("os.name").toLowerCase();
        return getConfigPathBase(os);
    }
}
