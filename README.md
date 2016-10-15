# PDFData: putting data into, getting data out of PDF

## Background

[View build instructions.](BUILD.md)

There is a growing movement to encourage those who publish information on the web to publish not just documents but also machine-readable data in a format that is easy to digest. Further, there is a desire that the data be represented in a way that leverages the "semantic web" by insuring that the data uses URLs to identify vocabularies and linkages.

In this effort, PDF has a bad reputation as a format which thwarts data extraction and (re)use.

This project is a proof of concept exploring ways in which data can be added to, annotated, or otherwise stored in PDF
files and discovered, using public, open source libraries, and staying within the ISO 32000 (PDF) standard.

A couple of observations: 

First, while the data associated with a document is thought of as being in the document itself, often what is wanted is MORE, so no amount of analysis of arbitrary PDF files to extract data will suffice. Rather, some process of adding data (back) into the document is needed (Just as RDFa is used to annotate HTML).  

Second, there are several places where information in a PDF file could be treated as data, all supported by the PDF standard (ISO 32000) although not by all PDF implementations. PDF implementations are growing, with native implementations in Chrome, IOS, Windows 10, Firefox via PDF.js.

## What's in the project?

Primarily, this project consists of a Java library for:

* Adding data to PDF file, as an attachment; converting from PDF annotations; modifying the XMP. 
* Extracting data from PDF files, enumerating all of the places where data could be stashed, and streaming the results.

It also has:

* A server that hosts a web interface for demonstrating simple applications of the library.
* Some examples of data-bearing PDF files.
* Some tooling for extracting, manipulating, and searching data-bearing PDFs.
* Some documentation.
* Open design and implementation issues.


## Running

There is a release, available as a runnable JAR [here](https://github.com/Aiybe/PDFData/releases).
You can also try it out on the web [here](https://pdf.abe.im).

`od-reader/src/test/resources/docs` contains four example PDF files, labeled based on the storage types they contain.

To run the project outside of an IDE, `cd` into the project's root directory and run `./gradlew run`.
A help message should be printed. Additional arguments can be supplied after `run`.

__Note:__ Gradle may indicate a failure if you run without supplying any arguments.
This is simply because the program returns a non-zero exit code.

    Usage: pdfdata
        read  <pdf file> [-o output file] [-f format]
        write <source file> <pdf file>
    
    Options:
        -h, --help: print this help message and exit
    
    Supported Formats:
        TURTLE (default), CSV, JSON, RDF/XML
