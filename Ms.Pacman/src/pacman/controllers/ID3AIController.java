package pacman.controllers;

import java.util.List;
import java.util.Random;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.controllers.Controller;

public class ID3AIController extends Controller<MOVE>{

	private Random rnd=new Random();
	private MOVE[] allMoves=MOVE.values();
	
	public Node GenerateTree() {
		Node RootNode = new Node();
		
		
		return RootNode;
	}
	
	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
	public MOVE getMove(Game game,long timeDue)
	{
		return allMoves[rnd.nextInt(allMoves.length)];
	}
	
	private class Node {
		
		private List<Node> NodeList;
		
		
		
	}
}