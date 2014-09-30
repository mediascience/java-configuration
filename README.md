java-configuration
==================

[![Build Status](https://travis-ci.org/mediascience/java-configuration.svg)](https://travis-ci.org/mediascience/java-configuration)

Simple environment-based configuration for Java.

## Usage

### Import to Project
```xml
<dependency>
    <groupId>com.msiops.garage</groupId>
    <artifactId>garage-configuration</artifactId>
    <version>${v.configuration}</version>
</dependency>
```

See [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%20%22com.msiops.garage%22%20a%3A%22garage-configuration%22) for latest version

### Load Properties
```java
// environment name is in sysprop "garage.environment"
// default environment is "development"

// properties loaded from resource, "{environment}.properties"
// in class's package.
final Properties props = Configuration.of(MyClass.class);

// with defaults
final Properties props = Configuration.of(MyClass.class, defProps);
```

### Override Properties
```java
final Properties props = ...
final Collection<String> overrides = Arrays.asList("sys.prop", "sys.other.prop");

// any matching system properties override
Properties overridden = Collections.override(props, overrides);
```

### Detach
```java
Properties p = new Properties();
p.setProperty("key","value");

Properties detached = Configuration.detach(p);
p.setProperty("key","changed");
assert detached.getProperty("key").equals("value");
```

### Partial Properties Map
```java
Properties p = new Properties();
p.setProperty("base.a", "avalue");
p.setProperty("base.b", "bvalue");

Map<String,String> partial = Configuration.asMap(p, "base");
assert partial.get("a").equals("avalue");
assert partial.get("b").equals("bvalue");
```


## Versioning

Releases in the 0.x series are the Wild West. Anything can change between
releases--package names, method signatures, behavior, whatever. But if you
like it as it is right now, all the tests pass so just use it at its current
version and have fun.

The next version series will be 1.x. Every release in that series will be
backward compatible with every lower-numbered release in the same series
except possibly in the case of 1) a bug fix or 2) a correction to an
underspecification.

An incompatible change to the interface, behavior, license, or anything else
after the 1.x series is published will result in a new series, such as
2.x.

## Acknowledgements

This module attempts to simplify environment-based configuration as prescribed
in _Continuous Delivery_ by Humble and Farley.

Thanks to Media Science International for its support of FOSS.

## License

Licensed to Media Science International (MSI) under one or more
contributor license agreements. See the NOTICE file distributed with this
work for additional information regarding copyright ownership. MSI
licenses this file to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.




