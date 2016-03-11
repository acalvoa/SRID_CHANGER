/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package GEOCGR.KMLread;

import SPATIAL.LineType;
import SPATIAL.MultiGeometryType;
import SPATIAL.PointType;
import SPATIAL.PolygonType;
import SPATIAL.Spatial;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 *
 * @author acalvoa
 */
public class READER {
    private Feature List;
    private List<Feature> places;
    public READER() throws IOException, FileNotFoundException, SQLException, Exception {
        this.readKML();
    }
    public void readKML() throws FileNotFoundException, IOException, SQLException, Exception{
        gea.adapters.OracleConnector ora = new gea.adapters.OracleConnector();
        ora.connect();
        //ResultSet query = ora1.query("SELECT * FROM GEO_CENTROCOMUNA");
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println("ASEGURESE DE QUE EL ARCHIVO SE ENCUENTRE EN LA CARPETA DEL JAR");
        System.out.println("--------------------------------------------------------------");
        System.out.println("INGRESE NOMBRE DEL ARCHIVO DE SEDES CONSISTORIALES");
        Scanner s = new Scanner(System.in);
        String opcion = s.next();
        System.out.println("LEYENDO ARCHIVO "+opcion);
        File archivo = new File(opcion);
        Kml kml = Kml.unmarshal(archivo);
        Document document = (Document)kml.getFeature();
        List<Feature> li = document.getFeature();
        Folder fo = (Folder)li.get(0);
        String name = fo.getName();
        List<Feature> places = fo.getFeature();
        int enc = 0;
        int noenc = 0;
        //CREAMOS LOS ARCHIVOS DE LOG
        FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
            fichero = new FileWriter("LOG");
            pw = new PrintWriter(fichero);
            //INSTANCIAMOS LAS LLAMADAS
            if(places.size()>1){
                System.out.println("SE ENCONTRARON ELEMENTOS DE "+places.size()+" REGIONES");
                for(int l=0; l<places.size();l++){
                    Folder folder = (Folder)places.get(l);
                    String nameregion = folder.getName();
                    System.out.println("REGION "+nameregion);
                    List<Feature> placesregion = folder.getFeature();

                    for(int k=0; k< placesregion.size(); k++){
                        Placemark place = (Placemark) placesregion.get(k);
                        String nombre = place.getName().replaceAll("[á|Á]", "a");
                        nombre = nombre.replaceAll("[é|É]", "e");
                        nombre = nombre.replaceAll("[í|Í]", "i");
                        nombre = nombre.replaceAll("[ó|Ó]", "o");
                        nombre = nombre.replaceAll("[ú|Ú]", "u");
                        System.out.print("NOMBRE: "+nombre);
                        ResultSet GEO = ora.query("SELECT * FROM SPATIAL_DATA_COMUNAL WHERE COMUNA LIKE '"+nombre.toUpperCase()+"'");
                        boolean status = false;
                        while(GEO.next()){
                            status = true;
                            enc++;
                            System.out.print(" - Encontrado.\n\r");
                            if(place.getGeometry().getClass() == Point.class){
                                Point spa = (Point) place.getGeometry();
                                double[] coord = new double[2];
                                coord[0] = spa.getCoordinates().get(0).getLongitude();
                                coord[1] = spa.getCoordinates().get(0).getLatitude();
                                JGeometry new_geom = JGeometry.createPoint(coord,2,4326);
                                PreparedStatement ps = ora.getCon().prepareStatement("UPDATE SPATIAL_DATA_COMUNAL SET CENTROCONSISTORIAL=? WHERE COMUNA LIKE ?");
                                STRUCT obj = JGeometry.store(new_geom, ora.getCon());
                                ps.setObject(1, obj);
                                ps.setString(2, nombre.toUpperCase());
                                ps.execute();
                                ps.close();
                            }
                        }
                        if(!status){
                            noenc++;
                            pw.println(nombre.toUpperCase()+"");
                            System.out.print(" - No Encontrado.\n\r");
                        }
                        
                    }
                }
            }  
 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           // Nuevamente aprovechamos el finally para 
           // asegurarnos que se cierra el fichero.
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
        System.out.println("\n\r\n\r\n\r STADISTICS");
        System.out.println("-------------------------------");
        System.out.println("COMUNAS WITH MATCH AND CHANGE="+enc);
        System.out.println("COMUNAS WITH NOT MATCH AND CHANGE ="+noenc);
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        ora.close();
        System.in.read();
    }
}
