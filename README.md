# XMPTest

A little project to explore options for adding linked open data to PDF.

## Structure

What are all these files?

* `build/` and `.gradle` files are project configuration.
* `.iml` files are IntelliJ IDEA module files.
* `od-frontend/` contains the code for the command-line reader/writer interface.
* `od-reader/` contains the code for the reader/writer itself.
* `xmpcore/` contains the code for a modified version of Adobe's XMPCore library.

The actual source code is in the `src/` folder of each module.

## Setting Up a Build Environment

1. Make sure you have the JDK installed. We're using JDK 8, and the code will not build on older versions.
2. Download IntelliJ IDEA. The Community Edition has absolutely everything we'll need for this project.
3. Clone this repository.
4. In IntelliJ, go to File -> Open... and choose the build.gradle file in the cloned directory.

You may need in IntelliJ to go to Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle
and set "Gradle JVM" to the right JVM. If the list is empty, go to File -> Project Structure -> SDKs and 
add a new one. IntelliJ should find the right installation for you.

We should write up design decisions and issues (when this gets big, separate or start using GitHub issues).

## What We're Producing
* some tooling for making data-bearing PDFs
* some examples of data-bearing PDF files
* some tooling for extracting, manipulating, searching data-bearing PDF
* some documentation

Anything else? Elaborate?

What are the requirements?

What's important now is to write down what we know and what questions remain.

All references and why they're important.
