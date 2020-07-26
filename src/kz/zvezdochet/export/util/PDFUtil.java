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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.CategoryStyler;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler.LegendLayout;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.Styler.TextAlignment;
import org.knowm.xchart.style.XYStyler;

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
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
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
import kz.zvezdochet.core.util.OsUtil;
import kz.zvezdochet.core.util.PlatformUtil;
import kz.zvezdochet.core.util.Translit;
import kz.zvezdochet.export.Activator;
import kz.zvezdochet.export.bean.Bar;
import kz.zvezdochet.export.handler.PageEventHandler;

/**
 * Набор утилит для pdf-экспорта
 * @author Natalie Didenko
 *
 */
public class PDFUtil {

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
	public static BaseColor FONTCOLORGRAY = new BaseColor(153, 153, 153);
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
	 * Цвет нейтральных сообщений
	 */
	public static BaseColor FONTNEUTRAL = new BaseColor(51, 102, 153);

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
	        Chunk chunk = new Chunk("© 2014-" + Calendar.getInstance().get(Calendar.YEAR) + " Астролог ", font);
	        p.add(chunk);
	        chunk = new Chunk("Наталья Звездочёт", fonta);
	        chunk.setAnchor(WEBSITE);
	        p.add(chunk);
	        p.add(Chunk.NEWLINE);
	        p.add(new Chunk("Все права защищены", font));
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
		String fontdir = getFontDir();
		return BaseFont.createFont(fontdir + "/" + FONTFILE, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
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

	public static String AUTHOR = "Наталья Звездочёт";
	public static String WEBSITE = "https://zvezdochet.guru";

	/**
	 * Поиск метаданных по умолчанию
	 * @param doc документ
	 * @param title название документа
	 */
	public static void getMetaData(Document doc, String title) {
        doc.addTitle(title);
        doc.addSubject(title);
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
	        Chunk chunk = new Chunk(text, font).setLocalDestination(Translit.convert(text, true));
	        if (null == anchor)
	        	p.add(chunk);
	        else {
	        	Anchor anchorTarget = new Anchor(chunk);
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
     * @param anchor анкор
	 * @return секция
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Section printSection(Chapter chapter, String title, String anchor) throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		Font fonth3 = new Font(baseFont, 16, Font.BOLD, FONTCOLORH);
		Paragraph p = new Paragraph("", fonth3);

        if (null == anchor)
        	p.add(new Phrase(title, fonth3));
        else {
        	Anchor anchorTarget = new Anchor(title, fonth3);
        	anchorTarget.setName(anchor);
        	p.add(anchorTarget);
        }

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
		if (!OsUtil.getOS().equals(OsUtil.OS.LINUX))
			return printPie2(writer, title, bars, width, height, legend);
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(getFontDir());
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
	 * @return изображение диаграммы Image|PdfTable
	 */
	public static Element printBars(PdfWriter writer, String title, String cattitle, String valtitle, Bar[] bars, 
			float width, float height, boolean legend, boolean vertical, boolean customColors) {
		if (!OsUtil.getOS().equals(OsUtil.OS.LINUX))
			return printTableChart(writer, bars, title, false);
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(getFontDir());
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

            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 9);
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
	 * @param bars массив категорий диаграммы
	 * @param title наименование диаграммы
	 * @param showValue true|false отобразить|скрыть числовое значение
	 * @return табличная диаграмма
	 */
	public static PdfPTable printTableChart(PdfWriter writer, Bar[] bars, String title, boolean showValue) {
        PdfPTable table = new PdfPTable(3);
        float height = 20;

        BaseColor[] colors = { BaseColor.RED, BaseColor.YELLOW, BaseColor.GREEN, BaseColor.BLUE,
        	BaseColor.ORANGE, BaseColor.PINK, BaseColor.CYAN, BaseColor.MAGENTA,
        	BaseColor.BLACK, BaseColor.DARK_GRAY, BaseColor.GRAY, BaseColor.LIGHT_GRAY, BaseColor.WHITE};

        double maxval = 0;
        for (Bar bar : bars) {
        	if (null == bar)
        		continue;
	    	double val = bar.getValue();
	    	if (val < 0.1)
	    		bar.setValue(0.1);
	    	if (val > maxval)
	    		maxval = val;
        }
        int MAX_VALUE = 20;
        float factor = (maxval >= MAX_VALUE) ? 7 : 14;
        float maxvalue = (float)maxval * factor;

        try {
	        table.setWidths(new float[] { maxvalue, 20, 180 });
	        table.setSpacingBefore(10);
	        table.setSummary(title);
			BaseFont baseFont = getBaseFont();
	        Font font = new Font(baseFont, 10, Font.NORMAL, BaseColor.BLACK);
	        BaseColor bcolor = new BaseColor(230, 230, 250);
	        int i = -1;
			for (Bar bar : bars) {
	        	if (null == bar)
	        		continue;
				double val = bar.getValue();
				//элемент диаграммы
		        PdfTemplate tpl = writer.getDirectContent().createTemplate(maxvalue, height);
		        Color color = bar.getColor();
		        tpl.setColorFill((null == color) ? colors[++i] : new BaseColor(color.getRed(), color.getGreen(), color.getBlue()));
		        tpl.rectangle(0, 0, (float)val * factor, height);
		        tpl.fill();
		        writer.releaseTemplate(tpl);
		        PdfPCell cell = new PdfPCell(Image.getInstance(tpl));
		        cell.setBorder(PdfPCell.NO_BORDER);
		        cell.setPadding(2);
		        cell.setBackgroundColor(bcolor);
		        table.addCell(cell);

				//числовое значение
		        String sval = showValue? String.valueOf(val) : "";
		        cell = new PdfPCell(new Phrase(sval, font));
		        cell.setBorder(PdfPCell.NO_BORDER);
		        cell.setBackgroundColor(bcolor);
		        table.addCell(cell);
	
				//подпись
		        cell = new PdfPCell(new Phrase(bar.getName(), font));
		        cell.setBorder(PdfPCell.NO_BORDER);
		        cell.setBackgroundColor(bcolor);
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
		if (!OsUtil.getOS().equals(OsUtil.OS.LINUX))
			return removeTags(html, font);
		
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
		if (!OsUtil.getOS().equals(OsUtil.OS.LINUX))
			return printStackChart2(writer, title, cattitle, valtitle, bars, width, height, legend, false);
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(getFontDir());
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
	 * @param map массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 * @link http://www.codejava.net/java-se/graphics/using-jfreechart-to-draw-xy-line-chart-with-xydataset
	 */
	@SuppressWarnings("unchecked")
	public static Image printGraphics(PdfWriter writer, String title, String cattitle, String valtitle, Map<String, Object[]> map, float width, float height, boolean legend) {
		if (!OsUtil.getOS().equals(OsUtil.OS.LINUX))
			return printGraphics2(writer, title, cattitle, valtitle, map, width, height, legend);
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(getFontDir());
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;
		    
		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

			XYSeriesCollection dataset = new XYSeriesCollection();
			for (Map.Entry<String, Object[]> entry : map.entrySet()) {
		        XYSeries series = new XYSeries(entry.getKey());
				Object[] arr = entry.getValue();
				List<Integer> names = (List<Integer>)arr[0];
				List<Double> values = (List<Double>)arr[1];
        		for (int i = 0; i < names.size(); i++)
					series.add(names.get(i), values.get(i));
        		dataset.addSeries(series);
			}

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
	 * @param dataset массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 */
	public static Image printTimeChart(PdfWriter writer, String title, String cattitle, String valtitle, TimeSeriesCollection dataset, float width, float height, boolean legend) {
		if (!OsUtil.getOS().equals(OsUtil.OS.LINUX))
			return printTimeChart2(writer, title, cattitle, valtitle, dataset, width, height, legend);
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(getFontDir());
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

            final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            int scnt = dataset.getSeries().size();
            for (int i = 0; i < scnt; i++)
            	renderer.setSeriesStroke(i, new BasicStroke(4.0f));
            plot.setRenderer(renderer);
                
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
		String filename = getFontDir() + "/Cardo-Regular.ttf";
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
		    mapper.insertDirectory(getFontDir());
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
    	html = html.replaceAll("<li>", "<p>   • ").replaceAll("</li>", "</p>");
    	return new Phrase(html.replaceAll("\\<.*?>", ""), font);
	}

	/**
	 * Поиск шрифта для нейтрального текста
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getNeutralFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 12, Font.NORMAL, FONTNEUTRAL);
	}

	/**
	 * Генерация гендерного толкования в виде фразы для табличной ячейки
	 * @param dict справочник
	 * @param female true|false женщина|мужчина
	 * @param child true|false ребёнок|взрослый
	 * @param health true - использовать толкование о здоровье
	 * @return Phrase фраза
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Phrase printGenderCell(ITextGender dict, boolean female, boolean child, boolean health) throws DocumentException, IOException {
		if (dict != null) {
			Phrase phrase = new Phrase();
			List<TextGender> genders = dict.getGenderTexts(female, child);
			for (TextGender gender : genders) {
				if (!health && gender.getType().equals("health"))
					continue;
				Paragraph p = new Paragraph(PDFUtil.getGenderHeader(gender.getType()), getSubheaderFont());
				phrase.add(p);
				phrase.add(Chunk.NEWLINE);
				phrase.add(Chunk.NEWLINE);
				String html = gender.getText();
				phrase.add(new Paragraph(removeTags(html, getRegularFont())));
			};
			phrase.add(Chunk.NEWLINE);
			return phrase;
		}
		return null;
	}

	/**
	 * Ширина бокового поля документа по умолчанию
	 */
	public static float PAGEBORDERWIDTH = 40;

	/**
	 * Задание ширины границ ячейки
	 * @param cell ячейка
	 * @param top верхняя граница
	 * @param right правая граница
	 * @param bottom нижняя граница
	 * @param left левая граница
	 */
	public static void setCellBorderWidths(PdfPCell cell, float top, float right, float bottom, float left) {
		cell.setBorderWidthTop(top);
		cell.setBorderWidthRight(right);
		cell.setBorderWidthBottom(bottom);
		cell.setBorderWidthLeft(left);
	}

	/**
	 * Разбивка большого текста на порции для размещения в табличной ячейке
	 * @param html HTML-текст
	 * @return массив частей текста
	 */
	public static List<String> splitHtml(String html) {
		int LIMIT = 2220;
		List<String> parts = new ArrayList<String>();
		if (html.length() <= LIMIT) {
			parts.add(html);
			return parts;
		}

		Matcher m = Pattern.compile("(?=(</p>))").matcher(html);
		List<Integer> pos = new ArrayList<Integer>();
		while (m.find()) 
		    pos.add(m.start());

		List<Integer> pos2 = new ArrayList<Integer>();
		for (int i = 0; i < pos.size(); i++) {
			int p = pos.get(i);
			int l = pos2.isEmpty() ? LIMIT : LIMIT + pos2.get(pos2.size() - 1);
			if (p >= l)
				if (i > 0)
					pos2.add(pos.get(i - 1));
		}
		pos2.add(html.length());

		int steps = pos2.size();
		for (int i = 0; i < steps; i++) {
			int beginIndex = (0 == i) ? 0 : pos2.get(i - 1) + 4;
			int endIndex = pos2.get(i) + 4;
			parts.add(i == steps - 1
				? html.substring(beginIndex)
				: html.substring(beginIndex, endIndex));
		}
		return parts;
	}

	/**
	 * Выравнивание текста ячейки вертикально по центру
	 * @param cell ячейка
	 * @param fontSize размер шрифта
	 * @param capHeight размер заглавных символов шрифта
	 */
	public static void setCellVertical(PdfPCell cell, float fontSize, float capHeight) {
		cell.setPaddingBottom(2 * (fontSize - capHeight));
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE); 
	}

	/**
	 * Генерация текстового толкования в виде фразы для табличной ячейки
	 * @param html HTML-текст
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Phrase printTextCell(String html) throws DocumentException, IOException {
		Phrase phrase = new Phrase();
		html = html.replace("<ul>", "<div>")
				.replace("</ul>", "</div>")
			.replace("<ol>", "<div>")
				.replace("</ol>", "</div>")
			.replace("<li>", "<p> • ")
				.replace("</li>", "</p>");

		phrase.add(new Paragraph(removeTags(html, getRegularFont())));
		return phrase;
	}

	/**
	 * Печать секции-подраздела
	 * @param section раздел документа
	 * @param title наименование секции
     * @param anchor анкор
	 * @return секция
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Section printSubsection(Section section, String title, String anchor) throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		Font fonth3 = new Font(baseFont, 14, Font.BOLD, FONTCOLORH);

		Paragraph p = new Paragraph("", fonth3);

        if (null == anchor)
        	p.add(new Phrase(title, fonth3));
        else {
        	Anchor anchorTarget = new Anchor(title, fonth3);
        	anchorTarget.setName(anchor);
        	p.add(anchorTarget);
        }
		p.setSpacingBefore(10);
		p.add(Chunk.NEWLINE);
		printHr(p);
		return section.addSection(p);
	}

	/**
	 * Поиск шрифта для мелкого текста
	 * @return Font шрифт
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Font getSmallFont() throws DocumentException, IOException {
		BaseFont baseFont = getBaseFont();
		return new Font(baseFont, 10, Font.NORMAL);
	}

	/**
	 * Поиск директории с шрифтами
	 * @return String каталог
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static String getFontDir() throws DocumentException, IOException {
		return PlatformUtil.getPath(Activator.PLUGIN_ID, "/font").getPath();
	}

	public static void printTOC(Document document, PageEventHandler handler) throws DocumentException {
		try {
	        Chapter intro = new Chapter(printHeader(new Paragraph(), "Содержание", null), 0);
	        intro.setNumberDepth(0);
	        document.add(intro);
	
	        Map<String, Integer> pageByTitle = handler.getPageByTitle();
	        final Map<String, PdfTemplate> tocPlaceholder = handler.getTocPlaceholder();
	
		    for (Map.Entry<String, Integer> entry : pageByTitle.entrySet()) {
	            final String title = entry.getKey();
	            final Chunk chunk = new Chunk(title, getRegularFont()).setLocalGoto(Translit.convert(title, true));
	            document.add(new Paragraph(chunk));
	
	            // Add a placeholder for the page reference
	            document.add(new VerticalPositionMark() {
	                @Override
	                public void draw(final PdfContentByte canvas, final float llx, final float lly, final float urx, final float ury, final float y) {
	                    final PdfTemplate createTemplate = canvas.createTemplate(50, 50);
	                    tocPlaceholder.put(title, createTemplate);
	                    canvas.addTemplate(createTemplate, urx - 50, y);
	                }
	            });
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	/**
	 * Генерация линейных диаграмм с датами
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
	public static Image printLineChart(PdfWriter writer, String title, String cattitle, String valtitle, CategoryDataset dataset, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

		    DefaultFontMapper mapper = new DefaultFontMapper();
		    mapper.insertDirectory(getFontDir());
		    String fontname = getFontName();
		    DefaultFontMapper.BaseFontParameters pp = mapper.getBaseFontParameters(fontname);
		    if (pp != null)
		        pp.encoding = BaseFont.IDENTITY_H;
		    
		    PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tpl = cb.createTemplate(width, height);
			Graphics2D g2d = new PdfGraphics2D(tpl, width, height, mapper);
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);

		    JFreeChart chart = ChartFactory.createLineChart(title, cattitle, valtitle, dataset, PlotOrientation.VERTICAL, legend, true, false);
            java.awt.Font font = new java.awt.Font(fontname, java.awt.Font.PLAIN, 12);
            chart.getTitle().setFont(font);

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(new java.awt.Color(230, 230, 250));
            java.awt.Font sfont = new java.awt.Font(fontname, java.awt.Font.PLAIN, 10);
            CategoryAxis axis = plot.getDomainAxis();
            axis.setLabelFont(sfont);
            plot.getRangeAxis().setLabelFont(sfont);

            int count = dataset.getColumnCount();
            if (count > 5)
            	axis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            for (int i = 0; i < count; i++)
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
	 * Генерация толкования по типу в виде фразы для табличной ячейки
	 * @param dict справочник
	 * @param type love|deal
	 * @return Phrase фраза
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static Phrase printGenderCell(ITextGender dict, String type) throws DocumentException, IOException {
		if (dict != null) {
			Phrase phrase = new Phrase();
			TextGender gender = dict.getGenderText(type);
			if (null == gender)
				return null;
			
			phrase.add(Chunk.NEWLINE);
			Paragraph p = new Paragraph(PDFUtil.getGenderHeader(gender.getType()), getSubheaderFont());
			phrase.add(p);
			phrase.add(Chunk.NEWLINE);
			phrase.add(Chunk.NEWLINE);
			String html = gender.getText();
			phrase.add(new Paragraph(removeTags(html, getRegularFont())));
			phrase.add(Chunk.NEWLINE);
			return phrase;
		}
		return null;
	}

	/**
	 * Генерация толкования по типу
	 * @param dict справочник
	 * @param type love|deal
	 * @return Phrase фраза
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void printGender(Section section, ITextGender dict, String type) throws DocumentException, IOException {
		if (dict != null) {
			TextGender gender = dict.getGenderText(type);
			if (null == gender)
				return;

			section.add(Chunk.NEWLINE);
			Paragraph p = new Paragraph(PDFUtil.getGenderHeader(gender.getType()), getSubheaderFont());
			section.add(p);
			section.add(Chunk.NEWLINE);
			String html = gender.getText();
			section.add(new Paragraph(removeTags(html, getRegularFont())));
			section.add(Chunk.NEWLINE);
		}
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
	 * @link https://knowm.org/open-source/xchart/xchart-example-code
	 */
	public static Image printPie2(PdfWriter writer, String title, Bar[] bars, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

			PieChart chart = new PieChartBuilder().width((int)width).height((int)height).title(title).build();
			PieStyler styler = chart.getStyler();
            java.awt.Font sfont = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 10);

            legend = true;
			if (legend) {
				styler.setLegendVisible(true);
				styler.setLegendFont(sfont);
				styler.setLegendBackgroundColor(new java.awt.Color(240, 240, 230));
				styler.setLegendBorderColor(java.awt.Color.WHITE);
				boolean big = bars.length > 6;
				if (big)
					styler.setLegendPadding(1);
			}
			styler.setChartTitleFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12));
			styler.setHasAnnotations(false);
			styler.setChartBackgroundColor(java.awt.Color.WHITE);
			styler.setPlotBackgroundColor(new java.awt.Color(230, 230, 250));
			styler.setPlotBorderColor(java.awt.Color.WHITE);

			java.awt.Color[] colors = { java.awt.Color.RED, java.awt.Color.YELLOW, java.awt.Color.GREEN, java.awt.Color.BLUE,
				java.awt.Color.ORANGE, java.awt.Color.PINK, java.awt.Color.CYAN, java.awt.Color.MAGENTA,
				java.awt.Color.BLACK, java.awt.Color.DARK_GRAY, java.awt.Color.GRAY, java.awt.Color.LIGHT_GRAY, java.awt.Color.WHITE};

			java.awt.Color[] sliceColors = new java.awt.Color[bars.length];
			for (int i = 0; i < bars.length; i++) {
				Bar bar = bars[i];
				if (bar != null) {
					Color color = bar.getColor();
					sliceColors[i] = (null == color)
						? colors[i]
						: new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
					chart.addSeries(bar.getName(), bar.getValue());
				}
			}
			styler.setSeriesColors(sliceColors);

	        Image image = null;
	        try {
	        	String filename = PlatformUtil.getPath(Activator.PLUGIN_ID, "/tmp/pie.png").getPath();
	            BitmapEncoder.saveBitmapWithDPI(chart, filename, BitmapFormat.PNG, 300);
	            image = Image.getInstance(filename);
	            image.scaleAbsolute(width, height);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        } catch (DocumentException e1) {
	        	e1.printStackTrace();
	        }
			return image;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
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
	 * @param vertical true|false вертикальные|горизонтальные подписи
	 * @return изображение диаграммы
	 */
	@SuppressWarnings("unchecked")
	public static Image printStackChart2(PdfWriter writer, String title, String cattitle, String valtitle, Bar[] bars, float width, float height, boolean legend, boolean vertical) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

	        CategoryChart chart = new CategoryChartBuilder().width((int)width).height((int)height).title(title).xAxisTitle(cattitle).yAxisTitle(valtitle).build();
//	        CategoryChart chart = new CategoryChartBuilder().width((int)width).height((int)height).title(title).xAxisTitle(cattitle).yAxisTitle(valtitle).theme(ChartTheme.GGPlot2).build();
			CategoryStyler styler = chart.getStyler();
            java.awt.Font sfont = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 10);

			if (legend) {
				styler.setLegendVisible(true);
				styler.setLegendFont(sfont);
				styler.setLegendBackgroundColor(java.awt.Color.WHITE);
				styler.setLegendBorderColor(java.awt.Color.DARK_GRAY);				
				styler.setLegendPosition(LegendPosition.OutsideS);
				styler.setLegendLayout(LegendLayout.Horizontal);
			}
			styler.setChartTitleFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12));
			styler.setChartBackgroundColor(java.awt.Color.WHITE);
			styler.setPlotBackgroundColor(new java.awt.Color(230, 230, 250));
			styler.setPlotBorderColor(java.awt.Color.WHITE);
			styler.setOverlapped(true);
			styler.setXAxisTitleVisible(false);
			if (vertical) {
				styler.setXAxisLabelRotation(60);
				styler.setXAxisLabelAlignmentVertical(TextAlignment.Right);
			}
			styler.setAxisTickLabelsFont(sfont);
			styler.setYAxisTitleVisible(false);
			styler.setYAxisTicksVisible(vertical);

	        Map<String, Object[]> map = new HashMap<>();
			for (Bar bar : bars) {
				if (bar != null) {
					Object[] arr = map.get(bar.getCategory());
					List<String> names = null;
					List<Double> values = null;
					if (null == arr) {
						names = new ArrayList<String>();
						values = new ArrayList<Double>();
					} else {
						names = (List<String>)arr[0];
						values = (List<Double>)arr[1];
					}
					names.add(bar.getName());
					values.add(bar.getValue());
					map.put(bar.getCategory(), new Object[] {names, values});
				}
			}

			for (Map.Entry<String, Object[]> entry : map.entrySet()) {
				Object[] arr = entry.getValue();
				List<String> names = (List<String>)arr[0];
				List<Double> values = (List<Double>)arr[1];
				chart.addSeries(entry.getKey(), names, values);
			}

	        Image image = null;
	        try {
	        	String filename = PlatformUtil.getPath(Activator.PLUGIN_ID, "/tmp/stack.png").getPath();
	            BitmapEncoder.saveBitmapWithDPI(chart, filename, BitmapFormat.PNG, 300);
	            image = Image.getInstance(filename);
	            image.scaleAbsolute(width, height);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        } catch (DocumentException e1) {
	        	e1.printStackTrace();
	        }
			return image;
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
	 * @param map массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 * @link http://www.codejava.net/java-se/graphics/using-jfreechart-to-draw-xy-line-chart-with-xydataset
	 */
	@SuppressWarnings("unchecked")
	public static Image printGraphics2(PdfWriter writer, String title, String cattitle, String valtitle, Map<String, Object[]> map, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

	        XYChart chart = new XYChartBuilder().width((int)width).height((int)height).title(title).build();
			XYStyler styler = chart.getStyler();
            java.awt.Font sfont = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 10);

			if (legend) {
				styler.setLegendVisible(true);
				styler.setLegendFont(sfont);
				styler.setLegendBackgroundColor(new java.awt.Color(240, 240,230));
				styler.setLegendBorderColor(java.awt.Color.WHITE);				
				styler.setLegendPosition(LegendPosition.OutsideS);
				styler.setLegendLayout(LegendLayout.Horizontal);
			}
			styler.setChartTitleFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12));
			styler.setChartBackgroundColor(java.awt.Color.WHITE);
			styler.setPlotBackgroundColor(new java.awt.Color(230, 230, 250));
			styler.setPlotBorderColor(java.awt.Color.WHITE);

			for (Map.Entry<String, Object[]> entry : map.entrySet()) {
				Object[] arr = entry.getValue();
				List<Integer> names = (List<Integer>)arr[0];
				List<Double> values = (List<Double>)arr[1];
				chart.addSeries(entry.getKey(), names, values);
			}

	        Image image = null;
	        try {
	        	String filename = PlatformUtil.getPath(Activator.PLUGIN_ID, "/tmp/graph.png").getPath();
	            BitmapEncoder.saveBitmapWithDPI(chart, filename, BitmapFormat.PNG, 300);
	            image = Image.getInstance(filename);
	            image.scaleAbsolute(width, height);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        } catch (DocumentException e1) {
	        	e1.printStackTrace();
	        }
			return image;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Генерация временных диаграмм
	 * @param writer обработчик генерации PDF-файла
	 * @param title заголовок диаграммы
	 * @param cattitle заголовок категории
	 * @param valtitle заголовок значения
	 * @param dataset массив значений
	 * @param width ширина диаграммы
	 * @param height высота диаграммы
	 * @param legend true|false присутствие|отсутствие легенды
	 * @return изображение диаграммы
	 */
	@SuppressWarnings("unchecked")
	public static Image printTimeChart2(PdfWriter writer, String title, String cattitle, String valtitle, TimeSeriesCollection dataset, float width, float height, boolean legend) {
		try {
	        if (0 == width)
	        	width = 320;
	        if (0 == height)
	        	height = 240;

	        XYChart chart = new XYChartBuilder().width((int)width).height((int)height).title(title).build();
			XYStyler styler = chart.getStyler();
            java.awt.Font sfont = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 10);

			if (legend) {
				styler.setLegendVisible(true);
				styler.setLegendFont(sfont);
				styler.setLegendBackgroundColor(new java.awt.Color(240, 240,230));
				styler.setLegendBorderColor(java.awt.Color.WHITE);				
				styler.setLegendPosition(LegendPosition.OutsideS);
				styler.setLegendLayout(LegendLayout.Horizontal);
			}
			styler.setChartTitleFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12));
			styler.setChartBackgroundColor(java.awt.Color.WHITE);
			styler.setPlotBackgroundColor(new java.awt.Color(230, 230, 250));
			styler.setPlotBorderColor(java.awt.Color.WHITE);
			styler.setDatePattern("dd.MM");
			styler.setAxisTickLabelsFont(sfont);

			for (Object object : dataset.getSeries()) {
				TimeSeries series = (TimeSeries)object;
				List<TimeSeriesDataItem> items = (List<TimeSeriesDataItem>)series.getItems();
				List<Date> names = new ArrayList<Date>();
				List<Number> values = new ArrayList<Number>();
				for (TimeSeriesDataItem tsdi : items) {
					names.add(tsdi.getPeriod().getStart());
					values.add(tsdi.getValue());
				}
				chart.addSeries(series.getKey().toString(), names, values);
			}

	        Image image = null;
	        try {
	        	String filename = PlatformUtil.getPath(Activator.PLUGIN_ID, "/tmp/time.png").getPath();
	            BitmapEncoder.saveBitmapWithDPI(chart, filename, BitmapFormat.PNG, 300);
	            image = Image.getInstance(filename);
	            image.scaleAbsolute(width, height);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        } catch (DocumentException e1) {
	        	e1.printStackTrace();
	        }
			return image;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
