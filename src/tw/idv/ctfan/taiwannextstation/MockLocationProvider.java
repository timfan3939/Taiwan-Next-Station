package tw.idv.ctfan.taiwannextstation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class MockLocationProvider {
	  String providerName;
	  Context ctx;
	  
	  private class LOC {
		  double lon;
		  double lat;
		  double alt;
		  double spd;
		  double head;
		  
		  
		  public LOC(double longtitude, double latitude, double altitude, double speed, double head) {
			  lon = longtitude;
			  lat = latitude;
			  alt = altitude;
			  spd = speed;
		  }
		  
		  public Location toLocation(String providerName) {
			  Location loc = new Location(providerName);
			  
			  loc.setLongitude(lon);
			  loc.setAltitude(alt);
			  loc.setSpeed((float)spd);
			  loc.setLatitude(lat);
			  loc.setBearing((float)head);
			  
			  return loc;
		  }
	  };
	  
	  ArrayList<LOC> testCase;
	  
	  void LoadTestCase() {
		  testCase = new ArrayList<LOC>();

			try {
				InputStream is = this.ctx.getApplicationContext().getResources().openRawResource(R.raw.testdata1);
				BufferedReader fin = new BufferedReader(new InputStreamReader(is, "UTF-8") );
				String line;
				double lon, lat, alt, spd, head;
				
				while( (line = fin.readLine()) != null ){
					String[] subLine = line.split(",");
					if(subLine.length!=6) {
						continue;
					}
					else if(subLine[0].matches("SN"))
						continue;
					else {
						lon = Double.parseDouble(subLine[2]);
						lat = Double.parseDouble(subLine[1]);
						spd = Double.parseDouble(subLine[5]);
						alt = Double.parseDouble(subLine[3]);
						head = Double.parseDouble(subLine[4]);
						
						testCase.add(new LOC(lon, lat, alt, spd, head));
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
	  }
	 
	  public MockLocationProvider(String name, Context ctx) {
	    this.providerName = name;
	    this.ctx = ctx;
	    
	    LoadTestCase();
	 
	    LocationManager lm = (LocationManager) ctx.getSystemService(
	      Context.LOCATION_SERVICE);
	    lm.addTestProvider(providerName, false, false, false, false, false, 
	      true, true, 0, 5);
	    lm.setTestProviderEnabled(providerName, true);
	  }
	 
	  @SuppressLint("NewApi")
	public void pushLocation(double lat, double lon) {
	    LocationManager lm = (LocationManager) ctx.getSystemService(
	      Context.LOCATION_SERVICE);
	 
	    Location mockLocation = new Location(providerName);
	    mockLocation.setLatitude(lat);
	    mockLocation.setLongitude(lon); 
	    mockLocation.setAltitude(0); 
	    mockLocation.setAccuracy(10);
	    mockLocation.setTime(System.currentTimeMillis());
	    mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());  
	    lm.setTestProviderLocation(providerName, mockLocation);
	  }
	  
	  @SuppressLint("NewApi")
	public void pushLocation(LOC loc) {
		    LocationManager lm = (LocationManager) ctx.getSystemService(
		  	      Context.LOCATION_SERVICE);
		    Location location = loc.toLocation(providerName);
		    location.setTime(System.currentTimeMillis());
		    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
		    location.setAccuracy(10);
		    
		    lm.setTestProviderLocation(providerName, location);
	  }
	 
	  public void shutdown() {
	    LocationManager lm = (LocationManager) ctx.getSystemService(
	      Context.LOCATION_SERVICE);
	    lm.removeTestProvider(providerName);
	  }
	  
	  public static class MockLocSendRunnable implements Runnable {
		  
		  MockLocationProvider mlp;
		  Handler handler;
		  
		public MockLocSendRunnable(Handler handler, MockLocationProvider mlp) {
			this.mlp = mlp;
			this.handler = handler;
		}
		  
		
		int counter = 0;
		
		@Override
		public void run() {
			if(counter>mlp.testCase.size()) {
				Log.e("MockLocationProvider", "Counter is less then the size of test data " + counter + " " + mlp.testCase.size());
			}
			mlp.pushLocation(mlp.testCase.get(counter));
			Log.v("Location ID", "" + counter);
			counter++;
			handler.postDelayed(this, 1000);
		}		  
	  }
	}