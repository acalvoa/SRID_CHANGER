package gea.types;

import gea.utils.geoutils.ConversorCoordenadas;
import gea.utils.geoutils.GeoAlgorithm;

import org.json.JSONArray;
import org.json.JSONML;
import org.json.JSONObject;

public class OBRA extends Type{
	String gml;
	JSONObject coord = null;
	public OBRA(String GML){
		super();
		this.gml = GML;
	}
	public void GMLCONVERT(String GML){
		
		this.extract_polygon(JSONML.toJSONObject(GML));
	}
	public void GMLCONVERT(){
		this.extract_polygon(JSONML.toJSONObject(this.gml));
	}
	public void extract_polygon(JSONObject poly){
		coord = new JSONObject();
		if(poly.getString("tagName").equals("gml:LineString")){
			coord.put("TYPE", "POLYLINE");
			JSONArray coor = new JSONArray();
			JSONObject temp = poly;
			String[] cor = temp.getJSONArray("childNodes").getJSONObject(0).getJSONArray("childNodes").getString(0).split(" ");
			for(int k=0; k< cor.length; k++){
				String[] cord = cor[k].split(",");
				coor.put(cord[0]);
				coor.put(cord[1]);
			}
			coord.put("COORDINATES", coor);
		}
		else if(poly.getString("tagName").equals("gml:Point")){
			coord.put("TYPE", "POINT");
			JSONArray coor = new JSONArray();
			JSONObject temp = poly;
			String[] cor = temp.getJSONArray("childNodes").getJSONObject(0).getJSONArray("childNodes").getString(0).split(",");
			coor.put(cor[0]);
			coor.put(cor[1]);
			coord.put("COORDINATES", coor);
		}
		else if(poly.getString("tagName").equals("gml:MultiGeometry")){
			coord.put("TYPE", "MULTYGEOMETRY");
			JSONArray l = new JSONArray();
			JSONObject temp = poly;
			JSONArray geos = temp.getJSONArray("childNodes");
			for(int h=0; h<geos.length(); h++){
				JSONObject geo = geos.getJSONObject(h);
				String type = geo.getJSONArray("childNodes").getJSONObject(0).getString("tagName");
				if(type.equals("gml:Point")){
					JSONObject element = new JSONObject();
					JSONArray cor = new JSONArray();
					element.put("TYPE", "POINT");
					String[] cord = geo.getJSONArray("childNodes").getJSONObject(0).getJSONArray("childNodes").getJSONObject(0).getJSONArray("childNodes").getString(0).split(",");
					cor.put(cord[0]);
					cor.put(cord[1]);
					element.put("COORDINATES", cor);
					l.put(element);
				}
				else if(type.equals("gml:LineString")){
					JSONObject element = new JSONObject();
					JSONArray cor = new JSONArray();
					element.put("TYPE", "POLYLINE");
					String[] coordenadas = geo.getJSONArray("childNodes").getJSONObject(0).getJSONArray("childNodes").getJSONObject(0).getJSONArray("childNodes").getString(0).split(" ");
					for(int d=0; d<coordenadas.length; d++){
						String[] coo = coordenadas[d].split(",");
						cor.put(coo[0]);
						cor.put(coo[1]);
					}
					element.put("COORDINATES", cor);
					l.put(element);
				}
			}
			coord.put("ELEMENTS", l);
		}
	}
	public JSONObject getCoord() {
		return coord;
	}
	@Override
	public String toString(){
		if(this.gml != null){
			this.GMLCONVERT();
			return coord.toString();
		}
		else{
			coord = new JSONObject();
			coord.put("TYPE", "NOTREFERENCE");
			return coord.toString();
		}
			
		
	}
	public static String GetSQL(String field) {
		// TODO Auto-generated method stub
		return "SDO_UTIL.TO_GMLGEOMETRY("+field+") AS "+field+",";
	}

}
