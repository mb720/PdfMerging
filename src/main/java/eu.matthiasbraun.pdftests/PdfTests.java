package eu.matthiasbraun.pdftests;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

public final class PdfTests {
    private static final Logger LOG = LoggerFactory.getLogger(PdfTests.class);
    private static final String DEFAULT_FONT_FILE = "DroidSans.ttf";

    private PdfTests() {
    }

    public static void main(String... args) {
        PDDocument firstPdf = createPdfWithOnePage("First PDF");
        PDDocument secondPdf = createPdfWithOnePage("Second PDF");
        PDDocument mergedPdf = merge(Arrays.asList(firstPdf, secondPdf));

        String userHome = System.getProperty("user.home");
        File firstPdfDestination = new File(userHome + "/Desktop/first.pdf");
        save(firstPdf, firstPdfDestination);
        File secondPdfDestination = new File(userHome + "/Desktop/second.pdf");
        save(secondPdf, secondPdfDestination);
        File mergedPdfDestination = new File(userHome + "/Desktop/merged.pdf");
        save(mergedPdf, mergedPdfDestination);
    }

    private static PDDocument merge(Collection<PDDocument> pdfs) {
        PDDocument mergedPdf = new PDDocument();
        PDFMergerUtility merger = new PDFMergerUtility();
        pdfs.forEach(pdf -> {
            try {
                merger.appendDocument(mergedPdf, pdf);
            } catch (IOException e) {
                LOG.warn("Could not append PDF", e);
            }
        });
        return mergedPdf;
    }

    private static void addText(String text, PDFont font, PDPageContentStream content) throws IOException {
        int fontSize = 80;
        content.setFont(font, fontSize);
        content.beginText();
        content.showText(text);
        content.endText();
    }

    /**
     * Saves a {@code document} to a {@code destination} and closes it afterwards.
     *
     * @param document    the {@link PDDocument} we want to save
     * @param destination the {@link File} where we to save the {@code document}
     */
    private static void save(PDDocument document, File destination) {
        if (document != null) {
            try {
                LOG.info("Saving PDF to {}", destination.getAbsolutePath());
                document.save(destination);
            } catch (IOException e) {
                LOG.warn("Could not save document {} at {}", document, destination, e);
            } finally {
                close(document);
            }
        } else {
            LOG.warn("PDF that should have been saved to {} is null", destination);
        }
    }

    private static void close(PDDocument document) {
        try {
            document.close();
        } catch (IOException e) {
            LOG.warn("Could not close document {}", document, e);
        }
    }

    private static PDDocument createPdfWithOnePage(String text) {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            addText(text, getFont(document), content);
        } catch (IOException e) {
            LOG.warn("Exception while adding text to content stream", e);
        }
        document.addPage(page);
        return document;
    }

    private static PDFont getFont(PDDocument doc) {
        PDFont font = PDType1Font.HELVETICA;
        try (final InputStream fontFileStream = PdfTests.class.getClassLoader().getResourceAsStream(DEFAULT_FONT_FILE)) {
            // The font has to be loaded as a TTF
            font = PDType0Font.load(doc, fontFileStream);
        } catch (IOException e) {
            LOG.warn("Could not load font {}", DEFAULT_FONT_FILE, e);
        }
        return font;
    }
}