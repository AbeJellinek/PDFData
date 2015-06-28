A little project to explore options for adding linked open data to PDF.

What are all these files?
* build and gradle files are project configuration.
* xmptest.imi

The actual source code is in src

Building:

Here are the steps to set up a dev environment and import the project:

1. Make sure you have the JDK installed. We're using JDK 8
2. Download IntelliJ IDEA. The Community edition has absolutely everything we'll need for this project
3. Clone this repository.
4. In IntelliJ, go to File -> Open... and choose the build.gradle file in the cloned directory.

You may need in IntelliJ ti go to Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle and set "Gradle JVM" to the right JVM. If the list is empty, go to File -> Project Structure -> SDKs and add a new one. IntelliJ should find the right installation for you.
