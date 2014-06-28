package tw.idv.ctfan.taiwannextstation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ListAdapter;

public class MainActivity extends Activity implements LocationListener{
	
	private LocationManager locationManager;
	private Handler handler;
	
	private class Station {
		public Station(String n, Line l) {
			name = n;
			line = new ArrayList<Line>();
			line.add(l);
			distance = (float)9999.0;
		}
		
		String name;
		ArrayList<Line> line;
		double lon;
		double lat;
		float distance;
		int counter = 222;
	}
	
	private class Line {
		public Line(String n) {
			name = n;
			stations = new ArrayList<Station>();
		}
		String name;
		ArrayList<Station> stations;
		
		public String toString() {
			return name + " (" + stations.size() + ")";
		}
	}
	
	ArrayList<Station> allStation;
	ArrayList<Line> allLine;
	
	private Station FindStation(String s) {
		for(Station sta:allStation) {
			if(sta.name.compareTo(s)==0) {
				return sta;
			}
		}
		return null;
	}
	
	public class StationListAdapter implements ListAdapter {
		
		public ArrayList<Station> m_stationList;
		private Context m_context;
		int type;
		
		public StationListAdapter(Context context, ArrayList<Station> stationList, int type) {
			super();
			
			m_stationList = stationList;
			m_context = context;
			this.type = type;
			
			if(m_stationList == null) {
				m_stationList = new ArrayList<Station>();
			}
		}
		
		public void SetList(ArrayList<Station> stationList) {
			m_stationList = stationList;
			if(m_stationList == null)
				m_stationList = new ArrayList<Station>();
		}

		@Override
		public int getCount() {
			return m_stationList.size();
		}

		@Override
		public Object getItem(int position) {
			return m_stationList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			return 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout layout = new LinearLayout(m_context);
			layout.setOrientation(LinearLayout.VERTICAL);
			
			Station sta = m_stationList.get(position);
			
			TextView name = new TextView(m_context);
			name.setText(sta.name + sta.counter);
			name.setTextSize(20);
			if(sta == m_centralStation)
				name.setTextColor(Color.RED);
			layout.addView(name);
			
			if(type==0) {
				TextView dis = new TextView(m_context);
				dis.setText(String.format("%f m", sta.distance));
				dis.setTextSize(12);
				layout.addView(dis);
			}
			
			TextView pos = new TextView(m_context);
			pos.setText(String.format("%.6f\n%.6f", sta.lon, sta.lat));
			pos.setTextSize(12);
			layout.addView(pos);	
			
			
			return layout;
		}

		@Override
		public int getViewTypeCount() {
			return 4;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return m_stationList.isEmpty();
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}
		
	}
	
	public class LineListAdapter implements ListAdapter {
		
		public ArrayList<Line> m_lineList;
		private Context m_context;
		
		public LineListAdapter(Context context, ArrayList<Line> lineList) {
			super();
			m_context = context;
			m_lineList = lineList;
			
			if(m_lineList == null) {
				m_lineList = new ArrayList<Line>();
			}
		}
		
		public void SetList(ArrayList<Line> stationList) {
			m_lineList = stationList;
			if(m_lineList == null)
				m_lineList = new ArrayList<Line>();
		}

		@Override
		public int getCount() {
			return m_lineList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return m_lineList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public int getItemViewType(int arg0) {
			return 1;
		}

		@Override
		public View getView(int pos, View view, ViewGroup parent) {
			LinearLayout layout = new LinearLayout(m_context);
			layout.setOrientation(LinearLayout.VERTICAL);
			
			TextView textView = new TextView(m_context);
			
			Line line = m_lineList.get(pos);
			textView.setText(line.toString());
			textView.setTextSize(20);
			
			layout.addView(textView);
			
			textView = new TextView(m_context);
			Station sta1 = line.stations.get(0);
			Station sta2 = line.stations.get(line.stations.size()-1);
			textView.setText(sta1.name + " - " + sta2.name);
			textView.setTextSize(12);
			
			layout.addView(textView);
			
			return layout;
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return m_lineList.isEmpty();
		}

		@Override
		public void registerDataSetObserver(DataSetObserver arg0) {
			
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver arg0) {
			
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int arg0) {
			return true;
		}
		
	}
	
	MockLocationProvider mock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		allStation = new ArrayList<Station>();
		allLine = new ArrayList<Line>();
		LoadLineInfo(MainActivity.this.getApplicationContext());
		LoadPos(MainActivity.this.getApplicationContext());		

//		mock = new MockLocationProvider(LocationManager.NETWORK_PROVIDER, this);
		
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);		
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, this);
		
//		handler = new Handler();
//		handler.postDelayed(new MockLocationProvider.MockLocSendRunnable(handler, mock), 3000);
		
		lineListView = (ListView)findViewById(R.id.otherLine);
		lineArrayAdapter = new LineListAdapter(this, allLine);
		lineListView.setAdapter(lineArrayAdapter);	
		
		currLineStationAdapter = new StationListAdapter(this, null, 0);
		currListView = (ListView)findViewById(R.id.currLine);
		currListView.setAdapter(currLineStationAdapter);
		
		otherLineStationAdapter = new StationListAdapter(this, null, 1);
		otherLineView = (ListView)findViewById(R.id.otherLineStation);
		otherLineView.setAdapter(otherLineStationAdapter);
		
		lineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				otherLineStationAdapter.SetList(lineArrayAdapter.m_lineList.get(pos).stations);
				otherLineView.invalidateViews();
				
				int p = lineArrayAdapter.m_lineList.get(pos).stations.indexOf(m_centralStation);
				otherLineView.smoothScrollToPosition(p);
			}
		});
		
		otherLineView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				// TODO: change m_atLine
			}
		});
	}
	
	static LineListAdapter lineArrayAdapter;
	static StationListAdapter currLineStationAdapter;
	static StationListAdapter otherLineStationAdapter;
	static ListView lineListView;
	static ListView currListView;
	static ListView otherLineView;	
	
	
	public void LoadLineInfo(Context context) {

		try {
			InputStream is = context.getApplicationContext().getResources().openRawResource(R.raw.line);
			BufferedReader fin = new BufferedReader(new InputStreamReader(is, "UTF-8") );
			String line;
			Line currentLine = null;
			Station currentStation;
			
			while( (line = fin.readLine()) != null ){
				String[] subLine = line.split(",");
				if(subLine.length!=4) {
					continue;
				}
				if(subLine[0].isEmpty()) {
					currentLine = null;
				}
				else if(subLine[1].isEmpty() || subLine[1].matches("id")) {
					currentLine = new Line(subLine[0]);
					allLine.add(currentLine);
				}
				else if(subLine[2].isEmpty()) {
					continue;
				}
				else if(subLine[3].isEmpty()) {
					continue;
				}
				else {
					currentStation = FindStation(subLine[0]);
					if(currentStation==null) {
						currentStation = new Station(subLine[0], currentLine);
						allStation.add(currentStation);
					}
					else {
						currentStation.line.add(currentLine);
					}
					currentLine.stations.add(currentStation);
				}
			}
		}
		catch (Exception e) {
			Toast.makeText(this, "Some error", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		Toast.makeText(this, "Read Station: " + allStation.size() + "\nRead Line: " + allLine.size(), Toast.LENGTH_LONG).show();
	}
	
	public void LoadPos(Context context) {
		InputStream is = context.getResources().openRawResource(R.raw.pos);
		BufferedReader fin = new BufferedReader(new InputStreamReader(is) );
		String line;
		Station currentStation;
		try {
			while( (line = fin.readLine()) != null ){
				String[] subLine = line.split(",");
				if(subLine[0].isEmpty()) {
					continue;
				}
				else if(subLine[1].isEmpty()) {
					continue;
				}
				else if(subLine[2].isEmpty()) {
					continue;
				}
				else if(subLine[0].compareTo("lat")==0) {
					continue;
				}
				else
				{
					currentStation = FindStation(subLine[2]);
					if(currentStation!=null) {
						currentStation.lon = Double.parseDouble(subLine[1]);
						currentStation.lat = Double.parseDouble(subLine[0]);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}	

	ArrayList<Station> m_monitoringStation = new ArrayList<Station>();
	ArrayList<Station> m_sublistStation = new ArrayList<Station>();
	Station m_centralStation = null;
	Line m_atLine = null;
	Station m_towardStation = null;
	Station m_leavingStation = null;
	
	int leavingCentralStationCounter = 0;

	@Override
	public void onLocationChanged(Location loc) {
		TextView textView = (TextView)this.findViewById(R.id.lonTextView);
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();
		textView.setText(String.format("Lon: %.6f", lon));
		

		textView = (TextView)this.findViewById(R.id.latTextView);
		textView.setText(String.format("Lat: %.6f", lat));
		
		
		if(m_centralStation == null) {
			m_centralStation = FindNearestStation(loc);
			CollectNearStation();
			UpdateMonitoringStation();
		}
		UpdateMonitoringStationDistance(loc);
		
		if(m_centralStation.counter == 333) { // leaving central station
			Station s = null;
			for(int i=0; i<m_monitoringStation.size(); i++) {
				if(m_monitoringStation.get(i).counter == 111) {
					s = m_monitoringStation.get(i);
					break;
				}
			}
			
			if(s!= null) {				
				
				m_towardStation = s;				
				if(m_centralStation.distance > 500.0) {
					m_leavingStation = m_centralStation;
					m_centralStation = m_towardStation;
					int pos1 = m_sublistStation.indexOf(m_leavingStation);
					int pos2 = m_sublistStation.indexOf(m_centralStation);
					int pos = pos2 + (pos2-pos1);
					if(pos<m_sublistStation.size()&&pos>=0)
						m_towardStation = m_sublistStation.get(pos2 + (pos2-pos1));
					CollectNearStation();
					UpdateMonitoringStation();
					
					((TextView)this.findViewById(R.id.nowStation)).setTextColor(Color.BLACK);
					((TextView)this.findViewById(R.id.arrow)).setTextColor(Color.RED);
					
					lineArrayAdapter.SetList(m_centralStation.line);
					otherLineStationAdapter.SetList(null);
					lineListView.invalidateViews();
					otherLineView.invalidateViews();
				}
			}
		}
		else if(m_centralStation.counter == 222) { // Stay their
			((TextView)this.findViewById(R.id.nowStation)).setTextColor(Color.RED);
		} 
		else if(m_centralStation.counter == 111) { // approacning central station
			if(m_centralStation.distance < 300.0) {
				((TextView)this.findViewById(R.id.nowStation)).setTextColor(Color.RED);
				((TextView)this.findViewById(R.id.arrow)).setTextColor(Color.BLACK);				
			}
			else if(m_centralStation.distance < 700.0) {
				((TextView)this.findViewById(R.id.nowStation)).setTextColor(Color.RED);
				((TextView)this.findViewById(R.id.arrow)).setTextColor(Color.RED);				
			}
		}
		
		
		
		
		// Update at Station Tag
		textView = (TextView)this.findViewById(R.id.leavingStation);
		textView.setText((m_leavingStation==null?"???":m_leavingStation.name));

		textView = (TextView)this.findViewById(R.id.nowStation);
		textView.setText((m_centralStation==null?"???":m_centralStation.name));

		textView = (TextView)this.findViewById(R.id.towardStation);
		textView.setText((m_towardStation==null?"???":m_towardStation.name));
		
		
		
		currListView.invalidateViews();	
	}
	
	private void CollectNearStation() {
		if(m_atLine == null) m_atLine = m_centralStation.line.get(0);
		m_sublistStation.clear();
	
		int pos = m_atLine.stations.indexOf(m_centralStation);
		for(int i = pos-3; i <=pos+3; i++) {
			if(i>=0 && i<m_atLine.stations.size()) {
				m_sublistStation.add(m_atLine.stations.get(i));
			}
		}		
		currLineStationAdapter.SetList(m_sublistStation);
		currListView.invalidateViews();
	}
	
	private void UpdateMonitoringStation() {
		m_monitoringStation.clear();
		for(Station s:m_sublistStation) {
			m_monitoringStation.add(s);
			
			for(Line l:s.line) {
				if(l!=m_atLine) {
					int pos = l.stations.indexOf(s);
					if(pos-1 >= 0)
						m_monitoringStation.add(l.stations.get(pos-1));
					if(pos+1 < l.stations.size()) 
						m_monitoringStation.add(l.stations.get(pos+1));
				}
			}
		}		
	}
	
	private void UpdateMonitoringStationDistance(Location loc) {
		Log.e("Update", "UpdateMonitoringStationDistance Station count:" + m_monitoringStation.size());
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();
		
		float[] result = new float[3];
		for(Station s:m_monitoringStation) {
			Location.distanceBetween(lat, lon, s.lat, s.lon, result);
			if(s.distance < result [0])
				s.counter = s.counter/10 + 300;
			else if(s.distance > result[0])
				s.counter = s.counter/10 + 100;
			else
				s.counter = s.counter/10 + 200;
			s.distance = result[0];
		}
		
		Collections.sort(m_monitoringStation, new Comparator<Station>() {

			@Override
			public int compare(Station arg0, Station arg1) {
				double offset = arg0.distance - arg1.distance;
				
				if(offset >0.0) 
					return 1;
				else if(offset <0.0)
					return -1;
				else return 0;
			}
			
		});
	}
	
	private Station FindNearestStation(Location loc) {
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();
		
		float[] result = new float[3];
		for(Station s:this.allStation) {
			Location.distanceBetween(lat, lon, s.lat, s.lon, result);
			s.distance = result[0];
		}
		Collections.sort(allStation, new Comparator<Station>() {

			@Override
			public int compare(Station arg0, Station arg1) {
				double offset = arg0.distance - arg1.distance;
				
				if(offset >0.0) 
					return 1;
				else if(offset <0.0)
					return -1;
				else return 0;
			}
			
		});
		
		
		if(allStation.get(0).distance<2000.0)
			return allStation.get(0);
		return null;
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	protected void OnDestroy() {
		mock.shutdown();
		super.onDestroy();
	}

}
