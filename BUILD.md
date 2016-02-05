# XMPTool Build Instructions

## Setting Up a Build Environment

1. Make sure you have the JDK installed. We're using JDK 8, and the code will not build on older versions.
2. Download IntelliJ IDEA. The Community Edition has absolutely everything we'll need for this project.
3. Clone this repository.
4. In IntelliJ, go to File -> Open... and choose the build.gradle file in the cloned directory.

You may need in IntelliJ to go to Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle
and set "Gradle JVM" to the right JVM. If the list is empty, go to File -> Project Structure -> SDKs and
add a new one. IntelliJ should find the right installation for you.

## Hosting

The web interface can easily be hosted on any server with a Java installation.

To build a runnable JAR, `cd` into the root directory and run `./gradlew od-web:distZip`. This command will create a
ZIP file with scripts inside to run on any platform. You can upload that ZIP to your server, or run it locally. No
database configuration or configuration in general is required.

When you run, a port can be specified with the `--server.port=` command-line option.
