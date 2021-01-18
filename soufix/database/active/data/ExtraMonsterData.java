package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.main.Main;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExtraMonsterData extends AbstractDAO<Object>
{
  public ExtraMonsterData(HikariDataSource dataSource)
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

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * from extra_monster");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        Main.world.addExtraMonster(RS.getInt("idMob"),RS.getString("superArea"),RS.getString("subArea"),RS.getInt("chances"));
      }
    }
    catch(SQLException e)
    {
      super.sendError("Extra_monsterData load",e);
    } finally
    {
      close(result);
    }
  }
}
