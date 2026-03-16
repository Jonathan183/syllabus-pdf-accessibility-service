Syllabus PDF Exporter (Tagged PDF)
---------------------------------

POST /api/export/pdfua
Body: { "html": "<!doctype html>...", "filename":"x.pdf", "title":"...", "lang":"en-US" }

Local:
  mvn spring-boot:run
  # then use the HTML generator's Export Accessible PDF button (configured to localhost:8080)
