java-configuration
==================

Simple environment-based configuration for Java.

# Usage

## Import to Project

(not yet in maven central)

## Load Properties
```java
// environment name is in sysprop "garage.environment"
// default environment is "development"

// properties loaded from resource, "{environment}.properties"
// in class's package.
final Properties props = Configuration.of(MyClass.class);

// with defaults
final Properties props = Configuration.of(MyClass.class, defProps);
```

## Override Properties
```java
final Properties props = ...
final Collection<String> overrides = Arrays.asList("sys.prop", "sys.other.prop");

// any matching system properties override
Properties overridden = Collections.override(props, overrides);
```
