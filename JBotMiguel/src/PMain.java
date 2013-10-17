import evolutionaryrobotics.PostEvaluation;


public class PMain {
	
	public static void main(String[] args) {
		//new String[]{"--controllers","+classname=CrossForageArbitrator"}
		//new String[]{"--controllers","+classname=CrossForageArbitrator","--evaluation","classname=PreyAggregationExponentialEvaluationFunction,robotpercentage=0.5,preypercentage=0.5,robotdistance=0.5,preydistance=0.2"}
		double[][] values = new PostEvaluation(
				new String[]{"dir=bigdisk/september2013/cross_forage_size_variable/forage_with_closed_doors_derived","localevaluation=0","steps=15000"}).runPostEval();
		
		int nRuns = 10;
		
		double[] results = new double[nRuns];
		double[] averages = new double[nRuns];
		double[] stdDeviations = new double[nRuns];
		
		int maxIndex = 0;
		
		for(int i = 0 ; i < results.length ; i++) {
			
			double val = 0;
			
			for(int j = 0 ; j < values[i].length ; j++)
				val+= values[i][j];
			
			averages[i] = getAverage(values[i]);
			stdDeviations[i] = getStdDeviation(values[i],averages[i]);
			
			results[i] = val;
			
			if(results[i] > results[maxIndex])
				maxIndex = i;
		}
		
		int best = maxIndex+1;
		
		String result = "#best: "+best+"\n";
		
		for(int i = 0 ; i < values.length ; i++) {
			result+=(i+1)+" ";
			for(double j : values[i])
				result+=j+" ";
			
			result+="("+averages[i]+" +- "+stdDeviations[i]+")\n";
		}
		
		double overallAverage = getAverage(averages);
		
		result+="Overall: "+overallAverage+" +- "+getStdDeviation(averages, overallAverage);
		
		System.out.println(result);
	}
	
	public static  double getAverage(double[] values) {
		
		double avg = 0;
		
		for(double i : values)
			avg+=i;
		
		return avg/(double)values.length;
	}
	
	public static double getStdDeviation(double[] values, double avg) {
		
		double stdDeviation = 0;
		
		for(double d : values)
			stdDeviation+=Math.pow(d-avg,2);
		
		return Math.sqrt(stdDeviation/(double)values.length);
	}

}