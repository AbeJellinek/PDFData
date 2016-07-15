package im.abe.pdfdata;

public enum Format {
    CSV(".csv", "text/csv"),
    JSON(".json", "application/json"),
    RDF_XML(".xml", "application/rdf+xml"),
    TURTLE(".turtle", "text/turtle");

    private String extension;
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
