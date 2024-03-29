package soufix.database.passive.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.client.Player;
import soufix.database.Database;
import soufix.database.passive.AbstractDAO;
import soufix.entity.mount.Mount;
import soufix.main.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MountData extends AbstractDAO<Mount>
{
	private int nextId = -1;
  public MountData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
    Result result=null;
    try
    {
      result=getData("SELECT * from `world.entity.mounts` WHERE `id` = "+String.valueOf((int)obj)+";");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        Main.world.addMount(new Mount(RS.getInt("id"),RS.getInt("color"),RS.getInt("sex"),RS.getInt("amour"),RS.getInt("endurance"),RS.getInt("level"),RS.getLong("xp"),RS.getString("name"),RS.getInt("fatigue"),RS.getInt("energy"),RS.getInt("reproductions"),RS.getInt("maturity"),RS.getInt("serenity"),RS.getString("objects"),RS.getString("ancestors"),RS.getString("capacitys"),RS.getInt("size"),RS.getInt("cell"),RS.getShort("map"),RS.getInt("owner"),RS.getInt("orientation"),RS.getLong("fecundatedDate"),RS.getInt("couple"),RS.getInt("savage")));
      }
    }
    catch(SQLException e)
    {
      e.printStackTrace();
      super.sendError("MountData load",e);
    } finally
    {
      close(result);
    }
  }

  @Override
  public boolean update(Mount mount)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `world.entity.mounts` SET `name` = ?, `xp` = ?, `level` = ?, `endurance` = ?, `amour` = ?, `maturity` = ?, `serenity` = ?, `reproductions` = ?,"+"`fatigue` = ?, `energy` = ?, `ancestors` = ?, `objects` = ?, `owner` = ?, `capacitys` = ?, `size` = ?, `cell` = ?, `map` = ?,"+" `orientation` = ?, `fecundatedDate` = ?, `couple` = ? WHERE `id` = ?;");
      p.setString(1,mount.getName());
      p.setLong(2,mount.getExp());
      p.setInt(3,mount.getLevel());
      p.setInt(4,mount.getEndurance());
      p.setInt(5,mount.getAmour());
      p.setInt(6,mount.getMaturity());
      p.setInt(7,mount.getState());
      p.setInt(8,mount.getReproduction());
      p.setInt(9,mount.getFatigue());
      p.setInt(10,mount.getEnergy());
      p.setString(11,mount.getAncestors());
      p.setString(12,mount.parseObjectsToString());
      p.setInt(13,mount.getOwner());
      p.setString(14,mount.parseCapacitysToString());
      p.setInt(15,mount.getSize());
      p.setInt(16,mount.getCellId());
      p.setInt(17,mount.getMapId());
      p.setInt(18,mount.getOrientation());
      p.setLong(19,mount.getFecundatedDate());
      p.setInt(20,mount.getCouple());
      p.setInt(21,mount.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("MountData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public void delete(int id)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("DELETE FROM `world.entity.mounts` WHERE `id` = ?;");
      p.setInt(1,id);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("MountData delete",e);
    } finally
    {
      close(p);
    }
  }

  public void delete(Player player)
  {
    this.delete(player.getMount().getId());
    Main.world.delDragoByID(player.getMount().getId());
    player.setMountGiveXp(0);
    player.setMount(null);
    Database.getStatics().getPlayerData().update(player);
  }

  public void add(Mount mount)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("INSERT INTO `world.entity.mounts`(`id`, `color`, `sex`, `name`, `xp`, `level`, `endurance`, `amour`, `maturity`, `serenity`, `reproductions`, `fatigue`, `energy`,"+"`objects`, `ancestors`, `capacitys`, `size`, `map`, `cell`, `owner`, `orientation`, `fecundatedDate`, `couple`, `savage`) "+"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      p.setInt(1,mount.getId());
      p.setInt(2,mount.getColor());
      p.setInt(3,mount.getSex());
      p.setString(4,mount.getName());
      p.setLong(5,mount.getExp());
      p.setInt(6,mount.getLevel());
      p.setInt(7,mount.getEndurance());
      p.setInt(8,mount.getAmour());
      p.setInt(9,mount.getMaturity());
      p.setInt(10,mount.getState());
      p.setInt(11,mount.getReproduction());
      p.setInt(12,mount.getFatigue());
      p.setInt(13,mount.getEnergy());
      p.setString(14,mount.parseObjectsToString());
      p.setString(15,mount.getAncestors());
      p.setString(16,mount.parseCapacitysToString());
      p.setInt(17,mount.getSize());
      p.setInt(18,mount.getMapId());
      p.setInt(19,mount.getCellId());
      p.setInt(20,mount.getOwner());
      p.setInt(21,mount.getOrientation());
      p.setLong(22,mount.getFecundatedDate());
      p.setInt(23,mount.getCouple());
      p.setInt(24,mount.getSavage());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("MountData add",e);
    } finally
    {
      close(p);
    }
  }

  public int getNextId()
  {
	  if (this.nextId != -1) {
		  this.nextId++;
		  return this.nextId;
	  }
    	Result result=null;
    	int nextObjectId = 0;
    	try {
    		 result = getData("SELECT MIN(id) AS min FROM `world.entity.mounts`;");
            ResultSet RS = result.resultSet;
            boolean found = RS.first();
            if (found) nextObjectId = RS.getInt("max");
            else nextObjectId = 1;
        } catch (SQLException e) {
            logger.error("WorldEntityData mount", e);
        } finally {
            close(result);
        }
    	 this.nextId = nextObjectId+1;
		return nextObjectId+1;

  }
}
