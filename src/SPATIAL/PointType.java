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

import oracle.spatial.geometry.*;
import oracle.sql.STRUCT;

/**
 *
 * @author acalvoa
 */
public class PointType extends SpatialBase{
    //DEFINIMOS LAS PROPIEDADES
    List<Coordinate> coordenadas = new ArrayList<Coordinate>();  
    DecimalFormat df = new DecimalFormat("#.####");
    private final String tipo = "Punto";
    private final int SDOTYPE = JGeometry.GTYPE_POINT;
    private final int SDO_SRID = 4326;
    private final String SDO_POINT = "NULL";
    private final int SDO_ETYPE	 = 1;
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
    	JGeometry aux = JGeometry.createPoint(getSDO_ORDINATE_ARRAY(), 2, this.SDO_SRID);
    	STRUCT obj;
        try {
                obj = JGeometry.store(aux, con.getCon());
                return obj;
        } catch (SQLException e) {
                System.out.println("El objeto espacial no se pudo almacenar temporalmente: "+e.getMessage());

        }
        return null;
    }

    public PointType(List<Coordinate> ele) {
        this.coordenadas = ele;
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(simbolos);         
    }

    @Override
    public int size() {
        return 1;
    }
    @Override
    public JGeometry getJGeometry(){
        JGeometry aux = JGeometry.createPoint(this.getSDO_ORDINATE_ARRAY(), 2, this.SDO_SRID);
        return aux;
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
            aux[(i*2)] = c.getLongitude();
            aux[((i*2)+1)] = c.getLatitude();
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
    
}
