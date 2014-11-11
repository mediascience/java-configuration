/**
 * Licensed under the Apache License, Version 2.0 (the "License") under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for information regarding copyright
 * ownership. You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msiops.garage.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Configuration {

    public static final String ENVIRONMENT_PROPERTY = Helper.ENVIRONMENT_PROPERTY;

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

    static String currentEnvironment() {
        return Helper.environment();
    }

    static Properties detach(final Properties props) {

        final Properties rval = new Properties();

        props.stringPropertyNames().forEach(k -> {
            rval.setProperty(k, props.getProperty(k));
        });

        return rval;

    }

    static Properties of(final Class<?> key) {

        return Helper.load(key, Helper.environment(), new Properties());

    }

    static Properties of(final Class<?> key, final Properties defaults) {

        return Helper.load(key, Helper.environment(), defaults);

    }

    static Properties of(final Class<?> key, final String environment) {

        return Helper.load(key, environment, new Properties());

    }

    static Properties of(final Class<?> key, final String environment,
            final Properties defaults) {

        return Helper.load(key, environment, defaults);

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
