package soufix.entity.npc;

import soufix.client.Player;
import soufix.game.World;
import soufix.main.Main;
import soufix.object.ObjectTemplate;
import soufix.quest.Quest;
import soufix.utility.Pair;

import java.util.*;

public class NpcTemplate
{
  private int id, bonus, gfxId, scaleX, scaleY, sex, color1, color2, color3;
  private String accessories;
  private int extraClip, customArtWork;
  private String path;
  private Quest quest;
  private byte informations;

  private Map<Integer, Integer> initQuestions=new HashMap<>();
  private ArrayList<ObjectTemplate> sales=new ArrayList<>();
  private List<Pair<ArrayList<Pair<Integer, Integer>>, ArrayList<Pair<Integer, Integer>>>> exchanges;

  public NpcTemplate(int id, int bonus, int gfxId, int scaleX, int scaleY, int sex, int color1, int color2, int color3, String accessories, int extraClip, int customArtWork, String questions, String sales, String exchanges, String path, byte informations)
  {
    this.id=id;
    this.setBonus(bonus);
    this.gfxId=gfxId;
    this.scaleX=scaleX;
    this.scaleY=scaleY;
    this.sex=sex;
    this.color1=color1;
    this.color2=color2;
    this.color3=color3;
    this.accessories=accessories;
    this.extraClip=extraClip;
    this.customArtWork=customArtWork;
    this.path=path;
    this.informations=informations;

    if(questions.split("\\|").length>1)
    {
      for(String question : questions.split("\\|"))
      {
        try
        {
          initQuestions.put(Integer.parseInt(question.split(",")[0]),Integer.parseInt(question.split(",")[1]));
        }
        catch(Exception e)
        {
          e.printStackTrace();
          Main.world.logger.error("#1# Erreur sur une question id sur le PNJ d'id : "+id);
        }
      }
    } else
    {
      if(questions.equalsIgnoreCase(""))
        this.initQuestions.put(-1,-1);
      else
        this.initQuestions.put(-1,Integer.parseInt(questions));
    }

    if(!sales.equals(""))
    {
      for(String obj : sales.split(","))
      {
        try
        {
          ObjectTemplate template=Main.world.getObjTemplate(Integer.parseInt(obj));
          if(template!=null)
            this.sales.add(template);
        }
        catch(NumberFormatException e)
        {
          e.printStackTrace();
          Main.world.logger.error("#2# Erreur sur un item en vente sur le PNJ d'id : "+id);
        }
      }
    }

    if(!exchanges.equals(""))
    {
      try
      {
        this.exchanges=new ArrayList<>();
        for(String data : exchanges.split("~"))
        {
          ArrayList<Pair<Integer, Integer>> gives=new ArrayList<>(),
              gets=new ArrayList<>();

          String[] split=data.split("\\|");
          String give=split[1],get=split[0];

          for(String obj : give.split(","))
          {
            split=obj.split(":");
            gives.add(new Pair<>(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
          }

          for(String obj : get.split(","))
          {
            split=obj.split(":");
            gets.add(new Pair<>(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
          }
          this.exchanges.add(new Pair<>(gets,gives));
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
        Main.world.logger.error("#3# Erreur sur l'exchanges sur le PNJ d'id : "+id);
      }
    }
  }

  public void setInfos(int id, int bonusValue, int gfxID, int scaleX, int scaleY, int sex, int color1, int color2, int color3, String access, int extraClip, int customArtWork, String questions, String ventes, String exchanges, String path, byte informations)
  {
    this.id=id;
    this.setBonus(bonusValue);
    this.gfxId=gfxID;
    this.scaleX=scaleX;
    this.scaleY=scaleY;
    this.sex=sex;
    this.color1=color1;
    this.color2=color2;
    this.color3=color3;
    this.accessories=access;
    this.extraClip=extraClip;
    this.customArtWork=customArtWork;
    this.path=path;
    this.sales.clear();
    this.informations=informations;
    this.initQuestions.clear();

    if(questions.split("\\|").length>1)
    {
      for(String question : questions.split("\\|"))
      {
        try
        {
          this.initQuestions.put(Integer.parseInt(question.split(",")[0]),Integer.parseInt(question.split(",")[1]));
        }
        catch(Exception e)
        {
          e.printStackTrace();
          Main.world.logger.error("#2# Erreur sur une question id sur le PNJ d'id : "+this.id);
        }
      }
    } else
    {
      this.initQuestions.put(-1,Integer.parseInt(questions));
    }

    if(!ventes.equals(""))
    {
      for(String obj : ventes.split(","))
      {
        try
        {
          ObjectTemplate template=Main.world.getObjTemplate(Integer.parseInt(obj));
          if(template!=null)
            this.sales.add(template);
        }
        catch(NumberFormatException e)
        {
          e.printStackTrace();
          Main.world.logger.error("#2# Erreur sur un item en vente sur le PNJ d'id : "+id);
        }
      }
    }

    if(!exchanges.equals(""))
    {
      try
      {
        this.exchanges=new ArrayList<>();
        for(String data : exchanges.split("~"))
        {
          String[] split=data.split("\\|");
          String give=split[1],get=split[0];
          ArrayList<Pair<Integer, Integer>> gives=new ArrayList<>(),
              gets=new ArrayList<>();

          for(String obj : give.split(","))
          {
            split=obj.split(":");
            gives.add(new Pair<>(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
          }

          for(String obj : get.split(","))
          {
            split=obj.split(":");
            gets.add(new Pair<>(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
          }
          this.exchanges.add(new Pair<>(gets,gives));
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
        Main.world.logger.error("#3# Erreur sur l'exchanges sur le PNJ d'id : "+id);
      }
    }
  }

  public int getId()
  {
    return id;
  }

  public int getGfxId()
  {
    return gfxId;
  }

  public int getScaleX()
  {
    return scaleX;
  }

  public int getScaleY()
  {
    return scaleY;
  }

  public int getSex()
  {
    return sex;
  }

  public int getColor1()
  {
    return color1;
  }

  public int getColor2()
  {
    return color2;
  }

  public int getColor3()
  {
    return color3;
  }

  public String getAccessories()
  {
    return accessories;
  }

  public int getExtraClip()
  {
    return extraClip;
  }

  public void setExtraClip(int extraClip)
  {
    this.extraClip=extraClip;
  }

  public int getCustomArtWork()
  {
    return customArtWork;
  }

  public String getPath()
  {
    return path;
  }

  public Quest getQuest()
  {
    return quest;
  }

  public void setQuest(Quest quest)
  {
    this.quest=quest;
  }

  public byte getInformations()
  {
    return informations;
  }

  public int getInitQuestionId(int id)
  {
    if(this.initQuestions.get(id)==null)
      for(Integer entry : this.initQuestions.values())
        return entry;
    return this.initQuestions.get(id);
  }

  public String getItemVendorList(Player perso)
  {
    StringBuilder items=new StringBuilder();
    if(this.sales.isEmpty())
      return "";
    for(ObjectTemplate obj : this.sales) {
    	items.append(obj.parseItemTemplateStats()).append("|");
    }
    return items.toString();
  }

  public ArrayList<ObjectTemplate> getAllItem()
  {
    return sales;
  }

  public boolean addItemVendor(ObjectTemplate template)
  {
    if(this.sales.contains(template))
      return false;
    this.sales.add(template);
    return true;
  }

  public boolean removeItemVendor(int id)
  {
    Iterator<ObjectTemplate> iterator=this.sales.iterator();

    while(iterator.hasNext())
    {
      ObjectTemplate template=iterator.next();
      if(template.getId()==id)
        iterator.remove();
    }

    return true;
  }

  public boolean haveItem(int id)
  {
    for(ObjectTemplate template : sales)
      if(template.getId()==id)
        return true;
    return false;
  }

  public ArrayList<Pair<Integer, Integer>> verifItemGet(ArrayList<Pair<Integer, Integer>> objects)
  {
    if(this.exchanges==null)
      return null;
    boolean ok1=true,ok2=false;
    for(Pair<ArrayList<Pair<Integer, Integer>>, ArrayList<Pair<Integer, Integer>>> entry0 : this.exchanges)
    {
      ok1=true;
      for(Pair<Integer, Integer> entry1 : entry0.getLeft())
      {
        if(ok2)
          break;
        ok2=false;

        for(Pair<Integer, Integer> entry2 : objects)
        {
          if(String.valueOf(entry1.getLeft()).equals(String.valueOf(World.getGameObject(entry2.getLeft()).getTemplate().getId()))&&String.valueOf(entry1.getRight()).equals(String.valueOf(entry2.getRight())))
            ok2=true;
          if(!ok2)
            ok1=false;
          else
            break;
        }
      }

      if(ok1&&objects.size()==entry0.getLeft().size())
        return entry0.getRight();
    }
    return null;
  }

  public int getBonus()
  {
    return bonus;
  }

  public void setBonus(int bonus)
  {
    this.bonus=bonus;
  }
}
