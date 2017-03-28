# PDFData Build Instructions

## Setting Up a Build Environment

1. Make sure you have the JDK installed. We're using JDK 8, and the code will not build on older versions.
If you just want to build/test from the command line, you can stop here.
2. Download IntelliJ IDEA. The Community Edition has absolutely everything we'll need for this project.
3. Clone this repository.
4. In IntelliJ, go to File -> Open... and choose the build.gradle file in the cloned directory.

You may need in IntelliJ to go to Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle
and set "Gradle JVM" to the right JVM. If the list is empty, go to File -> Project Structure -> SDKs and
add a new one. IntelliJ should find the right installation for you.

## Building the Tool

*Needs more.*

To build the tool itself, `cd` into the root directory and run `./gradlew od-frontend:jar`. If you only want to build
the library, but not the frontend tool, use `od-reader:jar` instead. The output will appear in the `build/libs/`
directory of whichever module you choose to build.

## Building/Hosting the Web Service

The web interface can easily be hosted on any server with a Java installation.

To build a runnable JAR, `cd` into the root directory and run `./gradlew od-web:distZip`. This command will create a
ZIP file in `od-web/build/distributions/` with scripts inside to run on any platform. You can upload that ZIP to your
server, or run it locally. No database configuration or configuration in general is required.

When you run, a port can be specified with the `--server.port=` command-line option.

## Project Structure

What are all these files?

* `build/` and `.gradle` files are project configuration.
* `.iml` files are IntelliJ IDEA module files.
* `AddData.xml` is an Adobe Acrobat action for adding linked data.
* `examples/` contains PDF and data file example suites.
* `od-frontend/` contains the code for the command-line reader/writer interface.
* `od-reader/` contains the code for the reader/writer itself.
* `od-web/` contains the code for the web service.
* `xmpcore/` contains the code for a modified version of Adobe's XMPCore library.

The actual source code is in the `src/` folder of each module.