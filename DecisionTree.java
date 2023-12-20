import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;
import java.util.Collections;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split

	// Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;

	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf

		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}

		private double[] findBestSplit(ArrayList<Datum> datalist) {
			double best_avg_enthropy = Double.POSITIVE_INFINITY;
			double best_attr = -1;
			double best_threshold = -1;
			int j = 0;
			for (int a = 0; a < datalist.get(0).x.length; a++) {
				for (int i = 0; i < datalist.size(); i++) {
					double t = datalist.get(i).x[a];
					ArrayList<Datum> data1 = new ArrayList<>();
					ArrayList<Datum> data2 = new ArrayList<>();
					for (Datum datum : datalist){
						if (datum.x[a] >= t){
							data1.add(datum);
						} else {
							data2.add(datum);
						}
					}
					double enthropy1 = calcEntropy(data1);
					double enthropy2 = calcEntropy(data2);
					double current_avg_enth = (enthropy1 * data1.size() + enthropy2 * data2.size()) / datalist.size();
					if (best_avg_enthropy > current_avg_enth) {
						best_avg_enthropy = current_avg_enth;
						best_attr = a;
						best_threshold = t;
					}
				}
				j++;
				if (j == datalist.size()) {
					double[] sol = new double[2];
					sol[0] = best_attr;
					sol[1] = best_threshold;
					return sol;
				}
			}
			double[] sol = new double[2];
			sol[0] = best_attr;
			sol[1] = best_threshold;
			return sol;
		}

		// this method takes in a datalist (ArrayList of type datum). It returns the calling DTNode object
		// as the root of a decision tree trained using the datapoints present in the datalist variable and minSizeDatalist.
		// Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist) {

			//ADD CODE HERE
			if (datalist.size() < minSizeDatalist) {
				this.leaf = true;
				this.label = findMajority(datalist);
				return this;
			} else {
				boolean check = true;
				int firstLabel = datalist.getFirst().y;
				if (datalist.isEmpty()){
					this.leaf = true;
					this.label = datalist.getFirst().y;
					return this;
				}
				for (Datum datum : datalist) {
					if (datum.y != firstLabel) {
						check = false;
					}
				}
				if (check) {
					this.leaf = true;
					this.label = datalist.getFirst().y;
					return this;
				}
				this.attribute = (int) findBestSplit(datalist)[0];
				this.threshold = findBestSplit(datalist)[1];
				this.leaf = false;
				ArrayList<Datum> data1 = new ArrayList<>();
				ArrayList<Datum> data2 = new ArrayList<>();
				for (Datum datum : datalist){
					if (datum.x[this.attribute] < this.threshold){
						data1.add(datum);
					} else {
						data2.add(datum);
					}
				}
				this.left = (new DTNode()).fillDTNode(data1);
				this.right = (new DTNode()).fillDTNode(data2);
				return this;
			}
		}


		// This is a helper method. Given a datalist, this method returns the label that has the most
		// occurrences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist) {

			int [] votes = new int[2];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}

			if (votes[0] >= votes[1])
				return 0;
			else
				return 1;
		}




		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {

			//ADD CODE HERE
			if (this.leaf){
				return this.label;
			} else {
				if (xQuery[attribute] < threshold){
					return left.classifyAtNode(xQuery);
				} else {
					return right.classifyAtNode(xQuery);
				}
			}
		}


		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		public boolean equals(Object dt2)
		{

			//ADD CODE HERE
			if (this == dt2){
				return true;
			}
			else if (dt2 == null || getClass() != dt2.getClass()){
				return false;
			}
			DTNode otherNode = (DTNode) dt2;
			if (this.leaf && otherNode.leaf) {
				// For leaf nodes, compare labels
				return this.label == otherNode.label;
			} else if (!this.leaf && !otherNode.leaf) {
				// For internal nodes, compare attributes, thresholds, and child nodes recursively
				return this.attribute == otherNode.attribute
						&& Double.compare(this.threshold, otherNode.threshold) == 0
						&& java.util.Objects.equals(left, otherNode.left)
						&& java.util.Objects.equals(right, otherNode.right);
			}


			return false; //dummy code.  Update while completing the assignment.
		}
	}



	//Given a dataset, this returns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist) {
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		return this.rootDTNode.classifyAtNode( xQuery );
	}

	// Checks the performance of a DecisionTree on a dataset
	// This method is provided in case you would like to compare your
	// results with the reference values provided in the PDF in the Data
	// section of the PDF
	String checkPerformance( ArrayList<Datum> datalist) {
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

}
