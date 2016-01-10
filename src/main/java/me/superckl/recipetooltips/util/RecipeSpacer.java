package me.superckl.recipetooltips.util;

public class RecipeSpacer {

	private final int rows;
	private final int columns;
	private int row;
	private int column;
	private boolean hasNext = true;

	public RecipeSpacer(final int rows, final int columns) {
		this.rows = rows;
		this.columns = columns;
		if(this.rows <= 0 && this.columns <= 0)
			this.hasNext = false;
	}

	public int[] next(){
		final int[] xy = new int[] {this.column*18, this.row*18};
		this.column++;
		if(this.column >= this.columns){
			this.row++;
			this.column = 0;
		}
		if(this.row >= this.rows)
			this.hasNext = false;
		return xy;
	}

	public boolean hasNext(){
		return this.hasNext;
	}

}
