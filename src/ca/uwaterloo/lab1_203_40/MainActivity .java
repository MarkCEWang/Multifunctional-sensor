package ca.uwaterloo.Lab4_203_40;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.R.layout;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.uwaterloo.Lab4_203_40.MainActivity.PlaceholderFragment;
import ca.uwaterloo.Lab4_203_40.R;
import mapper.*;

public class MainActivity extends Activity {
	//static LineGraphView graph;
	static Mapper mv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
		//graph = new LineGraphView(getApplicationContext(),100,Arrays.asList("x","y","z"));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu,  v,  menuInfo);
		mv.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item) || mv.onContextItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			//FORMATTING
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			LinearLayout lmain = (LinearLayout)rootView.findViewById(R.id.layout);
			lmain.setOrientation(LinearLayout.VERTICAL);
			//lmain.addView(graph);
			//graph.setVisibility(View.VISIBLE);
			
			//MAP
			mv = new Mapper(getActivity().getApplicationContext(), 900, 760, 35, 35);
			getActivity().registerForContextMenu(mv);
			MapLoader mm = new MapLoader();
			PedometerMap map = mm.loadMap(getActivity().getExternalFilesDir(null), "Lab-room-peninsula.svg");
			mv.setMap(map);
			lmain.addView(mv);
		
			//SENSORS
			SensorManager sensorManager = (SensorManager)rootView.getContext().getSystemService(SENSOR_SERVICE);
			
			//RESET BUTTON:
			Button reset = new Button(rootView.getContext());
			lmain.addView(reset);
			reset.setText("CLEAR");
			reset.setOnClickListener(new View.OnClickListener() {
				public void onClick(View clear) {
					for (int i = 0; i<3; i++) {
						AccEventListener.accRec[i] = 0;
						AccEventListener.smoothedAcc[i] = 0;
						MagEventListener.magRec[i] = 0;
						//RotEventListener.rotRec[i] = 0;
					}
					StepEventListener.stepCount = 0;
					StepEventListener.displaceNS = 0;
					StepEventListener.displaceWE = 0;
					mv.removeAllLabeledPoints();
					mv.setUserPath(null);
				}
			});
			
			//CALIBRATE 'NORTH' BUTTON:
			Button setNorth = new Button(rootView.getContext());
			lmain.addView(setNorth);
			setNorth.setText("Calibrate 'North'");
			setNorth.setOnClickListener(new View.OnClickListener() {
				public void onClick(View clear) {
					getRoute.north = getOrientation.azimut;
				}
			});
			
			//ACCELEROMETER
//			TextView accDisplay = new TextView(rootView.getContext());
//			accDisplay.setText("Acceleration X: Y: Z: ");
//			lmain.addView(accDisplay);
//			TextView[] accData = new TextView[4];
//			for (int i = 0; i<3 ; i++) {
//				accData[i] = new TextView(rootView.getContext());
//				lmain.addView(accData[i]);
//			}		
//			Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//			SensorEventListener acc = new AccEventListener(accData);
//			sensorManager.registerListener(acc, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
			
			//STEP SENSOR
			TextView[] displaceData = new TextView[3];
			for (int i = 0; i<3; i++) {
				displaceData[i] = new TextView(rootView.getContext());
				lmain.addView(displaceData[i]);
			}
			Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			SensorEventListener step = new StepEventListener(displaceData);
			sensorManager.registerListener(step, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
			
			//GET ORIENTATION
			TextView orientDisplay = new TextView(rootView.getContext());
			orientDisplay.setText("Orientation: ");
			lmain.addView(orientDisplay);
			TextView[] orientData = new TextView[4];
			for (int i = 0; i<4; i++) {
				orientData[i] = new TextView(rootView.getContext());
				lmain.addView(orientData[i]);
			}
			Sensor orientSensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			Sensor orientSensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			SensorEventListener orient = new getOrientation(orientData);
			sensorManager.registerListener(orient, orientSensor1, SensorManager.SENSOR_DELAY_UI);
			sensorManager.registerListener(orient, orientSensor2, SensorManager.SENSOR_DELAY_UI);
						
			
			//GET ROUTE
			TextView pointsDisplay = new TextView(rootView.getContext());
			pointsDisplay.setText("Start/End: ");
			lmain.addView(pointsDisplay);
			TextView[] pointsData = new TextView[4];
			for (int i = 0; i<4; i++) {
				pointsData[i] = new TextView(rootView.getContext());
				lmain.addView(pointsData[i]);
			}
			IMapperListener router = new getRoute(pointsData);
			mv.addListener(router);
						
			return rootView;
		}	
	}
}

class getRoute implements IMapperListener {
	static TextView[] output = new TextView[4];
	static PointF start, end;
	float startX, startY, endX, endY;
	List<InterceptPoint> intersectionsList = new ArrayList<InterceptPoint>();
	static List<PointF> endPoints = new ArrayList<PointF>();
	List<LineSegment> lines = new ArrayList<LineSegment>();
	PointF intersection = new PointF();
	PointF compare = new PointF();
	PointF reference = new PointF();
	boolean right = false;
	boolean down = false;
	boolean left = false;
	boolean up = false;
	static double angleBetween;
	static double north;
	static int tracker = 1;
	
	public getRoute(TextView[] outputView) {
		for (int i = 0; i<4; i++) {
			output[i] = outputView[i];
		}
	}
	
	public void update(Mapper source, PointF start, PointF end) {
		intersectionsList.clear();
		endPoints.clear();
		boolean firstline = true;
		if (end != null && start != null) {
			PointF holder = new PointF();
			holder = start;
			endPoints.add(start);
			right = true;
			intersectionsList = source.calculateIntersections(start, end);
			if (intersectionsList.isEmpty()) {
				endPoints.add(start);
				endPoints.add(end);				
				source.setUserPath(endPoints);
				angleBetween = Math.atan2(end.y-start.y, end.x-start.x);
				angleBetween = angleBetween * 57.3;
				output[0].setText("Angle: " + angleBetween + " " + "north is: " + north);
				LineSegment tmp = new LineSegment(start, end);
				getOrientation.takeSteps = (int) (tmp.length()/0.9);
			}
			else {
				for (int i = 0; i<100 ; i++) {
					if (right == true) {
						reference.x = 25;
						reference.y = holder.y;
						intersectionsList = source.calculateIntersections(holder, reference);
						intersection = intersectionsList.get(0).getPoint();
						intersection.offset((float)-0.9, 0);
						if (firstline = true) {
							LineSegment line = new LineSegment(start, intersection);
							getOrientation.takeSteps = (int)(line.length()/0.9);
							firstline = false;
						}
						holder = intersection;
						right = false;
						down = true;
						compare.x = end.x;
						compare.y = holder.y;
						intersectionsList = source.calculateIntersections(holder, compare);
						if (intersectionsList.isEmpty()) {
							endPoints.add(compare);
							endPoints.add(end);
							break;
						}
						if (endPoints.contains(intersection)) {
							endPoints.subList(endPoints.indexOf(intersection), endPoints.size()).clear();
						}
						else {
							endPoints.add(intersection);
						}	
//						intersectionsList = source.calculateIntersections(holder, end);
//						if (intersectionsList.isEmpty()) {
//							endPoints.add(end);
//							break;
//						}
					}
					
					if (down == true) {
						reference.x = holder.x;
						reference.y = 23;
						intersectionsList = source.calculateIntersections(holder, reference);
						intersection = intersectionsList.get(0).getPoint();
						if (intersection.y >= 20) {
							intersection.y = (float)19.5;
						}
						intersection.offset(0, (float)-0.9);
						holder = intersection;
						down = false;
						left = true;
						compare.x = holder.x;
						compare.y = end.y;
						intersectionsList = source.calculateIntersections(compare, end);
						if (intersectionsList.isEmpty()) {
							endPoints.add(compare);
							endPoints.add(end);
							break;
						}	
						if (endPoints.contains(intersection)) {
							endPoints.subList(endPoints.indexOf(intersection), endPoints.size()).clear();
						}
						else {
							endPoints.add(intersection);
						}
					}
					if (left == true) {
						reference.x = 0;
						reference.y = holder.y;
						intersectionsList = source.calculateIntersections(holder, reference);
						intersection = intersectionsList.get(0).getPoint();
						intersection.offset((float) 0.9, 0);
						holder = intersection;
						left = false;
						up = true;
						compare.x = end.x;
						compare.y = holder.y;
						intersectionsList = source.calculateIntersections(holder, compare);
						if (intersectionsList.isEmpty()) {
							endPoints.add(compare);
							endPoints.add(end);
							break;
						}
						if (endPoints.contains(intersection)) {
							endPoints.subList(endPoints.indexOf(intersection), endPoints.size()).clear();
						}
						else {
							endPoints.add(intersection);
						}
					}
					if (up == true) {
						reference.x = holder.x;
						reference.y = 0;
						intersectionsList = source.calculateIntersections(holder, reference);
						intersection = intersectionsList.get(0).getPoint();
						intersection.offset(0, (float) 0.9);
						holder = intersection;
						up = false; 
						right = true;
						compare.x = holder.x;
						compare.y = end.y;
						intersectionsList = source.calculateIntersections(compare, end);
						if (intersectionsList.isEmpty()) {
							endPoints.add(compare);
							endPoints.add(end);
							break;
						}
						if (endPoints.contains(intersection)) {
							endPoints.subList(endPoints.indexOf(intersection), endPoints.size()).clear();
						}
						else {
							endPoints.add(intersection);
						}
					}
				}
				if (endPoints.size() >= 5) {
					LineSegment first = new LineSegment(endPoints.get(endPoints.size()-5), endPoints.get(endPoints.size()-4));
					LineSegment overlap = new LineSegment(endPoints.get(endPoints.size()-2), endPoints.get(endPoints.size()-1));
					PointF intersect = new PointF();
					intersect = first.findIntercept(overlap);
					if (intersect != null) {
						endPoints.add(intersect);
					}
				}
				output[3].setText("("+endPoints.get(0).x+", "+endPoints.get(0).y+") "+", "+"("+endPoints.get(1).x+", "+endPoints.get(1).y+")");
				source.setUserPath(endPoints);
			}
		}
	}
	public void locationChanged(Mapper source, PointF loc){
		start = loc;
		output[1].setText("Start: (" + start.x + ", " + start.y + ")");
		source.setUserPoint(loc);
		update(source, start, end);
		//CurrentChanged(source);
	}
	public void DestinationChanged(Mapper source, PointF dest){
		end = dest;
		output[2].setText("End: (" + end.x + ", " + end.y + ")" + "\n");
		update(source, start, end);
	}
	public static void CurrentChanged (Mapper source) {
		if (start != null && end != null && !(Math.abs(getRoute.end.x - MainActivity.mv.getUserPoint().x) <= 0.9 && Math.abs(getRoute.end.y - MainActivity.mv.getUserPoint().y) <= 0.9)) {
			PointF location = new PointF();
			if (source.calculateIntersections(source.getStartPoint(),source.getEndPoint()).isEmpty()) {
				location = source.getUserPoint();
				location.offset((float)(Math.sin((angleBetween+90)/57.3) * 0.9), (float)-(Math.cos((angleBetween+90)/57.3) * 0.9));
				source.setUserPoint(location);
		}
			else {
				if (tracker-1 != endPoints.indexOf(end)) {
					PointF starting = source.getUserPoint();
					PointF ending = endPoints.get(tracker);
					LineSegment tmp = new LineSegment(starting, ending);
					getOrientation.takeSteps = (int) (tmp.length()/0.9);

					if (Math.abs(starting.x-ending.x) <= 0.3) { 
						location = starting;
						if (starting.y < ending.y) {
							angleBetween = 90;
							location.offset(0, (float)0.9);
						}
						else {
							angleBetween = -90;
							location.offset(0, (float) -0.9);
						}
						source.setUserPoint(location);
					}
					if (Math.abs(starting.y-ending.y) <= 0.3) { 
						location = starting;
						if (starting.x < ending.x){
							angleBetween = 0;
							location.offset((float)0.9 ,0);
						}
						else {
							angleBetween = 180;
							location.offset((float)-0.9, 0);
						}
						source.setUserPoint(location);
					}
					if (Math.abs(ending.x- location.x)<= 0.4 && Math.abs(ending.y - location.y ) <= 0.4 ) {
						starting = location;
						source.setUserPoint(ending);
						//getOrientation.takeSteps = 0;
						tracker++;
						ending = endPoints.get(tracker);
					}
				}
			}
		}
	}
}

class StepEventListener implements SensorEventListener {
	TextView[] output = new TextView[3];
	float tmp;
	static int stepCount;
	static boolean[] SM = new boolean[] {false, false, false, false};
	static int displaceNS=0;
	static int displaceWE=0;
	
	public StepEventListener(TextView[] outputView) {
		for (int i = 0; i<3; i++) {
			output[i] = outputView[i];
		}
	}
	public void onAccuracyChanged(Sensor s, int i) {}
	
	public void onSensorChanged(SensorEvent se) {
		if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			float[] smoothedAccel = new float[] {0,0,0};
			for (int i = 0; i<3 ; i++) {
			smoothedAccel[i] += (float) (se.values[i]-smoothedAccel[i]/10);
			}

			if (smoothedAccel[2] < 6 && smoothedAccel[2] > 2.3) {
				SM[0] = true;
			}
			if (SM[0] && smoothedAccel[2] <= 1) {
				SM[1] = true;
			}
			if (SM[0] && SM[1] && smoothedAccel[2] >= 1.4) {
				SM[2] = true;
			}
			if (SM[0] && SM[1] && SM[2]){
				stepCount++;
				for (int i = 0; i<3 ; i++) {
					SM[i] = false;
				}
				if (getOrientation.azimut <= -133 || getOrientation.azimut >= 139) {
					//GOING SOUTH
					displaceNS--;
				}
				else if (getOrientation.azimut >= 46 && getOrientation.azimut < 139) {
					//GOING EAST
					displaceWE--;
				}
				else if (getOrientation.azimut >-133 && getOrientation.azimut < -43) {
					//GOING WEST
					displaceWE++;
				}
				else if (getOrientation.azimut >= -43 && getOrientation.azimut <46) {
					//GOING NORTH
					displaceNS++;
				}
				if (Math.abs(getOrientation.heading) <= 20 || Math.abs(getOrientation.heading)>= 340 ) {
					getOrientation.takeSteps--;
					//if (getOrientation.takeSteps >= 0) {
						getRoute.CurrentChanged(MainActivity.mv);
				//	}
				}
			}
			
			output[0].setText("Steps taken: " + stepCount + "\n");
			output[1].setText("Displacement N/S (N = +): " + displaceNS );
			output[2].setText("Displacement W/E (W = +) "  +  displaceWE + "\n");	
		}
	}
}


class getOrientation implements SensorEventListener {
	TextView[] output = new TextView[4];
	float[] accData = new float[3];
	float[] magData = new float[3];
	static int takeSteps;
	static double azimut;
	double pitch;
	double roll;
	static int heading;

	public getOrientation(TextView[] outputView) {
		for (int i = 0; i<4; i++) {
			output[i] = outputView[i];
		}
	}
	public void onAccuracyChanged(Sensor s, int i) {}
	public void onSensorChanged(SensorEvent se) {
		if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			for (int i = 0; i<3 ; i++) {
				accData[i] = se.values[i];
			}
		}
		if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			for (int i = 0; i<3 ; i++) {
				magData[i] = se.values[i];
			}
		}
		if (accData != null && magData != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean yeah = SensorManager.getRotationMatrix(R, I, accData, magData);
			if (yeah) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				//converting to degrees
				azimut = orientation[0] * 57.3 ;
				pitch = orientation[1] *57.3 ;
				roll = orientation[2] * 57.3;
			}
			heading = (int) (getRoute.north + 90 + getRoute.angleBetween);
			heading = (int)( heading - azimut);
			if (heading >= 250) {
				heading -= 360;
			}
			output[0].setText("Azimut: " + azimut);
			output[1].setText("Turn right: " + heading + "degrees" + "\n");
			if (getRoute.end != null && MainActivity.mv.getUserPoint() != null){
				if (takeSteps <= 0 && (Math.abs(MainActivity.mv.getUserPoint().x - MainActivity.mv.getEndPoint().x) <= 0.4) && 
						 (Math.abs(MainActivity.mv.getUserPoint().y - MainActivity.mv.getEndPoint().y) <= 0.4)) {
					output[2].setText("You have reached your destination");
				}
				else {
					output[2].setText("Take " + takeSteps + " steps");
				}
			}
		}
	}
}
class AccEventListener implements SensorEventListener {
	TextView[] output = new TextView[3];
	float[] tmp = new float[3];
	static float[] accRec = new float[] {0,0,0};
	public static float accZ;
	static float[] smoothedAcc = new float[] {0,0,0};

	
	public AccEventListener(TextView[] outputView) {
		for (int i = 0; i<3; i++) {
			output[i] = outputView[i];
		}
	}
	public void onAccuracyChanged(Sensor s, int i) {}
	public void onSensorChanged(SensorEvent se) {
		if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			for (int i = 0; i<3 ; i++) {
				smoothedAcc[i] += (se.values[i]- smoothedAcc[i])/10;
			}
//			MainActivity.graph.addPoint(smoothedAcc);
			for (int i = 0; i<3; i++) {
				tmp[i] = se.values[i];
				if (Math.abs(tmp[i])>= Math.abs(accRec[i])) {
					accRec[i] = Math.abs(tmp[i]);
				}
			}
//			output[0].setText("Recorded X: " + smoothedAcc[0] + "  Max X: " + accRec[0]);
//			output[1].setText("Recorded Y: " + smoothedAcc[1] + "  Max Y: " + accRec[1]);
//			output[2].setText("Recorded Z: " + smoothedAcc[2] + "  Max Z: " + accRec[2] + "\n");
			
		}
	}
}
class MagEventListener implements SensorEventListener {
	TextView[] output = new TextView[3];
	float[] tmp = new float[3];
	static float[] magRec = new float[] {0,0,0};

	
	public MagEventListener(TextView[] outputView) {
		for (int i = 0; i<3; i++) {
			output[i] = outputView[i];
		}
	}
	public void onAccuracyChanged(Sensor s, int i) {}
	public void onSensorChanged(SensorEvent se) {
		if(se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			for (int i = 0; i<3; i++) {
				tmp[i] = se.values[i];
				if (Math.abs(tmp[i]) >= Math.abs(magRec[i])) {
					magRec[i] = Math.abs(tmp[i]);
				}
			}
			output[0].setText("Recorded X: " + tmp[0] + "  Max X: " + magRec[0]);
			output[1].setText("Recorded Y: " + tmp[1] + "  Max Y: " + magRec[1]);
			output[2].setText("Recorded Z: " + tmp[2] + "  Max Z: " + magRec[2] + "\n");
		}
	}
}