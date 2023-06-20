import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Chess
{
	public static final long PAWN_STARTING_RANKS = 71776119061282560L;
	public static final long A_FILE = -9187201950435737472L;
	public static final long H_FILE = 72340172838076673L;
	public static final long FIRST_RANK = 255L;
	public static final long SECOND_RANK = 65280L;
	public static final long SEVENTH_RANK = 71776119061217280L;
	public static final long EIGHTH_RANK = -72057594037927936L;
	
	public static final long WHITE_KINGSIDE_CASTLE_FREE_SQUARES = 6L;
	public static final long WHITE_QUEENSIDE_CASTLE_FREE_SQUARES = 112L;
	public static final long BLACK_KINGSIDE_CASTLE_FREE_SQUARES = 432345564227567616L;
	public static final long BLACK_QUEENSIDE_CASTLE_FREE_SQUARES = 8070450532247928832L;
	
	public static final byte LOWERBOUND = 2;
	public static final byte EXACT = 0;
	public static final byte UPPERBOUND = 1;
	
	public static int DEPTH = 6;
	
	public static Board board = null;
	
	
	public static HashMap<Long, Board> TT = new HashMap<Long, Board>();
	
	public static long nodes = 0;
	
	public static int[][] evals = new int[5][64];
	
	public static long[][] rays = new long[8][64];
	
	public static long[][] z = new long[64][12];
	public static long[] zExtra = new long[13];
	/*
	 * 0 = blackTurn
	 * 1-4 = castling
	 * 5-12 = EP file
	 */
	
	public static char[][] chessBoard =
	{
		{'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
		{'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
		{'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}
	};
	
	public static void printLong(long l)
	{
		for(int i = 63; i>-1; i--)
		{
			if(((1L << i) & l) != 0)
			{
				System.out.print("x");
			}
			else
			{
				System.out.print(".");
			}
			if(i % 8 == 0)
			{
				System.out.println();
			}
		}
	}
	
	public static void evalSetup() throws FileNotFoundException
	{
		Scanner read = new Scanner("pawn:\r\n"
				+ "  0   0   0   0   0   0   0   0\r\n"
				+ "125 125 125 125 125 125 125 125\r\n"
				+ "115 120 120 120 120 120 120 115\r\n"
				+ "110 115 115 120 120 115 115 110\r\n"
				+ "110  95 110 115 115 110  95 110\r\n"
				+ "110  95 105 110 110 105  95 110\r\n"
				+ "100 100 100 100 100 100 100 100\r\n"
				+ "  0   0   0   0   0   0   0   0\r\n"
				+ "knight:\r\n"
				+ "250 275 280 285 285 280 275 250\r\n"
				+ "275 300 300 305 305 300 300 275\r\n"
				+ "280 305 320 320 320 320 305 280\r\n"
				+ "285 310 330 335 335 330 310 285\r\n"
				+ "285 305 320 335 335 320 305 285\r\n"
				+ "280 300 315 310 310 315 300 280\r\n"
				+ "275 300 300 310 310 300 300 275\r\n"
				+ "250 275 280 285 285 280 275 250\r\n"
				+ "bishop:\r\n"
				+ "250 275 300 300 300 300 275 250\r\n"
				+ "275 310 300 305 305 300 310 275\r\n"
				+ "300 305 320 320 320 320 305 300\r\n"
				+ "300 320 330 335 335 330 320 300\r\n"
				+ "300 320 330 335 335 330 320 300\r\n"
				+ "300 305 315 310 310 315 305 300\r\n"
				+ "275 310 300 305 305 300 310 275\r\n"
				+ "250 275 300 300 300 300 275 250\r\n"
				+ "rook:\r\n"
				+ "500 500 500 500 500 500 500 500\r\n"
				+ "500 500 500 500 500 500 500 500\r\n"
				+ "510 500 500 500 500 500 500 510\r\n"
				+ "510 500 500 500 500 500 500 510\r\n"
				+ "510 500 500 500 500 500 500 510\r\n"
				+ "460 500 500 500 500 500 500 460\r\n"
				+ "460 490 510 510 510 510 490 460\r\n"
				+ "480 475 500 520 520 520 475 480\r\n"
				+ "queen:\r\n"
				+ "860 870 875 880 880 875 870 860\r\n"
				+ "870 880 900 905 905 900 880 870\r\n"
				+ "875 900 910 915 915 910 900 875\r\n"
				+ "880 905 915 920 920 915 905 880\r\n"
				+ "880 905 915 920 920 915 905 880\r\n"
				+ "875 900 910 915 915 910 900 875\r\n"
				+ "870 880 900 905 905 900 880 870\r\n"
				+ "860 870 875 880 880 875 870 860\r\n"
				+ "");
		for(int i = 0; i<5; i++)
		{
			read.next();
			for(int j = 0; j<64; j++)
			{
				evals[i][63-j] = read.nextInt();
			}
		}
	}

	public static void raySetup()
	{
		for(int i = 0; i<8; i++)
		{
			for(int j = 0; j<64; j++)
			{
				int pos = j;
				switch(i)
				{
				case 0: //N
				while(pos<64)
				{
					rays[i][j] += 1L<<pos;
					pos+=8;
				} break;
				
				case 1: //NE
				while(pos<64)
				{
					rays[i][j] += 1L<<pos;
					pos+=7;
					if(pos % 8 == 7) break;
				} break;
				
				case 2: //E
				while(pos>-1)
				{
					rays[i][j] += 1L<<pos;
					pos--;
					if(pos % 8 == 7) break;
				} break;
				
				case 3: //SE
				while(pos>-1)
				{
					rays[i][j] += 1L<<pos;
					pos-=9;
					if(pos % 8 == 7) break;
				} break;
				
				case 4: //S
				while(pos>-1)
				{
					rays[i][j] += 1L<<pos;
					pos-=8;
				} break;
				
				case 5: //SW
				while(pos>-1)
				{
					rays[i][j] += 1L<<pos;
					pos-=7;
					if(pos % 8 == 0) break;
				} break;
				
				case 6: //W
				while(true)
				{
					rays[i][j] += 1L<<pos;
					pos++;
					if(pos % 8 == 0) break;
				} break;
				
				case 7: //NW
				while(pos<64)
				{
					rays[i][j] += 1L<<pos;
					pos+=9;
					if(pos % 8 == 0) break;
				} break;
				}
				rays[i][j] -= 1L << j;
			}
		}
	}
	
	public static void zobristSetup()
	{
		Random rndm = new Random(123456789L);
		for(int i = 0; i<64; i++)
		{
			for(int j = 0; j<12; j++)
			{
				z[i][j] = rndm.nextLong();
			}
		}
		for(int i = 0; i<13; i++)
		{
			zExtra[i] = rndm.nextLong();
		}
	}
	
	public static long hash(Board b)
	{
		long h = 0;
		long bit = 1;
		for(int i = 0; i<64; i++, bit<<=1)
		{
			if((b.WP & bit) == bit)
			{
				h ^= z[i][0];
			}
			else if((b.WR & bit) == bit)
			{
				h ^= z[i][1];
			}
			else if((b.WN & bit) == bit)
			{
				h ^= z[i][2];
			}
			else if((b.WB & bit) == bit)
			{
				h ^= z[i][3];
			}
			else if((b.WQ & bit) == bit)
			{
				h ^= z[i][4];
			}
			else if((b.WK & bit) == bit)
			{
				h ^= z[i][5];
			}
			else if((b.BP & bit) == bit)
			{
				h ^= z[i][6];
			}
			else if((b.BR & bit) == bit)
			{
				h ^= z[i][7];
			}
			else if((b.BN & bit) == bit)
			{
				h ^= z[i][8];
			}
			else if((b.BB & bit) == bit)
			{
				h ^= z[i][9];
			}
			else if((b.BQ & bit) == bit)
			{
				h ^= z[i][10];
			}
			else if((b.BK & bit) == bit)
			{
				h ^= z[i][11];
			}
		}
		if(!b.whiteTurn)
		{
			h ^= zExtra[0];
		}
		
		int castling = b.intel >> 7 & 15;
		if((castling & 1) == 1)
		{
			h ^= zExtra[1];
		}
		if((castling & 2) == 2)
		{
			h ^= zExtra[2];
		}
		if((castling & 4) == 4)
		{
			h ^= zExtra[3];
		}
		if((castling & 8) == 8)
		{
			h ^= zExtra[4];
		}
		
		if((b.intel & 16384) == 16384) //there is en passant
		{
			int file = (b.intel >> 11) & 7;
			file = 8-file;
			h ^= zExtra[4+file];
		}
		return h;
	}

	public static Board FENParser(String s)
	{
		int pos = 0;
		while(!s.isBlank())
		{
			char c = s.charAt(0);
			s = s.substring(1);
			if(Character.isLetter(c))
			{
				chessBoard[pos/8][pos%8] = c;
				pos++;
			}
			else if(Character.isDigit(c))
			{
				for(int i = 0; i<c-48; i++)
				{
					chessBoard[pos/8][pos%8] = ' ';
					pos++;
				}
			}
			else if(c == '/')
			{
				
			}
			else if(c == ' ')
			{
				Board b = newGameBoard();
				boolean whiteToMove = s.charAt(0) == 'w';
				if(!whiteToMove)
				{
					b.whiteTurn = false;
				}
				s = s.substring(2);
				
				String castling = s.substring(0, s.indexOf(" "));
				if(castling.equals("-"))
				{
					s = s.substring(2);
					b.intel &= ~1920;
				}
				else
				{
					s = s.replaceAll(castling, "");
					if(!castling.contains("K"))
					{
						b.intel &= ~128;
					}
					if(!castling.contains("Q"))
					{
						b.intel &= ~256;
					}
					if(!castling.contains("k"))
					{
						b.intel &= ~512;
					}
					if(!castling.contains("q"))
					{
						b.intel &= ~1024;
					}
				}
				s = s.trim();
				if(!s.startsWith("-"))
				{
					int file = s.charAt(0);
					b.intel |= 16384;
					file = 7 - (file-97);
					b.intel |= file << 11;
				}
				b.hash = hash(board);
				return b;
			}
		}
		return null;
	}
	
	public static Board newGameBoard()
	{
		long[] bitboards = new long[12];
		for(int i = 0; i<8; i++)
		{
			for(int j = 0; j<8; j++)
			{
				int bit = 63 - (8*i+j);
				switch(chessBoard[i][j])
				{
				case 'P': bitboards[0] += 1L<<bit; break;
				case 'R': bitboards[1] += 1L<<bit; break;
				case 'N': bitboards[2] += 1L<<bit; break;
				case 'B': bitboards[3] += 1L<<bit; break;
				case 'Q': bitboards[4] += 1L<<bit; break;
				case 'K': bitboards[5] += 1L<<bit; break;
				case 'p': bitboards[6] += 1L<<bit; break;
				case 'r': bitboards[7] += 1L<<bit; break;
				case 'n': bitboards[8] += 1L<<bit; break;
				case 'b': bitboards[9] += 1L<<bit; break;
				case 'q': bitboards[10] += 1L<<bit; break;
				case 'k': bitboards[11] += 1L<<bit; break;
				}
			}
		}
		//whiteTurn = true, starting intel && TTintel, hash is fixed below
		Board board = new Board(true, (short)2020, 0, 0, bitboards[0], bitboards[1], bitboards[2], bitboards[3], bitboards[4], bitboards[5], bitboards[6], bitboards[7], bitboards[8], bitboards[9], bitboards[10], bitboards[11]);
		board.hash = hash(board);
		return board;
	}
	
	public static void makeCapture(Board b, long newPos)
	{
		int pos = Long.numberOfTrailingZeros(newPos);
		if(b.whiteTurn)
		{
			if((newPos & b.BP) != 0)
			{
				b.BP -= newPos;
				b.hash ^= z[pos][6];
			}
			if((newPos & b.BR) != 0)
			{
				b.BR -= newPos;
				b.hash ^= z[pos][7];
			}
			if((newPos & b.BN) != 0)
			{
				b.BN -= newPos;
				b.hash ^= z[pos][8];
			}
			if((newPos & b.BB) != 0)
			{
				b.BB -= newPos;
				b.hash ^= z[pos][9];
			}
			if((newPos & b.BQ) != 0)
			{
				b.BQ -= newPos;
				b.hash ^= z[pos][10];
			}
		}
		else
		{
			if((newPos & b.WP) != 0)
			{
				b.WP -= newPos;
				b.hash ^= z[pos][0];
			}
			if((newPos & b.WR) != 0)
			{
				b.WR -= newPos;
				b.hash ^= z[pos][1];
			}
			if((newPos & b.WN) != 0)
			{
				b.WN -= newPos;
				b.hash ^= z[pos][2];
			}
			if((newPos & b.WB) != 0)
			{
				b.WB -= newPos;
				b.hash ^= z[pos][3];
			}
			if((newPos & b.WQ) != 0)
			{
				b.WQ -= newPos;
				b.hash ^= z[pos][4];
			}
		}
	}
	
	public static LinkedList<Board> handlePromotion(Board b)
	{
		LinkedList<Board> moves = new LinkedList<Board>();
		
		if(b.whiteTurn)
		{
			int pawnToPromote = 63-Long.numberOfLeadingZeros(b.WP);
			b.WP -= 1L << pawnToPromote;
			Board newBoard = b.clone();
			
			newBoard.whiteTurn = false;
			newBoard.WR += 1L << pawnToPromote;
			newBoard.hash ^= z[pawnToPromote][1];
			moves.add(newBoard);
			
			newBoard = b.clone();
			newBoard.whiteTurn = false;
			newBoard.WN += 1L << pawnToPromote;
			newBoard.hash ^= z[pawnToPromote][2];
			moves.add(newBoard);
			
			newBoard = b.clone();
			newBoard.whiteTurn = false;
			newBoard.WB += 1L << pawnToPromote;
			newBoard.hash ^= z[pawnToPromote][3];
			moves.add(newBoard);
			
			newBoard = b.clone();
			newBoard.whiteTurn = false;
			newBoard.WQ += 1L << pawnToPromote;
			newBoard.hash ^= z[pawnToPromote][4];
			moves.add(newBoard);
		}
		else
		{
			int pawnToPromote = Long.numberOfTrailingZeros(b.BP);
			b.BP -= 1L << pawnToPromote;
			Board newBoard = b.clone();
			
			newBoard.whiteTurn = true;
			newBoard.BR += 1L << pawnToPromote;
			newBoard.hash ^= z[pawnToPromote][7];
			moves.add(newBoard);
			
			newBoard = b.clone();
			newBoard.whiteTurn = true;
			newBoard.BN += 1L << pawnToPromote;
			newBoard.hash ^= z[pawnToPromote][8];
			moves.add(newBoard);
			
			newBoard = b.clone();
			newBoard.whiteTurn = true;
			newBoard.BB += 1L << pawnToPromote;
			newBoard.hash ^= z[pawnToPromote][9];
			moves.add(newBoard);
			
			newBoard = b.clone();
			newBoard.whiteTurn = true;
			newBoard.BQ += 1L << pawnToPromote;
			newBoard.hash ^= z[pawnToPromote][10];
			moves.add(newBoard);
		}
		
		return moves;
	}

	public static long knightMoves(Board b, int knight)
	{
		long nMoves = 0;
		long newPos = 1L << knight-17; //SSE
		if(knight > 16 && knight % 8 > 0)
		{
			nMoves |= newPos;
		}
		newPos = 1L << knight-15; //SSW
		if(knight > 15 && knight % 8 < 7)
		{
			nMoves |= newPos;
		}
		newPos = 1L << knight-10; //SEE
		if(knight > 9 && knight % 8 > 1)
		{
			nMoves |= newPos;
		}
		newPos = 1L << knight-6; //SWW
		if(knight > 7 && knight % 8 < 6)
		{
			nMoves |= newPos;
		}
		newPos = 1L << knight+6; //NEE
		if(knight < 56 && knight % 8 > 1)
		{
			nMoves |= newPos;
		}
		newPos = 1L << knight+10; //NWW
		if(knight < 54 && knight % 8 < 6)
		{
			nMoves |= newPos;
		}
		newPos = 1L << knight+15; //NNE
		if(knight < 48 && knight % 8 > 0)
		{
			nMoves |= newPos;
		}
		newPos = 1L << knight+17; //NNW
		if(knight < 47 && knight % 8 < 7)
		{
			nMoves |= newPos;
		}
		return nMoves;
	}
	
	public static long kingMoves(Board b, int king)
	{
		if(king == 64) //there is no king
		{
			return 0;
		}
		long kMoves = 0;
		long newPos = 1L << king-9; //SE
		if(king > 8 && king % 8 > 0)
		{
			kMoves |= newPos;
		}
		
		newPos = 1L << king-8; //S
		if(king > 7)
		{
			kMoves |= newPos;
		}
		
		newPos = 1L << king-7; //SW
		if(king > 7 && king % 8 < 7)
		{
			kMoves |= newPos;
		}
		
		newPos = 1L << king-1; //E
		if(king % 8 > 0)
		{
			kMoves |= newPos;
		}
		
		newPos = 1L << king+1; //W
		if(king % 8 < 7)
		{
			kMoves |= newPos;
		}
		
		newPos = 1L << king+7; //NE
		if(king < 56 && king % 8 > 0)
		{
			kMoves |= newPos;
		}
		
		newPos = 1L << king+8; //N
		if(king < 56)
		{
			kMoves |= newPos;
		}
		
		newPos = 1L << king+9; //NW
		if(king < 55 && king % 8 < 7)
		{
			kMoves |= newPos;
		}
		return kMoves;
	}
	
	public static long bishopMoves(Board b, int bishop, long blockers)
	{
		long bMoves = 0;
		for(byte by = 1; by<9; by+=2)
		{
			bMoves |= rays[by][bishop];
			long mask = blockers & rays[by][bishop];
			if(mask != 0)
			{
				//north vs south
				int block = (by > 2 && by < 6) ? 63-Long.numberOfLeadingZeros(mask) : Long.numberOfTrailingZeros(mask);
				bMoves &= ~rays[by][block];
			}
		}
		return bMoves;
	}
	
	public static long rookMoves(Board b, int rook, long blockers)
	{
		long rMoves = 0;
		for(byte by = 0; by<8; by+=2)
		{
			rMoves |= rays[by][rook];
			long mask = blockers & rays[by][rook];
			if(mask != 0)
			{
				//north vs south
				int block = (by > 1 && by < 5) ? 63-Long.numberOfLeadingZeros(mask) : Long.numberOfTrailingZeros(mask);
				rMoves &= ~rays[by][block];
			}
		}
		return rMoves;
	}
	
	public static boolean isLegal(Board b)
	{
		//invert because whiteTurn has already been toggled
		boolean whiteJustPlayed = !b.whiteTurn;
		long king = whiteJustPlayed ? b.WK : b.BK;
		long attacks = 0;
		long blockers = b.blockers();
		
		int oppKingPos = Long.numberOfTrailingZeros(whiteJustPlayed ? b.BK : b.WK);
		attacks |= kingMoves(b, oppKingPos);
		if((attacks & king) != 0)
		{
			return false;
		}
		
		if(whiteJustPlayed)
		{
			if(((b.BP & ~H_FILE) >> 9 & king) != 0)
			{
				return false;
			}
			if(((b.BP & ~A_FILE) >> 7 & king) != 0)
			{
				return false;
			}
		}
		else
		{
			if(((b.WP & ~H_FILE) << 7 & king) != 0)
			{
				return false;
			}
			if(((b.WP & ~A_FILE) << 9 & king) != 0)
			{
				return false;
			}
		}
		
		long oppKnights = (whiteJustPlayed ? b.BN : b.WN);
		while(oppKnights != 0) //there are knights
		{
			int knight = Long.numberOfTrailingZeros(oppKnights);
			attacks = knightMoves(b, knight);
			if((attacks & king) != 0)
			{
				return false;
			}
			oppKnights -= 1L << knight;
		}
		
		long oppRooks = whiteJustPlayed ? b.BR : b.WR;
		while(oppRooks != 0) //there are rooks
		{
			int rook = Long.numberOfTrailingZeros(oppRooks);
			attacks = rookMoves(b, rook, blockers);
			if((attacks & king) != 0)
			{
				return false;
			}
			attacks = 0;
			
			oppRooks -= 1L << rook;
		}
		
		long oppBishops = whiteJustPlayed ? b.BB : b.WB;
		while(oppBishops != 0) //there are bishops
		{
			int bishop = Long.numberOfTrailingZeros(oppBishops);
			attacks = bishopMoves(b, bishop, blockers);
			if((attacks & king) != 0)
			{
				return false;
			}
			attacks = 0;
			
			oppBishops -= 1L << bishop;
		}
		
		long oppQueen = whiteJustPlayed ? b.BQ : b.WQ;
		while(oppQueen != 0) //there are queens
		{
			int queen = Long.numberOfTrailingZeros(oppQueen);
			attacks = bishopMoves(b, queen, blockers) | rookMoves(b, queen, blockers);
			if((attacks & king) != 0)
			{
				return false;
			}
			attacks = 0;
			
			oppQueen -= 1L << queen;
		}
		
		
		return true;
	}
	
	public static LinkedList<Board> allMoves(Board b)
	{
		LinkedList<Board> moves = new LinkedList<Board>();
		
		long my = b.myPieces();
		long opp = b.oppPieces();
		long blockers = my|opp;
		long newPos = 0;
		
		long kingBoard = (b.whiteTurn ? b.WK : b.BK);
		int king = Long.numberOfTrailingZeros(kingBoard);
		
		if(b.whiteTurn)
		{
			if((b.intel & 128) == 128 && (blockers & WHITE_KINGSIDE_CASTLE_FREE_SQUARES) == 0 && b.WK == 8 && (b.WR & 1) != 0) //O-O
			{
				Board TLC = b.clone();
				TLC.intel &= 32767;
				TLC.whiteTurn = false;
				
				if(isLegal(TLC))
				{
					TLC.WK = 4;
					if(isLegal(TLC))
					{
						TLC.WK = 2;
						TLC.WR += 3;
						
						TLC.intel -= 128;
						TLC.hash ^= zExtra[1];
						if((TLC.intel & 256) == 256)
						{
							TLC.intel -= 256;
							TLC.hash ^= zExtra[2];
						}
						if((TLC.intel & 16384) == 16384)
						{
							TLC.intel &= ~16384;
							//TLC.hash ^= zExtra[]
						}
						
						TLC.hash = TLC.hash ^ z[3][5] ^ z[1][5] ^ z[0][1] ^ z[2][1];
						moves.add(TLC);
					}
				}
			}
			if((b.intel & 256) == 256 && (blockers & WHITE_QUEENSIDE_CASTLE_FREE_SQUARES) == 0 && b.WK == 8 && (b.WR & 128) != 0) //O-O-O
			{
				Board TLC = b.clone();
				TLC.intel &= 32767;
				TLC.whiteTurn = false;
				
				if(isLegal(TLC))
				{
					TLC.WK = 16;
					if(isLegal(TLC))
					{
						TLC.WK = 32;
						TLC.WR -= 112;
						
						TLC.intel -= 256;
						TLC.hash ^= zExtra[2];
						if((TLC.intel & 128) == 128)
						{
							TLC.intel -= 128;
							TLC.hash ^= zExtra[1];
						}
						TLC.intel &= ~16384;
						TLC.hash = TLC.hash ^ z[3][5] ^ z[5][5] ^ z[7][1] ^ z[4][1];
						moves.add(TLC);
					}
				}
			}
		}
		else
		{
			if((b.intel & 512) == 512 && (blockers & BLACK_KINGSIDE_CASTLE_FREE_SQUARES) == 0 && b.BK == 1L << 59 && (b.BR & 1L << 56) != 0) //O-O
			{
				Board TLC = b.clone();
				TLC.intel &= 32767;
				TLC.whiteTurn = true;
				
				if(isLegal(TLC))
				{
					TLC.BK = 1L << 58;
					if(isLegal(TLC))
					{
						TLC.BK = 1L << 57;
						TLC.BR += (1L << 58) - (1L << 56);

						TLC.intel -= 512;
						TLC.hash ^= zExtra[3];
						if((TLC.intel & 1024) == 1024)
						{
							TLC.intel -= 1024;
							TLC.hash ^= zExtra[4];
						}
						TLC.intel &= ~16384;
						TLC.hash = TLC.hash ^ z[59][11] ^ z[57][11] ^ z[56][7] ^ z[58][7];
						moves.add(TLC);
					}
				}
			}
			if((b.intel & 1024) == 1024 && (blockers & BLACK_QUEENSIDE_CASTLE_FREE_SQUARES) == 0 && b.BK == 1L << 59 && (b.BR & 1L << 63) != 0) //O-O-O
			{
				Board TLC = b.clone();
				TLC.intel &= 32767;
				TLC.whiteTurn = true;
				
				if(isLegal(TLC))
				{
					TLC.BK = 1L << 60;
					if(isLegal(TLC))
					{
						TLC.BK = 1L << 61;
						TLC.BR += (1L << 60) - (1L << 63);
						
						TLC.intel -= 1024;
						TLC.hash ^= zExtra[4];
						if((TLC.intel & 512) == 512)
						{
							TLC.intel -= 512;
							TLC.hash ^= zExtra[3];
						}
						TLC.intel &= ~16384;
						TLC.hash = TLC.hash ^ z[59][11] ^ z[61][11] ^ z[63][7] ^ z[60][7];
						moves.add(TLC);
					}
				}
			}
		}
		
		long kMoves = kingMoves(b, king) & ~my;
		
		while(kMoves != 0) //there are king moves
		{
			int pos = Long.numberOfTrailingZeros(kMoves);
			newPos = 1L << pos;
			Board newBoard = b.clone();
			if(b.whiteTurn)
			{
				newBoard.WK += newPos - (1L << king);
				newBoard.hash ^= z[king][5] ^ z[pos][5];
				if((newPos & opp) != 0)
				{
					makeCapture(newBoard, newPos);
				}
				else
				{
					newBoard.intel &= 32767;
				}
				if((newBoard.intel & 128) == 128)
				{
					newBoard.intel -= 128;
					newBoard.hash ^= zExtra[1];
				}
				if((newBoard.intel & 256) == 256)
				{
					newBoard.intel -= 256;
					newBoard.hash ^= zExtra[2];
				}
				newBoard.intel &= ~16384;
			}
			else
			{
				newBoard.BK += newPos - (1L << king);
				newBoard.hash ^= z[king][11] ^ z[pos][11];
				if((newPos & opp) != 0)
				{
					makeCapture(newBoard, newPos);
				}
				else
				{
					newBoard.intel &= 32767;
				}
				if((newBoard.intel & 512) == 512)
				{
					newBoard.intel -= 512;
					newBoard.hash ^= zExtra[3];
				}
				if((newBoard.intel & 1024) == 1024)
				{
					newBoard.intel -= 1024;
					newBoard.hash ^= zExtra[4];
				}
				newBoard.intel &= ~16384;
			}
			newBoard.whiteTurn = !newBoard.whiteTurn;
			moves.add(newBoard);
			kMoves -= newPos;
		}
		
		long pawnBoard = (b.whiteTurn ? b.WP : b.BP);
		while(pawnBoard != 0) //there are pawn moves
		{
			int pawn = Long.numberOfTrailingZeros(pawnBoard);
			long pos = 1L << pawn;
			
			if(b.whiteTurn)
			{
				if((blockers & pos << 8) == 0) //1-move push
				{
					Board newBoard = b.clone();
					newBoard.intel &= 32767;
					newBoard.WP += (pos << 8) - pos;
					
					newBoard.hash ^= z[pawn][0]; //remove pawn
					newBoard.intel &= ~16384;
					
					if((newBoard.WP & EIGHTH_RANK) != 0)
					{
						moves.addAll(handlePromotion(newBoard));
					}
					else
					{
						newBoard.hash ^= z[pawn+8][0]; //add new pawn
						
						newBoard.whiteTurn = false;
						moves.add(newBoard);
					}
				}
				if((pos & SECOND_RANK) != 0 && (blockers & pos << 8) == 0 && (blockers & pos << 16) == 0) //2-move push
				{
					Board newBoard = b.clone();
					newBoard.intel &= 32767;
					newBoard.WP += (pos << 16) - pos;
					newBoard.hash ^= z[pawn][0] ^ z[pawn+16][0];
					
					newBoard.intel |= 16384;
					newBoard.intel = (short) ((newBoard.intel & ~14336) | ((pawn % 8) << 11));
					newBoard.whiteTurn = false;
					moves.add(newBoard);
				}
				if((b.intel & 16384) == 16384) //en passant may be possible
				{
					if(pawn > 31 && pawn < 40)
					{
						int file = (b.intel & 14336) >> 11;
						if(file - pawn % 8 == -1)
						{
							Board newBoard = b.clone();
							newBoard.intel |= -32768;
							newBoard.WP += (pos << 7) - pos;
							newBoard.hash ^= z[pawn][0] ^ z[pawn+7][0];
							newBoard.intel &= ~16384;
							makeCapture(newBoard, pos >> 1);
							newBoard.whiteTurn = false;
							moves.add(newBoard);
						}
						else if(file - pawn % 8 == 1)
						{
							Board newBoard = b.clone();
							newBoard.intel |= -32768;
							newBoard.WP += (pos << 9) - pos;
							newBoard.hash ^= z[pawn][0] ^ z[pawn+9][0];
							newBoard.intel &= ~16384;
							makeCapture(newBoard, pos << 1);
							newBoard.whiteTurn = false;
							moves.add(newBoard);
						}
					}
				}
				if(pawn % 8 > 0 && (pos << 7 & opp) != 0)
				{
					Board newBoard = b.clone();
					newBoard.intel |= -32768;
					newBoard.WP += (pos << 7) - pos;
					newBoard.hash ^= z[pawn][0]; //remove pawn
					newBoard.intel &= ~16384;
					makeCapture(newBoard, pos << 7);
					if((newBoard.WP & EIGHTH_RANK) != 0)
					{
						moves.addAll(handlePromotion(newBoard));
					}
					else
					{
						newBoard.hash ^= z[pawn+7][0]; //add new pawn
						newBoard.whiteTurn = false;
						moves.add(newBoard);
					}
				}
				if(pawn % 8 < 7 && (pos << 9 & opp) != 0)
				{
					Board newBoard = b.clone();
					newBoard.intel |= -32768;
					newBoard.WP += (pos << 9) - pos;
					newBoard.hash ^= z[pawn][0]; //remove pawn
					newBoard.intel &= ~16384;
					makeCapture(newBoard, pos << 9);
					if((newBoard.WP & EIGHTH_RANK) != 0)
					{
						moves.addAll(handlePromotion(newBoard));
					}
					else
					{
						newBoard.hash ^= z[pawn+9][0]; //add new pawn
						newBoard.whiteTurn = false;
						moves.add(newBoard);
					}
				}
			}
			else
			{
				if((blockers & pos >> 8) == 0) //1-move push
				{
					Board newBoard = b.clone();
					newBoard.intel &= 32767;
					newBoard.BP += (pos >> 8) - pos;
					newBoard.hash ^= z[pawn][6]; //remove pawn
					newBoard.intel &= ~16384;
					if((newBoard.BP & FIRST_RANK) != 0)
					{
						moves.addAll(handlePromotion(newBoard));
					}
					else
					{
						newBoard.hash ^= z[pawn-8][6]; //add new pawn
						newBoard.whiteTurn = true;
						moves.add(newBoard);
					}
				}
				if((pos & SEVENTH_RANK) != 0 && (blockers & pos >> 8) == 0 && (blockers & pos >> 16) == 0) //2-move push
				{
					Board newBoard = b.clone();
					newBoard.intel &= 32767;
					newBoard.BP += (pos >> 16) - pos;
					newBoard.hash ^= z[pawn][6] ^ z[pawn-16][6];
					newBoard.intel |= 16384;
					newBoard.intel = (short) ((newBoard.intel & ~14336) | ((pawn % 8) << 11));
					newBoard.whiteTurn = true;
					moves.add(newBoard);
				}
				if((b.intel & 16384) == 16384) //en passant may be possible
				{
					if(pawn > 23 && pawn < 32)
					{
						int file = (b.intel & 14336) >> 11;
						if(file - pawn % 8 == -1)
						{
							Board newBoard = b.clone();
							newBoard.intel |= -32768;
							newBoard.BP += (pos >> 9) - pos;
							newBoard.hash ^= z[pawn][6] ^ z[pawn-9][6];
							newBoard.intel &= ~16384;
							makeCapture(newBoard, pos >> 1);
							newBoard.whiteTurn = true;
							moves.add(newBoard);
						}
						else if(file - pawn % 8 == 1)
						{
							Board newBoard = b.clone();
							newBoard.intel |= -32768;
							newBoard.BP += (pos >> 7) - pos;
							newBoard.hash ^= z[pawn][6] ^ z[pawn-7][6];
							newBoard.intel &= ~16384;
							makeCapture(newBoard, pos << 1);
							newBoard.whiteTurn = true;
							moves.add(newBoard);
						}
					}
				}
				if(pawn % 8 > 0 && (pos >> 9 & opp) != 0)
				{
					Board newBoard = b.clone();
					newBoard.intel |= -32768;
					newBoard.BP += (pos >> 9) - pos;
					newBoard.hash ^= z[pawn][6]; //remove pawn
					newBoard.intel &= ~16384;
					makeCapture(newBoard, pos >> 9);
					if((newBoard.BP & FIRST_RANK) != 0)
					{
						moves.addAll(handlePromotion(newBoard));
					}
					else
					{
						newBoard.hash ^= z[pawn-9][6]; //add new pawn
						newBoard.whiteTurn = true;
						moves.add(newBoard);
					}
				}
				if(pawn % 8 < 7 && (pos >> 7 & opp) != 0)
				{
					Board newBoard = b.clone();
					newBoard.intel |= -32768;
					newBoard.BP += (pos >> 7) - pos;
					newBoard.hash ^= z[pawn][6]; //remove pawn
					newBoard.intel &= ~16384;
					makeCapture(newBoard, pos >> 7);
					if((newBoard.BP & FIRST_RANK) != 0)
					{
						moves.addAll(handlePromotion(newBoard));
					}
					else
					{
						newBoard.hash ^= z[pawn-7][6]; //add new pawn
						newBoard.whiteTurn = true;
						moves.add(newBoard);
					}
				}
			}
			
			pawnBoard -= pos;
		}
		
		long knightBoard = (b.whiteTurn ? b.WN : b.BN);
		while(knightBoard != 0) //there are knights
		{
			int knight = Long.numberOfTrailingZeros(knightBoard);
			long nMoves = knightMoves(b, knight) & ~my;
			
			while(nMoves != 0)
			{
				int pos = Long.numberOfTrailingZeros(nMoves);
				newPos = 1L << pos;
				Board newBoard = b.clone();
				newBoard.intel &= ~16384;
				if(b.whiteTurn)
				{
					newBoard.WN += newPos - (1L << knight);
					newBoard.hash ^= z[knight][2] ^ z[pos][2];
					if((newPos & opp) != 0)
					{
						newBoard.intel |= -32768;
						makeCapture(newBoard, newPos);
					}
					else
					{
						newBoard.intel &= 32767;
					}
				}
				else
				{
					newBoard.BN += newPos - (1L << knight);
					newBoard.hash ^= z[knight][8] ^ z[pos][8];
					if((newPos & opp) != 0)
					{
						newBoard.intel |= -32768;
						makeCapture(newBoard, newPos);
					}
					else
					{
						newBoard.intel &= 32767;
					}
				}
				newBoard.whiteTurn = !newBoard.whiteTurn;
				moves.add(newBoard);
				nMoves -= newPos;
			}
			knightBoard -= 1L << knight;
		}
		
		long rookBoard = (b.whiteTurn ? b.WR : b.BR);
		while(rookBoard != 0) //there are rooks
		{
			int rook = Long.numberOfTrailingZeros(rookBoard);
			long rMoves = rookMoves(b, rook, blockers) & ~my;
			
			while(rMoves != 0)
			{
				int pos = Long.numberOfTrailingZeros(rMoves);
				newPos = 1L << pos;
				Board newBoard = b.clone();
				newBoard.intel &= ~16384;
				if(b.whiteTurn)
				{
					if(rook == 0 && (newBoard.intel & 128) == 128)
					{
						newBoard.intel &= ~128;
						newBoard.hash ^= zExtra[1];
					}
					if(rook == 7 && (newBoard.intel & 256) == 256)
					{
						newBoard.intel &= ~256;
						newBoard.hash ^= zExtra[2];
					}
					newBoard.WR += newPos - (1L << rook);
					newBoard.hash ^= z[rook][1] ^ z[pos][1];
					if((newPos & opp) != 0)
					{
						newBoard.intel |= -32768;
						makeCapture(newBoard, newPos);
					}
					else
					{
						newBoard.intel &= 32767;
					}
				}
				else
				{
					if(rook == 56 && (newBoard.intel & 512) == 512)
					{
						newBoard.intel &= ~512;
						newBoard.hash ^= zExtra[3];
					}
					if(rook == 63 && (newBoard.intel & 1024) == 1024)
					{
						newBoard.intel &= ~1024;
						newBoard.hash ^= zExtra[4];
					}
					newBoard.BR += newPos - (1L << rook);
					newBoard.hash ^= z[rook][7] ^ z[pos][7];
					if((newPos & opp) != 0)
					{
						newBoard.intel |= -32768;
						makeCapture(newBoard, newPos);
					}
					else
					{
						newBoard.intel &= 32767;
					}
				}
				newBoard.whiteTurn = !newBoard.whiteTurn;
				moves.add(newBoard);
				rMoves -= newPos;
			}
			rookBoard -= 1L << rook;
		}
		
		long bishopBoard = (b.whiteTurn ? b.WB : b.BB);
		while(bishopBoard != 0) //there are bishops
		{
			int bishop = Long.numberOfTrailingZeros(bishopBoard);
			long bMoves = bishopMoves(b, bishop, blockers) & ~my;
			while(bMoves != 0)
			{
				int pos = Long.numberOfTrailingZeros(bMoves);
				newPos = 1L << pos;
				Board newBoard = b.clone();
				newBoard.intel &= ~16384;
				if(b.whiteTurn)
				{
					newBoard.WB += newPos - (1L << bishop);
					newBoard.hash ^= z[bishop][3] ^ z[pos][3];
					if((newPos & opp) != 0)
					{
						newBoard.intel |= -32768;
						makeCapture(newBoard, newPos);
					}
					else
					{
						newBoard.intel &= 32767;
					}
				}
				else
				{
					newBoard.BB += newPos - (1L << bishop);
					newBoard.hash ^= z[bishop][9] ^ z[pos][9];
					if((newPos & opp) != 0)
					{
						newBoard.intel |= -32768;
						makeCapture(newBoard, newPos);
					}
					else
					{
						newBoard.intel &= 32767;
					}
				}
				newBoard.whiteTurn = !newBoard.whiteTurn;
				moves.add(newBoard);
				bMoves -= newPos;
			}
			bishopBoard -= 1L << bishop;
		}
		
		long queenBoard = (b.whiteTurn ? b.WQ : b.BQ);
		while(queenBoard != 0) //there are queens
		{
			int queen = Long.numberOfTrailingZeros(queenBoard);
			long qMoves = (bishopMoves(b, queen, blockers) | rookMoves(b, queen, blockers)) & ~my;
			while(qMoves != 0)
			{
				int pos = Long.numberOfTrailingZeros(qMoves);
				newPos = 1L << pos;
				Board newBoard = b.clone();
				newBoard.intel &= ~16384;
				if(b.whiteTurn)
				{
					newBoard.WQ += newPos - (1L << queen);
					newBoard.hash ^= z[queen][4] ^ z[pos][4];
					if((newPos & opp) != 0)
					{
						makeCapture(newBoard, newPos);
					}
					else
					{
						newBoard.intel &= 32767;
					}
				}
				else
				{
					newBoard.BQ += newPos - (1L << queen);
					newBoard.hash ^= z[queen][10] ^ z[pos][10];
					if((newPos & opp) != 0)
					{
						makeCapture(newBoard, newPos);
					}
					else
					{
						newBoard.intel &= 32767;
					}
				}
				newBoard.whiteTurn = !newBoard.whiteTurn;
				moves.add(newBoard);
				qMoves -= newPos;
			}
			queenBoard -= 1L << queen;
		}
		
		LinkedList<Board> legalMoves = new LinkedList<Board>();
		for(Board move : moves)
		{
			if(isLegal(move))
			{
				move.hash ^= zExtra[0];
				legalMoves.add(move);
			}
		}
		
		return legalMoves;
	}
	
	public static long numOfMoves(Board b, int depth)
	{
		if(depth == 0)
		{
			return 1;
		}
		LinkedList<Board> moves = allMoves(b);
		long ans = 0;
		for(Board move : moves)
		{
			ans += numOfMoves(move, depth-1);
		}
		return ans;
	}
	
	public static String move(Board old, Board now)
	{
		long move = 0;
		if(old.whiteTurn)
		{
			move = old.WK ^ now.WK;
			if(move == 0)
			{
				move = old.WP ^ now.WP;
				if(move == 0)
				{
					move = old.WR ^ now.WR;
					if(move == 0)
					{
						move = old.WB ^ now.WB;
						if(move == 0)
						{
							move = old.WQ ^ now.WQ;
							if(move == 0)
							{
								move = old.WN ^ now.WN;
							}
						}
					}
				}
			}
		}
		else
		{
			move = old.BK ^ now.BK;
			if(move == 0)
			{
				move = old.BP ^ now.BP;
				if(move == 0)
				{
					move = old.BR ^ now.BR;
					if(move == 0)
					{
						move = old.BB ^ now.BB;
						if(move == 0)
						{
							move = old.BQ ^ now.BQ;
							if(move == 0)
							{
								move = old.BN ^ now.BN;
							}
						}
					}
				}
			}
		}
		int start = Long.numberOfTrailingZeros(move & old.myPieces());
		int end = Long.numberOfTrailingZeros(move & now.oppPieces());
		String endChar = "";
		if(end == 64)
		{
			if(old.whiteTurn)
			{
				if((old.WQ ^ now.WQ) != 0)
				{
					end = Long.numberOfTrailingZeros(old.WQ ^ now.WQ);
					endChar = "q";
				}
				else if((old.WR ^ now.WR) != 0)
				{
					end = Long.numberOfTrailingZeros(old.WR ^ now.WR);
					endChar = "r";
				}
				else if((old.WB ^ now.WB) != 0)
				{
					end = Long.numberOfTrailingZeros(old.WB ^ now.WB);
					endChar = "b";
				}
				else if((old.WN ^ now.WN) != 0)
				{
					end = Long.numberOfTrailingZeros(old.WN ^ now.WN);
					endChar = "n";
				}
			}
			else
			{
				if((old.BQ ^ now.BQ) != 0)
				{
					end = Long.numberOfTrailingZeros(old.BQ ^ now.BQ);
					endChar = "q";
				}
				else if((old.BR ^ now.BR) != 0)
				{
					end = Long.numberOfTrailingZeros(old.BR ^ now.BR);
					endChar = "r";
				}
				else if((old.BB ^ now.BB) != 0)
				{
					end = Long.numberOfTrailingZeros(old.BB ^ now.BB);
					endChar = "b";
				}
				else if((old.BN ^ now.BN) != 0)
				{
					end = Long.numberOfTrailingZeros(old.BN ^ now.BN);
					endChar = "n";
				}
			}
		}
		String ans = (char)(8 - start%8 + 96)+""+(start/8 + 1)+""+(char)(8 - end%8 + 96)+""+(end/8 + 1)+endChar;
		return ans;
	}

	public static int search(Board board, int depth, int a, int b)
	{
		nodes++;
		
		//store a copy of alpha, since it will change
		int original = a;
		
		Board bestMove = null;
		
		if(depth == 0) //search has gone far enough, evaluate
		{
			a = eval(board);
			bestMove = TT.getOrDefault(board.hash, null);
		}
		else
		{
			if(TT.containsKey(board.hash)) //this was searched before, use saved result
			{
				int TTval = TT.get(board.hash).TTintel;
				int TTdepth = (TTval & 1022) >> 2;
				int TTscore = TTval >> 10;
				
				//disregard the saved information if it was saved at shallow depth
				//(if we are currently searching a larger depth, we want a better result than pre-saved info from a shallow depth)
				if(TTdepth >= depth)
				{
					//previous search saved was...
					switch(TTval % 4)
					{
						//...an exact search, so return the value
						case EXACT:
						{
							return TTscore;
						}
						
						//...a fail-low
						case LOWERBOUND:
						{
							a = Math.max(a, TTscore);
						}
						
						//...a fail-high
						case UPPERBOUND:
						{
							b = Math.min(b, TTscore);
						}
					}
				}
				
				//fail-high, exit
				if(a >= b)
				{
					return TTscore;
				}
			}
			
			LinkedList<Board> moves = allMoves(board);
			if(TT.containsKey(board.hash))
			{
				moves.addFirst(TT.get(board.hash));
			}
			
			if(moves.size() == 0) //there are no moves that can be made, must be either stalemate or checkmate
			{
				//flip the 'turn-to-move', and test if legal position (then flip back!)
				//if legal, we have no moves, so it is stalemate
				//if not, we are in check, and since we have no moves, it is checkmate
				board.whiteTurn = !board.whiteTurn;
				boolean legal = isLegal(board);
				board.whiteTurn = !board.whiteTurn;
				
				if(legal)
				{
					return 0;
				}
				else
				{
					return -99_999;
				}
			}
			
			
			//for each possible move, recursively search
			int pos = 0;
			for(Board move : moves)
			{				
				int score;
				
				score = -search(move, depth-1, -b, -a);
				
				//adjust alpha to a new minimum
				if(a < score)
				{
					a = score;
					bestMove = move;
				}
				
				//if alpha has reached beta, it is a fail-high, exit
				if(a >= b)
				{
					break;
				}
				pos++;
			}
		}
		
		
		//if the score is a mate score, change it slightly to indicate how many moves until mate
		//(mate scores are so large, small changes like this will not fool the engine into choosing something else)
		if(a > 99_000)
		{
			a--;
		}
		if(a < -99_000)
		{
			a++;
		}
		
		//if we found a best move, save what we have learned
		if(bestMove != null)
		{
			int store = (a << 10) + (depth << 2);
			
			//if alpha was reduced, mark as a fail-low
			if(a <= original)
			{
				store += UPPERBOUND;
			}
			
			//if alpha reached beta, mark as a fail-high
			else if(a >= b)
			{
				store += LOWERBOUND;
			}
			
			bestMove.TTintel = store;
			TT.put(board.hash, bestMove);
		}
		
		return a;
	}
	
	public static int eval(Board board)
	{
		int ans = 0;
		//net eval
		if(Long.bitCount(board.blockers()) > 6)
		{
			int pos = 0;
			long worker = board.WP;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans += evals[0][pos];
				worker -= 1L << pos;
			}
			worker = board.WN;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans += evals[1][pos];
				worker -= 1L << pos;
			}
			worker = board.WB;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans += evals[2][pos];
				worker -= 1L << pos;
			}
			worker = board.WR;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans += evals[3][pos];
				worker -= 1L << pos;
			}
			worker = board.WQ;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans += evals[4][pos];
				worker -= 1L << pos;
			}
			
			
			worker = board.BP;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans -= evals[0][63-pos];
				worker -= 1L << pos;
			}
			worker = board.BN;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans -= evals[1][63-pos];
				worker -= 1L << pos;
			}
			worker = board.BB;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans -= evals[2][63-pos];
				worker -= 1L << pos;
			}
			worker = board.BR;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans -= evals[3][63-pos];
				worker -= 1L << pos;
			}
			worker = board.BQ;
			while(worker != 0)
			{
				pos = Long.numberOfTrailingZeros(worker);
				ans -= evals[4][63-pos];
				worker -= 1L << pos;
			}
		}
		//basic eval
		else
		{
			ans += Long.bitCount(board.WP);
			ans += 3*Long.bitCount(board.WN);
			ans += 3*Long.bitCount(board.WB);
			ans += 5*Long.bitCount(board.WR);
			ans += 9*Long.bitCount(board.WQ);
			
			ans -= Long.bitCount(board.BP);
			ans -= 3*Long.bitCount(board.BN);
			ans -= 3*Long.bitCount(board.BB);
			ans -= 5*Long.bitCount(board.BR);
			ans -= 9*Long.bitCount(board.BQ);
			
			ans *= 100;
		}

		return ans * (board.whiteTurn ? 1 : -1);
	}
	
	public static String PV(Board b, int depth)
	{
		if(depth == 0)
		{
			return "";
		}
		
		Board nextBest = TT.getOrDefault(b.hash, null);
		
		if(nextBest == null)
		{
			return "";
		}
		else
		{
			return move(b, nextBest) + " " + PV(nextBest, depth-1);
		}
	}
	
	public static void UCI()
	{
		Scanner read = new Scanner(System.in);
		while(true)
		{
			String input = read.nextLine();
			switch(input)
			{
				case "uci" ->
				{
					System.out.println("id name ChessBot");
					System.out.println("id author Thomas");
					System.out.println("uciok");
				}
				case "isready" ->
				{
					System.out.println("readyok");
				}
				case "ucinewgame" ->
				{
					board = newGameBoard();
				}
				case "quit" ->
				{
					System.exit(0);
				}
				case "print" ->
				{
					
				}
			}
			if(input.startsWith("position "))
			{
				input = input.substring(9);
				if(input.startsWith("startpos"))
				{
					input = input.substring(8);
					FENParser("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"); //this resets the chessBoard[][] variable
					board = newGameBoard();
				}
				else if(input.startsWith("fen "))
				{
					input = input.substring(4);
					board = FENParser(input);
				}
				if(input.contains("moves"))
				{
					try
					{
						int pos = input.indexOf("moves");
						input = input.substring(pos+6);
						board = newGameBoard();
						Scanner moves = new Scanner(input);
						while(moves.hasNext())
						{
							String move = moves.next();
							LinkedList<Board> possibleMoves = allMoves(board);
							for(Board b : possibleMoves)
							{
								if(move(board, b).equals(move))
								{
									board = b;
									break;
								}
							}
						}
						moves.close();
					}
					catch(Throwable e)
					{
						e.printStackTrace();
					}
				}
			}
			else if(input.startsWith("go"))
			{
				long start = System.nanoTime();
				int i = 1;
				for(; i<=100; i++)
				{
					if(System.nanoTime()-start > 1_000_000_000L)
					{
						break;
					}
					
					DEPTH = i;
					int best = search(board, i, -200_000, 200_000);
					
					long time = Math.round((System.nanoTime()-start)/1_000_000.0);
					if(best < -99_900)
					{
						int movesToMate = (int)Math.round((best + 100_000));
						System.out.println("info depth "+i+" nodes "+nodes+" score mate -"+(movesToMate+1)/2+" time "+time+" nps "+Math.round((nodes*1.0/time) * 1_000)+" hashfull 0 tbhits 0 pv "+PV(board, i));
						break;
					}
					else if(best > 99_900)
					{
						int movesToMate = (int)Math.round((100_000 - best));
						System.out.println("info depth "+i+" nodes "+nodes+" score mate "+(movesToMate+1)/2+" time "+time+" nps "+Math.round((nodes*1.0/time) * 1_000)+" hashfull 0 tbhits 0 pv "+PV(board, i));
						break;
					}
					else
					{
						System.out.println("info depth "+i+" nodes "+nodes+" score cp "+best+" time "+time+" nps "+Math.round((nodes*1.0/time) * 1_000)+" hashfull 0 tbhits 0 pv "+PV(board, i));
					}
					
					//System.out.println("hash table size: "+TT.size());
				}
				Board moveToMake = TT.getOrDefault(board.hash, null);
				System.out.println("bestmove "+move(board, moveToMake));
				nodes = 0;
				TT.clear();
			}
		}
	}
	
	
	public static void main(String[] args) throws InterruptedException, FileNotFoundException
	{
		evalSetup();
		raySetup();
		zobristSetup();
		board = newGameBoard();
		UCI();
	}
}