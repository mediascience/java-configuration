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
package com.msiops.garage.configuration;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Configuration {

    static final String ENVIRONMENT_PROPERTY = "garage.environment";

    static Map<String, String> asMap(final Properties from, final String prefix) {

        final Pattern scanner = Pattern.compile(Pattern.quote(prefix + ".")
                + "(.+)");

        final HashMap<String, String> accum = new HashMap<>();
        from.stringPropertyNames().forEach(k -> {
            final Matcher m = scanner.matcher(k);
            if (m.matches()) {
                accum.put(m.group(1), from.getProperty(k));
            }
        });
        return Collections.unmodifiableMap(accum);

    }

    static Properties detach(final Properties props) {

        final Properties rval = new Properties();

        props.stringPropertyNames().forEach(k -> {
            rval.setProperty(k, props.getProperty(k));
        });

        return rval;

    }

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

    static Properties override(final Properties properties,
            final Collection<String> with) {

        return override(properties, with, System.getProperties());

    }

    static Properties override(final Properties properties,
            final Collection<String> with, final Properties from) {
        final Properties rval = new Properties(properties);
        with.forEach(k -> {
            final String ovr = from.getProperty(k);
            if (ovr != null) {
                rval.setProperty(k, ovr);
            }
        });
        return rval;

    }

    static Properties overrideFromEnv(final Properties properties,
            final Map<String, String> with) {

        return overrideFromVars(properties, with, System.getenv());

    }

    static Properties overrideFromSyspropsAndEnv(final Properties properties,
            final Map<String, String> with) {

        return overrideFromEnv(override(properties, with.values()), with);

    }

    static Properties overrideFromVars(final Properties properties,
            final Map<String, String> with, final Map<String, String> from) {

        final Properties rval = new Properties(properties);

        with.entrySet().forEach(e -> {
            if (from.containsKey(e.getKey())) {
                rval.setProperty(e.getValue(), from.get(e.getKey()));
            }
        });

        return rval;

    }

}
