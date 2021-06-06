package by.bsuir.audio_stream.client.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;

public final class ClientConfiguration {

    private static final Logger logger = LogManager.getLogger(ClientConfiguration.class);

    private static ClientConfiguration instance;

    private final Properties properties;
    private final URL serverUrl;
    private final Path fxmlDirectoryPath;

    private ClientConfiguration() {
        properties = loadProperties();
        serverUrl = initServerUrl();
        fxmlDirectoryPath = initFxmlDirectoryPath();
    }

    public static synchronized ClientConfiguration getInstance() {
        if (instance == null) {
            instance = new ClientConfiguration();
        }
        return instance;
    }

    public Path getFxmlDirectoryPath() {
        return fxmlDirectoryPath;
    }

    public Properties getProperties() {
        return properties;
    }

    public URL getServerUrl() {
        return serverUrl;
    }

    private Path initFxmlDirectoryPath() {
        String fxmlDirectoryPathString = properties.getProperty("fxml.path");
        if (fxmlDirectoryPathString == null) {
            logger.error("FXML directory path is unknown (property value not found).");
            System.exit(5);
        }
        Path fxmlDirectoryPathValue = Path.of(fxmlDirectoryPathString);
        return fxmlDirectoryPathValue;
    }

    private Properties loadProperties() {
        Properties loadedProperties = new Properties();
        InputStream propertiesInputStream = getClass().getResourceAsStream("/app.properties");
        if (propertiesInputStream != null) {
            try {
                loadedProperties.load(propertiesInputStream);
            } catch (IOException e) {
                logger.error("I/O error occurred while reading config file.", e);
            }
        } else {
            logger.error("Config file was not found.");
        }
        return loadedProperties;
    }

    private URL initServerUrl() {
        try {
            String serverHostName = properties.getProperty("audio.server.hostname");
            String serverPortString = properties.getProperty("audio.server.port");
            int serverPort = Integer.parseInt(serverPortString);
            URL certainServerUrl = new URL("http", serverHostName, serverPort, "");
            return certainServerUrl;
        } catch (MalformedURLException e) {
            logger.error("Server URL is invalid. Exiting.");
            System.exit(4);
            return null;
        }
    }
}
