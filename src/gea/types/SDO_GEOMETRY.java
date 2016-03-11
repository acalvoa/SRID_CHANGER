package gea.types;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template*/

import java.math.BigDecimal;
import java.sql.SQLException;

import org.json.JSONArray;

import oracle.sql.ARRAY;
import oracle.sql.STRUCT;

/**
 *
 * @author acalvoa
 */
public class SDO_GEOMETRY {
    // ELEMENTOS GEO
    BigDecimal SDO_GTYPE;
    BigDecimal SDO_SRID;
    STRUCT SDO_POINT_TYPE;
    ARRAY SDO_ELEM_INFO;
    ARRAY SDO_ORDINATES;
    // IDENTIFICADORES
    String TYPE;
    
    public SDO_GEOMETRY(STRUCT geo) throws SQLException{
        Object[] at = geo.getAttributes();
        this.SDO_GTYPE = (at[0] != null)?(BigDecimal)at[0]: null;
        this.SDO_SRID = (at[1] != null)?(BigDecimal)at[1]: null;
        this.SDO_POINT_TYPE = (at[2] != null)?(STRUCT)at[2]: null;
        this.SDO_ELEM_INFO = (at[3] != null)?(ARRAY)at[3]: null;
        this.SDO_ORDINATES = (at[4] != null)?(ARRAY)at[4]: null;
        this.identify();
    }
    private void identify(){
        int type = (this.SDO_GTYPE.intValue()%1000)%100;
        if(type == 0) this.TYPE = "UNKNOWN";
        else if(type == 1) this.TYPE = "POINT";
        else if(type == 2) this.TYPE = "LINE";
        else if(type == 3) this.TYPE = "POLYGON";
        else if(type == 4) this.TYPE = "MULTIGEOMETRY";
        else if(type == 5) this.TYPE = "MULTIPOINT";
        else if(type == 6) this.TYPE = "MULTILINE";
        else if(type == 7) this.TYPE = "MULTIPOLYGON";
    }
    public String getTYPE(){
        return this.TYPE;
    }
    public String getSDO_OBJECT() throws SQLException{
        String SDO_SRID_T = (this.SDO_SRID != null)?String.valueOf(this.SDO_SRID.intValue()): null;
        String SDO_POINT_TYPE_T;
        if(this.SDO_POINT_TYPE != null){
            Object[] m = SDO_POINT_TYPE.getAttributes();
            String x = (m[0]!=null)?String.valueOf(((BigDecimal)m[0]).doubleValue()):null;
            String y = (m[1]!=null)?String.valueOf(((BigDecimal)m[1]).doubleValue()):null;
            String z = (m[2]!=null)?String.valueOf(((BigDecimal)m[2]).doubleValue()):null;
            SDO_POINT_TYPE_T = "MDSYS.SDO_POINT_TYPE("+x+","+y+","+z+")";
        }else{
            SDO_POINT_TYPE_T = "NULL";
        }
        String SDO_ELEM_INFO_T = (SDO_ELEM_INFO!=null)?"MDSYS.SDO_ELEM_INFO_ARRAY("+this.SDO_ELEM_INFO.stringValue().replace("[", "").replace("]", "")+")":null;
        String SDO_ORDINATES_T = (SDO_ORDINATES!=null)?"MDSYS.SDO_ORDINATE_ARRAY("+this.SDO_ORDINATES.stringValue().replace("[", "").replace("]", "")+")":null;
        return "MDSYS.SDO_GEOMETRY("+this.SDO_GTYPE.intValue()+","+SDO_SRID_T+","+SDO_POINT_TYPE_T+", "+SDO_ELEM_INFO_T+", "+SDO_ORDINATES_T+")";
    }
	public BigDecimal getSDO_GTYPE() {
		return SDO_GTYPE;
	}
	public void setSDO_GTYPE(BigDecimal sDO_GTYPE) {
		SDO_GTYPE = sDO_GTYPE;
	}
	public BigDecimal getSDO_SRID() {
		return SDO_SRID;
	}
	public void setSDO_SRID(BigDecimal sDO_SRID) {
		SDO_SRID = sDO_SRID;
	}
	public STRUCT getSDO_POINT_TYPE() {
		return SDO_POINT_TYPE;
	}
	public void setSDO_POINT_TYPE(STRUCT sDO_POINT_TYPE) {
		SDO_POINT_TYPE = sDO_POINT_TYPE;
	}
	public ARRAY getSDO_ELEM_INFO() {
		return SDO_ELEM_INFO;
	}
	public void setSDO_ELEM_INFO(ARRAY sDO_ELEM_INFO) {
		SDO_ELEM_INFO = sDO_ELEM_INFO;
	}
	public void setSDO_ORDINATES(ARRAY sDO_ORDINATES) {
		SDO_ORDINATES = sDO_ORDINATES;
	}
	public void setTYPE(String tYPE) {
		TYPE = tYPE;
	}
	public JSONArray getCoordinate() throws SQLException{
		JSONArray polys = new JSONArray();
		for(int i=0; i< polys.length(); i= i+3){
			JSONArray ele = new JSONArray();
			/*if((i+3)<def.length){
				System.out.println("multiple");
				/*for(int l=Integer.parseInt(def[i]);l<Integer.parseInt(def[i+3]); l++){
					ele.put(coord[l]);
				}*/
			/*}
			else
			{
				System.out.println("simple");
				/*for(int l=Integer.parseInt(def[i]);l<coord.length; l++){
					ele.put(coord[l]);
				}*/
			/*}*/
		/*	polys.put(ele);
		return polys;*/
		}
		return polys;
		
	}
    
}
