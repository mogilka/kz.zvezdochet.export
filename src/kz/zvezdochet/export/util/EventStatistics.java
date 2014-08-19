package kz.zvezdochet.export.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import kz.zvezdochet.analytics.util.AnalyticsUtil;
import kz.zvezdochet.bean.House;
import kz.zvezdochet.bean.Planet;
import kz.zvezdochet.bean.Sign;
import kz.zvezdochet.core.bean.Model;
import kz.zvezdochet.core.service.DataAccessException;
import kz.zvezdochet.core.util.CoreUtil;
import kz.zvezdochet.service.HouseService;
import kz.zvezdochet.service.SignService;
import kz.zvezdochet.util.AstroUtil;
import kz.zvezdochet.util.Configuration;

/**
 * Набор статистических данных события
 * @author Nataly Didenko
 *
 */
public class EventStatistics {
	private Configuration conf;
	private Map<String, Double> planetSigns;
	private Map<String, Double> planetHouses;
	private Map<String, Double> planetElements;
	private Map<String, Double> planetYinYangs;
	private Map<String, Double> planetHalfspheres;
	private Map<String, Double> planetSquares;
	private Map<String, Double> planetCrosses;
	private Map<String, Double> planetZones;
	private Map<String, Integer> signPlanets;

	private Map<String, Double> houseElements;
	private Map<String, Double> houseYinYangs;
	private Map<String, Double> houseHalfspheres;
	private Map<String, Double> houseSquares;
	private Map<String, Double> houseCrosses;
	private Map<String, Double> houseZones;
	
	public EventStatistics(Configuration conf) {
		this.conf = conf;
	}

	/**
	 * Вычисление выраженных знаков Зодиака
	 * @param main признак того, что нужно учитывать только индивидуальные планеты
	 * @return карта приоритетных знаков
	 * @throws DataAccessException 
	 */
	public Map<String, Double> getPlanetSigns(boolean main) throws DataAccessException {
		if (conf.getPlanets() != null) {
			conf.initPlanetSigns();
			planetSigns = new HashMap<String, Double>();
			signPlanets = new HashMap<String, Integer>();
			for (Model model : conf.getPlanets()) {
				Planet planet = (Planet)model;
				if (main && !planet.isMain()) continue;
				double value = 0.0;
				Object object = planetSigns.get(planet.getSign().getCode());
				if (object != null)
					value = (Double)object;
				value += planet.getScore();
				planetSigns.put(planet.getSign().getCode(), value);

				object = signPlanets.get(planet.getSign().getCode());
				value = object != null ? (Integer)object : 0;
				signPlanets.put(planet.getSign().getCode(), (int)++value);
			}
		}
		return planetSigns;
	}

	/**
	 * Вычисление выраженных домов Зодиака
	 * @throws DataAccessException 
	 */
	public void initPlanetHouses() throws DataAccessException {
		if (conf.getPlanets() != null) {
			planetHouses = new HashMap<String, Double>();
			for (Model model : conf.getPlanets()) {
				Planet planet = (Planet)model;
				for (int i = 0; i < conf.getHouses().size(); i++) {
					House house1 = (House)conf.getHouses().get(i);
					int j = (i == conf.getHouses().size() - 1) ? 0 : i + 1;
					House house2 = (House)conf.getHouses().get(j);
					if (AstroUtil.getSkyPointHouse(house1.getCoord(), house2.getCoord(), planet.getCoord())) { 
						planet.setHouse(house1);
						double value = 0.0;
						Object object = planetHouses.get(house1.getCode());
						if (object != null)
							value = (Double)object;
						value += planet.getScore();
						planetHouses.put(house1.getCode(), value);
					}
				}
			}
		}
	}

	/**
	 * Поиск астрологического дома конфигурации по коду
	 * @param code код дома
	 * @return астрологический дом конфигурации
	 * @throws DataAccessException 
	 */
	public House getHouse(String code) throws DataAccessException {
		for (Model model : conf.getHouses())
			if (((House)model).getCode().equals(code))
				return (House)model;
		return null;
	}

	/**
	 * Вычисление выраженных зон Зодиака
	 * @return карта приоритетных зон
	 * @throws DataAccessException 
	 */
	public void initPlanetDivisions() throws DataAccessException {
		if (planetSigns != null) {
			planetElements = new HashMap<String, Double>();
			planetYinYangs = new HashMap<String, Double>();
			planetHalfspheres = new HashMap<String, Double>();
			planetSquares = new HashMap<String, Double>();
			planetCrosses = new HashMap<String, Double>();
			planetZones = new HashMap<String, Double>();
			
			Iterator<Map.Entry<String, Double>> iterator = planetSigns.entrySet().iterator();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	Sign sign = (Sign)new SignService().find(entry.getKey());
				double value = 0.0;
				
				//выделенность стихий
		    	String division = AnalyticsUtil.signToElement(sign.getNumber());
				Object object = planetElements.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				planetElements.put(division, value);

				//выделенность инь-ян
				division = AnalyticsUtil.signToYinYang(sign.getNumber());
				object = planetYinYangs.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				planetYinYangs.put(division, value);

				//выделенность полусфер
				division = AnalyticsUtil.signToVerticalHalfSphere(sign.getNumber());
				object = planetHalfspheres.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				planetHalfspheres.put(division, value);

				division = AnalyticsUtil.signToHorizontalHalfSphere(sign.getNumber());
				object = planetHalfspheres.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				planetHalfspheres.put(division, value);

				//выделенность квадратов
				division = AnalyticsUtil.signToSquare(sign.getNumber());
				object = planetSquares.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				planetSquares.put(division, value);

				//выделенность крестов
				division = AnalyticsUtil.signToCross(sign.getNumber());
				object = planetCrosses.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				planetCrosses.put(division, value);

				//выделенность зон
				division = AnalyticsUtil.signToZone(sign.getNumber());
				object = planetZones.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				planetZones.put(division, value);
		    }
		}
	}

	public Map<String, Integer> getSignPlanets() {
		return signPlanets;
	}

	public Map<String, Double> getPlanetElements() {
		return planetElements;
	}

	public Map<String, Double> getPlanetSigns() {
		return planetSigns;
	}

	public Map<String, Double> getPlanetYinYangs() {
		return planetYinYangs;
	}

	public Map<String, Double> getPlanetHalfspheres() {
		return planetHalfspheres;
	}

	public Map<String, Double> getPlanetSquares() {
		return planetSquares;
	}

	public Map<String, Double> getPlanetCrosses() {
		return planetCrosses;
	}

	public Map<String, Double> getPlanetZones() {
		return planetZones;
	}

	public Map<String, Double> getPlanetHouses() {
		return planetHouses;
	}

	/**
	 * Вычисление выраженных зон домов
	 * @return карта приоритетных зон
	 * @throws DataAccessException 
	 */
	public void initHouseDivisions() throws DataAccessException {
		if (planetHouses != null) {
			houseElements = new HashMap<String, Double>();
			houseYinYangs = new HashMap<String, Double>();
			houseHalfspheres = new HashMap<String, Double>();
			houseSquares = new HashMap<String, Double>();
			houseCrosses = new HashMap<String, Double>();
			houseZones = new HashMap<String, Double>();
			
			Iterator<Map.Entry<String, Double>> iterator = planetHouses.entrySet().iterator();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
		    	House house = (House)new HouseService().find(entry.getKey());
				double value = 0.0;
				
				//выделенность стихий
		    	String division = AnalyticsUtil.houseToElement(house.getNumber());
				Object object = houseElements.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				houseElements.put(division, value);

				//выделенность инь-ян
				division = AnalyticsUtil.houseToYinYang(house.getNumber());
				object = houseYinYangs.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				houseYinYangs.put(division, value);

				//выделенность полусфер
				division = AnalyticsUtil.houseToVerticalHalfSphere(house.getNumber());
				object = houseHalfspheres.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				houseHalfspheres.put(division, value);

				division = AnalyticsUtil.houseToHorizontalHalfSphere(house.getNumber());
				object = houseHalfspheres.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				houseHalfspheres.put(division, value);

				//выделенность квадратов
				division = AnalyticsUtil.houseToSquare(house.getNumber());
				object = houseSquares.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				houseSquares.put(division, value);

				//выделенность крестов
				division = AnalyticsUtil.houseToCross(house.getNumber());
				object = houseCrosses.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				houseCrosses.put(division, value);

				//выделенность зон
				division = AnalyticsUtil.houseToZone(house.getNumber());
				object = houseZones.get(division);
				if (object != null)
					value = (Double)object;
				value += entry.getValue();
				houseZones.put(division, value);
		    }
		}
	}

	public Map<String, Double> getHouseElements() {
		return houseElements;
	}

	public Map<String, Double> getHouseYinYangs() {
		return houseYinYangs;
	}

	public Map<String, Double> getHouseHalfspheres() {
		return houseHalfspheres;
	}

	public Map<String, Double> getHouseSquares() {
		return houseSquares;
	}

	public Map<String, Double> getHouseCrosses() {
		return houseCrosses;
	}

	public Map<String, Double> getHouseZones() {
		return houseZones;
	}

	/**
	 * Вычисление выраженных основных домов Зодиака
	 * @return карта главных домов
	 * @throws DataAccessException 
	 */
	public Map<String, Double> getMainPlanetHouses() throws DataAccessException {
		Map<String, Double> houses = new HashMap<String, Double>();
		if (planetHouses != null) {
			Iterator<Map.Entry<String, Double>> iterator = planetHouses.entrySet().iterator();
			HouseService service = new HouseService();
		    while (iterator.hasNext()) {
		    	Entry<String, Double> entry = iterator.next();
				//по индексу трети определяем дом, в котором она находится
		    	House house = (House)service.find(entry.getKey());
				double value = entry.getValue();
				int index;
				if (CoreUtil.isArrayContainsNumber(new int[] {1,4,7,10,13,16,19,22,25,28,31,34}, house.getNumber()))
					index = house.getNumber();
				else
					index = (house.getNumber() % 3 == 0) ? house.getNumber() - 2 : house.getNumber() - 1;
		    	house = service.getHouse(index);

				Object object = houses.get(house.getCode());
				if (object != null)
					value += (Double)object;
				houses.put(house.getCode(), value);
			}
		}
		return houses;
	}

	/**
	 * Вычисление выраженных подкатегорий крестов знаков Зодиака
	 * @return карта знаков
	 * @throws DataAccessException 
	 */
	public Map<String, Double> getCrossSigns() throws DataAccessException {
		Map<String, Double> types = new HashMap<String, Double>();
		if (planetSigns != null) {
			double sum = 0.0;
			String[] signs = {"Aries", "Libra"};
			for (String sign : signs) {
				Double value = planetSigns.get(sign);
				if (value != null) sum += value; 
			}
			types.put("Активность в предложении", sum);
			
			sum = 0.0;
			signs = new String[] {"Cancer", "Capricornus"};
			for (String sign : signs) {
				Double value = planetSigns.get(sign);
				if (value != null) sum += value; 
			}
			types.put("Активность в реализации", sum);
			
			sum = 0.0;
			signs = new String[] {"Taurus", "Leo", "Scorpio"};
			for (String sign : signs) {
				Double value = planetSigns.get(sign);
				if (value != null) sum += value; 
			}
			types.put("Испытанные методы", sum);
			
			sum = 0.0;
			signs = new String[] {"Ophiuchus", "Aquarius"};
			for (String sign : signs) {
				Double value = planetSigns.get(sign);
				if (value != null) sum += value; 
			}
			types.put("Рискованные методы", sum);
			
			sum = 0.0;
			signs = new String[] {"Virgo", "Sagittarius"};
			for (String sign : signs) {
				Double value = planetSigns.get(sign);
				if (value != null) sum += value; 
			}
			types.put("Последовательность", sum);
			
			sum = 0.0;
			signs = new String[] {"Pisces", "Gemini"};
			for (String sign : signs) {
				Double value = planetSigns.get(sign);
				if (value != null) sum += value; 
			}
			types.put("Непоследовательность", sum);
		}
		return types;
	}

	/**
	 * Вычисление выраженных подкатегорий крестов астрологических домов
	 * @return карта домов
	 * @throws DataAccessException 
	 */
	public Map<String, Double> getCrossHouses() throws DataAccessException {
		Map<String, Double> types = new HashMap<String, Double>();
		if (planetHouses != null) {
			double sum = 0.0;
			String[] houses = {"I", "VII"};
			for (String house : houses) {
				Double value = planetHouses.get(house);
				if (value != null) sum += value; 
			}
			types.put("Активность в предложении", sum);
			
			sum = 0.0;
			houses = new String[] {"IV", "X"};
			for (String house : houses) {
				Double value = planetHouses.get(house);
				if (value != null) sum += value; 
			}
			types.put("Активность в реализации", sum);
			
			sum = 0.0;
			houses = new String[] {"II", "VIII"};
			for (String house : houses) {
				Double value = planetHouses.get(house);
				if (value != null) sum += value; 
			}
			types.put("Испытанные методы", sum);
			
			sum = 0.0;
			houses = new String[] {"V", "XI"};
			for (String house : houses) {
				Double value = planetHouses.get(house);
				if (value != null) sum += value; 
			}
			types.put("Рискованные методы", sum);
			
			sum = 0.0;
			houses = new String[] {"VI", "IX"};
			for (String house : houses) {
				Double value = planetHouses.get(house);
				if (value != null) sum += value; 
			}
			types.put("Последовательность", sum);
			
			sum = 0.0;
			houses = new String[] {"XII", "III"};
			for (String house : houses) {
				Double value = planetHouses.get(house);
				if (value != null) sum += value; 
			}
			types.put("Непоследовательность", sum);
		}
		return types;
	}
}
