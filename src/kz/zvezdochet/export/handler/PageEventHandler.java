package kz.zvezdochet.export.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import kz.zvezdochet.export.util.PDFUtil;

/**
 * Обработчик страниц
 * @author Natalie Didenko
 */
public class PageEventHandler implements PdfPageEvent {
	
	@Override
	public void onStartPage(PdfWriter writer, Document document) {}
	@Override
	public void onSectionEnd(PdfWriter writer, Document document, float paragraphPosition) {}

	@Override
	public void onSection(final PdfWriter writer, final Document document, final float paragraphPosition, final int depth, final Paragraph title) {
//		this.pageByTitle.put(title.getContent(), writer.getPageNumber());
	}

	@Override
	public void onParagraphEnd(PdfWriter writer, Document document, float paragraphPosition) {}
	@Override
	public void onParagraph(PdfWriter writer, Document document, float paragraphPosition) {}
	@Override
	public void onOpenDocument(PdfWriter writer, Document document) {}
	@Override
	public void onGenericTag(PdfWriter writer, Document document, Rectangle rectangle, String text) {}
	
	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		//колонтитулы
		try {
			BaseFont baseFont = PDFUtil.getBaseFont();
			PdfContentByte cb = writer.getDirectContent();
			Font fonth = new Font(baseFont, 10, Font.NORMAL, new BaseColor(153, 153, 153));
			float x = (document.right() - document.left()) / 2 + document.leftMargin();
	        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase("Астролог Наталья Звездочёт", fonth),
	        	x, document.top() + 10, 0);
	        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(String.valueOf(writer.getPageNumber()), fonth),
	        	x, document.bottom() - 15, 0);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCloseDocument(PdfWriter writer, Document document) {}
	
	@Override
	public void onChapterEnd(PdfWriter writer, Document document, float paragraphPosition) {}
	
	@Override
	public void onChapter(PdfWriter writer, Document document, float paragraphPosition, Paragraph title) {
		this.pageByTitle.put(title.getContent(), writer.getPageNumber());
	}

	/**
	 * table to store placeholder for all chapters and sections
	 */
	private final Map<String, PdfTemplate> tocPlaceholder = new HashMap<>();

	public Map<String, PdfTemplate> getTocPlaceholder() {
		return tocPlaceholder;
	}

	/**
	 * store the chapters and sections with their title here
	 */
	private final Map<String, Integer> pageByTitle = new HashMap<>();

	public Map<String, Integer> getPageByTitle() {
		return pageByTitle;
	}
}
