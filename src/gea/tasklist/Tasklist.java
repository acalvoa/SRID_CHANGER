package gea.tasklist;

import gea.properties.PropertyManager;
import gea.properties.PropertyManagerException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Tasklist {
    // ESTABLECEMOS LOS PARAMETROS DE CLASE
    String contexto;
    JSONObject config;
    String layout;
    String task;
    private String parametros;
    private JSONObject jsonparam;
    // ESTABLECEMOS EL CONSTRUCTOR
    public static JSONObject getConfig() throws IOException, PropertyManagerException{
        PropertyManager pm = PropertyManager.getInstance();
        JSONObject configu = new JSONObject();
        JSONObject db = new JSONObject();
        db.put("HOST", pm.getProperty(PropertyManager.GEOCGR_FILE,PropertyManager.GEOCGR.DBHOST).toString());
        db.put("PORT", pm.getProperty(PropertyManager.GEOCGR_FILE,PropertyManager.GEOCGR.DBPORT).toString());
        db.put("USERNAME", pm.getProperty(PropertyManager.GEOCGR_FILE,PropertyManager.GEOCGR.DBUSERNAME).toString());
        db.put("PASSWORD", pm.getProperty(PropertyManager.GEOCGR_FILE,PropertyManager.GEOCGR.DBPASSWORD).toString());
        db.put("DATABASE", pm.getProperty(PropertyManager.GEOCGR_FILE,PropertyManager.GEOCGR.DBDATABASE).toString());
        JSONObject app = new JSONObject();
        app.put("HOST", pm.getProperty(PropertyManager.GEOCGR_FILE,PropertyManager.GEOCGR.APPHOST).toString());
        app.put("PORT", pm.getProperty(PropertyManager.GEOCGR_FILE,PropertyManager.GEOCGR.APPPORT).toString());
        app.put("CLIENTPORT", pm.getProperty(PropertyManager.GEOCGR_FILE,PropertyManager.GEOCGR.CLIENTPORT).toString());
        configu.put("DATABASE",db);
        configu.put("WEBSOCKET", app);
        return configu;
    }
}
