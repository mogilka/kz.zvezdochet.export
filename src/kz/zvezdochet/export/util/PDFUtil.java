package kz.zvezdochet.export.util;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.ElementHandlerPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

import kz.zvezdochet.core.bean.ITextGender;
import kz.zvezdochet.core.bean.TextGender;
import kz.zvezdochet.core.util.PlatformUtil;
import kz.zvezdochet.export.Activator;
import kz.zvezdochet.export.bean.Bar;

/**
 * Набор утилит для pdf-экспорта
 * @author Nataly Didenko
 *
 */
public class PDFUtil {
	/**
	 * Каталог размещения обычных шрифтов
	 */
	public static String FONTDIR = "/usr/share/fonts/truetype/freefont";

	/**
	 * Наименование обычного шрифта
	 */
	public static String FONTFILE = "FreeSans.ttf";
	/**
	 * Наименование символьного шрифта
	 */
	public static String FONTSYMBOLFILE = "Cardo-Regular.ttf";

	/**
	 * Цвет разделов
	 */
	public static BaseColor FONTCOLORH = new BaseColor(51, 51, 102);
	/**
	 * Цвет ссылок и заголовков
	 */
	public static BaseColor FONTCOLOR = new BaseColor(102, 102, 153);
	/**
	 * Цвет подзаголовков
	 */
	public static BaseColor FONTCOLORSUBH = new BaseColor(153, 153, 204);
	/**
	 * Цвет примечаний
	 */
	public static BaseColor FONTCOLORGRAY = new BaseColor(102, 102, 102);
	/**
	 * Цвет критичных сообщений
	 */
	public static BaseColor FONTCOLORED = new BaseColor(153, 0, 51);
	/**
	 * Цвет предупреждений
	 */
	public static BaseColor FONTCOLORYELLOW = new BaseColor(255, 102, 51);
	/**
	 * Цвет позитивных сообщений
	 */
	public static BaseColor FONTGREEN = new BaseColor(0, 102, 51);

	/**
	 * Отображение информации о копирайте
	 * @return Paragraph абзац
	 */
	public static Paragraph printCopyright() {
        Paragraph p = new Paragraph();
		try {
			BaseFont baseFont = getBaseFont();
			Font font = new Font(baseFont, 10, Font.NORMAL);
			Font fonta = new Font(baseFont, 10, Font.UNDERLINE, FONTCOLOR);

	        p.setAlignment(Element.ALIGN_CENTER);
	        Chunk chunk = new Chunk("© 1998-" + Calendar.getInstance().get(Calendar.YEAR) + " Астролог ", font);
	        p.add(chunk);
	        chunk = new Chunk("Наталья Звездочёт", fonta);
	        chunk.setAnchor(WEBSITE);
	        p.add(chunk);
		} catch (Exception e) {
		    e.printStackTrace();
		}
        return p;
	}

	/**
	 * Поиск базового шрифта для кириллицы
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static BaseFont getBaseFont() throws DocumentException, IOException {
		return BaseFont.createFont(FONTDIR + "/" + FONTFILE, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	}

	/**
	 * Поиск шрифта для обычного текста
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getRegularFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, Font.NORMAL);
	}

	/**
	 * Поиск шрифта для гиперссылки
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getLinkFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, Font.UNDERLINE, FONTCOLOR);
	}

	/**
	 * Поиск шрифта для заголовка
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getHeaderFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 14, Font.BOLD, FONTCOLOR);
	}

	public static String AUTHOR = "Наталья Диденко";
	public static String WEBSITE = "https://zvezdochet.guru";

	/**
	 * Поиск метаданных по умолчанию
	 * @param doc документ
	 * @param title название документа
	 */
	public static void getMetaData(Document doc, String title) {
        doc.addTitle(title);
        doc.addSubject("Астрологический сервис Звездочёт");
        doc.addKeywords("гороскоп, прогноз, звездочёт, натальная астрология, сидерическая астрология");
        doc.addAuthor(AUTHOR);
        doc.addCreator(AUTHOR);
        doc.addCreationDate();
	}

    /**
     * Печать заголовка документа
     * @param p абзац-контейнер
     * @param text текст
     * @param anchor анкор
     * http://developers.itextpdf.com/examples/itext-action-second-edition/chapter-5#225-moviecountries1.java
     */
	public static Paragraph printHeader(Paragraph p, String text, String anchor) {
		try {
			BaseFont baseFont = getBaseFont();
			Font font = new Font(baseFont, 18, Font.BOLD, FONTCOLORH);
	        p.setAlignment(Element.ALIGN_CENTER);
	        if (null == anchor)
	        	p.add(new Phrase(text, font));
	        else {
	        	Anchor anchorTarget = new Anchor(text, font);
	        	anchorTarget.setName(anchor);
	        	p.add(anchorTarget);
	        }
			p.add(Chunk.NEWLINE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}

	/**
	 * Печать секции
	 * @param chapter раздел документа
	 * @param title наименование секции
	 * @return секция
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Section printSection(Chapter chapter, String title) throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		Font fonth3 = new Font(baseFont, 16, Font.BOLD, FONTCOLORH);
		Paragraph p = new Paragraph(title, fonth3);
		p.setSpacingBefore(10);
		p.add(Chunk.NEWLINE);
		printHr(p);
		return chapter.addSection(p);
	}

	/**
	 * Печать разделителя
	 * @param p абзац
	 */
	private static void printHr(Paragraph p) {
		p.add(new Chunk(new LineSeparator(2, 100, FONTCOLORH, Element.ALIGN_CENTER, 0)));	
	}

	/**
	 * Конвертация HTML-цвета в базовый
	 * @param htmlColor код HTML-цвета
	 * @return базовый цвет
	 */
	public static BaseColor htmlColor2Base(String htmlColor) {
		BaseColor color = BaseColor.BLACK;
		if (htmlColor.equals("maroon"))
			color = new BaseColor(102, 0, 51);
		else if (htmlColor.equals("teal"))
			color = new BaseColor(0, 102, 102);
		else if (htmlColor.equals("purple"))
			color = new BaseColor(204, 102, 51);
		else if (htmlColor.equals("navy"))
			color = new BaseColor(0, 51, 102);
		return color;
	}

	/**
	 * Поиск заголовка для гендерного текста
	 * @param type тип толкования
	 * @return заголовок
	 */
	public static String getGenderHeader(String type) {
	    if (type.equals("male"))
	    	return "Мужчина";
	    else if (type.equals("female"))
	    	return "Женщина";
	    else if (type.equals("child"))
	    	return "Ребёнок";
	    else if (type.equals("health"))
	    	return "Здоровье";
	    else if (type.equals("love"))
	    	return "Любовь";
	    else if (type.equals("family"))
	    	return "Семья";
	    else if (type.equals("deal"))
	    	return "Сотрудничество";
	    else
	    	return "";
	}

	/**
	 * Возвращает наименование обычного шрифта
	 * @return наименование шрифта
	 */
	public static String getFontName() {
		return "FreeSans";
	}

	/**
	 * Возвращает наименование символьного шрифта
	 * @return наименование шрифта
	 */
	public static String getFontSymbolName() {
		return "Cardo-Regular";
	}

	/**
	 * Генерация круговой диаграммы
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 */
	public static Image printPie(PdfWriter writer, String title, Bar[] bars, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;

		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

			DefaultPieDataset dataset = new DefaultPieDataset();
			for (Bar bar : bars)
				if (bar != null)
					dataset.setValue(bar.getName(), bar.getValue());

		    JFreeChart chart = ChartFactory.createPieChart(title, dataset, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            PiePlot plot = (PiePlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            plot.setOutlineVisible(false);
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            if (legend)
            	chart.getLegend().setItemFont(sfont);

            for (Bar bar : bars) {
            	if (null == bar)
            		continue;
            	Color color = bar.getColor();
            	plot.setSectionPaint(bar.getName(), new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
            	plot.setLabelFont(sfont);
            }
			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Генерация столбцовых диаграмм
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @param vertical true|false вертикальная|горизонтальная диаграмма
	 * @param customColors true|false цвета серий брать из массива значений|цвета по умолчанию
	 * @return изображение диаграммы
	 */
	public static Image printBars(PdfWriter writer, String title, String cattitle, String valtitle, Bar[] bars, 
			float width, float height, boolean legend, boolean vertical, boolean customColors) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;

		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for (Bar bar : bars)
				dataset.setValue(bar.getValue(), bar.getCategory(), bar.getName());

			PlotOrientation orientation = vertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL;
		    JFreeChart chart = ChartFactory.createBarChart(title, cattitle, valtitle, dataset, orientation, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);

            CategoryPlot plot = (CategoryPlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            plot.setOutlineVisible(false);
//            plot.setNoDataMessage("NO DATA!"); TODO

            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            plot.getDomainAxis().setTickLabelFont(sfont);

            if (legend)
            	chart.getLegend().setItemFont(sfont);

            if (customColors) {
            	BarRenderer.setDefaultBarPainter(new StandardBarPainter());
            	((BarRenderer)plot.getRenderer()).setBarPainter(new BarPainter() {
					@Override
					public void paintBarShadow(Graphics2D arg0, BarRenderer arg1, int arg2, int arg3, RectangularShape arg4,
						RectangleEdge arg5, boolean arg6) {}					
					@Override
					public void paintBar(Graphics2D arg0, BarRenderer arg1, int arg2, int arg3, RectangularShape arg4,
						RectangleEdge arg5) {}
				});

	            Paint[] colors = new Paint[bars.length];
				for (int i = 0; i < bars.length; i++) {
					Bar bar = bars[i];
					Color color = bar.getColor();
		            colors[i] = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
				}
	            final CategoryItemRenderer renderer = new CustomRenderer(colors);
//	            renderer.setshDefaultShadowsVisible(false);
	            plot.setRenderer(renderer);
            } else
                ((BarRenderer)plot.getRenderer()).setBarPainter(new StandardBarPainter());

			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Динамическое создание диаграммы с помощью стандартной html-таблицы
	 * @param writer обработчик генерации PDF-файла
	 * @param maxval максимальное число значения
	 * @param bars массив категорий диаграммы
	 * @param title наименование диаграммы
	 * @param baseFont базовый шрифт
	 * @return табличная диаграмма
	 */
	public static PdfPTable printTableChart(PdfWriter writer, double maxval, Bar[] bars, String title) {
        PdfPTable table = new PdfPTable(3);
        float height = 20;
        float factor = 14;
        float maxvalue = (float)maxval * factor;
        try {
	        table.setWidths(new float[] { maxvalue, 20, 120 });
	        table.setSpacingBefore(10);
	        table.setSummary(title);
			BaseFont baseFont = getBaseFont();
	        Font font = new Font(baseFont, 12, Font.NORMAL, BaseColor.BLACK);
			for (Bar bar : bars) {
				double val = bar.getValue();
				//элемент диаграммы
		        PdfTemplate tpl = writer.getDirectContent().createTemplate(maxvalue, height);
		        Color color = bar.getColor();
		        tpl.setColorFill(new BaseColor(color.getRed(), color.getGreen(), color.getBlue()));
		        tpl.rectangle(0, 0, (float)val * factor, height);
		        tpl.fill();
		        writer.releaseTemplate(tpl);
		        PdfPCell cell = new PdfPCell(Image.getInstance(tpl));
		        cell.setBorder(PdfPCell.NO_BORDER);
		        cell.setPadding(2);
		        cell.setBackgroundColor(new BaseColor(230, 230, 250));
		        table.addCell(cell);
	
				//числовое значение
		        cell = new PdfPCell(new Phrase(String.valueOf(val), font));
		        cell.setBorder(PdfPCell.NO_BORDER);
		        cell.setBackgroundColor(new BaseColor(230, 230, 250));
		        table.addCell(cell);
	
				//подпись
		        cell = new PdfPCell(new Phrase(bar.getName(), font));
		        cell.setBorder(PdfPCell.NO_BORDER);
		        cell.setBackgroundColor(new BaseColor(230, 230, 250));
		        table.addCell(cell);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	/**
	 * Конвертация HTML-кода в PDF-текст
	 * @param html HTML-код
	 * @param font шрифт
	 * @return фраза с текстом
	 */
	public static Phrase html2pdf(String html, Font font) {
	    Phrase phrase = new Phrase();
		try {
			//преобразуем html-абзацы в html-блоки
			//чтобы pdf-абзацы выглядели раздельно, а не слитно.
			//если этого не сделать, абзацы будут выполнять роль span, а не p
			if (html != null) {
				html = html.replace("<p>", "<div>")
					.replace("</p>", "</div>");
	
				InputStream is = new ByteArrayInputStream(html.getBytes("UTF-8"));
				FileInputStream fis = new FileInputStream(PlatformUtil.getPath(kz.zvezdochet.export.Activator.PLUGIN_ID, "/export.css").getPath());
			    HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
	            htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
	            ElementList elements = new ElementList();
	            ElementHandlerPipeline pdf = new ElementHandlerPipeline(elements, null);
	            HtmlPipeline pipeline = new HtmlPipeline(htmlContext, pdf);
				CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
				CssFile cssFile = XMLWorkerHelper.getCSS(fis);
				cssResolver.addCss(cssFile);
				CssResolverPipeline css = new CssResolverPipeline(cssResolver, pipeline);
				XMLWorker worker = new XMLWorker(css, true);
			    XMLParser p = new XMLParser(worker);
			    p.parse(is, Charset.forName("UTF-8"));
			    if (null == font)
			    	font = getRegularFont();
			    phrase.setFont(font);
			    phrase.addAll(elements);
			}
		} catch (Exception e) {
			System.out.println(html);
			e.printStackTrace();
		}
		return phrase;
	}

	/**
	 * Динамическое создание диаграммы с помощью стандартной html-таблицы
	 * Генерация диаграмм
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 */
	public static Image printStackChart(PdfWriter writer, String title, String cattitle, String valtitle, Bar[] bars, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;

		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for (Bar bar : bars)
				if (bar != null)
					dataset.setValue(bar.getValue(), bar.getCategory(), bar.getName());

		    JFreeChart chart = ChartFactory.createStackedBarChart(title, cattitle, valtitle, dataset, PlotOrientation.HORIZONTAL, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            CategoryPlot plot = (CategoryPlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            plot.setOutlineVisible(false);
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            plot.getDomainAxis().setTickLabelFont(sfont);
            plot.getRangeAxis().setTickLabelsVisible(false);

            if (legend)
            	chart.getLegend().setItemFont(sfont);

            BarRenderer renderer = (BarRenderer)plot.getRenderer();
            renderer.setBarPainter(new StandardBarPainter());
			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Генерация линейных диаграмм
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 * @link http://www.codejava.net/java-se/graphics/using-jfreechart-to-draw-xy-line-chart-with-xydataset
	 */
	public static Image printGraphics(PdfWriter writer, String title, String cattitle, String valtitle, XYSeriesCollection dataset, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;
		    
		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

		    JFreeChart chart = ChartFactory.createXYLineChart(title, cattitle, valtitle, dataset, PlotOrientation.VERTICAL, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);

            XYPlot plot = (XYPlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            plot.getDomainAxis().setLabelFont(sfont);
            plot.getRangeAxis().setLabelFont(sfont);

            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            for (int i = 0; i < plot.getSeriesCount(); i++)
            	renderer.setSeriesStroke(i, new BasicStroke(4.0f));
            plot.setRenderer(renderer);

            if (legend)
            	chart.getLegend().setItemFont(sfont);
            else
            	chart.getLegend().setVisible(false);

			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Генерация гендерного толкования
	 * @param section подраздел
	 * @param dict справочник
	 * @param female true|false женщина|мужчина
	 * @param child true|false ребёнок|взрослый
	 * @param health true - использовать толкование о здоровье
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void printGender(Section section, ITextGender dict, boolean female, boolean child, boolean health) throws DocumentException, IOException {
		if (dict != null) {
			List<TextGender> genders = dict.getGenderTexts(female, child);
			for (TextGender gender : genders) {
				if (!health && gender.getType().equals("health"))
					continue;
				Paragraph p = new Paragraph(PDFUtil.getGenderHeader(gender.getType()), getSubheaderFont());
				p.setSpacingBefore(10);
				section.add(p);
				section.add(new Paragraph(removeTags(gender.getText(), getRegularFont())));
			};
			section.add(Chunk.NEWLINE);
		}
	}

	/**
	 * Генерация временных диаграмм
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 */
	public static Image printTimeChart(PdfWriter writer, String title, String cattitle, String valtitle, TimeSeriesCollection dataset, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;
		    
		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

		    JFreeChart chart = ChartFactory.createTimeSeriesChart(title, cattitle, valtitle, dataset, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            XYPlot plot = (XYPlot)chart.getPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            plot.getDomainAxis().setLabelFont(sfont);
            plot.getRangeAxis().setLabelFont(sfont);

            if (legend)
            	chart.getLegend().setItemFont(sfont);
            else
            	chart.getLegend().setVisible(false);

            DateAxis axis = (DateAxis)plot.getDomainAxis();
            axis.setDateFormatOverride(new SimpleDateFormat("dd.MM"));
            axis.setAutoTickUnitSelection(false);
            axis.setVerticalTickLabels(true);

            final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
            int scnt = dataset.getSeries().size();
            for (int i = 0; i < scnt; i++)
            	renderer.setSeriesStroke(i, new BasicStroke(3));

			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Поиск базового шрифта для астрологических символов
	 * @return BaseFont базовый шрифт
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static BaseFont getAstroFont() throws DocumentException, IOException {
		String filename = PlatformUtil.getPath(Activator.PLUGIN_ID, "/font/Cardo-Regular.ttf").getPath();
		return BaseFont.createFont(filename, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	}

	/**
	 * Поиск шрифта для подзаголовка
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getHeaderAstroFont() throws DocumentException, IOException {
		BaseFont baseFont = getAstroFont();
		return new Font(baseFont, 14, Font.NORMAL, FONTCOLOR);
	}

	/**
	 * Поиск базового шрифта для спецсимволов
	 * @return BaseFont базовый шрифт
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static Font getHeaderSymbolFont() throws DocumentException, IOException {
		return new Font(Font.getFamily(BaseFont.SYMBOL), 14, Font.NORMAL, FONTCOLOR);
	}

	/**
	 * Поиск шрифта для подзаголовка
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getSubheaderFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, Font.BOLD, FONTCOLOR);
	}

	/**
	 * Поиск шрифта для критичного толкования
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getDangerFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, Font.NORMAL, FONTCOLORED);
	}

	/**
	 * Поиск шрифта для предупреждения
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getWarningFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, Font.NORMAL, FONTCOLORYELLOW);
	}

	/**
	 * Поиск шрифта для примечания
	 * @param italic true - использовать курсив
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getAnnotationFont(boolean italic) throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, italic ? Font.ITALIC : Font.NORMAL, FONTCOLORGRAY);
	}

	/**
	 * Генерация диаграммы Гантта
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param bars массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 * @link https://www.programcreek.com/java-api-examples/index.php?api=org.jfree.chart.util.LineUtilities
	 * @link https://www.programcreek.com/java-api-examples/?api=org.jfree.chart.axis.CategoryAxis
	 * @link http://www.jfree.org/forum/viewtopic.php?t=22805
	 */
	public static Image printGanttChart(PdfWriter writer, String title, String cattitle, String valtitle, IntervalCategoryDataset dataset, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 500;
	        if (0 == height) {
				int cnt = dataset.getRowCount();
				if (cnt < 2)
					height = 100;
				else if (cnt < 5)
					height = 250;
				else if (cnt < 10)
					height = 500;
				else
					height = 600;
	        }

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(FONTDIR);
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;
		    
		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

		    JFreeChart chart = ChartFactory.createGanttChart(title, cattitle, valtitle, dataset, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);
            CategoryPlot plot = chart.getCategoryPlot();
//            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            plot.getDomainAxis().setTickLabelFont(sfont);
            plot.getRangeAxis().setTickLabelFont(sfont);

            if (legend)
            	chart.getLegend().setItemFont(sfont);

            DateAxis axis = (DateAxis)plot.getRangeAxis();
            axis.setDateFormatOverride(new SimpleDateFormat("dd.MM"));
            axis.setAutoTickUnitSelection(false);
            axis.setVerticalTickLabels(true);

            final CategoryItemRenderer renderer = plot.getRenderer();
            java.awt.Color[] colors = getColors();
//            CategoryItemRendererState state = new CategoryItemRendererState(renderer.)
//            Line2D l2d = 
//            Shape shape = renderer.getBaseShape();
//            	BasicStroke stroke = new BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dot, 0.0f);
//            BasicStroke stroke = new BasicStroke(10.0f);
            for (int i = 0; i < dataset.getRowCount(); i++) {
            	java.awt.Color color = i >= colors.length ? java.awt.Color.black : colors[i];
            	renderer.setSeriesPaint(i, color);
//            	renderer.setSeriesStroke(i, stroke);
            }
            plot.getDomainAxis().setCategoryMargin(0.05);
            plot.getDomainAxis().setLowerMargin(0.05);
            plot.getDomainAxis().setUpperMargin(0.05);
//            GanttRenderer grenderer = (GanttRenderer)chart.getXYPlot().getRenderer();
//            grenderer.setItemMargin(0);

			chart.draw(g2d, r2d);
			g2d.dispose();
			return Image.getInstance(tpl);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Возвращает набор цветов для диаграммы Гантта
	 * @return массив AWT цветов
	 */
	private static java.awt.Color[] getColors() {
		return new java.awt.Color[] {
			java.awt.Color.black,
			java.awt.Color.blue,
			java.awt.Color.cyan,
			java.awt.Color.gray,
			java.awt.Color.green,
			java.awt.Color.magenta,
			java.awt.Color.orange,
			java.awt.Color.pink,
			java.awt.Color.red,

			new java.awt.Color(0, 102, 0),		//Very dark green
			new java.awt.Color(102, 51, 0),		//Brown
			new java.awt.Color(102, 0, 153),	//Purple

			new java.awt.Color(102, 102, 51),	//болотный
			new java.awt.Color(0, 102, 102),	//бирюзовый
			new java.awt.Color(102, 102, 153),	//сиреневый
			new java.awt.Color(204, 102, 0),	//оранжевый

			java.awt.Color.white,
			java.awt.Color.yellow,
			java.awt.Color.lightGray,
			java.awt.Color.darkGray,

			new java.awt.Color(255, 102, 102),	//Very light red
			new java.awt.Color(51, 204, 255),	//Very light blue
			new java.awt.Color(51, 153, 255),	//Light blue
			new java.awt.Color(102, 255, 102),	//Very light green
			new java.awt.Color(255, 255, 204),	//Very light yellow
			new java.awt.Color(255, 153, 0),	//Light orange
			new java.awt.Color(255, 204, 51),	//Gold
			new java.awt.Color(153, 102, 0),	//Light brown

			new java.awt.Color(153, 0, 0),		//Very dark red
			new java.awt.Color(102, 102, 51),	//Very dark blue
			new java.awt.Color(0, 153, 0),		//Dark green
			new java.awt.Color(51, 51, 51),		//Very dark grey
			new java.awt.Color(51, 0, 0)		//Dark brown
		};
	}

	/**
	 * Поиск шрифта для позитивного текста
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getSuccessFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, Font.NORMAL, FONTGREEN);
	}

    /**
     * Удаляет из html-текста все теги
     * @param html HTML-текст
     * @param font шрифт
     * @return строка без тегов
     */
    public static Phrase removeTags(String html, Font font) {
    	if (null == html)
    		return null;

		try {
			if (null == font)
				font = getRegularFont();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	if (html.indexOf("<ul>") > -1
    			|| html.indexOf("<ol>") > -1)
    		return html2pdf(html, font);

    	return new Phrase(html.replaceAll("\\<.*?>", ""), font);
	}
}
