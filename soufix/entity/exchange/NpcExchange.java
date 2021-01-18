package soufix.entity.exchange;

import java.util.ArrayList;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.npc.NpcTemplate;
import soufix.game.World;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.utility.Pair;

public class NpcExchange
{
  private Player player;
  private NpcTemplate npc;
  private long kamas1=0;
  private long kamas2=0;
  private ArrayList<Pair<Integer, Integer>> items1=new ArrayList<Pair<Integer, Integer>>();
  private ArrayList<Pair<Integer, Integer>> items2=new ArrayList<Pair<Integer, Integer>>();
  private boolean ok1;
  private boolean ok2;

  public NpcExchange(Player p, NpcTemplate n)
  {
    this.player=p;
    this.setNpc(n);
  }
  
  public synchronized long getKamas(boolean b)
  {
    if(b)
      return this.kamas2;
    return this.kamas1;
  }

  public synchronized void toogleOK(boolean paramBoolean)
  {
    if(paramBoolean)
    {
      this.ok2=(!this.ok2);
      SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    }
    else
    {
      this.ok1=(!this.ok1);
      SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok1,this.player.getId());
    }
    if((this.ok2)&&(this.ok1))
      apply();
  }

  public synchronized void setKamas(boolean ok, long kamas)
  {
    if(kamas<0L)
      return;
    this.ok2=(this.ok1=false);
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok1,this.player.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    if(ok)
    {
      this.kamas2=kamas;
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(),'G',"",String.valueOf(kamas));
      putAllGiveItem();
      return;
    }
    if(kamas>this.player.getKamas())
      return;
    this.kamas1=kamas;
    SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player,'G',"",String.valueOf(kamas));
    putAllGiveItem();
  }

  public synchronized void cancel()
  {
    if((this.player.getAccount()!=null)&&(this.player.getGameClient()!=null))
      SocketManager.GAME_SEND_EV_PACKET(this.player.getGameClient());
    this.player.setExchangeAction(null);
  }

  public synchronized void apply()
  {
    for(Pair<Integer, Integer> Pair : items1)
    {
      if(Pair.getRight()==0)
        continue;
      if(World.getGameObject(Pair.getLeft()).getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        continue;
      if(!this.player.hasItemGuid(Pair.getLeft()))
      {
        Pair.right=0;//On met la quantité a 0 pour éviter les problemes
        continue;
      }
      GameObject obj=World.getGameObject(Pair.getLeft());
      if((obj.getQuantity()-Pair.getRight())<1)
      {
        this.player.removeItem(Pair.getLeft());
        Main.world.removeGameObject(World.getGameObject(Pair.getLeft()).getGuid());
        Pair.right=obj.getQuantity();
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,Pair.getLeft());
      }
      else
      {
        obj.setQuantity(obj.getQuantity()-Pair.getRight());
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,obj);
      }
    }

    for(Pair<Integer, Integer> Pair1 : items2)
    {
      if(Pair1.getRight()==0)
        continue;
      if(Main.world.getObjTemplate(Pair1.getLeft())==null)
        continue;
      GameObject obj1=Main.world.getObjTemplate(Pair1.getLeft()).createNewItem(Pair1.getRight(),false);
      if(this.player.addObjet(obj1,true))
        World.addGameObject(obj1,true);
      SocketManager.GAME_SEND_Im_PACKET(this.player,"021;"+Pair1.getRight()+"~"+Pair1.getLeft());
    }
    this.player.setExchangeAction(null);
    SocketManager.GAME_SEND_EXCHANGE_VALID(this.player.getGameClient(),'a');
    Database.getStatics().getPlayerData().update(this.player);
  }

  public synchronized void addItem(int obj, int qua)
  {
    if(qua<=0)
      return;
    if(World.getGameObject(obj)==null)
      return;
    this.ok1=(this.ok2=false);
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok1,this.player.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    String str=obj+"|"+qua;
    Pair<Integer, Integer> Pair=getPairInList(items1,obj);
    if(Pair!=null)
    {
      Pair.right+=qua;
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player,'O',"+",""+obj+"|"+Pair.getRight());
      putAllGiveItem();
      return;
    }
    SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player,'O',"+",str);
    items1.add(new Pair<Integer, Integer>(obj,qua));
    putAllGiveItem();
  }

  public synchronized void removeItem(int guid, int qua)
  {
    if(qua<0)
      return;
    this.ok1=(this.ok2=false);
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok1,this.player.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    if(World.getGameObject(guid)==null)
      return;
    Pair<Integer, Integer> Pair=getPairInList(items1,guid);
    int newQua=Pair.getRight()-qua;
    if(newQua<1)
    {
      items1.remove(Pair);
      putAllGiveItem();
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player,'O',"-",""+guid);
    }
    else
    {
      Pair.right=newQua;
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player,'O',"+",""+guid+"|"+newQua);
      putAllGiveItem();
    }
  }

  public synchronized int getQuaItem(int obj, boolean b)
  {
    ArrayList<Pair<Integer, Integer>> list;
    if(b)
      list=this.items2;
    else
      list=this.items1;
    for(Pair<Integer, Integer> item : list)
      if(item.getLeft()==obj)
        return item.getRight();
    return 0;
  }

  public synchronized void clearItems()
  {
    if(this.items2.isEmpty())
      return;
    for(Pair<Integer, Integer> i : items2)
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(),'O',"-",i.getLeft()+"");
    this.kamas2=0;
    this.items2.clear();
    if(this.ok2)
    {
      this.ok2=false;
      SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    }
  }

  private synchronized Pair<Integer, Integer> getPairInList(ArrayList<Pair<Integer, Integer>> items, int guid)
  {
    for(Pair<Integer, Integer> Pair : items)
      if(Pair.getLeft()==guid)
        return Pair;
    return null;
  }

  public synchronized void putAllGiveItem()
  {
    ArrayList<Pair<Integer, Integer>> objects=this.npc.verifItemGet(this.items1);

    if(objects!=null)
    {
      this.clearItems();
      for(Pair<Integer, Integer> object : objects)
      {
        if(object.getRight()==-1)
        {
          int kamas=object.getLeft();

          if(kamas==-1)
          {
            for(Pair<Integer, Integer> pepite : this.items1)
              if(World.getGameObject(pepite.getLeft()).getTemplate().getId()==1)
                this.kamas2+=Integer.parseInt(World.getGameObject(pepite.getLeft()).getTxtStat().get(990).substring(9,13))*pepite.getRight();

            SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(),'G',"",String.valueOf(this.kamas2));
            continue;
          }

          this.kamas2+=kamas;
          SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(),'G',"",String.valueOf(this.kamas2));
          continue;
        }
        String str=object.getLeft()+"|"+object.getRight()+"|"+object.getLeft()+"|"+Main.world.getObjTemplate(object.getLeft()).getStrTemplate();
        this.items2.add(new Pair<Integer, Integer>(object.getLeft(),object.getRight()));
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(),'O',"+",str);
      }
      if(!this.ok2)
      {
        this.ok2=true;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
      }
    }
    else
    {
      this.clearItems();
    }
  }

  public NpcTemplate getNpc()
  {
    return npc;
  }

  public void setNpc(NpcTemplate npc)
  {
    this.npc=npc;
  }
}
