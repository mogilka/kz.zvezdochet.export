package kz.zvezdochet.export.util;

import html.Tag;
import kz.zvezdochet.core.util.CoreUtil;
import kz.zvezdochet.export.bean.Bar;

/**
 * Класс, предоставляющий вспомогательные методы для html-экспорта
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
	public static Tag getBoldTaggedString(String value) {
		Tag tag = new Tag("p", "class=name");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание тега, содержащего жирный текст
	 * @param value текст
	 * @return тег
	 */
	public static Tag getBoldTaggedSubstring(String value) {
		Tag tag = new Tag("strong", "class=name");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание тега, содержащего жирный текст по центру
	 * @param value текст
	 * @return тег
	 */
	public static Tag getBoldCenteredString(String value) {
		Tag tag = new Tag("p", "font-weight=bold text-align=center");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание абзаца, содержащего курсивный текст
	 * @param value текст
	 * @return тег абзаца
	 */
	public static Tag getItalicTaggedString(String value) {
		Tag tag = new Tag("p", "class=desc");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание абзаца, содержащего обычный текст
	 * @param value текст
	 * @return тег абзаца
	 */
	public static Tag getNormalTaggedString(String value) {
		Tag tag = new Tag("p");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание тега, содержащего мелкий текст
	 * @param value текст
	 * @return тег
	 */
	public static Tag getSmallTaggedString(String value) {
		Tag tag = new Tag("small");
		tag.add(value);
		return tag;
	}

	/**
	 * Создание тега, содержащего жирный мелкий текст
	 * @param value текст
	 * @return тег абзаца
	 */
	public static Tag getSmallBoldTaggedString(String value) {
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
	 * @param value текст
	 * @return тег строки таблицы
	 */
	public static Tag getTaggedHeader(String name, String link) {
		Tag tr = new Tag("tr");
		Tag td = new Tag("td", "class=header");
		Tag a = new Tag("a", "name=" + link);
		a.add(name);
		td.add(a);
		tr.add(td);
		return tr;
	}

	/**
	 * Создание заголовка для текста, который относится к мужчинам и женщинам
	 * @param isMale признак того, что человек мужского пола
	 * @param femaleText текст для женщин
	 * @param maleText текст для мужчин
	 * @return тег заголовка
	 */
	public static Tag getGenderHeader(boolean isMale, String maleText, String femaleText) {
		if (isMale) {
			if (maleText != null && maleText.length() > 0)  
				return getBoldTaggedString("Мужчина");
		} else {
			if (femaleText != null && femaleText.length() > 0)  
				return getBoldTaggedString("Женщина");
		}
		return null;
	}

	/**
	 * Создание текста, который относится к мужчинам и женщинам
	 * @param isMale признак того, что человек мужского пола
	 * @param femaleText текст для женщин
	 * @param maleText текст для мужчин
	 * @return тег
	 */
	public static Tag getGenderText(boolean isMale, String maleText, String femaleText) {
		if (isMale) {
		    if (maleText != null && maleText.length() > 0) 
				return getNormalTaggedString(maleText);
		} else {
			if (femaleText != null && femaleText.length() > 0)  
				return getNormalTaggedString(femaleText);
		}
		return null;
	}
		
	/**
	 * Определение строки, сочетающейся с величиной возраста
	 * @param age возраст
	 * @return строка, добавляемая к возрасту
	 */
	public static String getAgeString(int age) {
		String s = String.valueOf(age);
		String lastChar = s.substring(s.length() - 1); 
		if (age > 10 && age < 20)
			return s + " лет";
		else if (lastChar.equals('1'))
			return s + " год";
		else if (lastChar.endsWith("2") || lastChar.endsWith("4"))
			return s + " года";
		else if (lastChar.endsWith("5") || lastChar.endsWith("9") || lastChar.endsWith("0"))
			return s + " лет";
		else 
			return s + " лет";
	}
	
	/**
	 * Динамическое создание диаграммы
	 * с помощью стандартной html-таблицы
	 * @param colnum число колонок таблицы диаграммы
	 * @param bars массив категорий диаграммы
	 * @param chartName наименование диаграммы
	 * @return тег диаграммы
	 */
	public static Tag getTaggedChart(int colnum, Bar[] bars, String chartName) {
		Tag div = new Tag("div", "id=horizbar");
		
		if (chartName != null) {
			Tag b = new Tag("b");
			b.add(chartName);
			div.add(b);
		}		
		int height = 40 * bars.length;
		Tag table = new Tag("table", "width=50% border=0 cellpadding=0 cellspacing=5 align=center height=" + height);
		
		//строка с пустыми ячейками, определяющая количество столбцов
		Tag tr = new Tag("tr");
		for (int i = 0; i < colnum; i++) {
			Tag td = new Tag("td");
			tr.add(td);
		}
		table.add(tr);

		//строки с данными
		for (Bar bar : bars) {
			tr = new Tag("tr");
			int value = (int)(bar.getValue() * 2);
			//элемент диаграммы
			Tag td = new Tag("td", "colspan=" + value + 
					" bgcolor=#" + CoreUtil.colorToHex(bar.getColor()) + " align=middle");
			td.add(bar.getValue());
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
	public static Tag getPlotkitChart(Bar[] bars, String chartName, String type, 
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
}