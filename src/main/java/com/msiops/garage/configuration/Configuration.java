package com.msiops.garage.configuration;

import java.io.InputStream;
import java.util.Properties;

public interface Configuration {

    static final String ENVIRONMENT_PROPERTY = "garage.environment";

    static Properties of(final Class<?> key) {

        return of(key, new Properties());

    }

    static Properties of(final Class<?> key, final Properties defaults) {

        final String env = System.getProperty(ENVIRONMENT_PROPERTY,
                "development");

        try (InputStream is = key.getResourceAsStream(env + ".properties")) {

            final Properties rval = new Properties(defaults);
            rval.load(is);
            return rval;

        } catch (final Exception e) {
            return new Properties(defaults);
        }

    }

}
