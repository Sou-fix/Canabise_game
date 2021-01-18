package soufix.client;

import soufix.Hdv.HdvEntry;
import soufix.client.other.Restriction;
import soufix.command.administration.Group;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.game.GameClient;
import soufix.game.World;
import soufix.main.Config;
import soufix.main.Main;
import soufix.object.GameObject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Account
{
  public final Restriction restriction;
  private int id;
  private String name;
  private String pseudo;
  private String answer;
  private String currentIp="";
  private String lastIP="";
  private String lastConnectionDate="";
  private int points;
  private long muteTime=0;
  private String mutePseudo="";
  private boolean banned=false;
  private long subscriber=0;
  private long bankKamas=0;
  private Player currentPlayer;
  private GameClient gameClient;
  private byte state;
  private String lastVoteIP;
  private long heureVote;
  private int vip;
private List<GameObject> bank=new ArrayList<>();
  private List<Integer> friends=new ArrayList<>();
  private List<Integer> enemys=new ArrayList<>();
  private Map<Integer, ArrayList<HdvEntry>> hdvsItems;
  private boolean Load_ok = false;
  private String items_to_load;
  private boolean Load_items = false;
  

public Account(int guid, String name, String pseudo, String answer, boolean banned, String lastIp, String lastConnectionDate, String friends, String enemy, int points, long subscriber, long muteTime, String mutePseudo, String lastVoteIP, String heureVote, int vip)
  {
	this.restriction=Restriction.get(this.id);
	this.id=guid;
    this.name=name;
    this.pseudo=pseudo;
    this.answer=answer;
    this.banned=banned;
    this.lastIP=lastIp;
    this.lastConnectionDate=lastConnectionDate;
    this.hdvsItems=Main.world.getMyItems(guid);
    this.points=points;
    this.subscriber=subscriber;
    this.muteTime=muteTime;
    this.mutePseudo=mutePseudo;
    this.lastVoteIP=lastVoteIP;
    this.vip = vip;

    if(heureVote.equalsIgnoreCase(""))
      this.heureVote=0;
    else
      this.heureVote=Long.parseLong(heureVote);

    //Chargement de la liste d'amie
    if(friends != null)
    if(!friends.equalsIgnoreCase(""))
    {
      for(String f : friends.split(";"))
      {
        try
        {
          this.friends.add(Integer.parseInt(f));
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    //Chargement de la liste d'Enemy
    if(enemy != null)
    if(!enemy.equalsIgnoreCase(""))
    {
      for(String e : enemy.split(";"))
      {
        try
        {
          this.enemys.add(Integer.parseInt(e));
        }
        catch(Exception e1)
        {
          e1.printStackTrace();
        }
      }
    }
    //Chargement de la banque
    String bank=Database.getDynamics().getBankData().get(guid);
    if(bank==null)
    {
      Database.getDynamics().getBankData().add(guid);
    } else
    {
      this.bankKamas=Integer.parseInt(bank.split("@")[0]);
      String allItem="";
      try
      {
        allItem=bank.split("@")[1];
      }
      catch(Exception e2)
      {
      }
      if(!allItem.equals(""))
      {
    	  this.items_to_load=allItem;
      }
    }
   // if(!Database.getDynamics().getGiftData().existByAccount(guid))
    //  Database.getDynamics().getGiftData().create(guid);
  }

  public long getHeureVote()
  {
    return this.heureVote;
  }
  public void load_items() {
	  if(this.Load_items || this.items_to_load == null || this.items_to_load == "")
		  return;
      for(String item : this.items_to_load.split("\\|"))
      {
        if(!item.equals(""))
        {
          GameObject obj=World.getGameObject(Integer.parseInt(item));
          if(obj!=null)
            this.bank.add(obj);
        }
      }	
      this.items_to_load = null;
      this.Load_items = true;
  }

  public String getLastVoteIP()
  {
    return this.lastVoteIP;
  }

  public int getId()
  {
    return id;
  }

  public void setId(int i)
  {
    id=i;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String i)
  {
    name=i;
  }

  public String getPseudo()
  {
    return pseudo;
  }

  public String getAnswer()
  {
    return answer;
  }

  public String getCurrentIp()
  {
    return currentIp;
  }

  public void setCurrentIp(String i)
  {
    currentIp=i;
  }

  public String getLastIP()
  {
    return lastIP;
  }

  public void setLastIP(String i)
  {
    lastIP=i;
  }

  public String getLastConnectionDate()
  {
    return lastConnectionDate;
  }

  public void setLastConnectionDate(String i)
  {
    lastConnectionDate=i;
  }

  public int getPoints()
  {
    points=Database.getStatics().getAccountData().loadPoints(name);
    return points;
  }
  public int getvip()
  {
	return vip;  
  }
  public boolean isLoad_ok() {
	return Load_ok;
}

public void setLoad_ok(boolean load_ok) {
	Load_ok = load_ok;
}
  public void setVip(int vip) {
	  Database.getStatics().getAccountData().updatevip(id);  
	this.vip = vip;
}

  public boolean gettimevotesrp()
  {
    return Database.getStatics().getAccountData().load_timevote(currentIp);
  }
  public boolean gettimevoterpg()
  {
    return Database.getStatics().getAccountData().load_timevote2(currentIp);
  }

  public void setPoints(int i)
  {
    points=i;
    Database.getStatics().getAccountData().updatePoints(id,points);
  }

  public void mute(int time, String pseudo)
  {
    if(time<=0)
      return;
    muteTime=time;
    mutePseudo=pseudo;
    Database.getStatics().getAccountData().update(this);
    if(this.currentPlayer!=null)
      this.currentPlayer.send("Im117;"+pseudo+"~"+time);
  }

  public void unMute()
  {
    if(muteTime==0)
      return;
    muteTime=0;
    mutePseudo="";
    Database.getStatics().getAccountData().update(this);
  }

  public boolean isMuted()
  {
    if(muteTime==0)
      return false;
    if(muteTime>=System.currentTimeMillis()/60000)
      return true;
    muteTime=0;
    mutePseudo="";
    Database.getStatics().getAccountData().update(this);
    return false;
  }

  public long getMuteTime()
  {
    if(!isMuted())
      return 0;
    return muteTime;
  }

  public String getMutePseudo()
  {
    if(!isMuted())
      return "";
    return mutePseudo;
  }

  public List<GameObject> getBank()
  {
    return bank;
  }

  public String parseBankObjectsToDB()
  {
    StringBuilder str=new StringBuilder();
    if(this.bank.isEmpty()) {
    	if(this.Load_items)
    	return "";
    	if(this.items_to_load == null || this.items_to_load == "") {
    		return "";
    	}else
    	return this.items_to_load;	
    }
    for(GameObject gameObject : this.bank)
      str.append(gameObject.getGuid()).append("|");
    return str.toString();
  }

  public long getBankKamas()
  {
    return this.bankKamas;
  }

  public void setBankKamas(long i)
  {
    this.bankKamas=i;
    Database.getDynamics().getBankData().update(this);
  }

  public GameClient getGameClient()
  {
    return this.gameClient;
  }

  public void setGameClient(GameClient t)
  {
    this.gameClient=t;
  }

  public Map<Integer, Player> getPlayers()
  {
    Map<Integer, Player> players=new HashMap<>();
    new CopyOnWriteArrayList<>(Main.world.getPlayers()).stream().filter(player -> player!=null).filter(player -> player.getAccount()!=null).filter(player -> player.getAccount().getId()==this.getId()).forEach(player -> {
      if(player.getGameClient()==null)
        player.setAccount(this);
      players.put(player.getId(),player);
    });
    return players;
  }

  public Player getCurrentPlayer()
  {
    return this.currentPlayer;
  }

  public void setCurrentPlayer(Player player)
  {
    this.currentPlayer=player;
  }

  public boolean isBanned()
  {
    return this.banned;
  }

  public void setBanned(boolean banned)
  {
    this.banned=banned;
  }

  public boolean isOnline()
  {
    return this.gameClient!=null;
  }

  public void setState(int state)
  {
    this.state=(byte)state;
    Database.getStatics().getAccountData().update(this);
  }

  public byte getState()
  {
    return state;
  }

  public long getSubscribeRemaining()
  {
    long remaining=this.subscriber-System.currentTimeMillis();
    return remaining<=0L ? 0L : remaining;
  }

  public boolean isSubscribe()
  {
    if(!Config.getInstance().useSubscribe)
      return true;
    long remaining=this.subscriber-System.currentTimeMillis();
    return remaining>0L;
  }

  public boolean isSubscribeWithoutCondition()
  {
    long remaining=this.subscriber-System.currentTimeMillis();
    return remaining>0L;
  }

  public boolean createPlayer(String name, int sexe, int classe, int color1, int color2, int color3)
  {
    Player perso=Player.CREATE_PERSONNAGE(name,sexe,classe,color1,color2,color3,this);
    return perso!=null;
  }

  public void deletePlayer(int guid)
  {
    if(this.getPlayers().containsKey(guid))
      Main.world.removePlayer(this.getPlayers().get(guid));
  }

  public void sendOnline()
  {
    for(int id : this.friends)
    {
      Player player=Main.world.getPlayer(id);
      if(player!=null&&player.is_showFriendConnection()&&player.isOnline()&&player.getAccount().isFriendWith(this.id))
        SocketManager.GAME_SEND_FRIEND_ONLINE(this.currentPlayer,player);
    }
  }

  public void addFriend(int id)
  {
    if(this.id==id)
    {
      SocketManager.GAME_SEND_FA_PACKET(this.currentPlayer,"Ey");
      return;
    }

    Account account=Main.world.getAccount(id);

    if(account==null)
    {
      SocketManager.GAME_SEND_MESSAGE(this.currentPlayer,"Ce compte n'existe pas.");
      return;
    }

    Player player=account.getCurrentPlayer(); // Il est arrivï¿½ que le personnage soit null alors que ... non !

    if(player==null)
    {
      SocketManager.GAME_SEND_MESSAGE(this.currentPlayer,"Ce joueur n'existe pas.");
      return;
    }

    Group group=player.getGroupe();

    if(group!=null&&!group.isPlayer())
    {
      SocketManager.GAME_SEND_MESSAGE(this.currentPlayer,"Vous ne pouvez pas ajouter un membre du personnel comme ami.");
      return;
    }
    if(!this.friends.contains(id))
    {
      this.friends.add(id);
      SocketManager.GAME_SEND_FA_PACKET(this.currentPlayer,"K"+account.getPseudo()+player.parseToFriendList(id));
      Database.getStatics().getAccountData().update(this);
    } else
    {
      SocketManager.GAME_SEND_FA_PACKET(this.currentPlayer,"Ea");
    }
  }

  public void removeFriend(int id)
  {
    if(this.friends.contains(id))
    {
      Iterator<Integer> iterator=this.friends.iterator();
      while(iterator.hasNext())
        if(iterator.next()==id)
          iterator.remove();
      Database.getStatics().getAccountData().update(this);
    }
    SocketManager.GAME_SEND_FD_PACKET(this.currentPlayer,"K");
  }

  public boolean isFriendWith(int id)
  {
    return friends.contains(id);
  }

  //v2.7 - Replaced String += with StringBuilder
  public String parseFriendListToDB()
  {
    StringBuilder str=new StringBuilder();
    for(int i : this.friends)
    {
      if(!str.toString().equalsIgnoreCase(""))
        str.append(";");
      str.append(i+"");
    }
    return str.toString();
  }

  public String parseFriendList()
  {
    StringBuilder str=new StringBuilder();
    if(this.friends.isEmpty())
      return "";
    for(int i : this.friends)
    {
      Account C=Main.world.getAccountv2(i);
      if(C==null)
        continue;
      str.append("|").append(C.getPseudo());
      //on s'arrete la si aucun perso n'est connectï¿½
      if(!C.isOnline())
        continue;
      Player P=C.getCurrentPlayer();
      if(P==null)
        continue;
      str.append(P.parseToFriendList(id));
    }
    return str.toString();
  }

  public void addEnemy(String packet, int guid)
  {
    if(this.id==guid)
    {
      SocketManager.GAME_SEND_FA_PACKET(this.currentPlayer,"Ey");
      return;
    }
    if(!this.enemys.contains(guid))
    {
      this.enemys.add(guid);
      Player Pr=Main.world.getPlayerByName(packet);
      SocketManager.GAME_SEND_ADD_ENEMY(this.currentPlayer,Pr);
      Database.getStatics().getAccountData().update(this);
    } else
      SocketManager.GAME_SEND_iAEA_PACKET(this.currentPlayer);
  }

  public void removeEnemy(int id)
  {
    if(this.enemys.contains(id))
    {
      Iterator<Integer> iterator=this.enemys.iterator();
      while(iterator.hasNext())
        if(iterator.next()==id)
          iterator.remove();
      Database.getStatics().getAccountData().update(this);
    }
    SocketManager.GAME_SEND_iD_COMMANDE(this.currentPlayer,"K");
  }

  public boolean isEnemyWith(int id)
  {
    return enemys.contains(id);
  }

  //v2.7 - Replaced String += with StringBuilder
  public String parseEnemyListToDB()
  {
    StringBuilder str=new StringBuilder();
    for(int i : this.enemys)
    {
      if(!str.toString().equalsIgnoreCase(""))
        str.append(";");
      str.append(i+"");
    }
    return str.toString();
  }

  public String parseEnemyList()
  {
    StringBuilder str=new StringBuilder();
    if(this.enemys.isEmpty())
      return "";
    for(int i : this.enemys)
    {
      Account C=Main.world.getAccount(i);
      if(C==null)
        continue;
      str.append("|").append(C.getPseudo());
      //on s'arrete la si aucun perso n'est connectï¿½
      if(!C.isOnline())
        continue;
      Player P=C.getCurrentPlayer();
      if(P==null)
        continue;
      str.append(P.parseToEnemyList(id));
    }
    return str.toString();
  }

  //v2.8 - World Market
  public boolean recoverItem(int lineId)
  {
    if(this.currentPlayer==null||this.currentPlayer.getExchangeAction()==null)
      return false;
    if((Integer)this.currentPlayer.getExchangeAction().getValue()>=0)
      return false;

    int hdvID;
    //if(this.currentPlayer.getWorldMarket())
      hdvID=Config.getInstance().worldMarket;
   // else
    //  hdvID=Math.abs((Integer)this.currentPlayer.getExchangeAction().getValue());//Rï¿½cupï¿½re l'ID de l'HDV

    HdvEntry entry=null;
    try
    {
      ArrayList<HdvEntry> entries=this.hdvsItems.get(hdvID);
      if(entries==null||entries.isEmpty())
        return false;
      for(HdvEntry tempEntry : entries)
      {//Boucle dans la liste d'entry de l'HDV pour trouver un entry avec le meme cheapestID que spï¿½cifiï¿½
        if(tempEntry.getLineId()==lineId)
        {//Si la boucle trouve un objet avec le meme cheapestID, arrete la boucle
          entry=tempEntry;
          break;
        }
      }
    }
    catch(NullPointerException e)
    {
      e.printStackTrace();
      return false;
    }

    if(entry==null)//Si entry == null cela veut dire que la boucle s'est effectuï¿½ sans trouver d'item avec le meme cheapestID
      return false;
    if(entry.buy)
      return false;

    this.hdvsItems.get(hdvID).remove(entry);//Retire l'item de la liste des objets a vendre du compte
    GameObject obj=entry.getGameObject();

    if(this.currentPlayer.addObjetSimiler(obj,true,-1))
    {
      Main.world.removeGameObject(obj.getGuid());
    } else
    {
      this.currentPlayer.addObjet(obj);
    }
    Database.getDynamics().getHdvObjectData().delete(entry.getGameObject().getGuid());
    Main.world.getHdv(hdvID).delEntry(entry);//Retire l'item de l'HDV

    Database.getStatics().getPlayerData().update(this.currentPlayer);
    return true;
  }

  public HdvEntry[] getHdvEntries(int id)
  {
    if(this.hdvsItems.get(id)==null)
      return new HdvEntry[1];
    HdvEntry[] entries=new HdvEntry[this.hdvsItems.get(id).size()];

    for(int i=0;i<this.hdvsItems.get(id).size();i++)
      entries[i]=this.hdvsItems.get(id).get(i);
    return entries;
  }

  public int countHdvEntries(int id)
  {
    ArrayList<HdvEntry> hdvEntry=this.hdvsItems.get(id);
    return hdvEntry==null ? 0 : hdvEntry.size();
  }

  public void resetAllChars()
  {
    for(Player player : this.getPlayers().values())
    {
      if(player.getFight()!=null)
      {
        if(player.getParty()!=null)
          player.getParty().leave(player);
        player.setOnline(true);
      }

      if(player.getExchangeAction()!=null)
        GameClient.leaveExchange(player);
      if(player.getParty()!=null)
        player.getParty().leave(player);
      if(player.getCurCell()!=null)
        player.getCurCell().removePlayer(player);
      if(player.getCurMap()!=null&&player.isOnline())
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(),player.getId());

      player.setOnline(false);
    }
  }

  //v2.5 - follow system
  //v2.8 - GameClient session killing
  public void disconnect(Player player)
  {
    if(player.isLeader)
    {
      for(int i=0;i<player.followers.size();i++)
      {
        player.followers.get(i).isFollowing=false;
        player.followers.get(i).leader=null;
        SocketManager.GAME_SEND_MESSAGE(player.followers.get(i),"Le joueur que vous suiviez s'est déconnecté du jeu. Vous ne suivez plus personne.");
      }
      player.isLeader=false;
      player.followers.clear();
    }
    if(player.isFollowing)
    {
      SocketManager.GAME_SEND_MESSAGE(player.leader,"Une personne qui vous suivait s'est déconnectée.");
      player.leader.followers.remove(player);
      player.leader=null;
      player.isFollowing=false;
    }
    Database.getStatics().getAccountData().setLogged(this.getId(),0);
    Database.getStatics().getPlayerData().updateAllLogged(this.getId(),0);
    Database.getStatics().getPlayerData().update(player);
    player.resetStringStuff();
    if(player.getExchangeAction()!=null)
      GameClient.leaveExchange(player);
    if(player.getParty()!=null)
      player.getParty().leave(player);
    if(player.getMount()!=null)
      Database.getStatics().getMountData().update(player.getMount());
    if(player.getFight()!=null)
    {
      if(player.getFight().playerDisconnect(player,false))
      {
        Database.getStatics().getPlayerData().update(player);
        return;
      }
    }
    this.currentPlayer=null;
    this.gameClient=null;
    this.currentIp="";

   // for(Player character : this.getPlayers().values())
    //  Database.getStatics().getPlayerData().update(character);

    player.resetVars();
    this.resetAllChars();
    Database.getStatics().getAccountData().update(this);
    Main.world.logger.info("The player "+player.getName()+" come to disconnect.");
    Main.refreshTitle();
  }

  public void updateVote(String hour, String ip)
  {
    if(hour.equalsIgnoreCase(""))
      this.heureVote=0;
    else
      this.heureVote=Long.parseLong(hour);
    this.lastVoteIP=ip;
  }
  public void setSubscriber(long subscriber) {
	this.subscriber = subscriber;
}

}