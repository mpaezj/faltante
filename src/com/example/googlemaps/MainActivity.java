package com.example.googlemaps;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends Activity implements OnMapLongClickListener,
		OnMapClickListener, OnMyLocationChangeListener {
	private GoogleMap googleMap;
	private LatLngBounds zr =  new LatLngBounds(null, null);

	double lat2;
	double lon2;
	boolean on = false;
	private static ArrayList<LatLng> puntos = new ArrayList<LatLng>();

	static LocationManager lm;
	static MiLocationListener mlistener;

	public static String lat, lon, loc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mlistener = new MiLocationListener();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5,
				mlistener);

		googleMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.map)).getMap();

		googleMap.setMyLocationEnabled(true);

		googleMap.setOnMapClickListener(this);
		googleMap.setOnMapLongClickListener(this);
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		googleMap.getUiSettings().setCompassEnabled(true);
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);
		googleMap.getUiSettings().setAllGesturesEnabled(true);
		googleMap.setTrafficEnabled(true);

	}

	public void limpiar(View view) {
		googleMap.clear();
	}

	public void genzona(View view) {

		if (puntos.size() != 4) {
			Toast.makeText(this, "Deben haber 4 puntos para generar una zona",
					Toast.LENGTH_LONG).show();
		} else {

			PolygonOptions opcionesPoligono = new PolygonOptions()
					.add(new LatLng(puntos.get(0).latitude,
							puntos.get(0).longitude))
					.add(new LatLng(puntos.get(1).latitude,
							puntos.get(1).longitude))
					.add(new LatLng(puntos.get(2).latitude,
							puntos.get(2).longitude))
					.add(new LatLng(puntos.get(3).latitude,
							puntos.get(3).longitude));

			Polygon poligono = googleMap.addPolygon(opcionesPoligono);
		
					

			poligono.setFillColor(Color.BLUE); // Relleno del polígono
			poligono.setStrokeColor(Color.RED); // Bordes del polígono

			puntos = new ArrayList<LatLng>();

		}

		
	}
	public void genzonaR(View view) {

		if (puntos.size() != 2) {
			Toast.makeText(this, "Deben haber 2 puntos para generar una zona de riesgo",
					Toast.LENGTH_LONG).show();
		} else {
			zr = new LatLngBounds(new LatLng(puntos.get(0).latitude,puntos.get(0).longitude),
					              new LatLng(puntos.get(1).latitude,puntos.get(1).longitude));
			
		}

		
	}
	

	public void genruta(View view) {

		connectAsyncTask _connectAsyncTask = new connectAsyncTask();
		_connectAsyncTask.execute();

	}
	
	public void gencirculo(View view){
		 CircleOptions opcionesCirculo = new CircleOptions().center(
			       new LatLng(lat2,lon2)).radius(200);
			    googleMap.clear();
			    Circle circulo = googleMap.addCircle(opcionesCirculo);
			    circulo.setFillColor(Color.RED);
			    circulo.setStrokeColor(Color.RED);
			    circulo.setStrokeWidth(2f);
	}

	private class connectAsyncTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setMessage("analizando ruta, espere un momento");
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			fetchData();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (doc != null) {
				NodeList _nodelist = doc.getElementsByTagName("status");
				Node node1 = _nodelist.item(0);
				String _status1 = node1.getChildNodes().item(0).getNodeValue();
				if (_status1.equalsIgnoreCase("OK")) {
					NodeList _nodelist_path = doc
							.getElementsByTagName("overview_polyline");
					Node node_path = _nodelist_path.item(0);
					Element _status_path = (Element) node_path;
					NodeList _nodelist_destination_path = _status_path
							.getElementsByTagName("points");
					Node _nodelist_dest = _nodelist_destination_path.item(0);
					String _path = _nodelist_dest.getChildNodes().item(0)
							.getNodeValue();
					List<LatLng> directionPoint = decodePoly(_path);

					PolylineOptions rectLine = new PolylineOptions().width(10)
							.color(Color.RED);
					for (int i = 0; i < directionPoint.size(); i++) {
						rectLine.add(directionPoint.get(i));
					}
					// Adding route on the map
					googleMap.addPolyline(rectLine);
				}
			}
			progressDialog.dismiss();
		}
	}

	Document doc = null;

	private void fetchData() {
		StringBuilder urlString = new StringBuilder();
		urlString
				.append("http://maps.google.com/maps/api/directions/xml?origin=");
		urlString.append(lat);
		urlString.append(",");
		urlString.append(lon);
		urlString.append("&destination=");// to
		urlString.append(lat2);
		urlString.append(",");
		urlString.append(lon2);
		urlString.append("&sensor=true&mode=driving");
		Log.d("url", "::" + urlString.toString());
		HttpURLConnection urlConnection = null;
		URL url = null;
		try {
			url = new URL(urlString.toString());
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.connect();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = (Document) db.parse(urlConnection.getInputStream());// Util.XMLfromString(response);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<LatLng> decodePoly(String encoded) {
		ArrayList<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;
		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			poly.add(position);
		}
		return poly;
	}

	public class MiLocationListener implements LocationListener {

		public MiLocationListener() {
			// TODO Auto-generated constructor stub
		}

		public synchronized void onLocationChanged(Location location) {

			DecimalFormat f = new DecimalFormat("###.#####");

			lat = f.format(location.getLatitude()).replace(",", ".");
			lon = f.format(location.getLongitude()).replace(",", ".");
			LatLng pos = new LatLng(Double.parseDouble(lat),
					Double.parseDouble(lon));
			CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(pos, 15);
			googleMap.moveCamera(cam);
			
			if(zr.contains(pos)){
				Toast.makeText(getApplicationContext(), "Zona de riesgo",
						Toast.LENGTH_LONG).show();
			}

		}

		public synchronized void onProviderDisabled(String provider) {

		}

		public synchronized void onProviderEnabled(String provider) {

		}

		public synchronized void onStatusChanged(String provider, int status,
				Bundle extras) {

		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onMapLongClick(LatLng point) {
		// TODO Auto-generated method stub
		googleMap.addMarker(new MarkerOptions().position(point).title(
				point.toString()));

		lat2 = point.latitude;
		lon2 = point.longitude;

		puntos.add(point);

		// Toast.makeText(this,""+point,Toast.LENGTH_LONG).show();
		// Toast.makeText(this,""+coords.get(0),Toast.LENGTH_LONG).show();

	}

	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub
		googleMap.animateCamera(CameraUpdateFactory.newLatLng(point));

	}

	@Override
	public void onMyLocationChange(Location location) {
		// TODO Auto-generated method stub

	}

}
