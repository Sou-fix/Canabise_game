package soufix.area.map.maze;

import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.common.Formulas;
import soufix.common.SocketManager;
import soufix.main.Main;
import soufix.utility.TimerWaiterPlus;

public class PigDragon
{
  private static GameCase inside;
  private static GameCase outside;

  public void initialize()
  {
    PigDragon.setOutside(null);
    PigDragon.setInside(null);
    initializeMap(9371,413,274,262,36);
    initializeMap(9372,442,320,216,22);
    initializeMap(9373,414,262,144,48);
    initializeMap(9374,417,262,231,51);
    //initializeMap(9375,413,274,262,36); // Huiti�me
    initializeMap(9376,413,274,262,36);
    //initializeMap(9377,413,274,262,36); // Dix-huiti�me
    initializeMap(9378,413,274,262,36);
    initializeMap(9379,413,274,262,36);
    initializeMap(9380,442,320,216,22);
    //initializeMap(9381,442,320,216,22); // Douzi�me
    initializeMap(9382,442,320,216,22);
    initializeMap(9383,442,320,216,22);
    initializeMap(9384,442,320,216,22);
    initializeMap(9385,414,262,144,48);
    initializeMap(9386,414,262,144,48);
    //initializeMap(9387,414,262,144,48); // Quatorzi�me
    initializeMap(9388,414,262,144,48);
    initializeMap(9389,414,262,144,48);
    initializeMap(9390,417,262,231,51);
    initializeMap(9391,417,262,231,51);
    initializeMap(9392,417,262,231,51);
    initializeMap(9393,417,262,231,51);
    initializeMap(9394,417,262,231,51);
    initializeMap(9395,417,262,231,51);
    initializeExt();
    new TimerWaiterPlus(PigDragon::checkOutside,5*60*1000);
  }

  private static void checkOutside()
  {
    Main.world.pigDragon.initialize();
  }

  public static void close(GameMap map, GameCase cell)
  {
    if(map==null||cell==null)
      return;

    switch(cell.getId())
    {
      case 320: // Gauche
        close(map,(short)306);
        break;
      case 262: // Gauche
        switch(map.getId())
        {
          case 9371: // haut
          case 9374: // haut
          case 9390: // haut
          case 9375: // haut
          case 9391: // haut
          case 9376: // haut
          case 9392: // haut
          case 9377: // haut
          case 9378: // haut
          case 9393: // haut
          case 9379: // haut
          case 9394: // haut
            close(map,(short)248);
            break;
          case 9373: // bas
          case 9385: // bas
          case 9386: // bas
          case 9388: // bas
          case 9389: // bas
            close(map,(short)277);
            break;
          case 9395: // haut
            if(isInside(262))
              setInside(null);
            close(map,(short)248);
            break;
        }
        break;
      case 274: // Droite
        close(map,(short)259);
        break;
      case 231: // Droite
        if(map.getId()==(short)9395)
          if(isInside(231))
            setInside(null);
        close(map,(short)216);
        break;
      case 144: // Droite
        close(map,(short)158);
        break;
      case 51: // Haut
        if(map.getId()==(short)9395)
          if(isInside(51))
            setInside(null);
        close(map,(short)65);
        break;
      case 48: // Haut
        close(map,(short)63);
        break;
      case 22: // Haut
        close(map,(short)37);
        break;
      case 442: // Bas
        close(map,(short)413);
        close(map,(short)428);
        break;
      case 417: // Bas
        if(map.getId()==(short)9395)
          if(isInside(417))
            setInside(null);
        close(map,(short)402);
        break;
      case 414: // Bas
        close(map,(short)399);
        break;
    }
  }

  public static void closeExit(GameMap map, GameCase cell)
  {
    if(map==null||cell==null)
      return;

    switch(cell.getId())
    {
      case 262: // Gauche
        if(map.getId()==(short)9387)
          if(isOutside(277))
            setOutside(null);
        close(map,(short)277);
        break;
      case 216: // Droite
        if(map.getId()==(short)9381)
          if(isOutside(201))
            setOutside(null);
        close(map,(short)201);
        break;
      case 36: // Haut
        if(map.getId()==(short)9377)
          if(isOutside(36))
            setOutside(null);
        close(map,(short)50);
        break;
      case 413: // Bas
        if(map.getId()==(short)9375)
          if(isOutside(399))
            setOutside(null);
        close(map,(short)399);
        break;
    }
  }

  public static void open(GameMap map, GameCase cell)
  {
    if(map==null||cell==null)
      return;
    if(map.getId()==(short)9395)
      if(inside!=null)
        close(map,cell);

    switch(cell.getId())
    {
      case 320: // Gauche
        open(map,(short)306);
        break;
      case 262: // Gauche
        switch(map.getId())
        {
          case 9371: // haut
          case 9374: // haut
          case 9390: // haut
          case 9375: // haut
          case 9391: // haut
          case 9376: // haut
          case 9395: // haut
          case 9392: // haut
          case 9377: // haut
          case 9378: // haut
          case 9393: // haut
          case 9379: // haut
          case 9394: // haut
            if(map.getId()==(short)9395)
              setInside(map.getCase(248));
            open(map,(short)248);
            break;
          case 9373: // bas
          case 9385: // bas
          case 9386: // bas
          case 9388: // bas
          case 9389: // bas
            open(map,(short)277);
            break;
        }
        break;
      case 274: // Droite
        open(map,(short)259);
        break;
      case 231: // Droite
        if(map.getId()==(short)9395)
          setInside(map.getCase(216));
        open(map,(short)216);
        break;
      case 144: // Droite
        open(map,(short)158);
        break;
      case 51: // Haut
        if(map.getId()==(short)9395)
          setInside(map.getCase(65));
        open(map,(short)65);
        break;
      case 48: // Haut
        open(map,(short)63);
        break;
      case 22: // Haut
        open(map,(short)37);
        break;
      case 442: // Bas
        open(map,(short)413);
        open(map,(short)428);
        break;
      case 417: // Bas
        if(map.getId()==(short)9395)
          setInside(map.getCase(402));
        open(map,(short)402);
        break;
      case 414: // Bas
        open(map,(short)399);
        break;
    }
  }

  public static GameCase getLeftCell(GameMap map)
  {
    if(map==null)
      return null;
    switch(map.getId())
    {
      case 9372:
      case 9380:
      case 9381:
      case 9382:
      case 9383:
      case 9384:
        return map.getCase(320);
      case 9371:
      case 9374:
      case 9373:
      case 9385:
      case 9386:
      case 9390:
      case 9375:
      case 9391:
      case 9376:
      case 9395:
      case 9387:
      case 9388:
      case 9392:
      case 9377:
      case 9378:
      case 9393:
      case 9379:
      case 9389:
      case 9394:
        return map.getCase(262);
    }
    return null;
  }

  public static GameCase getRightCell(GameMap map)
  {
    if(map==null)
      return null;
    switch(map.getId())
    {
      case 9371:
      case 9375:
      case 9376:
      case 9378:
      case 9377:
      case 9379:
        return map.getCase(274);
      case 9374:
      case 9391:
      case 9390:
      case 9395:
      case 9392:
      case 9393:
      case 9394:
        return map.getCase(231);
      case 9372:
      case 9380:
      case 9381:
      case 9382:
      case 9383:
      case 9384:
        return map.getCase(216);
      case 9373:
      case 9385:
      case 9386:
      case 9387:
      case 9388:
      case 9389:
        return map.getCase(144);
    }
    return null;
  }

  public static GameCase getUpCell(GameMap map)
  {
    if(map==null)
      return null;
    switch(map.getId())
    {
      case 9374:
      case 9391:
      case 9395:
      case 9390:
      case 9392:
      case 9393:
      case 9394:
        return map.getCase(51);
      case 9386:
      case 9373:
      case 9385:
      case 9387:
      case 9388:
      case 9389:
        return map.getCase(48);
      case 9371:
      case 9375:
      case 9376:
      case 9377:
      case 9378:
      case 9379:
        return map.getCase(36);
      case 9372:
      case 9380:
      case 9381:
      case 9382:
      case 9383:
      case 9384:
        return map.getCase(22);
    }
    return null;
  }

  public static GameCase getDownCell(GameMap map)
  {
    if(map==null)
      return null;
    switch(map.getId())
    {
      case 9372:
      case 9384:
      case 9380:
      case 9381:
      case 9382:
      case 9383:
        return map.getCase(442);
      case 9390:
      case 9393:
      case 9374:
      case 9394:
      case 9391:
      case 9395:
      case 9392:
        return map.getCase(417);
      case 9373:
      case 9389:
      case 9385:
      case 9386:
      case 9387:
      case 9388:
        return map.getCase(414);
      case 9371:
      case 9375:
      case 9376:
      case 9377:
      case 9378:
      case 9379:
        return map.getCase(413);
    }
    return null;
  }

  private static void open(GameMap map, short cellId)
  {
    sendOpen(map,cellId);
    map.removeCase(cellId);
    map.getCases().add(new GameCase(map,cellId,true,(byte)0,(byte)0,(byte)0,true,true,-1));
  }

  private static void close(final GameMap map, final short cellId)
  {
    sendClose(map,cellId);
    map.removeCase(cellId);
    map.getCases().add(new GameCase(map,cellId,true,(byte)0,(byte)0,(byte)0,false,false,-1));
  }

  private static void sendOpen(GameMap map, int cellId)
  {
    SocketManager.GAME_UPDATE_CELL(map,cellId+";aaGaaaaaaa801;1");
    SocketManager.GAME_SEND_ACTION_TO_DOOR(map,cellId,true);
  }

  private static void sendOpen(Player p, int cellId)
  {
    SocketManager.GAME_UPDATE_CELL(p,cellId+";aaGaaaaaaa801;1");
    SocketManager.GAME_SEND_ACTION_TO_DOOR(p,cellId,true);
  }

  private static void sendClose(GameMap map, int cellId)
  {
    SocketManager.GAME_UPDATE_CELL(map,cellId+";aaaaaaaaaa801;1");
    SocketManager.GAME_SEND_ACTION_TO_DOOR(map,cellId,false);
  }

  private static void sendClose(Player p, int cellId)
  {
    SocketManager.GAME_UPDATE_CELL(p,cellId+";aaaaaaaaaa801;1");
    SocketManager.GAME_SEND_ACTION_TO_DOOR(p,cellId,false);
  }

  private static void initializeMap(int id, int c1, int c2, int c3, int c4)
  {
    // Ferme toutes les portes et ouvre une porte dans chaque salle.
    closeMap(id,c1,c2,c3,c4);
    GameMap map=Main.world.getMap((short)id);
    GameCase cell=randomCase(map,c1,c2,c3,c4);

    switch(id)
    {
      case 9395: // 13�me
        PigDragon.open(map,cell);
        break;
      case 9375: // 8�me
        if(cell.getId()==413)
        {
          if(PigDragon.outside==null)
            PigDragon.setOutside(map.getCase(returnCell(map,413)));
          else
            while(cell.getId()==413)
              cell=randomCase(map,c1,c2,c3,c4);
        }
        PigDragon.open(map,returnCell(map,cell.getId()));
        break;
      case 9381: // 12�me
        if(cell.getId()==216)
        {
          if(PigDragon.outside==null)
            PigDragon.setOutside(map.getCase(returnCell(map,216)));
          else
            while(cell.getId()==216)
              cell=randomCase(map,c1,c2,c3,c4);

        }
        PigDragon.open(map,returnCell(map,cell.getId()));
        break;
      case 9387: // 14�me
        if(cell.getId()==262)
        {
          if(PigDragon.outside==null)
            PigDragon.setOutside(map.getCase(returnCell(map,262)));
          else
            while(cell.getId()==262)
              cell=randomCase(map,c1,c2,c3,c4);
        }
        PigDragon.open(map,returnCell(map,cell.getId()));
        break;
      case 9377: // 18�me
        if(cell.getId()==36)
        {
          if(PigDragon.outside==null)
            PigDragon.setOutside(map.getCase(returnCell(map,36)));
          else
            while(cell.getId()==36)
              cell=randomCase(map,c1,c2,c3,c4);
        }
        PigDragon.open(map,returnCell(map,cell.getId()));
        break;
      default:
        PigDragon.open(map,cell);
        break;
    }
  }

  private static GameCase randomCase(GameMap map, int c1, int c2, int c3, int c4)
  {
    switch(Formulas.getRandomValue(0,3))
    {
      case 0:
        return map.getCase(c1);
      case 1:
        return map.getCase(c2);
      case 2:
        return map.getCase(c3);
      case 3:
        return map.getCase(c4);
    }
    return map.getCase(c1);
  }

  private static void initializeExt()
  {
    GameMap map8=Main.world.getMap((short)9375);
    GameCase cell8=map8.getCase(413);
    GameMap map12=Main.world.getMap((short)9381);
    GameCase cell12=map12.getCase(216);
    GameMap map14=Main.world.getMap((short)9387);
    GameCase cell14=map14.getCase(262);
    GameMap map18=Main.world.getMap((short)9377);
    GameCase cell18=map18.getCase(36);

    switch(Formulas.getRandomValue(0,3))
    {
      case 0: //map8
        //System.out.println("0");
        closeMap(9375,413,274,262,36);
        closeExit(map12,cell12);
        closeExit(map14,cell14);
        closeExit(map18,cell18);
        openExit(map8,cell8);
        break;
      case 1: //map12
        //System.out.println("1");
        closeMap(9381,442,320,216,22);
        closeExit(map8,cell8);
        closeExit(map14,cell14);
        closeExit(map18,cell18);
        openExit(map12,cell12);
        break;
      case 2: //map14
        //System.out.println("2");
        closeMap(9387,414,262,144,48);
        closeExit(map8,cell8);
        closeExit(map12,cell12);
        closeExit(map18,cell18);
        openExit(map14,cell14);
        break;
      case 3: //map18
        //System.out.println("3");
        closeMap(9377,413,274,262,36);
        closeExit(map8,cell8);
        closeExit(map12,cell12);
        closeExit(map14,cell14);
        openExit(map18,cell18);
        break;
    }
  }

  private static void closeMap(int id, int c1, int c2, int c3, int c4)
  {
    GameMap map=Main.world.getMap((short)id);
    close(map,map.getCase(c1));
    close(map,map.getCase(c2));
    close(map,map.getCase(c3));
    close(map,map.getCase(c4));
  }

  public static void sendPacketMap(Player perso)
  {
    GameMap map=perso.getCurMap();
    GameCase c1=null,c2=null,c3=null,c4=null;
    switch(map.getId())
    {
      case 9371:
      case 9375:
      case 9376:
      case 9377:
      case 9378:
      case 9379:
        c1=map.getCase(returnCell(map,413));
        c2=map.getCase(returnCell(map,274));
        c3=map.getCase(returnCell(map,262));
        c4=map.getCase(returnCell(map,36));
        break;
      case 9372:
      case 9380:
      case 9381:
      case 9382:
      case 9383:
      case 9384:
        c1=map.getCase(returnCell(map,442));
        c2=map.getCase(returnCell(map,320));
        c3=map.getCase(returnCell(map,216));
        c4=map.getCase(returnCell(map,22));
        break;
      case 9373:
      case 9385:
      case 9386:
      case 9387:
      case 9388:
      case 9389:
        c1=map.getCase(returnCell(map,414));
        c2=map.getCase(returnCell(map,262));
        c3=map.getCase(returnCell(map,144));
        c4=map.getCase(returnCell(map,48));
        break;
      case 9374:
      case 9390:
      case 9391:
      case 9392:
      case 9393:
      case 9394:
      case 9395:
        c1=map.getCase(returnCell(map,417));
        c2=map.getCase(returnCell(map,262));
        c3=map.getCase(returnCell(map,231));
        c4=map.getCase(returnCell(map,51));
        break;
    }

    if(c1!=null)
    {
      if(c1.isLoS())
        sendOpen(perso,c1.getId());
      else
        sendClose(perso,c1.getId());
    }
    if(c2!=null)
    {
      if(c2.isLoS())
        sendOpen(perso,c2.getId());
      else
        sendClose(perso,c2.getId());
    }
    if(c3!=null)
    {
      if(c3.isLoS())
        sendOpen(perso,c3.getId());
      else
        sendClose(perso,c3.getId());
    }
    if(c4!=null)
    {
      if(c4.isLoS())
        sendOpen(perso,c4.getId());
      else
        sendClose(perso,c4.getId());
    }
  }

  public static short returnCell(GameMap map, int cell)
  {
    switch(cell)
    {
      case 320: // Gauche
        return 306;
      case 262: // Gauche
        switch(map.getId())
        {
          case 9371: // haut
          case 9374: // haut
          case 9390: // haut
          case 9375: // haut
          case 9391: // haut
          case 9376: // haut
          case 9395: // haut
          case 9392: // haut
          case 9377: // haut
          case 9378: // haut
          case 9393: // haut
          case 9379: // haut
          case 9394: // haut
            return 248;
          case 9373: // bas
          case 9385: // bas
          case 9386: // bas
          case 9388: // bas
          case 9389: // bas
          case 9387:
            return 277;
        }
        break;
      case 274: // Droite
        return 259;
      case 231: // Droite
        return 216;
      case 216: // Droite
        return 201;
      case 144: // Droite
        return 158;
      case 51: // Haut
        return 65;
      case 48: // Haut
        return 63;
      case 36: // Haut
        return 50;
      case 22: // Haut
        return 37;
      case 442: // Bas
        return 428;
      case 417: // Bas
        return 402;
      case 414: // Bas
      case 413: // Bas
        return 399;
    }
    return -1;
  }

  private static void setOutside(GameCase cell)
  {
    PigDragon.outside=cell;
  }

  private static void setInside(GameCase cell)
  {
    PigDragon.inside=cell;
  }

  private static boolean isOutside(int cell)
  {
    return outside!=null&&(cell==outside.getId());
  }

  private static boolean isInside(int cell)
  {
    return inside!=null&&(cell==inside.getId());
  }

  public static void openExit(GameMap map, GameCase cell)
  {
    if(map==null||cell==null)
      return;

    switch(map.getId())
    {
      case 9387:
        open(map,(short)277);
        break;
      case 9381:
        open(map,(short)201);
        break;
      case 9377:
        open(map,(short)50);
        break;
      case 9375:
        open(map,(short)399);
        break;
    }
  }
}