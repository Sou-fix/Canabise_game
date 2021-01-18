package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.main.Boutique;
import soufix.main.Config;
import soufix.main.Main;
import soufix.main.Tokenshop;
import soufix.object.ObjectTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectTemplateData extends AbstractDAO<ObjectTemplate>
{
  public ObjectTemplateData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(ObjectTemplate obj)
  {
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM item_template;");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        ObjectTemplate template=new ObjectTemplate(RS.getInt("id"),RS.getString("statsTemplate"),RS.getString("name"),RS.getInt("type"),RS.getInt("level"),RS.getInt("pod"),RS.getInt("prix"),RS.getInt("panoplie"),RS.getString("conditions"),RS.getString("armesInfos"),RS.getInt("sold"),RS.getInt("avgPrice"),RS.getInt("points"),RS.getInt("newPrice"),RS.getInt("boutique"),RS.getInt("tokenShop"),RS.getInt("tokens"));
        if(Main.world.getObjTemplate(RS.getInt("id"))!=null)
        {
          Main.world.getObjTemplate(RS.getInt("id")).setInfos(RS.getString("statsTemplate"),RS.getString("name"),RS.getInt("type"),RS.getInt("level"),RS.getInt("pod"),RS.getInt("prix"),RS.getInt("panoplie"),RS.getString("conditions"),RS.getString("armesInfos"),RS.getInt("sold"),RS.getInt("avgPrice"),RS.getInt("points"),RS.getInt("newPrice"));
        }
        else
        {
          Main.world.addObjTemplate(template);
        }
       /* if(RS.getInt("points")!=0)
        {
          Boutique.items.add(template);
        }*/
        if(RS.getInt("tokenshop")!=0)
        {
          Tokenshop.items.add(template);
        }
      }
		for(String s : Config.getInstance().boutique.split(","))
	      {
			if (s.length() == 0)continue;
	        int iditem=(int)Integer.parseInt(s);
	        Boutique.items.add(Main.world.getObjTemplate(iditem));
	      }
    }
    catch(SQLException e)
    {
      super.sendError("Item_templateData load",e);
      Main.stop("unknown");
    } finally
    {
      close(result);
    }
  }

  public void saveAvgprice(ObjectTemplate template)
  {
    if(template==null)
      return;
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `item_template` SET sold = ?,avgPrice = ? WHERE id = ?");
      p.setLong(1,template.getSold());
      p.setInt(2,template.getAvgPrice());
      p.setInt(3,template.getId());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("Item_templateData saveAvgprice",e);
    } finally
    {
      close(p);
    }
  }
}
