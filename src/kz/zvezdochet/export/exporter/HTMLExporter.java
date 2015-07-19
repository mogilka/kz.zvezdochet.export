package kz.zvezdochet.export.exporter;

import html.Tag;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
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
import kz.zvezdochet.analytics.bean.CrossSign;
import kz.zvezdochet.analytics.bean.PlanetAspectText;
import kz.zvezdochet.analytics.bean.PlanetHouseText;
import kz.zvezdochet.analytics.bean.PlanetSignText;
import kz.zvezdochet.analytics.bean.PlanetText;
import kz.zvezdochet.analytics.service.AnalyticsService;
import kz.zvezdochet.analytics.service.CardTypeService;
import kz.zvezdochet.analytics.service.CategoryService;
import kz.zvezdochet.analytics.service.CrossSignService;
import kz.zvezdochet.analytics.service.DegreeService;
import kz.zvezdochet.analytics.service.PlanetAspectService;
import kz.zvezdochet.analytics.service.PlanetHouseService;
import kz.zvezdochet.analytics.service.PlanetSignService;
import kz.zvezdochet.analytics.service.PlanetTextService;
import kz.zvezdochet.bean.AspectType;
import kz.zvezdochet.bean.Cross;
import kz.zvezdochet.bean.Element;
import kz.zvezdochet.bean.Event;
import kz.zvezdochet.bean.Halfsphere;
import kz.zvezdochet.bean.House;
import kz.zvezdochet.bean.Place;
import kz.zvezdochet.bean.Planet;
import kz.zvezdochet.bean.Sign;
import kz.zvezdochet.bean.SkyPoint;
import kz.zvezdochet.bean.SkyPointAspect;
import kz.zvezdochet.bean.Square;
import kz.zvezdochet.bean.YinYang;
import kz.zvezdochet.bean.Zone;
import kz.zvezdochet.core.bean.Model;
import kz.zvezdochet.core.bean.TextGender;
import kz.zvezdochet.core.bean.TextGenderDictionary;
import kz.zvezdochet.core.util.CalcUtil;
import kz.zvezdochet.core.util.DateUtil;
import kz.zvezdochet.core.util.PlatformUtil;
import kz.zvezdochet.export.Activator;
import kz.zvezdochet.export.bean.Bar;
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
import kz.zvezdochet.util.Cosmogram;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

/**
 * Генератор HTML-файлов для экспорта данных
 * @author Nataly Didenko
 *
 */
@SuppressWarnings("unchecked")
public class HTMLExporter {
	private HTMLUtil util;
	private boolean child = false;
	private Display display;

	public HTMLExporter(Display display) {
		this.display = display;
		util = new HTMLUtil();
	}

	/**
	 * Генерация индивидуального гороскопа
	 * @param event событие
	 */
	public void generate(Event event) {
		child = event.getAge() < event.MAX_TEEN_AGE;
		saveCard(event);
		try {
			Tag html = new Tag("html");
			Tag head = new Tag("head");
			head.add(new Tag("meta", "http-equiv=Content-Type content=text/html; charset=UTF-8"));
			head.add(new Tag("link", "href=horoscope_files/horoscope.css rel=stylesheet type=text/css"));
			head.add(new Tag("link", "href=horoscope_files/chart.css rel=stylesheet type=text/css"));
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
			Place place = event.getPlace();
			if (null == place)
				place = new Place().getDefault();
			cell.add(DateUtil.fulldtf.format(event.getBirth()) +
				"&ensp;" + (event.getZone() >= 0 ? "UTC+" : "") + event.getZone() +
				"&ensp;" + (event.getDst() >= 0 ? "DST+" : "") + event.getDst() + 
				"&emsp;" + place.getName() +
				"&ensp;" + place.getLatitude() + "&#176;" +
				", " + place.getLongitude() + "&#176;");
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

			//знаки
			EventStatistics statistics = new EventStatistics(event.getConfiguration());
			Map<String, Double> signMap = statistics.getPlanetSigns(true);
			generateSignChart(table, signMap);
			statistics.initPlanetHouses();

			//градус рождения
			if (!child)
				generateDegree(event, table);
			
			//планеты в знаках
			generatePlanetsInSigns(event, table);
			
			//космограмма
			generateCard(event, table);
			
			//вид космограммы
			generateCardKind(event, table);

			//тип космограммы
			Map<String, Integer> signPlanetsMap = statistics.getSignPlanets();
			generateCardType(event, table, signPlanetsMap);
			
			//стихии
			statistics.initPlanetDivisions();
			statistics.initHouseDivisions();
			generateElements(event, table, statistics);
	
			//инь-ян
			generateYinYang(event, table, statistics);
			
			//полусферы
			generateHalfSpheres(event, table, statistics);
			
			//квадраты
			generateSquares(event, table, statistics, signMap);
			
			//кресты
			generateCrosses(event, table, statistics);
			
			//зоны
			generateZones(event, table, statistics);
			
			//планеты
			generatePlanets(event, table);
			
			//аспекты
			generateAspectTypes(event, table);
			//позитивные аспекты
			generateAspects(event, table, "Позитивные аспекты планет", "POSITIVE");
			//негативные аспекты
			generateAspects(event, table, "Негативные аспекты планет", "NEGATIVE");
			//конфигурации аспектов
			generateAspectConfigurations(event, table);

			//дома
			generateHouseChart(statistics, table);

			//планеты в домах
			Map<String, Double> houseMap = statistics.getPlanetHouses();
			generatePlanetInHouses(event, table, houseMap);
			//дома в знаках
			//generateHouseInSigns(event, table, houseMap);
			
			if (html != null) {
//				System.out.println(html);
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
				Sign sign = SkyPoint.getSign(house.getCoord(), event.getBirthYear());
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
					td.add(dict.getText());
					
					List<TextGender> genders = dict.getGenderTexts(event.isFemale(), child);
					for (TextGender gender : genders)
						printGenderText(gender, event, td);
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
				//Создаем информационный блок, только если дом не пуст
				if (planets.size() > 0) {
					Tag tr = util.getTaggedHeader(house.getHeaderName(), house.getLinkName());
					cell.add(tr);
			
					tr = new Tag("tr");
					Tag td = new Tag("td");
					PlanetHouseService service = new PlanetHouseService();
					for (Planet planet : planets) {
						PlanetHouseText dict = (PlanetHouseText)service.find(planet, house, null);
						if (dict != null) {
							td.add(util.getBoldTaggedString(
								planet.getName() + " " + house.getCombination()));
							td.add(dict.getText());
							
							List<TextGender> genders = dict.getGenderTexts(event.isFemale(), child);
							for (TextGender gender : genders)
								printGenderText(gender, event, td);
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
			Tag td = new Tag("td", "class=header id=planets");
			td.add("Планеты");
			tr.add(td);
			cell.add(tr);

			tr = new Tag("tr");
			td = new Tag("td");
			PlanetTextService service = new PlanetTextService();
			for (Model model : event.getConfiguration().getPlanets()) {
				Planet planet = (Planet)model;
				PlanetText planetText = null;
				if (planet.isKernel()) {
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "kernel");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + "-ядро"));
						td.add(planetText.getText());
					}
				} else if (planet.isBelt()) {
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "belt");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + "-пояс"));
						td.add(planetText.getText());
					}
				}
				if (planet.isSword()) {
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "sword");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + "-меч"));
						td.add(planetText.getText());
					}
				} else if (planet.isShield()) {
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "shield");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + "-щит"));
						td.add(planetText.getText());
					}
				}
				if (planet.inMine()) {
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "mine");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + " в шахте"));
						td.add(planetText.getText());
					}
				}
				if (planet.isDamaged()) {
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "damaged");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + "-дисгармония"));
						td.add(planetText.getText());
					}
				} else if (planet.isPerfect()) {
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "perfect");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + "-гармония"));
						td.add(planetText.getText());
					}
				} else if (planet.isBroken()) { 
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "weak");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + "-слабость"));
						td.add(planetText.getText());
					}
				}
				if (planet.isStrong()) {
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "strong");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + "-сила"));
						td.add(planetText.getText());
					}
				}
				if (planet.isRetrograde()) {
					planetText = (PlanetText)service.findByPlanet(planet.getId(), "retro");
					if (planetText != null) {
						td.add(util.getBoldTaggedString(planet.getName() + "-ретроград"));
						td.add(planetText.getText());
					}
				}
				if (planetText != null) {
					List<TextGender> genders = planetText.getGenderTexts(event.isFemale(), child);
					for (TextGender gender : genders)
						printGenderText(gender, event, td);
					td.add(new Tag("/br"));
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
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header id=configurations");
			td.add("Конфигурации аспектов планет");
			tr.add(td);
			cell.add(tr);

			tr = new Tag("tr");
			td = new Tag("td");

			for (int i = 0; i < 8; i++) {
				Tag img = new Tag("img", "src=horoscope_files/conf/cross.gif align=left");
				td.add(img);
				Tag tag = util.getBoldTaggedString("Конфигурация");
				td.add(tag);
				tag = util.getNormalTaggedString("Описание");
				td.add(tag);
				td.add(new Tag("/br"));
			}
			tr.add(td);
			cell.add(tr);
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
			Tag td = new Tag("td", "class=header id=aspects");
			td.add("Соотношение аспектов планет");
			tr.add(td);
			cell.add(tr);
	
			tr = new Tag("tr");
			td = new Tag("td");
			Tag p = new Tag("p");
			Tag span = new Tag("span", "style=color:#903");
			span.add("Больше гармоничных &mdash; меньше препятствий<br>");
			p.add(span);
			
			span = new Tag("span", "style=color:#063");
			span.add("Больше творческих &mdash; меньше ограничений<br>");
			p.add(span);
			
			span = new Tag("span", "style=color:#630");
			span.add("Больше нейтральных &mdash; больше планет проявляются в симбиозе, а не поодиночке<br>");
			p.add(span);
			
			span = new Tag("span");
			span.add("Больше негармоничных &mdash; больше резких и тяжёлых ситуаций<br>");
			p.add(span);
			
			span = new Tag("span", "style=color:#666");
			span.add("Больше скрытых &mdash; больше событий происходит за кулисами жизни<br>");
			p.add(span);
			
			span = new Tag("span", "style=color:#369");
			span.add("Больше кармических &mdash; больше тупиковых ситуаций, которые нужно преодолеть<br>");
			p.add(span);
			
			span = new Tag("span", "style=color:#399");
			span.add("Больше прогрессивных аспектов (искушения) &mdash; большим испытаниям вы подвергнуты<br>");
			p.add(span);
			
			span = new Tag("span", "style=color:#609");
			span.add("Чем больше духовных (ДАО, порабощение, магические аспекты) &mdash; тем более высокого уровня развития вы достигли");
			p.add(span);
			td.add(p);
			Tag chart = util.getCss3Chart(bars, null);
			td.add(chart);
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
			td.add(header);
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

						td.add(dict.getText());

						List<TextGender> genders = dict.getGenderTexts(event.isFemale(), child);
						for (TextGender gender : genders)
							printGenderText(gender, event, td);
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
			Tag td = new Tag("td", "class=header id=zones");
			td.add("Развитие духа в сознании");
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
				td.add(zone.getText());

				List<TextGender> genders = zone.getGenderTexts(event.isFemale(), child);
				for (TextGender gender : genders)
					printGenderText(gender, event, td);

				Tag p = new Tag("p");
				p.add("Диаграмма показывает, какие приоритеты человек ставит в своём развитии.");
				td.add(p);
				Tag chart = util.getCss3Chart(bars, null);
				td.add(chart);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				td.add("Развитие духа в действии");
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
				p = new Tag("p");
				p.add("Диаграмма показывает, как на событийном уровне, в действии меняются приоритеты развития человека по сравнению с предыдущей моделью.");
				td.add(p);
				chart = util.getCss3Chart(bars, null);
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
			Tag td = new Tag("td", "class=header id=crosses");
			td.add("Стратегия в сознании");
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
				td.add(cross.getText());

				List<TextGender> genders = cross.getGenderTexts(event.isFemale(), child);
				for (TextGender gender : genders)
					printGenderText(gender, event, td);

				Tag p = new Tag("p");
				p.add("Диаграмма показывает, какой тип стратегии наиболее присущ человеку в мыслях и принятии решений.");
				td.add(p);
				Tag chart = util.getCss3Chart(bars, null);
				td.add(chart);
				tr.add(td);
				cell.add(tr);
		    }

			//знаки
			crossMap = statistics.getCrossSigns();
			bars = new Bar[crossMap.size()];
			iterator = crossMap.entrySet().iterator();
			i = -1;
			CrossSignService service2 = new CrossSignService();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	CrossSign element = (CrossSign)service2.find(entry.getKey());
		    	bar.setName(element.getName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[++i] = bar;
		    }
			
			tr = new Tag("tr");
			td = new Tag("td");
			Tag p = new Tag("p");
			p.add("Диаграмма показывает, в каких качествах выражается стратегия намерений человека.");
			td.add(p);
			Tag chart = util.getCss3Chart(bars, null);
			td.add(chart);
			tr.add(td);
			cell.add(tr);

			//
			tr = new Tag("tr");
			td = new Tag("td", "class=header");
			td.add("Стратегия в поступках");
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
			p = new Tag("p");
			p.add("Диаграмма показывает, как стратегия человека меняется в действии (на событийном уровне, в социуме).");
			td.add(p);
			chart = util.getCss3Chart(bars, null);
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
		    	CrossSign element = (CrossSign)service2.find(entry.getKey());
		    	bar.setName(element.getName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	bars[++i] = bar;
		    }
			
			tr = new Tag("tr");
			td = new Tag("td");
			p = new Tag("p");
			p.add("Диаграмма показывает, в каких качествах выражается стратегия действий человека.");
			td.add(p);
			chart = util.getCss3Chart(bars, null);
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
	private void generateSquares(Event event, Tag cell, EventStatistics statistics, Map<String, Double> signMap) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header id=squares");
			td.add("Зрелость в сознании");
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
				td.add(square.getText());
				
				List<TextGender> genders = square.getGenderTexts(event.isFemale(), child);
				for (TextGender gender : genders)
					printGenderText(gender, event, td);
				
				Tag p = new Tag("p");
				p.add("Диаграмма показывает, как в мыслях и намерениях человека выражены качества разных возрастных групп.");
				td.add(p);
				Tag chart = util.getCss3Chart(bars, null);
				td.add(chart);
				tr.add(td);
				cell.add(tr);

				//знаки
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
				p = new Tag("p");
				p.add("Диаграмма показывает, в каких качествах выражается зрелость мыслей.");
				td.add(p);
				chart = util.getCss3Chart(bars, null);
				td.add(chart);
				tr.add(td);
				cell.add(tr);

				//
			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				td.add("Зрелость в поступках");
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
				p = new Tag("p");
				p.add("Диаграмма показывает, как на событийном уровне, в социуме меняется зрелость намерений человека по сравнению с предыдущей моделью.");
				td.add(p);
				chart = util.getCss3Chart(bars, null);
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
				p = new Tag("p");
				p.add("Диаграмма показывает, в каких качествах выражается зрелость поступков.");
				td.add(p);
				chart = util.getCss3Chart(bars, null);
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
			Tag td = new Tag("td", "class=header id=halfspheres");
			td.add("Экстраверсия и интроверсия в сознании");
			tr.add(td);
			cell.add(tr);
			
			Map<String, Double> sphereMap = statistics.getPlanetHalfspheres();
			List<Bar> bars = new ArrayList<Bar>();
			List<Bar> bars2 = new ArrayList<Bar>();
			Iterator<Map.Entry<String, Double>> iterator = sphereMap.entrySet().iterator();
			Halfsphere sphere = null;
			double score = 0.0;
			HalfsphereService service = new HalfsphereService();
			String[] extra = new String[] { "North", "West" };
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Bar bar = new Bar();
		    	Halfsphere element = (Halfsphere)service.find(entry.getKey());
		    	bar.setName(element.getDiaName());
		    	bar.setValue(entry.getValue());
		    	bar.setColor(element.getColor());
		    	if (Arrays.asList(extra).contains(element.getCode()))
		    		bars.add(bar);
		    	else
		    		bars2.add(bar);
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
				td.add(sphere.getText());
				
				List<TextGender> genders = sphere.getGenderTexts(event.isFemale(), child);
				for (TextGender gender : genders)
					printGenderText(gender, event, td);

				Tag p = new Tag("p");
				p.add("Диаграммы показывают, на что в мыслях нацелен человек.");
				td.add(p);
				Tag chart = util.getCss3Chart(bars.toArray(new Bar[2]), "Открытость");
				td.add(chart);
				chart = util.getCss3Chart(bars2.toArray(new Bar[2]), "Закрытость");
				td.add(chart);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				td.add("Экстраверсия и интроверсия в поступках");
				tr.add(td);
				cell.add(tr);
				
				sphereMap = statistics.getHouseHalfspheres();
				bars = new ArrayList<Bar>();
				bars2 = new ArrayList<Bar>();
				iterator = sphereMap.entrySet().iterator();
				sphere = null;
			    while (iterator.hasNext()) {
			    	Entry<String, Double> entry = iterator.next();
			    	Bar bar = new Bar();
			    	Halfsphere element = (Halfsphere)service.find(entry.getKey());
			    	bar.setName(element.getDiaName());
			    	bar.setValue(entry.getValue());
			    	bar.setColor(element.getColor());
			    	if (Arrays.asList(extra).contains(element.getCode()))
			    		bars.add(bar);
			    	else
			    		bars2.add(bar);
			    }
				
			    tr = new Tag("tr");
				td = new Tag("td");
				p = new Tag("p");
				p.add("Диаграммы показывают, как на практике меняется модель поведения человека в социуме.");
				td.add(p);
				chart = util.getCss3Chart(bars.toArray(new Bar[2]), "Открытость");
				td.add(chart);
				chart = util.getCss3Chart(bars2.toArray(new Bar[2]), "Закрытость");
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
			Tag td = new Tag("td", "class=header id=yinyang");
			td.add("Гармония мужского и женского начала в сознании");
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
				td.add(yinyang.getText());
				
				List<TextGender> genders = yinyang.getGenderTexts(event.isFemale(), child);
				for (TextGender gender : genders)
					printGenderText(gender, event, td);

				Tag p = new Tag("p");
				p.add("Диаграмма показывает, насколько активны намерения человека, когда он мыслит, принимает решения, находясь наедине с самим собой.");
				td.add(p);
				Tag chart = util.getCss3Chart(bars, null);
				td.add(chart);
				tr.add(td);
				cell.add(tr);

			    tr = new Tag("tr");
				td = new Tag("td", "class=header");
				td.add("Гармония мужского и женского начала в поступках");
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
				p = new Tag("p");
				p.add("Диаграмма показывает, как на событийном уровне, в социуме меняется активность намерений и проявлений человека по сравнению с предыдущей идеальной моделью.");
				td.add(p);
				chart = util.getCss3Chart(bars, null);
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
			Tag b = new Tag("h5");
			b.add("Характеристика личности");
			td.add(b);
			List<Model> list = new CategoryService().getList();
			for (Model model : list) {
				Category category = (Category)model;
				Tag a = new Tag("a", "href=#" + category.getCode());
				a.add(category.getName());
				td.add(a);
				td.add(new Tag("/br"));
			}
			td.add(new Tag("/br"));

			//общая информация
			Map<String, String> contents = new HashMap<String, String>();
			contents.put("celebrity", "Знаменитости");
			contents.put("signs", "Выраженные Знаки Зодиака");
			contents.put("dsigns", "Кредо вашей жизни");
			if (!child)
				contents.put("degree", "Символ рождения");

			b = new Tag("h5");
			b.add("Общая информация");
			td.add(b);
			for (Map.Entry<String, String> entry : contents.entrySet()) {
				Tag a = new Tag("a", "href=#" + entry.getKey());
				a.add(entry.getValue());
				td.add(a);
				td.add(new Tag("/br"));
			}
			td.add(new Tag("/br"));
			tr.add(td);
			subtable.add(tr);
			cell.add(subtable);

//---------------------------------------------------------------------------

			//правая часть содержания
			subtable = new Tag("table", "class=menu align=right");
			tr = new Tag("tr");
			td = new Tag("td");

			//описание космограммы
			contents = new HashMap<String, String>();
			contents.put("cosmogram", "Космограмма");
			contents.put("cardtype", "Самораскрытие");
			contents.put("cardkind", "Кармический потенциал");
			contents.put("elements", "Темперамент");
			contents.put("yinyang", "Мужское и женское начало");
			contents.put("halfspheres", "Экстраверсия");
			contents.put("squares", "Зрелость");
			contents.put("crosses", "Стратегия");
			contents.put("zones", "Развитие духа");
			contents.put("planets", "Планеты");
			contents.put("aspects", "Аспекты планет");
			contents.put("configurations", "Конфигурации аспектов планет");
			
			b = new Tag("h5");
			b.add("Космограмма");
			td.add(b);
			for (Map.Entry<String, String> entry : contents.entrySet()) {
				Tag a = new Tag("a", "href=#" + entry.getKey());
				a.add(entry.getValue());
				td.add(a);
				td.add(new Tag("/br"));
			}
			td.add(new Tag("/br"));

			b = new Tag("h5");
			b.add("Реализация личности");
			td.add(b);
			Tag a = new Tag("a", "href=#houses");
			a.add("Астрологические дома");
			td.add(a);
			td.add(new Tag("/br"));

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
					a = new Tag("a", "href=#" + house.getLinkName());
					a.add(house.getHeaderName());
					td.add(a);
					td.add(new Tag("/br"));
				}
			}
			td.add(new Tag("/br"));
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
				Tag td = new Tag("td", "class=header id=celebrity");
				td.add("Однодневки");
				tr.add(td);
				cell.add(tr);
	
				tr = new Tag("tr");
				td = new Tag("td");
				td.add(util.getNormalTaggedString("В один день с вами родились такие известные люди:"));
				Tag p = new Tag("p");
				
				for (Model model : list) {
					Event event = (Event)model;
					p.add(util.getSmallTaggedString(DateUtil.formatDate(event.getBirth())));
					Tag a = new Tag("a", "target=_blank href=" + event.getUrl());
					a.add(event.getName());
					p.add(a);
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
			Tag td = new Tag("td", "class=header id=signs");
			td.add("Выраженные Знаки Зодиака");
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
			td = new Tag("td", "class=align-center");
			Tag chart = util.getCss3Chart(bars, null);
			td.add(chart);
			tr.add(td);
			cell.add(tr);
	
			//кредо
			tr = new Tag("tr");
			td = new Tag("td", "class=header id=credo");
			td.add("Кредо Вашей жизни");
			tr.add(td);
			cell.add(tr);
	
			tr = new Tag("tr");
			td = new Tag("td");
			chart = util.getTaggedChart(17, bars2, null);
			td.add(chart);
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
			Tag td = new Tag("td", "class=header id=houses");
			td.add("Астрологические дома");
			tr.add(td);
			cell.add(tr);
	
			tr = new Tag("tr");
			td = new Tag("td");
			Tag p = new Tag("p");
			p.add("Астрологические дома отражают ваши врождённые возможности, багаж, с которым вы пришли в этот мир. "
					+ "Пригодится он вам или нет - покажет время. "
					+ "В любом случае, это отправная точка корабля событий, который поможет вам переплыть море жизни и реализовать свою миссию.");
			td.add(p);
			
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
			Tag chart = util.getTaggedChart(17, bars, null);
			td.add(chart);
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
					Tag td = new Tag("td", "class=header id=degree");
					td.add("Символ рождения");
					tr.add(td);
					cell.add(tr);
					
					tr = new Tag("tr");
					td = new Tag("td");
					Tag tag = util.getBoldTaggedString(degree.getId() + "&#176; " + degree.getCode());
					td.add(tag);
					
					tag = new Tag("p", "class=desc");
					if (degree.getDescription() != null)
						tag.add(degree.getDescription());
					td.add(tag);

					if (degree.getText() != null) {
						td.add(degree.getText());
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
				PlanetSignService service = new PlanetSignService();
				for (Model model : event.getConfiguration().getPlanets()) {
					Planet planet = (Planet)model;
				    if (planet.isMain()) {
				    	List<PlanetSignText> list = service.find(planet, planet.getSign());
				    	if (list != null && list.size() > 0)
				    		for (PlanetSignText object : list) {
				    			Category category = object.getCategory();
								Tag tr = util.getTaggedHeader(
										category.getName(),	//описание
										category.getCode()); //ссылка
								cell.add(tr);
	
								tr = new Tag("tr");
								Tag td = new Tag("td");
								td.add(object.getText());
								
								List<TextGender> genders = object.getGenderTexts(event.isFemale(), child);
								for (TextGender gender : genders)
									printGenderText(gender, event, td);
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
			//космограмма
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header id=cosmogram");
			td.add("Космограмма");
			tr.add(td);
			cell.add(tr);
	
			tr = new Tag("tr");
			td = new Tag("td");
			Tag p = new Tag("p", "class=shot");
			Tag object = new Tag("img",
				"src=horoscope_files/card.png " +
				"id=cosmogram " +
				"align=center");
			p.add(object);
			td.add(p);

			p = new Tag("p");
			p.add("Карта рождения (натальная карта) - это уникальный отпечаток положения планет на небе в момент вашего рождения. Планеты расположены так, как если бы вы смотрели на них с Земли:");
			td.add(p);
			Tag ul = new Tag("ul");
			p = new Tag("li");
			p.add("ближе к точке ASC расположены планеты, восходящие над горизонтом");
			ul.add(p);
			p = new Tag("li");
			p.add("ближе к MC - планеты в зените");
			ul.add(p);
			p = new Tag("li");
			p.add("ближе к DSC - планеты, заходящие за горизонт");
			ul.add(p);
			p = new Tag("li");
			p.add("ближе к IC - планеты в надире");
			ul.add(p);
			td.add(ul);

			p = new Tag("p");
			p.add("Планеты в знаках Зодиака и астрологических домах:");
			td.add(p);
			p = new Tag("table", "class=legend-list");
			int i = -1;
			for (Model model : event.getConfiguration().getPlanets()) {
				String trstyle = (++i % 2 > 0) ? "odd" : "";
				Planet planet = (Planet)model;
				Tag tr2 = new Tag("tr", "class=" + trstyle);
				Tag td2 = new Tag("td");
				Tag img = new Tag("img", "src=horoscope_files/planet/" + planet.getCode() + ".png");
				td2.add(img);
				td2.add(planet.getName());
				tr2.add(td2);

				td2 = new Tag("td");
				td2.add(planet.getDescription());
				tr2.add(td2);
				
				td2 = new Tag("td");
				td2.add(CalcUtil.roundTo(planet.getCoord(), 2) + "&#176;");
				tr2.add(td2);
				
				Sign sign = planet.getSign();
				Color color = sign.getColor();
				String cattr = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");";
				td2 = new Tag("td");
				img = new Tag("b", "style=color:" + cattr);
				img.add(sign.getSymbol());
				td2.add(img);
				td2.add(sign.getName());
				tr2.add(td2);

				House house = planet.getHouse();
				color = house.getColor();
				cattr = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");";
				td2 = new Tag("td");
				img = new Tag("span", "style=color:" + cattr);
				img.add(house.getDesignation());
				td2.add(img);
				tr2.add(td2);
				td2 = new Tag("td");
				td2.add(house.getCombination());
				tr2.add(td2);
				p.add(tr2);
			}
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
	private void generateCardKind(Event event, Tag cell) {
		try {
			Tag tr = new Tag("tr");
			Tag td = new Tag("td", "class=header id=cardkind");
			td.add("Кармический потенциал");
			tr.add(td);
			cell.add(tr);

			tr = new Tag("tr");
			td = new Tag("td");
			Tag p = new Tag("p");
			p.add("Вид космограммы &mdash; это вид сверху на рисунок карты рождения. Здесь важна общая картина, которая не в деталях, а глобально описывает ваше предназначение и кармический опыт прошлого. "
				+ "Определите, на каком уровне вы находитесь сейчас. Отследите по трём уровням своё развитие.");
			td.add(p);

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
//				Tag p = new Tag("h5"); 
//				p.add(type.getName());
//				td.add(p);
//				p = new Tag("p", "class=desc"); 
//				p.add(type.getDescription());
//				td.add(p);
//				p = new Tag("p"); 
//				p.add(type.getText());
//				td.add(p);
//			}
			tr.add(td);
			cell.add(tr);
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
						Tag td = new Tag("td", "class=header id=cardtype");
						td.add("Самораскрытие человека");
						tr.add(td);
						cell.add(tr);
	
						tr = new Tag("tr");
						td = new Tag("td");
						Tag tag = util.getBoldTaggedString("Тип космограммы: " + cardType.getName());
						td.add(tag);
						tag = util.getItalicTaggedString(cardType.getDescription());
						td.add(tag);
						td.add(cardType.getText());
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
			Tag td = new Tag("td", "class=header id=elements");
			td.add("Темперамент в сознании");
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
				Tag tag = util.getBoldTaggedString("Выраженные стихии: " + element.getName());
				td.add(tag);
				td.add(element.getText());
				List<TextGender> genders = element.getGenderTexts(event.isFemale(), child);
				for (TextGender gender : genders)
					printGenderText(gender, event, td);

				Tag p = new Tag("p");
				p.add("Диаграмма показывает, на чём мысленно сконцентрирован человек, какие проявления для него важны, необходимы, естественны.");
				td.add(p);
				Tag chart = util.getCss3Chart(bars, element.getTemperament());
				td.add(chart);
				tr.add(td);
				cell.add(tr);
		    }

		    tr = new Tag("tr");
			td = new Tag("td", "class=header");
			td.add("Темперамент в поступках");
			tr.add(td);
			cell.add(tr);
			
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
			td = new Tag("td");
			Tag p = new Tag("p");
			p.add("Диаграмма показывает, как на событийном уровне, в социуме меняются приоритеты человека по сравнению с предыдущей идеальной моделью.");
			td.add(p);
			Tag chart = util.getCss3Chart(bars, null);
			td.add(chart);
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
	 * @param gender объект текста для разных полов
	 * @param event событие
	 * @param cell тег-контейнер для вложенных тегов
	 */
	private void printGenderText(TextGender gender, Event event, Tag cell) {
		if (gender != null) {
			Tag tag = util.getGenderHeader(gender.getType());
			cell.add(tag); //заголовок
			cell.add(gender.getText()); //текст
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
		Tag a = new Tag("a", "href=http://zvezdochet.guru/ target=_blank");
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
				Tag td = new Tag("td", "class=header id=similar");
				td.add("Близкие по духу");
				tr.add(td);
				cell.add(tr);
	
				tr = new Tag("tr");
				td = new Tag("td");
				Tag p = new Tag("p");
				p.add("Известные люди, похожие на Вас по характеру:");
				td.add(p);

				p = new Tag("p");
				for (Model model : list) {
					Event event = (Event)model;
					p.add(util.getSmallTaggedString(DateUtil.formatDate(event.getBirth())));
					Tag a = new Tag("a", "target=_blank href=" + event.getUrl());
					a.add(event.getName());
					p.add(a);
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
}
