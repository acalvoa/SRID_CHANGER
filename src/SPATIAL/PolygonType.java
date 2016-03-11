/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SPATIAL;


import gea.adapters.OracleConnector;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.json.JSONArray;

/**
 *
 * @author acalvoa
 */
public class PolygonType extends SpatialBase{
    //DEFINIMOS LAS PROPIEDADES
    List<Coordinate> coordenadas = new ArrayList<Coordinate>(); 
    DecimalFormat df = new DecimalFormat("#.###"); //RANGO 2 DE PRECISION;
    private final String tipo = "Poligono";
    private final int SDOTYPE = JGeometry.GTYPE_POLYGON;
    private final int SDO_SRID = 4326;
    private final String SDO_POINT = "NULL";
    private final int SDO_ETYPE	 = 1003;
    private int SDO_STARTING_OFFSET = 1;
    private final int SDO_INTERPRETATION = 1;
    //DEFINIMOS LOS METODOS
    @Override
    public String getType() {
        return tipo;
    }

    @Override
    public List<Coordinate> getCoords() {
       return coordenadas;
    }

    @Override
    public STRUCT toObject(OracleConnector con){
    	JGeometry aux = JGeometry.createLinearPolygon(getSDO_ORDINATE_ARRAY(), 2, this.SDO_SRID);
    	aux.simplify(0.001);
    	STRUCT obj;
		try {
			obj = JGeometry.store(aux, con.getCon());
			return obj;
		} catch (SQLException e) {
			System.out.println("El objeto espacial no se pudo almacenar temporalmente: "+e.getMessage());
		}
        return null;
    }
    @Override
    public JGeometry getJGeometry(){
    	JGeometry aux = JGeometry.createPoint(getSDO_ORDINATE_ARRAY(), 2, this.SDO_SRID);
    	return aux;
    }
    public PolygonType(List<Coordinate> coords) {
           this.coordenadas = coords;
           DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
           simbolos.setDecimalSeparator('.');
           df.setDecimalFormatSymbols(simbolos);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void SdoOffset(int i) {
        this.SDO_STARTING_OFFSET = i;
    }

    @Override
    public int[] getSDO_ELEM_INFO() {
    	int[] aux = new int[3];
    	aux[0] = SDO_STARTING_OFFSET;
    	aux[1] = SDO_ETYPE;
    	aux[2] = SDO_INTERPRETATION;
    	return aux;
    }
    @Override
    public double[] getSDO_ORDINATE_ARRAY() {
        double[] aux = new double[coordenadas.size()*2];
        for(int i=0; i< coordenadas.size(); i++){
            Coordinate c = coordenadas.get(i);
            aux[(i*2)] = Double.valueOf(ROUND_COORD(c.getLongitude()));
            aux[((i*2)+1)] = Double.valueOf(ROUND_COORD(c.getLatitude()));
        }
        return aux;
    }

    @Override
    public int getNUM_ORDINATES_ARRAY() {
        return coordenadas.size();
    }

    @Override
    public String ROUND_COORD(double coord) {
        return df.format(coord);
    }

    @Override
    public void setPrecition(int precision) {
        String pres = "#.";
        for(int i =0; i< precision; i++){
            pres+= "#";
        }
        this.df= new DecimalFormat(pres);
    }
    public static JSONArray cluster_simplfication(JSONArray coords, double tolerance){
        DecimalFormat df = new DecimalFormat("0.00");
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(simbolos);
        double pivotx = coords.getDouble(0);
        double pivoty = coords.getDouble(1);
        JSONArray newcoords = new JSONArray();
        newcoords.put(new Float(df.format(pivotx)));
        newcoords.put(new Float(df.format(pivoty)));
        for(int i=2; i<coords.length(); i++){
                if(Math.sqrt(Math.pow((coords.getDouble(i)-pivotx), 2)+Math.pow((coords.getDouble(i+1)-pivoty), 2)) >= tolerance){
                        pivotx = coords.getDouble(i);
                        pivoty = coords.getDouble(i+1);
                        newcoords.put(new Float(df.format(pivotx)));
                        newcoords.put(new Float(df.format(pivoty)));
                }
                i++;
        }
        return newcoords;
    }
}
