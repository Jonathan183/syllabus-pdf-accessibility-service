package edu.gsw.syllabus;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.font.FontSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
public class PdfExportController {

    private static final Logger log = LoggerFactory.getLogger(PdfExportController.class);

    public record ExportRequest(String html, String filename, String title, String lang) {}

    @PostMapping("/pdfua")
    public ResponseEntity<?> exportPdf(@RequestBody ExportRequest req) {
        long t0 = System.currentTimeMillis();

        try {
            if (req == null || req.html() == null || req.html().isBlank()) {
                log.warn("PDF_EXPORT_FAILURE reason=MissingHTML ms={}", (System.currentTimeMillis() - t0));
                return ResponseEntity.badRequest().body("Missing HTML");
            }

            String filename = (req.filename() == null || req.filename().isBlank())
                    ? "syllabus.pdf"
                    : req.filename().replaceAll("[\\r\\n\\\"]", "");

            String title = (req.title() == null || req.title().isBlank())
                    ? "Syllabus"
                    : req.title();

            String lang = (req.lang() == null || req.lang().isBlank())
                    ? "en-US"
                    : req.lang();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);

            // Tagged PDF (structure tree)
            pdf.setTagged();

            // Language
            pdf.getCatalog().setLang(new PdfString(lang));

            // Viewer preferences: show document title (helps "Document settings" checks)
            pdf.getCatalog().setViewerPreferences(
                    new PdfViewerPreferences().setDisplayDocTitle(true)
            );

            // Document info metadata
            PdfDocumentInfo info = pdf.getDocumentInfo();
            info.setTitle(title);

            // Ensure XMP metadata packet exists (helps "Metadata" checks)
            pdf.getXmpMetadata();

            // Converter properties + font embedding
            ConverterProperties props = new ConverterProperties();
            props.setBaseUri("/app");

            FontProvider fontProvider = new FontProvider(new FontSet());
            fontProvider.addStandardPdfFonts();
            fontProvider.addDirectory("/app/fonts"); // put TTF/OTF here in container
            props.setFontProvider(fontProvider);

            // Convert HTML -> Tagged PDF
            HtmlConverter.convertToPdf(req.html(), pdf, props);

            pdf.close();

            byte[] pdfBytes = baos.toByteArray();
            long ms = System.currentTimeMillis() - t0;

            // Research-grade structured log event
            log.info("PDF_EXPORT_SUCCESS filename={} bytes={} ms={} lang={}", filename, pdfBytes.length, ms, lang);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            long ms = System.currentTimeMillis() - t0;
            log.error("PDF_EXPORT_FAILURE filename={} ms={} error={}", (req != null ? req.filename() : "null"), ms, e.toString(), e);
            return ResponseEntity.status(500).body("PDF export failed: " + e);
        }
    }
}
