/**
 * Licensed to Media Science International (MSI) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. MSI
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
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
    public void testOverride() {

        final String prop = "com.msiops.prop";

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        System.setProperty(prop, "overridden");

        final Properties actual = Configuration.override(props,
                Collections.singleton(prop));

        assertEquals("overridden", actual.getProperty(prop));

    }

    @Test
    public void testOverrideDoesNotModifyOriginal() {

        final String prop = "com.msiops.prop";

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        System.setProperty(prop, "overridden");

        Configuration.override(props, Collections.singleton(prop));

        assertEquals("value", props.getProperty(prop));

    }

    @Test
    public void testOverrideNotInSystemProps() {

        final String prop = "com.msiops.prop";

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        System.clearProperty(prop);

        final Properties actual = Configuration.override(props,
                Collections.singleton(prop));

        assertEquals("value", actual.getProperty(prop));

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
