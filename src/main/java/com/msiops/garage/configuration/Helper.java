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

        try (final InputStream dis = key
                .getResourceAsStream("default.properties");
                final InputStream eis = key.getResourceAsStream(env
                        + ".properties");) {

            final Properties rval = new Properties(defaults);
            if (dis != null) {
                rval.load(dis);
            }
            if (eis != null) {
                rval.load(eis);
            }
            return rval;

        } catch (final Exception e) {
            throw new RuntimeException("error loading default.properties");
        }

    }

    private Helper() {
        throw new AssertionError("no instance allowed");
    }

}
