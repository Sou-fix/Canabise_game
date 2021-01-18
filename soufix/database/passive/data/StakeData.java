package soufix.database.passive.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;

public class StakeData extends AbstractDAO<Object>
{

  public StakeData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Object obj)
  {
    return false;
  }
  
  public boolean add(int winClass, int winlevel, int loseClass, int loseLevel)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("INSERT INTO `stakes`(`winnerclass`, `winnerlevel`, `loserclass`, `loserlevel`) VALUES (?, ?, ?, ?)");
      p.setInt(1,winClass);
      p.setInt(2,winlevel);
      p.setInt(3,loseClass);
      p.setInt(4,loseLevel);
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("StakeData add",e);
    } finally
    {
      close(p);
    }
    return false;
  }
}
