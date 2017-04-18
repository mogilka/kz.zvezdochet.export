package kz.zvezdochet.export.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

public class SunSymbol {

    public static void main(String[] args) throws IOException, DocumentException {
    	String filename = "/home/nataly/workspace/kz.zvezdochet.export/test/sun.pdf";
        String fontpath = "/home/nataly/workspace/kz.zvezdochet.export/font/Cardo-Regular.ttf";
        String text = "this string contains special characters like this  \u2609, \u263D, \u263F, \u2640, \u2642";
        File file = new File(filename);
        file.getParentFile().mkdirs();

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        BaseFont bf = BaseFont.createFont(fontpath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font f = new Font(bf, 12);
        Paragraph p = new Paragraph(text, f);
        document.add(p);
        document.close();
    }
}
