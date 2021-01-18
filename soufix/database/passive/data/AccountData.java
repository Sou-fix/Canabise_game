package soufix.database.passive.data;

import ch.qos.logback.classic.Level;
import soufix.client.Account;
import soufix.database.passive.AbstractDAO;
import soufix.main.Config;
import soufix.main.Main;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountData extends AbstractDAO<Account>
{

  public AccountData(HikariDataSource source)
  {
    super(source);
    logger.setLevel(Level.ALL);
  }

  public void load(Object id)
  {
    Result result=null;
    try
    {
      result=super.getData("SELECT * FROM accounts WHERE guid = "+id.toString());
      ResultSet RS=result.resultSet;

      while(RS.next())
      {
        Account a=Main.world.getAccount(RS.getInt("guid"));
        if(a!=null&&a.isOnline())
          continue;

        Account C=new Account(RS.getInt("guid"),RS.getString("account").toLowerCase(),RS.getString("pseudo"),RS.getString("reponse"),(RS.getInt("banned")==1),RS.getString("lastIP"),RS.getString("lastConnectionDate"),RS.getString("friends"),RS.getString("enemy"),RS.getInt("points"),RS.getLong("subscribe"),RS.getLong("muteTime"),RS.getString("mutePseudo"),RS.getString("lastVoteIP"),RS.getString("heurevote"),RS.getInt("vip"));
        Main.world.addAccount(C);
       Main.world.ReassignAccountToChar(RS.getInt("guid"));
      }
    }
    catch(Exception e)
    {
    	close(result);
      super.sendError("AccountData load id",e);
    } finally
    {
      close(result);
    }
  }
  public void loadv2(Object id)
  {
    Result result=null;
    try
    {
      result=super.getData("SELECT * FROM accounts WHERE guid = "+id.toString());
      ResultSet RS=result.resultSet;

      while(RS.next())
      {
        Account a=Main.world.getAccount(RS.getInt("guid"));
        if(a!=null&&a.isOnline())
          continue;

        Account C=new Account(RS.getInt("guid"),RS.getString("account").toLowerCase(),RS.getString("pseudo"),RS.getString("reponse"),(RS.getInt("banned")==1),RS.getString("lastIP"),RS.getString("lastConnectionDate"),RS.getString("friends"),RS.getString("enemy"),RS.getInt("points"),RS.getLong("subscribe"),RS.getLong("muteTime"),RS.getString("mutePseudo"),RS.getString("lastVoteIP"),RS.getString("heurevote"),RS.getInt("vip"));
        Main.world.addAccount(C);
        Main.world.ReassignAccountToChar(RS.getInt("guid"));
      }
    }
    catch(Exception e)
    {
    	close(result);
      super.sendError("AccountData load id",e);
    } finally
    {
     close(result);
    }
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=super.getData("SELECT * from accounts");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        if(RS.getString("pseudo").isEmpty())
          continue;
        Account a=new Account(RS.getInt("guid"),RS.getString("account").toLowerCase(),RS.getString("pseudo"),RS.getString("reponse"),(RS.getInt("banned")==1),RS.getString("lastIP"),RS.getString("lastConnectionDate"),RS.getString("friends"),RS.getString("enemy"),RS.getInt("points"),RS.getLong("subscribe"),RS.getLong("muteTime"),RS.getString("mutePseudo"),RS.getString("lastVoteIP"),RS.getString("heurevote"),RS.getInt("vip"));
        Main.world.addAccount(a);
       // Main.world.ReassignAccountToChar(a);
        //a.setLoad_ok(true);
      }
    }
    catch(Exception e)
    {
      super.sendError("AccountData load",e);
      close(result);
    } finally
    {
      close(result);
    }
  }

  public void updateVoteAll()
  {
    Result result=null;
    Account a=null;
    try
    {
      result=super.getData("SELECT guid, heurevote, lastVoteIP from accounts");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        a=Main.world.getAccount(RS.getInt("guid"));
        if(a==null)
          continue;
        a.updateVote(RS.getString("heurevote"),RS.getString("lastVoteIP"));
      }
    }
    catch(SQLException e)
    {
      super.sendError("AccountData updateVoteAll",e);
      close(result);
    } finally
    {
      close(result);
    }
  }

  @Override
  public boolean update(Account acc)
  {
    PreparedStatement statement=null;
    try
    {
      statement=getPreparedStatement("UPDATE accounts SET banned = '"+(acc.isBanned() ? 1 : 0)+"', friends = '"+acc.parseFriendListToDB()+"', enemy = '"+acc.parseEnemyListToDB()+"', muteTime = '"+acc.getMuteTime()+"', mutePseudo = '"+acc.getMutePseudo()+"' WHERE guid = '"+acc.getId()+"'");
      execute(statement);
      return true;
    }
    catch(Exception e)
    {
      super.sendError("AccountData update",e);
      close(statement);
    } finally
    {
      close(statement);
    }
    return false;
  }

  public void updateLastConnection(Account compte)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE accounts SET `lastIP` = ?, `lastConnectionDate` = ? WHERE `guid` = ?");
      p.setString(1,compte.getCurrentIp());
      p.setString(2,compte.getLastConnectionDate());
      p.setInt(3,compte.getId());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("AccountData updateLastConnection",e);
      close(p);
    } finally
    {
      close(p);
    }
  }

  public void setLogged(int id, int logged)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `accounts` SET `logged` = ? WHERE `guid` = ?;");
      p.setInt(1,logged);
      p.setInt(2,id);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("guid: "+id+"  AccountData setLogged",e);
      close(p);
    } finally
    {
      close(p);
    }
  }
  public void abonnement(int id, long time)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `accounts` SET `subscribe` = ? WHERE `guid` = ?;");
      p.setLong(1,time);
      p.setInt(2,id);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("guid: "+id+"  AccountData setLogged",e);
      close(p);
    } finally
    {
      close(p);
    }
  }

  public boolean updateBannedTime(Account acc, long time)
  {
    PreparedStatement statement=null;
    try
    {
      statement=getPreparedStatement("UPDATE accounts SET banned = '"+(acc.isBanned() ? 1 : 0)+"', bannedTime = '"+time+"' WHERE guid = '"+acc.getId()+"'");
      execute(statement);
      return true;
    }
    catch(Exception e)
    {
      super.sendError("AccountData update",e);
      close(statement);
    } finally
    {
      close(statement);
    }
    return false;
  }
  public int load_vip(int guid) {
		int points = 0;
		Result result=null;
		try {
			try {
				result=super.getData("SELECT * from accounts WHERE `guid` LIKE '" + guid + "'");
				 ResultSet RS=result.resultSet;
				if (RS.next()) {
					points = RS.getInt("vip");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				close(result);
			}
		} finally {
			close(result);
		}
		return points;
	}
  public boolean load_timevote(String ip) {
		Result result=null;
		try {
			try {
				result=super.getData("SELECT * from accounts WHERE `lastIP` LIKE '" + ip + "'");
				
				 ResultSet RS=result.resultSet;
				 while(RS.next())
			      {
					 if (RS != null)
					if (System.currentTimeMillis() < (RS.getLong("heurevotesrp")+5400000)) {
						return false;
					}
				}
				 result=super.getData("SELECT * from accounts WHERE `lastVoteIPsrp` LIKE '" + ip + "'");
					
				 ResultSet RSs=result.resultSet;
				 while(RSs.next())
			      {
					 if (RSs != null)
					if (System.currentTimeMillis() < (RS.getLong("heurevotesrp")+5400000)) {
						return false;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				close(result);
			}
		} finally {
			close(result);
		}
		return true;
	}
  public boolean load_timevote2(String ip) {
		Result result=null;
		try {
			try {
				result=super.getData("SELECT * from accounts WHERE `lastIP` LIKE '" + ip + "'");
				
				 ResultSet RS=result.resultSet;
				 while(RS.next())
			      {
					 if (RS != null)
					if (System.currentTimeMillis() < (RS.getLong("heurevote")+10800000)) {
						return false;
					}
				}
				 result=super.getData("SELECT * from accounts WHERE `lastVoteIP` LIKE '" + ip + "'");
					
				 ResultSet RSs=result.resultSet;
				 while(RSs.next())
			      {
					 if (RSs != null)
					if (System.currentTimeMillis() < (RS.getLong("heurevote")+10800000)) {
						return false;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				close(result);
			}
		} finally {
			close(result);
		}
		return true;
	}


  /** Points **/
  public int loadPoints(String user)
  {
    return Config.getInstance().points.load(user);
  }

  public void updatePoints(int id, int points)
  {
    Config.getInstance().points.update(id,points);
  }

  public int loadPointsWithoutUsersDb(String user)
  {
    Result result=null;
    int points=0;
    try
    {
      result=super.getData("SELECT * from accounts WHERE `account` LIKE '"+user+"'");
      ResultSet RS=result.resultSet;
      if(RS.next())
      {
        points=RS.getInt("points");
      }
    }
    catch(SQLException e)
    {
      super.sendError("AccountData loadPoints",e);
      close(result);
    } finally
    {
      close(result);
    }
    return points;
  }
  public void updatevip(int id)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE accounts SET `vip` = ? WHERE `guid` = ?");
      p.setInt(1,1);
      p.setInt(2,id);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("update vip",e);
      close(p);
    } finally
    {
      close(p);
    }
  }
  public void updatePointsWithoutUsersDb(int id, int points)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE accounts SET `points` = ? WHERE `guid` = ?");
      p.setInt(1,points);
      p.setInt(2,id);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("AccountData updatePoints",e);
      close(p);
    } finally
    {
      close(p);
    }
  }

  public int loadPointsWithUsersDb(String account)
  {
    Result result=null;
    int points=0,user=-1;
    try
    {
      result=super.getData("SELECT account, users FROM `accounts` WHERE `account` LIKE '"+account+"'");
      ResultSet RS=result.resultSet;
      if(RS.next())
        user=RS.getInt("users");
      close(result);

      if(user==-1)
      {
        result=super.getData("SELECT id, points FROM `users` WHERE `id` = "+user+";");
        RS=result.resultSet;
        if(RS.next())
          points=RS.getInt("users");
      }
    }
    catch(SQLException e)
    {
      super.sendError("AccountData loadPoints",e);
      close(result);
    } finally
    {
      close(result);
    }
    return points;
  }

  public void updatePointsWithUsersDb(int id, int points)
  {
    PreparedStatement p=null;
    int user=-1;
    try
    {
      Result result=super.getData("SELECT guid, users FROM `accounts` WHERE `guid` LIKE '"+id+"'");
      ResultSet RS=result.resultSet;
      if(RS.next())
        user=RS.getInt("users");
      close(result);

      if(user!=-1)
      {
        p=getPreparedStatement("UPDATE `users` SET `points` = ? WHERE `id` = ?;");
        p.setInt(1,points);
        p.setInt(2,id);
        execute(p);
      }
    }
    catch(SQLException e)
    {
      super.sendError("AccountData updatePoints",e);
    } finally
    {
      close(p);
    }
  }
}