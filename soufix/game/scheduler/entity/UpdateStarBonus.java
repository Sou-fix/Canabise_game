package soufix.game.scheduler.entity;

import java.util.ArrayList;
import java.util.List;

import soufix.area.map.GameMap;
import soufix.database.Database;
import soufix.entity.monster.MobGroup;
import soufix.fight.Fight;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Main;
import soufix.utility.Pair;

public class UpdateStarBonus extends Updatable
{
  public final static Updatable updatable=new UpdateStarBonus(Config.getInstance().starUpdate);
  public final static ArrayList<MobGroup> groups=new ArrayList<MobGroup>();
  public UpdateStarBonus(int wait)
  {
    super(wait);
  }

  //v2.8 - batch update stars
  public void update()
  {
    if(this.verify())
    {
      List<Pair<Pair<MobGroup, Integer>, Integer>> normalMobs=new ArrayList<Pair<Pair<MobGroup, Integer>, Integer>>();
      List<Pair<MobGroup, Integer>> fixedMobs=new ArrayList<Pair<MobGroup, Integer>>();
      for(GameMap map : Main.world.getMaps())
      {
        for(MobGroup group : map.getMobGroups().values())
        {
          boolean inFight=false;
          for(Fight fight : map.getFights())
            if(fight.getMobGroup()==group)
              inFight=true;
          if(!inFight)
          {
            if(!group.isFix())
              normalMobs.add(new Pair<Pair<MobGroup, Integer>, Integer>(new Pair<MobGroup, Integer>(group,(int)group.getId()),(int)map.getId()));
            else
              fixedMobs.add(new Pair<MobGroup, Integer>(group,(int)map.getId()));
          }
        }
      }
      try
      {
        Database.getDynamics().getHeroicMobsGroups().batchUpdateFixStars(fixedMobs);
        Database.getDynamics().getHeroicMobsGroups().batchUpdateStars(normalMobs);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  @Override
  public Object get()
  {
    return null;
  }
}