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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.msiops.garage.configuration.Configuration;

public final class ConfigurationTest {

    private static final void assertPropertiesEquals(final Properties expected,
            final Properties actual) {

        assertEquals(flatten(expected), flatten(actual));

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
    public void testAsMap() {

        final String base = "some.map";

        final HashMap<String, String> expected = new HashMap<>();
        final Properties defs = new Properties();
        final Properties config = new Properties(defs);

        defs.setProperty(base + "." + "xyz", "something");
        expected.put("xyz", "something");

        Arrays.asList("abc", "def", "ghi").forEach(s -> {
            final String v = s + "0" + s;
            config.setProperty(base + "." + s, v);
            expected.put(s, v);
        });

        final Map<String, String> actual = Configuration.asMap(config, base);

        assertEquals(expected, actual);

    }

    @Test
    public void testDefaultEnvironment() {

        final Properties expected = load("development");

        final Properties actual = Configuration.of(ConfigurationTest.class);

        assertPropertiesEquals(expected, actual);

    }

    @Test
    public void testDetach() {

        final Properties bottom = new Properties();
        bottom.setProperty("a", "av");

        final Properties mid = new Properties(bottom);
        mid.setProperty("b", "bv");

        final Properties top = Configuration.detach(mid);

        bottom.setProperty("a", "newav");
        mid.setProperty("b", "newbv");

        assertEquals("av", top.getProperty("a"));
        assertEquals("bv", top.getProperty("b"));

    }

    @Test
    public void testExplicitEnvironment() {

        final Properties expected = load("production");

        System.setProperty(Configuration.ENVIRONMENT_PROPERTY, "production");
        final Properties actual = Configuration.of(ConfigurationTest.class);

        assertPropertiesEquals(expected, actual);

    }

    @Test
    public void testGetEnvironmentNameDefault() {

        assertEquals("development", Configuration.currentEnvironment());

    }

    @Test
    public void testGetEnvironmentNameExplicit() {

        System.setProperty(Configuration.ENVIRONMENT_PROPERTY, "production");

        assertEquals("production", Configuration.currentEnvironment());

    }

    @Test
    public void testLoadExplicitEnvironment() {

        final Properties expected = load("production");

        final Properties actual = Configuration.of(ConfigurationTest.class,
                "production");

        assertPropertiesEquals(expected, actual);

    }

    @Test
    public void testLoadExplicitEnvironmentWithDefaults() {

        final Properties loaded = load("production");
        final String firstKey = (String) loaded.keys().nextElement();

        final Properties defs = new Properties();
        defs.setProperty(firstKey, "default value");
        defs.setProperty("not.from.loaded", "another default value");

        final Properties expected = new Properties(defs);
        expected.putAll(loaded);

        final Properties actual = Configuration.of(ConfigurationTest.class,
                "production", defs);

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
    public void testOverrideFromArbitgraryDoesNotModifyOriginal() {

        final String prop = "com.msiops.prop";

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        final Properties overrides = new Properties();
        overrides.setProperty(prop, "overridden");

        Configuration.override(props, Collections.singleton(prop), overrides);

        assertEquals("value", props.getProperty(prop));

    }

    @Test
    public void testOverrideFromAribrary() {

        final String prop = "com.msiops.prop";

        final Properties overrides = new Properties();
        overrides.setProperty(prop, "overridden");

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        final Properties actual = Configuration.override(props,
                Collections.singleton(prop), overrides);

        assertEquals("overridden", actual.getProperty(prop));

    }

    @Test
    public void testOverrideFromEnvironment() {
        final String prop = "com.msiops.prop";
        final String var = System.getenv().keySet().iterator().next();

        final String origv = "value-should-not-be-in-environment";
        final String ovr = System.getenv(var);

        final Properties props = new Properties();
        props.setProperty(prop, origv);

        final Map<String, String> spec = Collections.singletonMap(var, prop);

        final Properties actual = Configuration.overrideFromEnv(props, spec);

        assertEquals(ovr, actual.getProperty(prop));

    }

    @Test
    public void testOverrideFromEnvironmentDoesNotModifyOriginal() {
        final String prop = "com.msiops.prop";
        final String var = System.getenv().keySet().iterator().next();

        final String origv = "value-should-not-be-in-environment";

        final Properties props = new Properties();
        props.setProperty(prop, origv);

        final Map<String, String> spec = Collections.singletonMap(var, prop);

        Configuration.overrideFromEnv(props, spec);

        assertEquals(origv, props.getProperty(prop));

    }

    @Test
    public void testOverrideFromEnvironmentNotInEnv() {

        final String prop = "com.msiops.prop";
        final String var = "NO_SUCH_ENV_VAR";

        final String origv = "value-should-not-be-in-environment";

        final Properties props = new Properties();
        props.setProperty(prop, origv);

        final Map<String, String> spec = Collections.singletonMap(var, prop);

        final Properties actual = Configuration.overrideFromEnv(props, spec);

        assertEquals(origv, actual.getProperty(prop));

    }

    @Test
    public void testOverrideFromEnvironmentNotInSpec() {
        final String prop = "com.msiops.prop";

        final String origv = "value-should-not-be-in-environment-or-sysprops1";

        final Properties props = new Properties();
        props.setProperty(prop, origv);

        final Map<String, String> spec = Collections.emptyMap();

        final Properties actual = Configuration.overrideFromEnv(props, spec);

        assertEquals(origv, actual.getProperty(prop));

    }

    @Test
    public void testOverrideFromSystemPropsAndEnvironment() {

        final String prop1 = "com.msiops.prop1";
        final String prop2 = "com.msiops.prop2";
        final String var1 = System.getenv().keySet().iterator().next();
        final String var2 = "ARBTRARY_NOT_MATCHED_VAR_NAME";

        final String origv1 = "value-should-not-be-in-environment-or-sysprops1";
        final String origv2 = "value-should-not-be-in-environment-or-sysprops2";

        final String ovr1 = System.getenv(var1);
        final String ovr2 = "override-2";

        System.setProperty(prop1, "something that will never be seen");
        System.setProperty(prop2, ovr2);

        final Properties props = new Properties();
        props.setProperty(prop1, origv1);
        props.setProperty(prop2, origv2);

        final Map<String, String> spec = new HashMap<>();
        spec.put(var1, prop1);
        spec.put(var2, prop2);

        final Properties actual = Configuration.overrideFromSyspropsAndEnv(
                props, spec);

        assertEquals(ovr1, actual.getProperty(prop1));
        assertEquals(ovr2, actual.getProperty(prop2));

    }

    @Test
    public void testOverrideFromSystemPropsAndEnvironmentDoesNotModifyOrig() {

        final String prop1 = "com.msiops.prop1";
        final String prop2 = "com.msiops.prop2";
        final String var1 = System.getenv().keySet().iterator().next();
        final String var2 = "ARBTRARY_NOT_MATCHED_VAR_NAME";

        final String origv1 = "value-should-not-be-in-environment-or-sysprops1";
        final String origv2 = "value-should-not-be-in-environment-or-sysprops2";

        final String ovr2 = "override-2";

        System.setProperty(prop1, "something that will never be seen");
        System.setProperty(prop2, ovr2);

        final Properties props = new Properties();
        props.setProperty(prop1, origv1);
        props.setProperty(prop2, origv2);

        final Map<String, String> spec = new HashMap<>();
        spec.put(var1, prop1);
        spec.put(var2, prop2);

        Configuration.overrideFromSyspropsAndEnv(props, spec);

        assertEquals(origv1, props.getProperty(prop1));
        assertEquals(origv2, props.getProperty(prop2));

    }

    @Test
    public void testOverrideFromVariablesMap() {

        final String prop = "com.msiops.prop";
        final String var = "PROP";

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        final Map<String, String> spec = Collections.singletonMap(var, prop);
        final Map<String, String> values = Collections.singletonMap(var,
                "overridden");

        final Properties actual = Configuration.overrideFromVars(props, spec,
                values);

        assertEquals("overridden", actual.getProperty(prop));

    }

    @Test
    public void testOverrideFromVariablesMapDoesNotModifyOriginal() {

        final String prop = "com.msiops.prop";
        final String var = "PROP";

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        final Map<String, String> spec = Collections.singletonMap(var, prop);
        final Map<String, String> values = Collections.singletonMap(var,
                "overridden");

        Configuration.overrideFromVars(props, spec, values);

        assertEquals("value", props.getProperty(prop));

    }

    @Test
    public void testOverrideFromVariablesMapNotInSpec() {

        final String prop = "com.msiops.prop";
        final String var = "PROP";

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        final Map<String, String> spec = Collections.emptyMap();
        final Map<String, String> values = Collections.singletonMap(var,
                "overridden");

        final Properties actual = Configuration.overrideFromVars(props, spec,
                values);

        assertEquals("value", actual.getProperty(prop));
    }

    @Test
    public void testOverrideFromVariablesMapNotInVars() {

        final String prop = "com.msiops.prop";
        final String var = "PROP";

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        final Map<String, String> spec = Collections.singletonMap(var, prop);
        final Map<String, String> values = Collections.emptyMap();

        final Properties actual = Configuration.overrideFromVars(props, spec,
                values);

        assertEquals("value", actual.getProperty(prop));
    }

    @Test
    public void testOverrideNotInArbitraryProps() {

        final String prop = "com.msiops.prop";

        final Properties props = new Properties();
        props.setProperty(prop, "value");

        final Properties overrides = new Properties();

        final Properties actual = Configuration.override(props,
                Collections.singleton(prop), overrides);

        assertEquals("value", actual.getProperty(prop));

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
