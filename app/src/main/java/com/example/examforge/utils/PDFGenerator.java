package com.example.examforge.utils;

import android.content.Context;
import android.os.Environment;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFGenerator {

    public static File generatePDF(Context context, String content, String fileName) throws DocumentException, IOException {
        Document document = new Document();
        // Get the public Documents directory.
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }
        File pdfFile = new File(documentsDir, fileName);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();
        document.add(new Paragraph(content));
        document.close();
        return pdfFile;
    }
}