package com.msiops.garage.configuration;

import java.io.InputStream;
import java.util.Properties;

final class Helper {

    public static final String DEFAULT_ENVIRONMENT = "development";

    public static final String ENVIRONMENT_PROPERTY = "garage.environment";

    public static String environment() {
        return System.getProperty(ENVIRONMENT_PROPERTY, DEFAULT_ENVIRONMENT);
    }

    public static Properties load(final Class<?> key, final String env,
            final Properties defaults) {

        try (InputStream is = key.getResourceAsStream(env + ".properties")) {

            final Properties rval = new Properties(defaults);
            rval.load(is);
            return rval;

        } catch (final Exception e) {
            return new Properties(defaults);
        }

    }

    private Helper() {
        throw new AssertionError("no instance allowed");
    }

}
