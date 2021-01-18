package soufix.entity.exchange;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.game.World;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.utility.Pair;

import java.util.ArrayList;

public class Stake
{
  protected final Player player1, player2;
  protected long kamas1=0, kamas2=0;
  protected ArrayList<Pair<Integer, Integer>> items1=new ArrayList<>(), items2=new ArrayList<>();
  protected boolean ok1, ok2;

  public Stake(Player player1, Player player2)
  {
    this.player1=player1;
    this.player2=player2;
  }

  private boolean isPodsOK(byte i)
  {
    int newpods=0;
    int oldpods=0;
    if(i==1)
    {
      int podsmax=this.player1.getMaxPod();
      int pods=this.player1.getPodUsed();
      for(Pair<Integer, Integer> couple : items2)
      {
        if(couple.getRight()==0)
          continue;
        GameObject obj=World.getGameObject(couple.getLeft());
        newpods+=obj.getTemplate().getPod()*couple.getRight();
      }
      if(newpods==0)
      {
        return true;
      }
      for(Pair<Integer, Integer> couple : items1)
      {
        if(couple.getRight()==0)
          continue;
        GameObject obj=World.getGameObject(couple.getLeft());
        oldpods+=obj.getTemplate().getPod()*couple.getRight();
      }
      if((newpods+pods-oldpods)>podsmax)
      {
        // Erreur 70
        // 1 + 70 => 170
        SocketManager.GAME_SEND_Im_PACKET(this.player1,"170");
        return false;
      }
    }
    else
    {
      int podsmax=this.player2.getMaxPod();
      int pods=this.player2.getPodUsed();
      for(Pair<Integer, Integer> couple : items1)
      {
        if(couple.getRight()==0)
          continue;
        GameObject obj=World.getGameObject(couple.getLeft());
        newpods+=obj.getTemplate().getPod()*couple.getRight();
      }
      if(newpods==0)
      {
        return true;
      }
      for(Pair<Integer, Integer> couple : items2)
      {
        if(couple.getRight()==0)
          continue;
        GameObject obj=World.getGameObject(couple.getLeft());
        oldpods+=obj.getTemplate().getPod()*couple.getRight();
      }
      if((newpods+pods-oldpods)>podsmax)
      {
        SocketManager.GAME_SEND_Im_PACKET(this.player2,"170");
        return false;
      }
    }
    return true;
  }

  public synchronized long getKamas(int guid)
  {
    int i=0;
    if(this.player1.getId()==guid)
      i=1;
    else if(this.player2.getId()==guid)
      i=2;

    if(i==1)
      return kamas1;
    else if(i==2)
      return kamas2;
    return 0;
  }

  public synchronized boolean toogleOk(int guid)
  {
    byte i=(byte)(this.player1.getId()==guid ? 1 : 2);
    if(this.isPodsOK(i))
    {
      if(i==1)
      {
        ok1=!ok1;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,guid);
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,guid);
      }
      else if(i==2)
      {
        ok2=!ok2;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,guid);
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,guid);
      }
      return (ok1&&ok2);
    }
    return false;
  }

  public synchronized void setKamas(int guid, long k)
  {
    ok1=false;
    ok2=false;

    int i=0;
    if(this.player1.getId()==guid)
      i=1;
    else if(this.player2.getId()==guid)
      i=2;
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,this.player2.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,this.player2.getId());
    if(k<0)
      return;
    if(i==1)
    {
      kamas1=k;
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'G',"",k+"");
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'G',"",k+"");
    }
    else if(i==2)
    {
      kamas2=k;
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'G',"",k+"");
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'G',"",k+"");
    }
  }

  public synchronized void cancel()
  {
    if(this.player1.getAccount()!=null)
      if(this.player1.getGameClient()!=null)
        SocketManager.GAME_SEND_EV_PACKET(this.player1.getGameClient());
    if(this.player2.getAccount()!=null)
      if(this.player2.getGameClient()!=null)
        SocketManager.GAME_SEND_EV_PACKET(this.player2.getGameClient());
    this.player1.setExchangeAction(null);
    this.player2.setExchangeAction(null);
  }

  //v2.7 - Replaced String += with StringBuilder
  public synchronized void apply()
  {
    StringBuilder str=new StringBuilder();
    try
    {
      str.append(this.player1.getName()+" : ");
      for(Pair<Integer, Integer> couple1 : items1)
        str.append(", ["+World.getGameObject(couple1.getLeft()).getTemplate().getId()+"@"+couple1.getLeft()+";"+couple1.getRight()+"]");
      str.append(" avec "+kamas1+" K.\n");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    try
    {
      str.append("Avec "+this.player2.getName());
      for(Pair<Integer, Integer> couple2 : items2)
        str.append(", ["+World.getGameObject(couple2.getLeft()).getTemplate().getId()+"@"+couple2.getLeft()+";"+couple2.getRight()+"]");
      str.append(" avec "+kamas2+" K.");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    for(Pair<Integer, Integer> couple : items1) // Les items du player vers le player2
    {
      if(couple.getRight()==0)
        continue;
      if(World.getGameObject(couple.getLeft())==null)
        continue;
      if(World.getGameObject(couple.getLeft()).getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        continue;

      if(!this.player1.hasItemGuid(couple.getLeft())) //If player doesnt have guid
      {
        SocketManager.GAME_SEND_MESSAGE(player1,"Erreur: un joueur a tenté d'échanger un objet qu'il ne possède pas.");
        SocketManager.GAME_SEND_MESSAGE(player2,"Erreur: un joueur a tenté d'échanger un objet qu'il ne possède pas.");
        SocketManager.GAME_SEND_EV_PACKET(player1.getGameClient());
        SocketManager.GAME_SEND_EV_PACKET(player2.getGameClient());
        this.player1.setExchangeAction(null);
        this.player2.setExchangeAction(null);
        return;
      }
    }

    if(player1.getCurMap()!=player2.getCurMap())
    {
      SocketManager.GAME_SEND_MESSAGE(player1,"Vous devez être sur la même carte pour faire une mise.");
      SocketManager.GAME_SEND_MESSAGE(player2,"Vous devez être sur la même carte pour faire une mise.");
      SocketManager.GAME_SEND_EV_PACKET(player1.getGameClient());
      SocketManager.GAME_SEND_EV_PACKET(player2.getGameClient());
      this.player1.setExchangeAction(null);
      this.player2.setExchangeAction(null);
      return;
    }

    if(player1.getCurMap().getPlaces().equalsIgnoreCase("|")||player2.getCurMap().getPlaces().equalsIgnoreCase("|")||player1.getCurMap().getPlaces().equalsIgnoreCase("")||player2.getCurMap().getPlaces().equalsIgnoreCase(""))
    {
      SocketManager.GAME_SEND_MESSAGE(player1,"Vous ne pouvez pas jouer sur cette carte car elle n'a pas de positions de combat.");
      SocketManager.GAME_SEND_MESSAGE(player2,"Vous ne pouvez pas jouer sur cette carte car elle n'a pas de positions de combat.");
      SocketManager.GAME_SEND_EV_PACKET(player1.getGameClient());
      SocketManager.GAME_SEND_EV_PACKET(player2.getGameClient());
      this.player1.setExchangeAction(null);
      this.player2.setExchangeAction(null);
      return;
    }

    this.player1.setExchangeAction(null);
    this.player2.setExchangeAction(null);
    SocketManager.GAME_SEND_Ow_PACKET(this.player1);
    SocketManager.GAME_SEND_Ow_PACKET(this.player2);
    SocketManager.GAME_SEND_STATS_PACKET(this.player1);
    SocketManager.GAME_SEND_STATS_PACKET(this.player2);
    SocketManager.GAME_SEND_STAKE_VALID(this.player1.getGameClient(),'a');
    SocketManager.GAME_SEND_STAKE_VALID(this.player2.getGameClient(),'a');
    player1.setStake(this);
    player2.setStake(this);
    this.player1.getCurMap().newFight(player1,player2,Constant.FIGHT_TYPE_STAKE);
  }

  public synchronized long winKamas(Player winner)
  {
    if(winner==this.player1)
    {
      Database.getStatics().getStakeData().add(this.player1.getClasse(),this.player1.getLevel(),this.player2.getClasse(),this.player2.getLevel());
      this.player1.addKamas(kamas2);
      this.player2.addKamas(-kamas2);
      return kamas2;
    }
    else
    {
      Database.getStatics().getStakeData().add(this.player2.getClasse(),this.player2.getLevel(),this.player1.getClasse(),this.player1.getLevel());
      this.player2.addKamas(kamas1);
      this.player1.addKamas(-kamas1);
      return kamas1;
    }
  }

  public synchronized ArrayList<Pair<Integer, Integer>> winItems(Player winner)
  {
    if(this.player1==winner)
    {
      for(Pair<Integer, Integer> couple : items2)
      {
        if(couple.getRight()==0)
          continue;
        if(World.getGameObject(couple.getLeft())==null)
          continue;
        if(World.getGameObject(couple.getLeft()).getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
          continue;
        if(!this.player2.hasItemGuid(couple.getLeft()))
        {
          couple.right=0;
          continue;
        }
        this.giveObject(couple,World.getGameObject(couple.getLeft()),this.player1,this.player2);
        return items2;
      }
    }

    else if(this.player2==winner)
    {
      for(Pair<Integer, Integer> couple : items1)
      {
        if(couple.getRight()==0)
          continue;
        if(World.getGameObject(couple.getLeft())==null)
          continue;
        if(World.getGameObject(couple.getLeft()).getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
          continue;
        if(!this.player1.hasItemGuid(couple.getLeft()))
        {
          couple.right=0;
          continue;
        }
        this.giveObject(couple,World.getGameObject(couple.getLeft()),this.player2,this.player1);
        return items1;
      }
    }
    return null;
  }

  protected void giveObject(Pair<Integer, Integer> couple, GameObject object, Player winner, Player loser)
  {
    if(object==null)
      return;
    if((object.getQuantity()-couple.getRight())<1)
    {
      loser.removeItem(couple.getLeft());
      couple.right=object.getQuantity();
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(loser,couple.getLeft());
      if(!winner.addObjet(object,true))
        Main.world.removeGameObject(couple.getLeft());
    }
    else
    {
      object.setQuantity(object.getQuantity()-couple.getRight());
      SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(loser,object);
      GameObject newObj=GameObject.getCloneObjet(object,couple.getRight());
      if(winner.addObjet(newObj,true))
        World.addGameObject(newObj,true);
    }
  }

  public synchronized void addItem(int guid, int qua, int pguid)
  {
    ok1=false;
    ok2=false;

    GameObject obj=World.getGameObject(guid);
    int i=0;

    if(this.player1.getId()==pguid)
      i=1;
    if(this.player2.getId()==pguid)
      i=2;

    if(qua==1)
      qua=1;
    String str=guid+"|"+qua;
    if(obj==null)
      return;
    if(obj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
      return;

    String add="|"+obj.getTemplate().getId()+"|"+obj.parseStatsString();
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,this.player2.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,this.player2.getId());
    if(i==1)
    {
      Pair<Integer, Integer> couple=getPairInList(items1,guid);
      if(couple!=null)
      {
        couple.right+=qua;
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'O',"+",""+guid+"|"+couple.getRight());
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'O',"+",""+guid+"|"+couple.getRight()+add);
        return;
      }
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'O',"+",str);
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'O',"+",str+add);
      items1.add(new Pair<>(guid,qua));
    }
    else if(i==2)
    {
      Pair<Integer, Integer> couple=getPairInList(items2,guid);
      if(couple!=null)
      {
        couple.right+=qua;
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'O',"+",""+guid+"|"+couple.getRight());
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'O',"+",""+guid+"|"+couple.getRight()+add);
        return;
      }
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'O',"+",str);
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'O',"+",str+add);
      items2.add(new Pair<>(guid,qua));
    }
  }

  public synchronized void removeItem(int guid, int qua, int pguid)
  {
    int i=0;
    if(this.player1.getId()==pguid)
      i=1;
    else if(this.player2.getId()==pguid)
      i=2;
    ok1=false;
    ok2=false;

    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,this.player2.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,this.player2.getId());

    GameObject object=World.getGameObject(guid);
    if(object==null)
      return;
    String add="|"+object.getTemplate().getId()+"|"+object.parseStatsString();

    if(i==1)
    {
      Pair<Integer, Integer> couple=getPairInList(items1,guid);

      if(couple==null)
        return;
      int newQua=couple.getRight()-qua;

      if(newQua<1)
      {
        items1.remove(couple);
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'O',"-",""+guid);
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'O',"-",""+guid);
      }
      else
      {
        couple.right=newQua;
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'O',"+",""+guid+"|"+newQua);
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'O',"+",""+guid+"|"+newQua+add);
      }
    }
    else if(i==2)
    {
      Pair<Integer, Integer> couple=getPairInList(items2,guid);

      if(couple==null)
        return;
      int newQua=couple.getRight()-qua;

      if(newQua<1)
      {
        items2.remove(couple);
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'O',"-",""+guid);
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'O',"-",""+guid);
      }
      else
      {
        couple.right=newQua;
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'O',"+",""+guid+"|"+newQua+add);
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'O',"+",""+guid+"|"+newQua);
      }
    }
  }

  public synchronized int getQuaItem(int itemID, int playerGuid)
  {
    ArrayList<Pair<Integer, Integer>> items;
    if(this.player1.getId()==playerGuid)
      items=items1;
    else
      items=items2;
    for(Pair<Integer, Integer> curCoupl : items)
      if(curCoupl.getLeft()==itemID)
        return curCoupl.getRight();
    return 0;
  }

  public static Pair<Integer, Integer> getPairInList(ArrayList<Pair<Integer, Integer>> items, int id)
  {
    for(Pair<Integer, Integer> couple : items)
      if(couple.getLeft()==id)
        return couple;
    return null;
  }
}