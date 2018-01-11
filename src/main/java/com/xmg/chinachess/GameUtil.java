package com.xmg.chinachess;

public class GameUtil {
	public static final int CHE = 7, MA = 5, XIANG = 2, SHI = 1, JIANG = 4, PAO = 3, BING = 6;

	// 默认地图（棋盘）
	public static final int[][] DEFAULT_MAP = { { 107, 105, 102, 101, 104, 101, 102, 105, 107 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 103, 0, 0, 0, 0, 0, 103, 0 }, { 106, 0, 106, 0, 106, 0, 106, 0, 106 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 206, 0, 206, 0, 206, 0, 206, 0, 206 },
			{ 0, 203, 0, 0, 0, 0, 0, 203, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 207, 205, 202, 201, 204, 201, 202, 205, 207 } };

	

	/**
	 * 传入地图，棋步；若棋子能走返回true，否则返回false。
	 * 
	 * @param map
	 * @param walk
	 * @return
	 */
	public static boolean canWalk(int map[][], Walk walk) {
		int color1 = getColor(map[walk.y1][walk.x1]);
		int color2 = getColor(map[walk.y2][walk.x2]);
		if (color1 == color2) {
			return false;
		}
		int code1 = map[walk.y1][walk.x1] % 100;
		int code2 = map[walk.y2][walk.x2] % 100;
		Walk w;
		int m[][];
		// 如果走棋者是黑方，则旋转地图和walk
		if (color1 == 1) {
			w = rotateWalk(walk);
			m = rotateMap(map);
		} else {
			w = walk;
			m = cloneMap(map);
		}
		int x1 = w.x1;
		int x2 = w.x2;
		int y1 = w.y1;
		int y2 = w.y2;
		if (code1 == CHE) {
			// ①车走直线，不能挡车
			if (x2 - x1 == 0) {
				// 竖着走
				int len = Math.abs(y2 - y1);
				int dir = (y2 - y1) / len;
				for (int i = 1; i < len; i++) {
					if (m[y1 + dir * i][x1] != 0) {
						// 遇到障碍
						return false;
					}
				}
				return true;
			} else if (y2 - y1 == 0) {
				// 横着走
				int len = Math.abs(x2 - x1);
				int dir = (x2 - x1) / len;
				for (int i = 1; i < len; i++) {
					if (m[y1][x1 + dir * i] != 0) {
						// 遇到障碍
						return false;
					}
				}
				return true;
			} else {
				// 斜着走
				return false;
			}
		} else if (code1 == MA) {
			// ②马走日，前压马腿
			if (Math.abs(x2 - x1) == 1) {
				if (Math.abs(y2 - y1) == 2) {
					if (m[(y1 + y2) / 2][x1] != 0) {
						// 压马腿
						return false;
					} else {
						return true;
					}
				} else {
					return false;
				}
			} else if (Math.abs(y2 - y1) == 1) {
				if (Math.abs(x2 - x1) == 2) {
					if (m[y1][(x1 + x2) / 2] != 0) {
						// 压马腿
						return false;
					} else {
						return true;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if (code1 == XIANG) {
			// ③象走田，前压象腿，不可越河
			if (y2 < 5) {
				return false;
			} else {
				if (Math.abs(x2 - x1) == 2 && Math.abs(y2 - y1) == 2) {
					if (m[(y1 + y2) / 2][(x1 + x2) / 2] != 0) {
						// 压象腿
						return false;
					} else {
						return true;
					}
				} else {
					return false;
				}
			}
		} else if (code1 == SHI) {
			// ④士空走斜一，吃走斜一，不可越将营
			if (x2 < 3 || x2 > 5 || y2 < 6) {
				// 越将营
				return false;
			} else {
				if (Math.abs(x2 - x1) == 1 && Math.abs(y2 - y1) == 1) {
					return true;
				} else {
					return false;
				}
			}
		} else if (code1 == JIANG) {
			// ⑤将走直一，（吃将可走直线可越将营），不可越将营
			if (x2 < 3 || x2 > 5 || y2 < 6) {
				// 越将营(吃对面将)
				if (code2 == JIANG) {
					if (x2 - x1 == 0) {
						// 竖着走
						int len = Math.abs(y2 - y1);
						int dir = (y2 - y1) / len;
						for (int i = 1; i < len; i++) {
							if (m[y1 + dir * i][x1] != 0) {
								// 遇到障碍
								return false;
							}
						}
						return true;
					}
				}
				return false;
			} else {
				int dx = Math.abs(x2 - x1);
				int dy = Math.abs(y2 - y1);
				if ((dx == 0 && dy == 1) || (dx == 1 && dy == 0)) {
					return true;
				} else {
					return false;
				}
			}
		} else if (code1 == PAO) {
			// ⑥炮空走直线，吃走炮台
			if (x2 - x1 == 0) {
				// 竖着走
				int len = Math.abs(y2 - y1);
				int dir = (y2 - y1) / len;
				// 障碍数量
				int count = 0;
				for (int i = 1; i < len; i++) {
					if (m[y1 + dir * i][x1] != 0) {
						// 遇到障碍
						count++;
					}
				}
				if (m[y2][x2] == 0) {
					// 空炮
					if (count == 0) {
						return true;
					} else {
						return false;
					}
				} else {
					// 飞炮
					if (count == 1) {
						return true;
					} else {
						return false;
					}
				}
			} else if (y2 - y1 == 0) {
				// 横着走
				int len = Math.abs(x2 - x1);
				int dir = (x2 - x1) / len;
				// 障碍数量
				int count = 0;
				for (int i = 1; i < len; i++) {
					if (m[y1][x1 + dir * i] != 0) {
						// 遇到障碍
						count++;
					}
				}
				if (m[y2][x2] == 0) {
					// 空炮
					if (count == 0) {
						return true;
					} else {
						return false;
					}
				} else {
					// 飞炮
					if (count == 1) {
						return true;
					} else {
						return false;
					}
				}
			} else {
				// 斜着走
				return false;
			}
		} else if (code1 == BING) {
			// ⑦兵可进不可退，走直一
			if (y1 < 5) {
				// 兵越河
				if (x2 - x1 == 0) {

					if (y1 - y2 == 1) {
						return true;
					} else {
						return false;
					}
				} else if (y2 - y1 == 0) {
					if (Math.abs(x2 - x1) == 1) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				// 兵未越河
				if ((x2 - x1 == 0) && (y1 - y2 == 1)) {
					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}

	// 复制地图
	public static int[][] cloneMap(int[][] map) {
		int m[][] = new int[map.length][];
		for (int i = 0; i < m.length; i++) {
			m[i] = map[i].clone();
		}
		return m;
	}

	public static int getColor(int code) {
		return code / 100;
	}

	/**
	 * 旋转地图
	 * 
	 * @param map
	 * @return
	 */
	public static int[][] rotateMap(int map[][]) {
		int m[][] = new int[10][9];
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 9; j++) {
				m[9 - i][8 - j] = map[i][j];
			}
		}
		return m;
	}

	/**
	 * 旋转走棋步骤坐标（将黑方改为红方）
	 * 
	 * @param w
	 * @return
	 */
	public static Walk rotateWalk(Walk w) {
		Walk w1 = new Walk();
		w1.x1 = 8 - w.x1;
		w1.x2 = 8 - w.x2;
		w1.y1 = 9 - w.y1;
		w1.y2 = 9 - w.y2;
		return w1;
	}

}
