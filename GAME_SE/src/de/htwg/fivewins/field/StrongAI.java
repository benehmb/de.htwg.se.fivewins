package de.htwg.fivewins.field;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Deque;
import java.util.List;

import de.htwg.fivewins.field.Field;

public class StrongAI extends AIAdapter{

	HashMap<Deque<Integer>, HashMap<Deque<Integer>, Double>> bigTree;
	LinkedList<Integer> liste;


	public StrongAI(String sign, Field field) {
		if(sign.equals("X") || sign.equals("O")) {
			this.whichPlayer = sign;
			this.field = field;
		} else {
			throw new IllegalArgumentException();
		}
	}



	private boolean isFree(int column, int row, String[][] f){
		boolean returnValue = false;
		if(f[column][row].equals("-")) {
			returnValue = true;
		}
		return returnValue;
	}


	/*
	 * isFreeList
	 * Sammelt alle freien Felder in einer Liste.
	 * Dort sind sie abgespeichert zB feld(12, 7) mit 1207.
	 */
	private LinkedList<Integer> isFreeList(String[][] f){
		liste = new LinkedList<Integer>();
		for (int i = 0; i < field.getSize(); i++){
			for(int j = 0; j < field.getSize(); j++){
				if(isFree(i, j, f)){
					int k = i*100+j;
					liste.add(k);
				}
			}
		}
		//liste.add(0);
		return liste;
	}



	@Override
	public String calculateCommand() {
		//bigTree initialisieren mit 0;
		bigTree = new HashMap<Deque<Integer>, HashMap<Deque<Integer>, Double>>();
		
		String tempField [][] = field.getGameField();
		needToWin = calculateNeedToWin();
		
		//Liste aufbaun mit allen verbleibendenfreien Feldern;
		isFreeList(tempField);
		//System.out.printf("%s%n", liste.toString());
		
		
		//bigTree aufbauen.
		//System.out.printf("%s %n", liste.toString());
		buildTree(0, liste);
		//System.out.printf("%s%n", bigTree.toString());
		//System.out.printf("%s%n", bigTree.size());
		
		
		
		//bigTree Wege berechnen
		calculateTree(0, liste, tempField);
		System.out.printf("%s%n", bigTree.toString());
		
		//bigTree günstigsten Weg suchen. Aufaddieren.
		sumTree(0, 0);
		
		//koordinaten vom besten Weg nehmen.
		int t = max(0);
		int column = t/100;
		int row = t - column*100;
		return column + "," + row;
	}

	
	private void  buildTree(int z, LinkedList<Integer> l1){
		Deque<Integer> i = new LinkedList<Integer>();
		i.add(z);
		bigTree.put(i, new HashMap<Deque<Integer>, Double>());
		buildTree(i, l1);
	}
	
	
	private HashMap<Deque<Integer>, HashMap<Deque<Integer>, Double>>  buildTree(Deque<Integer> z, LinkedList<Integer> l1){
		
		for(int i : l1){
			Deque<Integer> tmpDe = new LinkedList<Integer>();
			tmpDe.addAll(z);
			tmpDe.add(i);
			bigTree.put(tmpDe, new HashMap<Deque<Integer>, Double>());
			bigTree.get(z).put(tmpDe, 0.0);
			//System.out.printf("%s%n", bigTree.toString());
			
			LinkedList<Integer> tmpLi = new LinkedList<Integer>();
			tmpLi.addAll(l1);
			tmpLi.remove(l1.indexOf(i));
			buildTree(tmpDe, tmpLi);
		}
		
		//System.out.printf("%s%n", bigTree.toString());
		
		return bigTree;
	}
	
	
	
	private void calculateTree(int z, LinkedList<Integer> l1, String[][] f){
		
		for(int i : l1){
			int column = i / 100;
			int row = i - column*100;
			lastx = column - 1;
			lasty = row - 1;
			String tmpF[][] = f.clone();
			String sign = getPlayerSign();
			tmpF[column][row] = sign;
			getTurn(f);
			winRequest();
			
			Deque<Integer> tmpDe = new LinkedList<Integer>();
			tmpDe.add(z);
			tmpDe.add(i);
			
			LinkedList<Integer> tmpLi = new LinkedList<Integer>();
			tmpLi.addAll(l1);
			tmpLi.remove(l1.indexOf(i));
			
			if(win){
				win = false;
				if( getWinnerSign().equals("O")){
					fillTree(tmpDe, l1, 1.0);
				} else if( getWinnerSign().equals("X")){
					fillTree(tmpDe, l1, -1.0);
				}
			} else{
				//System.out.printf("%s%n", bigTree.toString());
				calculateTree(tmpDe, tmpLi, tmpF);
			}
		}
	}
	

	private void calculateTree(Deque<Integer> z, LinkedList<Integer> l1, String[][] f){
		
		for(int i : l1){
			int column = i / 100;
			int row = i - column*100;
			lastx = column;
			lasty = row;
			String tmpF[][] = f.clone();
			String sign = getPlayerSign();
			tmpF[column][row] = sign;
			getTurn(f);
			winRequest();
			
			Deque<Integer> tmpDe = new LinkedList<Integer>();
			tmpDe.addAll(z);
			tmpDe.add(i);
			
			LinkedList<Integer> tmpLi = new LinkedList<Integer>();
			tmpLi.addAll(l1);
			tmpLi.remove(l1.indexOf(i));
			
			if(win){
				win = false;
				if( getWinnerSign().equals("O")){
					fillTree(tmpDe, l1, 1.0);
				} else if( getWinnerSign().equals("X")){
					fillTree(tmpDe, l1, -1.0);
				}
			} else{
				calculateTree(tmpDe, tmpLi, tmpF);
			}
		}
	}



	/*
	 * sumTree
	 * durchläuft den Baum von unten und addiert alle Werte der Kindknoten zum Wert des Elternknoten
	 * UND LÖSCHT DIESE DANN!!!
	 */
	private void sumTree(int z, int depth){
		Deque<Integer> tmpDe = new LinkedList<Integer>();
		tmpDe.add(z);
		sumTree(tmpDe, depth);
	}
	
	
	private void sumTree(Deque<Integer> z, int depth){
		if(bigTree.get(z).keySet().size() > 2){
			for(Deque<Integer> i : bigTree.get(z).keySet()){
				sumTree(i, --depth);
			}
		} else {
			double sum = 0.0;
			for(Deque<Integer> i : bigTree.get(z).keySet()){
				for(Deque<Integer> j : bigTree.get(i).keySet()){
					sum += bigTree.get(i).get(j);
					bigTree.remove(j);
				}
				bigTree.get(z).put(i, sum);
			}
		}
	}



	/*
	 * fillTree
	 * füllt den Baum ab einem bestimmmten Knoten mit immer dem selben wert.
	 */
	private void fillTree(Deque<Integer> z, LinkedList<Integer> l1, double d){
		for(int i : l1){
			Deque<Integer> tmpDe = new LinkedList<Integer>();
			tmpDe.addAll(z);
			tmpDe.add(i);
			
			LinkedList<Integer> tmpLi = new LinkedList<Integer>();
			tmpLi.addAll(l1);
			tmpLi.remove(l1.indexOf(i));
			
			System.out.printf("%s%n", bigTree.toString());
			System.out.printf("%s%n", z.toString());
			System.out.printf("%s%n", tmpDe.toString());
			
			bigTree.get(z).put(tmpDe, d);
			fillTree(tmpDe, tmpLi, d);
		}
	}
	

	
	
	private int max(int z){
		int r = -100000;
		for(Deque<Integer> i : bigTree.get(z).keySet()){
			if(r < i.getLast()){
				r = i.getLast();
			}
		}
		return r;
	}
//}  

	
	

                      // Hässliche Copy&Paste Methoden für die Gewinnabfrage der Ai


private int FIVEWINS = 5;
private int needToWin = 0;
private boolean win = false;
private String winner = null;
private int lastx;
private int lasty;
private int turn =  0;



private int calculateNeedToWin() {
	if(field.getSize() < FIVEWINS) {
		return field.getSize();
	} 
	return needToWin;
}


private void getTurn(String[][] f){
	turn = field.getSize() - isFreeList(f).size();
}


public String getPlayerSign() {
	int result = turn % 2;
	if(result == 0) {
		return "X";
	} else {
		return "O";
	} 
}


public boolean getWinner() {
	return win;
}


public String getWinnerSign() {
	return winner;
}


public void winRequest() {
	int horizontal = winRequestHorizontal(lastx, lasty, 0, getPlayerSign(), true) 
			+ winRequestHorizontal(lastx, lasty, 0, getPlayerSign(), false) + 1;
	int vertical = winRequestVertical(lastx, lasty, 0, getPlayerSign(), true) 
			+ winRequestVertical(lastx, lasty, 0, getPlayerSign(), false) + 1;
	int diagonal = winRequestDiagonal(lastx, lasty, 0, getPlayerSign(), true) 
			+ winRequestDiagonal(lastx, lasty, 0, getPlayerSign(), false) + 1;
	int diagonalReflected = winRequestDiagonalReflected(lastx, lasty, 0, getPlayerSign(), true) 
			+ winRequestDiagonalReflected(lastx, lasty, 0, getPlayerSign(), false) + 1;
	
	if(vertical >= needToWin || horizontal >= needToWin || diagonal >= needToWin ||
			diagonalReflected >= needToWin) {
		win = true;
		winner = getPlayerSign();
	}
}


//operator true => Minus
private int winRequestHorizontal(int value, int fixValue, int n, String currentPlayer, 
		boolean operator) {
	int result = 0;
	if(value < 0 || value >= field.getSize() || 
			!field.getCellValue(value, fixValue).equals(currentPlayer) ) {
		return n-1;
	}
	
	if(operator) {
		result = winRequestHorizontal(value-1, fixValue,n+1, currentPlayer, operator);
	} else {
		result = winRequestHorizontal(value+1, fixValue,n+1, currentPlayer, operator);
	}
	return result;
}


//operator true => Minus
private int winRequestVertical(int fixValue, int value, int n, String currentPlayer, 
		boolean operator) {
	int result = 0;
	if(value < 0 || value >= field.getSize() || 
			!field.getCellValue(fixValue, value).equals(currentPlayer) ) {
		return n-1;
	}
	
	if(operator) {
		result = winRequestVertical(fixValue, value-1, n+1, currentPlayer, operator);
	} else {
		result = winRequestVertical(fixValue, value+1, n+1, currentPlayer, operator);
	}
	return result;
}


//operator true => doppel --
private int winRequestDiagonal(int value1, int value2, int n, String currentPlayer, 
		boolean operator) {
	int result = 0;
	if(value1 < 0 || value1 >= field.getSize() || value2 < 0 || value2 >= field.getSize() || 
			!field.getCellValue(value1, value2).equals(currentPlayer) ) {
		return n-1;
	}
	
	if(operator) {
		result = winRequestDiagonal(value1-1, value2-1,n+1, currentPlayer, operator);
	} else {
		result = winRequestDiagonal(value1+1, value2+1,n+1, currentPlayer, operator);
	}
	return result;
}


//operator true => +-
	private int winRequestDiagonalReflected(int value1, int value2, int n, String currentPlayer, 
			boolean operator) {
		int result = 0;
		if(value1 < 0 || value1 >= field.getSize() || value2 < 0 || value2 >= field.getSize() || 
				!field.getCellValue(value1, value2).equals(currentPlayer) ) {
			return n-1;
		}
		
		if(operator) {
			result = winRequestDiagonalReflected(value1+1, value2-1,n+1, currentPlayer, operator);
		} else {
			result = winRequestDiagonalReflected(value1-1, value2+1,n+1, currentPlayer, operator);
		}
		return result;
	}

}