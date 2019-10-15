package rddl.competition.generators;

/**
 *  A generator for instances of a fully observable game of life.
 *  
 *  @author Scott Sanner
 *  @version 3/1/11
 * 
 **/

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;

import rddl.competition.Records;
import rddl.policy.*;

public class PocmanPOMDPGen {
	
	static int[][] oriMap = null;
	//empty = 0; wall = 1; food = 2; pill = 3;  
	/*
	static int [][] oriMap = new int[][]{
			{2, 0, 0, 1, 3, 2, 0, 1, 1, 1, 0, 1, 1, 1, 0, 2, 3, 0, 2},
			{2, 1, 2, 0, 2, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 2, 1, 0},
			{2, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 2, 1, 0, 1, 0},
			{0, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 2, 2, 0, 2, 0, 2, 2},
			{2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0},
			{2, 1, 2, 2, 0, 1, 2, 2, 0, 0, 0, 0, 0, 1, 2, 2, 0, 1, 2},
			{0, 1, 2, 1, 2, 1, 0, 1, 0, 1, 1, 1, 0, 1, 2, 1, 0, 1, 2},
			{0, 0, 0, 1, 2, 2, 2, 1, 0, 1, 0, 0, 0, 2, 0, 1, 2, 2, 0},
			{0, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1, 0},
			{2, 2, 0, 1, 0, 0, 2, 1, 0, 1, 0, 0, 0, 2, 2, 1, 2, 0, 0},
			{2, 1, 0, 1, 2, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 2, 1, 2},
			{0, 1, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0 ,1, 2, 2, 2, 1, 0},
			{2, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0},
			{2, 1, 0, 2, 0, 2, 2, 2, 2, 0 ,0, 2, 2, 2, 2, 0, 0, 2, 2},
			{2, 1, 0, 1, 1, 1, 2, 1, 1, 1, 0, 1, 1, 1, 2, 1, 0, 1, 0},
			{0, 1, 2, 0, 2, 1, 2, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 2},
			{0, 0, 0, 1, 3, 2, 0, 1, 1, 1, 0, 1, 1, 1, 2, 2, 3, 0, 0},
	};
	*/
	public static void main(String[] args) throws Exception {
		
		Random r = new Random(2);
		int sizeX = Integer.valueOf(args[2]);
		int sizeY = Integer.valueOf(args[3]);
		int numGhosts = Integer.valueOf(args[4]);
		
		String Dir = args[0];
		String insName = args[1];
		
		char[][] map = new char[sizeX][sizeY];
		
		String content = new String();
		
		content += "non-fluents nf_pocman_inst_pomdp__" + insName + " {\n";
		content += "domain = pocman_pomdp;\n";
		content += "objects {\n";
		content += "xpos : {";
		for(int i = 0; i < sizeX - 1; i ++) {
			content += "x" + i + ", ";
		}
		content += "x" + (sizeX - 1) + "};\n";
		
		content += "ypos : {";
		for(int i = 0; i < sizeY - 1; i ++) {
			content += "y" + i + ", ";
		}
		content += "y" + (sizeY - 1) + "};\n";
		
		content += "agent: {a0};\n";
		
		content += "ghost : {";
		for(int i = 0; i < numGhosts - 1; i ++) {
			content += "g" + i + ", ";
		}
		content += "g" + (numGhosts - 1) + "};\n";
		
		content += "step : {";
		for(int i = 0; i < 14; i ++) {
			content += "s" + i + ", ";
		}
		content += "s" + 14 + "};\n";
		
		content += "};\n";
		
		content += "non-fluents {\n";
		
		for(int i = 0; i < sizeX; i ++) {
			for(int j = 0; j < sizeY; j ++) {
				if(oriMap == null) {
					if(r.nextDouble() < 0.15) {
						map[i][j] = 'w';
						content += "WALL-AT(x" + i + ", y" + j + ");\n";
					}
				}
				else {
					if(oriMap[i][j] == 1) {
						map[i][j] = 'w';
						content += "WALL-AT(x" + i + ", y" + j + ");\n";
					}
				}
			}
		}
		
		
		//NORTH SOUTH
		for(int j = 0; j < sizeY - 1; j ++) {
			content += "NORTH(y" + j + ", y" + (j+1) + ");\n";
			content += "SOUTH(y" + (j+1) + ", y" + (j) + ");\n";
		}
		
		//WEST EAST
		for(int i = 0; i < sizeX - 1; i ++) {
			content += "EAST(x" + i + ", x" + (i+1) + ");\n";
			content += "WEST(x" + (i+1) + ", x" + (i) + ");\n";
		}
		
		//food and pill
		for(int i = 0; i < sizeX; i ++) {
			for(int j = 0; j < sizeY; j ++) {
				if(oriMap == null) {
					if(map[i][j] != 'w') {
						if(r.nextDouble() < 0.6) {
							map[i][j] = 'f';
							content += "FOOD-AT(x" + i + ", y" + j + ");\n";
						}
						else {
							if(r.nextDouble() < ((4.0 / sizeX / sizeY) < 0.1 ? (4.0 / sizeX / sizeY) : 0.1)){
								map[i][j] = 'p';
								content += "PILL-AT(x" + i + ", y" + j + ");\n";
							}
						}
					}
				}
				else {
					if(oriMap[i][j] == 2) {
						map[i][j] = 'f';
						content += "FOOD-AT(x" + i + ", y" + j + ");\n";
					}
					else {
						if(oriMap[i][j] == 3){
							map[i][j] = 'p';
							content += "PILL-AT(x" + i + ", y" + j + ");\n";
						}
					}
				}
			}
		}
		
		//ranges
		for(int i = 0; i < sizeX; i ++) {
			for(int j = 0; j < sizeY; j ++) {
				for(int y = j + 1; y < sizeY; y ++) {
					if(map[i][y] != 'w') {
						content += "NORTH-VIS(x" + i + ", y" + j + ", y" + y + ");\n";
					}
					else {
						break;
					}
				}
				
				for(int y = j - 1; y >= 0; y --) {
					if(map[i][y] != 'w') {
						content += "SOUTH-VIS(x" + i + ", y" + j + ", y" + y + ");\n";
					}
					else {
						break;
					}
				}
				
				for(int x = i - 1; x >= 0; x --) {
					if(map[x][j] != 'w') {
						content += "WEST-VIS(x" + i + ", y" + j + ", x" + x + ");\n";
					}
					else {
						break;
					}
				}
				
				for(int x = i + 1; x < sizeX; x ++) {
					if(map[x][j] != 'w') {
						content += "EAST-VIS(x" + i + ", y" + j + ", x" + x + ");\n";
					}
					else {
						break;
					}
				}
				
				int left = i - 1 < 0 ? 0 : i - 1;
				int right = i + 1 > sizeX - 1 ? sizeX - 1 : i + 1 ;
				int top = j + 1 > sizeY - 1 ? sizeY - 1 : j + 1 ;;
				int bot = j - 1 < 0 ? 0 : j - 1;;
				for(int x = left; x <= right; x ++) {
					for(int y = bot; y <= top; y ++) {
						content += "SMELL-RANGE(x" + i + ", y" + j + ", x" + x + ", y" + y + ");\n";
						content += "HEAR-RANGE(x" + i + ", y" + j + ", x" + x + ", y" + y + ");\n";
					}
				}
				
				if(j - 2 >= 0) {
					content += "HEAR-RANGE(x" + i + ", y" + j + ", x" + i + ", y" + (j-2) + ");\n";
				}
				
				if(j + 2 < sizeY) {
					content += "HEAR-RANGE(x" + i + ", y" + j + ", x" + i + ", y" + (j+2) + ");\n";
				}
				
				if(i - 2 >= 0) {
					content += "HEAR-RANGE(x" + i + ", y" + j + ", x" + (i-2) + ", y" + j + ");\n";
				}
				
				if(i + 2 < sizeX) {
					content += "HEAR-RANGE(x" + i + ", y" + j + ", x" + (i+2) + ", y" + j + ");\n";
				}
				
				//ghost detect agent in ED <= 5
				for(int x = 0; x < sizeX; x ++) {
					for(int y = 0; y < sizeY; y ++) {
						if(ED(i, j, x, y) <= 5) {
							String dir = null;
							double minDis = ED(i, j, x, y);
							if(i - 1 >= 0 && ED(i-1, j, x, y) < minDis) {
								if(i-1 >= 0 && map[i-1][j] != 'w') {
									minDis = ED(i-1, j, x, y);
									dir = "WEST";
								}
								
							}
							if(i + 1 < sizeX && ED(i+1, j, x, y) < minDis) {
								if(i+1 >= 0 && map[i+1][j] != 'w') {
									minDis = ED(i+1, j, x, y);
									dir = "EAST";
								}
							}
							if(j + 1 < sizeY && ED(i, j+1, x, y) < minDis) {
								if(j+1 >= 0 && map[i][j+1] != 'w') {
									minDis = ED(i, j+1, x, y);
									dir = "NORTH";
								}
							}
							if(j - 1 >= 0 && ED(i, j-1, x, y) < minDis) {
								if(j-1 >= 0 && map[i][j-1] != 'w') {
									minDis = ED(i, j-1, x, y);
									dir = "SOUTH";
								}
							}
							if(dir != null)
								content += "DETECT-" + dir + "(x" + i + ", y" + j + ", x" + x + ", y" + y + ");\n";
							
							dir = null;
							//escape to east
							double maxDis = ED(i, j, x, y);
							if(i + 1 < sizeX && ED(i+1, j, x, y) > maxDis) {
								if(map[i+1][j] != 'w') {
									maxDis = ED(i+1, j, x, y);
									dir = "WEST";
								}
								
							}
							//escape to west
							if(i - 1 >= 0 && ED(i-1, j, x, y) > maxDis) {
								if(map[i-1][j] != 'w') {
									maxDis = ED(i-1, j, x, y);
									dir = "EAST";
								}
							}
							//escape to south
							if(j - 1 >= 0 && ED(i, j-1, x, y) > maxDis) {
								if(map[i][j-1] != 'w') {
									maxDis = ED(i, j-1, x, y);
									dir = "NORTH";
								}
							}
							//escape to north
							if(j + 1 < sizeY && ED(i, j+1, x, y) > maxDis) {
								if(map[i][j+1] != 'w') {
									maxDis = ED(i, j+1, x, y);
									dir = "SOUTH";
								}
							}
							if(dir != null)
								content += "DETECT-" + dir + "-DEF" + "(x" + i + ", y" + j + ", x" + x + ", y" + y + ");\n";
						}
						
					}
				}
				
			}
		}
		
		//NEXT-STEP
		for(int i = 14; i >= 1; i --) {
			content += "NEXT-STEP(s" + i + ", s" + (i-1) + ");\n";
		}
		
		//INITIAL-STEP
		content += "INITIAL-STEP(s14);\n";
		content += "BASE-STEP(s0);\n";
		
		content += "};\n";
		content += "}\n";
		
		content += "instance pocman_inst_pomdp__" + insName + " {\n";
		content += "domain = pocman_pomdp;\n";
		content += "non-fluents = nf_pocman_inst_pomdp__" + insName + ";\n"; 
		content += "init-state {\n";
		
		
		
		content += "power-step(s0);\n";
		
		if(oriMap != null) {
			content += "agent-at-x(x8);\n";
			content += "agent-at-y(y5);\n";
			content += "ghost-at-x(g0, x8);\n";
			content += "ghost-at-x(g0, x9);\n";
			content += "ghost-at-x(g1, x8);\n";
			content += "ghost-at-x(g1, x10);\n";
			content += "ghost-at-x(g2, x9);\n";
			content += "ghost-at-x(g2, x9);\n";
			content += "ghost-at-x(g3, x9);\n";
			content += "ghost-at-x(g3, x10);\n";
		}
		
		
		content += "};\n";
		content += "max-nondef-actions = 1;\n";
		content += "horizon = 40;\n";
		content += "discount = 1.0;\n";
		content += "}\n";
		
		Records rec = new Records();
		rec.fileAppendAbsDir(Dir + "pocman_inst_pomdp__" + insName + ".rddl", content);
	}

	static double ED(int i, int j, int x, int y) {
		return Math.sqrt(Math.pow(x - i, 2.0) + Math.pow(y - j, 2.0));
	}

}
