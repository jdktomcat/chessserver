package com.xmg.chinachess;

import java.util.ArrayList;

public class Game {

	// 走棋步数
	private int step;
	// 上一次更新游戏数据时间
	private long lastTime;
	// 步时
	private long walkTime;
	// 局时1
	private long time1;

	// 局时2
	private long time2;

	// 玩家1
	private Client user1;

	// 玩家2
	private Client user2;

	// 地图
	private int map[][];

	// 上一次走棋地图
	private int lastMap[][];
	// 走棋步
	private ArrayList<Walk> walks;

	public Game(Client user1, Client user2) {
		this.user1 = user1;
		this.user2 = user2;
		startGame();
	}

	// 返回所有步骤字符串
	public String getAllWalks() {
		String s = "";
		for (int i = 0; i < walks.size(); i++) {
			s += walks.get(i).toString() + ";";
		}
		return s;
	}

	public int[][] getMap() {
		return map;
	}

	public String getMapString() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 9; j++) {
				s.append(map[i][j] + ",");
			}
		}
		s.delete(s.length() - 1, s.length());
		return s.toString();
	}

	public Client getOthor(Client self) {
		if (self.equals(user1)) {
			return user2;
		} else {
			return user1;
		}
	}

	public int getStep() {
		return step;
	}

	public String getTime() {
		// 步时，time1，time2
		return walkTime + "," + time1 + "," + time2;
	}

	public String getUser() {
		return user1.getUser().toString() + ";" + user2.getUser().toString();
	}

	public User getUser1() {
		return user1.getUser();
	}

	public User getUser2() {
		return user2.getUser();
	}

	// 悔棋
	public void rollback() {
		if (lastMap != null) {
			map = lastMap;
			lastMap = null;
			step--;
			walks.remove(walks.size() - 1);
		}
	}

	public void startGame() {
		this.step = 0;
		this.map = GameUtil.cloneMap(GameUtil.DEFAULT_MAP);
		this.lastTime = System.currentTimeMillis();
		this.walkTime = 3 * 60 * 1000;
		this.time1 = 20 * 60 * 1000;
		this.time2 = 20 * 60 * 1000;
		this.lastMap = null;
		this.walks = new ArrayList<>();
	}

	@Override
	public String toString() {
		// 步数，walk，map
		Walk w = null;
		if(walks.size()>0){
			w = walks.get(walks.size() - 1);
		}else{
			w = new Walk(-1, -1, -1, -1);
		}
		return step + ";" + w + ";" + getMapString();
	}

	public synchronized void updateTime() {
		long dis = System.currentTimeMillis() - lastTime;
		lastTime = System.currentTimeMillis();
		walkTime -= dis;
		if (step % 2 == 0) {
			time2 -= dis;
		} else {
			time1 -= dis;
		}
		// 超时检测
		if (walkTime < 0) {
			// user1.gameOver(getColor(code))
			if (step == 0) {
				user1.gameOver(0, "第一步走棋");
			} else {
				if (step % 2 == 0) {
					// 红方超时，判输
					user1.gameOver(1, "红方超时");
					user1.gameOver(2, "红方超时");
				} else {
					user1.gameOver(1, "黑方超时");
					user1.gameOver(2, "黑方超时");
				}
			}
		}
	}

	/**
	 * 走棋，传入走棋的用户，走棋的步；
	 * 
	 * @param user
	 * @param walk
	 */
	public synchronized boolean walk(Client user, Walk w) {
		int color = map[w.y1][w.x1] / 100;
		if (color == 0) {
			return false;
		}
		if (color == 1) {
			if (!user.equals(user1)) {
				return false;
			}
			if (step % 2 == 0) {
				return false;
			}
		}
		if (color == 2) {
			if (!user.equals(user2)) {
				// 选子不是自己的棋子
				return false;
			}
			if (step % 2 == 1) {
				return false;
			}
		}

		// 轮到自己，并且选择点1是自己的棋子
		if (GameUtil.canWalk(this.map, w)) {
			this.lastMap = GameUtil.cloneMap(map);
			map[w.y2][w.x2] = map[w.y1][w.x1];
			map[w.y1][w.x1] = 0;
			this.walks.add(w);
			this.step++;
			// 更新步时
			if (color == 1) {
				if (time2 < 0) {
					walkTime = 1 * 60 * 1000;
				} else {
					walkTime = 3 * 60 * 1000;
				}
			} else {
				if (time1 < 0) {
					walkTime = 1 * 60 * 1000;
				} else {
					walkTime = 3 * 60 * 1000;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public void setUser(Client client) {
		if(user1.equals(client)){
			user1=client;
		}else{
			user2=client;
		}
	}
}
