package com.xmg.chinachess;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class Client implements Runnable {
	// 在线用户列表
	public static ArrayList<Client> onLineUsers = new ArrayList<>();
	// 匹配游戏列表
	public static ArrayList<Client> findGameUsers = new ArrayList<>();
	// 游戏实例
	private Game game;
	private User user;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	// 匹配游戏时的就绪状态
	private boolean isOk;

	public Client(Socket socket) {
		this.socket = socket;
	}

	/**
	 * 玩家同意和棋
	 */
	private void agreePeace() {
		gameOver(0, "玩家同意和棋");
	}

	/**
	 * 玩家同意悔棋，更新游戏数据，发送游戏数据
	 */
	private void agreeRollback() {
		if (game == null) {
			return;
		}
		game.rollback();
		try {
			sendLine("game:" + game.toString());
			game.getOthor(this).sendLine("game:" + game.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 请求和棋
	 */
	private void askPeace() {
		if (game == null) {
			return;
		}
		try {
			game.getOthor(this).sendLine("askpeace");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 请求悔棋
	 */
	private void askRollback() {
		if (game == null) {
			return;
		}
		try {
			game.getOthor(this).sendLine("askrollback");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 刷新时间
	 */
	private void askTime() {
		if (game != null) {
			game.updateTime();
			try {
				sendLine("time:" + game.getTime());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 用户取消匹配
	 */
	private void cancleFind() {
		isOk = false;
	}

	/**
	 * 掉线 将自己标记为为就绪状态，如果游戏实例为空，则删除在线列表中的自己，关闭连接
	 */
	public void deLine() {
		isOk = false;
		if (game == null) {
			synchronized (onLineUsers) {
				onLineUsers.remove(this);
			}
		} else {
			return;
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (user != null) {
			System.out.println("用户【" + user.getName() + "】退出，在线人数：" + onLineUsers.size());
		}
		user = null;
	}

	/**
	 * 上线
	 */
	public void enLine() {
		synchronized (onLineUsers) {
			int pos = onLineUsers.indexOf(this);
			if (pos != -1) {
				System.out.println("重复登陆");
				// 存在用户
				Client c = onLineUsers.get(pos);
				game = c.game;
				game.setUser(this);
				try {
					c.sendLine("failed:异地登陆！");
				} catch (Exception e) {
					e.printStackTrace();
				}
				c.game = null;
				c.deLine();
			}
			onLineUsers.add(this);
		}
		System.out.println("用户【" + user.getName() + "】加入，在线人数：" + onLineUsers.size());
	}

	/**
	 * 比较两个Client是否相同
	 */
	@Override
	public boolean equals(Object obj) {
		try {
			return ((Client) obj).user.equals(user);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * 匹配游戏，将自己标记为就绪状态，如果用户的游戏为空，说明用户上次上线没开始游戏，不是断线重连用户，需要进行匹配；
	 * 相反的，用户的游戏实例不为空，说明用户上次上线时进行的游戏还没有结束，则不需要进行匹配了，直接把上次游戏的即时数据通过用户连接发送给用户即可。
	 * 那么如何匹配游戏呢？每次接受到用户发送的匹配命令后，锁定匹配队列，进行遍历扫描；
	 * 如果在匹配队列中找到一个分数与自己相差不超过50的用户，则判断对方是否就绪，如果是就绪状态，就创建一个游戏实例，并通知对方已经加入游戏，
	 * 并删除匹配队中的己方和对方的数据； 如果在匹配队列中没找到匹配用户，就把判断自己是否在匹配队列中，如果不在，就将自己加入匹配队列中去。
	 * 
	 * 最后再次判断，如果用户匹配到游戏，就发送游戏数据给游戏用户。
	 */
	private void findGame() {
		isOk = true;
		Client other = null;
		boolean notFound = true;
		// 匹配游戏
		if (game == null) {
			synchronized (findGameUsers) {
				Iterator<Client> it = findGameUsers.iterator();
				while (it.hasNext()) {
					other = it.next();
					// 对方未就绪
					if (!other.isOk) {
						it.remove();
						continue;
					}
					// 是自己
					if (other.equals(this)) {
						it.remove();
						continue;
					}
					// 积分之差小于50
					if (Math.abs(user.getScore() - other.user.getScore()) < 50) {
						notFound = false;
						game = new Game(this, other);
						other.game = game;
						it.remove();
						break;
					}
				}
				if (notFound) {
					// 未匹配到游戏，将自己加入到匹配队列
					if (!findGameUsers.contains(this)) {
						findGameUsers.add(this);
					}
				}
			}
		} else {
			notFound = false;
			other = game.getOthor(this);
		}
		// 如果匹配到游戏
		if (!notFound) {
			try {
				sendLine("user:" + game.getUser());
				sendLine("game:" + game.toString());
				other.sendLine("user:" + game.getUser());
				other.sendLine("game:" + game.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 传入胜利者数字，0表示无胜利者，即为和棋，1表是胜利者是黑方，2表示胜利者是红方。 通过用户连接发送胜利者和所有走棋步骤给用户； 进行分数统计
	 * 
	 * @param n
	 */
	public void gameOver(int n, String reason) {
		if (game == null) {
			return;
		}
		User user1 = game.getUser1();
		User user2 = game.getUser2();
		UserDao ud = new UserDao();
		if (n == 0) {
			// 和棋
			user1.setDrCount(user1.getDrCount() + 1);
			user2.setDrCount(user2.getDrCount() + 1);
		} else if (n == 1) {
			// 玩家1赢了
			user1.setScore(user1.getScore() + 2);
			user2.setScore(user2.getScore() - 2);
			user1.setViCount(user1.getViCount() + 1);
			user2.setDeCount(user2.getDeCount() + 1);
		} else if (n == 2) {
			// 玩家2赢了
			user1.setScore(user1.getScore() - 2);
			user2.setScore(user2.getScore() + 2);
			user1.setDeCount(user1.getDeCount() + 1);
			user2.setViCount(user2.getViCount() + 1);
		}
		// 更新用户数据
		try {
			ud.update(user1);
			ud.update(user2);
		} catch (Exception e) {
			// 更新失败
			// 暂时不做处理
			e.printStackTrace();
		}
		try {
			game.getOthor(this).sendLine("gameover:" + n + ";" + reason + ";" + game.getAllWalks());
			sendLine("gameover:" + n + ";" + reason + ";" + game.getAllWalks());
		} catch (Exception e) {
			// 用户掉线,因为用户在游戏中，所以上次掉线并没有删除在线列表的数据，所以在这进行删除
			synchronized (onLineUsers) {
				onLineUsers.remove(this);
			}
		}
		game.getOthor(this).game = null;
		game = null;
	}

	/**
	 * 获取用户数据
	 * 
	 * @return
	 */
	public User getUser() {
		return user;
	}

	/**
	 * 玩家放弃游戏，游戏结束
	 */
	private void giveUp() {
		if (game == null) {
			return;
		}
		if (game.getUser1().equals(user)) {
			// 玩家1认输
			gameOver(2, "玩家投降");
		} else {
			// 玩家2 认输
			gameOver(1, "玩家投降");
		}
	}

	/**
	 * 传入用户数据，进行登陆。登陆失败则通过用户连接发送注册失败指令给用户，成功则发送登陆后的数据给用户。
	 * 
	 * @param content
	 */
	private void login(String content) {
		UserDao ud = new UserDao();
		try {
			user = User.fromString(content);
			ud.login(user);
			enLine();
			sendLine("loginback:" + user.toString());
		} catch (Exception e) {
			try {
				sendLine("loginback:failed");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

	@Override
	public void run() {
		try {
			if (onLineUsers.size() > Server.ONLINE_LIMIT) {
				sendLine("failed:在线人数超出服务器限制。");
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			is = socket.getInputStream();
			BufferedReader bd = new BufferedReader(new InputStreamReader(is, "utf8"));
			String data = null;
			while ((data = bd.readLine()) != null) {
				String tag[] = data.split(":");
				String cmd = tag[0];
				String content = null;
				if (tag.length == 2) {
					content = tag[1];
				}
				switch (cmd) {
				case "login":
					login(content);
					break;
				case "sign":
					sign(content);
					break;
				case "findgame":
					findGame();
					break;
				case "msg":
					receveMsg(data);
					break;
				case "walk":
					walk(content);
					break;
				case "askpeace":
					askPeace();
					break;
				case "agreepeace":
					agreePeace();
					break;
				case "askrollback":
					askRollback();
					break;
				case "agreerollback":
					agreeRollback();
					break;
				case "giveup":
					giveUp();
					break;
				case "asktime":
					askTime();
					break;
				case "canclefind":
					cancleFind();
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		deLine();

	}

	private void receveMsg(String data) {
		if(game!=null){
			try {
				sendLine(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				game.getOthor(this).sendLine(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 传入用户数据，进行注册。注册失败则通过用户连接发送注册失败指令给用户，成功则发送注册后的数据给用户
	 * 
	 * @param content
	 */
	private void sign(String content) {
		UserDao ud = new UserDao();
		try {
			user = User.fromString(content);
			ud.sign(user);
			enLine();
			sendLine("signback:" + user.toString());
		} catch (Exception e) {
			try {
				sendLine("signback:failed");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 传入数据字符串data，通过用户连接向用户发送字符串data+"\r\n"。
	 * 
	 * @param data
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void sendLine(String data) throws Exception {
		try {
			os = socket.getOutputStream();
			os.write((data + "\r\n").getBytes("utf8"));
			// System.out.println(data+"\n");
		} catch (Exception e) {
			// 发送失败，说明用户掉线
			deLine();
			throw new Exception("用户掉线");
		}
	}

	/**
	 * 传入走棋步骤，进行走棋计算
	 * 
	 * @param content
	 */
	private void walk(String content) {
		if (game == null) {
			return;
		}
		Walk w = Walk.fromString(content);
		int code = game.getMap()[w.y2][w.x2];
		if (game.walk(this, w)) {
			try {
				sendLine("game:" + game.toString());
				game.getOthor(this).sendLine("game:" + game.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 游戏结束判断
			if (code == (100 + GameUtil.JIANG)) {
				// 黑将被吃
				gameOver(2, "将死");
			}
			if (code == (200 + GameUtil.JIANG)) {
				// 红将被吃
				gameOver(1, "将死");
			}

		} else {
			try {
				sendLine("game:" + game.toString());
				game.getOthor(this).sendLine("game:" + game.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
