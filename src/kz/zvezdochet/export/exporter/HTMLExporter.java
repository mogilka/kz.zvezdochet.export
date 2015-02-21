package kz.zvezdochet.export.exporter;

import html.Tag;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import kz.zvezdochet.analytics.bean.Category;
import kz.zvezdochet.analytics.bean.PlanetAspectText;
import kz.zvezdochet.analytics.bean.PlanetHouseText;
import kz.zvezdochet.analytics.service.AnalyticsService;
import kz.zvezdochet.analytics.service.CardTypeService;
import kz.zvezdochet.analytics.service.CategoryService;
import kz.zvezdochet.analytics.service.DegreeService;
import kz.zvezdochet.analytics.service.PlanetAspectService;
import kz.zvezdochet.analytics.service.PlanetHouseService;
import kz.zvezdochet.bean.AspectType;
import kz.zvezdochet.bean.Cross;
import kz.zvezdochet.bean.Element;
import kz.zvezdochet.bean.Event;
import kz.zvezdochet.bean.Halfsphere;
import kz.zvezdochet.bean.House;
import kz.zvezdochet.bean.Planet;
import kz.zvezdochet.bean.Sign;
import kz.zvezdochet.bean.SkyPoint;
import kz.zvezdochet.bean.SkyPointAspect;
import kz.zvezdochet.bean.Square;
import kz.zvezdochet.bean.YinYang;
import kz.zvezdochet.bean.Zone;
import kz.zvezdochet.core.bean.GenderText;
import kz.zvezdochet.core.bean.Model;
import kz.zvezdochet.core.bean.TextGenderDictionary;
import kz.zvezdochet.core.util.DateUtil;
import kz.zvezdochet.core.util.PlatformUtil;
import kz.zvezdochet.export.Activator;
import kz.zvezdochet.export.bean.Bar;
import kz.zvezdochet.export.service.ExportService;
import kz.zvezdochet.export.util.EventStatistics;
import kz.zvezdochet.export.util.HTMLUtil;
import kz.zvezdochet.service.AspectTypeService;
import kz.zvezdochet.service.CrossService;
import kz.zvezdochet.service.ElementService;
import kz.zvezdochet.service.EventService;
import kz.zvezdochet.service.HalfsphereService;
import kz.zvezdochet.service.HouseService;
import kz.zvezdochet.service.SignService;
import kz.zvezdochet.service.SquareService;
import kz.zvezdochet.service.YinYangService;
import kz.zvezdochet.service.ZoneService;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Генератор HTML-файлов для экспорта данных
 * @author Nataly Didenko
 *
 */
@SuppressWarnings("unchecked")
public class HTMLExporter {
	private HTMLUtil util;

	public HTMLExporter() {
		util = new HTMLUtil();
	}

	/**
	 * Генерация индивидуального гороскопа
	 * @param event событие
	 */
	public void generate(Event event) {
		try {
			Tag html = new Tag("html");
			Tag head = new Tag("head");
			head.add(new Tag("meta", "http-equiv=Content-Type content=text/html; charset=UTF-8"));
			head.add(new Tag("script", "type=text/javascript src=mochikit/MochiKit.js"));
			head.add(new Tag("script", "type=text/javascript src=plotkit/Base.js"));
			head.add(new Tag("script", "type=text/javascript src=plotkit/Layout.js"));
			head.add(new Tag("script", "type=text/javascript src=plotkit/Canvas.js"));
			head.add(new Tag("script", "type=text/javascript src=plotkit/SweetCanvas.js"));
			head.add(new Tag("link", "href=horoscope_files/horoscope.css rel=stylesheet type=text/css"));
			Tag title = new Tag("title");
			title.add("Индивидуальный гороскоп");
			head.add(title);
			html.add(head);

			Tag body = new Tag("body");
			body.add(printCopyright());
			Tag table = new Tag("table");
			body.add(table);
			body.add(printCopyright());
			html.add(body);
	
			//дата события
			Tag row = new Tag("tr");
			Tag cell = new Tag("td", "class=mainheader");
			cell.add(DateUtil.fulldtf.format(event.getBirth()) +
				"&ensp;" + (event.getZone() > 0 ? "+" : "") + event.getZone() +
				"&emsp;" + event.getPlace().getName() +
				"&ensp;" + event.getPlace().getLatitude() + "&#176;" +
				", " + event.getPlace().getLongitude() + "&#176;");
			row.add(cell);
			table.add(row);
			
			//содержание
			row = new Tag("tr");
			cell = new Tag("td");
			generateContents(event, cell);
			row.add(cell);
			table.add(row);
	
			//знаменитости
			generateCelebrities(event.getBirth(), table);
			generateSimilar(event, table);

			//основные диаграммы
			EventStatistics statistics = new EventStatistics(event.getConfiguration());
			Map<String, Double> signMap = statistics.getPlanetSigns(true);
			//знаки
			generateSignChart(table, signMap);
			//дома
			statistics.initPlanetHouses();
//			generateHouseChart(statistics, table);
			
			//градус рождения
			generateDegree(event, table);
			
			//планеты в знаках
			generatePlanetsInSigns(event, table);
			
			//космограмма
			generateCard(event, table);
			
			//вид космограммы
			//generateCardKarma(event, table); 

			//тип космограммы
			Map<String, Integer> signPlanetsMap = statistics.getSignPlanets();
			generateCardType(event, table, signPlanetsMap);
			
			//выделенность стихий
			statistics.initPlanetDivisions();
			statistics.initHouseDivisions();
			generateElements(event, table, statistics);
	
			//выделенность инь-ян
			generateYinYang(event, table, statistics);
			
			//выделенность полусфер
			generateHalfSpheres(event, table, statistics);
			
			//выделенность квадратов
			signMap = statistics.getPlanetSigns(false);
			generateSquares(event, table, statistics);
			
			//выделенность крестов
			generateCrosses(event, table, statistics);
			
			//выделенность зон
			generateZones(event, table, statistics);
			
			//аспекты
			generateAspectTypes(event, table);
			//позитивные аспекты
			generateAspects(event, table, "Позитивные аспекты планет", "POSITIVE");
			//негативные аспекты
			generateAspects(event, table, "Негативные аспекты планет", "NEGATIVE");
			//конфигурации аспектов
			//generateAspectConfigurations(event, table);
			
			//планеты
			generatePlanets(event, table);
			
			//планеты в домах
			Map<String, Double> houseMap = statistics.getPlanetHouses();
			generatePlanetInHouses(event, table, houseMap);
			//дома в знаках
			//generateHouseInSigns(event, table, houseMap);
			
			if (html != null) {
				//System.out.println(html);
				export(html.toString());
			}
		} catch(Exception e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Ошибка", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Генерация домов в знаках.
	 * Применяется только для пустых домов и строится на тождестве:
	 * Дом в знаке = управитель знака в доме
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param houseMap карта домов
	 */
	private void generateHouseInSigns(Event event, Tag cell, Map<String, Double> houseMap) {
		if (null == event.getConfiguration().getHouses()) return;
		try {
			for (Model hmodel : event.getConfiguration().getHouses()) {
				House house = (House)hmodel;
				//Определяем количество планет в доме
				if (houseMap.get(house.getCode()) != null) continue;
				//Создаем информационный блок только если дом пуст
				Sign sign = SkyPoint.getSign(house.getCoord());
				Planet planet = new AnalyticsService().getSignPlanet(sign, "HOME");
				if (null == planet) continue;
				
				PlanetHouseText dict = (PlanetHouseText)
							new PlanetHouseService().find(planet, house, null);
				if (dict != null) {
					Tag tr = util.getTaggedHeader(house.getHeaderName(), house.getLinkName());
					cell.add(tr);
			
					tr = new Tag("tr");
					Tag td = new Tag("td");
					td.add(util.getBoldTaggedString(
							house.getShortName() + " в знаке " + sign.getName()));
					td.add(util.getNormalTaggedString(dict.getText()));
					printGenderText(dict.getGenderText(), event, td);		
					td.add(new Tag("/br"));
					tr.add(td);
					cell.add(tr);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация планет в домах
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param houseMap карта домов
	 */
	private void generatePlanetInHouses(Event event, Tag cell, Map<String, Double> houseMap) {
		if (null == event.getConfiguration().getHouses()) return;
		try {
			for (Model hmodel : event.getConfiguration().getHouses()) {
				House house = (House)hmodel;
				//Определяем количество планет в доме
				List<Planet> planets = new ArrayList<Planet>();
				for (Model pmodel : event.getConfiguration().getPlanets()) {
					Planet planet = (Planet)pmodel;
					if (planet.getCode().equals("Kethu")) continue;
					if (planet.getHouse().getId().equals(house.getId()))
						planets.add(planet);
				}
				//Создаем информационный блок только если дом не пуст
				if (planets.size() > 0) {
					Tag tr = util.getTaggedHeader(house.getHeaderName(), house.getLinkName());
					cell.add(tr);
			
					tr = new Tag("tr");
					Tag td = new Tag("td");
					for (Planet planet : planets) {
						PlanetHouseText dict = (PlanetHouseText)
							new PlanetHouseService().find(planet, house, null);
						if (dict != null) {
							td.add(util.getBoldTaggedString(
								planet.getName() + " " + house.getCombination()));
							td.add(util.getNormalTaggedString(dict.getText()));
							printGenderText(dict.getGenderText(), event, td);		
							td.add(new Tag("/br"));
						}
					}
					tr.add(td);
					cell.add(tr);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация планет
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void generatePlanets(Event event, Tag cell) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=planets");
			a.add("Планеты");
			td.add(a);
			tr.add(td);
			cell.add(tr);

			tr = new Tag("tr");
			td = new Tag("td");
			for (Model model : event.getConfiguration().getPlanets()) {
				Planet planet = (Planet)model;
				if (planet.isKernel() && planet.getKernelText() != null) {
					td.add(util.getBoldTaggedString(planet.getName() + "-ядро"));
					td.add(util.getNormalTaggedString(planet.getKernelText()));
				} else if (planet.isSword() && planet.getSwordText() != null) {
					td.add(util.getBoldTaggedString(planet.getName() + "-меч"));
					td.add(util.getNormalTaggedString(planet.getSwordText()));
				} else if (planet.isShield() && planet.getShieldText() != null) {
					td.add(util.getBoldTaggedString(planet.getName() + "-щит"));
					td.add(util.getNormalTaggedString(planet.getShieldText()));
				} else if (planet.isBelt() && planet.getBeltText() != null) {
					td.add(util.getBoldTaggedString(planet.getName() + "-пояс"));
					td.add(util.getNormalTaggedString(planet.getBeltText()));
				} else if (planet.inMine() && planet.getMineText() != null) {
					td.add(util.getBoldTaggedString(planet.getName() + " в шахте"));
					td.add(util.getNormalTaggedString(planet.getMineText()));
				} else if (planet.isDamaged() && planet.getDamagedText() != null) {
					td.add(util.getBoldTaggedString(planet.getName() + " поражённый"));
					td.add(util.getNormalTaggedString(planet.getDamagedText()));
				} else if (planet.isRetrograde() && planet.getRetroText() != null) {
					td.add(util.getBoldTaggedString(planet.getName() + "-ретроград"));
					td.add(util.getNormalTaggedString(planet.getRetroText()));
				} else if (planet.isPerfect() && planet.getStrongText() != null) {
					td.add(util.getBoldTaggedString(planet.getName() + "-сила"));
					td.add(util.getNormalTaggedString(planet.getStrongText()));
				} else if (planet.isBroken() && planet.getWeakText() != null) { 
					td.add(util.getBoldTaggedString(planet.getName() + "-слабость"));
					td.add(util.getNormalTaggedString(planet.getWeakText()));
				}
			}
			tr.add(td);
			cell.add(tr);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация конфигурации аспектов
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void generateAspectConfigurations(Event event, Tag cell) {
		try {
//		WriteLn(report,'<tr><td class=header><a name="configurations">Конфигурации планет</a></td></tr>');
//		WriteLn(report,'<tr><td>');
//		WriteLn(report,'<p class="name"><img src="horoscope_files/conf/cross.gif"></p>');
//		WriteLn(report,'</br>');
//		WriteLn(report,'<p class="name"><img src="horoscope_files/conf/cross.gif"></p>');
//		WriteLn(report,'</br>');
//		WriteLn(report,'<p class="name"><img src="horoscope_files/conf/cross.gif"></p>');
//		WriteLn(report,'</td></tr>');
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация статистики типов аспектов
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void generateAspectTypes(Event event, Tag cell) {
		try {
			event.getConfiguration().initPlanetAspects();
			List<Model> planets = event.getConfiguration().getPlanets();
			//фильтрация списка типов аспектов
			List<Model> aspectTypes = new AspectTypeService().getList();
			List<AspectType> types = new ArrayList<AspectType>();
		    for (Model model : aspectTypes) {
		    	AspectType type = (AspectType)model;
		    	if (type.getCode() != null &&
		    			!type.getCode().equals("COMMON") &&
		    			type.getName() != null)
		    		types.add(type);
		    }			
			
			Bar[] bars = new Bar[types.size()];
			int i = -1;
		    for (AspectType type : types) {
		    	Bar bar = new Bar();
		    	bar.setName(type.getName());
		    	int value = 0;
		    	for (Model model : planets) {
		    		Planet planet = (Planet)model;
					value += planet.getAspectCountMap().get(type.getCode());
		    	}
		    	bar.setValue(value / 2);
				bar.setColor(type.getDimColor());
		    	bars[++i] = bar;
		    }
			
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=aspects");
			a.add("Соотношение аспектов планет");
			td.add(a);
			tr.add(td);
			cell.add(tr);
	
			tr = new Tag("tr");
			td = new Tag("td");
//			Tag p = new Tag("p", "class=shot");
//			Tag chart = util.getTaggedChart(17, bars, null);
			Tag chart = util.getPlotkitChart(bars, null, "pie", "asptypechart", "asptypechartopts", "drawAsptypeChart", 4, 400);
			td.add(chart);
//			p.add(chart);
//			td.add(p);
			tr.add(td);
			cell.add(tr);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация аспектов планет
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param header заголовок блока информации
	 * @param aspectType код типа аспектов
	 */
	private void generateAspects(Event event, Tag cell, String header, String aspectType) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a");
			a.add(header);
			td.add(a);
			tr.add(td);
			cell.add(tr);
	
			tr = new Tag("tr");
			td = new Tag("td");
			for (SkyPointAspect aspect : event.getConfiguration().getAspects()) {
				if (!((Planet)aspect.getSkyPoint1()).isMain()) continue;
				AspectType type = aspect.getAspect().getType();
				if (type.getCode().equals(aspectType) ||
						(aspectType.equals("POSITIVE") &&
								!((Planet)aspect.getSkyPoint2()).getCode().equals("Lilith") &&
								!((Planet)aspect.getSkyPoint2()).getCode().equals("Kethu") &&
								type.getCode().equals("NEUTRAL")) ||
						(aspectType.equals("NEGATIVE") &&
								(((Planet)aspect.getSkyPoint2()).getCode().equals("Lilith") ||
								((Planet)aspect.getSkyPoint2()).getCode().equals("Kethu")) &&
								type.getCode().equals("NEUTRAL"))	
				) {
					PlanetAspectText dict = (PlanetAspectText)
						new PlanetAspectService().find(
								(Planet)aspect.getSkyPoint1(), 
								(Planet)aspect.getSkyPoint2(), 
								type);
					if (dict != null) {
						Tag tag = util.getBoldTaggedString(
							dict.getPlanet1().getName() + " " + 
							type.getSymbol() + " " + 
							dict.getPlanet2().getName());
						td.add(tag);

						tag = util.getNormalTaggedString(dict.getText());
						td.add(tag);
						printGenderText(dict.getGenderText(), event, td);
						td.add(new Tag("/br"));
					}
				}
			}
			tr.add(td);
			cell.add(tr);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Генерация зон
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param statistics объект статистики
	 */
	private void generateZones(Event event, Tag cell, EventStatistics statistics) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=zones");
			a.add("Развитие духа");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			Map<String, Double> zoneMap = statistics.getPlanetZones();
			Bar[] bars = new Bar[zoneMap.size()];
			Iterator<Map.Entry<String, Double>> iterator = zoneMap.entrySet().iterator();
			int i = -1;
			Zone zone = null;
			double score = 0.0;
			ZoneService service = new ZoneService();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	Zone element = (Zone)service.find(entry.getKey());
		    	bar.setName(element.getName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[++i] = bar;
		    	//определяем наиболее выраженный элемент
		    	if (entry.getValue() > score) {
		    		score = entry.getValue();
		    		zone = element;
		    	}
		    }
		    if (zone != null) {
				tr = new Tag("tr");
				td = new Tag("td");
				td.add(util.getBoldTaggedString(zone.getDescription()));
				td.add(util.getNormalTaggedString(zone.getText()));
				printGenderText(zone.getGenderText(), event, td);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				a = new Tag("a");
				a.add("Развитие духа в сознании");
				td.add(a);
				tr.add(td);
				cell.add(tr);
				
			    tr = new Tag("tr");
				td = new Tag("td");
//				Tag chart = util.getTaggedChart(17, bars, "Развитие духа в сознании");
				Tag chart = util.getPlotkitChart(bars, null, "pie", "zonechart", "zonechartopts", "drawZoneChart", 3, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				a = new Tag("a");
				a.add("Развитие духа в действии");
				td.add(a);
				tr.add(td);
				cell.add(tr);
				
				zoneMap = statistics.getHouseZones();
				bars = new Bar[zoneMap.size()];
				iterator = zoneMap.entrySet().iterator();
				i = -1;
				zone = null;
			    while (iterator.hasNext()) {
			    	Entry<String, Double> entry = iterator.next();
			    	Bar bar = new Bar();
			    	Zone element = (Zone)service.find(entry.getKey());
			    	bar.setName(element.getName());
			    	bar.setValue(entry.getValue());
			    	bar.setColor(element.getColor());
			    	bars[++i] = bar;
			    }
				
			    tr = new Tag("tr");
				td = new Tag("td");
//				chart = util.getTaggedChart(17, bars, "Развитие духа в действии");
				chart = util.getPlotkitChart(bars, null, "pie", "zone2chart", "zone2chartopts", "drawZoneChart2", 5, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);
		    }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация крестов
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param statistics объект статистики
	 */
	private void generateCrosses(Event event, Tag cell, EventStatistics statistics) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=crosses");
			a.add("Стратегия");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			Map<String, Double> crossMap = statistics.getPlanetCrosses();
			Bar[] bars = new Bar[crossMap.size()];
			Iterator<Map.Entry<String, Double>> iterator = crossMap.entrySet().iterator();
			int i = -1;
			Cross cross = null;
			double score = 0.0;
			CrossService service = new CrossService();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	Cross element = (Cross)service.find(entry.getKey());
		    	bar.setName(element.getDiaName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[++i] = bar;
		    	//определяем наиболее выраженный элемент
		    	if (entry.getValue() > score) {
		    		score = entry.getValue();
		    		cross = element;
		    	}
		    }
		    if (cross != null) {
				tr = new Tag("tr");
				td = new Tag("td");
				td.add(util.getBoldTaggedString(cross.getDescription()));
				td.add(util.getNormalTaggedString(cross.getText()));
				printGenderText(cross.getGenderText(), event, td);
				tr.add(td);
				cell.add(tr);
		    }
		    
			tr = new Tag("tr");
			td = new Tag("td", "class=header");
			a = new Tag("a", "name=crosses");
			a.add("Стратегия в сознании");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			tr = new Tag("tr");
			td = new Tag("td");
			Tag chart = util.getPlotkitChart(bars, null, "pie", "crosschart", "crosschartopts", "drawCrossChart", 3, 300);
			td.add(chart);
			tr.add(td);
			cell.add(tr);

			//знаки
			crossMap = statistics.getCrossSigns();
			bars = new Bar[crossMap.size()];
			iterator = crossMap.entrySet().iterator();
			i = -1;
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	bar.setName(entry.getKey());
		    	bar.setValue(entry.getValue());
		    	bars[++i] = bar;
		    }
			
			tr = new Tag("tr");
			td = new Tag("td");
			chart = util.getPlotkitChart(bars, null, "pie", "crossSignchart", "crossSignchartopts", "drawCrossSignChart", 5, 300);
			td.add(chart);
			tr.add(td);
			cell.add(tr);

			//
			tr = new Tag("tr");
			td = new Tag("td", "class=header");
			a = new Tag("a", "name=crosses");
			a.add("Стратегия в поступках");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			crossMap = statistics.getHouseCrosses();
			bars = new Bar[crossMap.size()];
			iterator = crossMap.entrySet().iterator();
			i = -1;
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	Cross element = (Cross)service.find(entry.getKey());
		    	bar.setName(element.getDiaName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[++i] = bar;
		    }
			
			tr = new Tag("tr");
			td = new Tag("td");
			chart = util.getPlotkitChart(bars, null, "pie", "cross2chart", "cross2chartopts", "drawCrossChart2", 2, 300);
			td.add(chart);
			td.add(new Tag("/br"));
			tr.add(td);
			cell.add(tr);
			
			//дома
			crossMap = statistics.getCrossHouses();
			bars = new Bar[crossMap.size()];
			iterator = crossMap.entrySet().iterator();
			i = -1;
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	bar.setName(entry.getKey());
		    	bar.setValue(entry.getValue());
		    	bars[++i] = bar;
		    }
			
			tr = new Tag("tr");
			td = new Tag("td");
			chart = util.getPlotkitChart(bars, null, "pie", "crossHousechart", "crossHousechartopts", "drawCrossHouseChart", 4, 300);
			td.add(chart);
			tr.add(td);
			cell.add(tr);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация квадратов
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param statistics объект статистики
	 */
	private void generateSquares(Event event, Tag cell, EventStatistics statistics) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=zones");
			a.add("Зрелость");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			Map<String, Double> squareMap = statistics.getPlanetSquares();
			Bar[] bars = new Bar[squareMap.size()];
			Iterator<Map.Entry<String, Double>> iterator = squareMap.entrySet().iterator();
			int i = -1;
			Square square = null;
			double score = 0.0;
			SquareService service = new SquareService();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	Square element = (Square)service.find(entry.getKey());
		    	bar.setName(element.getName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[++i] = bar;
		    	//определяем наиболее выраженный элемент
		    	if (entry.getValue() > score) {
		    		score = entry.getValue();
		    		square = element;
		    	}
		    }
		    if (square != null) {
				tr = new Tag("tr");
				td = new Tag("td");
				td.add(util.getBoldTaggedString(square.getDescription()));
				td.add(util.getNormalTaggedString(square.getText()));
				printGenderText(square.getGenderText(), event, td);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				a = new Tag("a");
				a.add("Зрелость в сознании");
				td.add(a);
				tr.add(td);
				cell.add(tr);
				
			    tr = new Tag("tr");
				td = new Tag("td");
				Tag chart = util.getPlotkitChart(bars, null, "pie", "squarechart", "squarechartopts", "drawSquareChart", 5, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);

				//знаки
				Map<String, Double> signMap = statistics.getPlanetSigns();
				bars = new Bar[signMap.size()];
				iterator = signMap.entrySet().iterator();
				i = -1;
				SignService service2 = new SignService();
			    while (iterator.hasNext()) {
			    	Entry<String, Double> entry = iterator.next();
			    	Bar bar = new Bar();
			    	Sign element = (Sign)service2.find(entry.getKey());
			    	bar.setName(element.getDiaName());
			    	bar.setValue(entry.getValue());
			    	bar.setColor(element.getColor());
			    	bars[++i] = bar;
			    }
				
			    tr = new Tag("tr");
				td = new Tag("td");
				chart = util.getPlotkitChart(bars, null, "pie", "squareSignChart", "squareSignChartOpts", "drawSquareSignChart", 4, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);

				//
			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				a = new Tag("a");
				a.add("Зрелость в поступках");
				td.add(a);
				tr.add(td);
				cell.add(tr);
				
				squareMap = statistics.getHouseSquares();
				bars = new Bar[squareMap.size()];
				iterator = squareMap.entrySet().iterator();
				i = -1;
			    while (iterator.hasNext()) {
			    	Entry<String, Double> entry = iterator.next();
			    	Bar bar = new Bar();
			    	Square element = (Square)service.find(entry.getKey());
			    	bar.setName(element.getName());
			    	bar.setValue(entry.getValue());
			    	bar.setColor(element.getColor());
			    	bars[++i] = bar;
			    }
				
			    tr = new Tag("tr");
				td = new Tag("td");
				chart = util.getPlotkitChart(bars, null, "pie", "square2chart", "square2chartopts", "drawSquare2Chart", 5, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);
				
				//дома
				Map<String, Double> houseMap = statistics.getMainPlanetHouses(); //TODO найти более оптимальный вариант, мат.формулу
				bars = new Bar[houseMap.size()];
				iterator = houseMap.entrySet().iterator();
				i = -1;
				HouseService hservice = new HouseService();
			    while (iterator.hasNext()) {
			    	Entry<String, Double> entry = iterator.next();
			    	Bar bar = new Bar();
					//по индексу трети определяем дом, в котором она находится
			    	House element = (House)hservice.find(entry.getKey());
			    	bar.setName(element.getDiaName());
			    	bar.setValue(entry.getValue());
			    	bar.setColor(element.getColor());
			    	bars[++i] = bar;
			    }
				
			    tr = new Tag("tr");
				td = new Tag("td");
				chart = util.getPlotkitChart(bars, null, "pie", "squareHouseChart", "squareHouseChartOpts", "drawSquareHouseChart", 4, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);
		    }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация полусфер
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param statistics объект статистики
	 */
	private void generateHalfSpheres(Event event, Tag cell, EventStatistics statistics) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=zones");
			a.add("Экстраверсия и интроверсия");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			Map<String, Double> sphereMap = statistics.getPlanetHalfspheres();
			Bar[] bars = new Bar[sphereMap.size()];
			Iterator<Map.Entry<String, Double>> iterator = sphereMap.entrySet().iterator();
			int i = -1;
			Halfsphere sphere = null;
			double score = 0.0;
			HalfsphereService service = new HalfsphereService();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	Halfsphere element = (Halfsphere)service.find(entry.getKey());
		    	bar.setName(element.getDiaName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[++i] = bar;
		    	//определяем наиболее выраженный элемент
		    	if (entry.getValue() > score) {
		    		score = entry.getValue();
		    		sphere = element;
		    	}
		    }
		    if (sphere != null) {
				tr = new Tag("tr");
				td = new Tag("td");
				td.add(util.getBoldTaggedString(sphere.getDescription()));
				td.add(util.getNormalTaggedString(sphere.getText()));
				printGenderText(sphere.getGenderText(), event, td);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				a = new Tag("a");
				a.add("Экстраверсия и интроверсия в сознании");
				td.add(a);
				tr.add(td);
				cell.add(tr);
				
			    tr = new Tag("tr");
				td = new Tag("td");
				Tag chart = util.getPlotkitChart(bars, null, "pie", "spherechart", "spherechartopts", "drawSphereChart", 0, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				a = new Tag("a");
				a.add("Экстраверсия и интроверсия в поступках");
				td.add(a);
				tr.add(td);
				cell.add(tr);
				
				sphereMap = statistics.getHouseHalfspheres();
				bars = new Bar[sphereMap.size()];
				iterator = sphereMap.entrySet().iterator();
				i = -1;
				sphere = null;
			    while (iterator.hasNext()) {
			    	Entry<String, Double> entry = iterator.next();
			    	Bar bar = new Bar();
			    	Halfsphere element = (Halfsphere)service.find(entry.getKey());
			    	bar.setName(element.getDiaName());
			    	bar.setValue(entry.getValue());
			    	bar.setColor(element.getColor());
			    	bars[++i] = bar;
			    }
				
			    tr = new Tag("tr");
				td = new Tag("td");
				chart = util.getPlotkitChart(bars, null, "pie", "sphere2chart", "sphere2chartopts", "drawSphere2Chart", 1, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);
		    }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация инь-ян
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param statistics объект статистики
	 */
	private void generateYinYang(Event event, Tag cell, EventStatistics statistics) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=zones");
			a.add("Гармония мужского и женского начала");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			Map<String, Double> yinYangMap = statistics.getPlanetYinYangs();
			Bar[] bars = new Bar[yinYangMap.size()];
			Iterator<Map.Entry<String, Double>> iterator = yinYangMap.entrySet().iterator();
			int i = -1;
			YinYang yinyang = null;
			double score = 0.0;
			YinYangService service = new YinYangService();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	YinYang element = (YinYang)service.find(entry.getKey());
		    	bar.setName(element.getDiaName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[++i] = bar;
		    	//определяем наиболее выраженный элемент
		    	if (entry.getValue() > score) {
		    		score = entry.getValue();
		    		yinyang = element;
		    	}
		    }
		    if (yinyang != null) {
				tr = new Tag("tr");
				td = new Tag("td");
				td.add(util.getBoldTaggedString(yinyang.getDescription()));
				td.add(util.getNormalTaggedString(yinyang.getText()));
				printGenderText(yinyang.getGenderText(), event, td);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				a = new Tag("a");
				a.add("Женское и мужское в сознании человека");
				td.add(a);
				tr.add(td);
				cell.add(tr);
				
			    tr = new Tag("tr");
				td = new Tag("td");
				Tag chart = util.getPlotkitChart(bars, null, "pie", "yinyangchart", "yinyangchartopts", "drawYinYangChart", 1, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				a = new Tag("a");
				a.add("Женское и мужское в поступках человека");
				td.add(a);
				tr.add(td);
				cell.add(tr);
				
				yinYangMap = statistics.getHouseYinYangs();
				bars = new Bar[yinYangMap.size()];
				iterator = yinYangMap.entrySet().iterator();
				i = -1;
				yinyang = null;
			    while (iterator.hasNext()) {
			    	Entry<String, Double> entry = iterator.next();
			    	Bar bar = new Bar();
			    	YinYang element = (YinYang)service.find(entry.getKey());
			    	bar.setName(element.getDiaName());
			    	bar.setValue(entry.getValue());
			    	bar.setColor(element.getColor());
			    	bars[++i] = bar;
			    }
				
			    tr = new Tag("tr");
				td = new Tag("td");
				chart = util.getPlotkitChart(bars, null, "pie", "yinyang2chart", "yinyang2chartopts", "drawYinYang2Chart", 3, 300);
				td.add(chart);
				tr.add(td);
				cell.add(tr);
		    }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация содержания
	 * @param event событие
	 * @param cell ячейка-контейнер таблицы разметки
	 */
	private void generateContents(Event event, Tag cell) {
		try {
			//левая часть содержания

			Tag subtable = new Tag("table", "class=menu align=left");
			Tag tr = new Tag("tr");
			Tag td = new Tag("td");
			
			//характеристика личности
			Tag b = new Tag("b");
			b.add("Характеристика личности");
			td.add(b);
			List<Model> list = new CategoryService().getList();
			for (Model model : list) {
				Category category = (Category)model;
				td.add(new Tag("/br"));
				Tag a = new Tag("a", "href=#" + category.getCode());
				a.add(category.getName());
				td.add(a);
			}
			td.add(new Tag("br"));

			//общая информация
			Map<String, String> contents = new HashMap<String, String>();
			contents.put("celebrity", "Знаменитости");
			contents.put("similar", "Близкие по духу");
			contents.put("signs", "Выраженные Знаки Зодиака");
			contents.put("dsigns", "Кредо вашей жизни");
			contents.put("degree", "Символ рождения");

			b = new Tag("b");
			b.add("Общая информация");
			td.add(b);
			for (Map.Entry<String, String> entry : contents.entrySet()) {
				td.add(new Tag("/br"));
				Tag a = new Tag("a", "href=#" + entry.getKey());
				a.add(entry.getValue());
				td.add(a);
			}
			td.add(new Tag("br"));
			tr.add(td);
			subtable.add(tr);
			cell.add(subtable);
	
			//правая часть содержания

			subtable = new Tag("table", "class=menu align=right");
			tr = new Tag("tr");
			td = new Tag("td");

			//описание космограммы
			contents = new HashMap<String, String>();
			contents.put("cosmogram", "Анализ космограммы");
			contents.put("planets", "Планеты");
			contents.put("aspects", "Аспекты планет");
			contents.put("configurations", "Конфигурации планет");
			
			b = new Tag("b");
			b.add("Космограмма");
			td.add(b);
			for (Map.Entry<String, String> entry : contents.entrySet()) {
				td.add(new Tag("/br"));
				Tag a = new Tag("a", "href=#" + entry.getKey());
				a.add(entry.getValue());
				td.add(a);
			}
			td.add(new Tag("br"));

			b = new Tag("b");
			b.add("Реализация личности");
			td.add(b);
			for (Model hmodel : event.getConfiguration().getHouses()) {
				House house = (House)hmodel;
				//Определяем количество планет в доме
				List<Planet> planets = new ArrayList<Planet>();
				for (Model pmodel : event.getConfiguration().getPlanets()) {
					Planet planet = (Planet)pmodel;
					if (planet.getCode().equals("Kethu")) continue;
					if (planet.getHouse().getId().equals(house.getId()))
						planets.add(planet);
				}
				//Создаем информационный блок только если дом не пуст
				if (planets.size() > 0) {
					td.add(new Tag("/br"));
					Tag a = new Tag("a", "href=#" + house.getLinkName());
					a.add(house.getHeaderName());
					td.add(a);
				}
			}
			td.add(new Tag("br"));
			tr.add(td);
			subtable.add(tr);
			cell.add(subtable);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Генерация знаменитостей
	 * @param date дата события
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void generateCelebrities(Date date, Tag cell) {
		try {
			List<Event> list = new EventService().findEphemeron(date);
			if (list != null && list.size() > 0) {
				Tag tr = new Tag("tr");
				Tag td = new Tag("td", "class=header");
				Tag a = new Tag("a", "name=celebrity");
				a.add("Однодневки");
				td.add(a);
				tr.add(td);
				cell.add(tr);
	
				tr = new Tag("tr");
				td = new Tag("td");
				td.add(util.getNormalTaggedString("В один день с вами родились такие известные люди:"));
				Tag p = new Tag("p");
				
				for (Model model : list) {
					Event event = (Event)model;
					p.add(util.getSmallTaggedString(DateUtil.formatDate(event.getBirth())));
					p.add(util.getBoldTaggedSubstring(event.getName()));
					p.add(util.getSmallTaggedString("&nbsp;&nbsp;&nbsp;" + event.getDescription()));
					p.add(new Tag("/br"));
				}
				td.add(p);
				tr.add(td);
				cell.add(tr);
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
	private void generateSignChart(Tag cell, Map<String, Double> signMap) {
		try {
			//выраженные знаки
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=signs");
			a.add("Выраженные Знаки Зодиака");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			Bar[] bars = new Bar[signMap.size()];
			Bar[] bars2 = new Bar[signMap.size()];
			Iterator<Map.Entry<String, Double>> iterator = signMap.entrySet().iterator();
			int i = -1;
			SignService service = new SignService();
		    while (iterator.hasNext()) {
		    	i++;
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	Sign sign = (Sign)service.find(entry.getKey());
		    	bar.setName(sign.getName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(sign.getColor());
		    	bars[i] = bar;
	
		    	bar = new Bar();
		    	bar.setName(sign.getDescription());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(sign.getColor());
		    	bars2[i] = bar;
		    }
			tr = new Tag("tr");
			td = new Tag("td");
//			Tag p = new Tag("p", "class=shot");
//			Tag chart = util.getTaggedChart(17, bars, null);
			Tag chart = util.getPlotkitChart(bars, null, "pie", "signchart", "signchartopts", "drawSignChart", 4, 300);
			td.add(chart);
//			p.add(chart);
//			td.add(p);
			tr.add(td);
			cell.add(tr);
	
			//кредо
			tr = new Tag("tr");
			td = new Tag("td", "class=header");
			a = new Tag("a", "name=dsigns");
			a.add("Кредо Вашей жизни");
			td.add(a);
			tr.add(td);
			cell.add(tr);
	
			tr = new Tag("tr");
			td = new Tag("td");
//			Tag p = new Tag("p", "class=shot");
//			chart = util.getTaggedChart(17, bars2, null);
			chart = util.getPlotkitChart(bars2, null, "pie", "sign2chart", "sign2chartopts", "drawSignChart2", 1, 300);
			td.add(chart);
//			p.add(chart);
//			td.add(p);
			tr.add(td);
			cell.add(tr);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация диаграмм домов
	 * @param statistics объект статистики события
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void generateHouseChart(EventStatistics statistics, Tag cell) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=dhouses");
			a.add("Чем будет заполнена ваша жизнь");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			Map<String, Double> houses = statistics.getPlanetHouses();
			Bar[] bars = new Bar[houses.size()];
			Iterator<Map.Entry<String, Double>> iterator = houses.entrySet().iterator();
	    	int i = -1;
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	House house = statistics.getHouse(entry.getKey());
		    	bar.setName(house.getShortName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(house.getColor());
		    	bars[++i] = bar;
		    }
			tr = new Tag("tr");
			td = new Tag("td");
//			Tag p = new Tag("p", "class=shot");
//			Tag chart = util.getTaggedChart(17, bars, null);
			Tag chart = util.getPlotkitChart(bars, null, "pie", "housechart", "housechartopts", "drawHouseChart", 3, 400);
			td.add(chart);
//			p.add(chart);
//			td.add(p);
			tr.add(td);
			cell.add(tr);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация градуса рождения
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void generateDegree(Event event, Tag cell) {
		try {
			if (event.getConfiguration().getHouses() != null &&
					event.getConfiguration().getHouses().size() > 0) {
				House house = (House)event.getConfiguration().getHouses().get(0);
				if (null == house) return;
				int value = (int)house.getCoord();
				Model model = new DegreeService().find(new Long(String.valueOf(value)));
			    if (model != null) {
			    	TextGenderDictionary degree = (TextGenderDictionary)model;
					Tag tr = new Tag("tr");
					Tag td = new Tag("td", "class=header");
					Tag a = new Tag("a", "name=degree");
					a.add("Символ рождения");
					td.add(a);
					tr.add(td);
					cell.add(tr);
					
					tr = new Tag("tr");
					td = new Tag("td");
					Tag p = new Tag("p", "class=name");
					Tag tag = util.getBoldTaggedString(degree.getId() + "&#176; " + degree.getCode());
					p.add(tag);
					td.add(p);
					
					p = new Tag("p", "class=desc");
					if (degree.getDescription() != null) {
						tag = util.getItalicTaggedString(degree.getDescription());
						p.add(tag);
					}
					td.add(p);

					if (degree.getText() != null) {
						tag = util.getNormalTaggedString(degree.getText());
						td.add(tag);
					}
					tr.add(td);
					cell.add(tr);
			    }
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация планет в знаках
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void generatePlanetsInSigns(Event event, Tag cell) {
		try {
			if (event.getConfiguration().getPlanets() != null) {
				for (Model model : event.getConfiguration().getPlanets()) {
					Planet planet = (Planet)model;
				    if (planet.isMain()) {
				    	List<Object> list = new ExportService().getPlanetInSignText(planet, planet.getSign());
				    	if (list != null && list.size() > 0) 
				    		for (Object object : list) {
				    			List<Object> row = (List<Object>)object;
								Tag tr = util.getTaggedHeader(
										row.get(1).toString(),	//description
										row.get(2).toString());	//linkname
								cell.add(tr);
	
								tr = new Tag("tr");
								Tag td = new Tag("td");
								Tag p = new Tag("p");
								p.add(row.get(3).toString());	//text
								td.add(p);
								
								GenderText genderText = new GenderText();
								genderText.setText((String)row.get(4));
								genderText.setType((String)row.get(5));
								printGenderText(genderText, event, td);
								td.add(new Tag("/br"));
								tr.add(td);
								cell.add(tr);
				    		}
				    }
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация космограммы
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void generateCard(Event event, Tag cell) {
		try {
			//генерация источника данных
			new XMLExporter(event.getConfiguration());
			
			//космограмма
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=cosmogram");
			a.add("Космограмма");
			td.add(a);
			tr.add(td);
			cell.add(tr);
	
			tr = new Tag("tr");
			td = new Tag("td");
			Tag p = new Tag("p", "class=shot");
			Tag object = new Tag("object", 
				"classid=clsid:d27cdb6e-ae6d-11cf-96b8-444553540000 " +
				"codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0\" " +
				"width=514 height=514 " +
				"id=cosmogram " +
				"align=center");
			Tag tag = new Tag("param", "name=allowScriptAccess value=sameDomain");
			object.add(tag);
			tag = new Tag("param", "name=movie value=horoscope_files/cosmogram.swf");
			object.add(tag);
			tag = new Tag("param", "name=quality value=high");
			object.add(tag);
			tag = new Tag("param", "name=bgcolor value=#ffffff");
			object.add(tag);
			tag = new Tag("embed", 
				"src=horoscope_files/cosmogram.swf " +
				"quality=high " +
				"bgcolor=#ffffff " +
				"width=514 height=514 " +
				"name=cosmogram " +
				"align=center " +
				"allowScriptAccess=sameDomain " +
				"type=application/x-shockwave-flash " +
				"pluginspage=http://www.macromedia.com/go/getflashplayer");
			object.add(tag);
			tag = new Tag("allowScriptAccess", "name=bgcolor value=#ffffff");
			object.add(tag);
			p.add(object);
			td.add(p);
			tr.add(td);
			cell.add(tr);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация вида космограммы
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * TODO фиктивные планеты при определении вида космограмм не считаются!!!
	 */
	private void generateCardKarma(Event event, Tag cell) {
		try {
//			CardKind type = null;
//			//упорядочиваем массив планет по возрастанию
//			List<Planet> planets = new ArrayList<Planet>();
//			for (BaseEntity entity : event.getConfiguration().getPlanets())
//				planets.add((Planet)entity);
//			Collections.sort(planets, new SkyPointComparator());
//			
//			//расчет интервалов между планетами
//			double max = 0.0;
//			double[] cuts = new double[planets.size()]; 
//			for (int i = 0; i < planets.size(); i++) {
//				int n = (i == planets.size() - 1) ? 0 : i + 1;
//				double value = CalcUtil.getDifference(planets.get(i).getCoord(), planets.get(n).getCoord());
//				cuts[i] = value;
//				if (value > max) max = value;
//			}
//			
//			if (type != null) {
//				Tag tr = new Tag("tr");
//				Tag td = new Tag("td", "class=header");
//				Tag a = new Tag("a", "name=karma");
//				a.add("Карма прошлой жизни");
//				td.add(a);
//				tr.add(td);
//				cell.add(tr);
//
//				tr = new Tag("tr");
//				td = new Tag("td");
//				Tag p = new Tag("p", "class=name"); 
//				p.add(type.getName());
//				td.add(p);
//				p = new Tag("p", "class=desc"); 
//				p.add(type.getDescription());
//				td.add(p);
//				p = new Tag("p"); 
//				p.add(type.getText());
//				td.add(p);
//				tr.add(td);
//				cell.add(tr);
//			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Генерация типа космограммы
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param signMap карта знаков
	 */
	private void generateCardType(Event event, Tag cell, Map<String, Integer> signMap) {
		try {
			if (event.getConfiguration().getPlanets() != null) {
				String type = "";
				Planet sun = (Planet)event.getConfiguration().getPlanets().get(0);
				Planet moon = (Planet)event.getConfiguration().getPlanets().get(1);
				
				if (sun.getSign().getId().equals(moon.getSign().getId()))
					type = "centered";
				else {
					int sunSign = signMap.get(sun.getSign().getCode());
					int moonSign = signMap.get(moon.getSign().getCode());
					if (sunSign > 1 & moonSign == 1)
						type = "solar";
					else if (sunSign == 1 & moonSign > 1)
						type = "lunar";
					else if (sunSign > 1 & moonSign > 1) {
						if (sunSign == moonSign)
							type = "equivalent";
						else
							type = (sunSign > moonSign) ? "solar_lunar" : "lunar_solar";
					} else if (sunSign == 1 & moonSign == 1) {
						//определяем знак, в котором больше всего планет
						int max = 0;
						for (Iterator<Integer> iterator = signMap.values().iterator(); iterator.hasNext();) {
							int value = iterator.next();
							if (max < value) max = value;
						}
						type = (max > 2) ? "planetary" : "scattered";
					}
				}
			
				if (type.length() > 0) {
				    Model model = new CardTypeService().find(type);
				    if (model != null) {
				    	TextGenderDictionary cardType = (TextGenderDictionary)model;
						Tag tr = new Tag("tr");
						Tag td = new Tag("td", "class=header");
						Tag a = new Tag("a", "name=type");
						a.add("Самораскрытие человека");
						td.add(a);
						tr.add(td);
						cell.add(tr);
	
						tr = new Tag("tr");
						td = new Tag("td");
						Tag tag = util.getBoldTaggedString(cardType.getName());
						td.add(tag);
						tag = util.getItalicTaggedString(cardType.getDescription());
						td.add(tag);
						tag = util.getNormalTaggedString(cardType.getText());
						td.add(tag);
						tr.add(td);
						cell.add(tr);
				    }
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Генерация стихий
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 * @param statistics объект статистики
	 */
	private void generateElements(Event event, Tag cell, EventStatistics statistics) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header");
			Tag a = new Tag("a", "name=elements");
			a.add("Темперамент");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			//определение выраженной стихии
			Map<String, Double> elementMap = statistics.getPlanetElements();
			String[] elements = new String[elementMap.size()];
			Bar[] bars = new Bar[elementMap.size()];
			Iterator<Map.Entry<String, Double>> iterator = elementMap.entrySet().iterator();
			int i = -1;
			ElementService service = new ElementService();
		    while (iterator.hasNext()) {
		    	i++;
		    	Entry<String, Double> entry = iterator.next();
		    	elements[i] = entry.getKey();
		    	Bar bar = new Bar();
		    	Element element = (Element)service.find(entry.getKey());
		    	bar.setName(element.getDiaName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[i] = bar;
		    }
		    Element element = null;
		    for (Model model : service.getList()) {
		    	element = (Element)model;
		    	String[] codes = element.getCode().split("_");
		    	if (codes.length == elements.length) {
		    		boolean match = true;
		    		for (String code : codes)
		    			if (!Arrays.asList(elements).contains(code)) {
		    				match = false;
		    				break;
		    			}
		    		if (match)
		    			break;
		    		else
		    			continue;
		    	}
		    }
			
		    if (element != null) {
				tr = new Tag("tr");
				td = new Tag("td");
				Tag p = new Tag("p");
				Tag tag = util.getBoldTaggedString(element.getName());
				p.add(tag);
				tag = util.getNormalTaggedString(element.getText());
				p.add(tag);
				printGenderText(element.getGenderText(), event, p);
				td.add(p);
				tr.add(td);
				cell.add(tr);
		    }
				
			//Характеристика проявлений личности
		    tr = new Tag("tr");
			td = new Tag("td", "class=header");
			a = new Tag("a");
			a.add("Темперамент в сознании");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			tr = new Tag("tr");
			td = new Tag("td");
			a = new Tag("p");
			a.add(element.getTemperament());
			td.add(a);
			
//			Tag chart = util.getTaggedChart(17, bars, null);
			Tag chart = util.getPlotkitChart(bars, null, "pie", "elemchart", "elemchartopts", "drawElemChart", 5, 300);
			td.add(chart);
			tr.add(td);
			cell.add(tr);
			
			//Характеристика реализации личности
			elementMap = statistics.getHouseElements();
			bars = new Bar[elementMap.size()];
			iterator = elementMap.entrySet().iterator();
			i = -1;
		    while (iterator.hasNext()) {
		    	i++;
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	element = (Element)service.find(entry.getKey());
		    	bar.setName(element.getDiaName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[i] = bar;
		    }
			
		    tr = new Tag("tr");
			td = new Tag("td", "class=header");
			a = new Tag("a");
			a.add("Темперамент в поступках");
			td.add(a);
			tr.add(td);
			cell.add(tr);
			
			tr = new Tag("tr");
			td = new Tag("td");
//			p = new Tag("p", "class=shot");
//			chart = util.getTaggedChart(17, bars, null);
			chart = util.getPlotkitChart(bars, null, "pie", "elem2chart", "elem2chartopts", "drawElemChart2", 0, 300);
			td.add(chart);
//			p.add(chart);
//			td.add(p);
			tr.add(td);
			cell.add(tr);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Экспорт данных в файл
	 * @param html-файл
	 * @todo использовать конфиг для задания пути
	 */
	private void export(String html) {
		try {
			String datafile = PlatformUtil.getPath(Activator.PLUGIN_ID, "/out/horoscope.html").getPath(); //$NON-NLS-1$
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter( 
				new FileOutputStream(datafile), "UTF-8"));
			writer.append(html);
			writer.close();
			//TODO показывать диалог, что документ сформирован
			//а ещё лучше открывать его
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Экспорт завершён");
		}
	}

	/**
	 * Печать текста для мужчин, женщин, детей
	 * @param text объект текста для разных полов
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void printGenderText(GenderText text, Event event, Tag cell) {
		if (text != null) {
			boolean isMale = !event.isFemale();
			String male = text.getText();
			String female = text.getType();
//			String child = text.getObjectId();
			Tag tag = util.getGenderHeader(isMale, male, female, child);
			if (tag != null) cell.add(tag);					//gender header
			tag = util.getGenderText(isMale, male, female, child);
			if (tag != null) cell.add(tag);					//gender
		}
	}

	/**
	 * Отображение информации о копирайте
	 * @return html-тег с содержимым
	 */
	private Tag printCopyright() {
		Tag cell = new Tag("div", "class=copyright");
		cell.add("&copy; 1998-");
		Tag script = new Tag("script", "type=text/javascript");
		script.add("var year = new Date().getYear(); if (year < 1000) year += 1900; document.write(year);");
		cell.add(script);
		cell.add("Астрологический сервис" + "&nbsp;");
		Tag a = new Tag("a", "href=http://zvezdochet.kz/ target=_blank");
		a.add("«Звездочёт»");
		cell.add(a);
		cell.add(" &mdash; Взгляни на себя в прошлом, настоящем и будущем");
		return cell;
	}

	/**
	 * Генерация похожих по характеру знаменитостей
	 * @param date дата события
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void generateSimilar(Event cevent, Tag cell) {
		try {
			List<Model> list = new EventService().findSimilar(cevent, 1);
			if (list != null && list.size() > 0) {
				Tag tr = new Tag("tr");
				Tag td = new Tag("td", "class=header");
				Tag a = new Tag("a", "name=similar");
				a.add("Близкие по духу");
				td.add(a);
				tr.add(td);
				cell.add(tr);
	
				tr = new Tag("tr");
				td = new Tag("td");
				Tag p = new Tag("p");
				for (Model model : list) {
					Event event = (Event)model;
					p.add(util.getSmallTaggedString(DateUtil.formatDate(event.getBirth())));
					p.add(util.getBoldTaggedSubstring(event.getName()));
					p.add(util.getSmallTaggedString("&nbsp;&nbsp;&nbsp;" + event.getDescription()));
					p.add(new Tag("/br"));
				}
				td.add(p);
				tr.add(td);
				cell.add(tr);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
