package soufix.entity.monster;

import soufix.game.World.Drop;
import soufix.main.Main;

import java.util.*;
import java.util.Map.Entry;

public class Monster
{
  private int id;
  private int gfxId;
  private int align;
  private String colors;
  private int ia=0;
  private int minKamas;
  private int maxKamas;
  private Map<Integer, MobGrade> grades=new HashMap<>();
  private ArrayList<Drop> drops=new ArrayList<>();
  private boolean isCapturable;
  private int aggroDistance=0;

  public Monster(int id, int gfxId, int align, String colors, String thisGrades, String thisSpells, String thisStats, String thisStatsInfos, String thisPdvs, String thisPoints, String thisInit, int minKamas, int maxKamas, String thisXp, int ia, boolean capturable, int aggroDistance)
  {
    this.id=id;
    this.gfxId=gfxId;
    this.align=align;
    this.colors=colors;
    this.minKamas=minKamas;
    this.maxKamas=maxKamas;
    this.ia=ia;
    this.isCapturable=capturable;
    this.aggroDistance=aggroDistance;
    int G=1;

    for(int n=0;n<12;n++)
    {
      try
      {
        //Grades
        String[] split=thisGrades.split("\\|");
        String grade=split[n];
        String[] infos=grade.split("@");
        int level=Integer.parseInt(infos[0]);
        String resists=infos[1];
        //Stats
        String stats=thisStats.split("\\|")[n];
        //Spells
        String spells="";
        if(!thisSpells.equalsIgnoreCase("||||")&&!thisSpells.equalsIgnoreCase("")&&!thisSpells.equalsIgnoreCase("-1"))
        {
          spells=thisSpells.split("\\|")[n];
          if(spells.equals("-1"))
            spells="";
        }
        //PDVMax//init
        int pdvmax=1;
        int init=1;

        try
        {
          pdvmax=Integer.parseInt(thisPdvs.split("\\|")[n]);
          init=Integer.parseInt(thisInit.split("\\|")[n]);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          Main.world.logger.error("#1# Error loading monster with id "+id);
        }
        //PA / PM
        int PA=3;
        int PM=3;
        int xp=10;

        try
        {
          String[] pts=thisPoints.split("\\|")[n].split(";");
          try
          {
            PA=Integer.parseInt(pts[0]);
            PM=Integer.parseInt(pts[1]);
            xp=Integer.parseInt(thisXp.split("\\|")[n]);
          }
          catch(Exception e1)
          {
            Main.world.logger.error("#2# Erreur lors du chargement du monstre (template) : "+id);
            e1.printStackTrace();
          }
        }
        catch(Exception e)
        {
          Main.world.logger.error("#3# Erreur lors du chargement du monstre (template) : "+id);
          e.printStackTrace();
        }
        grades.put(G,new MobGrade(this,G,level,PA,PM,resists,stats,thisStatsInfos,spells,pdvmax,init,xp,n));
        G++;
      }
      catch(Exception e)
      {
        // ok, pour les dopeuls ...
        //TODO: Enlever toutes les erreurs
      }
    }
  }

  public void setInfos(int gfxId, int align, String colors, String thisGrades, String thisSpells, String thisStats, String thisStatsInfos, String thisPdvs, String thisPoints, String thisInit, int minKamas, int maxKamas, String thisXp, int ia, boolean capturable, int aggroDistance)
  {
    this.gfxId=gfxId;
    this.align=align;
    this.colors=colors;
    this.minKamas=minKamas;
    this.maxKamas=maxKamas;
    this.ia=ia;
    this.isCapturable=capturable;
    this.aggroDistance=aggroDistance;
    int G=1;
    grades.clear();
    for(int n=0;n<12;n++)
    {
      try
      {
        //Grades
        String grade=thisGrades.split("\\|")[n];
        String[] infos=grade.split("@");
        int level=Integer.parseInt(infos[0]);
        String resists=infos[1];
        //Stats
        String stats=thisStats.split("\\|")[n];
        //Spells
        String spells=thisSpells.split("\\|")[n];
        if(spells.equals("-1"))
          spells="";
        //PDVMax//init
        int pdvmax=1;
        int init=1;

        try
        {
          pdvmax=Integer.parseInt(thisPdvs.split("\\|")[n]);
          init=Integer.parseInt(thisInit.split("\\|")[n]);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          Main.world.logger.error("#4# Erreur lors du chargement du monstre (template) : "+id);
        }
        //PA / PM
        int PA=3;
        int PM=3;
        int xp=10;

        try
        {
          String[] pts=thisPoints.split("\\|")[n].split(";");
          try
          {
            PA=Integer.parseInt(pts[0]);
            PM=Integer.parseInt(pts[1]);
            xp=Integer.parseInt(thisXp.split("\\|")[n]);
          }
          catch(Exception e1)
          {
            Main.world.logger.error("#5# Erreur lors du chargement du monstre (template) : "+id);
            e1.printStackTrace();
          }
        }
        catch(Exception e)
        {
          Main.world.logger.error("#6# Erreur lors du chargement du monstre (template) : "+id);
          e.printStackTrace();
        }
        grades.put(G,new MobGrade(this,G,level,PA,PM,resists,stats,thisStatsInfos,spells,pdvmax,init,xp,n));
        G++;
      }
      catch(Exception e)
      {
        // ok pour les dopeuls
        e.printStackTrace();
      }
    }
  }

  public int getId()
  {
    return this.id;
  }

  public int getGfxId()
  {
    return this.gfxId;
  }

  public int getAlign()
  {
    return this.align;
  }

  public String getColors()
  {
    return this.colors;
  }

  public int getIa()
  {
    return this.ia;
  }

  public int getMinKamas()
  {
    return this.minKamas;
  }

  public int getMaxKamas()
  {
    return this.maxKamas;
  }

  public Map<Integer, MobGrade> getGrades()
  {
    return this.grades;
  }

  public void addDrop(Drop D)
  {
    this.drops.add(D);
  }

  public ArrayList<Drop> getDrops()
  {
    return this.drops;
  }

  public boolean isCapturable()
  {
    return this.isCapturable;
  }

  public int getAggroDistance()
  {
    return this.aggroDistance;
  }

  public MobGrade getGradeByLevel(int lvl)
  {
    for(Entry<Integer, MobGrade> grade : getGrades().entrySet())
      if(grade.getValue().getLevel()==lvl)
        return grade.getValue();
    return null;
  }

  public MobGrade getRandomGrade()
  {
    int randomgrade=(int)(Math.random()*(6-1))+1;
    int graderandom=1;
    for(Entry<Integer, MobGrade> grade : getGrades().entrySet())
    {
      if(graderandom==randomgrade)
        return grade.getValue();
      else
        graderandom++;
    }
    return null;
  }
}
