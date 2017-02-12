[![Build Status](https://semaphoreci.com/api/v1/projects/695fc17f-200a-477d-9439-b9ec299d2b64/575275/badge.svg)](https://semaphoreci.com/marshallpierce/jetty-http-server-wrapper)
 [ ![Download](https://api.bintray.com/packages/marshallpierce/maven/com.palominolabs.http%3Ajetty-http-server-wrapper/images/download.svg) ](https://bintray.com/marshallpierce/maven/com.palominolabs.http%3Ajetty-http-server-wrapper/_latestVersion) 
 
This library makes it easy to set up an embedded [Jetty](http://www.eclipse.org/jetty/) server using [Guice Servlet](http://code.google.com/p/google-guice/wiki/Servlets)'s GuiceFilter. It also provides sane defaults for TLS, makes it easy to serve static files, and integrates logback-access. This doesn't let you do anything that Jetty's API doesn't already let you do; it just hides some boilerplate.

Artifacts are released in [Bintray](https://bintray.com/). For gradle, use the `jcenter()` repository. For maven, [go here](https://bintray.com/bintray/jcenter) and click "Set me up".

# Example

A simple HTTP server with a servlet:

```java
// set up Guice injector
Injector injector = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            // module for this library
            install(new HttpServerWrapperModule());
            // servlet module to bind servlets to paths
            install(new ServletModule() {
                @Override
                protected void configureServlets() {
                    bind(SomeServlet.class);
                    serve("/somewhere").with(SomeServlet.class);
                }
            });
        }
    });

// serve static files like /static/foo.jpg
HttpResourceHandlerConfig rhConfig = new HttpResourceHandlerConfig()
    .withBaseResource(Resource.newClassPathResource("/com/foo/bar/your-assets"))
    .withContextPath("/static");

HttpServerWrapperConfig config = new HttpServerWrapperConfig()
            .withResourceHandlerConfig(rhConfig)
            .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp("localhost", 8080));

injector.getInstance(HttpServerWrapperFactory.class)
    .getHttpServerWrapper(config)
    .start();
// you now have a server listening on localhost:8080
```

Requests that do not match any configured pattern for GuiceFilter (in this case, the `/somewhere` pattern configured in the ServletModule) will 404.

# What's In The Box
There are a few main classes you'll interact with.

### [`HttpServerConnectorConfig`](https://github.com/palominolabs/jetty-http-server-wrapper/blob/master/src/main/java/com/palominolabs/http/server/HttpServerConnectorConfig.java)
This class represents one individual connector. A server can have many connectors listening on different ports and with or without TLS.

To make a connector for plain HTTP:
```java
HttpServerConnectorConfig.forHttp("localhost", 8080)
```

For a plain HTTP connector, there's nothing further to configure.

For a HTTP+TLS connector, you must specify a keystore and passphrase.
```java
HttpServerConnectorConfig.forHttps("localhost", 8443)
            .withTlsKeystore(keyStore)
            .withTlsKeystorePassphrase("password");
```

You can use fluent-style `.with*` methods that return the current config object after modification, or plain old setters.

You may also specify the TLS cipher suites and TLS protocols to use, but the defaults are sane, so typically you can just leave them alone.

### [`HttpServerWrapperConfig`](https://github.com/palominolabs/jetty-http-server-wrapper/blob/master/src/main/java/com/palominolabs/http/server/HttpServerWrapperConfig.java)
This class represents config that is scoped at the server level, not the connector level.

For basic usage all you need to do is add a connector:
```java
HttpServerWrapperConfig config = new HttpServerWrapperConfig()
            .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp("localhost", 8080));
```

The default config generates access logs on stdout via [Logback Access](http://logback.qos.ch/access.html). You can specify a logback-access config file on the classpath:
```java
config.withAccessLogConfigFileInClasspath("/logback-access-test.xml")
```
or on the filesystem:
```java
config.withAccessLogConfigFileInFilesystem("/logback-access-test.xml")
```

You can also set the max form content size that Jetty will allow:
```java
config.withMaxFormContentSize(400000)
```

Like `HttpServerConnectorConfig`, you can use `.with*` methods or `.set*` methods to set parameters.

### [`HttpResourceHandlerConfig`](https://github.com/palominolabs/jetty-http-server-wrapper/blob/master/src/main/java/com/palominolabs/http/server/HttpResourceHandlerConfig.java)

Used for serving static files. See the Javadoc or source for all available config options.

```java
HttpResourceHandlerConfig rhConfig = new HttpResourceHandlerConfig()
    .withBaseResource(Resource.newClassPathResource("/com/foo/bar/your-assets"))
    .withWelcomeFiles(Lists.newArrayList("almost-like-index.html", "another-one.html"));
    .withEtags(true)
    .withContextPath("/static");

HttpServerWrapperConfig config = new HttpServerWrapperConfig()
    .withResourceHandlerConfig(rhConfig)
    ...

```

### [`HttpServerWrapperFactory`](https://github.com/palominolabs/jetty-http-server-wrapper/blob/master/src/main/java/com/palominolabs/http/server/HttpServerWrapperFactory.java)

This is what you inject into your own code to use with a `HttpServerWrapperConfig` to create a `HttpServerWrapper`.

### [`HttpServerWrapper`](https://github.com/palominolabs/jetty-http-server-wrapper/blob/master/src/main/java/com/palominolabs/http/server/HttpServerWrapper.java)

When you get a `HttpServerWrapper` instance from a `HttpServerWrapperFactory`, it contains a configured but not yet started Jetty server.

When you want to start the server, call `start()`. When you want to stop it, call `stop()`. Crazy, right?
