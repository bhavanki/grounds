# Grounds

A [MUSH](https://en.wikipedia.org/wiki/MUSH).

More details later. :)

## Requirements

* [JDK](https://adoptopenjdk.net/) 11

## Running

In normal multi-user mode:

```
$ java -jar grounds.jar --properties etc/server.properties
```

In single-user mode:

```
$ java -jar grounds.jar --properties etc/server.properties --single-user
```

## Building

Use [Apache Maven](https://maven.apache.org/).

```
$ mvn package
```

## License

[MIT License](LICENSE)

