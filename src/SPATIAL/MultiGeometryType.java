/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SPATIAL;

import gea.adapters.OracleConnector;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import oracle.jdbc.OracleConnection;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 *
 * @author acalvoa
 */
public class MultiGeometryType extends SpatialBase{
    //DEFINIMOS LAS PROPIEDADES
    List<Spatial> elementos = new ArrayList<Spatial>();
    DecimalFormat df = new DecimalFormat("#.##");
    private final String tipo = "MultiGeometria";
    private final int SDOTYPE = JGeometry.GTYPE_COLLECTION;
    private final int SDO_SRID = 4326;
    private final String SDO_POINT = "NULL";
    private final int SDO_ETYPE	 = 1003;
    private int SDO_STARTING_OFFSET = 0;
    private final int SDO_INTERPRETATION = 1;
    //DEFINIMOS LOS METODOS
    @Override
    public String getType() {
        return tipo;
    }
    @Override
    public ArrayList<Coordinate> getCoords() {
        return null; //To change body of generated methods, choose Tools | Templates.
    }
    public List<Spatial> getElements() {
        return elementos;
    }
    @Override
    public STRUCT toObject(OracleConnector con){
    	JGeometry aux = new JGeometry(this.SDOTYPE,this.SDO_SRID,this.getSDO_ELEM_INFO(),this.getSDO_ORDINATE_ARRAY());
    	STRUCT obj;
        try {
                obj = JGeometry.store(aux, con.getCon());
                return obj;
        } catch (SQLException e) {
                System.out.println("El objeto espacial no se pudo almacenar temporalmente: "+e.getMessage());
        }
        return null;
    }

    public MultiGeometryType(List<Spatial> elements) {
        this.elementos = elements;
    }
    public MultiGeometryType() {
        
    }
    public void addElement(Spatial ele){
        elementos.add(ele);
    }

    @Override
    public int size() {
        return elementos.size();
    }

    @Override
    public void SdoOffset(int i) {
        this.SDO_STARTING_OFFSET = i;
    }
    @Override
    public JGeometry getJGeometry(){
    	JGeometry aux = null;
    	return aux;
    }
    @Override
    public int[] getSDO_ELEM_INFO() {
        int[] sdo_info = new int[(elementos.size()*3)];
    	int offset = 1;
        for(int i=0; i<elementos.size(); i++){
            Spatial s = elementos.get(i);
            s.SdoOffset(offset);
            int[] info = s.getSDO_ELEM_INFO();
            sdo_info[(i*3)] = offset;
            sdo_info[((i*3)+1)] = info[1];
            sdo_info[((i*3)+2)] = info[2];
            offset = offset + (s.size()*2);
        }
        return sdo_info;
    }

    @Override
    public double[] getSDO_ORDINATE_ARRAY() {

        ArrayList<Double> l = new ArrayList<Double>();
        for(int i=0; i<elementos.size(); i++){
            Spatial s = elementos.get(i);
            double[] cord = s.getSDO_ORDINATE_ARRAY();
            for(int j=0; j<cord.length; j++){
            	l.add(cord[j]);
            }
        }
        Double[] aux = l.toArray(new Double[l.size()]);
        return ArrayUtils.toPrimitive(aux);
    }

    @Override
    public int getNUM_ORDINATES_ARRAY() {
        int coord = 0;
        for(int i=0; i<elementos.size(); i++){
            Spatial s = elementos.get(i);
            coord += s.getNUM_ORDINATES_ARRAY()*2;
        }
        return coord;
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
