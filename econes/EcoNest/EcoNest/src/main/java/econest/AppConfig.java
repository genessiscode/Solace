package econest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads application settings from {@code config.properties} on the classpath.
 *
 * <p>If the file is absent - which is the normal state after a fresh clone,
 * because {@code config.properties} is gitignored - every lookup falls back to
 * the caller-supplied default. The application therefore still starts on a
 * machine that has not been configured, provided the defaults are usable.</p>
 */
public final class AppConfig {

    private static final String CONFIG_FILE = "config.properties";

    private static final Properties PROPERTIES = load();

    private AppConfig() {
        // utility class - not instantiable
    }

    /**
     * Reads a setting.
     *
     * @param key      the property name, for example {@code db.url}
     * @param fallback the value to use when the file or the key is missing
     * @return the configured value, or {@code fallback}; never {@code null}
     *         unless {@code fallback} itself is {@code null}
     */
    public static String get(String key, String fallback) {

        String value = PROPERTIES.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    /**
     * Loads the properties file from the classpath once, at class
     * initialisation. A missing file is not an error.
     */
    private static Properties load() {

        Properties properties = new Properties();

        try (InputStream input = AppConfig.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (input != null) {
                properties.load(input);
            }

        } catch (IOException e) {

            System.out.println(
                    "Warning: could not read " + CONFIG_FILE
                            + " - using defaults. (" + e.getMessage() + ")"
            );
        }

        return properties;
    }
}
