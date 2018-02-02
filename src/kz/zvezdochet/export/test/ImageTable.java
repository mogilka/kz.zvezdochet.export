package kz.zvezdochet.export.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ImageTable {

    public static void main(String[] args) throws IOException, DocumentException {
    	String filename = "/home/nataly/workspace/kz.zvezdochet.export/test/table.pdf";
        File file = new File(filename);
        file.getParentFile().mkdirs();

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        PdfPTable table = new PdfPTable(3);
        //first row
		PdfPCell cell = new PdfPCell();
		cell.setBorder(Rectangle.TOP);
		table.addCell(cell);

		Paragraph p = new Paragraph("vertex");
		p.setAlignment(Element.ALIGN_CENTER);
		cell = new PdfPCell();
		cell.setBorder(Rectangle.TOP);
		cell.addElement(p);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase());
		cell.setBorder(Rectangle.TOP);
		table.addCell(cell);

		//second row
		cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		String imgfile = "/home/nataly/workspace/kz.zvezdochet.analytics/icons/conf/palm.gif";
		com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(imgfile);
		cell = new PdfPCell(image);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase());
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		//third row
		p = new Paragraph("left");
		p.setAlignment(Element.ALIGN_RIGHT);
		cell = new PdfPCell();
		cell.setBorder(Rectangle.BOTTOM);
		cell.addElement(p);
		table.addCell(cell);

		cell = new PdfPCell();
		cell.setBorder(Rectangle.BOTTOM);
		table.addCell(cell);

		p = new Paragraph("right");
		cell = new PdfPCell();
		cell.setBorder(Rectangle.BOTTOM);
		cell.addElement(p);
		table.addCell(cell);

		document.add(table);
        document.close();
    }
}
