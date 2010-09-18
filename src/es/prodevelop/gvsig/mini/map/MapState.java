/* gvSIG Mini. A free mobile phone viewer of free maps.
 *
 * Copyright (C) 2009 Prodevelop.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *   Prodevelop, S.L.
 *   Pza. Don Juan de Villarrasa, 14 - 5
 *   46001 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   prode@prodevelop.es
 *   http://www.prodevelop.es
 *
 *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Peque�a y
 *   Mediana Empresa de la Comunidad Valenciana) &
 *   European Union FEDER funds.
 *   
 *   2009.
 *   author Alberto Romeu aromeu@prodevelop.es 
 *   author Ruben Blanco rblanco@prodevelop.es 
 *   
 */

package es.prodevelop.gvsig.mini.map;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
import android.os.Environment;
import es.prodevelop.gvsig.mini.activities.Map;
import es.prodevelop.gvsig.mini.util.Utils;

/**
 * Class to persist phisically the map state between sesions. The state
 * is persisted into sdcard/gvsig/Utils.CONFIG_DIR/mapstate.txt
 * @author aromeu 
 * @author rblanco
 *
 */
public class MapState {	
	
	GPSPoint center;
	int zoomLevel;
	String layerName;
	private static final String fileName = "mapstate.txt";
	private String dirPath;
	public String gvTilesPath = null;
	Map map;
	private static final String LAT = "lat";
	private static final String LON = "lon";
	private static final String ZOOM = "zoom";
	private static final String LAYER = "layer";
	private static final String LAYER2 = "layer2";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String GVTILES = "gvtiles";	
	private final static Logger log = LoggerFactory.getLogger(MapState.class);
	
	/**
	 * The constructor
	 * @param map
	 */
	public MapState(Map map) {	
		try {
			log.setLevel(Utils.LOG_LEVEL);
			log.setClientID(this.toString());
			this.map = map;
			String SDDIR = Environment.getExternalStorageDirectory().getPath();
			String appDir = Utils.APP_DIR;
			String configDir = Utils.CONFIG_DIR;
			dirPath = SDDIR + File.separator + appDir + File.separator + configDir + File.separator;			
		} catch (Exception e) {
			log.error(e);
		}		
	}
	
	/**
	 * Persists the map state
	 */
	public void persist() {
		try {
			if (map != null) {				
				File f = new File(dirPath + fileName);
				log.debug("Persist map state to: " + f.getAbsolutePath());
				if (!f.exists()) {
					File dirFile = new File(dirPath);
					dirFile.mkdirs();
					f.createNewFile();
				}
//				} else {
//					f.delete();
//					f.createNewFile();
//				}
				FileWriter logwriter = new FileWriter(f, false);

				BufferedWriter out = new BufferedWriter(logwriter);
				out.write(ZOOM + "=" + map.osmap.getZoomLevel()+"\n");
				out.write(LAYER + "=" + map.osmap.getMRendererInfo().getNAME()+"\n");
				
				
				out.write(X + "=" + map.osmap.getMRendererInfo().getCenter().getX() +"\n");
				out.write(Y + "=" + map.osmap.getMRendererInfo().getCenter().getY() +"\n");
				
				out.write(GVTILES +"=" +gvTilesPath);
				
				out.close();	
				log.debug("Map state sucessfully persisted");
			}			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * Loads the map state from the previous state
	 * @return True if the map state is loaded correctly
	 */
	public boolean load() {		
		FileReader configReader = null;
		BufferedReader reader = null;
		try {
			File f = new File(dirPath + fileName);
			if (f != null && f.exists()) {
				log.debug("load map state: " + f.getAbsolutePath());
				configReader = new FileReader(f);
				reader = new BufferedReader(configReader);	
				
			} else {
				log.debug("Map state not exists in disk");
				return false;				
			}
			
			String line = null;
			String[] part;
			HashMap properties = new HashMap();
			while ((line = reader.readLine()) != null) {
				part = line.split("=");
				properties.put(part[0], part[1]);
				log.debug(part[1]);
			}
			
			double x = 0.0;
			double y = 0.0;
			
			try {
				x = Double.valueOf(properties.get(X).toString()).doubleValue();
				y = Double.valueOf(properties.get(Y).toString()).doubleValue();
			} catch (Exception ignore) {
				
			}
			
			int zoom = Integer.valueOf(properties.get(ZOOM).toString()).intValue();
			String layer = properties.get(LAYER).toString();
			String layer2 = properties.get(LAYER2).toString();
			String gvTilesPath = properties.get(GVTILES).toString();			
				
			if (gvTilesPath.compareTo("") != 0 && gvTilesPath.compareToIgnoreCase("null") != 0) {
				log.debug("gvtilesPath: " + gvTilesPath);
				this.gvTilesPath = (gvTilesPath);
				Layers.getInstance().loadProperties(this.gvTilesPath);
			}
			map.osmap.onLayerChanged(layer);
			map.osmap.setMapCenter(x, y);
			map.osmap.setZoomLevel(zoom);
			
			return true;
		} catch (Exception e) {
			log.error(e);
			return false;
		}
	}
}