/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SPATIAL;


import gea.adapters.OracleConnector;

import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 *
 * @author acalvoa
 */
public interface Spatial {
    public String getType();
    public List<Coordinate> getCoords();
    public STRUCT toObject(OracleConnector con);
    public void SdoOffset(int i);
    public int size();
    public JGeometry getJGeometry();
    public int[] getSDO_ELEM_INFO();
    public double[] getSDO_ORDINATE_ARRAY();
    public int getNUM_ORDINATES_ARRAY();
    public String ROUND_COORD(double coord);
    public void setPrecition(int precision);
}
