import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HungarianMethod {
	private ArrayList<Integer> garage_capacity = new ArrayList<Integer>();
	private ArrayList<Garage> garages = new ArrayList<Garage>();
	private ArrayList<Car> pending_vehicles = new ArrayList<Car>();
	public static int CAPACITY = 5; //<-- change this
	public static final int CAPACITY_VAR = 1;
	public static double LAMBDA = 5.0; //<-- change this
	private int goal = 0;
	private int numGarages = 4;
	private int numCars;
	private int alpha = 1;
	private double eta = 0.4;
	private int beta = 1;
	private double zeta = 0.4;
    public static int iterations = 0;
    private static int total_capacity;
    public static double total_cost_diff = 0.0;
    public static int num_samples = 0;
    public static double total_distance_diff = 0.0;
	double minCost = 0;
	double maxDist = 0;
	double maxCost = 0;
	double[][] diffMatrix;
	boolean[][] starMatrix;
	boolean[] coveredRows;
	boolean[] coveredColumns;
	boolean[][] primeMatrix;
	int minDim;
	Integer[] solution;

	
	public static void main(String[] args){
        for ( int i = 0; i < args.length; i+=2 ) {
            if (args[i].equals ("L")) {
                LAMBDA = Double.parseDouble(args[i+1]);
            }
            if (args[i].equals ("C")) {
                CAPACITY = Integer.parseInt(args[i+1]);
            }
        }
		System.out.println("Simulation stats");
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		System.out.println(timeStamp); 
		
		for(int p = 0; p < 100; p++ ) {
			Random rand = new Random();
			total_capacity = 0;
			int caps[] = new int[4];
			caps[0] = rand.nextInt(CAPACITY);
			total_capacity += caps[0];
			if (CAPACITY-total_capacity > 0) {
				caps[1] = rand.nextInt(CAPACITY-total_capacity);				
			} else {
				caps[1] = 0;
			}
			total_capacity += caps[1];
			if (CAPACITY-total_capacity > 0) {
				caps[2] = rand.nextInt(CAPACITY-total_capacity);				
			} else {
				caps[2] = 0;
			}
			total_capacity += caps[2];
			caps[3] = CAPACITY-total_capacity;
			total_capacity = CAPACITY;
			
			HungarianMethod marketStreeParkingManager = new HungarianMethod();
        	
			marketStreeParkingManager.garage_capacity.add(caps[0]);
			Garage bush350 = new Garage("g1", 0, 0, 25, caps[0]);
	
			marketStreeParkingManager.garage_capacity.add(caps[1]);
			Garage california555 = new Garage("g2", 0, 1056, 16, caps[1]);
			
			marketStreeParkingManager.garage_capacity.add(caps[2]);
			Garage bush225 = new Garage("g3", -528, 0, 13, caps[2]);
			
			marketStreeParkingManager.garage_capacity.add(caps[3]);			
			Garage halleck240 = new Garage("g4", 492, 712, 10, caps[3]);
			
			marketStreeParkingManager.garages.add(bush350);
			marketStreeParkingManager.garages.add(california555);
			marketStreeParkingManager.garages.add(bush225);
			marketStreeParkingManager.garages.add(halleck240);
			
	
			marketStreeParkingManager.manage_traffic();
			marketStreeParkingManager = null;
		}
		
		System.out.println("Simulation ends");
		timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        System.out.println("L = "+LAMBDA);
        System.out.println("C = "+CAPACITY);
            
        double avg_cost_diff=total_cost_diff/num_samples;
        System.out.print("Avg cost diff = " + avg_cost_diff);
        double avg_distance_diff=total_distance_diff/num_samples;
        System.out.println(" Avg dist diff = " + avg_distance_diff);

        System.out.println(timeStamp);
            
	}
	
	public void manage_traffic(){
		
//		try{
			for(int r = 0; r < 1000; r++) {
//				System.out.println();
//	        	if(pending_vehicles.size() > 0) {
//		        	pending_vehicles.clear();  
//		        	System.gc();
//	        	}	
				
	        	pending_vehicles.clear();  
	        	
	        	minCost = 0;
	        	maxDist = 0;
	        	maxCost = 0;
	        	System.gc();
						
				numCars = getPoisson(LAMBDA);
				while(numCars == 0){
					numCars = getPoisson(LAMBDA);
				}
				
//				System.out.println("#Cars="+ numCars);
//				numCars = 3;
				
				double[][] costMatrix = new double[numCars][total_capacity];
				solution = new Integer[numCars];
				diffMatrix = new double [numCars][total_capacity];
				starMatrix = new boolean [numCars][total_capacity];
				coveredRows = new boolean [numCars];
				coveredColumns = new boolean[total_capacity];
				primeMatrix = new boolean [numCars][total_capacity];
				double[][] feeMatrix = new double[numCars][numGarages];
				double[][] distMatrix = new double[numCars][numGarages];
								
				if(numCars > 0) {
			        for(int i = 0; i < numCars; i++){
						int duration = (int)(Math.random()*8+1);
			    		Car car = new Car((int)(Math.random()*3000-1500), (int)(Math.random()*3000-1500), goal, duration);
			    		pending_vehicles.add(car);
		
			        }    
		        }
				
		        
				for(int i = 0; i < numCars; i++){
					for(int j = 0; j < numGarages; j++) {
						double cost = pending_vehicles.get(i).getParkingDuration() * garages.get(j).getParkingRate();
			        	double dist = Math.sqrt(Math.pow(pending_vehicles.get(i).getLocation_x() - garages.get(j).getLocation_x(),2) +
				        				Math.pow(pending_vehicles.get(i).getLocation_y() - garages.get(j).getLocation_y(), 2));
					    
					    if(maxDist < dist)
					    	maxDist = dist;
					    
					    if(maxCost < cost)
					    	maxCost = cost;
					    
					}
				}
				
//				System.out.println("maxCost: " +maxCost);
				
				for(int i = 0; i < numCars; i++){
					int n = 0;
					for(int j = 0; j < numGarages; j++) {
						double cost = pending_vehicles.get(i).getParkingDuration() * garages.get(j).getParkingRate();
			        	double dist = Math.sqrt(Math.pow(pending_vehicles.get(i).getLocation_x() - garages.get(j).getLocation_x(),2) +
				        				Math.pow(pending_vehicles.get(i).getLocation_y() - garages.get(j).getLocation_y(), 2));
						
			        	for (int k = 0; k < garage_capacity.get(j); k++) {			        	
			        		costMatrix[i][n] = alpha * Math.pow(cost/maxCost, eta) + beta * Math.pow(dist/maxDist, zeta);
			        		if(minCost < costMatrix[i][n])
			        			minCost = costMatrix[i][n];
			        		n++;
			        	}
					}
				}
				
       /*         String garageCap = String.format ("%s%d_%d_%s", "g_cap", CAPACITY, CAPACITY_VAR, ".csv");
                BufferedWriter g_caps = new BufferedWriter(new FileWriter(garageCap, true));//<-- change this
                String carCost = String.format ("%s%d_%d_%s", "cars_cost", CAPACITY, CAPACITY_VAR, ".csv");
                BufferedWriter cars_cost = new BufferedWriter(new FileWriter(carCost, true));//<-- change this
                String carsBreakdown = String.format ("%s%d_%d_%s", "cars_breakdown", CAPACITY, CAPACITY_VAR, ".csv");
                BufferedWriter car_min = new BufferedWriter(new FileWriter(carsBreakdown, true));//<-- change this
	    		StringBuilder g_caps_sb = new StringBuilder();
	    	    StringBuilder cars_cost_sb = new StringBuilder();
	    	    StringBuilder car_min_sb = new StringBuilder();
                
			
	    	    if (r==0) {
                g_caps_sb.append("g1");
	        	g_caps_sb.append(",");
	        	g_caps_sb.append("g2");
	        	g_caps_sb.append(",");	        
	        	g_caps_sb.append("g3");
	        	g_caps_sb.append(",");
	        	g_caps_sb.append("g4");
	        	g_caps_sb.append("\n");
                }
		    
			    for(int cap: garage_capacity){
			    		g_caps_sb.append(cap);
			    		g_caps_sb.append(",");
			    }
			    
                g_caps_sb.setLength(g_caps_sb.length() - 1);
				g_caps_sb.append("\n");
		
			    g_caps.write(g_caps_sb.toString());
			    
                g_caps.close();*/
			    
				minCost = minCost * numCars * numGarages * 1000;
				
				for(int i = 0; i < numCars; i++)
					solution[i] = -1;
//				
				double finalCost = AssignmentOptimal(costMatrix);
                
  /*              if (r==0) {
		        car_min_sb.append("Car #");
		        car_min_sb.append(",");
		        car_min_sb.append("min cost");
		        car_min_sb.append(",");
		        car_min_sb.append("min dis");
		        car_min_sb.append(",");
		        car_min_sb.append("assigned cost");
		        car_min_sb.append(",");
		        car_min_sb.append("assigned distance");
		        car_min_sb.append(",");
		        car_min_sb.append("n.cost");
		        car_min_sb.append(",");
		        car_min_sb.append("n.distance");
		        car_min_sb.append("\n");
                }*/
                
                
		        for(int i = 0; i < numCars; i++){
	        		for(int j = 0; j < numGarages; j++){
	        			distMatrix[i][j] = (int) (Math.sqrt(Math.pow(pending_vehicles.get(i).getLocation_x() - garages.get(j).getLocation_x(),2) +
		        				Math.pow(pending_vehicles.get(i).getLocation_y() - garages.get(j).getLocation_y(), 2)));
	        		}
		        }
		        
		        for(int i = 0; i < numCars; i++){
        			for(int j = 0; j < numGarages; j++){
        			feeMatrix[i][j] = (int)(pending_vehicles.get(i).getParkingDuration() * garages.get(j).getParkingRate());
        			}
		        }
		        
		        for(int i = 0; i < numCars; i++){
	        		double car_min_cost = feeMatrix[i][0];
	        		double car_min_distance = distMatrix[i][0];
	        		
        			for(int j = 0; j < numGarages; j++){
	        			if(car_min_cost > feeMatrix[i][j])
	        				car_min_cost = feeMatrix[i][j];
	        			if(car_min_distance > distMatrix[i][j])
	        				car_min_distance = distMatrix[i][j];
        			}
        			
      /*  			car_min_sb.append(i);
        			car_min_sb.append(",");
        			car_min_sb.append(car_min_cost);
        			car_min_sb.append(",");
        			car_min_sb.append(car_min_distance);
        			car_min_sb.append(",");*/
        			int assigned_g_index = solution[i];
        			if(assigned_g_index > 0) {
	        		/*	car_min_sb.append(feeMatrix[i][assigned_g_index]);
	        			car_min_sb.append(",");
	        			car_min_sb.append(distMatrix[i][assigned_g_index]);
	        			car_min_sb.append(",");
	        			car_min_sb.append(feeMatrix[i][assigned_g_index]/maxCost);
	        			car_min_sb.append(",");
	        			car_min_sb.append(distMatrix[i][assigned_g_index]/maxDist);*/
        				int k = 0;
        				int n = garage_capacity.get(k);
        				while (assigned_g_index >= n) {
        					k++;
        					n += garage_capacity.get(k);
        				}
	                    total_cost_diff = total_cost_diff + feeMatrix[i][k] - car_min_cost;
	                    total_distance_diff = total_distance_diff + distMatrix[i][k] - car_min_distance;
	                    num_samples ++;
	                    
        			/*} else {
        				car_min_sb.append("NA");
	        			car_min_sb.append(",");
	        			car_min_sb.append("NA");
	        			car_min_sb.append(",");
	        			car_min_sb.append("NA");
	        			car_min_sb.append(",");
	        			car_min_sb.append("NA");
        			}
        			car_min_sb.append("\n");*/
                    

	        			
		        }
		     /*   car_min.write(car_min_sb.toString());
		        car_min.close();
				
                
                if (r==0) {
				cars_cost_sb.append("LAMBDA");
		        cars_cost_sb.append(",");
		        cars_cost_sb.append("#cars");
		        cars_cost_sb.append(",");
		        cars_cost_sb.append("Total Cost");
		        cars_cost_sb.append("\n");
                }
                
		        cars_cost_sb.append(LAMBDA);
		        cars_cost_sb.append(",");
		        cars_cost_sb.append(numCars);
		        cars_cost_sb.append(",");
		        cars_cost_sb.append(minCost);	
		        cars_cost_sb.append("\n");
                
	        
		        cars_cost.write(cars_cost_sb.toString());
		        cars_cost.close();*/
		        
		        
			}
		    
		        iterations++;
		        if (iterations%10000 == 0) {
                    System.out.print("Total cars = " + num_samples);
                    double avg_cost_diff=total_cost_diff/num_samples;
                    System.out.print(" Avg cost diff = " + avg_cost_diff);
                    double avg_distance_diff=total_distance_diff/num_samples;
                    System.out.println(" Avg dist diff = " + avg_distance_diff);
                }

		}
	/*	}catch (IOException e) {
	    		System.out.println("file write failed"); 
		}*/
	}

	
	public int getPoisson(double lambda){
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		
		do {
			k++;
			p *= Math.random();
		} while(p > L);
		
		return k-1;
	}
	
	private double AssignmentOptimal (double[][] costMatrix) 
	{
		double cost = 0;
		
		// preliminary steps
		if (numCars <= total_capacity) {
			minDim = numCars;
			
			for (int row = 0; row < numCars; row++) {
				//find smallest element
				double minValue = costMatrix[row][0];
				for (int col = 1; col < total_capacity; col++) {
					if (costMatrix[row][col]<minValue) 
						minValue = costMatrix[row][col];
				}
			
				// subtract the smallest element from every element of the row
				for (int col = 0; col < total_capacity; col++) {
					diffMatrix[row][col] = costMatrix[row][col]-minValue;
				}
			}
		
		
			//step 1 and 2a
			for (int row = 0; row < numCars; row++) {
				for (int col = 0; col < total_capacity; col++) {
					if (Math.abs(diffMatrix[row][col]) < Math.ulp(1.0)) {
						if (!coveredColumns[col]) {
							starMatrix[row][col] = true;
							coveredColumns[col] = true;
							break;
						}
					}
				}
			}
		} else {
			minDim = total_capacity;
			
			for (int col = 0; col < total_capacity; col++) {
				//find the smallest element in the column
				double minValue = costMatrix[0][col];
				for (int row = 1; row < numCars; row++) {
					if (costMatrix[row][col] < minValue)
						minValue = costMatrix[row][col];
				}
				
				//subtract the smallest element frpm each element in the column
				for (int row = 0; row < numCars; row++) {
					diffMatrix[row][col] = costMatrix[row][col] - minValue;
				}
			}
			
			// step 1 and 2a
			for (int col = 0; col < total_capacity; col++) {
				for (int row = 0; row < numCars; row++) {
					if (Math.abs(diffMatrix[row][col]) < Math.ulp(1.0)) {
						if (!coveredRows[row])
						{
							starMatrix[row][col] = true;
							coveredColumns[col] = true;
							coveredRows[row] = true;
							break;
						}
					}
				}
			}
			
			for (int row = 0; row < numCars; row++) coveredRows[row] = false;
		}
		
		// move to step 2b
		step2b ();
		
		// compute cost and remove invalid assignments
		cost = ComputeAssignment (costMatrix);
		return cost;
	}
	
	private void BuildAssignment ()
	{
		for (int row = 0; row < numCars; row++) {
			for (int col = 0; col < total_capacity; col++) {
				if (starMatrix[row][col]) {
					solution[row] = col;
					break;
				}
			}
		}
	}
	
	private double ComputeAssignment (double[][] costMatrix)
	{
		double cost = 0;
		for (int row = 0; row < numCars; row++) {
			int col = solution[row];
			if (col >= 0)
				cost += costMatrix[row][col];
		}
		return cost;
	}
	
	private void step2a ()
	{
		// cover every column contain a starred zero
		for (int col = 0; col < total_capacity; col++) {
			for (int row = 0; row < numCars; row++) {
				if (starMatrix[row][col]) {
					coveredColumns[col] = true;
					break;
				}
			}
		}
		
		//move to step 2b
		step2b ();
	}
	
	private void step2b ()
	{
		// count covered columns
		int numCoveredColumns = 0;
		for (int col = 0; col < total_capacity; col++) {
			if (coveredColumns[col]) numCoveredColumns++;
		}
		
		if (numCoveredColumns == minDim) {
			// algorithm finished
			BuildAssignment ();
		} else {
			// move to step 3
			step3 ();
		}
	}
	
	private void step3 ()
	{
		boolean zeroFound = true;
		
		while (zeroFound)
		{
			zeroFound = false;
			for (int col = 0; col < total_capacity; col++) {
				if (!coveredColumns[col]) {
					for (int row = 0; row < numCars; row++) {
						if (!coveredRows[row] && Math.abs(diffMatrix[row][col])<Math.ulp(1.0)) {
							//prime zero
							primeMatrix[row][col] = true;
							
							//find starred zero in current row
							int starCol = 0; 
							while (starCol < total_capacity &&!starMatrix[row][starCol]) 
								starCol++;
							
							if (starCol == total_capacity) //no starred zero
							{
								//move to step 4
								step4 (primeMatrix, row, col);
								return;
							} else {
								coveredRows[row] = true;
								coveredColumns[starCol] = false;
								zeroFound = true;
								break;
							}
						}
					}
				}
			}
		}
		
		//move to step 5
		step5 ();
	}
	
	private void step4 (boolean[][] primeMatrix, int row, int col)
	{
		//generate temporary copy of starMatrix
		boolean[][] newStarMatrix = new boolean [numCars][total_capacity];
		for (int i = 0; i < numCars; i++) {
			for (int j = 0; j < total_capacity; j++) {
				newStarMatrix[i][j] = starMatrix[i][j];
			}
		}
		
		// star current zero
		newStarMatrix[row][col] = true;
		
		//find starred zeros in current column
		int starCol = col;
		int starRow = 0;
		while (starRow < numCars && !starMatrix[starRow][starCol]) 
			starRow++;
		
		while (starRow < numCars) {
			//unstar the starred zero
			newStarMatrix[starRow][starCol] = false;
			
			//find primed zero in current row 
			int primeRow = starRow;
			int primeCol = 0;
			while (primeCol < total_capacity &&!primeMatrix[primeRow][primeCol]) 
				primeCol++;
			
			// star the primed zero
			if (primeCol == total_capacity) {
				break;				
			}
			newStarMatrix[primeRow][primeCol] = true;
			
			//find starred zero in current column
			starCol = primeCol;
			starRow = 0;
			while (starRow < numCars && !starMatrix[starRow][starCol])
				starRow++;
		}
		
		//use temporary copy as the new starMatrix, delete all prime
		for (int i = 0; i < numCars; i++) {
			for (int j = 0; j < total_capacity; j++) {
				primeMatrix[i][j]=false;
				starMatrix[i][j] = newStarMatrix[i][j];
			}
			coveredRows[i] = false;
		}
		
		// mover to step 2a
		step2a ();
	}
	
	private void step5 ()
	{
		//find smallest uncovered element h
		double h = 1e300;
		for (int row = 0; row < numCars; row++) {
			if (!coveredRows[row]) {
				for (int col = 0; col < total_capacity; col++) {
					if (!coveredColumns[col]) {
						if (diffMatrix[row][col] < h) h = diffMatrix[row][col];
					}
				}
			}
		}
		
		//add h to each covered row
		for (int row = 0; row < numCars; row++) {
			if (coveredRows[row]) {
				for (int col = 0; col < total_capacity; col++)
					diffMatrix[row][col] += h;
			}
		}
		
		//subtract h from each uncovered column
		for (int col = 0; col < total_capacity; col++) {
			if (!coveredColumns[col]) {
				for (int row = 0; row < numCars; row++) 
					diffMatrix[row][col] -= h;
			}
		}
		
		// move to step 3
		step3 ();
	}
}
