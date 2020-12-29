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
	//This should be passed down with every iteration. right now it is not
	private Node RootNode;
	
	
	
	public Node GenerateTree(Node node,List <String> oldAttributeList) {
		List<LinkedHashMap> processedList=node.getDataSet();
		List<String> attributeList = new ArrayList<>(oldAttributeList);
		if (isAllMovesSame(processedList)) {

			node.setLabel(processedList.get(0).get("Direction").toString());
			node.setLeaf(true);
			return node;
		}
		if (attributeList.size() == 1){

			node.setLabel(FindMajorityMove(processedList));
			node.setLeaf(true);
			return node;
			}

		//System.out.println(attributeList);
		String currentAttribute= SelectAttribute(processedList, attributeList);// attributeList.get(0);

		node.setLabel(currentAttribute); // change to get the one with least entropy
		
		attributeList.remove(0);
		System.out.println(currentAttribute);
		List<Node> childNodes = CreateChildNodes(processedList,currentAttribute);
		node.setChildren(childNodes);
		for (Node child:childNodes) {
			GenerateTree(child,attributeList);
		}
		return node;
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
		//System.out.println(currentAttribute);
		SubSets=splitOnAttribute(mapList,currentAttribute);
		System.out.println("Splitting on attribute" + currentAttribute);
		//System.out.println(SubSets.size());
		for (List<LinkedHashMap> SubSet : SubSets) {
			ChildNodes.add(new Node(SubSet));	
		}
		return ChildNodes;
	}
	
	public List<List<LinkedHashMap>> splitOnAttribute(List<LinkedHashMap> mapList,String currentAttribute) {
		List<List<LinkedHashMap>> SubSets = new ArrayList<List<LinkedHashMap>>();
		
		//System.out.println(currentAttribute);

		for (LinkedHashMap map : mapList) {
			boolean added = false;
			String mapValue = map.get(currentAttribute).toString();
			//System.out.println(mapList.size());
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
		double dataEntropy = 0;	
		LinkedHashMap<String, Integer> subMap = getTargetMap(processedList);
		
		dataSetEntropy = CalcEntropy(subMap, (double)subMap.size(), (double)processedList.size());
		System.out.println("Total entropy of dataset: " + Double.toString(dataSetEntropy));
		LinkedHashMap<String, Integer> targetMap;
		List<List<LinkedHashMap>> SubSets;
		String bestGain = attributeList.get(0);
		double maxGain=Double.MIN_VALUE;
		for (String attribute : attributeList) {
			if(attribute != "Direction") {
				SubSets=splitOnAttribute(processedList,attribute);
				double mean=0;
				for (List<LinkedHashMap> subset: SubSets) {
					targetMap=getTargetMap(subset);
					mean+=CalcInformationGain(targetMap, (double)targetMap.size(), (double)subset.size());
				}
				System.out.println("Attribute gain: " + Double.toString(dataSetEntropy - mean) + ", from attribute: " + attribute);
	
				//mean=mean/SubSets.size();//Borde man räkna ut totentropy-denna entropy? o sätta som gain? eller onödigt?
				if ((dataSetEntropy - mean) > maxGain){
					maxGain=dataSetEntropy - mean;
					bestGain=attribute;
				}
			}
		}
	
		System.out.println(bestGain);
		return bestGain;	
	}
		
	public double CalcEntropy(LinkedHashMap<String, Integer> subMap, double nbrOfPossibilities, double dataSize) {
		double Entropysum = 0;
		for(String key : subMap.keySet()) {
			double quota = (double)subMap.get(key)/dataSize;
			Entropysum -= quota * (Math.log10(quota)/Math.log10(2));
		}
		//return nbrOfPossibilities/dataSize*Entropysum;
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
		attributeList.add("InkyEdible");
		attributeList.add("PinkyEdible");
		attributeList.add("SueEdible");
		attributeList.add("Direction");
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
			map.put("InkyEdible",Boolean.toString(DataSet[i].isInkyEdible()));
			map.put("SueEdible",Boolean.toString(DataSet[i].isSueEdible()));
			map.put("PinkyEdible",Boolean.toString(DataSet[i].isPinkyEdible()));
			map.put("Direction",DataSet[i].getDirectionChosen().toString());
			processedList.add(map);
		}
		return processedList;
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
		private List<Node> ChildNodes=new ArrayList<Node>();;
		private List<LinkedHashMap> DataSet=new ArrayList<LinkedHashMap>();
		
		public Node(List<LinkedHashMap> subSet) {
			this.DataSet=subSet;
			//System.out.println("New Node Created");
			//System.out.println(DataSet.get(0).get("PinkyDist"));
	
		}
		
		public void setChildren(List<Node> childNodes) {
			this.ChildNodes=childNodes;
			
		}

		public boolean isLeaf() {
			return isLeaf;
		}
		public void setLeaf(boolean isLeaf) {
			this.isLeaf = isLeaf;
			//System.out.println("Leaf " + label);
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
		System.out.println(-0.3* (Math.log10(0.3)/Math.log10(2)));
		final DataTuple[] DataSet=DataSaverLoader.LoadPacManData();
		//private List<LinkedHashMap> processedList=new ArrayList<LinkedHashMap>();
		ID3AIController cont=new ID3AIController();
		List<LinkedHashMap>processedList= cont.PreprocessingData(DataSet);
		List<String>attributeList=cont.setupAttributes();
		Node Root=cont.setandgetroot(processedList);
		cont.GenerateTree(Root,attributeList);
		
	}
}