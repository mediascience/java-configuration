package fn.com.msiops.garage.configuration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.msiops.garage.configuration.Configuration;

public final class ConfigurationTest {

    private static final void assertPropertiesEquals(final Properties p1,
            final Properties p2) {

        assertEquals(flatten(p1), flatten(p2));

    }

    private static Map<String, String> flatten(final Properties props) {

        final HashMap<String, String> flattened = new HashMap<>();
        props.stringPropertyNames().forEach(k -> {
            flattened.put(k, props.getProperty(k));
        });
        return Collections.unmodifiableMap(flattened);

    }

    private static Properties load(final String env) {
        try (InputStream is = ConfigurationTest.class.getResourceAsStream(env
                + ".properties")) {
            final Properties rval = new Properties();
            rval.load(is);
            return rval;
        } catch (final IOException e) {
            throw new RuntimeException("cannot load '" + env + "'", e);
        }
    }

    @Before
    public void setup() {
        System.clearProperty(Configuration.ENVIRONMENT_PROPERTY);
    }

    @Test
    public void testDefaultEnvironment() {

        final Properties expected = load("development");

        final Properties actual = Configuration.of(ConfigurationTest.class);

        assertPropertiesEquals(expected, actual);

    }

    @Test
    public void testExplicitEnvironment() {

        final Properties expected = load("production");

        System.setProperty(Configuration.ENVIRONMENT_PROPERTY, "production");
        final Properties actual = Configuration.of(ConfigurationTest.class);

        assertPropertiesEquals(expected, actual);

    }

    @Test
    public void testUnconfiguredEnvironment() {

        System.setProperty(Configuration.ENVIRONMENT_PROPERTY, "nonexistent");
        final Properties actual = Configuration.of(ConfigurationTest.class);

        assertTrue(flatten(actual).isEmpty());

    }

    @Test
    public void testUnconfiguredEnvironmentWithDefaults() {

        final Properties defs = new Properties();
        defs.setProperty("not.from.loaded", "default value");

        System.setProperty(Configuration.ENVIRONMENT_PROPERTY, "nonexistent");
        final Properties actual = Configuration.of(ConfigurationTest.class,
                defs);

        assertPropertiesEquals(defs, actual);

    }

    @Test
    public void testWithDefaults() {

        final Properties loaded = load("production");
        final String firstKey = (String) loaded.keys().nextElement();

        final Properties defs = new Properties();
        defs.setProperty(firstKey, "default value");
        defs.setProperty("not.from.loaded", "anoter default value");

        final Properties expected = new Properties(defs);
        expected.putAll(loaded);

        System.setProperty(Configuration.ENVIRONMENT_PROPERTY, "production");
        final Properties actual = Configuration.of(ConfigurationTest.class,
                defs);

        assertPropertiesEquals(expected, actual);

    }

}
