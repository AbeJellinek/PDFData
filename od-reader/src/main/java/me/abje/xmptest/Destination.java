package me.abje.xmptest;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFileAttachment;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Destination {

    private static final String KEY_NAMEDDEST = "nameddest";
    private static final String KEY_PAGE = "page";

    public abstract void writeAttachment(PDDocument doc, PDComplexFileSpecification file) throws IOException;

    private static Map<String, String> parseFragmentIdentifier(String fragment) throws IllegalArgumentException {
        try {
            if (fragment.startsWith("#")) // remove hash on beginning of fragment
                fragment = fragment.substring(1);
            String[] parts = fragment.split("&");
            Map<String, String> parameters = new HashMap<>();

            for (String part : parts) {
                if (part.indexOf('=') == -1) {
                    String key = URLDecoder.decode(part, "UTF-8");
                    parameters.put(key, key);
                } else {
                    String[] keyValue = part.split("=");
                    if (keyValue.length != 2)
                        throw new IllegalArgumentException("Invalid fragment format.");

                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    parameters.put(key, value);
                }
            }

            return parameters;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static Destination fragment(String fragment) throws IllegalArgumentException {
        Map<String, String> parameters = parseFragmentIdentifier(fragment);
        if (parameters.containsKey(KEY_NAMEDDEST))
            return named(parameters.get(KEY_NAMEDDEST));
        if (parameters.containsKey(KEY_PAGE))
            return page(Integer.parseInt(parameters.get(KEY_PAGE)) - 1);
        return document();
    }

    public static Destination document() {
        return new DocumentDestination();
    }

    public static Destination page(int page) {
        return new PageDestination(page);
    }

    public static Destination named(String name) {
        return new NamedDestination(name);
    }

    private static class DocumentDestination extends Destination {
        @Override
        public void writeAttachment(PDDocument doc, PDComplexFileSpecification file) throws IOException {
            PDDocumentNameDictionary names = doc.getDocumentCatalog().getNames();
            if (names == null)
                names = new PDDocumentNameDictionary(doc.getDocumentCatalog());

            PDEmbeddedFilesNameTreeNode efTree = names.getEmbeddedFiles();
            if (efTree == null)
                efTree = new PDEmbeddedFilesNameTreeNode();

            Map<String, PDComplexFileSpecification> efMap = intoMap(efTree.getNames());
            efMap.put(file.getFileDescription(), file);
            efTree.setNames(efMap);

            names.setEmbeddedFiles(efTree);
            doc.getDocumentCatalog().setNames(names);
        }

        private <K, V> Map<K, V> intoMap(Map<K, V> firstMap) {
            if (firstMap == null) {
                return new HashMap<>();
            } else {
                return new HashMap<>(firstMap);
            }
        }
    }

    private static class PageDestination extends Destination {
        private PDPage page;
        private int pageNumber;

        public PageDestination(PDPage page) {
            this.page = page;
        }

        public PageDestination(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        @Override
        public void writeAttachment(PDDocument doc, PDComplexFileSpecification file) throws IOException {
            if (page == null)
                page = doc.getDocumentCatalog().getPages().get(pageNumber);

            PDAnnotationFileAttachment annotation = new PDAnnotationFileAttachment();
            annotation.setFile(file);
            annotation.setPage(page);
            annotation.setAttachementName(PDAnnotationFileAttachment.ATTACHMENT_NAME_PAPERCLIP);
            annotation.setSubject(AttachmentDataStorage.STORED_DATA);

            PDRectangle rect = new PDRectangle();
            rect.setLowerLeftX(5);
            rect.setLowerLeftY(5);
            rect.setUpperRightX(15);
            rect.setUpperRightY(25);
            annotation.setRectangle(rect);

            List<PDAnnotation> annotations = page.getAnnotations();
            annotations.add(annotation);
            page.setAnnotations(annotations);
        }
    }

    private static class NamedDestination extends Destination {
        private String name;

        public NamedDestination(String name) {
            this.name = name;
        }

        @Override
        public void writeAttachment(PDDocument doc, PDComplexFileSpecification file) throws IOException {
            PDAnnotationFileAttachment annotation = new PDAnnotationFileAttachment();
            PDDocumentNameDictionary names = doc.getDocumentCatalog().getNames();
            if (names == null)
                throw new RuntimeException("unknown destination: " + name);
            PDDestinationNameTreeNode dests = names.getDests();
            if (dests == null)
                throw new RuntimeException("unknown destination: " + name);
            PDPageDestination destination = dests.getNames().get(name);
            if (destination == null)
                throw new RuntimeException("unknown destination: " + name);

            if (destination instanceof PDPageFitDestination) {
                new PageDestination(destination.getPage()).writeAttachment(doc, file);
            } else {
                int x = 0;
                int y = 0;

                if (destination instanceof PDPageFitWidthDestination) {
                    y = ((PDPageFitWidthDestination) destination).getTop();
                } else if (destination instanceof PDPageFitHeightDestination) {
                    x = ((PDPageFitHeightDestination) destination).getLeft();
                } else if (destination instanceof PDPageFitRectangleDestination) {
                    PDPageFitRectangleDestination pageFitRectangle = (PDPageFitRectangleDestination) destination;
                    x = pageFitRectangle.getLeft();
                    y = pageFitRectangle.getTop();
                } else if (destination instanceof PDPageXYZDestination) {
                    PDPageXYZDestination xyz = (PDPageXYZDestination) destination;
                    x = xyz.getLeft();
                    y = xyz.getTop();
                }

                PDPage page = destination.getPage();

                annotation.setFile(file);
                annotation.setPage(page);
                annotation.setAttachementName(PDAnnotationFileAttachment.ATTACHMENT_NAME_PAPERCLIP);
                annotation.setSubject(AttachmentDataStorage.STORED_DATA);

                PDRectangle rect = new PDRectangle();
                rect.setLowerLeftX(x);
                rect.setLowerLeftY(y + 20);
                rect.setUpperRightX(x + 10);
                rect.setUpperRightY(y);
                annotation.setRectangle(rect);

                List<PDAnnotation> annotations = page.getAnnotations();
                annotations.add(annotation);
                page.setAnnotations(annotations);
            }
        }
    }
}
