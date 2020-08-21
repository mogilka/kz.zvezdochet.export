package kz.zvezdochet.export.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class Table {

    public static void main(String[] args) throws IOException, DocumentException {
    	String filename = "/home/nataly/workspace/kz.zvezdochet.export/test/table.pdf";
        File file = new File(filename);
        file.getParentFile().mkdirs();

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        BaseColor thcolor = new BaseColor(153, 153, 204);
        BaseColor tdcolor = new BaseColor(204, 204, 255);
        BaseColor bcolor = BaseColor.WHITE;
        float bwidth = 1;
        float padding = 8;

        PdfPTable table = new PdfPTable(2);
		PdfPCell cell = new PdfPCell(new Phrase("Header 1"));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(padding);
		cell.setBackgroundColor(thcolor);
		cell.setUseVariableBorders(true);
		cell.setBorderColor(bcolor);
		cell.setBorderWidth(bwidth);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Header 2"));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(padding);
		cell.setBackgroundColor(thcolor);
		cell.setUseVariableBorders(true);
		cell.setBorderColor(bcolor);
		cell.setBorderWidth(bwidth);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat"));
		cell.setPadding(padding);
		cell.setBackgroundColor(tdcolor);
		cell.setUseVariableBorders(true);
		cell.setBorderColor(bcolor);
		cell.setBorderWidth(bwidth);
		table.addCell(cell);

		cell = new PdfPCell(new Phrase("Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum"));
		cell.setPadding(padding);
		cell.setBackgroundColor(tdcolor);
		cell.setUseVariableBorders(true);
		cell.setBorderColor(bcolor);
		cell.setBorderWidth(bwidth);
		table.addCell(cell);

		document.add(table);
        document.close();
    }
}
