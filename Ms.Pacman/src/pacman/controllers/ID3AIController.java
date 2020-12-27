package pacman.controllers;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	private List <Function> attributeList=new ArrayList<Function>();
	private Node RootNode = new Node();
	
	
	
	public Node GenerateTree(List<HashMap> processedList) {
		Node node= new Node();
		
		if (isAllMovesSame(processedList)) {
			node.setLeaf(true);
			node.setLabel(processedList.get(0).get("Direction").toString());
			return node;
		}
		if (attributeList.size()==0){
			node.setLeaf(true);
			node.setLabel(FindMajorityMove(processedList));
			return node;
			}
		
		//String attribute=SelectAttribute(DataSet, attributeList);
		//String CurrentAttribute= attributeList.get(0);
		return node;
		}
		
		
	public boolean isAllMovesSame(List<HashMap> processedList) {
		String outCome =processedList.get(0).get("Direction").toString();
		//System.out.println(processedList.get(0).get("Direction").getClass());
		boolean isAllSame=true;
		
		for (int i=1; i<processedList.size();i++) {
			if(!processedList.get(0).get("Direction").toString().equals(outCome)) {
				isAllSame=false;
				break;
			}
		}
		return isAllSame;
	}
	public String FindMajorityMove(ArrayList<HashMap> processedList) {
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
			}else if (outCome==MOVE.RIGHT) {
				sumRight++;
				if (sumRight>maxVal) {
					maxVal=sumRight;
					MajorityMove=MOVE.RIGHT;
				}
			}else if (outCome==MOVE.UP) {
				sumUp++;
				if (sumUp>maxVal) {
					maxVal=sumUp;
					MajorityMove=MOVE.UP;
				}
			}else if(outCome==MOVE.DOWN) {
				sumDown++;
				if (sumDown>maxVal) {
					maxVal=sumDown;
					MajorityMove=MOVE.DOWN;
				}
			}else {
				sumNeutral++;
				if (sumNeutral>maxVal) {
					maxVal=sumNeutral;
					MajorityMove=MOVE.NEUTRAL;
				}
			}
			
		}
		return MajorityMove;

	}
	public int SelectAttribute(DataTuple [] DataSet, List<String> attributeList) {
		int nbrPossible;
		int [] subAttributes = null;
		for (String attribute: attributeList) {
			
			
			switch (attribute) {
			case "SueDir":
				subAttributes = new int [4];
				for (DataTuple dataRow: DataSet) {
					subAttributes[dataRow.getSueDir().ordinal()]++;
				}
			case "InkyDir":
				subAttributes = new int [4];
				for (DataTuple dataRow: DataSet) {
					subAttributes[dataRow.getInkyDir().ordinal()]++;
				}
			case "BlinkyDir":
				subAttributes = new int [4];
				for (DataTuple dataRow: DataSet) {
					subAttributes[dataRow.getBlinkyDir().ordinal()]++;
				}
			case "PinkyDir":
				subAttributes = new int [4];
				for (DataTuple dataRow: DataSet) {
					subAttributes[dataRow.getPinkyDir().ordinal()]++;
				}
			case "SueDist":
				subAttributes = new int [5];
				for (DataTuple dataRow: DataSet) {
					subAttributes[(dataRow.getSueDist().ordinal())]++;
				}
			case "InkyDist":
				subAttributes = new int [5];
				for (DataTuple dataRow: DataSet) {
					subAttributes[dataRow.getInkyDist().ordinal()]++;
				}
			case "BlinkyDist":
				subAttributes = new int [5];
				for (DataTuple dataRow: DataSet) {
					subAttributes[dataRow.getBlinkyDir().ordinal()]++;
				}
			case "PinkyDist":
				subAttributes = new int [5];
				for (DataTuple dataRow: DataSet) {
					subAttributes[dataRow.getPinkyDir().ordinal()]++;
				}
			default:
				subAttributes = new int [0];
			}
		}
		double Entropy = CalcEntropy(subAttributes, subAttributes.length, DataSet.length);
		return 1;	
	}
	
	public double CalcEntropy(int [] attributeFrequency, int nbrOfPossibilities, int dataSize) {
		double sum = 0;
		for(int i = 0; i < attributeFrequency.length; i++) {
			sum -= ((double) attributeFrequency[i]/nbrOfPossibilities) * Math.log10((double)attributeFrequency[i]/nbrOfPossibilities*Math.log10(2));
		}
		return nbrOfPossibilities/dataSize*sum;
	}

	
	
	public List<HashMap> PreprocessingData(DataTuple[] DataSet) {
		
		List<HashMap> processedList=new ArrayList<HashMap>();
		for (int i=0;i<DataSet.length;i++) {
			HashMap<String,String> map=new HashMap<String, String>();
			map.put("PinkyDist",DataSet[i].getPinkyDist().toString());
			map.put("InkyDist", DataSet[i].getInkyDist().toString());
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
			map.put("InkyEdible",Boolean.toString(DataSet[i].isInkyEdible()));
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
		private List<Node> NodeList;
		public boolean isLeaf() {
			return isLeaf;
		}
		public void setLeaf(boolean isLeaf) {
			this.isLeaf = isLeaf;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String string) {
			this.label = string;
		}
		public List<Node> getNodeList() {
			return NodeList;
		}
		public void setNodeList(List<Node> nodeList) {
			NodeList = nodeList;
		}	
	
	}
	

	

	public static void main(String[] args) {
		final DataTuple[] DataSet=DataSaverLoader.LoadPacManData();
		//private List<HashMap> processedList=new ArrayList<HashMap>();
		ID3AIController cont=new ID3AIController();
		List<HashMap>processedList= cont.PreprocessingData(DataSet);
		cont.GenerateTree(processedList);
		
		
	}
}