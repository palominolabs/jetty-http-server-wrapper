This library makes it easy to set up an embedded [Jetty](http://www.eclipse.org/jetty/) server using [Guice Servlet](http://code.google.com/p/google-guice/wiki/Servlets)'s GuiceFilter.

# Example

A simple HTTP server with a servlet:

```
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

HttpServerWrapperConfig config = new HttpServerWrapperConfig()
            .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp("localhost", 8080));

injector.getInstance(HttpServerWrapperFactory.class)
    .getHttpServerWrapper(config)
    .start();
// you now have a server listening on localhost:8080
```

# What's In The Box
There are four main classes you'll interact with.

## [`HttpServerConnectorConfig`](https://github.com/palominolabs/jetty-http-server-wrapper/blob/master/src/main/java/com/palominolabs/http/server/HttpServerConnectorConfig.java)
This class represents one individual connector. A server can have many connectors listening on different ports and with or without TLS.

To make a connector for plain HTTP:
```
HttpServerConnectorConfig.forHttp("localhost", 8080)
```
and for HTTP with TLS:
```
HttpServerConnectorConfig.forHttps("localhost", 8443)
```

For a plain HTTP connector, there's nothing further to configure.

For a HTTP+TLS connector, you must specify a keystore and passphrase.
```
HttpServerConnectorConfig.forHttps("localhost", 8443)
            .withTlsKeystore(keyStore)
            .withTlsKeystorePassphrase("password");
```

You can use fluent-style `.with*` methods that return the current config object after modification, or plain old setters.
Be
You may also specify the TLS cipher suites and TLS protocols to use, but the defaults are sane, so typically you can just leave them alone.

## [`HttpServerWrapperConfig`](https://github.com/palominolabs/jetty-http-server-wrapper/blob/master/src/main/java/com/palominolabs/http/server/HttpServerWrapperConfig.java)
This class represents config that is scoped at the server level, not the connector level.

For basic usage all you need to do is add a connector:
```
HttpServerWrapperConfig config = new HttpServerWrapperConfig()
            .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp("localhost", 8080));
```

The default config generates access logs on stdout via [Logback Access](http://logback.qos.ch/access.html). You can specify a logback-access config file on the classpath:
```
config.withAccessLogConfigFileInClasspath("/logback-access-test.xml")
```
or on the filesystem:
```
config.withAccessLogConfigFileInFilesystem("/logback-access-test.xml")
```

You can also set the max form content size that Jetty will allow:
```
config.withMaxFormContentSize(400000)
```

Like `HttpServerConnectorConfig`, you can use `.with*` methods or `.set*` methods to set parameters.

## [`HttpServerWrapperFactory`](https://github.com/palominolabs/jetty-http-server-wrapper/blob/master/src/main/java/com/palominolabs/http/server/HttpServerWrapperFactory.java)

This is what you inject into your own code to use with a `HttpServerWrapperConfig` to create a `HttpServerWrapper`.

## [`HttpServerWrapper`](https://github.com/palominolabs/jetty-http-server-wrapper/blob/master/src/main/java/com/palominolabs/http/server/HttpServerWrapper.java)

When you get a `HttpServerWrapper` instance from a `HttpServerWrapperFactory`, it contains a configured but not yet started Jetty server.

When you want to start the server, call `start()`. When you want to stop it, call `stop()`. Crazy, right?