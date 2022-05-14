package HanmaPlayer;
import mnkgame.*;
import java.util.Random;

public class Player implements mnkgame.MNKPlayer {

	private int myM;
	private int myN;
	private int myK;
	private boolean myFirst;
	private int myTimeout_in_secs;
	private Random rand;
	private MNKBoard bo;
	private MNKGameState myWin;
	private MNKGameState yourWin;
	private int TIMEOUT;
	private int myPoints;
	private int enemyPoints;
	private boolean turn;
	private boolean winnable;
	private int direction;
	private MNKCellState statoMio;
	private int iteratore;

	public Player() {
        
	}

    @Override
	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {

	//initializer of all needed values for the evaluate 
		rand = new Random(System.currentTimeMillis()); //random used for the first move when the board is empty
		bo = new MNKBoard(M,N,K); //board used locally for calculations 
		myM = M;
		myN = N;
		myFirst = first;
		if(myFirst) statoMio = MNKCellState.P1;
		else statoMio = MNKCellState.P2;
		myK = K;
		myTimeout_in_secs = timeout_in_secs;
		myPoints = 0; //estimated point value for every cell evaluated
		enemyPoints = 0; //estimated point value for every enemy cell evaluated 
		winnable = false; //Flag for every cell evaluated, true if the move can possibly lead into a win
		direction = 0; //directions for the evaluate check
		iteratore = 0;
	}
	
	public MNKCell upleft(MNKCell Attuale){ //moving the focused cell diagonally up-left 
		MNKCell A = new MNKCell(Attuale.i+1 , Attuale.j-1, Attuale.state);
		return A;
	} 

	public MNKCell upright(MNKCell Attuale){ //moving the focused cell diagonally up-right
		MNKCell A = new MNKCell(Attuale.i+1 , Attuale.j+1, Attuale.state);
		return A;
	} 

	public MNKCell upwards(MNKCell Attuale){ //moving the focused cell diagonally up
		MNKCell A = new MNKCell(Attuale.i+1 , Attuale.j, Attuale.state);
		return A;
	} 

	public MNKCell downwards(MNKCell Attuale){ //moving the focused cell diagonally down
		MNKCell A = new MNKCell(Attuale.i-1 , Attuale.j, Attuale.state);
		return A;
	}

	public MNKCell downleft(MNKCell Attuale){ //moving the focused cell diagonally down-left
		MNKCell A = new MNKCell(Attuale.i-1 , Attuale.j-1, Attuale.state);
		return A;
	}

	public MNKCell downright(MNKCell Attuale){ //moving the focused cell diagonally down-right
		MNKCell A = new MNKCell(Attuale.i-1 , Attuale.j+1, Attuale.state);
		return A;
	}

	public MNKCell right(MNKCell Attuale){ //moving the focused cell diagonally right
		MNKCell A = new MNKCell(Attuale.i , Attuale.j+1 , Attuale.state);
		return A;
	}

	public MNKCell left(MNKCell Attuale){ //moving the focused cell diagonally left
		MNKCell A = new MNKCell(Attuale.i , Attuale.j-1, Attuale.state);
		return A;
	}

	public int controllo(int dir, MNKCell[][] situa, MNKCell cella, int tot, int iterazione, int goal, MNKCellState giocatore, boolean stop ){
		// dir first value = 0
		// situa first value = situazione
		// cella first value = MC[lastvalue-1]
		// tot first value = 0
		// iterazione first value = 0
		// goal first value = 0 
		// giocatore first value = statoMio 
		// stop first value = false
		boolean bordo;
		int direction = dir;
		MNKCell cella_attuale = cella;
		int nowgoal = goal;
		int nowtot = tot;
		int iter = iterazione;
		int x = 0;
		boolean fermo = stop;
		MNKCell cella_prossima = new MNKCell(cella_attuale.i, cella_attuale.j, cella_attuale.state);
		{
			while(direction < 8){

				if(direction == 0){
					while (iter < myK-1){
 						if( cella_prossima.i+1 > 0 && cella_prossima.i+1 < myM && cella_prossima.j-1 > 0 && cella_prossima.j-1 < myN){

							cella_prossima = upleft(cella_attuale);

							if(cella_prossima.state == giocatore){
								if (fermo = true){
									nowgoal = goal + 1;
								}
								nowtot = tot + 2;
								iter = iter + 1;
								nowtot = controllo(direction, situa, cella_prossima, nowtot, iter, nowgoal, giocatore, fermo);
							}
							else if(cella_prossima.state == MNKCellState.FREE){
								nowtot = tot;
								fermo = true;
								iter = iter + 1;
								nowtot = controllo(direction, situa, cella_prossima, nowtot, iter, nowgoal, giocatore, fermo);
							}
							else{
								nowtot = tot + 1;
								fermo = false;
								direction = 7;
								iter = 0;
								nowtot = controllo(direction, situa, cella, tot, iter, goal, giocatore, fermo);
							 }
							
						 }
						else{
							fermo = false;
							direction = 7;
							iter = 0;
							nowtot = controllo(direction, situa, cella, tot, iter, goal, giocatore, fermo);
						}

					}
					fermo = false;
					direction = 7;
					iter = 0;
					nowtot = controllo(direction, situa, cella, tot, iter, goal, giocatore, fermo);

				}

			} return nowtot;
		}
	}


    @Override
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {

		long start = System.currentTimeMillis();//initialization of the timeout needed valor
		int fila = 0;
		boolean flag = false;

		// MxN matrix which combines in one single structure the infos from both FC and MC
		//doing so results in a matrix with localized cells that basically tell us the state of the whole game 
		//this structure will be used later to evaluate moves and analyze the cells spatially next to each other
		MNKCell[][] situazione = new MNKCell[myM][myN]; 
		for (int t = 0 ; t < FC.length ; t++){
			situazione[FC[t].i][FC[t].j] = FC[t];
		}
		for (int t = 0 ; t < MC.length ; t++){
			situazione[MC[t].i][MC[t].j] = MC[t];
		}

		// If there is just one possible move, return immediately
		if(FC.length == 1){
			return FC[0]; 
		}

		//if the player is starting make a random move 
		if(myFirst == true){
			int pos   = rand.nextInt(FC.length); 
			MNKCell c = FC[pos]; // random move
			bo.markCell(c.i,c.j); //selecting the move by marking the cell
			return c;
		}
		
		//if there is more than one move possible recover the last one played
		if(MC.length > 0) {
			MNKCell c = MC[MC.length-1]; // Recover the last move from the marked cells vactor
			MNKCell d = FC[0];

			controllo(direction, situazione, d, myPoints, iteratore, fila, statoMio, flag);

			bo.markCell(c.i,c.j); // Save the last move in the local MNKBoard
		}

		return FC[0];
	}

    @Override
	public String playerName() {
		return "Hanma player";
	}
}
