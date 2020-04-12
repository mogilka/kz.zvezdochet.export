package kz.zvezdochet.export.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
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

public class Html2Pdf {

    public static void main(String[] args) throws IOException, DocumentException {
    	String filename = "/Users/natalie/workspace/kz.zvezdochet.export/test/html2pdf.pdf";
        File file = new File(filename);
        file.getParentFile().mkdirs();
        Document document = new Document();
        @SuppressWarnings("unused")
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        String url = "https://zvezdochet.guru";
    	String name = "Звездочёт";
        String html = "<h4>Добрый день, " + name + "!</h4>" + 
        	"<p>Кто-то ответил на ваш комментарий на сайте <a href=\"" + url + "\">\" + name + \"</a>.</p>" + 
        	"<h5>Текст сообщения</h5>" + 
    		"<blockquote>Женщин этого типа можно разделить на две категории:</blockquote>" + 
        	"<br>" + 
        	"<ul>\n" + 
       		"<li>светская дама – экстpавагантная, с независимым складом ума. Любит искусство, книги и блестящих людей;</li>" + 
    		"<li>пацанка – дикарка с хаpактеpом мальчишки. Любит движение и споpт.</li>" + 
    		"</ul>" + 
        	"<br>" + 
        	"<a href=\"" + url + "\" class=\"btn\">Перейти на сайт</a>" + 
    		"<br><br>" + 
        	"<p style=\"color:red\">На данное письмо отвечать не нужно.</p>";


		InputStream is = new ByteArrayInputStream(html.getBytes("UTF-8"));
		String cssfile = "/Users/natalie/workspace/kz.zvezdochet.export/export.css";
		FileInputStream fis = new FileInputStream(cssfile);
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
	    XMLParser parser = new XMLParser(worker);
	    parser.parse(is, Charset.forName("UTF-8"));

	    String fontdir = "/Users/natalie/workspace/kz.zvezdochet.export/font";
	    BaseFont baseFont = BaseFont.createFont(fontdir + "/FreeSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	    Font font = new Font(baseFont, 12, Font.NORMAL);
	    Phrase phrase = new Phrase();
	    phrase.setFont(font);
	    phrase.addAll(elements);
        Paragraph p = new Paragraph(phrase);
        document.add(p);
        document.close();
    }
}
