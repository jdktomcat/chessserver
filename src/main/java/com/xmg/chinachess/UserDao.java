package com.xmg.chinachess;

import java.sql.ResultSet;

public class UserDao {
	public static void win(User user) throws Exception {
		String sql = "update user set score=score+2,victory_count=victory_count+1 where name='" + user.getName() + "';";
		DbUtil db = new DbUtil();
		try {
			db.execute(sql);
		} catch (Exception e) {
			throw e;
		} finally {
			db.close();
		}
	}

	public static void defeat(User user) throws Exception {
		String sql = "update user set score=score-2,defeat_count=defeat_count+1 where name='" + user.getName() + "';";
		DbUtil db = new DbUtil();
		try {
			db.execute(sql);
		} catch (Exception e) {
			throw e;
		} finally {
			db.close();
		}
	}

	public static void draw(User user) throws Exception {
		String sql = "update user set draw_count=draw_count+1 where name='" + user.getName() + "';";
		DbUtil db = new DbUtil();
		try {
			db.execute(sql);
		} catch (Exception e) {
			throw e;
		} finally {
			db.close();
		}
	}

	// 更新数据
	public void update(User user) throws Exception {
		String sql = "update user set head=" + user.getHead() + ",score=" + user.getScore() + ",victory_count="
				+ user.getViCount() + ",defeat_count=" + user.getDeCount() + ",draw_count=" + user.getDrCount()
				+ " where name='" + user.getName() + "';";
		DbUtil db = new DbUtil();
		try {
			db.execute(sql);
		} catch (Exception e) {
			throw e;
		} finally {
			db.close();
		}
	}

	// 修改密码
	public void updatePassword(User user) throws Exception {
		String sql = "update user set password='" + user.getPassword() + "' where name='" + user.getName() + "';";
		DbUtil db = new DbUtil();
		try {
			db.execute(sql);
		} catch (Exception e) {
			throw e;
		} finally {
			db.close();
		}
	}

	// 注册
	public void sign(User user) throws Exception {
		String sql = "insert into user (name,password,head,join_time)values('" + user.getName() + "','"
				+ user.getPassword() + "','" + user.getHead() + "',now())";
		DbUtil db = new DbUtil();
		try {
			db.execute(sql);
		} catch (Exception e) {
			throw e;
		} finally {
			db.close();
		}
	}

	// 登陆
	public void login(User user) throws Exception {
		String sql = "select name,head,score,victory_count,defeat_count,draw_count from user where name='"
				+ user.getName() + "' and password='" + user.getPassword() + "' ";
		DbUtil db = new DbUtil();
		try {
			ResultSet rs = db.executeQuerry(sql);
			if (rs.next()) {
				user.setPassword("*");
				user.setName(rs.getString(1));
				user.setHead(rs.getInt(2) + "");
				user.setScore(rs.getInt(3));
				user.setViCount(rs.getInt(4));
				user.setDeCount(rs.getInt(5));
				user.setDrCount(rs.getInt(6));
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			db.close();
		}
	}

}
