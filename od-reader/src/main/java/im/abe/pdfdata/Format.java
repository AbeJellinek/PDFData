package im.abe.pdfdata;

public enum Format {
    /**
     * CSV data format. Columns separated by commas, rows by lines. Values can be quoted.
     */
    CSV(".csv", "text/csv"),

    /**
     * JSON format. Written using JavaScript object syntax.
     */
    JSON(".json", "application/json"),

    /**
     * XML-based representation of raw RDF data.
     */
    RDF_XML(".xml", "application/rdf+xml"),

    /**
     * The most concise, idiomatic RDF format.
     */
    TURTLE(".turtle", "text/turtle");

    /**
     * The file extension associated with this format.
     */
    private String extension;

    /**
     * This format's MIME type.
     */
    private String mime;

    Format(String extension, String mime) {
        this.extension = extension;
        this.mime = mime;
    }

    public String getExtension() {
        return extension;
    }

    public String getMime() {
        return mime;
    }

    /**
     * Find a format by the given name. "RDF", "RDF_XML", "RDF/XML", "RDFXML" all map to the same thing.
     *
     * @param name The name to look up.
     * @return The format if found; CSV otherwise.
     */
    public static Format find(String name) {
        switch (name.toUpperCase()) {
            case "JSON":
                return Format.JSON;
            case "RDF":
            case "RDF_XML":
            case "RDF/XML":
            case "RDFXML":
                return Format.RDF_XML;
            case "TURTLE":
                return Format.TURTLE;
            default:
                return Format.CSV;
        }
    }
}
