package kz.zvezdochet.export.util;

import html.Tag;
import kz.zvezdochet.core.util.CalcUtil;
import kz.zvezdochet.core.util.CoreUtil;
import kz.zvezdochet.export.bean.Bar;

import org.eclipse.swt.graphics.Color;

/**
 * Набор утилит для html-экспорта
 * @author Nataly Didenko
 *
 */
@SuppressWarnings("unchecked")
public class HTMLUtil {
	
	/**
	 * Создание абзаца, содержащего жирный текст
	 * @param value текст
	 * @return тег абзаца
	 */
	public Tag getBoldTaggedString(String value) {
		Tag tag = new Tag("h5");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание тега, содержащего жирный текст
	 * @param value текст
	 * @return тег
	 */
	public Tag getBoldTaggedSubstring(String value) {
		Tag tag = new Tag("strong", "class=name");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание тега, содержащего жирный текст по центру
	 * @param value текст
	 * @return тег
	 */
	public Tag getBoldCenteredString(String value) {
		Tag tag = new Tag("p", "font-weight=bold text-align=center");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание абзаца, содержащего курсивный текст
	 * @param value текст
	 * @return тег абзаца
	 */
	public Tag getItalicTaggedString(String value) {
		Tag tag = new Tag("p", "class=desc");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание абзаца, содержащего обычный текст
	 * @param value текст
	 * @return тег абзаца
	 */
	public Tag getNormalTaggedString(String value) {
		Tag tag = new Tag("p");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание тега, содержащего мелкий текст
	 * @param value текст
	 * @return тег
	 */
	public Tag getSmallTaggedString(String value) {
		Tag tag = new Tag("small");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание тега, содержащего жирный мелкий текст
	 * @param value текст
	 * @return тег абзаца
	 */
	public Tag getSmallBoldTaggedString(String value) {
		Tag tag = new Tag("small");
		Tag b = new Tag("b");
		b.add(value);
		tag.add(b);
		return tag;
	}

	/**
	 * Создание строки таблицы, содержащей ячейку с заголовком текста.
	 * Текст жирный, центрированный, прописными буквами.
	 * Заливка и цвет текста ячейки определяются CSS-настройками HTML-документа
	 * @param name текст
	 * @param anchor метка
	 * @return тег строки таблицы
	 */
	public Tag getTaggedHeader(String name, String anchor) {
		Tag tr = new Tag("tr");
		Tag td = new Tag("td", "class=header id=" + anchor);
		td.add(name);
		tr.add(td);
		return tr;
	}

	/**
	 * Создание заголовка для текста, который относится к мужчинам и женщинам
	 * @param type тип толкования
	 * @return тег заголовка
	 */
	public Tag getGenderHeader(String type) {
		String typeh = "";
	    if (type.equals("male"))
	    	typeh = "Мужчина";
	    else if (type.equals("female"))
	    	typeh = "Женщина";
	    else if (type.equals("child"))
	    	typeh = "Ребёнок";
		return getBoldTaggedString(typeh);
	}

	/**
	 * Динамическое создание диаграммы с помощью стандартной html-таблицы
	 * @param colnum число колонок таблицы диаграммы
	 * @param bars массив категорий диаграммы
	 * @param chartName наименование диаграммы
	 * @return тег диаграммы
	 */
	public Tag getTaggedChart(int colnum, Bar[] bars, String chartName) {
		Tag div = new Tag("div", "class=chart style=height:" + (40 * bars.length) + "px");
		if (chartName != null) {
			Tag b = new Tag("b");
			b.add(chartName);
			div.add(b);
		}		
		Tag table = new Tag("table");
		
		//строка с пустыми ячейками, определяющая количество столбцов
		Tag tr = new Tag("tr");
		for (int i = 0; i < colnum; i++) {
			Tag td = new Tag("th");
			tr.add(td);
		}
		table.add(tr);

		//строки с данными
		for (Bar bar : bars) {
			tr = new Tag("tr");
			double val = bar.getValue();
			int value = (int)(val * 2);
			//элемент диаграммы
			Tag td = new Tag("td", "class=filled colspan=" + value + 
					" bgcolor=#" + CoreUtil.colorToHex(bar.getColor()) + " align=middle");
			td.add(val);
			tr.add(td);

			//числовое значение
			td = new Tag("td", "colspan=" + (colnum - value));
			tr.add(td);

			//подпись
			td = new Tag("td");
			td.add(bar.getName());
			tr.add(td);
			table.add(tr);
		}
		div.add(table);
		return div;
	}

	/**
	 * Динамическое создание диаграммы
	 * с помощью библиотеки plotkit
	 * @param bars массив категорий диаграммы
	 * @param chartName наименование диаграммы
	 * @param type тип диаграммы (bar | pie)
	 * @param divname наименование блока канвы рисования
	 * @param optname наименование переменной параметров
	 * @param funcname наименование функции рисования
	 * @param color число, определяющее цвет диаграммы:<br>
	 * 	0	синий<br>
	 * 	1	бордовый<br>
	 * 	2	зеленый<br>
	 * 	3	фиолетовый<br>
	 * 	4	морской волны<br>
	 * 	5	светло-коричневый<br>
	 * 	6	черный
	 * @return тег диаграммы
	 */
	public Tag getPlotkitChart(Bar[] bars, String chartName, String type, 
					String divname, String optname, String funcname, int color, int height) {
		Tag div = new Tag("div");
		if (chartName != null) {
			Tag b = new Tag("b");
			b.add(chartName);
			div.add(b);
		}
		
		StringBuffer labels = new StringBuffer();
		StringBuffer values = new StringBuffer();
		for (int i = 0; i < bars.length; i++) {
			if (labels.length() > 0) labels.append(", ");
			labels.append("{v:" + i + ", label:\"" + bars[i].getName() + "\"}");
			if (values.length() > 0) values.append(", ");
			values.append("[" + i + ", " + bars[i].getValue() + "]");
		}
		
		Tag script = new Tag("script", "type=text/javascript");
		String options = "var " + optname + " = { " +
		   "\"IECanvasHTC\": \"/plotkit/iecanvas.htc\", " +
		   "\"colorScheme\": PlotKit.Base.palette(PlotKit.Base.baseColors()[" + color + "]), " +
		   "\"padding\": {left: 0, right: 0, top: 10, bottom: 30}, " +
		   "\"xTicks\": [" + labels + "], " +
		   "\"drawYAxis\": false, " +
		   "\"axisLabelWidth\": 200, " +
		   "\"drawBackground\": false, " +
		   "\"axisLabelFontSize\": 11" +
		   "};";
		script.add(options);
		
		String function = "function " + funcname + "() { " +
		    "var layout = new PlotKit.Layout(\"" + type + "\", " + optname + "); " +
		    "layout.addDataset(\"sqrt\", [" + values + "]); " +
		    "layout.evaluate(); " +
		    "var canvas = MochiKit.DOM.getElement(\"" + divname + "\"); " +
		    "var plotter = new PlotKit.SweetCanvasRenderer(canvas, layout, " + optname + "); " +
		    "plotter.render(); }";
		script.add(function);

		String call = "MochiKit.DOM.addLoadEvent(" + funcname + ");";
		script.add(call);
		div.add(script);
		
		Tag canvdiv = new Tag("div");
		Tag canvas = new Tag("canvas", "id=" + divname + " height=" + height + " width=1000");
		canvdiv.add(canvas);
		div.add(canvdiv);
		return div;

//		<script type="text/javascript">
//		var options = {
//		   "IECanvasHTC": "/plotkit/iecanvas.htc",
//		   "colorScheme": PlotKit.Base.palette(PlotKit.Base.baseColors()[4]),
//		   "padding": {left: 0, right: 0, top: 10, bottom: 30},
//		   "xTicks": [{v:0, label:"Я великолепен!"}, 
//		          {v:1, label:"Со мной можно иметь дело"}, 
//		          {v:2, label:"Надо всегда быть начеку"}],
//		   "drawYAxis": false,
//		   "axisLabelWidth": 70,
//		   "drawBackground": false,
//		   "axisLabelFontSize": 11
//		};
//
//		function drawGraph() {
//		    var layout = new PlotKit.Layout("pie", options);
//		    layout.addDataset("sqrt", [[0, 3], [1, 1.5], [2, 4]]);
//		    layout.evaluate();
//		    var canvas = MochiKit.DOM.getElement("graph");
//		    var plotter = new PlotKit.SweetCanvasRenderer(canvas, layout, options);
//		    plotter.render();
//		}
//		MochiKit.DOM.addLoadEvent(drawGraph);
//			</script>
//
//			<div><canvas id="graph" height="300" width="800"></canvas></div>
	}

	/**
	 * Динамическое создание круговой диаграммы с помощью CSS3
	 * @param bars массив категорий диаграммы
	 * @param chartName наименование диаграммы
	 * @return тег диаграммы
	 */
	public Tag getCss3Chart(Bar[] bars, String chartName) {
		Tag div = new Tag("div", "class=chart");
		if (chartName != null) {
			Tag b = new Tag("h5");
			b.add(chartName);
			div.add(b);
		}

		Tag legend = new Tag("div", "class=legend");
		double total = 0;
		for (Bar bar : bars) {
			if (null == bar) continue;
			double val = bar.getValue();
			if (0 == val) continue;
			total += val;
			Color color = bar.getColor();
			String attr = "style=border-left-color:rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
			Tag ldiv = new Tag("div", "class=legend-item " + attr);
			ldiv.add(bar.getName());
			Tag span = new Tag("span");
			span.add(val);
			ldiv.add(span);
			legend.add(ldiv);
		}
		div.add(legend);

		int i = -1;
		double deg = 0;
		for (Bar bar : bars) {
			if (null == bar) continue;
			String attr = "";
			if (++i > 0)
				attr = "style=-moz-transform:rotate(" + deg + "deg);-webkit-transform:rotate(" + deg + "deg);-o-transform:rotate(" + deg + "deg);transform:rotate(" + deg + "deg);";
			double percent = bar.getValue() * 100 / total;
			String hclass = (percent >= 50) ? "hold50" : "hold";
			Tag hdiv = new Tag("div", "class=" + hclass + " " + attr);
			Color color = bar.getColor();
			String cattr = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");";
			double angle = percent * 360 / 100;
			attr = "style=background-color:" + cattr + "-moz-transform:rotate(" + angle + "deg);-webkit-transform:rotate(" + angle + "deg);-o-transform:rotate(" + angle + "deg);transform:rotate(" + angle + "deg);";
			Tag pdiv = new Tag("div", "class=pie " + attr);
			hdiv.add(pdiv);

			if (percent >= 50)
				hdiv.add(new Tag("div", "class=piefill " + attr));

			div.add(hdiv);
			deg += angle;
		}
		return div;
	}

	/**
	 * Динамическое создание диаграммы с помощью стандартной html-таблицы
	 * @param total общее количество очков
	 * @param bars массив категорий диаграммы
	 * @param chartName наименование диаграммы
	 * @return тег диаграммы
	 */
	public Tag getUlChart(int total, Bar[] bars, String chartName) {
		int height = 40 * bars.length;
		Tag div = new Tag("div", "class=chart style=height:" + height + "px");
		if (chartName != null) {
			Tag b = new Tag("b");
			b.add(chartName);
			div.add(b);
		}		
		Tag ul = new Tag("ul", "height=" + height);
		
		//строки с данными
		for (Bar bar : bars) {
			double val = bar.getValue();
			double percent = val * 100 / total; 
			Tag li = new Tag("li", "style=background:#" + CoreUtil.colorToHex(bar.getColor()) + ";background-position-x:" + percent + "%");
			li.add(CalcUtil.roundTo(percent, 0) + "%   " + bar.getName());
			ul.add(li);
		}
		div.add(ul);
		return div;
	}
}
