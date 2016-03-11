package SRID_CHANGER;

import gea.adapters.OracleConnector;
import gea.properties.PropertyManagerException;
import gea.types.GEOM;
import gea.types.OBRA;
import gea.types.PRESITIONGEOM;
import java.io.*; 
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jxl.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XLS { 
    private void leerArchivoExcel(String archivoDestino) throws IOException, PropertyManagerException, TransformerException { 
        OracleConnector ora = new OracleConnector();
        ora.connect();
        JSONObject datafinal = new JSONObject();
        try { 
            Scanner s = new Scanner(System.in);
            System.out.println("Introdusca el Area");
            String areatxt = s.nextLine();
            Workbook archivoExcel = Workbook.getWorkbook(new File(archivoDestino)); 
            for (int sheetNo = 0; sheetNo < archivoExcel.getNumberOfSheets(); sheetNo++){ 
                Sheet hoja = archivoExcel.getSheet(sheetNo); 
                int numColumnas = hoja.getColumns(); 
                int numFilas = hoja.getRows(); 
                String data; 
                for (int fila = 1; fila < numFilas; fila++) {
                    if(!hoja.getCell(3, fila).getContents().equals(areatxt)){
                        continue;   
                    } 
                    data = hoja.getCell(1, fila).getContents(); 
                    ResultSet GEO = ora.query("SELECT COM.C_COMUNA_SUBDERE, PROV.C_PROV_SUBDERE, PROV.C_REG_SUBDERE FROM GEO_COMUNAS COM"
                            + "INNER JOIN GEO_PROVINCIAS PROV ON COM.PROV_X_PROV = PROV.X_PROV"
                            + "WHERE COM.T_COMUNA LIKE '"+data+"'");
                    if(!datafinal.has(data)){
                        JSONObject p = new JSONObject();
                        p.put("metadata", new JSONArray());
                        datafinal.put(data, p);
                    }
                    while(GEO.next()){
                        String cod = GEO.getString(1);
                        JSONObject dataesp = new JSONObject();
                        dataesp.put("area", hoja.getCell(3, fila).getContents());
                        datafinal.getJSONObject(data).put("comuna",data);
                        dataesp.put("presupuesto", hoja.getCell(4, fila).getContents());
                        dataesp.put("ejecucion", hoja.getCell(5, fila).getContents());
                        dataesp.put("porcentaje", hoja.getCell(6, fila).getContents());
                        dataesp.put("anoejercicio", hoja.getCell(7, fila).getContents());
                        dataesp.put("comuna", GEO.getString(1));
                        dataesp.put("provincia", GEO.getString(2));
                        dataesp.put("region", GEO.getString(3));
                        datafinal.getJSONObject(data).getJSONArray("metadata").put(dataesp);
                        datafinal.getJSONObject(data).put("subdere",cod);
                        datafinal.getJSONObject(data).put("comuna",data);
                        GEO = ora.query("SELECT SDO_UTIL.TO_GMLGEOMETRY(GEOMSEDE) FROM SPATIAL_SEDE_CONSISTORIAL WHERE C_COM_SUBDERE LIKE '"+cod+"'");
                        while(GEO.next()){
                            OBRA geom = new OBRA(GEO.getString(1));
                            geom.GMLCONVERT();
                            JSONObject obra = geom.getCoord();
                            if(obra.getString("TYPE").equals("POINT")){
                                JSONArray coord = obra.getJSONArray("COORDINATES");
                                datafinal.getJSONObject(data).put("coord", coord);
                            }
                        }
                    }
                } 
            } 
        } catch (Exception ioe) { 
            ioe.printStackTrace(); 
        } 
        this.makeKML(datafinal,1);
        this.makeJSON(datafinal);
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        ora.close();
        System.in.read();
    } 
    public void leer() throws IOException, PropertyManagerException, TransformerException{
        Scanner s = new Scanner(System.in);
        System.out.println("El archivo corresponde a SICA(1) o SICOGEN(2)?");
        String opciones = s.next();
        System.out.println("Introduce nombre del archivo.");
        String opcion = s.next();
        if(opciones.equals("1")){
            this.leerArchivoExcelSica(opcion);
        }
        else{
            this.leerArchivoExcel(opcion);
        }
    }
    private void makeKML(JSONObject make, int tipo) throws IOException, TransformerConfigurationException, TransformerException{
        Scanner s = new Scanner(System.in);
        System.out.println("Introdusca el nombre de la archivo");
        String archivotxt = s.nextLine();
        String ruta = archivotxt+".kml";
        File archivo = new File(ruta);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try{
                DocumentBuilder builder = factory.newDocumentBuilder();
                DOMImplementation implementation = builder.getDOMImplementation();
                Document document = implementation.createDocument("http://www.opengis.net/kml/2.2", "kml", null);
                Element raiz = document.createElement("Document");  // creamos el elemento raiz
                Element elemento = document.createElement("Folder"); //creamos un nuevo elemento
                elemento.setAttribute("name", archivotxt);
                document.getDocumentElement().appendChild(raiz);  //pegamos la raiz al documento
                raiz.appendChild(elemento); //pegamos el elemento hijo a la raiz
                /*Element schema = document.createElement("Schema");
                schema.setAttribute("name", "model_data");
                schema.setAttribute("id", "model_data_pattern");
                Element comuna = document.createElement("SimpleField");
                comuna.setAttribute("name", "Comuna");
                comuna.setAttribute("type", "string");
                schema.appendChild(comuna);
                Element subdere = document.createElement("SimpleField");
                subdere.setAttribute("name", "Codigo Subdere");
                subdere.setAttribute("type", "string");
                schema.appendChild(subdere);
                Element area = document.createElement("SimpleField");
                area.setAttribute("name", "Area");
                area.setAttribute("type", "string");
                schema.appendChild(area);
                Element presu = document.createElement("SimpleField");
                presu.setAttribute("name", "Presupuesto");
                presu.setAttribute("type", "string");
                schema.appendChild(presu);
                Element ejecu = document.createElement("SimpleField");
                ejecu.setAttribute("name", "Ejecucion");
                ejecu.setAttribute("type", "string");
                schema.appendChild(ejecu);
                Element porce = document.createElement("SimpleField");
                porce.setAttribute("name", "Porcentaje Ejecucion");
                porce.setAttribute("type", "string");
                schema.appendChild(porce);
                Element anio = document.createElement("SimpleField");
                anio.setAttribute("name", "Fecha Ejecucion");
                anio.setAttribute("type", "string");
                schema.appendChild(anio);
                elemento.appendChild(schema);*/
                
                Iterator<String> keys = make.keys();
                while(keys.hasNext()){
                    String llave = keys.next();
                    JSONObject obra = make.getJSONObject(llave);
                    Element placemark = document.createElement("Placemark");
                    Element punto = document.createElement("Point");
                    if(!obra.has("coord")) continue;
                    JSONArray coord = obra.getJSONArray("coord");
                    Element coordinates = document.createElement("coordinates");
                    coordinates.appendChild(document.createTextNode(coord.getString(0)+","+coord.getString(1)));
                    punto.appendChild(coordinates);
                    placemark.appendChild(punto);
                    JSONArray info = obra.getJSONArray("data");
                    Element EXT_DATA = document.createElement("ExtendedData");
                    if(tipo == 1){
                        for(int i=0;i<info.length(); i++){
                            JSONObject dataobj = info.getJSONObject(i);        
                            Element COMUNA_DATA = document.createElement("Data");
                            COMUNA_DATA.setAttribute("name", "Comuna");
                            this.generateValue(document,obra.getString("comuna"),"Comuna",COMUNA_DATA);
                            EXT_DATA.appendChild(COMUNA_DATA);
                            Element SUBDERE_DATA = document.createElement("Data");
                            SUBDERE_DATA.setAttribute("name", "Codigo Subdere");
                            this.generateValue(document,obra.getString("subdere"),"Codigo Subdere",SUBDERE_DATA);
                            EXT_DATA.appendChild(SUBDERE_DATA);
                            Element IDMP_DATA = document.createElement("Data");
                            IDMP_DATA.setAttribute("name", "Area");
                            this.generateValue(document,dataobj.getString("area"),"Area",IDMP_DATA);
                            EXT_DATA.appendChild(IDMP_DATA);
                            Element GEOCOD_DATA = document.createElement("Data");
                            GEOCOD_DATA.setAttribute("name", "Presupuesto");
                            this.generateValue(document,dataobj.getString("presupuesto"),"Presupuesto",GEOCOD_DATA);
                            EXT_DATA.appendChild(GEOCOD_DATA);
                            Element GEOCOD_EJE = document.createElement("Data");
                            GEOCOD_EJE.setAttribute("name", "Ejecucion");
                            this.generateValue(document,dataobj.getString("ejecucion"),"Ejecucion",GEOCOD_EJE);
                            EXT_DATA.appendChild(GEOCOD_EJE);
                            Element GEOCOD_POR = document.createElement("Data");
                            GEOCOD_POR.setAttribute("name", "Porcentaje Ejecucion");
                            this.generateValue(document,dataobj.getString("porcentaje"),"Porcentaje Ejecucion",GEOCOD_POR);
                            EXT_DATA.appendChild(GEOCOD_POR);
                            Element GEOCOD_ANIO = document.createElement("Data");
                            GEOCOD_ANIO.setAttribute("name", "Fecha Ejecucion");
                            this.generateValue(document,dataobj.getString("anoejercicio"),"Fecha Ejecucion",GEOCOD_ANIO);
                            EXT_DATA.appendChild(GEOCOD_ANIO);
                            break;
                        }
                    }
                    else if(tipo == 2){
                        for(int i=0;i<info.length(); i++){
                            JSONObject dataobj = info.getJSONObject(i);   
                            Element COMUNA_DATA = document.createElement("Data");
                            COMUNA_DATA.setAttribute("name", "Comuna");
                            this.generateValue(document,obra.getString("comuna"),"Comuna",COMUNA_DATA);
                            EXT_DATA.appendChild(COMUNA_DATA);
                            Element SUBDERE_DATA = document.createElement("Data");
                            SUBDERE_DATA.setAttribute("name", "Servicio");
                            this.generateValue(document,dataobj.getString("servicio"),"Servicio",SUBDERE_DATA);
                            EXT_DATA.appendChild(SUBDERE_DATA);
                            Element IDMP_DATA = document.createElement("Data");
                            IDMP_DATA.setAttribute("name", "Tipo");
                            this.generateValue(document,dataobj.getString("tipo"),"Tipo",IDMP_DATA);
                            EXT_DATA.appendChild(IDMP_DATA);
                            Element GEOCOD_DATA = document.createElement("Data");
                            GEOCOD_DATA.setAttribute("name", "Año Informe");
                            this.generateValue(document,dataobj.getString("anioinforme"),"Año Informe",GEOCOD_DATA);
                            EXT_DATA.appendChild(GEOCOD_DATA);
                            Element GEOCOD_EJE = document.createElement("Data");
                            GEOCOD_EJE.setAttribute("name", "Fecha Informe");
                            this.generateValue(document,dataobj.getString("fechainforme"),"Fecha Informe",GEOCOD_EJE);
                            EXT_DATA.appendChild(GEOCOD_EJE);
                            Element GEOCOD_POR = document.createElement("Data");
                            GEOCOD_POR.setAttribute("name", "Nombre Informe");
                            this.generateValue(document,dataobj.getString("nombreinforme"),"Nombre Informe",GEOCOD_POR);
                            EXT_DATA.appendChild(GEOCOD_POR);
                            Element GEOCOD_ANIO = document.createElement("Data");
                            GEOCOD_ANIO.setAttribute("name", "Ficha");
                            this.generateValue(document,"<a href='"+dataobj.getString("ficha")+"'>"+dataobj.getString("ficha")+"</a>","Ficha",GEOCOD_ANIO);
                            EXT_DATA.appendChild(GEOCOD_ANIO);
                            Element GEOCOD_PDF = document.createElement("Data");
                            GEOCOD_PDF.setAttribute("name", "Link Ficha PDF");
                            this.generateValue(document,"<a href='"+dataobj.getString("linkpdf")+"'>"+dataobj.getString("linkpdf")+"</a>","Link Ficha PDF",GEOCOD_PDF);
                            EXT_DATA.appendChild(GEOCOD_PDF);
                            break;
                        }
                    }
                    
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
    private void makeJSON(JSONObject make) throws IOException, TransformerConfigurationException, TransformerException{
        Scanner s = new Scanner(System.in);
        System.out.println("Introdusca el nombre de la archivo");
        String archivotxt = s.nextLine();
        String ruta = archivotxt+".json";
        File archivo = new File(ruta);
        FileWriter fichero = null;
        PrintWriter pw = null;
        fichero = new FileWriter(archivo);
        pw = new PrintWriter(fichero);
        pw.write(make.toString());
        fichero.close();
    }
    private void makeJSON(JSONArray make) throws IOException, TransformerConfigurationException, TransformerException{
        Scanner s = new Scanner(System.in);
        System.out.println("Introdusca el nombre de la archivo");
        String archivotxt = s.nextLine();
        String ruta = archivotxt+".json";
        File archivo = new File(ruta);
        FileWriter fichero = null;
        PrintWriter pw = null;
        fichero = new FileWriter(archivo);
        pw = new PrintWriter(fichero);
        pw.write(make.toString());
        fichero.close();
    }
    private void generateValue(Document doc, String value, String name, Element nodo){
        Element named = doc.createElement("displayName");
        named.setTextContent(name);
        nodo.appendChild(named);
        Element ele = doc.createElement("value");
        ele.setTextContent(value);
        nodo.appendChild(ele);
    }

    private void leerArchivoExcelSica(String archivoDestino) throws IOException, PropertyManagerException, TransformerException {
        OracleConnector ora = new OracleConnector();
        ora.connect();
        JSONObject datafinal = new JSONObject();
        try { 
            Scanner s = new Scanner(System.in);
            System.out.println("Introdusca el Area");
            String areatxt = s.nextLine();
            Workbook archivoExcel = Workbook.getWorkbook(new File(archivoDestino)); 
            for (int sheetNo = 0; sheetNo < archivoExcel.getNumberOfSheets(); sheetNo++){ 
                Sheet hoja = archivoExcel.getSheet(sheetNo); 
                int numColumnas = hoja.getColumns(); 
                int numFilas = hoja.getRows(); 
                String data; 
                for (int fila = 1; fila < numFilas; fila++) {
                    if(!hoja.getCell(3, fila).getContents().equals(areatxt)){
                        continue;   
                    } 
                    data = hoja.getCell(2, fila).getContents(); 
                    ResultSet GEO = ora.query("SELECT COM.C_COMUNA_SUBDERE, PROV.C_PROV_SUBDERE, PROV.C_REG_SUBDERE FROM GEO_COMUNAS COM "
                            + "INNER JOIN GEO_PROVINCIAS PROV ON COM.PROV_X_PROV = PROV.X_PROV"
                            + "WHERE COM.T_COMUNA LIKE '"+data+"'");
                    if(!datafinal.has(data)){
                        JSONObject p = new JSONObject();
                        p.put("metadata", new JSONArray());
                        datafinal.put(data, p);
                    }
                    while(GEO.next()){
                        String cod = GEO.getString(1);
                        JSONObject dataesp = new JSONObject();
                        dataesp.put("servicio", hoja.getCell(1, fila).getContents());
                        dataesp.put("comuna", hoja.getCell(2, fila).getContents());
                        dataesp.put("anioinforme", hoja.getCell(5, fila).getContents());
                        dataesp.put("tipo", hoja.getCell(3, fila).getContents());
                        dataesp.put("fechainforme", hoja.getCell(6, fila).getContents());
                        dataesp.put("nombreinforme", hoja.getCell(7, fila).getContents());
                        dataesp.put("ficha", hoja.getCell(13, fila).getContents());
                        dataesp.put("linkpdf", hoja.getCell(14, fila).getContents());
                        dataesp.put("comuna", GEO.getString(1));
                        dataesp.put("provincia", GEO.getString(2));
                        dataesp.put("region", GEO.getString(3));
                        datafinal.getJSONObject(data).getJSONArray("metadata").put(dataesp);
                        datafinal.getJSONObject(data).put("subdere",cod);
                        datafinal.getJSONObject(data).put("comuna",data);
                        GEO = ora.query("SELECT SDO_UTIL.TO_GMLGEOMETRY(GEOMSEDE) FROM SPATIAL_SEDE_CONSISTORIAL WHERE C_COM_SUBDERE LIKE '"+cod+"'");
                        while(GEO.next()){
                            OBRA geom = new OBRA(GEO.getString(1));
                            geom.GMLCONVERT();
                            JSONObject obra = geom.getCoord();
                            if(obra.getString("TYPE").equals("POINT")){
                                JSONArray coord = obra.getJSONArray("COORDINATES");
                                datafinal.getJSONObject(data).put("coord", coord);
                            }
                        }
                    }
                } 
                break;
            } 
        } catch (Exception ioe) { 
            ioe.printStackTrace(); 
        } 
        
        System.out.println("\n\rPRESS ANY KEY TO CONTINUE");
        ora.close();
        this.makeKML(datafinal,2);
        this.makeJSON(datafinal);
        System.in.read();
    }
    public void getRegiones() throws IOException, PropertyManagerException, SQLException, TransformerException{
        OracleConnector ora = new OracleConnector();
        ora.connect();
        JSONArray datafinal = new JSONArray();
            ResultSet GEO = ora.query("SELECT REG.T_REGION,C_REGION_SUBDERE, SPA.HECTAREAS, SDO_UTIL.TO_GMLGEOMETRY(SPA.GEOMREGIONAL), SDO_UTIL.TO_GMLGEOMETRY(SPA.CENTROIDE) FROM GEO_REGIONES REG "
                    + "INNER JOIN SPATIAL_DATA_REGIONAL SPA ON REG.C_REGION_SUBDERE = SPA.C_REG");
        while(GEO.next()){
            JSONObject regionjson = new JSONObject();
            JSONObject metadata = new JSONObject();
            metadata.put("T_REGION", GEO.getString(1));
            metadata.put("T_SUBDERE", GEO.getString(2));
            metadata.put("HECTAREAS", GEO.getString(3));
            OBRA geom = new OBRA(GEO.getString(5));
            geom.GMLCONVERT();
            JSONObject obra = geom.getCoord();
            JSONArray coord = obra.getJSONArray("COORDINATES");
            metadata.put("CENTROIDE", coord);
            regionjson.put("metadata", metadata);
            GEOM geometry = new GEOM (GEO.getString(4));
            geometry.GMLCONVERT();
            regionjson.put("SPATIAL_DATA", geometry.getCoord());
            regionjson.put("name", GEO.getString(1));
            datafinal.put(regionjson);
        }
        this.makeJSON(datafinal);
        ora.close();
    }

    void getProvincias() throws IOException, PropertyManagerException, SQLException, TransformerException{
        OracleConnector ora = new OracleConnector();
        ora.connect();
        JSONArray datafinal = new JSONArray();
        ResultSet GEO = ora.query("SELECT REG.T_PROVINCIA,C_PROV_SUBDERE, SPA.HECTAREAS, SDO_UTIL.TO_GMLGEOMETRY(SPA.GEOMPROVINCIA), SDO_UTIL.TO_GMLGEOMETRY(SPA.CENTROIDE), SPA.X_REGI FROM GEO_PROVINCIAS REG INNER JOIN SPATIAL_DATA_PROVINCIAL SPA ON REG.C_PROV_SUBDERE = SPA.CINE_PROV");
        while(GEO.next()){
            JSONObject regionjson = new JSONObject();
            JSONObject metadata = new JSONObject();
            metadata.put("T_PROVINCIA", GEO.getString(1));
            metadata.put("T_SUBDERE", GEO.getString(2));
            metadata.put("HECTAREAS", GEO.getString(3));
            metadata.put("REGION", GEO.getString(6));
            if(GEO.getString(5) != null){
                OBRA geom = new OBRA(GEO.getString(5));
                geom.GMLCONVERT();
                JSONObject obra = geom.getCoord();
                JSONArray coord = obra.getJSONArray("COORDINATES");
                metadata.put("CENTROIDE", coord);
            }
            if(GEO.getString(4) != null){
                GEOM geometry = new GEOM(GEO.getString(4));
                geometry.GMLCONVERT();
                regionjson.put("SPATIAL_DATA", geometry.getCoord());
            }
            regionjson.put("metadata", metadata);
            regionjson.put("name", GEO.getString(1));
            datafinal.put(regionjson);
        }
        this.makeJSON(datafinal);
        ora.close();
    }

    void getComunas() throws IOException, PropertyManagerException, SQLException, TransformerException{
        OracleConnector ora = new OracleConnector();
        ora.connect();
        JSONArray datafinal = new JSONArray();
        ResultSet GEO = ora.query("SELECT REG.T_COMUNA,C_COMUNA_SUBDERE, SPA.HECTAREAS, SPA.CINE_PROV, SPA.X_REGI, SDO_UTIL.TO_GMLGEOMETRY(SPA.GEOMCOMUNAL), SDO_UTIL.TO_GMLGEOMETRY(SPA.CENTROIDE), SDO_UTIL.TO_GMLGEOMETRY(SPA.CENTROCONSISTORIAL)  FROM GEO_COMUNAS REG INNER JOIN SPATIAL_DATA_COMUNAL SPA ON REG.C_COMUNA_SUBDERE = SPA.CINE_COM");
        while(GEO.next()){
            JSONObject regionjson = new JSONObject();
            JSONObject metadata = new JSONObject();
            metadata.put("T_COMUNA", GEO.getString(1));
            metadata.put("T_SUBDERE", GEO.getString(2));
            metadata.put("HECTAREAS", GEO.getString(3));
            metadata.put("PROVINCIA", GEO.getString(4));
            metadata.put("REGION", GEO.getString(5));
            if(GEO.getString(6) != null){
                OBRA geom = new OBRA(GEO.getString(7));
                geom.GMLCONVERT();
                JSONObject obra = geom.getCoord();
                JSONArray coord = obra.getJSONArray("COORDINATES");
                metadata.put("CENTROIDE", coord);
            }
            //CENTRO CONSISTORIAL
            if(GEO.getString(7) != null){
                OBRA geomcentro = new OBRA(GEO.getString(8));
                geomcentro.GMLCONVERT();
                JSONObject obracentro = geomcentro.getCoord();
                JSONArray coords = obracentro.getJSONArray("COORDINATES");
                metadata.put("C_CONSISTORIAL", coords);
            }
            //METADATA
            
            if(GEO.getString(8) != null){
                GEOM geometry = new GEOM(GEO.getString(6));
                geometry.GMLCONVERT();
                regionjson.put("SPATIAL_DATA", geometry.getCoord());
            }
            regionjson.put("metadata", metadata);
            regionjson.put("name", GEO.getString(1));
            datafinal.put(regionjson);
        }
        this.makeJSON(datafinal);
        ora.close();
    }
}