package kz.zvezdochet.export.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;

public class Doc {

    public static void main(String[] args) throws IOException, DocumentException {
    	String filename = "/home/nataly/workspace/kz.zvezdochet.export/test/doc.pdf";
        String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        File file = new File(filename);
        file.getParentFile().mkdirs();

        PdfDocument document = new PdfDocument();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.addWriter(writer);
        document.setPageSize(PageSize.A4);
        document.open();
        Paragraph p = new Paragraph(text);
        document.add(p);
        document.close();
    }
}
