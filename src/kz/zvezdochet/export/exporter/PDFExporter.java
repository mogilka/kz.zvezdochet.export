package kz.zvezdochet.export.exporter;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.ChapterAutoNumber;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.ListItem;
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
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import kz.zvezdochet.bean.Event;
import kz.zvezdochet.bean.Place;
import kz.zvezdochet.bean.Sign;
import kz.zvezdochet.core.bean.Model;
import kz.zvezdochet.core.util.DateUtil;
import kz.zvezdochet.core.util.PlatformUtil;
import kz.zvezdochet.export.Activator;
import kz.zvezdochet.export.util.EventStatistics;
import kz.zvezdochet.service.EventService;
import kz.zvezdochet.service.SignService;
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
	private Font font, fonta;

	public PDFExporter(Display display) {
		this.display = display;
		try {
			baseFont = BaseFont.createFont("/usr/share/fonts/truetype/ubuntu-font-family/Ubuntu-R.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//			BaseFont baseFontBold = BaseFont.createFont("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//			FontFamily fontFamily = Font.FontFamily.HELVETICA;
			font = new Font(baseFont, 12, Font.NORMAL);
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
	        writer.setPageEvent(new PageEventHandler(doc));
	        doc.open();

	        //metadata
	        doc.addTitle("Индивидуальный гороскоп");
	        doc.addSubject("Астрологический сервис Звездочёт");
	        doc.addKeywords("гороскоп, звездочёт, сидерическая астрология");
	        doc.addAuthor("Наталья Диденко");
	        doc.addCreator("Наталья Диденко");
	        doc.addCreationDate();

	        //раздел
			Chapter chapter = new ChapterAutoNumber("Общая информация");
			chapter.setNumberDepth(0);

			//дата события
			Paragraph p = new Paragraph();
			Place place = event.getPlace();
			if (null == place)
				place = new Place().getDefault();
			String text = DateUtil.fulldtf.format(event.getBirth()) +
				" " + (event.getZone() >= 0 ? "UTC+" : "") + event.getZone() +
				" " + (event.getDst() >= 0 ? "DST+" : "") + event.getDst() + 
				" " + place.getName() +
				" " + place.getLatitude() + "°" +
				", " + place.getLongitude() + "°";
			printHeader(p, text);
			chapter.add(p);

			chapter.add(new Paragraph("Гороскоп описывает вашу личность как с позиции силы, так и с позиции слабости. "
				+ "Психологи утверждают, что развивать слабые стороны бессмысленно, лучше акцентироваться на достоинствах, это более эффективно. "
				+ "Не зацикливайтесь на недостатках. Искренне занимайтесь тем, к чему лежит душа, используя благоприятные возможности. "
				+ "Судьба и так сделает всё, чтобы помочь вам закалить ваш характер.", font));

			//знаменитости
			printCelebrities(chapter, event.getBirth());
			printSimilar(chapter, event);
			
			//знаки
			EventStatistics statistics = new EventStatistics(event.getConfiguration());
			Map<String, Double> signMap = statistics.getPlanetSigns(true);
			printSigns(writer, chapter, signMap);
			statistics.initPlanetHouses();

			
			
			
			doc.add(chapter);


			//изображение
			String card = PlatformUtil.getPath(Activator.PLUGIN_ID, "/out/horoscope_files/card.png").getPath();
			com.itextpdf.text.Image image2 = com.itextpdf.text.Image.getInstance(card);
			image2.scaleAbsolute(120f, 120f);
			Section section1 = chapter.addSection("section");
			section1.add(image2);
			      
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

    protected class PageEventHandler implements PdfPageEvent {
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
			PdfContentByte cb = writer.getDirectContent();
			Font fonth = new Font(baseFont, 10, Font.NORMAL, new BaseColor(153, 153, 153));
			float y = (doc.right() - doc.left()) / 2 + doc.leftMargin();
	        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase("Астрологический сервис Звездочёт", fonth),
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

    /**
     * @param container
     * @param text
     * http://developers.itextpdf.com/examples/itext-action-second-edition/chapter-5#225-moviecountries1.java
     */
    private void printHeader(Paragraph container, String text) {
		try {
			Font fonth3w = new Font(baseFont, 10, Font.BOLD, new BaseColor(255, 255, 255));

            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.setSpacingBefore(1);
//            table.setPadding(4);
            // t.setSpacing(4);
            // t.setBorderWidth(1);

            PdfPCell cell = new PdfPCell(new Phrase(text, fonth3w));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new BaseColor(153, 153, 204));
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(5);
            table.addCell(cell);
            container.add(table);
			
//	        Chunk chunk = new Chunk(text, fonth3w);
//	        chunk.setBackground(new BaseColor(153, 153, 204));
//	        chunk.setLineHeight(14);
//	        Paragraph p = new Paragraph();
//	        p.setAlignment(Element.ALIGN_CENTER);
//	//        p.setSpacingBefore(1);
//	//        p.setSpacingAfter(1);
//	        p.setLeading(0, 4);
//	        p.add(chunk);
//	//        p.setIndentationLeft(5);
//	//        p.setIndentationRight(5);
//			doc.add(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация знаменитостей
	 * @param date дата события
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void printCelebrities(Chapter chapter, Date date) {
		try {
			List<Event> events = new EventService().findEphemeron(date);
			if (events != null && events.size() > 0) {
				Section section = printSection(chapter, "Однодневки");
				section.add(new Paragraph("В один день с вами родились такие известные люди:", font));

				com.itextpdf.text.List list = new com.itextpdf.text.List(false, false, 10);
				for (Model model : events) {
					Event event = (Event)model;
					ListItem li = new ListItem();
			        Chunk chunk = new Chunk(DateUtil.formatDate(event.getBirth()), font);
			        li.add(chunk);

			        chunk = new Chunk("  ");
			        li.add(chunk);
			        chunk = new Chunk(event.getName(), fonta);
			        chunk.setAnchor(event.getUrl());
			        li.add(chunk);

			        chunk = new Chunk("   " + event.getDescription(), font);
			        li.add(chunk);
			        list.add(li);
				}
				section.add(list);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void printHr(Paragraph paragraph) {
		paragraph.add(new Chunk(new LineSeparator(2, 100, new BaseColor(102, 102, 153), Element.ALIGN_CENTER, 0)));	
	}

	private Section printSection(Chapter chapter, String text) {
		Font fonth3 = new Font(baseFont, 16, Font.BOLD, new BaseColor(102, 102, 153));
		Paragraph p = new Paragraph(text, fonth3);
		p.setSpacingBefore(10);
		p.add(Chunk.NEWLINE);
		printHr(p);
		return chapter.addSection(p);
	}

	/**
	 * Генерация похожих по характеру знаменитостей
	 * @param date дата события
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void printSimilar(Chapter chapter, Event event) {
		try {
			List<Model> events = new EventService().findSimilar(event, 1);
			if (events != null && events.size() > 0) {
				Section section = printSection(chapter, "Близкие по духу");
				section.add(new Paragraph("Известные люди, похожие на вас по характеру:", font));

				com.itextpdf.text.List list = new com.itextpdf.text.List(false, false, 10);
				for (Model model : events) {
					Event man = (Event)model;
					ListItem li = new ListItem();
			        Chunk chunk = new Chunk(DateUtil.formatDate(man.getBirth()), font);
			        li.add(chunk);

			        chunk = new Chunk("  ");
			        li.add(chunk);
			        chunk = new Chunk(man.getName(), fonta);
			        chunk.setAnchor(man.getUrl());
			        li.add(chunk);

			        chunk = new Chunk("   " + man.getDescription(), font);
			        li.add(chunk);
			        list.add(li);
				}
				section.add(list);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация диаграмм знаков
	 * @param cell тег-контейнер для вложенных тегов
	 * @param signMap карта знаков
	 */
	private void printSigns(PdfWriter writer, Chapter chapter, Map<String, Double> signMap) {
		try {
			//выраженные знаки
			Section section = printSection(chapter, "Знаки Зодиака");

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory("/usr/share/fonts/truetype/ubuntu-font-family");
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters("Ubuntu");
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;

		    PdfContentByte cb = writer.getDirectContent();
	        float width = 320;
	        float height = 240;

			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

			DefaultPieDataset dataset = new DefaultPieDataset();
			Iterator<Map.Entry<String, Double>> iterator = signMap.entrySet().iterator();
			SignService service = new SignService();
			List<Sign> signs = new ArrayList<Sign>();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Sign sign = (Sign)service.find(entry.getKey());
		    	signs.add(sign);
				dataset.setValue(sign.getName(), entry.getValue());
		    }

		    String header = "Выраженные знаки Зодиака";
		    JFreeChart chart = ChartFactory.createPieChart(header, dataset, true, true, false);
            java.awt.Font font = new java.awt.Font("Ubuntu", java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            PiePlot plot = (PiePlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(204, 204, 255));
            plot.setOutlineVisible(false);
            java.awt.Font sfont = new java.awt.Font("Ubuntu", java.awt.Font.PLAIN, 10);
            chart.getLegend().setItemFont(sfont);

            for (Sign sign : signs) {
            	Color color = sign.getColor();
            	plot.setSectionPaint(sign.getName(), new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
            	plot.setLabelFont(sfont);
            }
			chart.draw(g2d, r2d);
			g2d.dispose();

			com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(tpl);
			section.add(image);
	
			//кредо
//			tr = new Tag("tr");
//			td = new Tag("td", "class=header id=credo");
//			td.add("Кредо вашей жизни");
//			tr.add(td);
//			cell.add(tr);
//	
//			tr = new Tag("tr");
//			td = new Tag("td");
//			chart = util.getTaggedChart(17, bars2, null);
//			td.add(chart);
//			tr.add(td);
//			cell.add(tr);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
