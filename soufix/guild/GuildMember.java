package soufix.guild;

import java.util.Map;
import java.util.TreeMap;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import soufix.client.Player;
import soufix.database.Database;
import soufix.main.Constant;

public class GuildMember
{
  private int guid;
  private String name;
  private int level ;
  private int gfx;
  private Player player;
  private Guild guild;
  private int rank=0;
  private byte pXpGive=0;
  private long xpGave=0;
  private int rights=0;
  private String lastCo;
  private int alig;
  private Map<Integer, Boolean> haveRight=new TreeMap<>();

  public GuildMember(Player player, Guild guild, int rank, long x, byte pXp, int ri, String lastCo)
  {
    this.player=player;
    this.guild=guild;
    this.rank=rank;
    this.xpGave=x;
    this.pXpGive=pXp;
    this.rights=ri;
    this.lastCo=lastCo;
    parseIntToRight(this.rights);
  }
  public GuildMember(int guid,String name, int level , int gfxid,Player player, Guild guild, int rank, long x, byte pXp, int ri, String lastCo,int alig)
  {
	this.guid=guid;  
	this.name=name;
	this.level=level;
	this.gfx=gfxid;
    this.player=player;
    this.guild=guild;
    this.rank=rank;
    this.xpGave=x;
    this.pXpGive=pXp;
    this.rights=ri;
    this.lastCo=lastCo;
    parseIntToRight(this.rights);
    this.alig=alig;
  }

  public int getAlign()
  {
	  if(player == null)
		  return this.alig;
    return player.get_align();
  }
  public int getAlignv2()
  {
    return this.alig;
  }

  public int getGfx()
  {
	  if(player == null)
		  return this.gfx;
    return player.getGfxId();
  }
  public int getGfxv2()
  {
    return this.gfx;
  }

  public int getLvl()
  {
	  if(player == null)
		  return this.level;
    return player.getLevel();
  }
  public int getLvlv2()
  {
    return this.level;
  }

  public String getName()
  {
    return player.getName();
  }
  public String getNamev2()
  {
    return this.name;
  }

  public int getGuid()
  {
	  if(this.player == null)
		  return this.guid;
    return player.getId();
  }
  public int getGuidv2()
  {
    return this.guid;
  }

  public int getRank()
  {
    return rank;
  }

  public void setRank(int i)
  {
    this.rank=i;
  }

  public Guild getGuild()
  {
    return guild;
  }

  public String parseRights()
  {
    return Integer.toString(this.rights,36);
  }

  public int getRights()
  {
    return rights;
  }

  public long getXpGave()
  {
    return xpGave;
  }

  public int getPXpGive()
  {
    return pXpGive;
  }
  public void setPlayer(Player player) {
	this.player = player;
	System.out.println(10);
}


  public String getLastCo()
  {
    return lastCo;
  }

  public void setLastCo(String lastCo)
  {
    this.lastCo=lastCo;
  }

  public int getHoursFromLastCo()
  {
    String[] split=this.lastCo.split("~");
    return Days.daysBetween(new LocalDate(Integer.parseInt(split[0]),Integer.parseInt(split[1]),Integer.parseInt(split[2])),new LocalDate()).getDays()*24;
  }

  public Player getPlayer()
  {
    return player;
  }

  public boolean canDo(int rightValue)
  {
    return this.rights==1 ? true : haveRight.get(rightValue);
  }

  public void setAllRights(int rank, byte xp, int right, Player perso)
  {
    if(rank==-1)
      rank=this.rank;

    if(xp<0)
      xp=this.pXpGive;
    if(xp>90)
      xp=90;

    if(right==-1)
      right=this.rights;

    this.rank=rank;
    this.pXpGive=xp;

    if(right!=this.rights&&right!=1) //V�rifie si les droits sont pareille ou si des droits de meneur; pour ne pas faire la conversion pour rien
      parseIntToRight(right);
    this.rights=right;

   Database.getDynamics().getGuildMemberData().update(perso);
  }

  public void giveXpToGuild(long xp)
  {
    this.xpGave+=xp;
    this.guild.addXp(xp);
  }

  public void initRight()
  {
    haveRight.put(Constant.G_BOOST,false);
    haveRight.put(Constant.G_RIGHT,false);
    haveRight.put(Constant.G_INVITE,false);
    haveRight.put(Constant.G_BAN,false);
    haveRight.put(Constant.G_ALLXP,false);
    haveRight.put(Constant.G_HISXP,false);
    haveRight.put(Constant.G_RANK,false);
    haveRight.put(Constant.G_POSPERCO,false);
    haveRight.put(Constant.G_COLLPERCO,false);
    haveRight.put(Constant.G_USEENCLOS,false);
    haveRight.put(Constant.G_AMENCLOS,false);
    haveRight.put(Constant.G_OTHDINDE,false);
  }

  public void parseIntToRight(int total)
  {
    if(haveRight.isEmpty())
      initRight();
    if(total==1)
      return;
    if(haveRight.size()>0)//Si les droits contiennent quelque chose -> Vidage (M�me si le HashMap supprimerais les entr�es doublon lors de l'ajout)
      haveRight.clear();
    initRight();//Remplissage des droits

    Integer[] mapKey=haveRight.keySet().toArray(new Integer[haveRight.size()]); //R�cup�re les clef de map dans un tableau d'Integer

    while(total>0)
    {
      for(int i=haveRight.size()-1;i<haveRight.size();i--)
      {
        if(mapKey[i].intValue()<=total)
        {
          total^=mapKey[i].intValue();
          haveRight.put(mapKey[i],true);
          break;
        }
      }
    }
  }

}