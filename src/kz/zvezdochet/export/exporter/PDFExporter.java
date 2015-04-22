package kz.zvezdochet.export.exporter;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import kz.zvezdochet.bean.Event;
import kz.zvezdochet.core.util.PlatformUtil;
import kz.zvezdochet.export.Activator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * Генератор PDF-файла натальной карты
 * @author Nataly Didenko
 *
 */
public class PDFExporter {
	private boolean child = false;
	private Display display;
	private Font font, fonth1, fonth3;

	public PDFExporter(Display display) {
		this.display = display;
		try {
			BaseFont baseFont = BaseFont.createFont("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			BaseFont baseFontBold = BaseFont.createFont("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			font = new Font(baseFont, 12, Font.NORMAL);
			fonth1 = new Font(baseFontBold, 20, Font.BOLD, new BaseColor(51, 51, 102));
			fonth3 = new Font(baseFontBold, 16, Font.BOLD, new BaseColor(102, 102, 153));
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Генерация индивидуального гороскопа
	 * @param event событие
	 * http://stackoverflow.com/questions/12997739/jfreechart-itext-put-multiple-charts-in-one-pdf
	 * http://viralpatel.net/blogs/generate-pie-chart-bar-graph-in-pdf-using-itext-jfreechart/
	 * http://viralpatel.net/blogs/generate-pdf-file-in-java-using-itext-jar/
	 * http://itextpdf.com/examples/iia.php?id=131
	 */
	public void generate(Event event) {
		child = event.getAge() < event.MAX_TEEN_AGE;
		try {
			Document document = new Document();
			String pdf = PlatformUtil.getPath(Activator.PLUGIN_ID, "/out/horoscope.pdf").getPath();
	        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdf));
	        document.open();

	        PdfContentByte cb = writer.getDirectContent();
	        float width = PageSize.A4.getWidth();
	        float height = PageSize.A4.getHeight();
	        
	        //якорь
	        Anchor anchorTarget = new Anchor("First page of the document.");
	        anchorTarget.setName("BackToTop");
	        Paragraph paragraph1 = new Paragraph();
	        paragraph1.setSpacingBefore(50);
	        paragraph1.add(anchorTarget);
	        document.add(paragraph1);

	        //абзац
	        document.add(new Paragraph("Some more text on the first page with different color and font type.", font));
	        String html = "<ul><li><b>очаровательная, ласковая, приторно-нежная</b>. Застенчивая, немного задиристая, с детскими вспышками раздражения. Считает, что рождена для любви, и может добровольно стать смиренной до рабства. Слишком переоценивает своего мужчину, ищет в нём идеального отца;</li><li><b>дама со строптивым характером</b>, пытающаяся водрузить себя на пьедестал, внушить окружающим чувство недосягаемости своей любви.</li></ul>";
	        InputStream is = new ByteArrayInputStream(html.getBytes());
	        XMLWorkerHelper.getInstance().parseXHtml(writer, document, is, Charset.forName("UTF-8"));
	        //document.add(new Paragraph(html, font));
	  
	        //глава
	        Paragraph title1 = new Paragraph("Chapter 1", fonth1);
			Chapter chapter1 = new Chapter(title1, 1);
			chapter1.setNumberDepth(0);
			document.add(chapter1);
			
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
			String card = PlatformUtil.getPath(Activator.PLUGIN_ID, "/out/card.png").getPath();
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
			document.add(section1);

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
		        
	        document.close();			
			
			
			
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
}
