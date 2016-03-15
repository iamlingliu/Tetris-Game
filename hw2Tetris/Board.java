// Board.java
package hw2Tetris;

import java.util.Arrays;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean DEBUG = true;
	boolean committed;
	private int[] widths;
	private int[] heights;
	private int maxHeight;
	private boolean[][] backup;
	private int[] back_widths;
	private int[] back_heights;
	private int back_maxHeight;
	
	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		committed = true;
		
		widths = new int[height];
		heights = new int[width];
		
		backup = new boolean[width][height];
		back_widths = new int[height];
		back_heights = new int[width];

		// YOUR CODE HERE	 
	}
	
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		
		return maxHeight; // YOUR CODE HERE
		
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			System.out.print(this);
			int[] widthsCheck = new int[height];
			int maxHeightCheck =0;
			for(int i =0; i< width;i++){
				int heightCheck = 0;
				for(int j =0; j< height;j++){
					if(grid[i][j])
					{
						heightCheck = j+1;
						widthsCheck[j]++;

						if(maxHeightCheck<j+1)
							maxHeightCheck = j+1;
					}
				}
				if(heightCheck!=heights[i])
					throw new RuntimeException("Heights check failed");
			}
			if(!Arrays.equals(widthsCheck, widths))
				throw new RuntimeException("Widths check failed");

			if(maxHeightCheck != maxHeight)
				throw new RuntimeException("Max Height check failed");
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		int result = 0;
		int[] skirt = piece.getSkirt();
		for(int i =0 ; i < skirt.length;i++)
		{
			int y = heights[x+i]-skirt[i];
			if(y>result)
				result=y;
		}
		return result;
	}
	
public int computeColumnHeight(int x) {
		
		if (x < 0 || x >= width) {
			return 0;
		}
		int i = height - 1;
		while(i >= 0) {
			if(grid[x][i] == true) {
				return i + 1;
			}
			i--;
		}
		return 0; 
	}
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		
		if (x < 0 || x >= width) {
			return 0;
		}
		return heights[x]; 
	}
	

	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		
		if (y < 0 || y >= height) {
			return 0;
		}
		 return widths[y];
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if(x >= 0 && x < width && y >=0 && y < height) {
			return grid[x][y];
		}
		return true; // YOUR CODE HERE 
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	private void backup() {
		for (int i = 0; i < width; ++i) {
			System.arraycopy(grid[i], 0, backup[i], 0, height);
		}
		System.arraycopy(heights, 0, back_heights, 0, width);
		System.arraycopy(widths, 0, back_widths, 0, height);
		//back_maxHeight = maxHeight;
	}
	
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");
		 backup();
		
		int result = PLACE_OK;
		
		TPoint[] body = piece.getBody();
		int pieceX, pieceY;
		for(TPoint t : body) {
			pieceX = x + t.x;
			pieceY = y + t.y;
			if(pieceX < 0 || pieceX >= width || pieceY <0 || pieceY >= height) {
				result = PLACE_OUT_BOUNDS;
				break;
			}
			
			if(grid[pieceX][pieceY] == true) {
				result = PLACE_BAD;
				break;
			}

			grid[pieceX][pieceY] = true;
			
			widths[pieceY]++;
			if (pieceY + 1 > heights[pieceX]) {
				heights[pieceX] = pieceY + 1;
			}
			if (widths[pieceY] == width) {
				result = PLACE_ROW_FILLED;
			}
		}
		// YOUR CODE HERE
		maxHeight = computeMaxHeight();
		committed = false;
		sanityCheck();
		return result;
	}
	
	
	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		backup();
		boolean hasFilledRow = false;
		int rowTo,rowFrom,rowsCleared;
		rowsCleared = 0;

		// clearing row using a single pass method given in the handout
		for(rowTo=0,rowFrom =1;rowFrom<maxHeight;rowTo++,rowFrom++)
		{
			if(!hasFilledRow && widths[rowTo]==width)
			{
				hasFilledRow=true;
				rowsCleared++;
			}

			while(hasFilledRow && rowFrom<maxHeight && widths[rowFrom]==width)
			{
				rowsCleared++;
				rowFrom++;
			}

			if(hasFilledRow)
				copySingleRow(rowTo,rowFrom);

		}

		if(hasFilledRow)
			fillEmptyRows(rowTo,maxHeight);
		/*
		int rowsCleared = 0;
		int row = 0;
		int to = 0, from = 1;
		
		while(row < maxHeight) {
			if (widths[row] == width) {
				for(int i = 0; i < width; ++i) {
					grid[i][row] = false;
				}
				widths[row] = 0;
				rowsCleared++;
				to = row;
				break;
			}
			row++;
		}
		from = to + 1;
		while(from < maxHeight) {
			if(widths[from] == width) {
				for(int i = 0; i < width; ++i) {
					grid[i][from] = false;
				}
				widths[from] = 0;
				from++;
				rowsCleared++;
			}
			else{
				for(int i = 0; i < width; ++i) {
					grid[i][to] = grid[i][from];
					grid[i][from] = false;
				}
				widths[to] = widths[from];
				widths[from] = 0;
				to++;
				from++;
			}
		}
		*/
		for (int i = 0; i < width; ++i) {
			heights[i] = computeColumnHeight(i);
		}
		maxHeight = computeMaxHeight();
		// YOUR CODE HERE
		
		sanityCheck();
		committed = false;
		return rowsCleared;
	}

	/**
	 * private helper method that fills empty rows between
	 * specified low and high rows
	 * */
	private void fillEmptyRows(int lowRow, int highRow) {

		for(int j = lowRow;j<highRow;j++){
			widths[j]=0;
			for(int i = 0;i<width;i++)
				grid[i][j] =false;

		}
	}


	/**
	 * private helper method that copies a single row
	 * If the rowFrom parameter is more than the max row
	 * index specified by maxHeight, then empty the row
	 * pointed by rowTo.
	 * */
	private void copySingleRow(int rowTo, int rowFrom) {

		if(rowFrom<maxHeight)
		{
			for(int i = 0;i<width;i++)
			{
				grid[i][rowTo] = grid[i][rowFrom];
				widths[rowTo] = widths[rowFrom];
			}
		}
		else
		{
			for(int i = 0;i<width;i++)
			{
				grid[i][rowTo] = false;
				widths[rowTo] = 0;
			}
		}
	}

	private int computeMaxHeight() {
		int res = 0;
		for (int i = 0; i < width; ++i) {
			if (heights[i] > res) {
				res = heights[i];
			}
		}
		return res;
	}


	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if (committed) {
			return;
		}
		swap();
		committed = true;
		// YOUR CODE HERE
	}
	
	private void swap(){

		int[] temp = back_widths;
		back_widths = widths;
		widths = temp;

		temp = back_heights;
		back_heights = heights;
		heights = temp;

		boolean[][] gridtemp = backup;
		backup = grid;
		grid = gridtemp;

		maxHeight = computeMaxHeight();
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}


	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
	
	public static void main (String[] args) {

	}
}


