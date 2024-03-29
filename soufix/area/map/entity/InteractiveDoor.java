package soufix.area.map.entity;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;

import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.entity.monster.MobGroup;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.utility.Pair;
import soufix.utility.TimerWaiterPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class InteractiveDoor
{

  private static final ArrayList<InteractiveDoor> interactiveDoors=new ArrayList<>();

  private final ArrayList<Short> maps=new ArrayList<>();
  private final Map<Short, ArrayList<Short>> doorsEnable=new HashMap<>();
  private final Map<Short, ArrayList<Short>> doorsDisable=new HashMap<>();
  private final Map<Short, ArrayList<Short>> cellsEnable=new HashMap<>();
  private final Map<Short, ArrayList<Short>> cellsDisable=new HashMap<>();
  private final Map<Short, ArrayList<Pair<Short, String>>> requiredCells=new HashMap<>();

  private Pair<Short, Short> button;
  private short time=30;
  private boolean state=false;

  public InteractiveDoor(String maps, String doorsEnable, String doorsDisable, String cellsEnable, String cellsDisable, String requiredCells, String button, short time)
  {
    for(String map : maps.split(","))
      this.maps.add(Short.parseShort(map));

    if(!doorsEnable.isEmpty())
      this.stock(this.doorsEnable,doorsEnable);
    if(!doorsDisable.isEmpty())
      this.stock(this.doorsDisable,doorsDisable);
    if(!cellsEnable.isEmpty())
      this.stock(this.cellsEnable,cellsEnable);
    if(!cellsDisable.isEmpty())
      this.stock(this.cellsDisable,cellsDisable);

    if(!requiredCells.isEmpty())
    {
      for(String data : requiredCells.split("@"))
      {
        String[] split=data.split(":");
        short map=Short.parseShort(split[0]);
        String cells=split[1];

        for(String cell : cells.split(";"))
        {
          split=cell.split(",");
          if(!this.requiredCells.containsKey(map))
            this.requiredCells.put(map,new ArrayList<>());
          this.requiredCells.get(map).add(new Pair<>(Short.parseShort(split[0]),split.length>1 ? split[1] : null));
        }
      }
    }

    if(!button.equals("-1"))
    {
      String[] split=button.split(",");
      this.button=new Pair<>(Short.parseShort(split[0]),Short.parseShort(split[1]));
    }

    this.time=time;
    InteractiveDoor.interactiveDoors.add(this);
  }

  private void stock(Map<Short, ArrayList<Short>> arrayListMap, String value)
  {
    for(String data : value.split("@"))
    {
      String[] split=data.split(":");
      short map=Short.parseShort(split[0]);
      String cells=split[1];

      for(String cell : cells.split(","))
      {
        if(!arrayListMap.containsKey(map))
          arrayListMap.put(map,new ArrayList<>());
        arrayListMap.get(map).add(Short.parseShort(cell));
      }
    }
  }

  public static boolean tryActivate(Player player, GameCase gameCase)
  {
    for(InteractiveDoor interactiveDoor : InteractiveDoor.interactiveDoors)
    {
      if(interactiveDoor.button!=null&&player.getCurMap().getId()==interactiveDoor.button.getLeft()&&gameCase.getId()==interactiveDoor.button.getRight())
      {
        interactiveDoor.check(player);
        return true;
      }
    }
    return false;
  }

  public static void show(Player player)
  {
    InteractiveDoor.interactiveDoors.stream().filter(interactiveDoor -> interactiveDoor.state).forEach(interactiveDoor -> {
      interactiveDoor.setState(interactiveDoor.cellsEnable,true,false,player);
      interactiveDoor.setState(interactiveDoor.cellsDisable,false,false,player);
      interactiveDoor.setState(interactiveDoor.doorsEnable,true,true,player);
      interactiveDoor.setState(interactiveDoor.doorsDisable,false,true,player);
    });
  }

  public static void check(Player player, GameMap gameMap)
  {
    try
    {
      for(InteractiveDoor interactiveDoor : InteractiveDoor.interactiveDoors)
        if(interactiveDoor.maps.contains(gameMap.getId()))
          if(interactiveDoor.button==null&&interactiveDoor.check(player))
            break;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public synchronized boolean check(Player player)
  {
    if(this.state)
      return false;
    boolean ok=true;

    for(Entry<Short, ArrayList<Pair<Short, String>>> requiredCells : this.requiredCells.entrySet())
    {
      final GameMap gameMap=Main.world.getMap(requiredCells.getKey());
      if(gameMap==null)
        continue;
      boolean loc=false;
      for(Pair<Short, String> couple : requiredCells.getValue())
      {
        GameCase gameCase=gameMap.getCase(couple.getLeft());
        if(gameCase==null)
          continue;

        switch(player.getCurMap().getId())
        {
          case 1884:
            if(gameCase.getPlayers().size()>0)
            {
              loc=true;
              ok=true;
            }
            break;
        }
        if(loc)
          break;

        if(couple.getRight()!=null)
        {
          if(!Condition.isValid(player,gameCase,couple.getRight()))
          {
            ok=false;
            break;
          }
        }
        else if(gameCase.getPlayers().size()==0)
        {
          ok=false;
          break;
        }
      }

      if(!ok)
        break;
    }

    if(ok)
    {
      this.open();
      new TimerWaiterPlus(this::close,this.time*1000);
    }
    return ok;
  }

  private void open()
  {
    if(this.state)
      return;

    this.setState(this.cellsEnable,true,false,null);
    this.setState(this.cellsDisable,false,false,null);
    this.setState(this.doorsEnable,true,true,null);
    this.setState(this.doorsDisable,false,true,null);
    this.state=true;
  }

  private void close()
  {
    if(!this.state)
      return;

    this.setState(this.cellsEnable,false,false,null);
    this.setState(this.cellsDisable,true,false,null);
    this.setState(this.doorsEnable,false,true,null);
    this.setState(this.doorsDisable,true,true,null);
    this.state=false;
  }

  //v2.7 - Replaced String += with StringBuilder
  private void setState(Map<Short, ArrayList<Short>> arrayListMap, boolean active, boolean doors, Player player)
  {
    StringBuilder packet=new StringBuilder("GDF");

    for(Entry<Short, ArrayList<Short>> entry : arrayListMap.entrySet())
    {
      GameMap gameMap=Main.world.getMap(entry.getKey());
      if(gameMap==null)
        continue;
      if(player!=null&&player.getCurMap()!=null&&player.getCurMap().getId()!=gameMap.getId())
        continue;

      for(short cell : entry.getValue())
      {
        if(doors)
          packet.append(this.setStateDoor(cell,active,player!=null));
        else
          this.setStateCell(gameMap,cell,active,player);
      }

      if(player!=null)
        player.send(packet.toString());
      else
        for(Player target : gameMap.getPlayers())
          target.send(packet.toString());
    }
  }

  private String setStateDoor(int cell, boolean active, boolean fast)
  {
    return "|"+cell+(!fast ? (active ? ";2" : ";4") : (active ? ";3" : ";1"));
  }
  
  private void setStateCell(GameMap gameMap, short cell, boolean active, Player player)
  {
    String packet="GDC"+cell;
    GameCase gameCase=gameMap.getCase(cell),temporaryCell;
    gameMap.removeCase(cell);

    if(active)
    {
      temporaryCell=new GameCase(gameMap,cell,true,(byte)0,(byte)0,(byte)0,true,true,-1);
      temporaryCell.setOnCellStop(gameCase.getOnCellStop());
      gameMap.getCases().add(temporaryCell);
      packet+=";aaGaaaaaaa801;1";
    }
    else
    {
      temporaryCell=new GameCase(gameMap,cell,true,(byte)0,(byte)0,(byte)0,false,false,-1);
      temporaryCell.setOnCellStop(gameCase.getOnCellStop());
      gameMap.getCases().add(temporaryCell);
      packet+=";aaaaaaaaaa801;1";
    }

    if(player!=null)
      player.send(packet);
    else
      for(Player target : gameMap.getPlayers())
        target.send(packet);
  }

  private static class Condition
  {

    public static boolean isValid(Player player, GameCase gameCase, String request)
    {
      Jep jep=new Jep();
      request=request.replace("&","&&").replace("=","==").replace("|","||").replace("!","!=").replace("~","==");
      try
      {
        // Item template cell
        GameObject object=gameCase.getDroppedItem(false);
        jep.addVariable("ITC",object!=null ? object.getTemplate().getId() : -1);

        //Mob Group Cell
        if(request.contains("MGC"))
          request=Condition.parseMGC(player,request);
        //Parse request..
        jep.parse(request);
        Object result=jep.evaluate();
        return result!=null ? Boolean.valueOf(result.toString()) : false;
      }
      catch(JepException e)
      {
        e.printStackTrace();
      }
      return false;
    }

    private static String parseMGC(Player player, String request)
    {
      String[] data=request.split("==")[1].split("-");
      for(MobGroup mobGroup : player.getCurMap().getMobGroups().values())
        for(String id : data)
          if(mobGroup.getCellId()==Short.parseShort(id))
            return "1==1";
      return "1==0";
    }
  }
}
