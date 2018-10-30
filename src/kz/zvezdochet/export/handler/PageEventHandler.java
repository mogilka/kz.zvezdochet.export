package kz.zvezdochet.export.handler;

import java.io.IOException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEvent;
import com.itextpdf.text.pdf.PdfWriter;

import kz.zvezdochet.export.util.PDFUtil;

public class PageEventHandler implements PdfPageEvent {
    protected Document doc;

    public PageEventHandler(Document doc) {
        this.doc = doc;
    }
	
	@Override
	public void onStartPage(PdfWriter arg0, Document arg1) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onSectionEnd(PdfWriter arg0, Document arg1, float arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSection(PdfWriter arg0, Document arg1, float arg2, int arg3, Paragraph arg4) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onParagraphEnd(PdfWriter arg0, Document arg1, float arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onParagraph(PdfWriter arg0, Document arg1, float arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onOpenDocument(PdfWriter arg0, Document arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onGenericTag(PdfWriter arg0, Document arg1, Rectangle arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onEndPage(PdfWriter writer, Document doc) {
		//колонтитулы
		try {
			BaseFont baseFont = PDFUtil.getBaseFont();
			PdfContentByte cb = writer.getDirectContent();
			Font fonth = new Font(baseFont, 10, Font.NORMAL, new BaseColor(153, 153, 153));
			float x = (doc.right() - doc.left()) / 2 + doc.leftMargin();
	        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase("Астролог Наталья Звездочёт", fonth),
	        	x, doc.top() + 10, 0);
	        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(String.valueOf(writer.getPageNumber()), fonth),
	        	x, doc.bottom() - 10, 0);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCloseDocument(PdfWriter arg0, Document arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onChapterEnd(PdfWriter arg0, Document arg1, float arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onChapter(PdfWriter arg0, Document arg1, float arg2, Paragraph arg3) {
		// TODO Auto-generated method stub
		
	}
}
