/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SRID_CHANGER;

import GEOCGR.KMLread.READER;
import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import gea.adapters.OracleConnector;
import gea.properties.PropertyManagerException;
import gea.types.OBRA;
import gea.types.PRESITIONGEOM;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 *
 * @author acalvoa
 */
public class SRID_CHANGER {
    public static void main(String args[]) throws  IOException, PropertyManagerException, SQLException, Exception {
        //VAMOS A CREAR LOS OBJETOS
        while(true){
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println("SRID CHANGER.\n\r");
            System.out.println("RECUERDE INCLUIR EL ARCHIVO DE CONFIGURACION configSRID.properties en la carpeta de properties.\n\r");
            System.out.println("SELECCIONE LA OPCION\n\r");
            System.out.println("1 - LEER SRID DE PROYECTOS Y ANALIZAR");
            System.out.println("2 - APLICAR CAMBIO SRID 4326 A PROYECTOS");
            System.out.println("3 - ANALIZAR GEOMETRIAS");
            System.out.println("4 - RECTIFICAR PUNTOS");
            System.out.println("5 - RECTIFICAR LINEAS");
            System.out.println("6 - ACTUALIZAR SEDES CONSISTORIALES DESDE DB");
            System.out.println("7 - CORREGIR SEDES CONSISTORIALES");
            System.out.println("8 - INTEGRAR SEDES CONSISTORIALES");
            System.out.println("9 - TEST REGIONAL");
            System.out.println("10 - OBTENER OBRAS DE UN SERVICIO");
            System.out.println("11 - OBTENER CONSOLIDADO DE UN SERVICIO");
            System.out.println("12 - LEER ARCHIVO EXCEL Y GENERAR KML");
            System.out.println("13 - OBTENER CARTOGRAFIA");
            System.out.println("14 - SALIR\n\r");
            Scanner s = new Scanner(System.in);
            String opcion = s.next();
            if(opcion.equals("1")){
                read();
            }
            else if(opcion.equals("2"))
            {
                change();
            }
            else if(opcion.equals("3"))
            {
                analizeGEO();
            }
            else if(opcion.equals("4"))
            {
                rectPoints();
            }
            else if(opcion.equals("5"))
            {
                rectLines();
            }
            else if(opcion.equals("6"))
            {
                AsignarSedesComunas();
            }
            else if(opcion.equals("7"))
            {
                sedesConsistoriales();
            }
            else if(opcion.equals("8"))
            {
                integrarSedesKML();
            }
            else if(opcion.equals("9"))
            {
                testRegional();
            }
            else if(opcion.equals("10"))
            {
                getByServicio();
            }
            else if(opcion.equals("11"))
            {
                consolidadoGetByServicio();
            }
            else if(opcion.equals("12"))
            {
                XLS xls = new XLS();
                xls.leer();
            }
            else if(opcion.equals("13"))
            {
                ObtenerCartografia();
            }
            else if(opcion.equals("14"))
            {
                break;
            }
            else
            {
                System.out.println("ELIJA UNA OPCION CORRECTA");
            }
        }
    }
    public static void change() throws IOException, PropertyManagerException, SQLException {
        OracleConnector ora = new OracleConnector();
        ora.connect();
        int SRIDOK = 0;
        int NULOS = 0;
        ResultSet GEO = ora.query("SELECT PROY_X_PROY,SPATIAL_OBJECT FROM SPATIAL_DATA");
        while(GEO.next()){
            System.out.println("READING PROY:"+GEO.getString(1));
            STRUCT st = (oracle.sql.STRUCT) GEO.getObject(2);
            if(st != null){
                JGeometry j_geom = JGeometry.load(st);
                j_geom.setSRID(4326);
                PreparedStatement ps = ora.getCon().prepareStatement("UPDATE SPATIAL_DATA SET SPATIAL_OBJECT=? WHERE PROY_X_PROY=?");
                STRUCT obj = JGeometry.store(j_geom, ora.getCon());
                ps.setObject(1, obj);
                ps.setInt(2, Integer.valueOf(GEO.getString(1)));
                ps.execute();
                System.out.println("CHANGE PROY:"+GEO.getString(1)+" READY");
                ps.close();
                SRIDOK++;
            }
            else
            {
                System.out.println("PROY:"+GEO.getString(1)+" GEOMETRY NULL");
                NULOS++;
            }
        }
        System.out.println("\n\r\n\r\n\r STADISTICS");
        System.out.println("-------------------------------");
        System.out.println("PROJECTS WITH SRID 4326 CHANGED ="+SRIDOK);
        System.out.println("PROYECTS WITH NULL SPATIAL OBJECT ="+NULOS);
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        System.in.read();
    }
    public static void read() throws IOException, PropertyManagerException, SQLException {
        OracleConnector ora = new OracleConnector();
        ora.connect();
        ResultSet GEO = ora.query("SELECT PROY_X_PROY,SPATIAL_OBJECT FROM SPATIAL_DATA");
        int SRIDOK = 0;
        int SRIDNE = 0;
        int NULOS = 0;
        while(GEO.next()){
            System.out.println("READING PROY:"+GEO.getString(1));
            STRUCT st = (oracle.sql.STRUCT) GEO.getObject(2);
            if(st != null){
                JGeometry j_geom = JGeometry.load(st);
                System.out.println("VERIFYING SRID PROY:"+GEO.getString(1));
                if(j_geom.getSRID() == 4326){
                   SRIDOK++;
                }
                else
                {
                    SRIDNE++;
                }
            }
            else
            {
                NULOS++;
                System.out.println("PROY:"+GEO.getString(1)+" GEOMETRY NULL");
            }
        }
        System.out.println("\n\r\n\r\n\r STADISTICS");
        System.out.println("-------------------------------");
        System.out.println("PROJECTS WITH SRID 4326 OK ="+SRIDOK);
        System.out.println("PROYECTS WITH SRID 4326 NOT OK ="+SRIDNE);
        System.out.println("PROYECTS WITH NULL SPATIAL OBJECT ="+NULOS);
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        System.in.read();
    }
    public static void analizeGEO() throws IOException, PropertyManagerException, SQLException {
        OracleConnector ora = new OracleConnector();
        ora.connect();
        ResultSet GEO = ora.query("SELECT PROY_X_PROY,SPATIAL_OBJECT FROM SPATIAL_DATA");
        int POINTS = 0;
        int LINES = 0;
        int MULTI = 0;
        int NULOS = 0;
        int NULLPROY = 0;
        while(GEO.next()){
            System.out.println("READING PROY:"+GEO.getString(1));
            STRUCT st = (oracle.sql.STRUCT) GEO.getObject(2);
            if(st != null){
                JGeometry j_geom = JGeometry.load(st);
                if(j_geom.getType() == JGeometry.GTYPE_POINT){
                    System.out.println("PROY:"+GEO.getString(1)+" IS A POINT.");
                    POINTS++;
                }
                else if(j_geom.getType() == JGeometry.GTYPE_CURVE){
                    System.out.println("PROY:"+GEO.getString(1)+" IS A LINE.");
                    LINES++;
                }
                else if(j_geom.getType() == JGeometry.GTYPE_COLLECTION){
                    System.out.println("PROY:"+GEO.getString(1)+" IS A MULTIGEOMETRY.");
                    MULTI++;
                }
            }
            else
            {
                NULOS++;
                NULLPROY = Integer.valueOf(GEO.getString(1));
                System.out.println("PROY:"+GEO.getString(1)+" GEOMETRY NULL");
            }
        }
        System.out.println("\n\r\n\r\n\r STADISTICS");
        System.out.println("-------------------------------");
        System.out.println("PROJECTS WITH POINT GEOMETRY ="+POINTS);
        System.out.println("PROYECTS WITH LINE GEOMETRY ="+LINES);
        System.out.println("PROYECTS WITH MULTIGEOMETRY ="+MULTI);
        System.out.println("PROYECTS WITH NULL SPATIAL OBJECT ="+NULOS);
        System.out.println("PROYECTS NULL ="+NULLPROY);
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        System.in.read();
    }
    public static void rectPoints() throws IOException, PropertyManagerException, SQLException {
        OracleConnector ora = new OracleConnector();
        ora.connect();
        ResultSet GEO = ora.query("SELECT PROY_X_PROY,SPATIAL_OBJECT FROM SPATIAL_DATA");
        int POINTS = 0;
        int NULOS = 0;
        while(GEO.next()){
            System.out.println("READING PROY:"+GEO.getString(1));
            STRUCT st = (oracle.sql.STRUCT) GEO.getObject(2);
            if(st != null){
                JGeometry j_geom = JGeometry.load(st);
                if(j_geom.getType() == JGeometry.GTYPE_POINT){
                    double[] coord = j_geom.getPoint();
                    JGeometry new_geom = JGeometry.createPoint(coord,2,4326);
                    PreparedStatement ps = ora.getCon().prepareStatement("UPDATE SPATIAL_DATA SET SPATIAL_OBJECT=? WHERE PROY_X_PROY=?");
                    STRUCT obj = JGeometry.store(new_geom, ora.getCon());
                    ps.setObject(1, obj);
                    ps.setInt(2, Integer.valueOf(GEO.getString(1)));
                    ps.execute();
                    System.out.println("PROY:"+GEO.getString(1)+" POINT RECTIFIED");
                    ps.close();
                    POINTS++;
                }
            }
            else
            {
                NULOS++;
                System.out.println("PROY:"+GEO.getString(1)+" GEOMETRY NULL");
            }
        }
        System.out.println("\n\r\n\r\n\r STADISTICS");
        System.out.println("-------------------------------");
        System.out.println("PROJECTS WITH POINT GEOMETRY RECTIFIED="+POINTS);
        System.out.println("PROYECTS WITH NULL SPATIAL OBJECT ="+NULOS);
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        System.in.read();
    }
    public static void rectLines() throws IOException, PropertyManagerException, SQLException {
        OracleConnector ora = new OracleConnector();
        ora.connect();
        ResultSet GEO = ora.query("SELECT PROY_X_PROY,SPATIAL_OBJECT FROM SPATIAL_DATA");
        int LINES = 0;
        int NULOS = 0;
        while(GEO.next()){
            System.out.println("READING PROY:"+GEO.getString(1));
            STRUCT st = (oracle.sql.STRUCT) GEO.getObject(2);
            if(st != null){
                JGeometry j_geom = JGeometry.load(st);
                if(j_geom.getType() == JGeometry.GTYPE_CURVE){
                    double[] coord = j_geom.getOrdinatesArray();
                    JGeometry new_geom = JGeometry.createLinearLineString(coord,2,4326);
                    PreparedStatement ps = ora.getCon().prepareStatement("UPDATE SPATIAL_DATA SET SPATIAL_OBJECT=? WHERE PROY_X_PROY=?");
                    STRUCT obj = JGeometry.store(new_geom, ora.getCon());
                    ps.setObject(1, obj);
                    ps.setInt(2, Integer.valueOf(GEO.getString(1)));
                    ps.execute();
                    System.out.println("PROY:"+GEO.getString(1)+" LINE RECTIFIED");
                    ps.close();
                    LINES++;
                }
            }
            else
            {
                NULOS++;
                System.out.println("PROY:"+GEO.getString(1)+" GEOMETRY NULL");
            }
        }
        System.out.println("\n\r\n\r\n\r STADISTICS");
        System.out.println("-------------------------------");
        System.out.println("PROJECTS WITH LINES GEOMETRY RECTIFIED="+LINES);
        System.out.println("PROYECTS WITH NULL SPATIAL OBJECT ="+NULOS);
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        System.in.read();
    }

    private static void sedesConsistoriales() throws IOException, PropertyManagerException, SQLException {
        OracleConnector ora = new OracleConnector();
        OracleConnector ora2 = new OracleConnector();
        OracleConnector ora3 = new OracleConnector();
        ora.connect();
        ora2.connect();
        ora3.connect();
        ResultSet GEO = ora.query("SELECT COMUNA,CINE_COM,CENTROIDE,CENTROCONSISTORIAL FROM SPATIAL_DATA_COMUNAL");
        int comuna = 0;
        int sedes = 0;
        int nosedes = 0;
        while(GEO.next()){
            comuna++;
            System.out.println("FIX COMUNA:"+GEO.getString(1));
            STRUCT st = (oracle.sql.STRUCT) GEO.getObject(3);
            DecimalFormat df = new DecimalFormat("#.####");
            if(st != null){
                JGeometry j_geom = JGeometry.load(st);
                if(j_geom.getType() == JGeometry.GTYPE_POINT){
                    double[] coord = j_geom.getPoint();
                    double coord_x_comuna = Double.valueOf(df.format(coord[0]).replace(",","."));
                    double coord_y_comuna = Double.valueOf(df.format(coord[1]).replace(",","."));
                    //OBRAS ADJUDICADAS
                    ResultSet GEO2 = ora2.query("SELECT SPA.PROY_X_PROY,SPATIAL_OBJECT FROM SPATIAL_DATA SPA \n" +
                    "INNER JOIN GEO_UBICACIONES_PROYECTO UBI ON\n" +
                    "SPA.PROY_X_PROY = UBI.PROY_X_PROY\n" +
                    "INNER JOIN GEO_COMUNAS COM ON\n" +
                    "COM.X_COMU = UBI.COMU_X_COMU\n" +
                    "WHERE COM.C_COMUNA_SUBDERE LIKE '"+GEO.getString(2)+"'\n" + 
                    "AND SPA.SPATIAL_TOOL IS NULL\n" +
                    "AND SPA.KML IS NULL");
                    while(GEO2.next()){
                        System.out.print("Obra COD: "+GEO2.getString(1));
                        STRUCT st2 = (oracle.sql.STRUCT) GEO2.getObject(2);
                        if(st2 != null){
                            JGeometry j_geom2 = JGeometry.load(st2);
                            if(j_geom2.getType() == JGeometry.GTYPE_POINT){
                                double[] coord_obra = j_geom2.getPoint();
                                double coord_x_proyecto = Double.valueOf(df.format(coord_obra[0]).replace(",","."));
                                double coord_y_proyecto = Double.valueOf(df.format(coord_obra[1]).replace(",","."));
                                STRUCT sede = (oracle.sql.STRUCT) GEO.getObject(4);
                                if(sede != null){
                                    JGeometry sede_geom = JGeometry.load(sede);
                                    double[] coord_sede = sede_geom.getPoint();
                                    double coord_x_sede = Double.valueOf(df.format(coord_sede[0]).replace(",","."));
                                    double coord_y_sede = Double.valueOf(df.format(coord_sede[1]).replace(",","."));
                                    if(coord_x_proyecto == coord_x_comuna && coord_y_proyecto == coord_y_comuna){
                                        System.out.print(" - SEDE CONSITORIAL ENCONTRADA");
                                        PreparedStatement ps = ora3.getCon().prepareStatement("UPDATE SPATIAL_DATA SET SPATIAL_OBJECT=?, SPATIAL_TOOL=?, KML=? WHERE PROY_X_PROY=?");
                                        STRUCT obj = JGeometry.store(sede_geom, ora3.getCon());
                                        ps.setObject(1, obj);
                                        ps.setString(2, "NOUBICATION");
                                        ps.setString(3, "No Existe");
                                        ps.setString(4, GEO2.getString(1));
                                        ps.execute();
                                        System.out.println(" - OBRA RECTIFICADA");
                                        ps.close();
                                        sedes++;
                                    }
                                    else if(coord_x_proyecto == coord_x_sede && coord_y_proyecto == coord_y_sede){
                                        System.out.print(" - SEDE CONSITORIAL ENCONTRADA");
                                        System.out.println(" - OBRA PRERECTIFICADA");
                                        sedes++;
                                    }
                                    else
                                    {
                                        PreparedStatement ps = ora3.getCon().prepareStatement("UPDATE SPATIAL_DATA SET SPATIAL_TOOL=?, KML=? WHERE PROY_X_PROY=?");
                                        ps.setString(1, "DRAW");
                                        ps.setString(2, "No Existe");
                                        ps.setString(3, GEO2.getString(1));
                                        ps.execute();
                                        System.out.println(" - OBRA RECTIFICADA");
                                        ps.close();
                                        nosedes++;
                                    }
                                }
                            }
                            else
                            {
                                PreparedStatement ps = ora3.getCon().prepareStatement("UPDATE SPATIAL_DATA SET SPATIAL_TOOL=?, KML=? WHERE PROY_X_PROY=?");
                                ps.setString(1, "DRAW");
                                ps.setString(2, "No Existe");
                                ps.setString(3, GEO2.getString(1));
                                ps.execute();
                                System.out.println(" - OBRA RECTIFICADA");
                                ps.close();
                                nosedes++;
                            }
                        }
                        System.out.print("\n\r");
                    }
                    
                }
            }
        }
        ora.close();
        ora2.close();
        ora3.close();
        System.out.println("\n\r\n\r\n\r STADISTICS");
        System.out.println("-------------------------------");
        System.out.println("COMUNAS FIXED ="+comuna);
        System.out.println("PROJECT WITH SEDES CONSISTORIALES FOUND ="+sedes);
        System.out.println("PROJECT WITHOUT SEDES CONSISTORIALES FOUND ="+nosedes);
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        System.in.read();
    }

    private static void AsignarSedesComunas() throws IOException, SQLException, PropertyManagerException {
        OracleConnector ora = new OracleConnector();
        ora.connect();
        ResultSet GEO = ora.query("SELECT C_COM_SUBDERE,GEOMSEDE FROM SPATIAL_SEDE_CONSISTORIAL");
        int LINES = 0;
        int NULOS = 0;
        while(GEO.next()){
            System.out.println("READING COMUNA:"+GEO.getString(1));
            STRUCT st = (oracle.sql.STRUCT) GEO.getObject(2);
            if(st != null){
                JGeometry j_geom = JGeometry.load(st);
                if(j_geom.getType() == JGeometry.GTYPE_POINT){
                    PreparedStatement ps = ora.getCon().prepareStatement("UPDATE SPATIAL_DATA_COMUNAL SET CENTROCONSISTORIAL=? WHERE CINE_COM=?");
                    STRUCT obj = JGeometry.store(j_geom, ora.getCon());
                    ps.setObject(1, obj);
                    ps.setString(2, GEO.getString(1));
                    ps.execute();
                    System.out.println("COMUNA:"+GEO.getString(1)+" - SEDE UPDATE");
                    ps.close();
                    LINES++;
                }
                 else
                {
                    NULOS++;
                    System.out.println("COMUNA:"+GEO.getString(1)+" GEOMETRY NULL");
                }
            }
        }
        System.out.println("\n\r\n\r\n\r STADISTICS");
        System.out.println("-------------------------------");
        System.out.println("PROJECTS WITH LINES GEOMETRY RECTIFIED="+LINES);
        System.out.println("PROYECTS WITH NULL SPATIAL OBJECT ="+NULOS);
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        ora.close();
        System.in.read();
    }

    private static void integrarSedesKML() throws SQLException, Exception {
        READER kr = new READER();
    }
    
    private static void testRegional() throws IOException, SQLException, PropertyManagerException {
        OracleConnector ora = new OracleConnector();
        ora.connect();
        ResultSet GEO = ora.query("SELECT GEOMREGIONAL FROM SPATIAL_DATA_REGIONAL WHERE C_REG = '12'");
        int LINES = 0;
        int NULOS = 0;
        while(GEO.next()){
            System.out.println("DESEMPAQUETANDO REGION");
            STRUCT st = (oracle.sql.STRUCT) GEO.getObject(1);
            if(st != null){
                JGeometry j_geom = JGeometry.load(st);
                System.out.println(j_geom.getSize());
            }
        }
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        ora.close();
        System.in.read();
    }
    private static void getByServicio() throws IOException, SQLException, PropertyManagerException {
        System.out.println("Indique el servicio que desea extraer.");   
        Scanner s = new Scanner(System.in);
        String servicio = s.next();
        OracleConnector ora = new OracleConnector();
        ora.connect();
        ResultSet GEO = ora.query("SELECT COUNT(*) AS CANTIDAD FROM OBRAS_CAST WHERE SERV_CONTR LIKE '%"+servicio+"%'");
        System.out.println("Obras encontradas");
        GEO.next();
        System.out.println(GEO.getInt(1));
        System.out.println("¿Desea volcar la información en archivos KML? (SI/NO)");
        String volcar = s.next();
        if(volcar.toUpperCase().equals("SI")){
            System.out.println("Introdusca el nombre de la carpeta");
            String carpeta = s.next();
            File directorio = new File(carpeta); 
            directorio.mkdir();
            GEO = ora.query("SELECT CODPROYECTO,SDO_UTIL.TO_GMLGEOMETRY(SPATIAL_OBJECT) AS SPATIAL_OBJECT FROM OBRAS_CAST WHERE SERV_CONTR LIKE '%"+servicio+"%'");
            while(GEO.next()){
                String ruta = carpeta+"/"+GEO.getInt(1)+".kml";
                File archivo = new File(ruta);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                try{
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    DOMImplementation implementation = builder.getDOMImplementation();
                    Document document = implementation.createDocument("http://www.opengis.net/kml/2.2", "kml", null);
                    Element raiz = document.createElement("Document");  // creamos el elemento raiz
                    Element elemento = document.createElement("Folder"); //creamos un nuevo elemento
                    elemento.setAttribute("name", String.valueOf(GEO.getInt(1)));
                    document.getDocumentElement().appendChild(raiz);  //pegamos la raiz al documento
                    raiz.appendChild(elemento); //pegamos el elemento hijo a la raiz
                    OBRA geom = new OBRA(GEO.getString(2));
                    geom.GMLCONVERT();
                    JSONObject obra = geom.getCoord();
                    Element placemark = document.createElement("Placemark");
                        if(obra.getString("TYPE").equals("POINT")){
                            Element punto = document.createElement("Point");
                            JSONArray coord = obra.getJSONArray("COORDINATES");
                            Element coordinates = document.createElement("coordinates");
                            coordinates.appendChild(document.createTextNode(coord.getString(0)+","+coord.getString(1)));
                            punto.appendChild(coordinates);
                            placemark.appendChild(punto);
                        }
                        else if(obra.getString("TYPE").equals("POLYLINE")){
                            Element linea = document.createElement("LineString");
                            JSONArray coord = obra.getJSONArray("COORDINATES");
                            String coords = "";
                            for(int i=0; i<coord.length(); i+=2){
                                coords.concat(String.valueOf(coord.getDouble(i))+","+String.valueOf(coord.getDouble((i+1)))+" ");
                            }
                            Element coordinates = document.createElement("coordinates");
                            coordinates.appendChild(document.createTextNode(coords));
                            linea.appendChild(coordinates);
                            placemark.appendChild(linea);
                        }
                        else if(obra.getString("TYPE").equals("MULTYGEOMETRY")){
                            Element multi = document.createElement("MultiGeometry");
                            JSONArray coord = obra.getJSONArray("ELEMENTS");
                            for(int i=0; i<coord.length(); i++){
                                JSONObject obj = coord.getJSONObject(i);
                                if(obj.getString("TYPE").equals("POINT")){
                                    Element punto = document.createElement("Point");
                                    JSONArray coord2 = obj.getJSONArray("COORDINATES");
                                    Element coordinates = document.createElement("coordinates");
                                    coordinates.appendChild(document.createTextNode(coord2.getString(0)+","+coord2.getString(1)));
                                    punto.appendChild(coordinates);
                                    multi.appendChild(punto);
                                }
                                else if(obj.getString("TYPE").equals("POLYLINE")){
                                    Element linea = document.createElement("LineString");
                                    JSONArray coord2 = obj.getJSONArray("COORDINATES");
                                    String coords = "";
                                    for(int l=0; l<coord2.length(); l+=02){
                                        coords = coords.concat(String.valueOf(coord2.getDouble(l))+","+String.valueOf(coord2.getDouble((l+1)))+" ");
                                    }
                                    Element coordinates = document.createElement("coordinates");
                                    coordinates.appendChild(document.createTextNode(coords));
                                    linea.appendChild(coordinates);
                                    multi.appendChild(linea);
                                }
                            }
                            placemark.appendChild(multi);
                        }
                    elemento.appendChild(placemark);
                    Source source = new DOMSource(document);
                    Result result = new StreamResult(archivo); //nombre del archivo

                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.transform(source, result);

                } catch(Exception e){
                    System.err.println("Error: "+e);
                }
            }
        }
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        ora.close();
        System.in.read();
    }
    private static void consolidadoGetByServicio() throws IOException, SQLException, PropertyManagerException {
        System.out.println("Indique el servicio que desea extraer.");   
        Scanner s = new Scanner(System.in);
        String servicio = s.next();
        OracleConnector ora = new OracleConnector();
        ora.connect();
        ResultSet GEO = ora.query("SELECT COUNT(DISTINCT CODPROYECTO) AS CANTIDAD FROM OBRAS_CAST WHERE SERV_CONTR LIKE '%"+servicio+"%'");
        System.out.println("Obras encontradas");
        GEO.next();
        System.out.println(GEO.getInt(1));
        System.out.println("¿Desea volcar la información en archivos KML? (SI/NO)");
        String volcar = s.next();
        ArrayList codigos = new ArrayList();
        if(volcar.toUpperCase().equals("SI")){
            System.out.println("Introdusca el nombre de la carpeta");
            String carpeta = s.next();
            File directorio = new File(carpeta); 
            directorio.mkdir();
            GEO = ora.query("SELECT CODPROYECTO,SDO_UTIL.TO_GMLGEOMETRY(SPATIAL_OBJECT) AS SPATIAL_OBJECT, ID_MERCADO_PUB FROM OBRAS_CAST WHERE SERV_CONTR LIKE '%"+servicio+"%'");
            String ruta = carpeta+"/"+servicio+".kml";
            File archivo = new File(ruta);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try{
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    DOMImplementation implementation = builder.getDOMImplementation();
                    Document document = implementation.createDocument("http://www.opengis.net/kml/2.2", "kml", null);
                    Element raiz = document.createElement("Document");  // creamos el elemento raiz
                    Element elemento = document.createElement("Folder"); //creamos un nuevo elemento
                    elemento.setAttribute("name", "Consolidado "+servicio);
                    document.getDocumentElement().appendChild(raiz);  //pegamos la raiz al documento
                    raiz.appendChild(elemento); //pegamos el elemento hijo a la raiz
                    Element schema = document.createElement("Schema");
                    schema.setAttribute("name", "model_data");
                    schema.setAttribute("id", "model_data_pattern");
                    Element IDMP = document.createElement("SimpleField");
                    IDMP.setAttribute("name", "ID MERCADO PUBLICO");
                    IDMP.setAttribute("type", "string");
                    schema.appendChild(IDMP);
                    Element GEOCOD = document.createElement("SimpleField");
                    GEOCOD.setAttribute("name", "CODIGO GEO");
                    GEOCOD.setAttribute("type", "string");
                    schema.appendChild(GEOCOD);
                    elemento.appendChild(schema);
                    while(GEO.next()){
                        if(codigos.indexOf(GEO.getString(1)) != -1) continue;
                        codigos.add(GEO.getString(1));
                        OBRA geom = new OBRA(GEO.getString(2));
                        geom.GMLCONVERT();
                        JSONObject obra = geom.getCoord();
                        Element placemark = document.createElement("Placemark");
                        placemark.setAttribute("name", GEO.getString(3));
                        if(obra.getString("TYPE").equals("POINT")){
                            Element punto = document.createElement("Point");
                            JSONArray coord = obra.getJSONArray("COORDINATES");
                            Element coordinates = document.createElement("coordinates");
                            coordinates.appendChild(document.createTextNode(coord.getString(0)+","+coord.getString(1)));
                            punto.appendChild(coordinates);
                            placemark.appendChild(punto);
                        }
                        else if(obra.getString("TYPE").equals("POLYLINE")){
                            Element linea = document.createElement("LineString");
                            JSONArray coord = obra.getJSONArray("COORDINATES");
                            String coords = "";
                            for(int i=0; i<coord.length(); i+=2){
                                coords = coords.concat(String.valueOf(coord.getDouble(i))+","+String.valueOf(coord.getDouble((i+1)))+" ");
                            }
                            Element coordinates = document.createElement("coordinates");
                            coordinates.appendChild(document.createTextNode(coords));
                            linea.appendChild(coordinates);
                            placemark.appendChild(linea);
                        }
                        else if(obra.getString("TYPE").equals("MULTYGEOMETRY")){
                            Element multi = document.createElement("MultiGeometry");
                            JSONArray coord = obra.getJSONArray("ELEMENTS");
                            for(int i=0; i<coord.length(); i++){
                                JSONObject obj = coord.getJSONObject(i);
                                if(obj.getString("TYPE").equals("POINT")){
                                    Element punto = document.createElement("Point");
                                    JSONArray coord2 = obj.getJSONArray("COORDINATES");
                                    Element coordinates = document.createElement("coordinates");
                                    coordinates.appendChild(document.createTextNode(coord2.getString(0)+","+coord2.getString(1)));
                                    punto.appendChild(coordinates);
                                    multi.appendChild(punto);
                                }
                                else if(obj.getString("TYPE").equals("POLYLINE")){
                                    Element linea = document.createElement("LineString");
                                    JSONArray coord2 = obj.getJSONArray("COORDINATES");
                                    String coords = "";
                                    for(int l=0; l<coord2.length(); l+=2){
                                        coords = coords.concat(String.valueOf(coord2.getDouble(l))+","+String.valueOf(coord2.getDouble((l+1)))+" ");
                                    }
                                    Element coordinates = document.createElement("coordinates");
                                    coordinates.appendChild(document.createTextNode(coords));
                                    linea.appendChild(coordinates);
                                    multi.appendChild(linea);
                                }
                            }
                            placemark.appendChild(multi);
                        }    
                        
                        Element EXT_DATA = document.createElement("ExtendedData");
                        Element SCH_DATA = document.createElement("SchemaData");
                        SCH_DATA.setAttribute("schemaUrl", "#model_data_pattern");
                            Element IDMP_DATA = document.createElement("SimpleData");
                            IDMP_DATA.setAttribute("name", "ID MERCADO PUBLICO");
                            IDMP_DATA.setTextContent(GEO.getString(3));
                            SCH_DATA.appendChild(IDMP_DATA);
                            Element GEOCOD_DATA = document.createElement("SimpleData");
                            GEOCOD_DATA.setAttribute("name", "CODIGO GEO");
                            GEOCOD_DATA.setTextContent(GEO.getString(1));
                            SCH_DATA.appendChild(GEOCOD_DATA);
                        EXT_DATA.appendChild(SCH_DATA);
                        placemark.appendChild(EXT_DATA);
                        elemento.appendChild(placemark);
                    }
                    Source source = new DOMSource(document);
                    Result result = new StreamResult(archivo); //nombre del archivo
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.transform(source, result);
            } catch(Exception e){
                System.err.println("Error: "+e);
            }
        }
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        ora.close();
        System.in.read();
    }
    private static void ObtenerCartografia() throws IOException, SQLException, PropertyManagerException, TransformerException {
        System.out.println("INDIQUE LA CARTOGRAFIA QUE DESEA OBTENER."); 
        System.out.println("(1)REGIONAL, (2)PROVINCIAL, (3)COMUNAL");   
        Scanner s = new Scanner(System.in);
        String opcion = s.next();
        switch (opcion) {
            case "1":
                {
                    XLS xls = new XLS();
                    xls.getRegiones();
                    break;
                }
            case "2":
                {
                    XLS xls = new XLS();
                    xls.getProvincias();
                    break;
                }
            case "3":
                {
                    XLS xls = new XLS();
                    xls.getComunas();
                    break;
                }
        }
    }
}
