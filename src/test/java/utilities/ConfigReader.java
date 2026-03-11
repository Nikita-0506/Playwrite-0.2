package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//@Author: neha.verma@inadev.com
//@Date: 12 July 2025
//@Desc: This class holds configuration file access methods


public class ConfigReader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in classpath!");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        // First check if system property exists (for Maven runtime parameters)
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue;
        }
        // Fall back to config.properties file
        return properties.getProperty(key);
    }
}
