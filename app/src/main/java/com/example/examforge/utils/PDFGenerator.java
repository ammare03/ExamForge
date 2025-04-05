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
        
        
        File pdfDir = new File(context.getFilesDir(), "pdfs");
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }
        
        
        File pdfFile = new File(pdfDir, fileName);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();
        
        
        formatAndAddContent(document, content);
        
        document.close();
        return pdfFile;
    }
    
    private static void formatAndAddContent(Document document, String content) throws DocumentException {
        
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        
        
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            
            if (line.startsWith("###")) {
                String headingText = line.substring(3).trim();
                Paragraph heading = new Paragraph(headingText, headingFont);
                heading.setSpacingBefore(10);
                heading.setSpacingAfter(7);
                document.add(heading);
                continue;
            }
            
            
            if (line.trim().equals("---")) {
                document.add(new Paragraph(" ", normalFont));
                continue;
            }
            
            
            if (line.contains("**")) {
                Paragraph paragraph = new Paragraph();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                
                
                Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
                Matcher matcher = pattern.matcher(line);
                
                int lastEnd = 0;
                while (matcher.find()) {
                    
                    String normalText = line.substring(lastEnd, matcher.start());
                    if (!normalText.isEmpty()) {
                        paragraph.add(new Chunk(normalText, normalFont));
                    }
                    
                    
                    String boldText = matcher.group(1);
                    paragraph.add(new Chunk(boldText, boldFont));
                    
                    lastEnd = matcher.end();
                }
                
                
                if (lastEnd < line.length()) {
                    paragraph.add(new Chunk(line.substring(lastEnd), normalFont));
                }
                
                document.add(paragraph);
            } else {
                
                document.add(new Paragraph(line, normalFont));
            }
        }
    }
}