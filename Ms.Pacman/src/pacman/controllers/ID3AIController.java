package pacman.controllers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import dataRecording.DataSaverLoader;
import dataRecording.DataTuple;
import dataRecording.DataTuple.DiscreteTag;

import java.util.Random;
import java.util.function.Function;

import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.controllers.Controller;

public class ID3AIController extends Controller<MOVE>{

	private Random rnd=new Random();
	private MOVE[] allMoves=MOVE.values();
	private int nbrOfNodes=1;
	private Node RootNode;
	
	public Node GenerateTree(Node node,List <String> oldAttributeList) {
		List<LinkedHashMap> processedList=node.getDataSet();
		List<String> attributeList = new ArrayList<>(oldAttributeList);
		if (isAllMovesSame(processedList)) {
			System.out.println("Perfectly categorized leaf:");
			node.setLabel(processedList.get(0).get("Direction").toString());
			node.setLeaf(true);
			return node;
		}
		if (attributeList.size() == 0){
			System.out.println("Leaf categorized by majority: ");
			node.setLabel(FindMajorityMove(processedList));
			node.setLeaf(true);
			return node;
			}

		String currentAttribute= SelectAttribute(processedList, attributeList);// attributeList.get(0);
		node.setLabel(currentAttribute); // change to get the one with least entropy 
		attributeList.remove(currentAttribute);
		System.out.println("-------");
		System.out.println("Node Label: " + currentAttribute);
		List<Node> childNodes = CreateChildNodes(processedList,currentAttribute);
		System.out.println("Creating: " + childNodes.size() + " children");
		for (Node child : childNodes) {
			System.out.println(child.getSplitValue());
		}
		System.out.println("-------");
		nbrOfNodes+=childNodes.size();
		node.setChildren(childNodes);
		for (Node child:childNodes) {
			GenerateTree(child,attributeList);
		}
		return node;
		}	
	
	public String TraverseTree(Node node, LinkedHashMap dataMap) {
		String attribute = node.getLabel();
		List <Node> nodeList = node.getNodeList(); // current nodes children
		Node childNode = null;
		if (node.isLeaf()) {
			return attribute;
		}
		
		for (Node child : nodeList) {
			if (child.getSplitValue() == dataMap.get(attribute)) {
				childNode = child;
				break;
			}			
		}
		if (childNode == null) {
			System.out.println("Path not in training dataset");
			return null;
		}
		return TraverseTree(childNode, dataMap);
	}
	
	public Node setandgetroot(List<LinkedHashMap> processedList) {
		RootNode=new Node(processedList);
		return RootNode;
	}
	public boolean isAllMovesSame(List<LinkedHashMap> processedList) {
		String outCome =processedList.get(0).get("Direction").toString();
		boolean isAllSame=true;
		for (int i=1; i<processedList.size();i++) {
			if(!processedList.get(i).get("Direction").toString().equals(outCome)) {
				isAllSame=false;
				break;
			}
		}
		return isAllSame;
	}
	public List<Node> CreateChildNodes(List<LinkedHashMap> mapList,String currentAttribute) {
		List<List<LinkedHashMap>> SubSets;
		List <Node> ChildNodes=new ArrayList<Node>();
		SubSets=splitOnAttribute(mapList,currentAttribute);
		for (List<LinkedHashMap> SubSet : SubSets) {
			Node node = new Node(SubSet);
			node.setSplitValue(SubSet.get(0).get(currentAttribute).toString());
			ChildNodes.add(node);
		}
		return ChildNodes;
	}
	
	public List<List<LinkedHashMap>> splitOnAttribute(List<LinkedHashMap> dataSet,String currentAttribute) {
		List<List<LinkedHashMap>> SubSets = new ArrayList<List<LinkedHashMap>>();
		for (LinkedHashMap map : dataSet) {
			boolean added = false;
			String mapValue = map.get(currentAttribute).toString();
			for (List<LinkedHashMap> SubSet : SubSets) {
				if (SubSet.get(0) != null) {
					if (SubSet.get(0).get(currentAttribute).toString().equals(mapValue)) {
						SubSet.add(map);
						added = true;
						break;
					}
				}
			}
			if (added == false) {
				List<LinkedHashMap> subset = new ArrayList<LinkedHashMap>();
				subset.add(map);
				SubSets.add(subset);
			}

		}
		return SubSets;
	}
	
	public String FindMajorityMove(List<LinkedHashMap> processedList) {
		int sumLeft,sumRight,sumUp,sumDown,sumNeutral;
		sumLeft=0;sumRight=0;sumUp=0;sumDown=0;sumNeutral=0;
		int maxVal=0;
		String MajorityMove="NEUTRAL";
		for (int i=0; i<processedList.size();i++) {
			String outCome =processedList.get(i).get("Direction").toString();
			if (outCome.equals("LEFT")) {
				sumLeft++;
				if (sumLeft>maxVal) {
					maxVal=sumLeft;
					MajorityMove="LEFT";
				}
			}else if (outCome.equals("RIGHT")) {
				sumRight++;
				if (sumRight>maxVal) {
					maxVal=sumRight;
					MajorityMove="RIGHT";
				}
			}else if (outCome.equals("UP")) {
				sumUp++;
				if (sumUp>maxVal) {
					maxVal=sumUp;
					MajorityMove="UP";
				}
			}else if(outCome.equals("DOWN")) {
				sumDown++;
				if (sumDown>maxVal) {
					maxVal=sumDown;
					MajorityMove="DOWN";
				}
			}else {
				sumNeutral++;
				if (sumNeutral>maxVal) {
					maxVal=sumNeutral;
					MajorityMove="NEUTRAL";
				}
			}
			
		}
		return MajorityMove;

	}
	public String SelectAttribute(List<LinkedHashMap> processedList, List<String> attributeList) {	
		double dataSetEntropy = 0;
		LinkedHashMap<String, Integer> subMap = getTargetMap(processedList);
		dataSetEntropy = CalcEntropy(subMap, (double)subMap.size(), (double)processedList.size());
		//System.out.println("Total entropy of dataset: " + Double.toString(dataSetEntropy));
		LinkedHashMap<String, Integer> targetMap;
		List<List<LinkedHashMap>> SubSets;
		String bestGain = attributeList.get(0);
		double maxGain=Double.MIN_VALUE;
		double gain;
		for (String attribute : attributeList) {
			if(attribute != "Direction") {
				SubSets=splitOnAttribute(processedList,attribute);
				double meanEntropy=0;
				for (List<LinkedHashMap> subset: SubSets) {
					targetMap=getTargetMap(subset);
					meanEntropy+=CalcInformationGain(targetMap, (double)targetMap.size(), (double)subset.size());
				}
				gain=dataSetEntropy-meanEntropy;
				//System.out.println("Attribute mean: " + Double.toString(gain) + ", from attribute: " + attribute);
				if (gain > maxGain){
					maxGain= meanEntropy;
					bestGain=attribute;
				}
			}
		}
		return bestGain;	
	}
		
	public double CalcEntropy(LinkedHashMap<String, Integer> subMap, double nbrOfPossibilities, double dataSize) {
		double Entropysum = 0;
		for(String key : subMap.keySet()) {
			double quota = (double)subMap.get(key)/dataSize;
			Entropysum -= quota * (Math.log10(quota)/Math.log10(2));
		}
		return Entropysum;
	} 
	
	public double CalcInformationGain (LinkedHashMap<String, Integer> subMap, double nbrOfPossibilities, double dataSize) {
		double InformationGainSum = 0;
		for(String key : subMap.keySet()) {
			double quota = (double)subMap.get(key)/dataSize;
			InformationGainSum -= quota * (Math.log10(quota)/Math.log10(2));
		}
		return nbrOfPossibilities/dataSize*InformationGainSum;
	}
	public LinkedHashMap<String, Integer> getTargetMap(List<LinkedHashMap> processedList) {
		LinkedHashMap<String, Integer> targetMap = new LinkedHashMap<String, Integer>();
		for (LinkedHashMap map: processedList) {
			String currentValue = (String)map.get("Direction");
			if (targetMap.containsKey(currentValue)) {
				int intVal = targetMap.get(currentValue);
				intVal = intVal + 1;
				targetMap.put(currentValue, intVal);
			}
			else {
				targetMap.put(currentValue, 1);
			}
		}
		return targetMap;
	}

	public List<String> setupAttributes(){
		List <String> attributeList=new ArrayList<String>();
		attributeList.add("PinkyDist");
		attributeList.add("InkyDist");
		attributeList.add("BlinkyDist");
		attributeList.add("SueDist");
		attributeList.add("PinkyDir");
		attributeList.add("InkyDir");
		attributeList.add("BlinkyDir");
		attributeList.add("SueDir");
		attributeList.add("BlinkyEdible");
		//attributeList.add("Direction");
		return attributeList;
	}
	
	public List<LinkedHashMap> PreprocessingData(DataTuple[] DataSet) {
		List<LinkedHashMap> processedList=new ArrayList<LinkedHashMap>();
		for (int i=0;i<DataSet.length;i++) {
			LinkedHashMap<String,String> map=new LinkedHashMap<String, String>();
			map.put("PinkyDist",DataSet[i].getPinkyDist().toString());
			map.put("BlinkyDist", DataSet[i].getBlinkyDist().toString());
			map.put("SueDist", DataSet[i].getSueDist().toString());
			map.put("InkyDist", DataSet[i].getInkyDist().toString());
			map.put("InkyDir", DataSet[i].getInkyDir().toString());
			map.put("PinkyDir", DataSet[i].getPinkyDir().toString());
			map.put("BlinkyDir", DataSet[i].getInkyDir().toString());
			map.put("SueDir", DataSet[i].getSueDir().toString());
			map.put("BlinkyEdible",Boolean.toString(DataSet[i].isBlinkyEdible()));
			map.put("Direction",DataSet[i].getDirectionChosen().toString());
			processedList.add(map);
		}
		return processedList;
	}
	
	
	
	public int getNbrOfNodes() {
		return nbrOfNodes;
	}
	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
	public MOVE getMove(Game game,long timeDue)
	{
		return allMoves[rnd.nextInt(allMoves.length)];
	}
	
	private class Node {
		private boolean isLeaf=false;
		private String label;
		private String splitValue;
		private List<Node> ChildNodes=new ArrayList<Node>();;
		private List<LinkedHashMap> DataSet=new ArrayList<LinkedHashMap>();
		
		public Node(List<LinkedHashMap> subSet) {
			this.DataSet=subSet;
			//System.out.println("New Node Created");
			//System.out.println(DataSet.get(0).get("PinkyDist"));
	
		}
		
		public String getSplitValue() {
			return splitValue;
		}
		
		public void setSplitValue(String splitVal) {
			splitValue = splitVal;
		}
		
		
		public void setChildren(List<Node> childNodes) {
			this.ChildNodes=childNodes;
			
		}

		public boolean isLeaf() {
			return isLeaf;
		}
		public void setLeaf(boolean isLeaf) {
			this.isLeaf = isLeaf;
			System.out.println("Leaf: " + label);
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String string) {
			this.label = string;
		}
		public List<Node> getNodeList() {
			return ChildNodes;
		}
		public List<LinkedHashMap> getDataSet() {
			return this.DataSet;
		}
	
		
		
		
	}
	

	

	public static void main(String[] args) {
		
		final DataTuple[] DataSet=DataSaverLoader.LoadPacManData();
		final DataTuple[] TrainingData = new DataTuple [3351];
		final DataTuple[] TestData = new DataTuple [837];
		
		for (int i = 0; i < TrainingData.length; i++) {
			TrainingData[i] = DataSet[i];
		}
		for (int i = 0; i < TestData.length; i++) {
			TestData[i] = DataSet[3351 + i];
		}
		

		//private List<LinkedHashMap> processedList=new ArrayList<LinkedHashMap>();
		ID3AIController cont=new ID3AIController();
		List<LinkedHashMap>processedTrainingData= cont.PreprocessingData(TrainingData);
		List<LinkedHashMap>processedTestData= cont.PreprocessingData(TestData);
		List<String>attributeList=cont.setupAttributes();	
		Node Root=cont.setandgetroot(processedTrainingData);
		cont.GenerateTree(Root,attributeList);
		System.out.println("Number of attributes: " + attributeList.size() );
		System.out.println("Number of created nodes: "+ cont.getNbrOfNodes());
		int Correct = 0;
		int Incorrect = 0;
		
		
		for (int i = 0; i < processedTestData.size(); i++) {
			String TraverseResult = cont.TraverseTree(Root ,processedTestData.get(i));
			if (TraverseResult != null ) {
				if (TraverseResult.equals(processedTestData.get(i).get("Direction"))) {
					Correct++;
				}
				else {
					Incorrect++;
				}
			}else {
			//	Incorrect++;
			}
		}
		System.out.println("Correct: " + Correct);
		System.out.println("Incorrect: " + Incorrect);
	}
}