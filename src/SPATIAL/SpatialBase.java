package SPATIAL;

import gea.adapters.OracleConnector;

import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public abstract class SpatialBase implements Spatial{
	public abstract String getType();
    public abstract List<Coordinate> getCoords();
    public abstract STRUCT toObject(OracleConnector con);
    public abstract void SdoOffset(int i);
    public abstract int size();
    public abstract int[] getSDO_ELEM_INFO();
    public abstract JGeometry getJGeometry();
    public abstract double[] getSDO_ORDINATE_ARRAY();
    public abstract int getNUM_ORDINATES_ARRAY();
    public abstract String ROUND_COORD(double coord);
    public abstract void setPrecition(int precision);
}
