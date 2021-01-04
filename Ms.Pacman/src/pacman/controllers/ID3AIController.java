package pacman.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import dataRecording.DataSaverLoader;
import dataRecording.DataTuple;
import dataRecording.DataTuple.DiscreteTag;

import java.util.Random;
import java.util.function.Function;

import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.controllers.Controller;

public class ID3AIController extends Controller<MOVE>{

	private Random rnd=new Random();
	private MOVE[] allMoves=MOVE.values();
	private int nbrOfNodes=1;
	private Node RootNode;
	private MOVE move = MOVE.RIGHT;
	private static FileWriter writer;
	
	public Node GenerateTree(Node node,List <String> oldAttributeList, int depth) {
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
		node.setLabel(currentAttribute);
		attributeList.remove(currentAttribute);
		System.out.println("--");
		System.out.println("Node Label: " + currentAttribute);
		List<Node> childNodes = CreateChildNodes(processedList,currentAttribute);
		System.out.println("Creating: " + childNodes.size() + " children");
		for (Node child : childNodes) {
			System.out.println(child.getSplitValue());
		}
		System.out.println("--");
		nbrOfNodes+=childNodes.size();
		node.setChildren(childNodes);
		WriteTree("Node Label: " + node.getLabel() + ", value: " + node.getSplitValue(), depth );
		for (Node child:childNodes) {
			GenerateTree(child,attributeList, depth + 1);
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
			childNode = nodeList.get((int)Math.random()*((nodeList.size())));
			System.out.println("Path not in training dataset");
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
		attributeList.add("PinkyEdible");
		attributeList.add("InkyEdible");
		attributeList.add("SueEdible");
		
		//attributeList.add("Direction");
		return attributeList;
	}
	
	public List<LinkedHashMap> PreprocessingData(List<DataTuple> trainingData) {
		List<LinkedHashMap> processedList=new ArrayList<LinkedHashMap>();
		for (DataTuple dataTuple : trainingData) {
			LinkedHashMap<String,String> map=new LinkedHashMap<String, String>();
			map.put("PinkyDist",dataTuple.getPinkyDist().toString());
			map.put("BlinkyDist", dataTuple.getBlinkyDist().toString());
			map.put("SueDist", dataTuple.getSueDist().toString());
			map.put("InkyDist", dataTuple.getInkyDist().toString());
			map.put("InkyDir", dataTuple.getInkyDir().toString());
			map.put("PinkyDir", dataTuple.getPinkyDir().toString());
			map.put("BlinkyDir", dataTuple.getInkyDir().toString());
			map.put("SueDir", dataTuple.getSueDir().toString());
			map.put("BlinkyEdible",Boolean.toString(dataTuple.isBlinkyEdible()));
			map.put("InkyEdible",Boolean.toString(dataTuple.isInkyEdible()));
			map.put("PinkyEdible",Boolean.toString(dataTuple.isPinkyEdible()));
			map.put("SueEdible",Boolean.toString(dataTuple.isSueEdible()));
			map.put("Direction",dataTuple.getDirectionChosen().toString());
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
		LinkedHashMap<String,String> map=new LinkedHashMap<String, String>();
		map.put("PinkyDist",(discretizeDistance(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.PINKY)))).toString());
		map.put("BlinkyDist", (discretizeDistance(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.BLINKY)))).toString());
		map.put("SueDist", (discretizeDistance(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.SUE)))).toString());
		map.put("InkyDist", (discretizeDistance(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.INKY)))).toString());
		map.put("InkyDir", game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.INKY), DM.PATH).toString());
		map.put("PinkyDir", game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.PINKY), DM.PATH).toString());
		map.put("BlinkyDir",game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.BLINKY), DM.PATH).toString());
		map.put("SueDir", game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.SUE), DM.PATH).toString());
		map.put("BlinkyEdible",Boolean.toString(game.isGhostEdible(GHOST.BLINKY)));
		map.put("InkyEdible",Boolean.toString(game.isGhostEdible(GHOST.INKY)));
		map.put("PinkyEdible",Boolean.toString(game.isGhostEdible(GHOST.PINKY)));
		map.put("SueEdible",Boolean.toString(game.isGhostEdible(GHOST.SUE)));
		String TraverseResult = TraverseTree(RootNode,map);
		
		if (TraverseResult == null) {
			move = MOVE.NEUTRAL;
			return move;
		/**	int current=game.getPacmanCurrentNodeIndex();
			int closestGhostDistance = 1000;
			GHOST closestGhost = GHOST.PINKY;
			for(GHOST ghost : GHOST.values()) {
	            //if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
	                if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<closestGhostDistance) {
	                	closestGhost = ghost;
	                	closestGhostDistance = game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));
	                }
			}
	                    return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(closestGhost),DM.PATH);
	       **/             
		}
		//System.out.println(TraverseResult);
		
		if (TraverseResult.equals("LEFT")) {
			System.out.println("LEFT");
			move = MOVE.LEFT;
		}
		else if (TraverseResult.equals("RIGHT")) {
			System.out.println("RIGHT");
			move = MOVE.RIGHT;
		}
		else if (TraverseResult.equals("UP")) {
			System.out.println("UP");
			move = MOVE.UP;
		}
		else if (TraverseResult.equals("DOWN")) {
			System.out.println("DOWN");
			move = MOVE.DOWN;
		}
		else {
			System.out.println("NEUTRAL");
			move = MOVE.NEUTRAL;
		}
		return move;
	}
	
	public class Node {
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
	
	public enum DiscreteTag {
		VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH, NONE;

		public static DiscreteTag DiscretizeDouble(double aux) {
			if (aux < 0.1)
				return DiscreteTag.VERY_LOW;
			else if (aux <= 0.3)
				return DiscreteTag.LOW;
			else if (aux <= 0.5)
				return DiscreteTag.MEDIUM;
			else if (aux <= 0.7)
				return DiscreteTag.HIGH;
			else
				return DiscreteTag.VERY_HIGH;
		}
	}
	
	public DiscreteTag discretizeDistance(int dist) {
		if (dist == -1)
			return DiscreteTag.NONE;
		double aux = this.normalizeDistance(dist);
		return DiscreteTag.DiscretizeDouble(aux);
	}
	
	public double normalizeDistance(int dist) {
		return ((dist - 0) / (double) (150 - 0)) * (1 - 0) + 0;
	}
	
	public static void CreateFile() {
	    try {
	        File myObj = new File("myTreeFile.txt");
	        if (myObj.createNewFile()) {
	          System.out.println("File created: " + myObj.getName());
	        } else {
	          System.out.println("File already exists.");
	        }
	      } catch (IOException e) {
	        System.out.println("An error occurred.");
	        e.printStackTrace();
	      }
	    
    	try {
    	      writer = new FileWriter("myTreeFile.txt");
    	    } catch (IOException e) {
    	      System.out.println("An error occurred.");
    	      e.printStackTrace();
    	    }
    }
	
	public void WriteTree(String text, int depth) {
		StringBuilder stringBuilder = new StringBuilder();
		
		for (int i = 0; i < depth; i++ ) {
			stringBuilder.append("            ");
		}
		stringBuilder.append(text); 
		try {
			writer.write(stringBuilder.toString() + "\n");
			
		} catch (IOException e) {
  	      System.out.println("An error occurred while writing with stringBuilder.");
  	      e.printStackTrace();
  	    }
	}
	
	
	

	public static void main(String[] args) {
		
		CreateFile();
		
		final DataTuple[] DataSet=DataSaverLoader.LoadPacManData();
		final List<DataTuple> TrainingData = new ArrayList<DataTuple>();
		final List<DataTuple>  TestData = new ArrayList<DataTuple>();
		
		for (int i = 0; i <  DataSet.length; i++) {
			if ((i%5)==0) {
			TestData.add(DataSet[i]);
			}
			else {
				TrainingData.add(DataSet[i]);
			}
		}
		ID3AIController cont=new ID3AIController();
		List<LinkedHashMap>processedTrainingData= cont.PreprocessingData(TrainingData);
		List<LinkedHashMap>processedTestData= cont.PreprocessingData(TestData);
		List<String>attributeList=cont.setupAttributes();	
		Node Root=cont.setandgetroot(processedTrainingData);
		cont.GenerateTree(Root,attributeList, 0);
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
					//System.out.println("Result from tree: " + TraverseResult+ " Result from testdata: " +processedTestData.get(i).get("Direction"));
					Incorrect++;
				}
			}else {
				Incorrect++;
				//System.out.println("Result from tree: " + TraverseResult+ " Result from testdata: " +processedTestData.get(i).get("Direction"));
			}
		}
		System.out.println("Correct: " + Correct);
		System.out.println("Incorrect: " + Incorrect);
		System.out.println(("Correct percentage: " + ((double)Correct/(double)(Correct+Incorrect))*100) + "%" );

	}
}