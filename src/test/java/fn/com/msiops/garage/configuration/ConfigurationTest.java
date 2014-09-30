package fn.com.msiops.garage.configuration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.msiops.garage.configuration.Configuration;

public final class ConfigurationTest {

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

        assertEquals(expected, actual);

    }

    @Test
    public void testExplicitEnvironment() {

        final Properties expected = load("production");

        System.setProperty(Configuration.ENVIRONMENT_PROPERTY, "production");
        final Properties actual = Configuration.of(ConfigurationTest.class);

        assertEquals(expected, actual);

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

        assertEquals(expected, actual);

    }

}
