package pacman.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import dataRecording.DataSaverLoader;
import dataRecording.DataTuple;
import dataRecording.DataTuple.DiscreteTag;

import java.util.Random;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.controllers.Controller;

public class ID3AIController extends Controller<MOVE>{

	private Random rnd=new Random();
	private MOVE[] allMoves=MOVE.values();
	private List <String> attributeList = new ArrayList<String>();
	private Node RootNode = new Node();
	private DataTuple[] DataSet=DataSaverLoader.LoadPacManData();
	
	
	public Node GenerateTree(DataTuple[] DataSet) {
		Node node= new Node();
		
		if (isAllMovesSame(DataSet)) {
			node.setLeaf(true);
			node.setLabel(DataSet[0].getDirectionChosen());
			return node;
		}
		if (attributeList.size()==0){
			node.setLeaf(true);
			node.setLabel(FindMajorityMove(DataSet));
			return node;
			}
		String attribute=SelectAttribute(DataSet, attributeList);
			
		}
		
		
	public boolean isAllMovesSame(DataTuple[] DataSet) {
		MOVE outCome =DataSet[0].getDirectionChosen();
	
		boolean isAllSame=true;
		for (int i=1; i<DataSet.length;i++) {
			if(DataSet[i].getDirectionChosen()!=outCome) {
				isAllSame=false;
				break;
			}
		}
		return isAllSame;
	}
	public MOVE FindMajorityMove(DataTuple[] DataSet) {
		int sumLeft,sumRight,sumUp,sumDown,sumNeutral;
		sumLeft=0;sumRight=0;sumUp=0;sumDown=0;sumNeutral=0;
		int maxVal=0;
		MOVE MajorityMove=MOVE.NEUTRAL;
		for (int i=0; i<DataSet.length;i++) {
			MOVE outCome =DataSet[i].getDirectionChosen();
			if (outCome==MOVE.LEFT) {
				sumLeft++;
				if (sumLeft>maxVal) {
					maxVal=sumLeft;
					MajorityMove=MOVE.LEFT;
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
	public void PreprocessingData(DataTuple[] Dataset) {
		
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
		private MOVE label;
		private List<Node> NodeList;
		public boolean isLeaf() {
			return isLeaf;
		}
		public void setLeaf(boolean isLeaf) {
			this.isLeaf = isLeaf;
		}
		public MOVE getLabel() {
			return label;
		}
		public void setLabel(MOVE move) {
			this.label = move;
		}
		public List<Node> getNodeList() {
			return NodeList;
		}
		public void setNodeList(List<Node> nodeList) {
			NodeList = nodeList;
		}	
		
	}
	

	public static void main(String[] args) {
		ID3AIController cont=new ID3AIController();
		cont.GenerateTree();
	}
}