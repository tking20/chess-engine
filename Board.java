public class Board
{
	public long WP, WR, WN, WB, WQ, WK, BP, BR, BN, BB, BQ, BK;
	public long hash = 0;
	public short intel;
	/*0-6 50mr
	*7-10 castling rights
	*11-13 en passant file (a=7, h=0)
	*14 en passant flag
	*15 capture flag
	*
	*0000 0111 1110 0100*/
	
	public int TTintel;
	/*
	 * 0-1 = flag
	 * 2-9 = depth
	 * 10-31 = score
	 */
	
	public boolean whiteTurn;
	
	public Board(boolean wt, short in, int tti, long h, long bit0, long bit1, long bit2, long bit3, long bit4, long bit5, long bit6, long bit7, long bit8, long bit9, long bit10, long bit11)
	{
		whiteTurn = wt;
		TTintel = tti;
		intel = in;
		hash = h;
		WP = bit0;
		WR = bit1;
		WN = bit2;
		WB = bit3;
		WQ = bit4;
		WK = bit5;
		BP = bit6;
		BR = bit7;
		BN = bit8;
		BB = bit9;
		BQ = bit10;
		BK = bit11;
	}

	public Board clone()
	{
		return new Board(whiteTurn, intel, TTintel, hash, WP, WR, WN, WB, WQ, WK, BP, BR, BN, BB, BQ, BK);
	}
	
	public long blockers()
	{
		return WP|WR|WN|WB|WQ|WK|BP|BR|BN|BB|BQ|BK;
	}
	
	public long myPieces()
	{
		return whiteTurn ? WP|WR|WN|WB|WQ|WK : BP|BR|BN|BB|BQ|BK;
	}
	
	public long oppPieces()
	{
		return whiteTurn ? BP|BR|BN|BB|BQ|BK : WP|WR|WN|WB|WQ|WK;
	}

	public String toString()
	{
		String ans = whiteTurn ? "white\n" : "black\n";
		long pos = 1L << 63;
		for(int i = 0; i<64; i++, pos >>>= 1)
		{
			if((WP & pos) == pos)
			{
				ans += "P";
			}
			else if((WR & pos) == pos)
			{
				ans += "R";
			}
			else if((WN & pos) == pos)
			{
				ans += "N";
			}
			else if((WB & pos) == pos)
			{
				ans += "B";
			}
			else if((WQ & pos) == pos)
			{
				ans += "Q";
			}
			else if((WK & pos) == pos)
			{
				ans += "K";
			}
			else if((BP & pos) == pos)
			{
				ans += "p";
			}
			else if((BR & pos) == pos)
			{
				ans += "r";
			}
			else if((BN & pos) == pos)
			{
				ans += "n";
			}
			else if((BB & pos) == pos)
			{
				ans += "b";
			}
			else if((BQ & pos) == pos)
			{
				ans += "q";
			}
			else if((BK & pos) == pos)
			{
				ans += "k";
			}
			else
			{
				ans += ".";
			}
			if(i % 8 == 7)
			{
				ans += "\n";
			}
		}
		return ans;
	}

}