package tw.idv.ctfan.taiwannextstation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;

public class MockLocationProvider {
	  String providerName;
	  Context ctx;
	 
	  public MockLocationProvider(String name, Context ctx) {
	    this.providerName = name;
	    this.ctx = ctx;
	 
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
		  
		static final double[] lat = {
			25.0683159661,
			25.0778570026,
			25.077923974,
			25.0934529584,
			25.1083340216,
			25.1027510036,
			25.1089279633,
			25.0869060215,
			25.0655439869,
			25.0499640405,
			25.0411529839,
			25.0344959926
		};
		
		static final double[] lon = {
			121.6617349908,
			121.6676299833,
			121.6936230194,
			121.7139280122,
			121.7290539946,
			121.761886999,
			121.806147974,
			121.8275309633,
			121.822558986,
			121.79784704,
			121.7751630116,
			121.7637509666
		};
		
		static int station = 0;
		static int count = 0;
		static int status = 0;
		
		static double[] latList;
		static double[] lonList;

		@Override
		public void run() {
			if(count >= 0) {
				if(status == 0) {
					count--;
					mlp.pushLocation(lat[station], lon[station]);
				}
				else if(status == 1) {
					mlp.pushLocation(latList[count], lonList[count]);
					count--;
				}
				
			} else {
				if(status == 0) {
					count = 15;
					status = 1;
					latList = new double[count+1];
					lonList = new double[count+1];
					
					double latDiff = (lat[station+1] - lat[station])/count;
					double lonDiff = (lon[station+1] - lon[station])/count;
					
					for(int i=0; i<=count; i++) {
						latList[count-i] = lat[station] + latDiff*i;
						lonList[count-i] = lon[station] + lonDiff*i;
					}
				}
				else if(status == 1) {
					count = 5;
					status = 0;
					station++;
					station %= lat.length-1;
				}
			}
			handler.postDelayed(this, 1000);
		}		  
	  }
	}