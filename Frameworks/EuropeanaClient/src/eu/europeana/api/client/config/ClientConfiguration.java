package eu.europeana.api.client.config;

import java.io.InputStream;
import java.util.Properties;

import eu.europeana.api.client.exception.TechnicalRuntimeException;

public class ClientConfiguration {

	protected static final String EUROPEANA_CLIENT_PROPERTIES_FILE = "/europeana-client.properties";
	protected static final String PROP_EUROPEANA_API_KEY = "europeana.api.key";
	protected static final String PROP_EUROPEANA_SEARCH_URI = "europeana.search.uri";
	private static Properties properties = null;

	private static ClientConfiguration singleton;

	/**
	 * Hide the default constructor
	 */
	private ClientConfiguration() {
	};

	/**
	 * Accessor method for the singleton
	 * 
	 * @return
	 */
	public static synchronized ClientConfiguration getInstance() {
		singleton = new ClientConfiguration();
		singleton.loadProperties();
		return singleton;
	}

	/**
	 * Laizy loading of configuration properties
	 */
	public synchronized void loadProperties() {
        properties = new Properties();
		/*try {
			InputStream resourceAsStream = getClass().getResourceAsStream(
					EUROPEANA_CLIENT_PROPERTIES_FILE);
			if (resourceAsStream != null)
				getProperties().load(resourceAsStream);
			else
				throw new TechnicalRuntimeException(
						"No properties file found in classpath! "
								+ EUROPEANA_CLIENT_PROPERTIES_FILE);

		} catch (Exception e) {
			throw new TechnicalRuntimeException(
					"Cannot read configuration file: "
							+ EUROPEANA_CLIENT_PROPERTIES_FILE, e);
		}*/

	}

	/**
	 * provides access to the configuration properties. It is not recommended to
	 * use the properties directly, but the
	 * 
	 * @return
	 */
	Properties getProperties() {
		return properties;
	}

	/**
	 * 
	 * @return the name of the file storing the client configuration
	 */
	String getConfigurationFile() {
		return EUROPEANA_CLIENT_PROPERTIES_FILE;
	}

	/**
	 * This method provides access to the API key defined in the configuration
	 * file
	 * @see PROP_EUROPEANA_API_KEY
	 * 
	 * @return
	 */
	public String getApiKey() {
		return "yEXj2LbU4";
	}
	
	/**
	 * This method provides access to the search uri value defined in the configuration
	 * file
	 * @see PROP_EUROPEANA_SEARCH_URI
	 * 
	 * @return
	 */
	public String getSearchUri() {
		return "http://www.europeana.eu/api/v2/search.json";
	}
}
