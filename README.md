## Installation

This package is available on GitHub Packages as an open source project.

### Maven

First, add the GitHub Packages repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub letzmc Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/letzmc/SoilEngine</url>
    </repository>
</repositories>
```

Then add the dependency:

```xml
<dependency>
    <groupId>com.github.letzmc</groupId>
    <artifactId>soilengine</artifactId>
    <version>1.0</version>
</dependency>
```

### Gradle

Add the GitHub Packages repository to your `build.gradle`:

```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = "https://maven.pkg.github.com/letzmc/SoilEngine"
    }
}
```

Then add the dependency:

```groovy
dependencies {
    implementation 'com.github.letzmc:soilengine:1.0'
}
```
