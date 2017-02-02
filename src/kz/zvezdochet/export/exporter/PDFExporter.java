package kz.zvezdochet.export.exporter;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Calendar;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEvent;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import kz.zvezdochet.bean.Event;
import kz.zvezdochet.bean.Place;
import kz.zvezdochet.core.util.DateUtil;
import kz.zvezdochet.core.util.PlatformUtil;
import kz.zvezdochet.export.Activator;
import kz.zvezdochet.util.Cosmogram;

/**
 * Генератор PDF-файла натальной карты
 * @author Nataly Didenko
 * http://stackoverflow.com/questions/12997739/jfreechart-itext-put-multiple-charts-in-one-pdf
 * http://viralpatel.net/blogs/generate-pie-chart-bar-graph-in-pdf-using-itext-jfreechart/
 * http://viralpatel.net/blogs/generate-pdf-file-in-java-using-itext-jar/
 * http://itextpdf.com/examples/iia.php?id=131
 * http://stackoverflow.com/questions/17825782/how-to-convert-html-to-pdf-using-itext
 * https://github.com/flyingsaucerproject/flyingsaucer/blob/master/flying-saucer-examples/src/main/java/PDFRenderToMultiplePages.java
 * http://itextsupport.com/apidocs/itext5/latest/com/itextpdf/text/package-summary.html
 * http://www.vogella.com/tutorials/JavaPDF/article.html
 * http://developers.itextpdf.com/examples/itext5-building-blocks/chunk-examples
 * http://developers.itextpdf.com/examples/graphics/pattern-colors#1575-gradienttoptobottom.java
 * http://developers.itextpdf.com/question/how-change-line-spacing-text
 * https://sourceforge.net/p/itext/sandbox/ci/c05c80778a0ea01b901b3027d433b77e68f595af/tree/src/sandbox/objects/FitTextInRectangle.java#l45
 * http://developers.itextpdf.com/frequently-asked-developer-questions?id=223
 */
public class PDFExporter {
	private boolean child = false;
	private Display display;
	private BaseFont baseFont;
	private Font font, fonth1, fonth3, fonta;

	public PDFExporter(Display display) {
		this.display = display;
		try {
			baseFont = BaseFont.createFont("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//			BaseFont baseFontBold = BaseFont.createFont("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//			FontFamily fontFamily = Font.FontFamily.HELVETICA;
			font = new Font(baseFont, 12, Font.NORMAL);
			fonth1 = new Font(baseFont, 20, Font.BOLD, new BaseColor(51, 51, 102));
			fonth3 = new Font(baseFont, 16, Font.BOLD, new BaseColor(102, 102, 153));
			fonta = new Font(baseFont, 12, Font.UNDERLINE, new BaseColor(102, 102, 153));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация индивидуального гороскопа
	 * @param event событие
	 */
	public void generate(Event event) {
		child = event.getAge() < event.MAX_TEEN_AGE;
		saveCard(event);
		try {
			Document doc = new Document();
			String filename = PlatformUtil.getPath(Activator.PLUGIN_ID, "/out/horoscope.pdf").getPath();
			PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filename));
	        writer.setPageEvent(new FooterEventHandler(doc));
	        doc.open();

	        //metadata
	        doc.addTitle("Индивидуальный гороскоп");
	        doc.addSubject("Астрологический сервис Звездочёт");
	        doc.addKeywords("гороскоп, звездочёт, сидерическая астрология");
	        doc.addAuthor("Наталья Диденко");
	        doc.addCreator("Наталья Диденко");
	        doc.addCreationDate();

//	        PdfContentByte cb = writer.getDirectContent();
//	        float width = PageSize.A4.getWidth();
//	        float height = PageSize.A4.getHeight();

			//дата события
			Place place = event.getPlace();
			if (null == place)
				place = new Place().getDefault();
			String text = DateUtil.fulldtf.format(event.getBirth()) +
				" " + (event.getZone() >= 0 ? "UTC+" : "") + event.getZone() +
				" " + (event.getDst() >= 0 ? "DST+" : "") + event.getDst() + 
				" " + place.getName() +
				" " + place.getLatitude() + "°" +
				", " + place.getLongitude() + "°";
			printHeader(doc, text);

	        //якорь
	        Anchor anchor = new Anchor("First page of the document.");
	        anchor.setName("BackToTop");
	        Paragraph p = new Paragraph();
	        p.setSpacingBefore(50);
	        p.add(anchor);
	        doc.add(p);

	        //абзац
	        doc.add(new Paragraph("Some more text on the first page with different color and font type.", font));
	        String html = "<ul><li><b>очаровательная, ласковая, приторно-нежная</b>. Застенчивая, немного задиристая, с детскими вспышками раздражения. Считает, что рождена для любви, и может добровольно стать смиренной до рабства. Слишком переоценивает своего мужчину, ищет в нём идеального отца;</li><li><b>дама со строптивым характером</b>, пытающаяся водрузить себя на пьедестал, внушить окружающим чувство недосягаемости своей любви.</li></ul>";
	        InputStream is = new ByteArrayInputStream(html.getBytes());
	        XMLWorkerHelper.getInstance().parseXHtml(writer, doc, is, Charset.forName("UTF-8"));
//	        document.add(new Paragraph(html, font));
	  
	        //глава
	        Paragraph title1 = new Paragraph("Chapter 1", fonth1);
			Chapter chapter1 = new Chapter(title1, 1);
			chapter1.setNumberDepth(0);
			doc.add(chapter1);
			
			//секция
			Paragraph title11 = new Paragraph("This is Section 1 in Chapter 1", fonth3);
			Section section1 = chapter1.addSection(title11);
			Paragraph someSectionText = new Paragraph("This text comes as part of section 1 of chapter 1.");
			section1.add(someSectionText);
			someSectionText = new Paragraph("Following is a 3 X 2 table.");
			section1.add(someSectionText);
				
			//таблица
			PdfPTable t = new PdfPTable(3);
			t.setSpacingBefore(25);
			t.setSpacingAfter(25);
			PdfPCell c1 = new PdfPCell(new Phrase("Header1"));  
			t.addCell(c1);
			PdfPCell c2 = new PdfPCell(new Phrase("Header2"));
			t.addCell(c2);
			PdfPCell c3 = new PdfPCell(new Phrase("Header3"));
			t.addCell(c3);
			t.addCell("1.1");
			t.addCell("1.2");
			t.addCell("1.3");
			section1.add(t);
			      
			//список
			com.itextpdf.text.List l = new com.itextpdf.text.List(true, false, 10);
			l.add(new ListItem("First item of list"));
			l.add(new ListItem("Second item of list"));
			section1.add(l);
			      
			//изображение
			String card = PlatformUtil.getPath(Activator.PLUGIN_ID, "/out/horoscope_files/card.png").getPath();
			com.itextpdf.text.Image image2 = com.itextpdf.text.Image.getInstance(card);
			image2.scaleAbsolute(120f, 120f);
			section1.add(image2);
			      
			//Добавление якоря в основной документ
			Paragraph title2 = new Paragraph("Using Anchor", font);
			section1.add(title2);
			title2.setSpacingBefore(5000);
			Anchor anchor2 = new Anchor("Back To Top");
			anchor2.setReference("#BackToTop");
			section1.add(anchor2);
			doc.add(section1);

	        // Pie chart
//			PdfTemplate pie = cb.createTemplate(width, height);
//			Graphics2D g2d1 = new PdfGraphics2D(pie, width, height);
//			Rectangle2D r2d1 = new Rectangle2D.Double(0, 0, width, height);
//
//			DefaultPieDataset dataset = new DefaultPieDataset();
//			dataset.setValue(String.format("%s, %s", "name", "surname"), 10);
//			dataset.setValue(String.format("%s, %s", "name2", "surname2"), 20);
//
//			JFreeChart chart = ChartFactory.createPieChart("Movies / directors", dataset, true, true, false);
//			chart.draw(g2d1, r2d1);
//			g2d1.dispose();
//			cb.addTemplate(pie, 0, height);
//
//			// Bar chart
//			PdfTemplate bar = cb.createTemplate(width, height);
//			Graphics2D g2d2 = new PdfGraphics2D(bar, width, height);
//			Rectangle2D r2d2 = new Rectangle2D.Double(0, 0, width, height);
//			
//			DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
//			dataset2.setValue(5, "movies", String.format("%s, %s", "name", "surname"));
//			dataset2.setValue(15, "movies", String.format("%s, %s", "name2", "surname2"));
//
//	        chart = ChartFactory.createBarChart("Movies / directors", "Director",
//	        	"# Movies", dataset2, PlotOrientation.HORIZONTAL, false, true, false);
//	        chart.draw(g2d2, r2d2);
//	        g2d2.dispose();
//	        cb.addTemplate(bar, 0, 0);

	        printCopyright(doc);

	        doc.close();			
			
			
			
//			title.add("Индивидуальный гороскоп");
//			head.add(title);
//			html.add(head);
//
//			Tag body = new Tag("body");
//			body.add(printCopyright());
//			Tag table = new Tag("table");
//			body.add(table);
//			body.add(printCopyright());
//			html.add(body);
//	
//			//дата события
//			Tag row = new Tag("tr");
//			Tag cell = new Tag("td", "class=mainheader");
//			cell.add(DateUtil.fulldtf.format(event.getBirth()) +
//				"&ensp;" + (event.getZone() > 0 ? "+" : "") + event.getZone() +
//				"&emsp;" + event.getPlace().getName() +
//				"&ensp;" + event.getPlace().getLatitude() + "&#176;" +
//				", " + event.getPlace().getLongitude() + "&#176;");
//			row.add(cell);
//			table.add(row);
//			
//			//содержание
//			row = new Tag("tr");
//			cell = new Tag("td");
//			generateContents(event, cell);
//			row.add(cell);
//			table.add(row);
//	
//			//знаменитости
//			generateCelebrities(event.getBirth(), table);
//			generateSimilar(event, table);
//
//			//основные диаграммы
//			EventStatistics statistics = new EventStatistics(event.getConfiguration());
//			Map<String, Double> signMap = statistics.getPlanetSigns(true);
//			//знаки
//			generateSignChart(table, signMap);
//			//дома
//			statistics.initPlanetHouses();
////			generateHouseChart(statistics, table);
//			
//			//градус рождения
//			generateDegree(event, table);
//			
//			//планеты в знаках
//			generatePlanetsInSigns(event, table);
//			
//			//космограмма
//			generateCard(event, table);
//			
//			//вид космограммы
//			//generateCardKarma(event, table); 
//
//			//тип космограммы
//			Map<String, Integer> signPlanetsMap = statistics.getSignPlanets();
//			generateCardType(event, table, signPlanetsMap);
//			
//			//выделенность стихий
//			statistics.initPlanetDivisions();
//			statistics.initHouseDivisions();
//			generateElements(event, table, statistics);
//	
//			//выделенность инь-ян
//			generateYinYang(event, table, statistics);
//			
//			//выделенность полусфер
//			generateHalfSpheres(event, table, statistics);
//			
//			//выделенность квадратов
//			signMap = statistics.getPlanetSigns(false);
//			generateSquares(event, table, statistics);
//			
//			//выделенность крестов
//			generateCrosses(event, table, statistics);
//			
//			//выделенность зон
//			generateZones(event, table, statistics);
//			
//			//аспекты
//			generateAspectTypes(event, table);
//			//позитивные аспекты
//			generateAspects(event, table, "Позитивные аспекты планет", "POSITIVE");
//			//негативные аспекты
//			generateAspects(event, table, "Негативные аспекты планет", "NEGATIVE");
//			//конфигурации аспектов
//			//generateAspectConfigurations(event, table);
//			
//			//планеты
//			generatePlanets(event, table);
//			
//			//планеты в домах
//			Map<String, Double> houseMap = statistics.getPlanetHouses();
//			generatePlanetInHouses(event, table, houseMap);
//			//дома в знаках
//			//generateHouseInSigns(event, table, houseMap);
//			document.close();
//			
//			if (html != null) {
////				System.out.println(html);
//				export(html.toString());
//			}
		} catch(Exception e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Ошибка", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Сохранение космограммы в PNG-файл
	 * @param event событие
	 */
	private void saveCard(Event event) {
		try {
		    Image image = new Image(display, Cosmogram.HEIGHT, Cosmogram.HEIGHT);
		    GC gc = new GC(image);
		    gc.setBackground(new Color(display, 254, 250, 248));
		    gc.fillRectangle(image.getBounds());
			new Cosmogram(event.getConfiguration(), null, null, gc);
			ImageLoader loader = new ImageLoader();
		    loader.data = new ImageData[] {image.getImageData()};
		    try {
				String card = PlatformUtil.getPath(Activator.PLUGIN_ID, "/out/horoscope_files/card.png").getPath();
			    loader.save(card, SWT.IMAGE_PNG);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		    image.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Отображение информации о копирайте
	 */
	private void printCopyright(Document doc) {
		try {
			Font font = new Font(baseFont, 10, Font.NORMAL);
			Font fonta = new Font(baseFont, 10, Font.UNDERLINE, new BaseColor(102, 102, 153));

	        Paragraph paragraph = new Paragraph();
	        paragraph.setAlignment(Element.ALIGN_CENTER);
	        Chunk chunk = new Chunk("© 1998-" + Calendar.getInstance().get(Calendar.YEAR) + " Астрологический сервис ", font);
	        paragraph.add(chunk);
	        chunk = new Chunk("Звездочёт", fonta);
	        chunk.setAnchor("https://zvezdochet.guru");
	        paragraph.add(chunk);
	        doc.add(paragraph);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

    private void printBreak(Document doc, int number) {
		try {
			for (int i = 0; i < number; i++)
				doc.add(new Paragraph(" "));
			
//			Chunk linebreak = new Chunk(new DottedLineSeparator());
//			doc.Add(linebreak);  
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }

    public void watermarkText(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        int n = reader.getNumberOfPages();
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        PdfContentByte under = stamper.getUnderContent(1);
        Font f = new Font(FontFamily.HELVETICA, 15);
        Phrase p = new Phrase(
            "This watermark is added UNDER the existing content", f);
        ColumnText.showTextAligned(under, Element.ALIGN_CENTER, p, 297, 550, 0);
        PdfContentByte over = stamper.getOverContent(1);
        p = new Phrase("This watermark is added ON TOP OF the existing content", f);
        ColumnText.showTextAligned(over, Element.ALIGN_CENTER, p, 297, 500, 0);
        p = new Phrase(
            "This TRANSPARENT watermark is added ON TOP OF the existing content", f);
        over.saveState();
        PdfGState gs1 = new PdfGState();
        gs1.setFillOpacity(0.5f);
        over.setGState(gs1);
        ColumnText.showTextAligned(over, Element.ALIGN_CENTER, p, 297, 450, 0);
        over.restoreState();
        stamper.close();
        reader.close();
    }

    public void watermarkImg() {
//    	Document document = new Document();
//    	PdfReader pdfReader = new PdfReader(strFileLocation);
//    	PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileStream(strFileLocationOut, FileMode.Create, FileAccess.Write, FileShare.None));
//    	iTextSharp.text.Image img = iTextSharp.text.Image.GetInstance(WatermarkLocation);
//    	
//    	Rectangle pagesize = reader.getCropBox(pageIndex);
//    	if (pagesize == null)
//    	    pagesize = reader.getMediaBox(pageIndex);
//    	img.SetAbsolutePosition(
//    	    pagesize.GetLeft(),
//    	    pagesize.GetBottom());
//    	
//    	img.SetAbsolutePosition(100, 300);
//    	PdfContentByte waterMark;
//    	for (int pageIndex = 1; pageIndex <= pdfReader.NumberOfPages; pageIndex++) {
//    	    waterMark = pdfStamper.GetOverContent(pageIndex);
//    	    waterMark.AddImage(img);
//    	}
//    	pdfStamper.FormFlattening = true;
//    	pdfStamper.Close();
	}

    protected class FooterEventHandler implements PdfPageEvent {
        protected Document doc;

        public FooterEventHandler(Document doc) {
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
			PdfContentByte cb = writer.getDirectContent();
			Font fonth = new Font(baseFont, 10, Font.NORMAL, new BaseColor(153, 153, 153));
			float y = (doc.right() - doc.left()) / 2 + doc.leftMargin();
	        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase("Звездочёт", fonth),
	        	y, doc.top() + 10, 0);
	        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(String.valueOf(writer.getPageNumber()), fonth),
	        	y, doc.bottom(), 0);
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

    private void printHeader(Document doc, String text) {
		try {
	        printBreak(doc, 1);
			Font fonth3w = new Font(baseFont, 10, Font.BOLD, new BaseColor(255, 255, 255));
	        Chunk chunk = new Chunk(text, fonth3w);
	        chunk.setBackground(new BaseColor(153, 153, 204));
	        chunk.setLineHeight(14);
	        Paragraph p = new Paragraph();
	        p.setAlignment(Element.ALIGN_CENTER);
	//        p.setSpacingBefore(1);
	//        p.setSpacingAfter(1);
	        p.setLeading(0, 4);
	        p.add(chunk);
	//        p.setIndentationLeft(5);
	//        p.setIndentationRight(5);
			doc.add(p);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
