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

public class MathSymbol {

    public static void main(String[] args) throws IOException, DocumentException {
    	String filename = "/home/nataly/workspace/kz.zvezdochet.export/test/math.pdf";
        String fontpath = "/usr/share/fonts/truetype/freefont/FreeSans.ttf";
        String text = "this string contains special characters like this  \u2208, \u2229, \u2211, \u222b, \u2206";
        text += "\nthis string contains special characters like this  \u2190, \u2191, \u2192, \u2193";
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
