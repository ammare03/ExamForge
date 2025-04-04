package com.example.examforge.utils;

import android.content.Context;
import android.os.Environment;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFGenerator {

    public static File generatePDF(Context context, String content, String fileName) throws DocumentException, IOException {
        Document document = new Document();
        
        // Create directory for PDFs in internal app storage
        File pdfDir = new File(context.getFilesDir(), "pdfs");
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }
        
        // Create the PDF file in the app's internal storage
        File pdfFile = new File(pdfDir, fileName);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();
        
        // Format the content
        formatAndAddContent(document, content);
        
        document.close();
        return pdfFile;
    }
    
    private static void formatAndAddContent(Document document, String content) throws DocumentException {
        // Define fonts
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        
        // Split content by lines
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            // Handle ### for headings (Large and Bold)
            if (line.startsWith("###")) {
                String headingText = line.substring(3).trim();
                Paragraph heading = new Paragraph(headingText, headingFont);
                heading.setSpacingBefore(10);
                heading.setSpacingAfter(7);
                document.add(heading);
                continue;
            }
            
            // Handle --- for line breaks (just skip these lines)
            if (line.trim().equals("---")) {
                document.add(new Paragraph(" ", normalFont));
                continue;
            }
            
            // Handle ** for bold text
            if (line.contains("**")) {
                Paragraph paragraph = new Paragraph();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                
                // Define a pattern to find text between ** markers
                Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
                Matcher matcher = pattern.matcher(line);
                
                int lastEnd = 0;
                while (matcher.find()) {
                    // Add normal text before the match
                    String normalText = line.substring(lastEnd, matcher.start());
                    if (!normalText.isEmpty()) {
                        paragraph.add(new Chunk(normalText, normalFont));
                    }
                    
                    // Add bold text (text between **)
                    String boldText = matcher.group(1);
                    paragraph.add(new Chunk(boldText, boldFont));
                    
                    lastEnd = matcher.end();
                }
                
                // Add any remaining normal text after the last match
                if (lastEnd < line.length()) {
                    paragraph.add(new Chunk(line.substring(lastEnd), normalFont));
                }
                
                document.add(paragraph);
            } else {
                // Regular text (no formatting)
                document.add(new Paragraph(line, normalFont));
            }
        }
    }
}