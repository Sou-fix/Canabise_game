package soufix.game;

import org.apache.mina.core.session.IoSession;

import soufix.Hdv.Hdv;
import soufix.Hdv.HdvEntry;
import soufix.Hdv.HdvLine;
import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.area.map.entity.House;
import soufix.area.map.entity.InteractiveDoor;
import soufix.area.map.entity.InteractiveObject;
import soufix.area.map.entity.MountPark;
import soufix.area.map.entity.Trunk;
import soufix.area.map.entity.Tutorial;
import soufix.client.Account;
import soufix.client.Player;
import soufix.client.other.Party;
import soufix.command.CommandAdmin;
import soufix.command.CommandPlayer;
import soufix.command.administration.AdminUser;
import soufix.common.ConditionParser;
import soufix.common.CryptManager;
import soufix.common.Encriptador;
import soufix.common.Formulas;
import soufix.common.PathFinding;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.Collector;
import soufix.entity.Npc;
import soufix.entity.Prism;
import soufix.entity.exchange.CraftSecure;
import soufix.entity.exchange.Exchange;
import soufix.entity.exchange.NpcExchange;
import soufix.entity.exchange.NpcExchangePets;
import soufix.entity.exchange.NpcRessurectPets;
import soufix.entity.exchange.PlayerExchange;
import soufix.entity.exchange.Stake;
import soufix.entity.mount.Mount;
import soufix.entity.npc.NpcAnswer;
import soufix.entity.npc.NpcQuestion;
import soufix.entity.npc.NpcTemplate;
import soufix.entity.pet.Pet;
import soufix.entity.pet.PetEntry;
import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.spells.Spell;
import soufix.game.action.ExchangeAction;
import soufix.game.action.GameAction;
import soufix.guild.Guild;
import soufix.guild.GuildMember;
import soufix.job.Job;
import soufix.job.JobAction;
import soufix.job.JobConstant;
import soufix.job.JobStat;
import soufix.job.fm.BreakingObject;
import soufix.job.fm.Rune;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Logging;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectTemplate;
import soufix.object.entity.Capture;
import soufix.object.entity.Fragment;
import soufix.other.Action;
import soufix.other.Dopeul;
import soufix.other.Titre;
import soufix.quest.Quest;
import soufix.quest.QuestPlayer;
import soufix.quest.QuestStep;
import soufix.utility.Logger;
import soufix.utility.LoggerManager;
import soufix.utility.NameGenerator;
import soufix.utility.Pair;
import soufix.utility.TimerWaiterPlus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class GameClient
{
  private final IoSession session;
  private Account account;
  private Player player;
  private boolean walk=false;
  private AdminUser adminUser;
  private final Map<Integer, GameAction> actions=new HashMap<>();
  public long timeLastTradeMsg=0, timeLastRecrutmentMsg=0, timeLastAlignMsg=0, timeLastChatMsg=0, timeLastIncarnamMsg=0, timeLastTaverne, lastPacketTime=0, action=0, timeLastAct=0 , timeLastd=0;
private String preparedKeys;
  private int averagePing=0;
  private boolean creatingCharacter=false;
  private boolean characterSelect=true;
	public String[] _aKeys = new String[16];
	public int currentKey;
	public boolean crypt;
  public Logger _logger;
  public boolean show_cell_fight;
  public boolean show_cell_BANK;
  public boolean BANK_RAPIDE;
  public long timeLasttpcommande=0 , timeLastprisme_zaap_zaapi=0;
  public int chek;

  public GameClient(IoSession session)
  {
	    this.session=session;
	    this.session.write("HG");

   try {
	   Main.gameServer.newClient();
	   this._logger = LoggerManager.getLoggerByIp(this.session.getRemoteAddress().toString().substring(1).split("\\:")[0]);
   if(!Config.getInstance().Ip.contains("127.0.0.1")){
		crearPacketKey();
		}
   }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public IoSession getSession()
  {
    return session;
  }

  public Player getPlayer()
  {
    return this.player;
  }

  public Account getAccount()
  {
    return account;
  }

  public String getPreparedKeys()
  {
    return preparedKeys;
  }

  public void parsePacket(String packet) throws InterruptedException
  {
	  if(this.crypt){
			packet = Encriptador.unprepareData((String) packet, this.currentKey, this._aKeys);
			}
    this.lastPacketTime=System.currentTimeMillis();
	if (this.account == null) {
        this._logger.addToLog(
                "[GAME] Account=Null Character=Null [PACKET] =" + packet);
    } else if (this.player == null) {
        this._logger.addToLog(
                "[GAME] Account=" + this.account.getName() + " Character=Null [PACKET] =" + packet);
    } else {
        this._logger.addToLog(
                "[Game] Account=" + this.account.getName() + " Character=" + this.player.getName() + " MAPID=" + this.player.getCurMap().getId() + "  KAMAS=" + this.player.getKamas() + " [PACKET] =" + packet); 
        }
    if(packet.length()>3&&packet.substring(0,4).equalsIgnoreCase("ping"))
    {
      SocketManager.GAME_SEND_PONG(this);
      return;
    }
    if(packet.length()>4&&packet.substring(0,5).equalsIgnoreCase("qping"))
    {
      SocketManager.GAME_SEND_QPONG(this);
      return;
    }
   /* if(this.player!=null)
    {
      if(this.player.isChangeName())
      {
        this.changeName(packet);
        return;
      }
    }*/
    switch(packet.charAt(0))
    {
      case 'A':
        parseAccountPacket(packet);
        break;
      case 'B':
        parseBasicsPacket(packet);
        break;
      case 'C':
        parseConquestPacket(packet);
        break;
      case 'c':
        parseChanelPacket(packet);
        break;
      case 'D':
        parseDialogPacket(packet);
        break;
      case 'd':
        parseDocumentPacket(packet);
        break;
      case 'E':
        parseExchangePacket(packet);
        break;
      case 'e':
        parseEnvironementPacket(packet);
        break;
      case 'F':
        parseFrienDDacket(packet);
        break;
      case 'f':
        parseFightPacket(packet);
        break;
      case 'G':
        parseGamePacket(packet);
        break;
      case 'g':
        parseGuildPacket(packet);
        break;
      case 'h':
        parseHousePacket(packet);
        break;
      case 'i':
        parseEnemyPacket(packet);
        break;
      case 'J':
        parseJobOption(packet);
        break;
      case 'K':
        parseHouseKodePacket(packet);
        break;
      case 'O':
        parseObjectPacket(packet);
        break;
      case 'P':
        parseGroupPacket(packet);
        break;
      case 'R':
        parseMountPacket(packet);
        break;
      case 'Q':
        parseQuestData(packet);
        break;
      case 'S':
        parseSpellPacket(packet);
        break;
      case 'T':
        parseFoireTroll(packet);
        break;
      case 'W':
        parseWaypointPacket(packet);
        break;
      case 'Z':
       // parseCustomPacket(packet);
    	  switch(packet.charAt(1))
  	    {
  	    case 's':
  	    	 switch(packet.charAt(2))
  	  	    {
  	  	    case 'C':
  	  	    	this.changement_color(packet);
  	  	    break;
  	  	case '5':
  		    SocketManager.GAME_SEND_MESSAGE(player,"Le Service titre coute <b>"+Config.getInstance().prix_titre+"</b> points!","000000");
  	  		this.send("bà‘");
	  	    break;
  	  case 'T':
	  	    
	  	    break;
	  	    
  	  	    }
  	        break;
  	    }
        break;
      case 'X':
    	  switch(packet.charAt(1))
    	    {
    	      case 'D':
    	        Core(packet);
    	        break;
    	      case 'F': //Changer de morph
    				MorphitemChange(packet);
    			break;
    	      case 'S':
    	    	  changement_size(packet);
    	        break;
    	      case 'N':
    	    	  changeName(packet);
    	        break;
    	      case 'C':
    	  	    	this.changement_color(packet);
    	  	    break;
    	      case 'T':
    	    	  this.add_titre(packet);  
    	      case 'M':
    	    	  this.Change_pos(packet);
  	  	    break;
    	  	  
    	    }
           break;

      default:
        break;
    }
  }

  /**
   * AccountPacket *
   */
  private void parseAccountPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'A':
        addCharacter(packet);
        break;
      case 'E':
          Shop(packet);
       break;
      case 'B':
        boost(packet);
        break;
      case 'D':
        deleteCharacter(packet);
        break;
      case 'f':
        getQueuePosition();
        break;
      case 'g':
       // getGifts(packet.substring(2));
        break;
      case 'G':
       // attributeGiftToCharacter(packet.substring(2));
        break;
      case 'i':
        sendIdentity(packet);
        break;
      case 'k':
        //setKeyIndex(Byte.parseByte(packet.substring(2), 16));
        break;
      case 'L':
    	//  SocketManager.PACKET_ALL_CLASSE(this); 
    	 /* this.send("à‘A");
    	  this.send("à‘B");
    	  this.send("à‘a0");
    	  this.send("à‘e2.0,100,100");
    	  this.send("à‘rR");
    	  this.send("à‘D10");
    	  this.send("à‘I");
    	  this.send("à‘p20");
    	  this.send("à‘Vhttp://localhost/mp3/");
    	  this.send("bo1");*/
        getCharacters(/*(packet.length() == 2)*/);
        break;
      case 'R':
        retry(Integer.parseInt(packet.substring(2)));
        break;
      case 'S':
        setCharacter(packet);
        break;
      case 'T':
        sendTicket(packet);
        break;
      case 'V':
        requestRegionalVersion();
        break;
      case 'P':
        String name=NameGenerator.nameGenerator.compose((int)(Math.random()*3+Formulas.getRandomValue(1,5)));
        SocketManager.send(this,"APK"+name);
        break;
    }
  }
  private void crearPacketKey() {
	  try {
	   currentKey = Formulas.getRandomValue(1, 15);
	   String key = Encriptador.crearKey(16);
	   _aKeys[currentKey] = Encriptador.prepareKey(key);
	   SocketManager.ENVIAR_AK_KEY_ENCRIPTACION_PACKETS(this,currentKey, key);
	   crypt = true;
	  }
	   catch(Exception e)
	    {
	      e.printStackTrace();
	    }
	 }
  //v2.5 - 5th character fix
  private void addCharacter(String packet)
  {
    String[] infos=packet.substring(2).split("\\|");
    if(Database.getStatics().getPlayerData().exist(infos[0]))
    {
      SocketManager.GAME_SEND_NAME_ALREADY_EXIST(this);
      return;
    }
    //Validation du nom du this.playernnage
    boolean isValid=true;
    String name=infos[0].toLowerCase();
    //Và¯Â¿Â½rifie d'abord si il contient des termes dà¯Â¿Â½finit
    if(name.length()>20||name.length()<3||name.contains("mod")||name.contains("admin")||name.contains("putain")||name.contains("administrateur")||name.contains("puta"))
    {
      isValid=false;
    }

    //Si le nom passe le test, on và¯Â¿Â½rifie que les caractà¯Â¿Â½re entrà¯Â¿Â½ sont correct.
    if(isValid)
    {
      int tiretCount=0;
      for(char curLetter : name.toCharArray())
      {
        if(!((curLetter>='a'&&curLetter<='z')||curLetter=='-'))
        {
          isValid=false;
          break;
        }
        if(curLetter=='-')
        {
          if(tiretCount>=1)
          {
            isValid=false;
            break;
          }
          else
          {
            tiretCount++;
          }
        }
      }
    }

    String cap=name.substring(0,1).toUpperCase()+name.substring(1);

    //Si le nom est invalide
    if(!isValid)
    {
      SocketManager.GAME_SEND_NAME_ALREADY_EXIST(this);
      return;
    }
    if(this.account.getPlayers().size()>5)
    {
      SocketManager.GAME_SEND_CREATE_PERSO_FULL(this);
      return;
    }
    if(this.account.createPlayer(cap,Integer.parseInt(infos[2]),Integer.parseInt(infos[1]),Integer.parseInt(infos[3]),Integer.parseInt(infos[4]),Integer.parseInt(infos[5])))
    {
      SocketManager.GAME_SEND_CREATE_OK(this);
      SocketManager.GAME_SEND_PERSO_LIST(this,this.account.getPlayers(),this.account.getSubscribeRemaining());
    }
    else
    {
      SocketManager.GAME_SEND_CREATE_FAILED(this);
    }
  }
  

  private void boost(String packet)
  {
    try
    {
      int stat=-1;
      if(this.player.getMorphMode())
      {
        this.player.sendMessage("Vous étes incarné, vous ne pouvez pas utiliser vos points caractéristiques dans cet état.");
        return;
      }

      if(packet.substring(2).contains(";"))
      {
        stat=Integer.parseInt(packet.substring(2).split(";")[0]);
        if(stat>0)
        {
          int code=0;
          code=Integer.parseInt(packet.substring(2).split(";")[1]);
          if(code<0)
            return;
          if(this.player.get_capital()<code)
            code=this.player.get_capital();
          this.player.boostStatFixedCount(stat,code);
        }
      }
      else
      {
        stat=Integer.parseInt(packet.substring(2).split("/u000A")[0]);
        this.player.boostStat(stat,true);
        SocketManager.GAME_SEND_STATS_PACKET(this.player);
       Database.getStatics().getPlayerData().update(this.player);
      }
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
  }

  private void deleteCharacter(String packet)
  {
    String[] split=packet.substring(2).split("\\|");
    int GUID=Integer.parseInt(split[0]);
    String answer=split.length>1 ? split[1] : "";
    if(this.account.getPlayers().containsKey(GUID)&&!this.account.getPlayers().get(GUID).isOnline())
    {
      if(this.account.getPlayers().get(GUID).getLevel()<20||(this.account.getPlayers().get(GUID).getLevel()>=20&&answer.equals(this.account.getAnswer().replace(" ","%20"))))
      {
        this.account.deletePlayer(GUID);
        SocketManager.GAME_SEND_PERSO_LIST(this,this.account.getPlayers(),this.account.getSubscribeRemaining());
      }
      else
      {
        SocketManager.GAME_SEND_DELETE_PERSO_FAILED(this);
      }
    }
    else
    {
      SocketManager.GAME_SEND_DELETE_PERSO_FAILED(this);
    }
  }

  private void getQueuePosition()
  {
    SocketManager.MULTI_SEND_Af_PACKET(this,1,1,1,1);
    //SocketManager.MULTI_SEND_Af_PACKET(this, this.queuePlace.getPlace(), QueueThreadPool.executor.getQueue().size(), 0, 1);
  }
  /*
  //v2.7 - replaced String += with StringBuilder
  private void getGifts(String packet)
  {
	  try {
    String gifts=Database.getDynamics().getGiftData().getByAccount(this.account.getId());
    if(gifts==null)
      return;
    if(!gifts.isEmpty())
    {
      StringBuilder data=new StringBuilder();
      int item=-1;
      for(String object : gifts.split(";"))
      {
        int id=Integer.parseInt(object.split(",")[0]),qua=Integer.parseInt(object.split(",")[1]);

        if(data.toString().isEmpty())
          data.append("1~"+Integer.toString(id,16)+"~"+Integer.toString(qua,16)+"~~"+Main.world.getObjTemplate(id).getStrTemplate());
        else
          data.append(";1~"+Integer.toString(id,16)+"~"+Integer.toString(qua,16)+"~~"+Main.world.getObjTemplate(id).getStrTemplate());
        if(item==-1)
          item=id;
      }
      SocketManager.GAME_SEND_Ag_PACKET(this,item,data.toString());
    }
  }
  catch(Exception e)
  {
    e.printStackTrace();
    SocketManager.GAME_SEND_ATTRIBUTE_FAILED(this);
    this.disconnect();
  }
  }
  private void attributeGiftToCharacter(String packet)
  {
    String[] infos=packet.split("\\|");

    int template=Integer.parseInt(infos[0]);
    Player player=Main.world.getPlayer(Integer.parseInt(infos[1]));

    if(player==null)
      return;

    String gifts=Database.getDynamics().getGiftData().getByAccount(this.account.getId());

    if(gifts.isEmpty())
      return;

    for(String data : gifts.split(";"))
    {
      String[] split=data.split(",");
      int id=Integer.parseInt(split[0]);

      if(id==template)
      {
        int qua=Integer.parseInt(split[1]),jp=Integer.parseInt(split[2]);
        GameObject obj;

        if(qua==1)
        {
          obj=Main.world.getObjTemplate(template).createNewItem(qua,(jp==1));
          if(player.addObjet(obj,true))
            World.addGameObject(obj,true);
          if(obj.getTemplate().getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
            obj.setMountStats(player,null);
          String str1=id+","+qua+","+jp,str2=id+","+qua+","+jp+";",str3=";"+id+","+qua+","+jp;

          gifts=gifts.replace(str2,"").replace(str3,"").replace(str1,"");
        }
        else
        {
          obj=Main.world.getObjTemplate(template).createNewItem(1,(jp==1));
          if(player.addObjet(obj,true))
            World.addGameObject(obj,true);
          if(obj.getTemplate().getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
            obj.setMountStats(player,null);
          String str1=id+","+qua+","+jp,str2=id+","+qua+","+jp+";",str3=";"+id+","+qua+","+jp;

          String cstr1=id+","+(qua-1)+","+jp,cstr2=id+","+(qua-1)+","+jp+";",cstr3=";"+id+","+(qua-1)+","+jp;

          gifts=gifts.replace(str2,cstr2).replace(str3,cstr3).replace(str1,cstr1);
        }
        Database.getDynamics().getGiftData().update(player.getAccID(),gifts);
      }
    }

    Database.getStatics().getPlayerData().update(player);

    if(gifts.isEmpty())
      player.send("AG");
    else
    {
      this.getGifts("");
      player.send("AG");
    }
  }
*/
  private void sendIdentity(String packet)
  {
  }
  public long getTimeLastTaverne() {
	return timeLastTaverne;
}

public void setTimeLastTaverne(long timeLastTaverne) {
	this.timeLastTaverne = timeLastTaverne;
}

  private void getCharacters()
  {
	  try {
    this.account.setGameClient(this);
    for(Player player : this.account.getPlayers().values())
    {
      if(player!=null)
        if(player.getFight()!=null&&player.getFight().getFighterByPerso(player)!=null)
        {
          this.player=player;
          this.player.OnJoinGame();
          return;
        }
    }

    SocketManager.GAME_SEND_PERSO_LIST(this,this.account.getPlayers(),this.account.getSubscribeRemaining());
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	      SocketManager.GAME_SEND_ATTRIBUTE_FAILED(this);
	      this.disconnect();
	    }
  }

  private void retry(int id)
  {
    final Player player=this.account.getPlayers().get(id);

    if(player!=null)
    {
      player.revive();
      SocketManager.GAME_SEND_PERSO_LIST(this,this.account.getPlayers(),this.account.getSubscribeRemaining());
    }
    else
    {
      this.getSession().write("BN");
    }
  }

  //v2.6 - connection limit system
  private void setCharacter(String packet)
  {
	  try {
    int id=Integer.parseInt(packet.substring(2));

    if(this.account.getPlayers().get(id)!=null)
    {
      int i=Main.world.getOnlinePlayerCountSameIP(this);
      boolean isPlayer=false;
      if(this.account.getPlayers().get(id).getGroupe()==null)
        isPlayer=true;
      else if(this.account.getPlayers().get(id).getGroupe().isPlayer())
        isPlayer=true;
      if(isPlayer) //not admin group, check limit
      {
        if(i<Config.getInstance().maxconnections)
        {
          this.player=this.account.getPlayers().get(id);
          if(this.player!=null)
          {
            if(this.player.isDead()==1&&Config.getInstance().HEROIC)
              this.getSession().write("BN");
            else
            {
              this.setCharacterSelect(false);
              this.player.OnJoinGame();
            }
            return;
          }
        }
      }
      else //admin group, dont check limit
      {
        this.player=this.account.getPlayers().get(id);
        if(this.player!=null)
        {
          if(this.player.isDead()==1&&Config.getInstance().HEROIC)
            this.getSession().write("BN");
          else
          {
            this.setCharacterSelect(false);
            this.player.OnJoinGame();
          }
          return;
        }
      }
    }
    SocketManager.GAME_SEND_PERSO_SELECTION_FAILED(this);
  }
  catch(Exception e)
  {
    e.printStackTrace();
    SocketManager.GAME_SEND_ATTRIBUTE_FAILED(this);
    this.disconnect();
  }
  }

  private void sendTicket(String packet)
  {
    try
    {
      int id=Integer.parseInt(packet.substring(2));
      if(!Main.gameServer.waitingaccount.contains(id+";")) {
    	  SocketManager.GAME_SEND_ATTRIBUTE_FAILED(this);;	
			this.kick();
      }else {
    	  account=Main.world.getAccount(id);
          if(account==null)
    	  Database.getStatics().getAccountData().load(id);
          else
          if(!account.isLoad_ok())
          Main.world.ReassignAccountToChar(id);
      }
      if(this.account==null || !this.account.isLoad_ok())
      {
			if(this.chek <= 10){
				chek++;
				 new TimerWaiterPlus(() -> {
					 session.write("Af"+chek+"|10|0|1|1");
					sendTicket(packet);	
				 },1000);
			return;
		}else{
				System.out.println("compte not load "+id);
			SocketManager.GAME_SEND_ATTRIBUTE_FAILED(this);;	
			this.kick();
			return;
      }
      }
      else
      {
          Main.gameServer.waitingaccount.remove(id+";");
    	  if(account.getCurrentPlayer()!=null)
              account.getGameClient().kick();
        String ip=this.session.getRemoteAddress().toString().substring(1).split(":")[0];

        if(this.account.getGameClient()!=null)
          this.account.getGameClient().kick();

        this.account.setGameClient(this);
        this.account.setCurrentIp(ip);
        Database.getStatics().getAccountData().setLogged(this.account.getId(),1);

        if(Logging.USE_LOG)
          Logging.getInstance().write("AccountIpConnect",this.account.getName()+" > "+ip);
          this.getSession().write("ATK0");
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      SocketManager.GAME_SEND_ATTRIBUTE_FAILED(this);
      this.disconnect();
    }
  }

  private void requestRegionalVersion()
  {
	  try
	  {
    SocketManager.GAME_SEND_AV0(this);
  }
  catch(Exception e)
  {
    e.printStackTrace();
    SocketManager.GAME_SEND_ATTRIBUTE_FAILED(this);
    this.disconnect();
  }
  }

  @SuppressWarnings("unused")
  private static String generateKey()
  {
    String key="";
    for(int i=0;i<32;i++)
      key=key.concat(String.valueOf(CryptManager.HASH[Formulas.random.nextInt(CryptManager.HASH.length-1)]));
    return key;
  }
  /** Fin Account Packet **/

  /**
   * Basics Packet *
   */
  private void parseBasicsPacket(String packet) throws InterruptedException
  {
    switch(packet.charAt(1))
    {
      case 'A'://Console
        authorisedCommand(packet);
        break;
      case 'D':
        getDate();
        break;
      case 'M':
        tchat(packet);
        break;
      case 'W': // Whois
        whoIs(packet);
        break;
      case 'S':
        this.player.useSmiley(packet.substring(2));
        break;
      case 'Y':
        chooseState(packet);
        break;
      case 'a':
        if(packet.charAt(2)=='M')
          goToMap(packet);
        break;
      case 'p': //average ping system
        setAveragePing(packet);
        break;
    }
  }

  private void authorisedCommand(String packet) throws InterruptedException
  {
    if(this.adminUser==null)
      this.adminUser=new CommandAdmin(this.player);
    if(this.player.getGroupe()==null||this.getPlayer()==null)
    {
      this.getAccount().getGameClient().disconnect();
      return;
    }

    if(Logging.USE_LOG)
      Logging.getInstance().write("CommandAdmin",this.getAccount().getCurrentIp()+" : "+this.getAccount().getName()+" > "+this.getPlayer().getName()+" > "+packet.substring(2));

    this.adminUser.apply(packet,false);
  }

  private void getDate()
  {
    SocketManager.GAME_SEND_SERVER_HOUR(this);
  }

  private void tchat(String packet)
  {
    String msg;
    String lastMsg="";

    if(this.player.isMuted())
    {
      if(this.player.getAccount()!=null)
        this.player.send("Im117;"+this.getAccount().getMutePseudo()+"~"+this.getAccount().getMuteTime());
      return;
    }
    if(this.player.getCurMap()!=null)
    {
      if(this.player.getCurMap().isMute()&&this.player.getGroupe()==null)
      {
        this.player.sendServerMessage("The map is currently muted.");
        return;
      }
    }

    packet=packet.replace("<","");
    packet=packet.replace(">","");
    if(packet.length()<6)
      return;

    switch(packet.charAt(2))
    {
      case '*'://Defaut
        if(System.currentTimeMillis()-timeLastChatMsg<500)
        {
          this.send("M10");
          return;
        }
        timeLastChatMsg=System.currentTimeMillis();
        if(!this.player.get_canaux().contains(packet.charAt(2)+""))
          return;

        msg=packet.split("\\|",2)[1];
        if(CommandPlayer.analyse(this.player,msg))
          return;
        if(msg.equals(lastMsg))
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"184");
          return;
        }
        if(Config.getInstance().spion)
        {
         SocketManager.GAME_SEND_cMK_PACKET_TO_ADMINV2("@",this.player.getId(),"Spion",this.player.getName()+" general :"+msg);
        }
        if(this.player.isSpec()&&this.player.getFight()!=null)
        {
          int team=this.player.getFight().getTeamId(this.player.getId());
          if(team==-1)
            return;
          SocketManager.GAME_SEND_cMK_PACKET_TO_FIGHT(this.player.getFight(),team,"#",this.player.getId(),this.player.getName(),msg);
          return;
        }
       if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
          if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10844)
            msg=Formulas.translateMsg(msg);
        if(this.player.getFight()==null)
          SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(this.player.getCurMap(),"",this.player.getId(),this.player.getName(),msg);
        else
          SocketManager.GAME_SEND_cMK_PACKET_TO_FIGHT(this.player.getFight(),7,"",this.player.getId(),this.player.getName(),msg);
        break;
      case '^':// Canal Incarnam
        msg=packet.split("\\|",2)[1];
        /*long x;
        if((x=System.currentTimeMillis()-timeLastIncarnamMsg)<30000)
        {
          x=(30000-x)/1000;//Chat antiflood
          SocketManager.GAME_SEND_Im_PACKET(this.player,"0115;"+((int)Math.ceil(x)+1));
          return;
        }
        if(msg.equals(lastMsg))
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"184");
          return;
        }
        if(this.player.getLevel()>150)
          return;
        timeLastIncarnamMsg=System.currentTimeMillis();
        msg=packet.split("\\|",2)[1];
        lastMsg=msg;*/
        if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
          if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10844)
            msg=Formulas.translateMsg(msg);
        SocketManager.GAME_SEND_cMK_PACKET_INCARNAM_CHAT(this.player,"^",this.player.getId(),this.player.getName(),msg);

        break;
      case '#'://Canal Equipe
        if(!this.player.get_canaux().contains(packet.charAt(2)+""))
          return;
        if(this.player.getFight()!=null)
        {
          msg=packet.split("\\|",2)[1];
          int team=this.player.getFight().getTeamId(this.player.getId());
          if(team==-1)
            return;
         if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
            if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10844)
              msg=Formulas.translateMsg(msg);
          SocketManager.GAME_SEND_cMK_PACKET_TO_FIGHT(this.player.getFight(),team,"#",this.player.getId(),this.player.getName(),msg);
          if(Config.getInstance().spion)
          {
           SocketManager.GAME_SEND_cMK_PACKET_TO_ADMINV2("@",this.player.getId(),"Spion",this.player.getName()+" equipe :"+msg);
          }
        }
        break;

      case '$'://Canal groupe
        if(!this.player.get_canaux().contains(packet.charAt(2)+""))
          return;
        if(this.player.getParty()==null)
          break;
        msg=packet.split("\\|",2)[1];
        if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
          if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10844)
            msg=Formulas.translateMsg(msg);
        SocketManager.GAME_SEND_cMK_PACKET_TO_GROUP(this.player.getParty(),"$",this.player.getId(),this.player.getName(),msg);
        if(Config.getInstance().spion)
        {
         SocketManager.GAME_SEND_cMK_PACKET_TO_ADMINV2("@",this.player.getId(),"Spion",this.player.getName()+" groupe :"+msg);
        }
        break;

      case ':'://Canal commerce
        if(!this.player.get_canaux().contains(packet.charAt(2)+""))
          return;
        long l;
        if(this.player.isInAreaNotSubscribe())
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(),'S');
          return;
        }
        if(this.player.cantCanal())
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"You do not have permission to use this channel.","B9121B");
        }
        else if(this.player.isInPrison())
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"You are imprisoned, you can not use this channel.","B9121B");
        }
        else
        {
          if(this.player.getGroupe()==null)
          {
            if((l=System.currentTimeMillis()-timeLastTradeMsg)<50000)
            {
              l=(50000-l)/1000;//On calcul la diffà¯Â¿Â½rence en secondes
              SocketManager.GAME_SEND_Im_PACKET(this.player,"0115;"+((int)Math.ceil(l)+1));
              return;
            }
          }
          timeLastTradeMsg=System.currentTimeMillis();
          msg=packet.split("\\|",2)[1];
         if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
            if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10844)
              msg=Formulas.translateMsg(msg);
          SocketManager.GAME_SEND_cMK_PACKET_TO_ALL(this.player,":",this.player.getId(),this.player.getName(),msg);
        }
        break;
      case '@'://Canal Admin
        if(this.player.getGroupe()==null)
          return;
        msg=packet.split("\\|",2)[1];
       SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@",this.player.getId(),this.player.getName(),msg);
        break;
      case '?'://Canal recrutement
        if(!this.player.get_canaux().contains(packet.charAt(2)+""))
          return;
        if(this.player.isInAreaNotSubscribe())
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(),'S');
          return;
        }
        long j;
        if(this.player.cantCanal())
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"You do not have permission to use this channel.","B9121B");
        }
        else if(this.player.isInPrison())
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"You are imprisoned, you can not use this channel.","B9121B");
        }
        else
        {
          if(this.player.getGroupe()==null)
          {
            if((j=System.currentTimeMillis()-timeLastRecrutmentMsg)<40000)
            {
              j=(40000-j)/1000;//On calcul la diffà¯Â¿Â½rence en secondes
              SocketManager.GAME_SEND_Im_PACKET(this.player,"0115;"+((int)Math.ceil(j)+1));
              return;
            }
          }
          timeLastRecrutmentMsg=System.currentTimeMillis();
          msg=packet.split("\\|",2)[1];
         if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
            if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10844)
              msg=Formulas.translateMsg(msg);
          SocketManager.GAME_SEND_cMK_PACKET_TO_ALL(this.player,"?",this.player.getId(),this.player.getName(),msg);
        }
        break;
      case '%'://Canal guilde
        if(!this.player.get_canaux().contains(packet.charAt(2)+""))
          return;
        if(this.player.get_guild()==null)
          return;
        msg=packet.split("\\|",2)[1];
        if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
          if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10844)
            msg=Formulas.translateMsg(msg);
        SocketManager.GAME_SEND_cMK_PACKET_TO_GUILD(this.player.get_guild(),"%",this.player.getId(),this.player.getName(),msg);
        if(Config.getInstance().spion)
        {
         SocketManager.GAME_SEND_cMK_PACKET_TO_ADMINV2("@",this.player.getId(),"Spion",this.player.getName()+" guilde :"+msg);
        }
        break;
      case '!'://Alignement
        if(!this.player.get_canaux().contains(packet.charAt(2)+""))
          return;
        if(this.player.isInAreaNotSubscribe())
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(),'S');
          return;
        }
        if(this.player.get_align()==0)
          return;
        if(this.player.getDeshonor()>=1)
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"183");
          return;
        }
        long k;
        if((k=System.currentTimeMillis()-timeLastAlignMsg)<30000)
        {
          k=(30000-k)/1000;//On calcul la diffà¯Â¿Â½rence en secondes
          SocketManager.GAME_SEND_Im_PACKET(this.player,"0115;"+((int)Math.ceil(k)+1));
          return;
        }
        timeLastAlignMsg=System.currentTimeMillis();
        msg=packet.split("\\|",2)[1];
        if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
          if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10844)
            msg=Formulas.translateMsg(msg);
        SocketManager.GAME_SEND_cMK_PACKET_TO_ALIGN("!",this.player.getId(),this.player.getName(),msg,this.player);
        if(Config.getInstance().spion)
        {
         SocketManager.GAME_SEND_cMK_PACKET_TO_ADMINV2("@",this.player.getId(),"Spion",this.player.getName()+" aligne :"+msg);
        }
        break;
      default:
        String nom=packet.substring(2).split("\\|")[0];
        msg=packet.split("\\|",2)[1];
        Player target=Main.world.getPlayerByName(nom);
        if(target==null)//si le this.playernnage n'existe pas
        {
          if(this.player.getGroupe()!=null)
            SocketManager.GAME_SEND_MESSAGE(this.player,"Target: "+target);
          SocketManager.GAME_SEND_CHAT_ERROR_PACKET(this,nom);
          return;
        }
        if(target.getAccount()==null)
        {
          if(this.player.getGroupe()!=null)
            SocketManager.GAME_SEND_MESSAGE(this.player,"Account: "+target.getAccount());
          SocketManager.GAME_SEND_CHAT_ERROR_PACKET(this,nom);
          return;
        }
        if(target.getGameClient()==null)//si le this.player n'est pas co
        {
          SocketManager.GAME_SEND_CHAT_ERROR_PACKET(this,nom);
          return;
        }
        if(target.getAccount().isEnemyWith(this.player.getAccount().getId())||!target.isDispo(this.player))
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"114;"+target.getName());
          return;
        }
        if(msg==lastMsg)
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"184");
          return;
        }
        if(this.player.getGroupe()==null&&target.isInvisible()) // Alors pas connectà¯Â¿Â½
        {
          SocketManager.GAME_SEND_CHAT_ERROR_PACKET(this,nom);
          return;
        }
        if(target.mpToTp)
        {
          if(this.player.getFight()!=null)
            return;
          this.player.thatMap=this.player.getCurMap().getId();
          this.player.thatCell=this.player.getCurCell().getId();
          this.player.teleport(target.getCurMap().getId(),target.getCurCell().getId());
          return;
        }
        if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
          if(this.player.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10844)
            msg=Formulas.translateMsg(msg);
        SocketManager.GAME_SEND_cMK_PACKET(target,"F",this.player.getId(),this.player.getName(),msg);
        SocketManager.GAME_SEND_cMK_PACKET(this.player,"T",target.getId(),target.getName(),msg);
        if(Config.getInstance().spion)
        {
         SocketManager.GAME_SEND_cMK_PACKET_TO_ADMINV2("@",this.player.getId(),"Spion",this.player.getName()+" prive :"+msg);
        }
        if(target.getAccount().isMuted())
          this.send("Im0168;"+target.getName()+"~"+target.getAccount().getMuteTime());
        break;
    }
  }

  private void whoIs(String packet)
  {
    packet=packet.substring(2);
    Player player=Main.world.getPlayerByName(packet);
    if(player==null)
    {
      if(packet.isEmpty())
        SocketManager.GAME_SEND_BWK(this.player,this.player.getAccount().getPseudo()+"|1|"+this.player.getName()+"|"+(this.player.getCurMap().getSubArea()!=null ? this.player.getCurMap().getSubArea().getArea().getId() : "-1"));
      else
        this.player.send("PIEn"+packet);

    }
    else
    {
      if(!player.isOnline())
      {
        this.player.send("PIEn"+player.getName());
        return;
      }
      if(this.player.getAccount().isFriendWith(player.getId()))
        SocketManager.GAME_SEND_BWK(this.player,player.getAccount().getPseudo()+"|1|"+player.getName()+"|"+(player.getCurMap().getSubArea()!=null ? player.getCurMap().getSubArea().getArea().getId() : "-1"));
      else if(player==this.player)
        SocketManager.GAME_SEND_BWK(this.player,this.player.getAccount().getPseudo()+"|1|"+this.player.getName()+"|"+(this.player.getCurMap().getSubArea()!=null ? this.player.getCurMap().getSubArea().getArea().getId() : "-1"));
      else
        SocketManager.GAME_SEND_BWK(this.player,player.getAccount().getPseudo()+"|1|"+player.getName()+"|-1");
    }
  }

  private void chooseState(String packet)
  {
    switch(packet.charAt(2))
    {
      case 'A': //Absent
        if(this.player._isAbsent)
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"038");
          this.player._isAbsent=false;
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"037");
          this.player._isAbsent=true;
        }
        break;
      case 'I': //Invisible
        if(this.player._isInvisible)
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"051");
          this.player._isInvisible=false;
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"050");
          this.player._isInvisible=true;
        }
        break;
    }
  }

  // Tà¯Â¿Â½là¯Â¿Â½portation de MJ
  private void goToMap(String packet)
  {
    if(this.player.getGroupe()==null)
      return;
    if(this.player.getGroupe().isPlayer())
      return;

    String datas=packet.substring(3);
    if(datas.isEmpty())
      return;
    int MapX=Integer.parseInt(datas.split(",")[0]);
    int MapY=Integer.parseInt(datas.split(",")[1]);
    ArrayList<GameMap> i=Main.world.getMapByPosInArrayPlayer(MapX,MapY,this.player);
    GameMap map=null;
    if(i.size()<=0)
      return;
    else if(i.size()>1)
      map=i.get(Formulas.getRandomValue(0,i.size()-1));
    else if(i.size()==1)
      map=i.get(0);
    if(map==null)
      return;
    int CellId=map.getRandomFreeCellId();
    if(map.getCase(CellId)==null)
      return;
    if(this.player.getFight()!=null)
      return;

    this.player.teleport(map.getId(),CellId);
  }

  /** Fin Basics Packet **/

  /**
   * Conquest Packet *
   */
  private void parseConquestPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'b':
        requestBalance();
        break;
      case 'B':
        getAlignedBonus();
        break;
      case 'W':
        worldInfos(packet);
        break;
      case 'I':
        prismInfos(packet);
        break;
      case 'F':
        prismFight(packet);
        break;
    }
  }

  public void requestBalance()
  {
    SocketManager.SEND_Cb_BALANCE_CONQUETE(this.player,Main.world.getBalanceWorld(this.player.get_align())+";"+Main.world.getBalanceArea(this.player.getCurMap().getSubArea().getArea(),this.player.get_align()));
  }

  public void getAlignedBonus()
  {
    double porc=Main.world.getBalanceWorld(this.player.get_align());
    double porcN=Math.rint((this.player.getGrade()/2.5)+1);
    SocketManager.SEND_CB_BONUS_CONQUETE(this.player,porc+","+porc+","+porc+";"+porcN+","+porcN+","+porcN+";"+porc+","+porc+","+porc);
  }

  private void worldInfos(String packet)
  {
    switch(packet.charAt(2))
    {
      case 'J':
        SocketManager.SEND_CW_INFO_WORLD_CONQUETE(this.player,Main.world.PrismesGeoposition(1));
        SocketManager.SEND_CW_INFO_WORLD_CONQUETE(this.player,Main.world.PrismesGeoposition(2));
        break;
      case 'V':
        SocketManager.SEND_CW_INFO_WORLD_CONQUETE(this.player,Main.world.PrismesGeoposition(1));
        SocketManager.SEND_CW_INFO_WORLD_CONQUETE(this.player,Main.world.PrismesGeoposition(2));
        break;
    }
  }

  private void prismInfos(String packet)
  {
    if(packet.charAt(2)=='J'||packet.charAt(2)=='V')
    {
      switch(packet.charAt(2))
      {
        case 'J':
          Prism prism=Main.world.getPrisme(this.player.getCurMap().getSubArea().getPrismId());
          if(prism!=null)
          {
            Prism.parseAttack(this.player);
            Prism.parseDefense(this.player);
          }
          SocketManager.SEND_CIJ_INFO_JOIN_PRISME(this.player,this.player.parsePrisme());
          break;
      }
    }
  }

  private void prismFight(String packet)
  {
    switch(packet.charAt(2))
    {
      case 'J':
        if(this.player.isInPrison())
          return;
        final int PrismeID=this.player.getCurMap().getSubArea().getPrismId();
        Prism prism=Main.world.getPrisme(PrismeID);
        if(prism==null)
          return;
        int FightID=-1;
        try
        {
          FightID=prism.getFightId();
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        short MapID=-1;
        try
        {
          MapID=prism.getMap();
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        int cellID=-1;
        try
        {
          cellID=prism.getCell();
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        if(PrismeID==-1||FightID==-1||MapID==-1||cellID==-1)
          return;
        if(this.player.getFight()!=null||prism.getAlignement()!=this.player.get_align()||this.player.isDead()==1||Main.world.getMap(MapID)==null)
        {
          SocketManager.GAME_SEND_BN(this.player);
          return;
        }

        final short map=MapID;
        final int cell=cellID;
        final Fight fight=Main.world.getMap(map).getFight(FightID);

        if(fight==null)
        {
          SocketManager.GAME_SEND_BN(this.player);
          return;
        }

        if(this.player.getCurMap().getId()!=MapID)
        {
          this.player.setCurMap(this.player.getCurMap());
          this.player.setCurCell(this.player.getCurCell());
          this.player.setOldMap(this.player.getCurMap().getId());
          this.player.setOldCell(this.player.getCurCell().getId());
          this.player.teleport(map,cell);
        }

        new TimerWaiterPlus(() -> {
          fight.joinPrismFight(this.player,(fight.getInit0().isPrisme() ? fight.getInit0() : fight.getInit1()).getTeam());
          Main.world.getOnlinePlayers().stream().filter(player -> player!=null).filter(player -> player.get_align()==player.get_align()).forEach(Prism::parseDefense);
        },2000);

        break;
    }
  }

  /** Fin Conquest Packet **/

  /**
   * Chat Packet *
   */
  private void parseChanelPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'C'://Changement des Canaux
        subscribeChannels(packet);
        break;
    }
  }

  private void subscribeChannels(String packet)
  {
    String chan=packet.charAt(3)+"";
    switch(packet.charAt(2))
    {
      case '+'://Ajthis du Canal
        this.player.addChanel(chan);
        break;
      case '-'://Desactivation du canal
        this.player.removeChanel(chan);
        break;
    }
  //Database.getStatics().getPlayerData().update(this.player);
  }

  /** Fin Chat Packet **/

  /**
   * Dialog Packet *
   */
  private void parseDialogPacket(String packet)
  {
	    final Party party=this.player.getParty();

    switch(packet.charAt(1))
    {
      case 'C'://Demande de l'initQuestion
        create(packet);
        if(party!=null&&this.player.getFight()==null&&party.getMaster()!=null&&party.getMaster().getName().equals(this.player.getName()))
        {
          new TimerWaiterPlus(() -> party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1,false)).forEach(follower -> follower.getGameClient().parseDialogPacket(packet)),0);
        }
        break;

      case 'R'://Rà¯Â¿Â½ponse du joueur
        response(packet);
        if(party!=null&&this.player.getFight()==null&&party.getMaster()!=null&&party.getMaster().getName().equals(this.player.getName()))
        {
          new TimerWaiterPlus(() -> party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster2(follower1,false)).forEach(follower -> follower.getGameClient().parseDialogPacket(packet)),0);
        }
        break;

      case 'V'://Fin du dialog
        leave();
        if(party!=null&&this.player.getFight()==null&&party.getMaster()!=null&&party.getMaster().getName().equals(this.player.getName()))
        {
          new TimerWaiterPlus(() -> party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1,false)).forEach(follower -> follower.getGameClient().parseDialogPacket(packet)),0);
        }
        break;
    }

  }

  private void create(String packet)
  {
    try
    {
      if(this.player.isInAreaNotSubscribe()||this.player.getExchangeAction()!=null)
      {
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(),'S');
        GameClient.leaveExchange(this.player);
        return;
      }

      int id=Integer.parseInt(packet.substring(2).split((char)0x0A+"")[0]);
      Collector collector=Main.world.getCollector(id);

      if(collector!=null&&collector.getMap()==this.player.getCurMap().getId())
      {
        SocketManager.GAME_SEND_DCK_PACKET(this,id);
        SocketManager.GAME_SEND_QUESTION_PACKET(this, Main.world.getGuild(collector.getGuildId()).parseQuestionTaxCollector());
        return;
      }

      Npc npc=this.player.getCurMap().getNpc(id);
      if(npc==null)
        return;

      SocketManager.GAME_SEND_DCK_PACKET(this,id);
      int questionId=npc.getTemplate().getInitQuestionId(this.player.getCurMap().getId());

      NpcQuestion question=Main.world.getNPCQuestion(questionId);

      if(question==null)
      {
        SocketManager.GAME_SEND_END_DIALOG_PACKET(this);
        return;
      }

      if(npc.getTemplate().getId()==870)
      {
        Quest quest=Quest.getQuestById(185);
        if(quest!=null)
        {
          QuestPlayer questPlayer=this.player.getQuestPersoByQuest(quest);
          if(questPlayer!=null)
          {
            if(questPlayer.isFinish())
            {
              SocketManager.GAME_SEND_END_DIALOG_PACKET(this);
              return;
            }
          }
        }
      }
      else if(npc.getTemplate().getId()==891)
      {
        Quest quest=Quest.getQuestById(200);
        if(quest!=null)
          if(this.player.getQuestPersoByQuest(quest)==null)
            quest.applyQuest(this.player);
      }
      else if(npc.getTemplate().getId()==925&&this.player.getCurMap().getId()==(short)9402)
      {
        Quest quest=Quest.getQuestById(231);
        if(quest!=null)
        {
          QuestPlayer questPlayer=this.player.getQuestPersoByQuest(quest);
          if(questPlayer!=null)
          {
            if(questPlayer.isFinish())
            {
              question=Main.world.getNPCQuestion(4127);
              if(question==null)
              {
                SocketManager.GAME_SEND_END_DIALOG_PACKET(this);
                return;
              }
            }
          }
        }
      }
      else if(npc.getTemplate().getId()==577&&this.player.getCurMap().getId()==(short)7596)
      {
        if(this.player.hasItemTemplate(2106,1))
          question=Main.world.getNPCQuestion(2407);
      }
      else if(npc.getTemplate().getId()==1041&&this.player.getCurMap().getId()==(short)10255&&questionId==5516)
      {
        if(this.player.get_align()==1)
        {// bontarien
          if(this.player.getSexe()==0)
            question=Main.world.getNPCQuestion(5519);
          else
            question=Main.world.getNPCQuestion(5520);
        }
        else if(this.player.get_align()==2)
        {// brakmarien
          if(this.player.getSexe()==0)
            question=Main.world.getNPCQuestion(5517);
          else
            question=Main.world.getNPCQuestion(5518);
        }
        else
        { // Neutre ou mercenaire
          question=Main.world.getNPCQuestion(5516);
        }
      }

      ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.TALKING_WITH,id);
      this.player.setExchangeAction(exchangeAction);

      SocketManager.GAME_SEND_QUESTION_PACKET(this,question.parse(this.player));

      for(QuestPlayer questPlayer : new ArrayList<>(this.player.getQuestPerso().values()))
      {
        boolean loc1=false;
        for(QuestStep questStep : questPlayer.getQuest().getQuestSteps())
          if(questStep.getNpc()!=null&&questStep.getNpc().getId()==this.player.getCurMap().getNpc(exchangeAction.getValue()).getTemplate().getId())
            loc1=true;

        Quest quest=questPlayer.getQuest();
        if(quest==null||questPlayer.isFinish())
          continue;
        NpcTemplate npcTemplate=quest.getNpcTemplate();
        if(npcTemplate==null&&!loc1)
          continue;

        quest.updateQuestData(this.player,false,0);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  //v2.7 - Replaced String += with StringBuilder
  private void response(String packet)
  {
    String[] infos=packet.substring(2).split("\\|");
    try
    {
      ExchangeAction<?> checkExchangeAction=this.player.getExchangeAction();
      if(checkExchangeAction==null||checkExchangeAction.getType()!=ExchangeAction.TALKING_WITH)
        return;

      @SuppressWarnings("unchecked")
      ExchangeAction<Integer> exchangeAction=(ExchangeAction<Integer>)this.player.getExchangeAction();
      if(this.player.getCurMap().getNpc(exchangeAction.getValue())==null)
        return;

      int answerId=Integer.parseInt(infos[1]);
      NpcQuestion question=Main.world.getNPCQuestion(Integer.parseInt(infos[0]));
      NpcAnswer answer=Main.world.getNpcAnswer(answerId);

      if(question==null||answer==null)
      {
        this.player.setIsOnDialogAction(-1);
        SocketManager.GAME_SEND_END_DIALOG_PACKET(this);
        return;
      }

      try
      {
        if(!this.player.getQuestPerso().isEmpty())
        {
          for(QuestPlayer QP : this.player.getQuestPerso().values())
          {
            if(QP.isFinish()||QP.getQuest()==null||QP.getQuest().getNpcTemplate()==null)
              continue;
            ArrayList<QuestStep> QEs=QP.getQuest().getQuestSteps();
            for(QuestStep qe : QEs)
            {
              if(qe==null)
                continue;
              if(QP.isQuestStepIsValidate(qe))
                continue;

              NpcTemplate npc=qe.getNpc();
              NpcTemplate curNpc=this.player.getCurMap().getNpc(exchangeAction.getValue()).getTemplate();

              if(npc==null||curNpc==null)
                continue;
              if(npc.getId()==curNpc.getId())
                QP.getQuest().updateQuestData(this.player,false,answerId);
            }
          }
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }

      if(answerId==6604||answerId==6605)
      {
        String stats="",statsReplace="";
        if(this.player.hasItemTemplate(10207,1))
          stats=this.player.getItemTemplate(10207).getTxtStat().get(Constant.STATS_NAME_DJ);
        try
        {
          for(String answer0 : question.getAnwsers().split(";"))
          {
            for(Action action : Main.world.getNpcAnswer(Integer.parseInt(answer0)).getActions())
            {
              if((action.getId()==15||action.getId()==16)&&this.player.hasItemTemplate(10207,1))
              {
                for(String i : stats.split(","))
                {
                  GameMap map=this.player.getCurMap();
                  if(map!=null)
                  {
                    Npc npc=map.getNpc(exchangeAction.getValue());
                    if(npc!=null&&npc.getTemplate()!=null&&Dopeul.parseConditionTrousseau(i.replace(" ",""),npc.getTemplate().getId(),map.getId()))
                    {
                      this.player.teleport(Short.parseShort(action.getArgs().split(",")[0]),Integer.parseInt(action.getArgs().split(",")[1]));
                      statsReplace=i;
                    }
                  }
                }
              }
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }

        if(answerId==6605&&!statsReplace.isEmpty())
        {
          StringBuilder newStats=new StringBuilder();
          for(String i : stats.split(","))
            if(!i.equals(statsReplace))
              newStats.append((newStats.toString().isEmpty() ? i : ","+i));
          this.player.getItemTemplate(10207).getTxtStat().remove(Constant.STATS_NAME_DJ);
          this.player.getItemTemplate(10207).getTxtStat().put(Constant.STATS_NAME_DJ,newStats.toString());
        }
        SocketManager.GAME_SEND_UPDATE_ITEM(this.player,this.player.getItemTemplate(10207));
      }
      else if(answerId==4628)
      {
        if(this.player.hasItemTemplate(9487,1) && Config.getInstance().HEROIC)
        {
          String date=this.player.getItemTemplate(9487,1).getTxtStat().get(Constant.STATS_DATE);
          long timeStamp=Long.parseLong(date);
          if(System.currentTimeMillis()-timeStamp<=1209600000)
          {
            new Action(1,"5522","",Main.world.getMap((short)10255)).apply(this.player,null,-1,-1);
            return;
          }
        }
        new Action(1,"5521","",Main.world.getMap((short)10255)).apply(this.player,null,-1,-1);
        return;
      }

      boolean leave=answer.apply(this.player);

      if(!answer.isAnotherDialog())
      {
        if(this.player.getIsOnDialogAction()==1)
        {
          this.player.setIsOnDialogAction(-1);
        }
        else
        {
          if(leave)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(this);
            if(this.player.getExchangeAction()!=null&&this.player.getExchangeAction().getType()==ExchangeAction.TALKING_WITH)
              this.player.setExchangeAction(null);
          }
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      this.player.setIsOnDialogAction(-1);
      this.player.setExchangeAction(null);
      SocketManager.GAME_SEND_END_DIALOG_PACKET(this);
    }
  }

  private void leave()
  {
    this.player.setAway(false);
    this.walk=false;
    if(this.player.getExchangeAction()!=null&&this.player.getExchangeAction().getType()==ExchangeAction.TALKING_WITH)
      this.player.setExchangeAction(null);
    SocketManager.GAME_SEND_END_DIALOG_PACKET(this);
  }

  /** Fin Dialog Packet **/

  /**
   * Document Packet *
   */
  private void parseDocumentPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'V':
        this.player.send("dV");
        break;
    }
  }

  /** Fin Document Packet **/

  /**
   * Exchange Packet *
   */
  private synchronized void parseExchangePacket(String packet)
  {
	  if(this.player == null)
		  return;
    if(this.player.isDead()==1)
      return;
    if(this.player.getGroupe() != null)
    if(this.player.getGroupe().getId() > 1 && this.player.getGroupe().getId() < 5) {
		 this.player.sendMessage("action impossible personnage staff"); 
		 GameClient.leaveExchange(this.player);
		 return;
	  }
    switch(packet.charAt(1))
    {
      case 'A'://Accepter demande d'à¯Â¿Â½change
        accept();
        break;
      case 'B'://Achat
        buy(packet);
        break;
      case 'H'://Demande prix moyen + catà¯Â¿Â½gorie
        bigStore(packet);
        break;
      case 'K'://Ok
        ready();
        break;
      case 'L'://jobAction : Refaire le craft prà¯Â¿Â½cedent
        replayCraft();
        break;
      case 'M'://Move (Ajthiser//retirer un objet a l'à¯Â¿Â½change)
        movementItemOrKamas(packet);
        break;
      case 'P':
        movementItemOrKamasDons(packet.substring(2));
        break;
      case 'q'://Mode marchand (demande de la taxe)
        askOfflineExchange();
        break;
      case 'Q'://Mode marchand (Si valider aprà¯Â¿Â½s la taxe)
        offlineExchange();
        break;
      case 'r'://Rides => Monture
        putInInventory(packet);
        break;
      case 'f'://Etable => Enclos
        putInMountPark(packet);
        break;
      case 'R'://liste d'achat NPC
        request(packet);
        break;
      case 'S'://Vente
        sell(packet);
        break;
      case 'J'://Livre artisant
        bookOfArtisant(packet);
        break;
      case 'W'://Metier public
        setPublicMode(packet);
        break;
      case 'V'://Fin de l'à¯Â¿Â½change
        leaveExchange(this.player);
        break;
    }
  }

  private void accept()
  {
	  try
	  {
    ExchangeAction<?> checkExchangeAction=this.player.getExchangeAction();

    if(Config.getInstance().tradeAsBlocked||this.player.isDead()==1||checkExchangeAction==null||!(checkExchangeAction.getValue() instanceof Integer)||(checkExchangeAction.getType()!=ExchangeAction.TRADING_WITH_PLAYER&&checkExchangeAction.getType()!=ExchangeAction.CRAFTING_SECURE_WITH&&checkExchangeAction.getType()!=ExchangeAction.CRAFTING_SECURE_WITH&&checkExchangeAction.getType()!=ExchangeAction.STAKE))
      return;

    @SuppressWarnings("unchecked")
    ExchangeAction<Integer> exchangeAction=(ExchangeAction<Integer>)this.player.getExchangeAction();
    Player target=Main.world.getPlayer(exchangeAction.getValue());
    if(target==null)
      return;

    checkExchangeAction=target.getExchangeAction();

    if(target.isDead()==1||checkExchangeAction==null||!(checkExchangeAction.getValue() instanceof Integer)||(checkExchangeAction.getType()!=ExchangeAction.TRADING_WITH_PLAYER&&checkExchangeAction.getType()!=ExchangeAction.CRAFTING_SECURE_WITH&&checkExchangeAction.getType()!=ExchangeAction.STAKE))
      return;

    int type=this.player.getIsCraftingType().get(0);

    if(checkExchangeAction.getType()==ExchangeAction.STAKE)
    {
      SocketManager.GAME_SEND_EXCHANGE_CONFIRM_OK(this,1);
      SocketManager.GAME_SEND_EXCHANGE_CONFIRM_OK(target.getGameClient(),1);
      Stake stake=new Stake(target,this.player);
      @SuppressWarnings("rawtypes")
      ExchangeAction newExchangeAction=new ExchangeAction<>(ExchangeAction.STAKE,stake);
      this.player.setExchangeAction(newExchangeAction);
      target.setExchangeAction(newExchangeAction);
      this.player.getIsCraftingType().clear();
      target.getIsCraftingType().clear();
      return;
    }

    switch(type)
    {
      case 1: // Echange PlayerVsPlayer
        SocketManager.GAME_SEND_EXCHANGE_CONFIRM_OK(this,1);
        SocketManager.GAME_SEND_EXCHANGE_CONFIRM_OK(target.getGameClient(),1);
        PlayerExchange exchange=new PlayerExchange(target,this.player);
        @SuppressWarnings("rawtypes")
        ExchangeAction newExchangeAction=new ExchangeAction<>(ExchangeAction.TRADING_WITH_PLAYER,exchange);
        this.player.setExchangeAction(newExchangeAction);
        target.setExchangeAction(newExchangeAction);
        this.player.getIsCraftingType().clear();
        target.getIsCraftingType().clear();
        break;
      case 12: // Craft sàƒÂ©ucirsàƒÂ©
      case 13:
        Player player1=(target.getIsCraftingType().get(0)==12 ? target : this.player);
        Player player2=(target.getIsCraftingType().get(0)==13 ? target : this.player);

        int max=JobConstant.getTotalCaseByJobLevel(player1.getMetierBySkill(player1.getIsCraftingType().get(1)).get_lvl());
        SocketManager.GAME_SEND_ECK_PACKET(this,type,max+";"+this.player.getIsCraftingType().get(1));
        SocketManager.GAME_SEND_ECK_PACKET(target.getGameClient(),target.getIsCraftingType().get(0),max+";"+this.player.getIsCraftingType().get(1));

        exchange=new CraftSecure(player1,player2);
        newExchangeAction=new ExchangeAction<>(ExchangeAction.CRAFTING_SECURE_WITH,exchange);
        this.player.setExchangeAction(newExchangeAction);
        target.setExchangeAction(newExchangeAction);
        break;
    }
	  }
    catch(Exception e)
    {
      e.printStackTrace();
      return;
    }
  }

  private void buy(String packet)
  {
    String[] infos=packet.substring(2).split("\\|");
    @SuppressWarnings("unused")
    ExchangeAction<?> checkExchangeAction=this.player.getExchangeAction();
    @SuppressWarnings("unchecked")
    ExchangeAction<Integer> exchangeAction=(ExchangeAction<Integer>)this.player.getExchangeAction();
    if(player.boutique)
    {
      try
      {
        int quantity=Integer.parseInt(infos[1]);
        ObjectTemplate template=Main.world.getObjTemplate(Integer.parseInt(infos[0]));
        if(template.getType()==18&&quantity>1)
        {
          this.player.sendMessage("Merci de n'acheter qu'un seul animal de compagnie, \\u00e0 la fois !");
          return;
        }
        if (template.getType() == 85 && quantity > 1) {
			this.player.sendMessage("Merci de n'acheter qu'un seul Gemme spirituelle \u00e0 la fois !");
			return;
		}
		if (template.getType() == 97 && quantity > 1) {
			this.player.sendMessage("Merci de n'acheter qu'un seul Dragodinde \u00e0 la fois !");
			return;
		}
        int value=template.getPoints()*quantity;
        int points=account.getPoints()-value;

        if(points<0)// Si le joueur n'a pas assez de point
        {
          int diferencia=value-account.getPoints();
          SocketManager.GAME_SEND_MESSAGE(player,"Vous n'avez pas assez de points pour acheter "+(quantity>1 ? "these items" : "cet objet")+", vous avez actuellement "+account.getPoints()+" points et tu as besoin "+diferencia+" plus.","FF0000");
          SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
          return;
        }
        account.setPoints(points);
        GameObject newObj=null;
        newObj=template.createNewItem(quantity,true);
        if(template.getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
        {
          Mount mount=new Mount(Constant.getMountColorByParchoTemplate(newObj.getTemplate().getId()),this.getPlayer().getId(),false);
          newObj.clearStats();
          newObj.getStats().addOneStat(995,-(mount.getId()));
          newObj.getTxtStat().put(996,this.getPlayer().getName());
          newObj.getTxtStat().put(997,mount.getName());
          mount.setToMax();
        }
        if(player.addObjet(newObj,true))// Return TRUE si c'est un
          World.addGameObject(newObj,true);
        SocketManager.GAME_SEND_BUY_OK_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(player);
        SocketManager.GAME_SEND_Ow_PACKET(player);
        SocketManager.GAME_SEND_MESSAGE(player,"Vous venez d'acheter "+(quantity>1 ? "items" : "item")+": <b>"+template.getName()+"</b> pour <b>"+value+"</b> points!","000000");
        SocketManager.GAME_SEND_MESSAGE(this.player,
				"Il te reste : " + this.account.getPoints() + " points boutique !");
    Database.getStatics().getPlayerData().shop_item(this.player.getName(), this.player.getAccID(), value, template.getId());
        if(player.getKamas() < 5000)
			SocketManager.GAME_SEND_STATS_BOUTIQUE_PACKET(player);
      }
      catch(Exception e)
      {
        e.printStackTrace();
        SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
        return;
      }
      return;
    }
    //v0.01 - Token Shop
    else if(player.tokenShop)
    {
      try
      {
        int quantity=Integer.parseInt(infos[1]);
        ObjectTemplate template=Main.world.getObjTemplate(Integer.parseInt(infos[0]));
        int value=template.getTokens()*quantity;
        int tokens=player.getTokens()-value;
        if(tokens<0) // Si le joueur n'a pas assez de point
        {
          int diferencia=value-player.getTokens();
          SocketManager.GAME_SEND_MESSAGE(player,"You do not have enough tokens to buy "+(quantity>1 ? "these items" : "this item")+", you currently have "+player.getTokens()+" tokens and you need "+diferencia+" more.","FF0000");
          SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
          return;
        }
        player.setTokens(tokens);
        GameObject newObj=null;
        newObj=template.createNewItem(quantity,false);

        if(player.addObjet(newObj,true))// Return TRUE si c'est un
          World.addGameObject(newObj,true);
        SocketManager.GAME_SEND_BUY_OK_PACKET(this);
        SocketManager.GAME_SEND_STATS_PACKET(player);
        SocketManager.GAME_SEND_Ow_PACKET(player);
        SocketManager.GAME_SEND_MESSAGE(player,"You have bought "+quantity+" <b>"+template.getName()+"</b>(s) for <b>"+value+"</b> tokens!","000000");
      }
      catch(Exception e)
      {
        e.printStackTrace();
        SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
        return;
      }
      return;
    }
    else if(exchangeAction.getType()==ExchangeAction.TRADING_WITH_OFFLINE_PLAYER)
    {
      Player seller=Main.world.getPlayer(exchangeAction.getValue());
      if(seller!=null&&seller!=this.player)
      {
        int itemID=0;
        int qua=0;
        int price=0;
        try
        {
          itemID=Integer.valueOf(infos[0]);
          qua=Integer.valueOf(infos[1]);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          return;
        }

        if(!seller.getStoreItems().containsKey(itemID)||qua<=0)
        {
          SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
          return;
        }
        price=seller.getStoreItems().get(itemID)*qua;
        int price2=seller.getStoreItems().get(itemID);
        GameObject itemStore=World.getGameObject(itemID);
        if(itemStore==null)
          return;
        if(price>this.player.getKamas())
          return;
        if(qua<=0||qua>100000)
          return;
        if(qua>itemStore.getQuantity())
          qua=itemStore.getQuantity();
        if(qua==itemStore.getQuantity())
        {
          seller.getStoreItems().remove(itemStore.getGuid());
          this.player.addObjet(itemStore,true);
        }
        else if(itemStore.getQuantity()>qua)
        {
          seller.getStoreItems().remove(itemStore.getGuid());
          itemStore.setQuantity(itemStore.getQuantity()-qua);
          seller.addStoreItem(itemStore.getGuid(),price2);

          GameObject clone=GameObject.getCloneObjet(itemStore,qua);
          if(this.player.addObjet(clone,true))
            World.addGameObject(clone,false);
        }
        else
        {
          SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
          return;
        }

        //remove kamas
        this.player.addKamas(-price);
        //add seller kamas
        seller.addKamas(price);
       Database.getStatics().getPlayerData().update(seller);
        //send packets
        SocketManager.GAME_SEND_STATS_PACKET(this.player);
        SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(seller,this.player);
        SocketManager.GAME_SEND_BUY_OK_PACKET(this);
        if(seller.getStoreItems().isEmpty())
        {
          if(Main.world.getSeller(seller.getCurMap().getId())!=null&&Main.world.getSeller(seller.getCurMap().getId()).contains(seller.getId()))
          {
            Main.world.removeSeller(seller.getId(),seller.getCurMap().getId());
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(seller.getCurMap(),seller.getId());
            leaveExchange(this.player);
          }
        }
      }
    }
    else
    {

      try
      {
        int id=Integer.parseInt(infos[0]),qua=Integer.parseInt(infos[1]);

        if(qua<=0||qua>100000)
          return;

        ObjectTemplate template=Main.world.getObjTemplate(id);
        Npc npc=this.player.getCurMap().getNpc(exchangeAction.getValue());

        if(template==null)
        {
          SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
          return;
        }
        if(template.getType()==18&&qua>1)
        {
          this.player.sendMessage("Merci de ne pas acheter un seul animal de compagnie, il serait seul.");
          return;
        }
        if (template.getType() == 85 && qua > 1) {
			this.player.sendMessage("Merci de n'acheter qu'un seul Gemme spirituelle \u00e0 la fois !");
			return;
		}
		if (template.getType() == 97 && qua > 1) {
			this.player.sendMessage("Merci de n'acheter qu'un seul Dragodinde \u00e0 la fois !");
			return;
		}
        if(npc==null)
          return;

        NpcTemplate npcTemplate=npc.getTemplate();

        if(!npcTemplate.haveItem(id))
        {
          SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
          return;
        }

        boolean attachObject=(npcTemplate.getInformations()&0x2)==2;
        if (npcTemplate.getId() ==  1121 ||npcTemplate.getId() ==  1127) {
			final int value = template.getPrice() * qua;
			if (!player.hasItemTemplate(10275, value)) {
				SocketManager.GAME_SEND_MESSAGE(this.player,
						"Vous n'avez pas assez de Pévétons pour acheter cet article.");
				SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
				return;
			}
			player.removeByTemplateID(10275, value);
			GameObject object=template.createNewItem(qua,(npcTemplate.getInformations()&0x1)==1);
	          if(template.getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
	          {
	            Mount mount=new Mount(Constant.getMountColorByParchoTemplate(object.getTemplate().getId()),this.getPlayer().getId(),false);
	            object.clearStats();
	            object.getStats().addOneStat(995,-(mount.getId()));
	            object.getTxtStat().put(996,this.getPlayer().getName());
	            object.getTxtStat().put(997,mount.getName());
	          }
	          if(this.player.addObjet(object,true))
	            World.addGameObject(object,true);
	          if(attachObject)
	            object.attachToPlayer(this.player);
	          SocketManager.GAME_SEND_BUY_OK_PACKET(this);
	          SocketManager.GAME_SEND_STATS_PACKET(this.player);
	          SocketManager.GAME_SEND_Ow_PACKET(this.player);
			return;
		}
        if(template.getPoints()>0&&(npcTemplate.getInformations()&0x4)==4)
        {
          int value=template.getPoints()*qua,points=this.account.getPoints();

          if(points<value)
          {
            SocketManager.GAME_SEND_MESSAGE(this.player,"Vous n'avez pas assez de points pour acheter cet article, vous avez actuellement "+points+" points et tu as besoin de "+(value-points)+" pour acheter cet item.");
            SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
            return;
          }

          this.account.setPoints(points-value);
          GameObject object=template.createNewItem(qua,(npcTemplate.getInformations()&0x1)==1);
          if(template.getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
          {
            Mount mount=new Mount(Constant.getMountColorByParchoTemplate(object.getTemplate().getId()),this.getPlayer().getId(),false);
            object.clearStats();
            object.getStats().addOneStat(995,-(mount.getId()));
            object.getTxtStat().put(996,this.getPlayer().getName());
            object.getTxtStat().put(997,mount.getName());
            mount.setToMax();
          }
          if(this.player.addObjet(object,true))
            World.addGameObject(object,true);
          if(attachObject)
            object.attachToPlayer(this.player);

          SocketManager.GAME_SEND_BUY_OK_PACKET(this);
          SocketManager.GAME_SEND_STATS_PACKET(this.player);
          SocketManager.GAME_SEND_Ow_PACKET(this.player);
          SocketManager.GAME_SEND_MESSAGE(this.player,
					"Vous venez d'acheter " + qua + (qua > 1 ? " items" : " item") + " : <b>"
							+ template.getName() + "</b> au prix de <b>" + value + "</b> points boutique !",
					"000000");
        SocketManager.GAME_SEND_MESSAGE(this.player,
					"Il te reste : " + this.account.getPoints() + " points boutique !");
        Database.getStatics().getPlayerData().shop_item(this.player.getName(), this.player.getAccID(), value, template.getId());
        if(player.getKamas() < 5000)
			SocketManager.GAME_SEND_STATS_BOUTIQUE_PACKET(player);
      
        }
        else if(template.getPoints()>0)
        {
          int value=template.getPoints()*qua,points=this.account.getPoints();

          if(points<value)
          {
        	  SocketManager.GAME_SEND_MESSAGE(this.player,"Vous n'avez pas assez de points pour acheter cet article, vous avez actuellement "+points+" points et tu as besoin de "+(value-points)+" pour acheter cet item.");
              SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
            return;
          }

          this.account.setPoints(points-value);
          GameObject object=template.createNewItem(qua,(npcTemplate.getInformations()&0x1)==1);
          if(template.getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
          {
            Mount mount=new Mount(Constant.getMountColorByParchoTemplate(object.getTemplate().getId()),this.getPlayer().getId(),false);
            object.clearStats();
            object.getStats().addOneStat(995,-(mount.getId()));
            object.getTxtStat().put(996,this.getPlayer().getName());
            object.getTxtStat().put(997,mount.getName());
            mount.setToMax();
          }
          if(this.player.addObjet(object,true))
            World.addGameObject(object,true);
          if(attachObject)
            object.attachToPlayer(this.player);

          SocketManager.GAME_SEND_BUY_OK_PACKET(this);
          SocketManager.GAME_SEND_STATS_PACKET(this.player);
          SocketManager.GAME_SEND_Ow_PACKET(this.player);
          SocketManager.GAME_SEND_MESSAGE(this.player,
					"Vous venez d'acheter " + qua + (qua > 1 ? " items" : " item") + " : <b>"
							+ template.getName() + "</b> au prix de <b>" + value + "</b> points boutique !",
					"000000");
          SocketManager.GAME_SEND_MESSAGE(this.player,
					"Il te reste : " + this.account.getPoints() + " points boutique !");
          Database.getStatics().getPlayerData().shop_item(this.player.getName(), this.player.getAccID(), value, template.getId());
          if(player.getKamas() < 5000)
  			SocketManager.GAME_SEND_STATS_BOUTIQUE_PACKET(player);
        }
        else if(template.getPoints()==0)
        {
          int price=template.getPrice()*qua;
          if(price<0)
            return;

          if(this.player.getKamas()<price)
          {
            SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
            return;
          }

          GameObject object=template.createNewItem(qua,(npcTemplate.getInformations()&0x1)==1);
          if(template.getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
          {
            Mount mount=new Mount(Constant.getMountColorByParchoTemplate(object.getTemplate().getId()),this.getPlayer().getId(),false);
            object.clearStats();
            object.getStats().addOneStat(995,-(mount.getId()));
            object.getTxtStat().put(996,this.getPlayer().getName());
            object.getTxtStat().put(997,mount.getName());
          }
          this.player.setKamas(this.player.getKamas()-price);
          if(this.player.addObjet(object,true))
            World.addGameObject(object,true);
          if(attachObject)
            object.attachToPlayer(this.player);
          SocketManager.GAME_SEND_BUY_OK_PACKET(this);
          SocketManager.GAME_SEND_STATS_PACKET(this.player);
          SocketManager.GAME_SEND_Ow_PACKET(this.player);
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
        SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
      }
    }
  }

  //v2.8 - World Market
  @SuppressWarnings("unused")
  private void bigStore(String packet)
  {
    if(this.player.getExchangeAction()==null||this.player.getExchangeAction().getType()!=ExchangeAction.AUCTION_HOUSE_BUYING||this.player.getFight()!=null||this.player.isAway())
      return;
    @SuppressWarnings("unchecked")
    ExchangeAction<Integer> exchangeAction=(ExchangeAction<Integer>)this.player.getExchangeAction();
    int templateID;
    switch(packet.charAt(2))
    {
      case 'B': //Confirmation d'achat
        String[] info=packet.substring(3).split("\\|");//ligneID|amount|price
        Hdv curHdv=null;
      //  if(player.getWorldMarket())
          curHdv=Main.world.getWorldMarket();
       // else
         // curHdv=Main.world.getHdv(Math.abs(exchangeAction.getValue()));

        int ligneID=Integer.parseInt(info[0]);
        byte amount=Byte.parseByte(info[1]);
        HdvLine hL=curHdv.getLine(ligneID);
        if(hL==null)
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"Une erreur s'est produite lors de la confirmation de l'achat. Veuillez contacter un membre du staff.");
          return;
        }
        HdvEntry hE=hL.doYouHave(amount,Integer.parseInt(info[2]));
        if(hE==null)
        {
          // Intervient lorsque un client achà¯Â¿Â½te plusieurs fois la mà¯Â¿Â½me ressource.
          // Par exemple une pyrute à¯Â¿Â½ 45'000k trois fois. Au bout d'un moment elle monte à¯Â¿Â½ 100'000k, mais le client
          // voit toujours 45'000k. Il doit il y avoir un manque de paquet envoyà¯Â¿Â½. La 4à¯Â¿Â½me avait buggà¯Â¿Â½.
          SocketManager.GAME_SEND_MESSAGE(this.player,"[2 - Template '"+hL.getTemplateId()+"'] Une erreur s'est produite lors de la confirmation de l'achat. Veuillez contacter un membre du staff.");
          return;
        }
        Integer owner=hE.getOwner();
        if(owner==null)
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"[3 - Template '"+hL.getTemplateId()+"'] This object does not have an owner. Please contact a staff member.");
          return;
        }
        if(owner==this.player.getAccount().getId())
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"You can not buy your own items.");
          return;
        }
        if(curHdv.buyItem(ligneID,amount,Integer.parseInt(info[2]),this.player))
        {
          SocketManager.GAME_SEND_EHm_PACKET(this.player,"-",ligneID+"");//Enleve la ligne
          if(curHdv.getLine(ligneID)!=null&&!curHdv.getLine(ligneID).isEmpty())
            SocketManager.GAME_SEND_EHm_PACKET(this.player,"+",curHdv.getLine(ligneID).parseToEHm());//Rà¯Â¿Â½ajthise la ligne si elle n'est pas vide
          this.player.refreshStats();
          SocketManager.GAME_SEND_Ow_PACKET(this.player);
          SocketManager.GAME_SEND_Im_PACKET(this.player,"068");//Envoie le message "Lot achetà¯Â¿Â½"
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"172");//Envoie un message d'erreur d'achat
        }
        break;
      case 'l'://Demande listage d'un template (les prix)
        templateID=Integer.parseInt(packet.substring(3));
        try
        {
          //if(player.getWorldMarket())
            SocketManager.GAME_SEND_EHl(this.player,Main.world.getWorldMarket(),templateID);
          //else
          //  SocketManager.GAME_SEND_EHl(this.player,Main.world.getHdv(Math.abs(exchangeAction.getValue())),templateID);
        }
        catch(NullPointerException e)//Si erreur il y a, retire le template de la liste chez le client
        {
          e.printStackTrace();
          SocketManager.GAME_SEND_EHM_PACKET(this.player,"-",templateID+"");
        }

        break;
      case 'P'://Demande des prix moyen
        templateID=Integer.parseInt(packet.substring(3));
        SocketManager.GAME_SEND_EHP_PACKET(this.player,templateID);
        break;
      case 'T'://Demande des template de la catà¯Â¿Â½gorie
        int categ=Integer.parseInt(packet.substring(3));
        String allTemplate;
       // if(player.getWorldMarket())
          allTemplate=Main.world.getWorldMarket().parseTemplate(categ);
        //else
        //  allTemplate=Main.world.getHdv(Math.abs(exchangeAction.getValue())).parseTemplate(categ);
        SocketManager.GAME_SEND_EHL_PACKET(this.player,categ,allTemplate);
        break;
      case 'S': //search
        String[] infos=packet.substring(3).split("\\|");//type | templateId
        int id=Integer.parseInt(infos[1]),category=Integer.parseInt(infos[0]);
        Hdv hdv=null;
        //if(player.getWorldMarket())
          hdv=Main.world.getWorldMarket();
        //else
        //  hdv=Main.world.getHdv(Math.abs(exchangeAction.getValue()));
        String templates=hdv.parseTemplate(category);

        if(templates.isEmpty())
        {
          this.player.send("EHS");
        }
        else
        {
          this.player.send("EHSK");
          SocketManager.GAME_SEND_EHL_PACKET(this.player,category,templates);
          SocketManager.GAME_SEND_EHP_PACKET(this.player,id);
          try
          {
           // if(player.getWorldMarket())
              SocketManager.GAME_SEND_EHl(this.player,Main.world.getWorldMarket(),id);
           // else
            //  SocketManager.GAME_SEND_EHl(this.player,Main.world.getHdv(Math.abs(exchangeAction.getValue())),id);
          }
          catch(NullPointerException e)
          {
            e.printStackTrace();
            SocketManager.GAME_SEND_EHM_PACKET(this.player,"-",String.valueOf(id));
          }
        }
        break;
    }
  }

  private void ready()
  {
    if(this.player.getExchangeAction()==null)
      return;

    ExchangeAction<?> exchangeAction=this.player.getExchangeAction();
    Object value=exchangeAction.getValue();

    if(exchangeAction.getType()==ExchangeAction.CRAFTING&&value instanceof JobAction)
    {
      if(((JobAction)value).isCraft())
      {
        ((JobAction)value).startCraft(this.player);
      }
    }

    if(exchangeAction.getType()==ExchangeAction.TRADING_WITH_NPC_EXCHANGE&&value instanceof NpcExchange)
      ((NpcExchange)value).toogleOK(false);

    if(exchangeAction.getType()==ExchangeAction.TRADING_WITH_NPC_PETS&&value instanceof NpcExchangePets)
      ((NpcExchangePets)value).toogleOK(false);

    if(exchangeAction.getType()==ExchangeAction.TRADING_WITH_NPC_PETS_RESURRECTION&&value instanceof NpcRessurectPets)
      ((NpcRessurectPets)value).toogleOK(false);

    if((exchangeAction.getType()==ExchangeAction.TRADING_WITH_PLAYER||exchangeAction.getType()==ExchangeAction.CRAFTING_SECURE_WITH)&&value instanceof Exchange)
      if(((Exchange)value).toogleOk(this.player.getId()))
        ((Exchange)value).apply();

    if(exchangeAction.getType()==ExchangeAction.STAKE)
      if(((Stake)value).toogleOk(this.player.getId()))
        ((Stake)value).apply();

    if(exchangeAction.getType()==ExchangeAction.BREAKING_OBJECTS&&value instanceof BreakingObject)
    {
      if(((BreakingObject)value).getObjects().isEmpty())
        return;

      Fragment fragment=new Fragment(Database.getStatics().getObjectData().getNextId(),"");

      for(Pair<Integer, Integer> couple : ((BreakingObject)value).getObjects())
      {
        GameObject object=this.player.getItems().get(couple.getLeft());

        if(object==null||couple.getRight()<1||object.getQuantity()<couple.getRight())
        {
          this.player.send("Ea3");
          break;
        }

        for(int k=couple.getRight();k>0;k--)
        {
          int type=object.getTemplate().getType();
          if(type>11&&type<16&&type>23&&type!=81&&type!=82)
            continue;
          for(Map.Entry<Integer, Integer> entry1 : object.getStats().getMap().entrySet())
          {
            int jet=entry1.getValue();
            for(Rune rune : Rune.runes)
            {
              short characteristic=Short.parseShort(Main.world.getObjTemplate(rune.getTemplateId()).getStrTemplate().split("#")[0],16);
              if(entry1.getKey()==characteristic)
              {
                if(rune.getTemplateId()==1557||rune.getTemplateId()==1558||rune.getTemplateId()==7438)
                {
                  double puissance=1.5*(Math.pow(object.getTemplate().getLevel(),2.0)/Math.pow(rune.getPower(),(5.0/4.0)))+((jet-1)/rune.getPower())*(66.66-1.5*(Math.pow(object.getTemplate().getLevel(),2.0)/Math.pow(rune.getPower(),(55.0/4.0))));
                  int chance=(int)Math.ceil(puissance);

                  if(chance>66)
                    chance=66;
                  else if(chance<=0)
                    chance=1;
                  if(Formulas.getRandomValue(1,100)<=chance)
                    fragment.addRune(rune.getTemplateId());
                }
                else
                {
                  double val=(double)rune.getStatsAdd();
                  if(rune.getTemplateId()==7451||rune.getTemplateId()==10662)
                    val*=3.0;

                  double tauxGetMin=Main.world.getTauxObtentionIntermediaire(val,true,(val!=30)),tauxGetMax=(tauxGetMin/(2.0/3.0))/0.9;
                  int tauxMax=(int)Math.ceil(tauxGetMax),tauxGet=(int)Math.ceil(tauxGetMin),tauxMin=2*(tauxMax-tauxGet)-2;

                  if(rune.getTemplateId()==7433||rune.getTemplateId()==7434||rune.getTemplateId()==7435||rune.getTemplateId()==7441)
                    tauxMax++;
                  if(jet<tauxMin)
                    continue;

                  for(int i=jet;i>0;i-=tauxMax)
                  {
                    int j=0;
                    if(i>tauxMax)
                      j=tauxMax;
                    else
                      j=i;
                    if(j==tauxMax)
                      fragment.addRune(rune.getTemplateId());
                    else if(Formulas.getRandomValue(1,100)<(100*(tauxMax-j)/(tauxMax-tauxMin)))
                      fragment.addRune(rune.getTemplateId());
                  }
                }
              }
            }
          }
        }

        if(couple.getRight()==object.getQuantity())
        {
          this.player.deleteItem(object.getGuid());
          Main.world.removeGameObject(object.getGuid());
          SocketManager.SEND_OR_DELETE_ITEM(this,object.getGuid());
        }
        else
        {
          object.setQuantity(object.getQuantity()-couple.getRight());
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,object);
        }
      }

      World.addGameObject(fragment,true);
      this.player.addObjet(fragment);
      SocketManager.GAME_SEND_Ec_PACKET(this.player,"K;8378");
      SocketManager.GAME_SEND_Ow_PACKET(this.player);
      SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"+8378");
      this.player.startActionOnCell(this.player.getGameAction());
      ((BreakingObject)value).getObjects().clear();
    }
  }

  private void replayCraft()
  {
    if(this.player.getExchangeAction()!=null&&this.player.getExchangeAction().getType()==ExchangeAction.CRAFTING)
      if(((JobAction)this.player.getExchangeAction().getValue()).getJobCraft()==null)
        ((JobAction)this.player.getExchangeAction().getValue()).putLastCraftIngredients();
  }

  //v2.8 - World Market
  private synchronized void movementItemOrKamas(String packet)
  {
    if(this.player.getExchangeAction()==null)
      return;
    if(packet.contains("NaN"))
    {
      this.player.sendMessage("Error : StartExchange: ("+this.player.getExchangeAction().getType()+") : "+packet);
      return;
    }
    switch(this.player.getExchangeAction().getType())
    {
      case ExchangeAction.TRADING_WITH_ME:
        switch(packet.charAt(2))
        {
          case 'O'://Objets
            if(packet.charAt(3)=='+')
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {
                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);
                int price=Integer.parseInt(infos[2]);

                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;
                if(qua<=0||obj.isAttach())
                  return;
                if(price<=0)
                  return;

                if(qua>obj.getQuantity())
                  qua=obj.getQuantity();
                this.player.addInStore(obj.getGuid(),price,qua);
              }
              catch(NumberFormatException e)
              {
                Main.world.logger.error("Error Echange Store '"+packet+"' => "+e.getMessage());
                e.printStackTrace();
                return;
              }
            }
            else
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {
                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);

                if(qua<=0)
                  return;
                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;
                if(qua<0)
                  return;
                if(qua>obj.getQuantity())
                  return;
                if(qua<obj.getQuantity())
                  qua=obj.getQuantity();
                this.player.removeFromStore(obj.getGuid(),qua);
              }
              catch(NumberFormatException e)
              {
                Main.world.logger.error("Error Echange Store '"+packet+"' => "+e.getMessage());
                e.printStackTrace();
                return;
              }
            }
            break;
        }
        break;

      case ExchangeAction.TRADING_WITH_COLLECTOR:
        Collector Collector=Main.world.getCollector((Integer)this.player.getExchangeAction().getValue());
        if(Collector==null||Collector.getInFight()>0)
          return;
        switch(packet.charAt(2))
        {
          case 'G'://Kamas
            if(packet.charAt(3)=='-') //On retire
            {
              long P_Kamas=-1;
              try
              {
                P_Kamas=Integer.parseInt(packet.substring(4));
              }
              catch(NumberFormatException e)
              {
                e.printStackTrace();
                Main.world.logger.error("Error Echange CC '"+packet+"' => "+e.getMessage());
              }
              if(P_Kamas<0)
                return;
              if(Collector.getKamas()>=P_Kamas)
              {//Faille non connu ! :p
                long P_Retrait=Collector.getKamas()-P_Kamas;
                Collector.setKamas(Collector.getKamas()-P_Kamas);
                if(P_Retrait<0)
                {
                  P_Retrait=0;
                  P_Kamas=Collector.getKamas();
                }
                Collector.setKamas(P_Retrait);
                this.player.addKamas(P_Kamas);
                SocketManager.GAME_SEND_STATS_PACKET(this.player);
                SocketManager.GAME_SEND_EsK_PACKET(this.player,"G"+Collector.getKamas());
              }
            }
            break;
          case 'O'://Objets
            if(packet.charAt(3)=='-') //On retire
            {
              String[] infos=packet.substring(4).split("\\|");
              int guid=0;
              int qua=0;
              try
              {
                guid=Integer.parseInt(infos[0]);
                qua=Integer.parseInt(infos[1]);
              }
              catch(NumberFormatException e)
              {
                // ok
                return;
              }

              if(guid<=0||qua<=0)
                return;

              GameObject obj=World.getGameObject(guid);
              if(obj==null)
                return;

              if(Collector.haveObjects(guid))
              {
                Collector.removeFromCollector(this.player,guid,qua);
              }
              Collector.addLogObjects(guid,obj);
            }
            break;
        }
        Database.getStatics().getGuildData().update(this.player.get_guild());
        break;
      case ExchangeAction.BREAKING_OBJECTS:
        final BreakingObject breakingObject=((BreakingObject)this.player.getExchangeAction().getValue());

        if(packet.charAt(2)=='O')
        {
          if(packet.charAt(3)=='+')
          {
            if(breakingObject.getObjects().size()>=8)
              return;

            String[] infos=packet.substring(4).split("\\|");

            try
            {
              int id=Integer.parseInt(infos[0]),qua=Integer.parseInt(infos[1]);

              if(!this.player.hasItemGuid(id))
                return;

              GameObject object=World.getGameObject(id);

              if(object==null)
                return;
              if(qua<1)
                return;
              if(qua>object.getQuantity())
                qua=object.getQuantity();

              int type=object.getTemplate().getType();
              if(type>11&&type<16&&type>23&&type!=81&&type!=82)
                return;

              SocketManager.SEND_EMK_MOVE_ITEM(this,'O',"+",id+"|"+breakingObject.addObject(id,qua));
            }
            catch(NumberFormatException e)
            {
              Main.world.logger.error("Error Echange CC '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }
          }
          else if(packet.charAt(3)=='-')
          {
            String[] infos=packet.substring(4).split("\\|");
            try
            {
              int id=Integer.parseInt(infos[0]);
              int qua=Integer.parseInt(infos[1]);

              GameObject object=World.getGameObject(id);

              if(object==null)
                return;
              if(qua<1)
                return;

              final int quantity=breakingObject.removeObject(id,qua);

              if(quantity<=0)
                SocketManager.SEND_EMK_MOVE_ITEM(this,'O',"-",id+"");
              else
                SocketManager.SEND_EMK_MOVE_ITEM(this,'O',"+",id+"|"+quantity);
            }
            catch(NumberFormatException e)
            {
              Main.world.logger.error("Error Echange CC '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }
          }
        }
        else if(packet.charAt(2)=='R')
        {
          final int count=Integer.parseInt(packet.substring(3));
          breakingObject.setCount(count);
          new TimerWaiterPlus(() -> {
            this.recursiveBreakingObject(breakingObject,0,count);
          },0);

        }
        else if(packet.charAt(2)=='r')
        {
          breakingObject.setStop(true);
        }
        break;
      case ExchangeAction.IN_MOUNT:
        Mount mount=this.player.getMount();
        if(mount==null)
          return;
        switch(packet.charAt(2))
        {
          case 'O':// Objet
            int id=0;
            int cant=0;
            try
            {
              id=Integer.parseInt(packet.substring(4).split("\\|")[0]);
              cant=Integer.parseInt(packet.substring(4).split("\\|")[1]);
            }
            catch(Exception e)
            {
              Main.world.logger.error("Error Echange DD '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }
            if(id==0||cant<=0)
              return;
            if(World.getGameObject(id)==null)
            {
              SocketManager.GAME_SEND_MESSAGE(this.player,"Erreur d'inventaire Dragodinde 1: l'article n'existe pas.");
              return;
            }
            switch(packet.charAt(3))
            {
              case '+':
                mount.addObject(id,cant,this.player);
                break;
              case '-':
                mount.removeObject(id,cant,this.player);
                break;
              case ',':
                break;
            }
            break;
        }
        break;

      case ExchangeAction.TRADING_WITH_NPC_EXCHANGE:
        switch(packet.charAt(2))
        {
          case 'O'://Objet ?
            if(packet.charAt(3)=='+')
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {
                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);
                int quaInExch=((NpcExchange)this.player.getExchangeAction().getValue()).getQuaItem(guid,false);

                if(!this.player.hasItemGuid(guid))
                  return;
                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;

                if(qua>obj.getQuantity()-quaInExch)
                  qua=obj.getQuantity()-quaInExch;
                if(qua<=0)
                  return;

                ((NpcExchange)this.player.getExchangeAction().getValue()).addItem(guid,qua);
              }
              catch(NumberFormatException e)
              {
                Main.world.logger.error("Error Echange NPC '"+packet+"' => "+e.getMessage());
                e.printStackTrace();
                return;
              }
            }
            else
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {
                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);

                if(qua<=0)
                  return;
                if(!this.player.hasItemGuid(guid))
                  return;

                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;
                if(qua>((NpcExchange)this.player.getExchangeAction().getValue()).getQuaItem(guid,false))
                  return;

                ((NpcExchange)this.player.getExchangeAction().getValue()).removeItem(guid,qua);
              }
              catch(NumberFormatException e)
              {
                Main.world.logger.error("Error Echange NPC '"+packet+"' => "+e.getMessage());
                e.printStackTrace();
                return;
              }
            }
            break;
          case 'G'://Kamas
            try
            {
              long numb=Integer.parseInt(packet.substring(3));
              if(this.player.getKamas()<numb)
                numb=this.player.getKamas();
              ((NpcExchange)this.player.getExchangeAction().getValue()).setKamas(false,numb);
            }
            catch(NumberFormatException e)
            {
              Main.world.logger.error("Error Echange NPC '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }
            break;
        }
        break;
      case ExchangeAction.TRADING_WITH_NPC_PETS:
        switch(packet.charAt(2))
        {
          case 'O'://Objet ?
            if(packet.charAt(3)=='+')
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {
                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);
                int quaInExch=((NpcExchangePets)this.player.getExchangeAction().getValue()).getQuaItem(guid,false);

                if(!this.player.hasItemGuid(guid))
                  return;
                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;

                if(qua>obj.getQuantity()-quaInExch)
                  qua=obj.getQuantity()-quaInExch;

                if(qua<=0)
                  return;

                ((NpcExchangePets)this.player.getExchangeAction().getValue()).addItem(guid,qua);
              }
              catch(NumberFormatException e)
              {
                Main.world.logger.error("Error Echange Pets '"+packet+"' => "+e.getMessage());
                e.printStackTrace();
                return;
              }
            }
            else
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {
                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);

                if(qua<=0)
                  return;
                if(!this.player.hasItemGuid(guid))
                  return;

                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;
                if(qua>((NpcExchangePets)this.player.getExchangeAction().getValue()).getQuaItem(guid,false))
                  return;

                ((NpcExchangePets)this.player.getExchangeAction().getValue()).removeItem(guid,qua);
              }
              catch(NumberFormatException e)
              {
                Main.world.logger.error("Error Echange Pets '"+packet+"' => "+e.getMessage());
                e.printStackTrace();
                return;
              }
            }
            break;
          case 'G'://Kamas
            try
            {
              long numb=Integer.parseInt(packet.substring(3));
              if(numb<0)
                return;
              if(this.player.getKamas()<numb)
                numb=this.player.getKamas();
              ((NpcExchangePets)this.player.getExchangeAction().getValue()).setKamas(false,numb);
            }
            catch(NumberFormatException e)
            {
              Main.world.logger.error("Error Echange Pets '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }
            break;
        }
        break;

      case ExchangeAction.TRADING_WITH_NPC_PETS_RESURRECTION:
        switch(packet.charAt(2))
        {
          case 'O'://Objet ?
            if(packet.charAt(3)=='+')
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {

                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);
                int quaInExch=((NpcRessurectPets)this.player.getExchangeAction().getValue()).getQuaItem(guid,false);

                if(!this.player.hasItemGuid(guid))
                  return;
                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;

                if(qua>obj.getQuantity()-quaInExch)
                  qua=obj.getQuantity()-quaInExch;

                if(qua<=0)
                  return;

                ((NpcRessurectPets)this.player.getExchangeAction().getValue()).addItem(guid,qua);
              }
              catch(NumberFormatException e)
              {
                Main.world.logger.error("Error Echange RPets '"+packet+"' => "+e.getMessage());
                e.printStackTrace();
                return;
              }
            }
            else
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {
                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);

                if(qua<=0)
                  return;
                if(!this.player.hasItemGuid(guid))
                  return;

                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;
                if(qua>((NpcRessurectPets)this.player.getExchangeAction().getValue()).getQuaItem(guid,false))
                  return;

                ((NpcRessurectPets)this.player.getExchangeAction().getValue()).removeItem(guid,qua);
              }
              catch(NumberFormatException e)
              {
                Main.world.logger.error("Error Echange RPets '"+packet+"' => "+e.getMessage());
                e.printStackTrace();
                return;
              }
            }
            break;
          case 'G'://Kamas
            try
            {
              long numb=Integer.parseInt(packet.substring(3));
              if(numb<0)
                return;
              if(this.player.getKamas()<numb)
                numb=this.player.getKamas();
              ((NpcRessurectPets)this.player.getExchangeAction().getValue()).setKamas(false,numb);
            }
            catch(NumberFormatException e)
            {
              e.printStackTrace();
              return;
            }
            break;
        }
        break;

      case ExchangeAction.AUCTION_HOUSE_SELLING:
        switch(packet.charAt(3))
        {
          case '-'://Retirer un objet de l'HDV
            int count=0,cheapestID=0;
            try
            {
              cheapestID=Integer.parseInt(packet.substring(4).split("\\|")[0]);
              count=Integer.parseInt(packet.substring(4).split("\\|")[1]);
            }
            catch(Exception e)
            {
              Main.world.logger.error("Error Echange HDV '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }
            if(count<=0)
              return;
            this.player.getAccount().recoverItem(cheapestID);//Retire l'objet de la liste de vente du compte
            SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this,'-',"",cheapestID+"");
            break;
          case '+'://Mettre un objet en vente
            if(Integer.parseInt(packet.substring(4).split("\\|")[1])>127)
            {
              SocketManager.GAME_SEND_MESSAGE(this.player,"Vous avez déjà  atteint le nombre maximum d'articles que vous pouvez vendre à  la fois.");
              return;
            }

            int itmID,price=0;
            byte amount=0;

            try
            {
              itmID=Integer.parseInt(packet.substring(4).split("\\|")[0]);
              amount=Byte.parseByte(packet.substring(4).split("\\|")[1]);
              price=Integer.parseInt(packet.substring(4).split("\\|")[2]);
            }
            catch(Exception e)
            {
              Main.world.logger.error("Error Echange HDV '"+packet+"' => "+e.getMessage());
              // Arrive quand price n'est pas dans le pacquet. C'est que le joueur ne veut pas mettre dans un hdv, mais dans autre chose ... Un paquet qui est MO+itmID|qtà¯Â¿Â½
              // Peeut-à¯Â¿Â½tre aprà¯Â¿Â½sa voir utilisà¯Â¿Â½ le concasseur ...
              e.printStackTrace();
              SocketManager.GAME_SEND_MESSAGE(this.player,"Une erreur s'est produite lors de la vente de votre article. Veuillez vous reconnecter pour résoudre le problà¨me.");
              return;
            }
            if(amount > 3 )
                return;
            if(amount<=0||price<=0)
              return;
            if(packet.substring(1).split("\\|")[2]=="0"||packet.substring(2).split("\\|")[2]=="0"||packet.substring(3).split("\\|")[2]=="0")
              return;
            Hdv curHdv=null;
           // if(player.getWorldMarket())
              curHdv=Main.world.getWorldMarket();
           // else
           //   curHdv=Main.world.getHdv(Math.abs((Integer)this.player.getExchangeAction().getValue()));
            curHdv.getHdvId();
            int taxe=(int)(price*(curHdv.getTaxe()/100));

            if(taxe<0)
              return;

            if(!this.player.hasItemGuid(itmID))//Và¯Â¿Â½rifie si le this.playernnage a bien l'item spà¯Â¿Â½cifià¯Â¿Â½ et l'argent pour payer la taxe
              return;
            if(this.player.getAccount().countHdvEntries(curHdv.getHdvId())>=curHdv.getMaxAccountItem())
            {
              SocketManager.GAME_SEND_Im_PACKET(this.player,"058");
              return;
            }
            if(this.player.getKamas()<taxe)
            {
              SocketManager.GAME_SEND_Im_PACKET(this.player,"176");
              return;
            }

            GameObject obj=World.getGameObject(itmID);//Rà¯Â¿Â½cupà¯Â¿Â½re l'item
            if(obj.isAttach())
              return;

            this.player.addKamas(taxe*-1);//Retire le montant de la taxe au this.playernnage
            SocketManager.GAME_SEND_STATS_PACKET(this.player);//Met a jour les kamas du client

            int qua=(amount==1 ? 1 : (amount==2 ? 10 : 100));

            if(qua>obj.getQuantity())//S'il veut mettre plus de cette objet en vente que ce qu'il possà¯Â¿Â½de
              return;
            int rAmount=(int)(Math.pow(10,amount)/10);
            int newQua=(obj.getQuantity()-rAmount);

            if(newQua<=0)//Si c'est plusieurs objets ensemble enleve seulement la quantità¯Â¿Â½ de mise en vente
            {
              this.player.removeItem(itmID);//Enlà¯Â¿Â½ve l'item de l'inventaire du this.playernnage
              SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,itmID);//Envoie un packet au client pour retirer l'item de son inventaire
            }
            else
            {
              obj.setQuantity(obj.getQuantity()-rAmount);
              SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,obj);
              GameObject newObj=GameObject.getCloneObjet(obj,rAmount);
              World.addGameObject(newObj,true);
              obj=newObj;
            }
            HdvEntry toAdd=new HdvEntry(Main.world.getNextObjectHdvId(),price,amount,this.player.getAccount().getId(),obj);
            curHdv.addEntry(toAdd,false); //Ajthise l'entry dans l'HDV
            SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this,'+',"",toAdd.parseToEmK()); //Envoie un packet pour ajthiser l'item dans la fenetre de l'HDV du client
            SocketManager.GAME_SEND_HDVITEM_SELLING(this.player);
            Database.getStatics().getPlayerData().update(this.player);
            break;
        }
        break;
      case ExchangeAction.CRAFTING:
        //Si pas action de craft, on s'arrete la
        if(!((JobAction)this.player.getExchangeAction().getValue()).isCraft())
          return;

        if(packet.charAt(2)=='O'&&((JobAction)this.player.getExchangeAction().getValue()).getJobCraft()==null)
        {
          packet=packet.replace("-",";-").replace("+",";+").substring(4);

          for(String part : packet.split(";"))
          {
            try
            {
              char c=part.charAt(0);
              String[] infos=part.substring(1).split("\\|");
              int id=Integer.parseInt(infos[0]),quantity=Integer.parseInt(infos[1]);

              if(quantity<=0)
                return;
              if(c=='+')
              {
                if(!this.player.hasItemGuid(id))
                  return;

                GameObject obj=this.player.getItems().get(id);

                if(obj==null)
                  return;
                if(obj.getQuantity()<quantity)
                  quantity=obj.getQuantity();

                ((JobAction)this.player.getExchangeAction().getValue()).addIngredient(this.player,id,quantity);
              }
              else if(c=='-')
              {
                ((JobAction)this.player.getExchangeAction().getValue()).addIngredient(this.player,id,-quantity);
              }
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
          }
        }
        else if(packet.charAt(2)=='R')
        {
          if(((JobAction)this.player.getExchangeAction().getValue()).getJobCraft()==null)
          {
            ((JobAction)this.player.getExchangeAction().getValue()).setJobCraft(((JobAction)this.player.getExchangeAction().getValue()).oldJobCraft);
          }
          ((JobAction)this.player.getExchangeAction().getValue()).getJobCraft().setAction(Integer.parseInt(packet.substring(3)));
        }
        else if(packet.charAt(2)=='r')
        {
          if(this.player.getExchangeAction().getValue()!=null)
          {
            if(((JobAction)this.player.getExchangeAction().getValue()).getJobCraft()!=null)
            {
              ((JobAction)this.player.getExchangeAction().getValue()).broken=true;
            }
          }
        }
        break;

      case ExchangeAction.IN_BANK:
        switch(packet.charAt(2))
        {
          case 'G'://Kamas
            if(Config.getInstance().tradeAsBlocked)
              return;
            long kamas=0;
            try
            {
              kamas=Integer.parseInt(packet.substring(3));
            }
            catch(Exception e)
            {
              Main.world.logger.error("Error Echange Banque '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }
            if(kamas==0)
              return;

            if(kamas>0)//Si On ajoute des kamas a la banque
            {
              if(this.player.getKamas()<kamas)
                kamas=this.player.getKamas();
              this.player.setBankKamas(this.player.getBankKamas()+kamas);//On ajthise les kamas a la banque
              this.player.setKamas(this.player.getKamas()-kamas);//On retire les kamas du this.playernnage
              SocketManager.GAME_SEND_STATS_PACKET(this.player);
              SocketManager.GAME_SEND_EsK_PACKET(this.player,"G"+this.player.getBankKamas());
            }
            else
            {
              kamas=-kamas;//On repasse en positif
              if(this.player.getBankKamas()<kamas)
                kamas=this.player.getBankKamas();
              this.player.setBankKamas(this.player.getBankKamas()-kamas);//On retire les kamas de la banque
              this.player.setKamas(this.player.getKamas()+kamas);//On ajthise les kamas du this.playernnage
              SocketManager.GAME_SEND_STATS_PACKET(this.player);
              SocketManager.GAME_SEND_EsK_PACKET(this.player,"G"+this.player.getBankKamas());
            }
            break;

          case 'O'://Objet
            if(Config.getInstance().tradeAsBlocked)
              return;
            int guid=0;
            int qua=0;
            try
            {
              guid=Integer.parseInt(packet.substring(4).split("\\|")[0]);
              qua=Integer.parseInt(packet.substring(4).split("\\|")[1]);
            }
            catch(Exception e)
            {
              Main.world.logger.error("Error Echange Banque '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }

            if(guid==0||qua<=0)
              return;

            switch(packet.charAt(3))
            {
              case '+'://Ajouter a la banque
                this.player.addInBank(guid,qua);
                break;

              case '-'://Retirer de la banque
                GameObject object=World.getGameObject(guid);
                if(object!=null)
                {
                  if(object.getTxtStat().containsKey(Constant.STATS_OWNER_1))
                  {
                    Player player=Main.world.getPlayerByName(object.getTxtStat().get(Constant.STATS_OWNER_1));
                    if(player!=null)
                    {
                      if(!player.getName().equals(this.player.getName()))
                        return;
                    }
                  }
                  this.player.removeFromBank(guid,qua);
                }
                break;
            }
            break;
          case 'B':
        		try {
        			if(BANK_RAPIDE) {
        				this.player.sendMessage("Deja en action");
        				return;
        			}
        			this.BANK_RAPIDE = true;
        			int time = 0;
        			
        			for(String s : packet.substring(4).split(";"))
          	      {
                		
                		if(s == null || s == "")
                			continue;
                		int id = Integer.parseInt(s);
                		 if(id <= 0)
                			 continue;
                		GameObject object=World.getGameObject(id);
                		if(object == null)
                			continue;
                		
                		new TimerWaiterPlus(() -> {
                			 this.player.addInBank(object.getGuid(),object.getQuantity());    
                		    },time);
                		 time= time+600;
          	      }
        			if(time != 0)
        			new TimerWaiterPlus(() -> {
           			this.BANK_RAPIDE = false;
           		    },time+100);
                break;
				} catch (Exception e) {
					this.BANK_RAPIDE = false;
				}
          	
        }
        break;

      case ExchangeAction.IN_TRUNK:
        if(Config.getInstance().tradeAsBlocked)
          return;
        Trunk t=(Trunk)this.player.getExchangeAction().getValue();

        switch(packet.charAt(2))
        {
          case 'G'://Kamas
            long kamas=0;
            try
            {
              kamas=Integer.parseInt(packet.substring(3));
            }
            catch(Exception e)
            {
              Main.world.logger.error("Error Echange Coffre '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }

            if(kamas==0)
              return;

            if(kamas>0)//Si On ajthise des kamas au coffre
            {
              if(this.player.getKamas()<kamas)
                kamas=this.player.getKamas();
              t.setKamas(t.getKamas()+kamas);//On ajthise les kamas au coffre
              this.player.setKamas(this.player.getKamas()-kamas);//On retire les kamas du this.playernnage
              SocketManager.GAME_SEND_STATS_PACKET(this.player);
            }
            else
            {
              kamas=-kamas;//On repasse en positif
              if(t.getKamas()<kamas)
                kamas=t.getKamas();
              t.setKamas(t.getKamas()-kamas);//On retire les kamas de la banque
              this.player.setKamas(this.player.getKamas()+kamas);//On ajthise les kamas du this.playernnage
              SocketManager.GAME_SEND_STATS_PACKET(this.player);
            }
            Main.world.getOnlinePlayers().stream().filter(player -> player.getExchangeAction()!=null&&player.getExchangeAction().getType()==ExchangeAction.IN_TRUNK&&((Trunk)this.player.getExchangeAction().getValue()).getId()==((Trunk)player.getExchangeAction().getValue()).getId()).forEach(P -> SocketManager.GAME_SEND_EsK_PACKET(P,"G"+t.getKamas()));
            Database.getDynamics().getTrunkData().update(t);
            break;

          case 'O'://Objet
            int guid=0;
            int qua=0;
            try
            {
              guid=Integer.parseInt(packet.substring(4).split("\\|")[0]);
              qua=Integer.parseInt(packet.substring(4).split("\\|")[1]);
            }
            catch(Exception e)
            {
              Main.world.logger.error("Error Echange Coffre '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }

            if(guid==0||qua<=0)
              return;

            switch(packet.charAt(3))
            {
              case '+'://Ajthiser a la banque
                t.addInTrunk(guid,qua,this.player);
                break;

              case '-'://Retirer de la banque
                t.removeFromTrunk(guid,qua,this.player);
                break;
            }
            break;
        }
        break;

      case ExchangeAction.CRAFTING_SECURE_WITH:
      case ExchangeAction.TRADING_WITH_PLAYER:
        switch(packet.charAt(2))
        {
          case 'O'://Objet ?
            if(packet.charAt(3)=='+')
            {
              for(String arg : packet.substring(4).split("\\+"))
              {
                String[] infos=arg.split("\\|");
                try
                {
                  int guid=Integer.parseInt(infos[0]);
                  int qua=Integer.parseInt(infos[1]);
                  int quaInExch=((PlayerExchange)this.player.getExchangeAction().getValue()).getQuaItem(guid,this.player.getId());

                  if(!this.player.hasItemGuid(guid))
                    return;
                  GameObject obj=World.getGameObject(guid);
                  if(obj==null)
                    return;

                  if(qua>obj.getQuantity()-quaInExch)
                    qua=obj.getQuantity()-quaInExch;

                  if(qua<=0||obj.isAttach())
                    return;

                  ((PlayerExchange)this.player.getExchangeAction().getValue()).addItem(guid,qua,this.player.getId());
                }
                catch(NumberFormatException e)
                {
                  this.player.sendMessage("Error: PlayerExchange : "+packet+"\n"+e.getMessage());
                  e.printStackTrace();
                  return;
                }
              }
            }
            else
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {
                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);

                if(qua<=0)
                  return;
                if(!this.player.hasItemGuid(guid))
                  return;

                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;
                if(qua>((PlayerExchange)this.player.getExchangeAction().getValue()).getQuaItem(guid,this.player.getId()))
                  return;

                ((PlayerExchange)this.player.getExchangeAction().getValue()).removeItem(guid,qua,this.player.getId());
              }
              catch(NumberFormatException e)
              {
                this.player.sendMessage("Error: PlayerExchange : "+packet+"\n"+e.getMessage());
               // e.printStackTrace();
                return;
              }
            }
            break;
          case 'G'://Kamas
            try
            {
              if(packet.substring(3).contains("NaN"))
                return;
              long numb=Integer.parseInt(packet.substring(3));
              if(this.player.getKamas()<numb)
                numb=this.player.getKamas();
              if(numb<0)
                return;
              ((PlayerExchange)this.player.getExchangeAction().getValue()).setKamas(this.player.getId(),numb);
            }
            catch(NumberFormatException e)
            {
              Main.world.logger.error("Error Echange PvP '"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }
            break;
        }
        break;

      case ExchangeAction.STAKE:
        switch(packet.charAt(2))
        {
          case 'O'://Objet ?
            if(packet.charAt(3)=='+')
            {
              for(String arg : packet.substring(4).split("\\+"))
              {
                String[] infos=arg.split("\\|");
                try
                {
                  int guid=Integer.parseInt(infos[0]);
                  int qua=Integer.parseInt(infos[1]);
                  int quaInExch=((Stake)this.player.getExchangeAction().getValue()).getQuaItem(guid,this.player.getId());

                  if(!this.player.hasItemGuid(guid))
                    return;
                  GameObject obj=World.getGameObject(guid);
                  if(obj==null)
                    return;

                  if(qua>obj.getQuantity()-quaInExch)
                    qua=obj.getQuantity()-quaInExch;

                  if(qua<=0||obj.isAttach())
                    return;

                  ((Stake)this.player.getExchangeAction().getValue()).addItem(guid,qua,this.player.getId());
                }
                catch(NumberFormatException e)
                {
                  this.player.sendMessage("Error: Stake : "+packet+"\n"+e.getMessage());
                  e.printStackTrace();
                  return;
                }
              }
            }
            else
            {
              String[] infos=packet.substring(4).split("\\|");
              try
              {
                int guid=Integer.parseInt(infos[0]);
                int qua=Integer.parseInt(infos[1]);

                if(qua<=0)
                  return;
                if(!this.player.hasItemGuid(guid))
                  return;

                GameObject obj=World.getGameObject(guid);
                if(obj==null)
                  return;
                if(qua>((Stake)this.player.getExchangeAction().getValue()).getQuaItem(guid,this.player.getId()))
                  return;

                ((Stake)this.player.getExchangeAction().getValue()).removeItem(guid,qua,this.player.getId());
              }
              catch(NumberFormatException e)
              {
                this.player.sendMessage("Error: Stake : "+packet+"\n"+e.getMessage());
                e.printStackTrace();
                return;
              }
            }
            break;
          case 'G'://Kamas
            try
            {
              if(packet.substring(3).contains("NaN"))
                return;
              long numb=Integer.parseInt(packet.substring(3));
              if(this.player.getKamas()<numb)
                numb=this.player.getKamas();
              if(numb<0)
                return;
              ((Stake)this.player.getExchangeAction().getValue()).setKamas(this.player.getId(),numb);
            }
            catch(NumberFormatException e)
            {
              Main.world.logger.error("Error Stake'"+packet+"' => "+e.getMessage());
              e.printStackTrace();
              return;
            }
            break;
        }
        break;
    }
  }

  private synchronized void movementItemOrKamasDons(String packet)
  {
    if(this.player.getExchangeAction()!=null&&this.player.getExchangeAction().getType()==ExchangeAction.CRAFTING_SECURE_WITH)
    {
      if(((CraftSecure)this.player.getExchangeAction().getValue()).getNeeder()==this.player)
      {
        byte type=Byte.parseByte(String.valueOf(packet.charAt(0)));
        switch(packet.charAt(1))
        {
          case 'O':
            String[] split=packet.substring(3).split("\\|");
            boolean adding=packet.charAt(2)=='+';
            int guid=Integer.parseInt(split[0]),quantity=Integer.parseInt(split[1]);

            ((CraftSecure)this.player.getExchangeAction().getValue()).setPayItems(type,adding,guid,quantity);
            break;
          case 'G':
            ((CraftSecure)this.player.getExchangeAction().getValue()).setPayKamas(type,Integer.parseInt(packet.substring(2)));
            break;
        }
      }
    }
  }

  private void askOfflineExchange()
  {
    if(this.player.getExchangeAction()!=null||this.player.getFight()!=null||this.player.isAway())
      return;
    if(this.player.parseStoreItemsList().isEmpty())
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"123");
      return;
    }
    if(Capture.isInArenaMap(this.player.getCurMap().getId())||this.player.getCurMap().noMarchand)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"113");
      return;
    }
    if(this.player.getCurMap().getId()==33||this.player.getCurMap().getId()==38||this.player.getCurMap().getId()==4601||this.player.getCurMap().getId()==4259||this.player.getCurMap().getId()==8036||this.player.getCurMap().getId()==10301)
    {
      if(this.player.getCurMap().getStoreCount()>=25)
      {
        SocketManager.GAME_SEND_Im_PACKET(this.player,"125;25");
        return;
      }
    }
    else if(this.player.getCurMap().getStoreCount()>=6)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"125;6");
      return;
    }
    for(Map.Entry<Integer, Integer> entry : this.player.getStoreItems().entrySet())
    {
      if(entry.getValue()<=0)
      {
        this.disconnect();
        return;
      }
    }

    long taxe=this.player.storeAllBuy()/1000;

    if(taxe<0)
    {
      this.disconnect();
      return;
    }

    SocketManager.GAME_SEND_Eq_PACKET(this.player,taxe);
  }

  private void offlineExchange()
  {
    if(Capture.isInArenaMap(this.player.getCurMap().getId())||this.player.getCurMap().noMarchand)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"113");
      return;
    }
    if(this.player.getCurMap().getId()==33||this.player.getCurMap().getId()==38||this.player.getCurMap().getId()==4601||this.player.getCurMap().getId()==4259||this.player.getCurMap().getId()==8036||this.player.getCurMap().getId()==10301)
    {
      if(this.player.getCurMap().getStoreCount()>=25)
      {
        SocketManager.GAME_SEND_Im_PACKET(this.player,"125;25");
        return;
      }
    }
    else if(this.player.getCurMap().getStoreCount()>=6)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"125;6");
      return;
    }
    long taxe=this.player.storeAllBuy()/1000;
    if(this.player.getKamas()<taxe)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"176");
      return;
    }
    if(taxe<0)
    {
      SocketManager.GAME_SEND_MESSAGE(this.player,"Erreur en mode marchand: la taxe est négative.");
      return;
    }
    int orientation=Formulas.getRandomValue(1,3);
    this.player.setKamas(this.player.getKamas()-taxe);
    this.player.set_orientation(orientation);
    GameMap map=this.player.getCurMap();
    this.player.setShowSeller(true);
    Main.world.addSeller(this.player);
    this.disconnect();
    map.getPlayers().stream().filter(player -> player!=null&&player.isOnline()).forEach(player -> SocketManager.GAME_SEND_MERCHANT_LIST(player,player.getCurMap().getId()));
  }

  private synchronized void putInInventory(String packet)
  {
    if(this.player.getExchangeAction()!=null&&this.player.getExchangeAction().getType()==ExchangeAction.IN_MOUNTPARK)
    {
      int id=-1;
      MountPark park=this.player.getCurMap().getMountPark();

      try
      {
        id=Integer.parseInt(packet.substring(3));
      }
      catch(Exception ignored)
      {
      }

      switch(packet.charAt(2))
      {
        case 'C':// Certificats -> Etable
          if(id==-1||!this.player.hasItemGuid(id))
            return;
          if(park.hasEtableFull(this.player.getId()))
          {
            this.player.send("Im1105");
            return;
          }

          GameObject object=World.getGameObject(id);
          Mount mount=Main.world.getMountById(-object.getStats().getEffect(995));

          if(mount==null)
            return;

          mount.setOwner(this.player.getId());
          this.player.removeItem(id);
          Main.world.removeGameObject(id);

          if(!park.getEtable().contains(mount))
            park.getEtable().add(mount);

          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,object.getGuid());

          Database.getStatics().getMountData().update(mount);
          Database.getStatics().getPlayerData().update(this.player);
          SocketManager.GAME_SEND_Ee_PACKET(this.player,mount.getSize()==50 ? '~' : '+',mount.parse());
          break;

        case 'c':// Etable -> Certificats
          mount=Main.world.getMountById(id);

          if(!park.getEtable().contains(mount)||mount==null)
            return;

          park.getEtable().remove(mount);
          mount.setOwner(this.player.getId());

          object=Constant.getParchoTemplateByMountColor(mount.getColor()).createNewItem(1,false);
          object.setMountStats(this.player,mount);

          World.addGameObject(object,true);
          this.player.addObjet(object);

          SocketManager.GAME_SEND_Ee_PACKET(this.player,'-',mount.getId()+"");
          Database.getStatics().getMountData().update(mount);
          Database.getStatics().getPlayerData().update(this.player);
          break;

        case 'g':// Equiper une dinde
          mount=Main.world.getMountById(id);

          if(!park.getEtable().contains(mount)||mount==null)
          {
            SocketManager.GAME_SEND_Im_PACKET(this.player,"1104");
            return;
          }
          if(this.player.getMount()!=null)
          {
            SocketManager.GAME_SEND_BN(this);
            return;
          }
          if(mount.getFecundatedDate()!=-1)
          {
            SocketManager.GAME_SEND_BN(this);
            return;
          }

          mount.setOwner(this.player.getId());
          park.getEtable().remove(mount);
          this.player.setMount(mount);

          SocketManager.GAME_SEND_Re_PACKET(this.player,"+",mount);
          SocketManager.GAME_SEND_Ee_PACKET(this.player,'-',mount.getId()+"");
          SocketManager.GAME_SEND_Rx_PACKET(this.player);
          Database.getStatics().getMountData().update(mount);
         Database.getStatics().getPlayerData().update(this.player);
          break;

        case 'p':// Equipe -> Etable
          if(this.player.getMount()!=null&&this.player.getMount().getId()==id)
          {
            if(park.hasEtableFull(this.player.getId()))
            {
              this.player.send("Im1105");
              return;
            }

            mount=this.player.getMount();
            if(mount.getObjects().size()==0)
            {
              if(this.player.isOnMount())
                this.player.toogleOnMount();

              if(!park.getEtable().contains(mount))
                park.getEtable().add(mount);

              mount.setOwner(this.player.getId());
              this.player.setMount(null);

              Database.getStatics().getMountData().update(mount);
              SocketManager.GAME_SEND_Ee_PACKET(this.player,mount.getSize()==50 ? '~' : '+',mount.parse());
              SocketManager.GAME_SEND_Re_PACKET(this.player,"-",null);
              SocketManager.GAME_SEND_Rx_PACKET(this.player);
            }
            else
            {
              SocketManager.GAME_SEND_Im_PACKET(this.player,"1106");
            }
            Database.getStatics().getMountData().update(mount);
           Database.getStatics().getPlayerData().update(this.player);
          }
          break;
      }
      Database.getStatics().getMountParkData().update(park);
    }
  }

  private synchronized void putInMountPark(String packet)
  {
    if(this.player.getExchangeAction()!=null&&this.player.getExchangeAction().getType()==ExchangeAction.IN_MOUNTPARK)
    {
      int id=-1;
      MountPark park=this.player.getCurMap().getMountPark();
      try
      {
        id=Integer.parseInt(packet.substring(3));
      }
      catch(Exception ignored)
      {
      }

      switch(packet.charAt(2))
      {
        case 'g':// Enclos -> Etable
          if(park.hasEtableFull(this.player.getId()))
          {
            this.player.send("Im1105");
            return;
          }

          Mount mount=Main.world.getMountById(id);
          if(!park.getEtable().contains(mount))
            park.getEtable().add(mount);
          park.delRaising(mount.getId());

          mount.setOwner(this.player.getId());
          this.player.getCurMap().getMountPark().delRaising(id);
          SocketManager.GAME_SEND_Ef_MOUNT_TO_ETABLE(this.player,'-',mount.getId()+"");

          SocketManager.GAME_SEND_Ee_PACKET(this.player,mount.getSize()==50 ? '~' : '+',mount.parse());
          SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(),id);
          mount.setMapId((short)-1);
          mount.setCellId(-1);

          Database.getStatics().getMountData().update(mount);
          Database.getStatics().getPlayerData().update(this.player);
          break;

        case 'p':// Etable -> Enclos
          if(this.player.getMount()!=null)
          {
            if(this.player.getMount().getObjects().size()!=0)
            {
              SocketManager.GAME_SEND_Im_PACKET(this.player,"1106");
              return;
            }
          }

          if(park.hasEnclosFull(this.player.getId()))
          {
            this.player.send("Im1107");
            return;
          }

          if(this.player.getMount()!=null&&this.player.getMount().getId()==id)
          {
            if(this.player.isOnMount())
              this.player.toogleOnMount();
            if(this.player.isOnMount())
              return;
            this.player.setMount(null);
          }

          mount=Main.world.getMountById(id);
          mount.setOwner(this.player.getId());
          mount.setMapId(park.getMap().getId());
          mount.setCellId(park.getPlaceOfSpawn());
          park.getEtable().remove(mount);
          park.addRaising(id);
          SocketManager.GAME_SEND_Ef_MOUNT_TO_ETABLE(this.player,'+',mount.parse());
          SocketManager.GAME_SEND_Ee_PACKET(this.player,'-',mount.getId()+"");
          SocketManager.GAME_SEND_GM_MOUNT_TO_MAP(park.getMap(),mount);

          Database.getStatics().getMountData().update(mount);
          Database.getStatics().getPlayerData().update(this.player);
          break;
      }
      Database.getStatics().getMountParkData().update(park);
    }
  }

  //v2.8 - World Market
  private void request(String packet)
  {
    if(this.player.getExchangeAction()!=null&&this.player.getExchangeAction().getType()!=ExchangeAction.AUCTION_HOUSE_BUYING&&this.player.getExchangeAction().getType()!=ExchangeAction.AUCTION_HOUSE_SELLING)
    {
      SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'O');
      return;
    }
    if(packet.substring(2,4).equals("25")&&this.player.getExchangeAction()==null)
    {
      try
      {
        String[] split=packet.split("\\|");
        int id=Integer.parseInt(split[1]);
        Player target=Main.world.getPlayer(id);

        if(target==null||target.getCurMap()!=this.player.getCurMap()||!target.isOnline()||player.getStake()!=null||target.getStake()!=null)
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'E');
          return;
        }
        if(target.isAway()||this.player.isAway()||target.getExchangeAction()!=null||this.player.getExchangeAction()!=null)
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'O');
          return;
        }
        if(target.getGroupe()!=null&&this.player.getGroupe()==null)
        {
          if(!target.getGroupe().isPlayer())
          {
            SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'E');
            return;
          }
        }
        ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.STAKE,id);
        this.player.setExchangeAction(exchangeAction);
        exchangeAction=new ExchangeAction<>(ExchangeAction.STAKE,this.player.getId());
        target.setExchangeAction(exchangeAction);

        this.player.getIsCraftingType().add(1);
        target.getIsCraftingType().add(1);

        SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(this,this.player.getId(),id,25);
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(target.getGameClient(),this.player.getId(),id,25);
      }
      catch(NumberFormatException e)
      {
        e.printStackTrace();
      }
    }
    else if(packet.substring(2,4).equals("13")&&this.player.getExchangeAction()==null)
    { // Craft sà¯Â¿Â½curisà¯Â¿Â½ : celui qui n'a pas le job ( this.player ) souhaite invità¯Â¿Â½ player
      try
      {
        String[] split=packet.split("\\|");
        int id=Integer.parseInt(split[1]);
        int skill=Integer.parseInt(split[2]);

        Player player=Main.world.getPlayer(id);

        if(player==null)
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'E');
          return;
        }
        if(player.getCurMap()!=this.player.getCurMap()||!player.isOnline())
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'E');
          return;
        }
        if(player.isAway()||this.player.isAway())
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'O');
          return;
        }

        ArrayList<Job> jobs=player.getJobs();

        if(jobs==null)
          return;

        GameObject object=player.getObjetByPos(Constant.ITEM_POS_ARME);

        if(object==null)
        {
          this.player.send("BN");
          return;
        }
        boolean ok=false;

        for(Job job : jobs)
        {
          if(job.getSkills().isEmpty())
            continue;
          if(!job.isValidTool(object.getTemplate().getId()))
            continue;

          for(GameCase cell : this.player.getCurMap().getCases())
          {
            if(cell.getObject()!=null)
            {
              if(cell.getObject().getTemplate()!=null)
              {
                int io=cell.getObject().getTemplate().getId();
                ArrayList<Integer> skills=job.getSkills().get(io);

                if(skills!=null)
                {
                  for(int arg : skills)
                  {
                    if(arg==skill&&PathFinding.getDistanceBetween(player.getCurMap(),player.getCurCell().getId(),cell.getId())<4)
                    {
                      ok=true;
                      break;
                    }
                  }
                }
              }
            }
          }

          if(ok)
            break;
        }

        if(!ok)
        {
          this.player.send("ERET");
          return;
        }

        ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.CRAFTING_SECURE_WITH,id);
        this.player.setExchangeAction(exchangeAction);
        ExchangeAction<Integer> exchangeAction1=new ExchangeAction<>(ExchangeAction.CRAFTING_SECURE_WITH,this.player.getId());
        player.setExchangeAction(exchangeAction1);

        this.player.getIsCraftingType().add(13);
        player.getIsCraftingType().add(12);
        this.player.getIsCraftingType().add(skill);
        player.getIsCraftingType().add(skill);

        SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(this,this.player.getId(),id,12);
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(player.getGameClient(),this.player.getId(),id,12);
      }
      catch(NumberFormatException e)
      {
        e.printStackTrace();
      }
      return;
    }
    else if(packet.substring(2,4).equals("12")&&this.player.getExchangeAction()==null)
    { // Craft sà¯Â¿Â½curisà¯Â¿Â½ : celui qui à¯Â¿Â½ le job ( this.player ) souhaite invità¯Â¿Â½ player
      try
      {
        String[] split=packet.split("\\|");
        int id=Integer.parseInt(split[1]);
        int skill=Integer.parseInt(split[2]);

        Player player=Main.world.getPlayer(id);

        if(player==null)
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'E');
          return;
        }
        if(player.getCurMap()!=this.player.getCurMap()||!player.isOnline())
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'E');
          return;
        }
        if(player.isAway()||this.player.isAway())
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'O');
          return;
        }

        ArrayList<Job> jobs=this.player.getJobs();
        if(jobs==null)
          return;

        GameObject object=this.player.getObjetByPos(Constant.ITEM_POS_ARME);
        if(object==null)
          return;

        boolean ok=false;

        for(Job job : jobs)
        {
          if(job.getSkills().isEmpty()||!job.isValidTool(object.getTemplate().getId()))
            continue;
          for(GameCase cell : this.player.getCurMap().getCases())
          {
            if(cell.getObject()!=null)
            {
              if(cell.getObject().getTemplate()!=null)
              {
                int io=cell.getObject().getTemplate().getId();
                ArrayList<Integer> skills=job.getSkills().get(io);

                if(skills!=null)
                {
                  for(int arg : skills)
                  {
                    if(arg==skill&&PathFinding.getDistanceBetween(this.player.getCurMap(),this.player.getCurCell().getId(),cell.getId())<4)
                    {
                      ok=true;
                      break;
                    }
                  }
                }
              }
            }
          }
          if(ok)
            break;
        }

        if(!ok)
        {
          this.player.sendMessage("Vous étes trop loin de l'atelier.");
          return;
        }

        ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.CRAFTING_SECURE_WITH,id);
        this.player.setExchangeAction(exchangeAction);
        exchangeAction=new ExchangeAction<>(ExchangeAction.CRAFTING_SECURE_WITH,this.player.getId());
        player.setExchangeAction(exchangeAction);

        this.player.getIsCraftingType().add(12);
        player.getIsCraftingType().add(13);
        this.player.getIsCraftingType().add(skill);
        player.getIsCraftingType().add(skill);

        SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(this,this.player.getId(),id,12);
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(player.getGameClient(),this.player.getId(),id,13);
      }
      catch(NumberFormatException e)
      {
        e.printStackTrace();
      }
      return;
    }
    else if(packet.substring(2,4).equals("11"))
    { //Ouverture HDV achat
    	boolean hdvs = false ;
    	if(Main.world.getHdv(this.player.getCurMap().getId()) != null)
    		hdvs = true;
      if(this.player.getExchangeAction()!=null) {
       // if(player.getWorldMarket())
       // {
          leaveExchange(this.player);
          player.setWorldMarket(true);
      }
       // }
       // else
       //   leaveExchange(this.player);
      if(this.player.getDeshonor()>=5)
      {
        SocketManager.GAME_SEND_Im_PACKET(this.player,"183");
        return;
      }
      Hdv hdv=null;
      //if(player.getWorldMarket())
        
    ///  else
     //   hdv=Main.world.getHdv(this.player.getCurMap().getId());
        if(hdvs) 
        	hdv=Main.world.getHdv(this.player.getCurMap().getId());
        else
        	hdv=Main.world.getWorldMarket();
      if(hdv!=null)
      {
        String info="1,10,100;"+hdv.getStrCategory()+";"+hdv.parseTaxe()+";"+hdv.getLvlMax()+";"+hdv.getMaxAccountItem()+";-1;"+hdv.getSellTime();
        SocketManager.GAME_SEND_ECK_PACKET(this.player,11,info);
        ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.AUCTION_HOUSE_BUYING,-this.player.getCurMap().getId()); //Rà¯Â¿Â½cupà¯Â¿Â½re l'ID de la map et rend cette valeur nà¯Â¿Â½gative
        this.player.setExchangeAction(exchangeAction);
      }
      return;
    }
    else if(packet.substring(2,4).equals("15")&&this.player.getExchangeAction()==null)
    {
      Mount mount=this.player.getMount();

      if(mount!=null)
      {
        ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.IN_MOUNT,mount.getId());
        this.player.setExchangeAction(exchangeAction);

        SocketManager.GAME_SEND_ECK_PACKET(this,15,String.valueOf(mount.getId()));
        SocketManager.GAME_SEND_EL_MOUNT_PACKET(this.player,mount);
        SocketManager.GAME_SEND_Ew_PACKET(this.player,mount.getActualPods(),mount.getMaxPods());
      }
      return;
    }
    else if(packet.substring(2,4).equals("17")&&this.player.getExchangeAction()==null)
    {//Ressurection famillier
      int id=Integer.parseInt(packet.substring(5));

      if(this.player.getCurMap().getNpc(id)!=null)
      {
        NpcRessurectPets ech=new NpcRessurectPets(this.player,this.player.getCurMap().getNpc(id).getTemplate());
        ExchangeAction<NpcRessurectPets> exchangeAction=new ExchangeAction<>(ExchangeAction.TRADING_WITH_NPC_PETS_RESURRECTION,ech);
        this.player.setExchangeAction(exchangeAction);
        SocketManager.GAME_SEND_ECK_PACKET(this.player,9,String.valueOf(id));
      }
    }
    else if(packet.substring(2,4).equals("10"))
    { //Ouverture HDV vente
    	boolean hdvs = false;
    	if(Main.world.getHdv(this.player.getCurMap().getId()) != null)
    		hdvs = true;
      if(this.player.getExchangeAction()!=null) {
       // if(player.getWorldMarket())
       // {
          leaveExchange(this.player);
          player.setWorldMarket(true);
      }
       // }
        //else
          //leaveExchange(this.player);
      if(this.player.getDeshonor()>=5)
      {
        SocketManager.GAME_SEND_Im_PACKET(this.player,"183");
        return;
      }
      Hdv hdv=null;
     // if(player.getWorldMarket())
      if(hdvs) 
    	  hdv=Main.world.getHdv(this.player.getCurMap().getId());
      else
        hdv=Main.world.getWorldMarket();
      //else
       // hdv=Main.world.getHdv(this.player.getCurMap().getId());
      if(hdv!=null)
      {
        String infos="1,10,100;"+hdv.getStrCategory()+";"+hdv.parseTaxe()+";"+hdv.getLvlMax()+";"+hdv.getMaxAccountItem()+";-1;"+hdv.getSellTime();
        SocketManager.GAME_SEND_ECK_PACKET(this.player,10,infos);
        ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.AUCTION_HOUSE_SELLING,-Main.world.changeHdv(this.player.getCurMap().getId()));
        this.player.setExchangeAction(exchangeAction);
        SocketManager.GAME_SEND_HDVITEM_SELLING(this.player);
      }
      return;
    }
    if(this.player.getExchangeAction()!=null)
    {
      SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'O');
      return;
    }
    switch(packet.charAt(2))
    {
      case '0'://Si NPC
        int id=Integer.parseInt(packet.substring(4));
        Npc npc=this.player.getCurMap().getNpc(id);

        if(npc!=null)
        {
          ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.TRADING_WITH_NPC,id);
          this.player.setExchangeAction(exchangeAction);

          SocketManager.GAME_SEND_ECK_PACKET(this,0,String.valueOf(id));
          SocketManager.GAME_SEND_ITEM_VENDOR_LIST_PACKET(this,npc);
        }
        break;
      case '1'://Si joueur
        try
        {
          id=Integer.parseInt(packet.substring(4));
          Player target=Main.world.getPlayer(id);

          if(target==null||target.getCurMap()!=this.player.getCurMap()||!target.isOnline())
          {
            SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'E');
            return;
          }
          if(target.isAway()||this.player.isAway()||target.getExchangeAction()!=null||this.player.getExchangeAction()!=null)
          {
            SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'O');
            return;
          }
          if(target.getGroupe()!=null&&this.player.getGroupe()==null)
          {
            if(!target.getGroupe().isPlayer())
            {
              SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'E');
              return;
            }
          }
          ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.TRADING_WITH_PLAYER,id);
          this.player.setExchangeAction(exchangeAction);
          exchangeAction=new ExchangeAction<>(ExchangeAction.TRADING_WITH_PLAYER,this.player.getId());
          target.setExchangeAction(exchangeAction);

          this.player.getIsCraftingType().add(1);
          target.getIsCraftingType().add(1);

          SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(this,this.player.getId(),id,1);
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(target.getGameClient(),this.player.getId(),id,1);
        }
        catch(NumberFormatException e)
        {
          e.printStackTrace();
        }
        break;
      case '2'://Npc Exchange
        id=Integer.parseInt(packet.substring(4));
        if(this.player.getCurMap().getNpc(id)!=null)
        {
          NpcExchange ech=new NpcExchange(this.player,this.player.getCurMap().getNpc(id).getTemplate());

          ExchangeAction<NpcExchange> exchangeAction=new ExchangeAction<>(ExchangeAction.TRADING_WITH_NPC_EXCHANGE,ech);
          this.player.setExchangeAction(exchangeAction);
          SocketManager.GAME_SEND_ECK_PACKET(this.player,2,String.valueOf(id));
        }
        break;
      case '4'://StorePlayer
        id=Integer.valueOf(packet.split("\\|")[1]);

        Player seller=Main.world.getPlayer(id);
        if(seller==null||!seller.isShowSeller()||seller.getCurMap()!=this.player.getCurMap())
          return;

        ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.TRADING_WITH_OFFLINE_PLAYER,id);
        this.player.setExchangeAction(exchangeAction);

        SocketManager.GAME_SEND_ECK_PACKET(this.player,4,String.valueOf(seller.getId()));
        SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(seller,this.player);
        break;
      case '6'://StoreItems
        exchangeAction=new ExchangeAction<>(ExchangeAction.TRADING_WITH_ME,this.player.getId());
        this.player.setExchangeAction(exchangeAction);

        SocketManager.GAME_SEND_ECK_PACKET(this.player,6,"");
        SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this.player,this.player);
        break;
      case '8'://Si Collector
        Collector collector=Main.world.getCollector(Integer.parseInt(packet.substring(4)));
        if(collector==null||collector.getInFight()>0||collector.getExchange()||collector.getGuildId()!=this.player.get_guild().getId()||collector.getMap()!=this.player.getCurMap().getId())
          return;
        if(!this.player.getGuildMember().canDo(Constant.G_COLLPERCO))
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"1101");
          return;
        }
        collector.setExchange(true);
        exchangeAction=new ExchangeAction<>(ExchangeAction.TRADING_WITH_COLLECTOR,collector.getId());
        this.player.setExchangeAction(exchangeAction);
        this.player.DialogTimer();

        SocketManager.GAME_SEND_ECK_PACKET(this,8,String.valueOf(collector.getId()));
        SocketManager.GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(this,collector);
        break;
      case '9'://Dà¯Â¿Â½posà¯Â¿Â½/Retirà¯Â¿Â½ un familier
        id=Integer.parseInt(packet.substring(4));

        if(this.player.getCurMap().getNpc(id)!=null)
        {
          NpcExchangePets ech=new NpcExchangePets(this.player,this.player.getCurMap().getNpc(id).getTemplate());
          this.player.setExchangeAction(new ExchangeAction<>(ExchangeAction.TRADING_WITH_NPC_PETS,ech));
          SocketManager.GAME_SEND_ECK_PACKET(this.player,9,String.valueOf(id));
        }
        break;
    }
  }

  //v2.8 - Fixed sellAll button
  private void sell(String packet)
  {
    try
    {
      String[] sellAllSplit=packet.substring(2).split(";");
      for(String s : sellAllSplit)
      {
        String[] infos=s.split("\\|");
        int id=Integer.parseInt(infos[0]),quantity=Integer.parseInt(infos[1]);

        if(!this.player.hasItemGuid(id))
        {
          SocketManager.GAME_SEND_SELL_ERROR_PACKET(this);
          return;
        }

        this.player.sellItem(id,quantity);
      }
    }
    catch(Exception e)
    {
      SocketManager.GAME_SEND_SELL_ERROR_PACKET(this);
    }
  }

  private void bookOfArtisant(String packet)
  {
    switch(packet.charAt(2))
    {
      case 'F':
        int Metier=Integer.parseInt(packet.substring(3));
        int cant=0;
        for(Player artissant : Main.world.getOnlinePlayers())
        {
          if(artissant.getMetiers().isEmpty())
            continue;
          String send="";
          int id=artissant.getId();
          String name=artissant.getName();
          String color=artissant.getColor1()+","+artissant.getColor2()+","+artissant.getColor3();
          String accesoire=artissant.getGMStuffString();
          int sex=artissant.getSexe();
          int map=artissant.getCurMap().getId();
          int inJob=(map==8731||map==8732) ? 1 : 0;
          int classe=artissant.getClasse();
          for(JobStat SM : artissant.getMetiers().values())
          {
            if(SM.getTemplate().getId()!=Metier)
              continue;
            cant++;
            send="+"+SM.getTemplate().getId()+";"+id+";"+name+";"+SM.get_lvl()+";"+map+";"+inJob+";"+classe+";"+sex+";"+color+";"+accesoire+";"+SM.getOptBinValue()+","+SM.getSlotsPublic();
            SocketManager.SEND_EJ_LIVRE(this.player,send);
          }
        }
        if(cant==0)
          SocketManager.GAME_SEND_MESSAGE(this.player,"Il n'y a actuellement aucun artisan disponible dans la profession que vous recherchez.");
        break;
    }
  }

  //v2.7 - Replaced String += with StringBuilder
  private void setPublicMode(String packet)
  {
    switch(packet.charAt(2))
    {
      case '+':
        this.player.setMetierPublic(true);
        StringBuilder metier=new StringBuilder();
        boolean first=false;
        for(JobStat SM : this.player.getMetiers().values())
        {
          SocketManager.SEND_Ej_LIVRE(this.player,"+"+SM.getTemplate().getId());
          if(first)
            metier.append(";");
          metier.append(JobConstant.actionMetier(SM.getTemplate().getId()));
          first=true;
        }
        SocketManager.SEND_EW_METIER_PUBLIC(this.player,"+");
        SocketManager.SEND_EW_METIER_PUBLIC(this.player,"+"+this.player.getId()+"|"+metier.toString());
        break;

      case '-':
        this.player.setMetierPublic(false);
        for(JobStat metiers : this.player.getMetiers().values())
        {
          SocketManager.SEND_Ej_LIVRE(this.player,"-"+metiers.getTemplate().getId());
        }
        SocketManager.SEND_EW_METIER_PUBLIC(this.player,"-");
        SocketManager.SEND_EW_METIER_PUBLIC(this.player,"-"+this.player.getId());
        break;
    }
  }

  public static void leaveExchange(Player player)
  {
    if(player.boutique)
    {
      player.boutique=false;
      player.send("EV");
      player.setExchangeAction(null);
     SocketManager.GAME_SEND_STATS_PACKET(player);
      return;
    }
    if(player.tokenShop)
    {
      player.tokenShop=false;
      player.send("EV");
      player.setExchangeAction(null);
      return;
    }
    if(player.getWorldMarket())
    {
      player.setWorldMarket(false);
      player.send("EV");
      player.setExchangeAction(null);
      return;
    }

    if(player.getExchangeAction()!=null)
    {
      ExchangeAction<?> exchangeAction=player.getExchangeAction();
      switch(exchangeAction.getType())
      {
        case ExchangeAction.TRADING_WITH_PLAYER:
          if(exchangeAction.getValue() instanceof Integer)
          {
            Player target=Main.world.getPlayer((Integer)exchangeAction.getValue());
            if(target!=null&&target.getExchangeAction()!=null&&target.getExchangeAction().getType()==ExchangeAction.TRADING_WITH_PLAYER)
            {
              target.send("EV");
              target.setExchangeAction(null);
            }
          }
          else
          {
            ((PlayerExchange)exchangeAction.getValue()).cancel();
          }
          break;
        case ExchangeAction.STAKE:
          if(exchangeAction.getValue() instanceof Integer)
          {
            Player target=Main.world.getPlayer((Integer)exchangeAction.getValue());
            target.setExchangeAction(null);
            player.setExchangeAction(null);
          }
          else
            ((Stake)exchangeAction.getValue()).cancel();
          break;
        case ExchangeAction.TRADING_WITH_NPC_PETS:
          ((NpcExchangePets)exchangeAction.getValue()).cancel();
          break;
        case ExchangeAction.TRADING_WITH_NPC_EXCHANGE:
          ((NpcExchange)exchangeAction.getValue()).cancel();
          break;
        case ExchangeAction.CRAFTING_SECURE_WITH:
          if(exchangeAction.getValue() instanceof Integer)
          {
            Player target=Main.world.getPlayer((Integer)exchangeAction.getValue());
            if(target!=null&&target.getExchangeAction()!=null&&target.getExchangeAction().getType()==ExchangeAction.CRAFTING_SECURE_WITH)
            {
              target.send("EV");
              target.setExchangeAction(null);
            }
          }
          else
          {
            ((CraftSecure)exchangeAction.getValue()).cancel();
          }
          break;
        case ExchangeAction.CRAFTING:
          player.send("EV");
          player.setDoAction(false);
          ((JobAction)exchangeAction.getValue()).resetCraft();
          break;

        case ExchangeAction.BREAKING_OBJECTS:
          ((BreakingObject)exchangeAction.getValue()).setStop(true);
          player.send("EV");
          break;
        case ExchangeAction.TRADING_WITH_NPC:
        case ExchangeAction.IN_MOUNT:
          player.send("EV");
          SocketManager.GAME_SEND_STATS_PACKET(player);
          break;
        case ExchangeAction.IN_MOUNTPARK:
          player.send("EV");
          ArrayList<GameObject> objects=new ArrayList<>();
          for(GameObject object : player.getItems().values())
          {
            Mount mount=Main.world.getMountById(-object.getStats().getEffect(995));

            if(mount==null&&object.getTemplate().getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
              objects.add(object);
          }
          for(GameObject object : objects)
            player.removeItem(object.getGuid(),object.getQuantity(),true,true);
          break;

        case ExchangeAction.IN_TRUNK:
          ((Trunk)exchangeAction.getValue()).setPlayer(null);
          player.send("EV");
          break;

        case ExchangeAction.TRADING_WITH_COLLECTOR:
          Collector collector=Main.world.getCollector((Integer)exchangeAction.getValue());
          if(collector==null)
            return;
          for(Player loc : Main.world.getGuild(collector.getGuildId()).getOnlineMembers())
          {
            if(!loc.isOnline())
              loc.setOnline(true);
            SocketManager.GAME_SEND_gITM_PACKET(loc,soufix.entity.Collector.parseToGuild(loc.get_guild().getId()));
            String str="";
            str+="G"+Integer.toString(collector.getN1(),36)+","+Integer.toString(collector.getN2(),36);
            str+="|.|"+Main.world.getMap(collector.getMap()).getX()+"|"+Main.world.getMap(collector.getMap()).getY()+"|";
            str+=player.getName()+"|"+(collector.getXp());
            if(!collector.getLogObjects().equals(""))
              str+=collector.getLogObjects();
            SocketManager.GAME_SEND_gT_PACKET(loc,str);
          }
          player.getGuildMember().giveXpToGuild(collector.getXp());
          player.getCurMap().RemoveNpc(collector.getId());
          SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(),collector.getId());
          collector.reloadTimer();
          collector.delCollector(collector.getId());
          player.send("EV");
          Database.getDynamics().getCollectorData().delete(collector.getId());
          break;

        default:
          player.setLivreArtisant(false);
          player.send("EV");
          break;
      }

      player.setExchangeAction(null);
      Database.getStatics().getPlayerData().update(player);
    }
  }

  /** Fin Exchange Packet **/

  /**
   * Emote Packet *
   */
  private void parseEnvironementPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'D'://Change direction
        setDirection(packet);
        break;
      case 'U'://Emote
        useEmote(packet);
        break;
    }
  }

  private void setDirection(String packet)
  {
    try
    {
      if(this.player.getFight()!=null||this.player.isDead()==1)
        return;
      int dir=Integer.parseInt(packet.substring(2));
      if(dir>7||dir<0)
        return;
      this.player.set_orientation(dir);
      SocketManager.GAME_SEND_eD_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),dir);
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
  }

  private void useEmote(String packet)
  {
    final int emote=Integer.parseInt(packet.substring(2));
    if(emote==-1)
      return;
    if(this.player==null)
      return;
    if(this.player.getFight()!=null)
      return;//Pas d'à¯Â¿Â½mote en combat
    if(!this.player.getEmotes().contains(emote))
      return;
    if(emote!=1||emote!=19&&this.player.isSitted())
      this.player.setSitted(false);

    switch(emote)
    {
      case 20://Tabouret hà¯Â¿Â½hà¯Â¿Â½ ( rime )
      case 19://s'allonger
      case 1:// s'asseoir
        this.player.setSitted(!this.player.isSitted());
        break;
    }

    if(this.player.emoteActive()==1||this.player.emoteActive()==19||this.player.emoteActive()==21)
      this.player.setEmoteActive(0);
    else
      this.player.setEmoteActive(emote);

    MountPark MP=this.player.getCurMap().getMountPark();
    SocketManager.GAME_SEND_eUK_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),this.player.emoteActive());
    if((emote==2||emote==4||emote==3||emote==6||emote==8||emote==10)&&MP!=null)
    {
      final ArrayList<Mount> mounts=new ArrayList<>();
      for(Integer id : MP.getListOfRaising())
      {
        Mount mount=Main.world.getMountById(id);
        if(mount!=null)
          if(mount.getOwner()==this.player.getId())
            mounts.add(mount);
      }
      final Player player=this.player;
      if(mounts.isEmpty())
        return;
      final Mount mount=mounts.get(Formulas.getRandomValue(0,mounts.size()-1));
      if(mounts.size()>0)
      {
        int cells=0;
        switch(emote)
        {
          case 2:
          case 4:
            cells=1;
            break;

          case 3:
          case 8:
            cells=Formulas.getRandomValue(2,3);
            break;

          case 6:
          case 10:
            cells=Formulas.getRandomValue(4,7);
            break;
        }

        mount.moveMounts(player,cells,!(emote==2||emote==3||emote==10));
      }
    }
  }

  /** Fin Emote Packet **/

  /**
   * Friend Packet *
   */
  private void parseFrienDDacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'A'://Ajthiser
        addFriend(packet);
        break;
      case 'D'://Effacer un ami
        removeFriend(packet);
        break;
      case 'L'://Liste
        SocketManager.GAME_SEND_FRIENDLIST_PACKET(this.player);
        break;
      case 'O':
        switch(packet.charAt(2))
        {
          case '-':
            this.player.SetSeeFriendOnline(false);
            SocketManager.GAME_SEND_BN(this.player);
            break;
          case '+':
            this.player.SetSeeFriendOnline(true);
            SocketManager.GAME_SEND_BN(this.player);
            break;
        }
        break;
      case 'J': //Wife
        joinWife(packet);
        break;
    }
  }

  private void addFriend(String packet)
  {
    if(this.player==null)
      return;
    int guid=-1;
    switch(packet.charAt(2))
    {
      case '%'://Nom de this.player
        packet=packet.substring(3);
        Player P=Main.world.getPlayerByName(packet);
        if(P==null||!P.isOnline())//Si P est nul, ou si P est nonNul et P offline
        {
          SocketManager.GAME_SEND_FA_PACKET(this.player,"Ef");
          return;
        }
        guid=P.getAccID();
        break;
      case '*'://Pseudo
        packet=packet.substring(3);
        Account C=Main.world.getAccountByPseudo(packet);
        if(C==null||!C.isOnline())
        {
          SocketManager.GAME_SEND_FA_PACKET(this.player,"Ef");
          return;
        }
        guid=C.getId();
        break;
      default:
        packet=packet.substring(2);
        Player Pr=Main.world.getPlayerByName(packet);
        if(Pr==null||!Pr.isOnline())//Si P est nul, ou si P est nonNul et P offline
        {
          SocketManager.GAME_SEND_FA_PACKET(this.player,"Ef");
          return;
        }
        guid=Pr.getAccount().getId();
        break;
    }
    if(guid==-1)
    {
      SocketManager.GAME_SEND_FA_PACKET(this.player,"Ef");
      return;
    }
    account.addFriend(guid);
  }

  private void removeFriend(String packet)
  {
    if(this.player==null)
      return;
    int guid=-1;
    switch(packet.charAt(2))
    {
      case '%'://Nom de this.player
        packet=packet.substring(3);
        Player P=Main.world.getPlayerByName(packet);
        if(P==null)//Si P est nul, ou si P est nonNul et P offline
        {
          SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
          return;
        }
        guid=P.getAccID();
        break;
      case '*'://Pseudo
        packet=packet.substring(3);
        Account C=Main.world.getAccountByPseudo(packet);
        if(C==null)
        {
          SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
          return;
        }
        guid=C.getId();
        break;
      default:
        packet=packet.substring(2);
        Player Pr=Main.world.getPlayerByName(packet);
        if(Pr==null||!Pr.isOnline())//Si P est nul, ou si P est nonNul et P offline
        {
          SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
          return;
        }
        guid=Pr.getAccount().getId();
        break;
    }
    if(guid==-1||!account.isFriendWith(guid))
    {
      SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
      return;
    }
    account.removeFriend(guid);
  }

  private void joinWife(String packet)
  {
    Player Wife=Main.world.getPlayer(this.player.getWife());
    if(Wife==null)
      return;
    if(!Wife.isOnline())
    {
      if(Wife.getSexe()==0)
        SocketManager.GAME_SEND_Im_PACKET(this.player,"140");
      else
        SocketManager.GAME_SEND_Im_PACKET(this.player,"139");

      SocketManager.GAME_SEND_FRIENDLIST_PACKET(this.player);
      return;
    }
    switch(packet.charAt(2))
    {
      case 'S'://Teleportation
        // TP Mariage : mettre une condition de donjon ...
        if(Wife.getCurMap().noTP||Wife.getCurMap().haveMobFix())
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"Une barrià¨re magique vous empàªche de rejoindre votre conjoint.");
          return;
        }
        if (System.currentTimeMillis() < this.timeLastprisme_zaap_zaapi) {
            player.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() - this.timeLastprisme_zaap_zaapi) / 1000+" seconde(s)");
            return;
        }
			this.timeLastprisme_zaap_zaapi = (System.currentTimeMillis()+1000);
        if(this.player.getFight()!=null)
          return;
        else
          this.player.meetWife(Wife);
        break;
      case 'C'://Suivre le deplacement
        if(packet.charAt(3)=='+')//Si lancement de la traque
        {
          if(this.player.follow!=null)
            this.player.follow.follower.remove(this.player.getId());
          SocketManager.GAME_SEND_FLAG_PACKET(this.player,Wife);
          this.player.follow=Wife;
          Wife.follower.put(this.player.getId(),this.player);
        }
        else
        //On arrete de suivre
        {
          SocketManager.GAME_SEND_DELETE_FLAG_PACKET(this.player);
          this.player.follow=null;
          Wife.follower.remove(this.player.getId());
        }
        break;
    }
  }

  /** Fin Friend Packet **/

  /**
   * Fight Packet *
   */
  private void parseFightPacket(String packet)
  {
    try
    {
      switch(packet.charAt(1))
      {
        case 'D'://Dà¯Â¿Â½tails d'un combat (liste des combats)
          int key=-1;
          try
          {
            key=Integer.parseInt(packet.substring(2).replace(0x0+"",""));
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
          if(key==-1)
            return;
          SocketManager.GAME_SEND_FIGHT_DETAILS(this,this.player.getCurMap().getFight(key));
          break;

        case 'H'://Aide
          if(this.player.getFight()==null)
            return;
          this.player.getFight().toggleHelp(this.player.getId());
          break;
        case 'L'://Lister les combats
          SocketManager.GAME_SEND_FIGHT_LIST_PACKET(this,this.player.getCurMap());
          break;
        case 'N'://Bloquer le combat
          if(this.player.getFight()==null)
            return;
          this.player.getFight().toggleLockTeam(this.player.getId());
          break;
        case 'P'://Seulement le groupe
          if(this.player.getFight()==null||this.player.getParty()==null)
            return;
          this.player.getFight().toggleOnlyGroup(this.player.getId());
          break;
        case 'S'://Bloquer les specs
          if(this.player.getFight()!=null)
            this.player.getFight().toggleLockSpec(this.player);
          break;

      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /** Fin Fight Packet **/

  /**
   * Game Packet *
   */
  private void parseGamePacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'A':
        if(this.player!=null)
          sendActions(packet);
        break;
      case 'C':
        if(this.player!=null)
          this.player.sendGameCreate();
        break;
      case 'd':
        showMonsterTarget(packet);
        break;
     /* case 'D':
    	  SocketManager.ENVIAR_GDM_MAPDATA_COMPLETO(this.player);
          break;*/
      case 'f':
        setFlag(packet);
        break;
      case 'F':
        this.player.setGhost();
        break;
      case 'I':
        getExtraInformations();
        break;
      case 'K':
        actionAck(packet);
        break;
      case 'P'://PvP Toogle
        this.player.toggleWings(packet.charAt(2));
        break;
      case 'p':
        setPlayerPosition(packet);
        break;
      case 'Q':
        leaveFight(packet);
        break;
      case 'R':
        readyFight(packet);
        break;
      case 't':
        if(this.player.getFight()!=null)
          this.player.getFight().playerPass(this.player);
        break;
    }
  }

  //v2.5 - follow system
  private synchronized void sendActions(String packet)
  {
    if(this.player.getDoAction())
    {
      SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
      return;
    }
    int actionID;
    try
    {
      actionID=Integer.parseInt(packet.substring(2,5));
    }
    catch(NumberFormatException e)
    {
     // e.printStackTrace();
      return;
    }
    int nextGameActionID=0;

    if(actions.size()>0)
    {
      //On prend le plus haut GameActionID + 1
      nextGameActionID=(Integer)(actions.keySet().toArray()[actions.size()-1])+1;
    }
    GameAction GA=new GameAction(nextGameActionID,actionID,packet);
    switch(actionID)
    {
      case 1://Deplacement
       // GameCase oldCase=this.player.getCurCell();
        gameParseDeplacementPacket(GA);
      /*  final Party party=this.player.getParty();

        if(party!=null&&this.player.getFight()==null&&party.getMaster()!=null&&party.getMaster().getName().equals(this.player.getName()))
        {
          new TimerWaiterPlus(() -> party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1,false)).forEach(follower -> {
            if(follower.getCurCell().getId()!=oldCase.getId())
              follower.teleport(follower.getCurMap().getId(),oldCase.getId());
            follower.getGameClient().sendActions(packet);
          }),0);
        }*/
        break;

      case 34://Get quest on sign.
        gameCheckSign(packet);

      case 300://Sort
        gameTryCastSpell(packet);
        break;

      case 303://Attaque CaC
        gameTryCac(packet);
        break;

      case 500://Action Sur Map
        gameAction(GA);
        this.player.setGameAction(GA);
        break;

      case 507://Panneau intà¯Â¿Â½rieur de la maison
        houseAction(packet);
        break;

      case 512:
        if(this.player.get_align()==Constant.ALIGNEMENT_NEUTRE)
          return;
        this.player.openPrismeMenu();
        break;

      case 618://Mariage oui
        this.player.setisOK(Integer.parseInt(packet.substring(5,6)));
        SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(this.player.getCurMap(),"",this.player.getId(),this.player.getName(),"Yes");
        Player boy=(Player)this.player.getCurMap().getCase(282).getPlayers().toArray()[0],girl=(Player)this.player.getCurMap().getCase(297).getPlayers().toArray()[0];

        if(girl.getisOK()>0&&boy.getisOK()>0)
          Main.world.wedding(girl,boy,1);
        else
          Main.world.priestRequest(boy,girl,this.player==boy ? girl : boy);
        break;
      case 619://Mariage non
        this.player.setisOK(0);
        SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(this.player.getCurMap(),"",this.player.getId(),this.player.getName(),"No");
        boy=(Player)this.player.getCurMap().getCase(282).getPlayers().toArray()[0];
        girl=(Player)this.player.getCurMap().getCase(297).getPlayers().toArray()[0];

        Main.world.wedding(girl,boy,0);
        break;

      case 900://Demande Defie
        if(Config.getInstance().fightAsBlocked)
          return;
        gameAskDuel(packet);
        break;

      case 901://Accepter Defie
        if(Config.getInstance().fightAsBlocked)
          return;
        gameAcceptDuel(packet);
        break;

      case 902://Refus/Anuler Defie
        gameCancelDuel(packet);
        break;

      case 903://Rejoindre combat
        gameJoinFight(packet);
        break;

      case 906://Agresser
        if(Config.getInstance().fightAsBlocked)
          return;
        gameAggro(packet);
        break;

      case 909://Collector
        if(Config.getInstance().fightAsBlocked)
          return;
        long calcul=System.currentTimeMillis()-Config.getInstance().startTime;
        if(calcul<600000)
        {
          this.player.sendMessage("You have to wait "+((600000-calcul)/60000)+" more minute(s) before attacking this perceptor.");
          return;
        }
        gameCollector(packet);
        break;

      case 912:// ataque Prisme
        if(Config.getInstance().fightAsBlocked)
          return;
        calcul=System.currentTimeMillis()-Config.getInstance().startTime;
        if(calcul<600000)
        {
          this.player.sendMessage("You have to wait "+((600000-calcul)/60000)+" more minute(s) before attacking this prism.");
          return;
        }
        gamePrism(packet);
        break;
    }
  }

  private void gameParseDeplacementPacket(GameAction GA)
  {
    String path=GA.packet.substring(5);
    if(this.player.getFight()==null)
    {
      if(this.player.getBlockMovement())
      {
        SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
        removeAction(GA);
        return;
      }
      if(this.player.isDead()==1)
      {
        SocketManager.GAME_SEND_BN(this.player);
        removeAction(GA);
        return;
      }
      if(this.player.getDoAction())
      {
        SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
        removeAction(GA);
        return;
      }
      if(this.player.getMount()!=null&&!this.player.isGhost())
      {
        if(!this.player.getMorphMode()&&(this.player.getPodUsed()>this.player.getMaxPod()||this.player.getMount().getActualPods()>this.player.getMount().getMaxPods()))
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"112");
          SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
          removeAction(GA);
          return;
        }
      }
      if(this.player.getPodUsed()>this.player.getMaxPod()&&!this.player.isGhost()&&!this.player.getMorphMode())
      {
        SocketManager.GAME_SEND_Im_PACKET(this.player,"112");
        SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
        removeAction(GA);
        return;
      }
      //Si dà¯Â¿Â½placement inutile
      GameCase targetCell=this.player.getCurMap().getCase(Main.world.getCryptManager().cellCode_To_ID(path.substring(path.length()-2)));
      try {

      if(this.player.getCurMap().getId()==6824&&this.player.start!=null&&targetCell.getId()==325&&!this.player.start.leave)
      {
        this.player.start.leave=true;
        SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
        removeAction(GA);
        return;
      }
      if(!targetCell.isWalkable(false))
      {
        SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
        removeAction(GA);
        return;
      }}
      catch(Exception e)
      {
        e.printStackTrace();
      }
      AtomicReference<String> pathRef=new AtomicReference<>(path);
      int result=PathFinding.isValidPath(this.player.getCurMap(),this.player.getCurCell().getId(),pathRef,null,this.player,targetCell.getId());
      if(this.player.getCurJobAction()!=null&&this.player.getCurJobAction().getJobCraft()!=null)
      {
        this.player.getCurJobAction().getJobCraft().jobAction.broken=true;
        System.err.println("curJob = "+player.getCurJobAction());
        this.player.setCurJobAction(null);
        System.err.println("curJob = null");
      }
      if(result<=-9999)
      {
        result+=10000;
        GA.tp=true;
      }
      if(result==0)
      {
        if(targetCell.getObject()!=null)
        {
          if(Config.getInstance().debugMode)
          {
            Main.world.logger.error("#1# Object Interactif : "+targetCell.getObject().getId());
            Main.world.logger.error("#1# On cellule : "+targetCell.getId());
          }
          InteractiveObject.getActionIO(this.player,targetCell,targetCell.getObject().getId());
          InteractiveObject.getSignIO(this.player,targetCell.getId(),targetCell.getObject().getId());
          SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
          removeAction(GA);
          return;
        }
        SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
        removeAction(GA);
        return;
      }
      if(result!=-1000&&result<0)
        result=-result;

      //On prend en compte le nouveau path
      path=pathRef.get();
      //Si le path est invalide
      if(result==-1000)
        path=Main.world.getCryptManager().getHashedValueByInt(this.player.get_orientation())+Main.world.getCryptManager().cellID_To_Code(this.player.getCurCell().getId());
      GA.args=path;

      if(this.player.walkFast)
      {
        this.player.getCurCell().removePlayer(this.player);
        SocketManager.GAME_SEND_BN(this);
        //On prend la case ciblà¯Â¿Â½e
        GameCase nextCell=this.player.getCurMap().getCase(Main.world.getCryptManager().cellCode_To_ID(path.substring(path.length()-2)));
        targetCell=this.player.getCurMap().getCase(Main.world.getCryptManager().cellCode_To_ID(GA.packet.substring(GA.packet.length()-2)));

        //On dà¯Â¿Â½finie la case et on ajthise le this.playernnage sur la case
        this.player.setCurCell(nextCell);
        this.player.setOldCell(nextCell.getId());
        this.player.set_orientation(Main.world.getCryptManager().getIntByHashedValue(path.charAt(path.length()-3)));
        this.player.getCurCell().addPlayer(this.player);
        if(!this.player.isGhost())
          this.player.setAway(false);
        this.player.getCurMap().onPlayerArriveOnCell(this.player,this.player.getCurCell().getId());
        if(targetCell.getObject()!=null)
        {
          if(Config.getInstance().debugMode)
          {
            Main.world.logger.error("#3# Object Interactif : "+targetCell.getObject().getId());
            Main.world.logger.error("#3# On cellule : "+targetCell.getId());
          }
          InteractiveObject.getActionIO(this.player,targetCell,targetCell.getObject().getId());
          InteractiveObject.getSignIO(this.player,targetCell.getId(),targetCell.getObject().getId());
        }
        SocketManager.GAME_SEND_GA_PACKET(this,"","0","","");
        removeAction(GA);
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(),this.player.getId());
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.player.getCurMap(),this.player);
        return;
      }
      else
        SocketManager.GAME_SEND_GA_PACKET_TO_MAP(this.player.getCurMap(),""+GA.id,1,this.player.getId()+"","a"+Main.world.getCryptManager().cellID_To_Code(this.player.getCurCell().getId())+path);

      this.addAction(GA);
      this.player.setSitted(false);
      this.player.setAway(true);
    }
    else
    {
      final Fighter fighter=this.player.getFight().getFighterByPerso(this.player);
      if(fighter!=null)
      {
        GA.args=path;
        this.player.getFight().cast(this.player.getFight().getFighterByPerso(this.player),() -> this.player.getFight().onFighterDeplace(fighter,GA));
      }
    }
  }

  private void gameCheckSign(String packet)
  {
    Quest quete=Quest.getQuestById(Integer.parseInt(packet.substring(5)));
    QuestPlayer qp=this.player.getQuestPersoByQuest(quete);
    if(qp==null)
      quete.applyQuest(this.player); // S'il n'a pas la quà¯Â¿Â½te
    else
      SocketManager.GAME_SEND_MESSAGE(this.player,"Vous avez déjà  commencé cette quàªte.");
    //this.player.addNewQuest(Integer.parseInt(packet.substring(5)));
  }

  private void gameTryCastSpell(String packet)
  {
    try
    {
      String[] split=packet.split(";");

      if(packet.contains("undefined")||split==null||split.length!=2)
        return;
      final int id=Integer.parseInt(split[0].substring(5)),cellId=Integer.parseInt(split[1]);
      final Fight fight=this.player.getFight();
      if(fight!=null)
      {
        Spell.SortStats SS=this.player.getSortStatBySortIfHas(id);
        if(SS!=null)
          if(this.player.getFight().getCurAction().isEmpty())
            this.player.getFight().cast(this.player.getFight().getFighterByPerso(this.player),() -> this.player.getFight().tryCastSpell(this.player.getFight().getFighterByPerso(this.player),SS,cellId));
      }
    }
    catch(NumberFormatException e)
    {
      System.err.println(packet+"\n"+e);
    }
  }

  private void gameTryCac(String packet)
  {
    try
    {
      if(packet.contains("undefined"))
        return;
      final int cell=Integer.parseInt(packet.substring(5));
      if(this.player.getFight()!=null&&this.player.getFight().getCurAction().isEmpty())
        this.player.getFight().cast(this.player.getFight().getFighterByPerso(this.player),() -> this.player.getFight().tryCaC(this.player,cell));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private synchronized void gameAction(GameAction GA)
  {
    String packet=GA.packet.substring(5);
    int cellID=-1;
    int actionID=-1;

    try
    {
      cellID=Integer.parseInt(packet.split(";")[0]);
      actionID=Integer.parseInt(packet.split(";")[1]);

    if(walk)
    {
      actions.put(-1,GA);
      return;
    }

    //Si packet invalide, ou cellule introuvable
    if(cellID==-1||actionID==-1||this.player==null||this.player.getCurMap()==null||this.player.getCurMap().getCase(cellID)==null)
      return;

    this.player.setOldCell(player.getCurCell().getId());
    GA.args=cellID+";"+actionID;
    this.player.getGameClient().addAction(GA);
    if(this.player.isDead()==0)
      this.player.startActionOnCell(GA);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void houseAction(String packet)
  {
    int actionID=Integer.parseInt(packet.substring(5));
    House h=this.player.getInHouse();
    if(h==null)
      return;
    switch(actionID)
    {
      case 81://Và¯Â¿Â½rouiller maison
        h.lock(this.player);
        break;
      case 97://Acheter maison
        h.buyIt(this.player);
        break;
      case 98://Vendre
      case 108://Modifier prix de vente
        h.sellIt(this.player);
        break;
    }
  }

  private void gameAskDuel(String packet)
  {
    if(this.player.getCurMap().getPlaces().equalsIgnoreCase("|"))
    {
      SocketManager.GAME_SEND_DUEL_Y_AWAY(this,this.player.getId());
      return;
    }
    try
    {
      if(this.player.cantDefie())
        return;
      int guid=Integer.parseInt(packet.substring(5));
      if(this.player.isAway()||this.player.getFight()!=null||this.player.isDead()==1)
      {
        SocketManager.GAME_SEND_DUEL_Y_AWAY(this,this.player.getId());
        return;
      }
      Player Target=Main.world.getPlayer(guid);
      if(Target==null)
        return;
      if(this.player.isInAreaNotSubscribe())
      {
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(),'S');
        return;
      }
      if(Target.isInAreaNotSubscribe())
      {
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(),'S');
        return;
      }

      if(Target.isAway()||Target.getFight()!=null||Target.getCurMap().getId()!=this.player.getCurMap().getId()||Target.isDead()==1||Target.getExchangeAction()!=null||this.player.getExchangeAction()!=null)
      {
        SocketManager.GAME_SEND_DUEL_E_AWAY(this,this.player.getId());
        return;
      }
      this.player.setDuelId(guid);
      this.player.setAway(true);
      player.setOldMap(player.getCurMap().getId());
      player.setOldCell(player.getCurCell().getId());
      Main.world.getPlayer(guid).setDuelId(this.player.getId());
      Main.world.getPlayer(guid).setAway(true);
      SocketManager.GAME_SEND_MAP_NEW_DUEL_TO_MAP(this.player.getCurMap(),this.player.getId(),guid);
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
  }

  //v2.8 - duel tele fix
  private void gameAcceptDuel(String packet)
  {
    if(this.player.cantDefie())
      return;
    int guid=-1;
    try
    {
      guid=Integer.parseInt(packet.substring(5));
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
      return;
    }
    if(this.player.getDuelId()!=guid||this.player.getDuelId()==-1||this.player.isDead()==1)
      return;
    player.setOldMap(player.getCurMap().getId());
    player.setOldCell(player.getCurCell().getId());
    SocketManager.GAME_SEND_MAP_START_DUEL_TO_MAP(this.player.getCurMap(),this.player.getDuelId(),this.player.getId());
    Fight fight=this.player.getCurMap().newFight(Main.world.getPlayer(this.player.getDuelId()),this.player,Constant.FIGHT_TYPE_CHALLENGE);
    Player player=Main.world.getPlayer(this.player.getDuelId());
    this.player.setFight(fight);
    this.player.setAway(false);
    player.setFight(fight);
    player.setAway(false);
  }

  private void gameCancelDuel(String packet)
  {
    try
    {
      if(this.player.getDuelId()==-1)
        return;
      SocketManager.GAME_SEND_CANCEL_DUEL_TO_MAP(this.player.getCurMap(),this.player.getDuelId(),this.player.getId());
      Player player=Main.world.getPlayer(this.player.getDuelId());
      player.setAway(false);
      player.setDuelId(-1);
      player.setFight(null);
      this.player.setAway(false);
      this.player.setDuelId(-1);
      this.player.setFight(null);
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
  }

  //v2.3 - Spectator join PvM
  private void gameJoinFight(String packet)
  {
    if(this.player.getFight()!=null)
      return;
    if(this.player.isDead()==1)
      return;
    String[] infos=packet.substring(5).split(";");
    if(infos.length==1)
    {
      try
      {
        Fight F=this.player.getCurMap().getFight(Integer.parseInt(infos[0]));
        if(F!=null)
        {
          if(F.getType()==Constant.FIGHT_TYPE_PVM&&!F.isBegin())
            F.joinFight(this.player,F.getStartGuid());
          else
            F.joinAsSpect(this.player);
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    else
    {
      try
      {
        int guid=Integer.parseInt(infos[1]);
        if(this.player.isAway())
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(this,'o',guid);
          return;
        }
        Player player=Main.world.getPlayer(guid);
        Fight fight=null;
        if(player==null)
        {
          Prism prism=Main.world.getPrisme(guid);
          if(prism!=null)
            fight=prism.getFight();
        }
        else
        {
          fight=player.getFight();
        }
        if(fight==null)
          return;
        if(fight.getState()>2)
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"191");
          return;
        }
        if(this.player.isInAreaNotSubscribe())
        {
          SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(),'S');
          return;
        }
        player.itemchek();
        if(fight.getPrism()!=null)
          fight.joinPrismFight(this.player,(fight.getTeam0().containsKey(guid) ? 0 : 1));
        else
          fight.joinFight(this.player,guid);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  private void gameAggro(String packet)
  {
    try
    {
      if(this.player==null)
        return;
      if(this.player.getFight()!=null)
        return;
      if(this.player.isGhost())
        return;
      if(this.player.isDead()==1)
        return;
      if(this.player.isInAreaNotSubscribe())
      {
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(),'S');
        return;
      }
      if(this.player.cantAgro())
        return;
      int id=Integer.parseInt(packet.substring(5));
      Player target=Main.world.getPlayer(id);
      if(target==null||!target.isOnline()||target.getFight()!=null||target.getCurMap().getId()!=this.player.getCurMap().getId()||target.get_align()==this.player.get_align()||this.player.getCurMap().getPlaces().equalsIgnoreCase("|")||!target.canAggro()||target.isDead()==1)
        return;
      if(this.player.getAccount().restriction.aggros.containsKey(target.getAccount().getCurrentIp()))
      {
        if((System.currentTimeMillis()-this.player.getAccount().restriction.aggros.get(target.getAccount().getCurrentIp()))<1000*60*60)
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"Vous devez attendre "+((((1000*60*60-((System.currentTimeMillis()-this.player.getAccount().restriction.aggros.get(target.getAccount().getCurrentIp()))))/60)/1000))+" plus de minutes avant d'attaquer.");
          return;
        }
        else
          this.player.getAccount().restriction.aggros.remove(target.getAccount().getCurrentIp());
      }

      this.player.getAccount().restriction.aggros.put(target.getAccount().getCurrentIp(),System.currentTimeMillis());

      if(target.isInAreaNotSubscribe())
      {
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(target.getGameClient(),'S');
        return;
      }
      if(target.get_align()==0)
      {
        this.player.setDeshonor(this.player.getDeshonor()+1);
        SocketManager.GAME_SEND_Im_PACKET(this.player,"084;1");
      }
      this.player.toggleWings('+');
      SocketManager.GAME_SEND_GA_PACKET_TO_MAP(this.player.getCurMap(),"",906,this.player.getId()+"",id+"");
      Database.getStatics().getPlayerData().logs_agro(this.player.getName(), target.getName());
      this.player.getCurMap().newFight(this.player,target,Constant.FIGHT_TYPE_AGRESSION);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void gameCollector(String packet)
  {
    try
    {
      if(this.player==null)
        return;
      if(this.player.getFight()!=null)
        return;
      if(this.player.getExchangeAction()!=null||this.player.isDead()==1||this.player.isAway())
        return;

      int id=Integer.parseInt(packet.substring(5));
      Collector target=Main.world.getCollector(id);

      if(target==null||target.getInFight()>0)
        return;
      if(this.player.getCurMap().getId()!=target.getMap())
        return;
      if(target.getExchange())
      {
        SocketManager.GAME_SEND_Im_PACKET(this.player,"1180");
        return;
      }

      SocketManager.GAME_SEND_GA_PACKET_TO_MAP(this.player.getCurMap(),"",909,this.player.getId()+"",id+"");
      this.player.getCurMap().startFightVersusPercepteur(this.player,target);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void gamePrism(String packet)
  {
    try
    {
      if(this.player.isGhost())
        return;
      if(this.player.getFight()!=null)
        return;
      if(this.player.getExchangeAction()!=null)
        return;
      if(this.player.get_align()==0)
        return;
      if(this.player.isDead()==1)
        return;
      int id=Integer.parseInt(packet.substring(5));
      Prism Prisme=Main.world.getPrisme(id);
      if((Prisme.getInFight()==0||Prisme.getInFight()==-2))
        return;
      SocketManager.SEND_GA_ACTION_TO_Map(this.player.getCurMap(),"",909,this.player.getId()+"",id+"");
      this.player.getCurMap().startFightVersusPrisme(this.player,Prisme);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void showMonsterTarget(String packet)
  {
    int chalID=0;
    chalID=Integer.parseInt(packet.split("i")[1]);
    if(chalID!=0&&this.player.getFight()!=null)
    {
      Fight fight=this.player.getFight();
      if(fight.getAllChallenges().containsKey(chalID))
        fight.getAllChallenges().get(chalID).showCibleToPerso(this.player);
    }
  }

  private void setFlag(String packet)
  {
    if(this.player==null)
      return;
    if(this.player.getFight()==null)
      return;
    int cellID=-1;
    try
    {
      cellID=Integer.parseInt(packet.substring(2));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(cellID==-1)
      return;
    this.player.getFight().showCaseToTeam(this.player.getId(),cellID);
  }

  private void getExtraInformations()
  {
    try
    {
      if(this.player!=null&&this.player.needEndFight()!=-1)
      {
        if(player.castEndFightAction())
          player.getCurMap().applyEndFightAction(player);
        player.setNeededEndFight(-1,null);
      }
      else
      {
        sendExtraInformations();
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  //v2.7 - Replaced String += with StringBuilder
  private void sendExtraInformations()
  {
    try
    {
      if(this.player==null)
        return;
      if(this.player.getFight()!=null&&!this.player.getFight().isFinish())
      {
        //Only Collector
        SocketManager.GAME_SEND_MAP_GMS_PACKETS(this.player.getFight().getMap(),this.player);
        SocketManager.GAME_SEND_GDK_PACKET(this);
        if(this.player.getFight().playerReconnect(this.player))
          return;
      }
      //Maisons
      House.load(this.player,this.player.getCurMap().getId());
      //Objets sur la Map
      SocketManager.GAME_SEND_MAP_GMS_PACKETS(this.player.getCurMap(),this.player);
      SocketManager.GAME_SEND_MAP_MOBS_GMS_PACKETS(this.player.getGameClient(),this.player.getCurMap());
      SocketManager.GAME_SEND_MAP_NPCS_GMS_PACKETS(this,this.player.getCurMap());
      SocketManager.GAME_SEND_MAP_PERCO_GMS_PACKETS(this,this.player.getCurMap());
      SocketManager.GAME_SEND_MAP_OBJECTS_GDS_PACKETS(this,this.player.getCurMap());
      SocketManager.GAME_SEND_GDK_PACKET(this);
      SocketManager.GAME_SEND_MAP_FIGHT_COUNT(this,this.player.getCurMap());
      SocketManager.SEND_GM_PRISME_TO_MAP(this,this.player.getCurMap());
      SocketManager.GAME_SEND_MERCHANT_LIST(this.player,this.player.getCurMap().getId());
      //Les drapeau de combats
      Fight.FightStateAddFlag(this.player.getCurMap(),this.player);
      //Enclos
      SocketManager.GAME_SEND_Rp_PACKET(this.player,this.player.getCurMap().getMountPark());
      //objet dans l'enclos
      SocketManager.GAME_SEND_GDO_OBJECT_TO_MAP(this,this.player.getCurMap());
      SocketManager.GAME_SEND_GM_MOUNT(this,this.player.getCurMap(),true);
      //items au sol
      this.player.getCurMap().sendFloorItems(this.player);
      //Porte intï¿½ractif
      InteractiveDoor.show(this.player);
      //Prisme
      Main.world.showPrismes(this.player);
      for(Player player : this.player.getCurMap().getPlayers())
      {
        ArrayList<Job> jobs=player.getJobs();

        if(jobs!=null)
        {
          GameObject object=player.getObjetByPos(Constant.ITEM_POS_ARME);

          if(object==null)
            continue;

          String packet="EW+"+player.getId()+"|";
          StringBuilder data=new StringBuilder();

          for(Job job : jobs)
          {
            if(job.getSkills().isEmpty())
              continue;
            if(!job.isValidTool(object.getTemplate().getId()))
              continue;

            for(GameCase cell : this.player.getCurMap().getCases())
            {
              if(cell.getObject()!=null)
              {
                if(cell.getObject().getTemplate()!=null)
                {
                  int io=cell.getObject().getTemplate().getId();
                  ArrayList<Integer> skills=job.getSkills().get(io);

                  if(skills!=null)
                    for(int skill : skills)
                      if(!data.toString().contains(String.valueOf(skill)))
                        data.append((data.toString().isEmpty() ? skill : ";"+skill));
                }
              }
            }

            if(!data.toString().isEmpty())
              break;
          }

          player.send(packet+data.toString());
          this.player.send(packet+data.toString());
        }
      }
      this.player.afterFight=false;
	  Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		dateFormat = new SimpleDateFormat("HH");
		int heure = Integer.parseInt(dateFormat.format(actDate));
      if(this.player.getCurMap().getId() == 10700) {
			if(heure == 4 || heure == 8 || heure == 12 || heure == 16 || heure == 20 || heure == 00)
			{
				Main.world.getMap((short)10700).openKrala2();	
			}
			
      }
      if(this.player.getCurMap().getId() == 11095) {
    	  if(heure == 4 || heure == 8 || heure == 12 || heure == 16 || heure == 20 || heure == 00) {
    		  
    	  }
    	  else
    		  this.player.teleport((short) 10700, 385);	
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }


  private void actionAck(String packet)
  {
    int id=-1;
    String[] infos=packet.substring(3).split("\\|");
    try
    {
      id=Integer.parseInt(infos[0]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return;
    }
    if(id==-1)
      return;
    GameAction GA=actions.get(id);
    if(GA==null)
      return;
    boolean isOk=packet.charAt(2)=='K';
    switch(GA.actionId)
    {
      case 1://Deplacement
        if(isOk)
        {
          if(this.player.getFight()==null)
          {
            assert this.player.getCurCell()!=null;
            this.player.getCurCell().removePlayer(this.player);
            SocketManager.GAME_SEND_BN(this);
            String path=GA.args;
            //On prend la case ciblà¯Â¿Â½e

            GameCase nextCell=this.player.getCurMap().getCase(Main.world.getCryptManager().cellCode_To_ID(path.substring(path.length()-2)));
            GameCase targetCell=this.player.getCurMap().getCase(Main.world.getCryptManager().cellCode_To_ID(GA.packet.substring(GA.packet.length()-2)));

            //FIXME: Anti cheat engine speedhack

            //On dà¯Â¿Â½finie la case et on ajoute le personnage sur la case
            this.player.setCurCell(nextCell);
            this.player.set_orientation(Main.world.getCryptManager().getIntByHashedValue(path.charAt(path.length()-3)));
            if(this.player.getCurCell() == null) {
            	this.player.setCurCell(this.player.getCurMap().getCase(200));	
            }
            this.player.getCurCell().addPlayer(this.player);
            if(!this.player.isGhost())
              this.player.setAway(false);
            this.player.getCurMap().onPlayerArriveOnCell(this.player,this.player.getCurCell().getId());
            if(targetCell != null)
            if(targetCell.getObject()!=null)
            {
              if(Config.getInstance().debugMode)
              {
                Main.world.logger.error("#2# Object Interactif : "+targetCell.getObject().getId());
                Main.world.logger.error("#2# On cellule : "+targetCell.getId());
              }
              InteractiveObject.getActionIO(this.player,targetCell,targetCell.getObject().getId());
              InteractiveObject.getSignIO(this.player,targetCell.getId(),targetCell.getObject().getId());
            }

            if(GA.tp)
            {
              GA.tp=false;
              this.player.teleport((short)9864,265);
              return;
            }
          }
          else
          {
            this.player.getFight().onGK(this.player);
            return;
          }
        }
        else
        {
          //Si le joueur s'arrete sur une case
          int newCellID=-1;
          try
          {
            newCellID=Integer.parseInt(infos[1]);
          }
          catch(Exception e)
          {
            e.printStackTrace();
            return;
          }
          if(newCellID==-1)
            return;
          String path=GA.args;
          this.player.getCurCell().removePlayer(this.player);
          this.player.setCurCell(this.player.getCurMap().getCase(newCellID));
          this.player.set_orientation(Main.world.getCryptManager().getIntByHashedValue(path.charAt(path.length()-3)));
          this.player.getCurCell().addPlayer(this.player);
          SocketManager.GAME_SEND_BN(this);
          if(GA.tp)
          {
            GA.tp=false;
            this.player.teleport((short)9864,265);
            return;
          }
        }
        break;

      case 500://Action Sur Map
        this.player.finishActionOnCell(GA);
        this.player.setGameAction(null);
        break;

    }
    removeAction(GA);
  }

  /*private void actionAck(String packet)
  {
    int id=-1;
    String[] infos=packet.substring(3).split("\\|");
    try
    {
      id=Integer.parseInt(infos[0]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return;
    }
    if(id==-1)
      return;
    GameAction GA=actions.get(id);
    if(GA==null)
      return;
    boolean isOk=packet.charAt(2)=='K';
    switch(GA.actionId)
    {
      case 1://Deplacement
        if(isOk)
        {
          if(this.player.getFight()==null)
          {
            assert this.player.getCurCell()!=null;
            this.player.getCurCell().removePlayer(this.player);
            SocketManager.GAME_SEND_BN(this);
            String path=GA.args;
            //On prend la case ciblà¯Â¿Â½e
  
            GameCase nextCell=this.player.getCurMap().getCase(Main.world.getCryptManager().cellCode_To_ID(path.substring(path.length()-2)));
            GameCase targetCell=this.player.getCurMap().getCase(Main.world.getCryptManager().cellCode_To_ID(GA.packet.substring(GA.packet.length()-2)));
  
            //FIXME: Anti cheat engine speedhack
  
            //On dà¯Â¿Â½finie la case et on ajoute le personnage sur la case
            if(nextCell!=null)
            {
              this.player.setCurCell(nextCell);
              this.player.setOldCell(nextCell.getId());
            }
            this.player.set_orientation(Main.world.getCryptManager().getIntByHashedValue(path.charAt(path.length()-3)));
            this.player.getCurCell().addPlayer(this.player);
            if(!this.player.isGhost())
              this.player.setAway(false);
            this.player.getCurMap().onPlayerArriveOnCell(this.player,this.player.getCurCell().getId());
            if(targetCell.getObject()!=null)
            {
              if(Config.getInstance().debugMode)
              {
                Main.world.logger.error("#2# Object Interactif : "+targetCell.getObject().getId());
                Main.world.logger.error("#2# On cellule : "+targetCell.getId());
              }
              InteractiveObject.getActionIO(this.player,targetCell,targetCell.getObject().getId());
              InteractiveObject.getSignIO(this.player,targetCell.getId(),targetCell.getObject().getId());
            }
  
            if(GA.tp)
            {
              GA.tp=false;
              this.player.teleport((short)9864,265);
              return;
            }
          }
          else
          {
            this.player.getFight().onGK(this.player);
            return;
          }
        }
        else
        {
          final Party party=this.player.getParty();
  
          if(party!=null&&this.player.getFight()==null&&party.getMaster()!=null&&party.getMaster().getName().equals(this.player.getName()))
            new TimerWaiterPlus(() -> party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1,false)).forEach(follower -> follower.getGameClient().actionAck(packet)),0);
  
          //Si le joueur s'arrete sur une case
          int newCellID=-1;
          try
          {
            newCellID=Integer.parseInt(infos[1]);
          }
          catch(Exception e)
          {
            e.printStackTrace();
            return;
          }
          if(newCellID==-1)
            return;
  
          String path=GA.args;
          this.player.getCurCell().removePlayer(this.player);
          this.player.setCurCell(this.player.getCurMap().getCase(newCellID));
          this.player.setOldCell(newCellID);
          this.player.set_orientation(Main.world.getCryptManager().getIntByHashedValue(path.charAt(path.length()-3)));
          this.player.getCurCell().addPlayer(this.player);
          SocketManager.GAME_SEND_BN(this);
          if(GA.tp)
          {
            GA.tp=false;
            this.player.teleport((short)9864,265);
            return;
          }
        }
        break;
  
      case 500://Action Sur Map
        this.player.finishActionOnCell(GA);
        this.player.setGameAction(null);
        break;
  
    }
    removeAction(GA);
  }*/

  private void setPlayerPosition(String packet)
  {
    if(this.player.getFight()==null)
      return;
    try
    {
      int cell=Integer.parseInt(packet.substring(2));
      this.player.getFight().exchangePlace(this.player,cell);
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
  }

  private void leaveFight(String packet)
  {
    int id=-1;

    if(!packet.substring(2).isEmpty())
    {
      try
      {
        id=Integer.parseInt(packet.substring(2));
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }

    Fight fight=this.player.getFight();

    if(fight==null)
      return;

    if(id>0)
    {
      Player target=Main.world.getPlayer(id);
      //On ne quitte pas un joueur qui : est null, ne combat pas, n'est pas de à¯Â¿Â½a team.
      if(target==null||target.getFight()==null)
        return;
      if(target.getFight().getTeamId(target.getId())!=this.player.getFight().getTeamId(this.player.getId()))
        return;

      if((fight.getInit0()!=null&&target==fight.getInit0().getPersonnage())||(fight.getInit1()!=null&&target==fight.getInit1().getPersonnage())||target==this.player)
        return;

      fight.leftFight(this.player,target);
    }
    else
    {
      fight.leftFight(this.player,null);
    }
  }

  private void readyFight(String packet)
  {
    if(this.player.getFight()==null)
      return;
    if(this.player.getFight().getState()!=Constant.FIGHT_STATE_PLACE)
      return;
    this.player.setReady(packet.substring(2).equalsIgnoreCase("1"));
    this.player.getFight().verifIfAllReady();
    SocketManager.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(this.player.getFight(),3,this.player.getId(),packet.substring(2).equalsIgnoreCase("1"));

    final Party party=this.player.getParty();

    if(party!=null&&party.getMaster()!=null&&party.getMaster().getName().equals(this.player.getName()))
    {
      new TimerWaiterPlus(() -> party.getPlayers().stream().filter(follower -> party.isWithTheMaster(follower,true)).forEach(follower -> follower.getGameClient().readyFight(packet)),500);
    }
    //TimerWaiter.addNext(() -> party.getPlayers().stream().filter(follower -> party.isWithTheMaster(follower,true)).forEach(follower -> follower.getGameClient().readyFight(packet)),500,TimerWaiter.DataType.CLIENT);
  }

  /** Fin Game Packet **/

  /**
   * Guild Packet *
   */
  private void parseGuildPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'B'://Stats
        boostCaracteristique(packet);
        break;
      case 'b'://Sorts
        boostSpellGuild(packet);
        break;
      case 'C'://Creation
        createGuild(packet);
        break;
      case 'f'://Tà¯Â¿Â½là¯Â¿Â½portation enclo de guilde
        teleportToGuildFarm(packet.substring(2));
        break;
      case 'F'://Retirer Collector
        removeTaxCollector(packet.substring(2));
        break;
      case 'h'://Tà¯Â¿Â½là¯Â¿Â½portation maison de guilde
        teleportToGuildHouse(packet.substring(2));
        break;
      case 'H'://Poser un Collector
        placeTaxCollector();
        break;
      case 'I'://Infos
        getInfos(packet.charAt(2));
        break;
      case 'J'://Join
        invitationGuild(packet.substring(2));
        break;
      case 'K'://Kick
        banToGuild(packet.substring(2));
        break;
      case 'P'://Promote
        changeMemberProfil(packet.substring(2));
        break;
      case 'T'://attaque sur Collector
        joinOrLeaveTaxCollector(packet.substring(2));
        break;
      case 'V'://Ferme le panneau de crà¯Â¿Â½ation de guilde
        leavePanelGuildCreate();
        break;
    }
  }

  private void boostCaracteristique(String packet)
  {
    if(this.player.get_guild()==null)
      return;
    Guild G=this.player.get_guild();
    if(!this.player.getGuildMember().canDo(Constant.G_BOOST))
      return;
    switch(packet.charAt(2))
    {
      case 'p'://Prospec
        if(G.getCapital()<1)
          return;
        if(G.getStats(176)>=500)
          return;
        G.setCapital(G.getCapital()-1);
        G.upgradeStats(176,1);
        break;
      case 'x'://Sagesse
        if(G.getCapital()<1)
          return;
        if(G.getStats(124)>=400)
          return;
        G.setCapital(G.getCapital()-1);
        G.upgradeStats(124,1);
        break;
      case 'o'://Pod
        if(G.getCapital()<1)
          return;
        if(G.getStats(158)>=5000)
          return;
        G.setCapital(G.getCapital()-1);
        G.upgradeStats(158,20);
        break;
      case 'k'://Nb Collector
        if(G.getCapital()<10)
          return;
        if(G.getNbrPerco()>=50)
          return;
        G.setCapital(G.getCapital()-10);
        G.setNbrPerco(G.getNbrPerco()+1);
        break;
    }
    Database.getStatics().getGuildData().update(G);
    SocketManager.GAME_SEND_gIB_PACKET(this.player,this.player.get_guild().parseCollectorToGuild());
  }

  private void boostSpellGuild(String packet)
  {
    if(this.player.get_guild()==null)
      return;
    Guild G2=this.player.get_guild();
    if(!this.player.getGuildMember().canDo(Constant.G_BOOST))
      return;
    int spellID=Integer.parseInt(packet.substring(2));
    if(G2.getSpells().containsKey(spellID))
    {
      if(G2.getCapital()<5)
        return;
      G2.setCapital(G2.getCapital()-5);
      G2.boostSpell(spellID);
      Database.getStatics().getGuildData().update(G2);
      SocketManager.GAME_SEND_gIB_PACKET(this.player,this.player.get_guild().parseCollectorToGuild());
    }
  }

  private void createGuild(String packet)
  {
    if(this.player==null)
      return;
    if(this.player.get_guild()!=null||this.player.getGuildMember()!=null)
    {
      SocketManager.GAME_SEND_gC_PACKET(this.player,"Ea");
      return;
    }
    if(this.player.getFight()!=null||this.player.isAway())
      return;
    try
    {
      String[] infos=packet.substring(2).split("\\|");
      //base 10 => 36
      String bgID=Integer.toString(Integer.parseInt(infos[0]),36);
      String bgCol=Integer.toString(Integer.parseInt(infos[1]),36);
      String embID=Integer.toString(Integer.parseInt(infos[2]),36);
      String embCol=Integer.toString(Integer.parseInt(infos[3]),36);
      String name=infos[4];
      if(Main.world.guildNameIsUsed(name))
      {
        SocketManager.GAME_SEND_gC_PACKET(this.player,"Ean");
        return;
      }

      //Validation du nom de la guilde
      String tempName=name.toLowerCase();
      boolean isValid=true;
      //Và¯Â¿Â½rifie d'abord si il contient des termes dà¯Â¿Â½finit
      if(tempName.length()>20||tempName.contains("mj")||tempName.contains("modo")||tempName.contains("fuck")||tempName.contains("admin"))
      {
        isValid=false;
      }
      //Si le nom passe le test, on và¯Â¿Â½rifie que les caractà¯Â¿Â½re entrà¯Â¿Â½ sont correct.
      if(isValid)
      {
        int tiretCount=0;
        for(char curLetter : tempName.toCharArray())
        {
          if(!((curLetter>='a'&&curLetter<='z')||curLetter>='A'&&curLetter<='Z'||curLetter=='-'))
          {
            if(curLetter=='\'')
              continue;
            isValid=false;
            break;
          }
          if(curLetter=='-')
          {
            if(tiretCount>=2)
            {
              isValid=false;
              break;
            }
            else
            {
              tiretCount++;
            }
          }
          if(curLetter==' ')
          {
            if(tiretCount>=2)
            {
              isValid=false;
              break;
            }
            else
            {
              tiretCount++;
            }
          }
        }
      }
      //Si le nom est invalide
      if(!isValid)
      {
        SocketManager.GAME_SEND_gC_PACKET(this.player,"Ean");
        return;
      }
      //FIN de la validation
      String emblem=bgID+","+bgCol+","+embID+","+embCol;//9,6o5nc,2c,0;
      if(Main.world.guildEmblemIsUsed(emblem))
      {
        SocketManager.GAME_SEND_gC_PACKET(this.player,"Eae");
        return;
      }
      if(this.player.getCurMap().getId()==2196)//Temple de crà¯Â¿Â½ation de guilde
      {
        if(!this.player.hasItemTemplate(1575,1))//Guildalogemme
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"14");
          return;
        }
        this.player.removeByTemplateID(1575,1);
      }
      Guild G=new Guild(name,emblem);
      GuildMember gm=G.addNewMember(this.player);
      gm.setAllRights(1,(byte)0,1,this.player);//1 => Meneur (Tous droits)
      this.player.setGuildMember(gm);//On ajthise le meneur
      Main.world.addGuild(G,true);
      Database.getDynamics().getGuildMemberData().update(this.player);
      //Packets
      SocketManager.GAME_SEND_gS_PACKET(this.player,gm);
      SocketManager.GAME_SEND_gC_PACKET(this.player,"K");
      SocketManager.GAME_SEND_gV_PACKET(this.player);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void teleportToGuildFarm(String packet)
  {
    if(this.player.get_guild()==null)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1135");
      return;
    }
    if(this.player.getFight()!=null||this.player.isAway())
      return;
    short MapID=Short.parseShort(packet);
    MountPark MP=Main.world.getMap(MapID).getMountPark();
    if(MP.getGuild().getId()!=this.player.get_guild().getId())
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1135");
      return;
    }
    int CellID=Main.world.getEncloCellIdByMapId(MapID);
    if(this.player.hasItemTemplate(9035,1))
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"022;1~9035");
      this.player.removeByTemplateID(9035,1);
      this.player.teleport(MapID,CellID);
    }
    else
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1159");
    }
  }

  private void removeTaxCollector(String packet)
  {
    if(this.player.get_guild()==null||this.player.getFight()!=null||this.player.isAway())
      return;
    if(!this.player.getGuildMember().canDo(Constant.G_POSPERCO))
      return;//On peut le retirer si on a le droit de le poser
    int idCollector=Integer.parseInt(packet);
    Collector Collector=Main.world.getCollector(idCollector);
    if(Collector==null||Collector.getInFight()>0)
      return;
    Collector.reloadTimer();
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(),idCollector);
    Database.getDynamics().getCollectorData().delete(Collector.getId());
    Collector.delCollector(Collector.getId());
    for(Player z : this.player.get_guild().getOnlineMembers())
    {
      if(!z.isOnline())
        z.setOnline(true);
      SocketManager.GAME_SEND_gITM_PACKET(z,soufix.entity.Collector.parseToGuild(z.get_guild().getId()));
      String str="";
      str+="R"+Integer.toString(Collector.getN1(),36)+","+Integer.toString(Collector.getN2(),36)+"|";
      str+=Collector.getMap()+"|";
      str+=Main.world.getMap(Collector.getMap()).getX()+"|"+Main.world.getMap(Collector.getMap()).getY()+"|"+this.player.getName();
      SocketManager.GAME_SEND_gT_PACKET(z,str);
    }
  }

  private void teleportToGuildHouse(String packet)
  {
    if(this.player.get_guild()==null)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1135");
      return;
    }

    if(this.player.getFight()!=null||this.player.isAway())
      return;
    int HouseID=Integer.parseInt(packet);
    House h=Main.world.getHouses().get(HouseID);
    if(h==null)
      return;
    if(this.player.get_guild().getId()!=h.getGuildId())
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1135");
      return;
    }
    if(!h.canDo(Constant.H_GTELE))
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1136");
      return;
    }
    if(this.player.hasItemTemplate(8883,1))
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"022;1~8883");
      this.player.removeByTemplateID(8883,1);
      this.player.teleport((short)h.getHouseMapId(),h.getHouseCellId());
    }
    else
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1137");
    }
  }

  @SuppressWarnings("unused")
  private void placeTaxCollector()
  {
    if(this.player.get_guild()==null||this.player.getFight()!=null||this.player.isAway())
      return;
    if(!this.player.getGuildMember().canDo(Constant.G_POSPERCO))
      return;//Pas le droit de le poser
    if(!this.player.get_guild().haveTenMembers())
      return;//Guilde invalide
    if(this.player.isInAreaNotSubscribe())
    {
      SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.player.getGameClient(),'S');
      return;
    }
    short price=(short)(1000+10*this.player.get_guild().getLvl()); //Calcul du prix du Collector
    if(this.player.getKamas()<price)
    {//Kamas insuffisants
      SocketManager.GAME_SEND_Im_PACKET(this.player,"182");
      return;
    }
    if(Collector.getCollectorByGuildId(this.player.getCurMap().getId())>0)
    {//La Map possà¯Â¿Â½de un Collector
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1168;1");
      return;
    }
    if(this.player.getCurMap().getPlaces().length()<5||Capture.isInArenaMap(this.player.getCurMap().getId())||this.player.getCurMap().noCollector)
    {//La map ne possà¯Â¿Â½de pas de "places"
      SocketManager.GAME_SEND_Im_PACKET(this.player,"113");
      return;
    }
    if(Collector.countCollectorGuild(this.player.get_guild().getId())>=this.player.get_guild().getNbrPerco())
      return;//Limite de Collector
    if(this.player.get_guild().timePutCollector.get(this.player.getCurMap().getId())!=null)
    {
      long time=this.player.get_guild().timePutCollector.get(this.player.getCurMap().getId());
      Long t=time;
      if(t==null)
      {
        time=Main.world.getCollectorByMap(this.player.getCurMap().getId()).getDate();
        this.player.get_guild().timePutCollector.put(this.player.getCurMap().getId(),time);
      }

      if((System.currentTimeMillis()-time)<(((10*this.player.get_guild().getLvl())*60)*1000))
      {
        this.player.send("Im1167;"+((((((10*this.player.get_guild().getLvl())*60)*1000)-(System.currentTimeMillis()-time))/1000)/60));
        return;
      }
      this.player.get_guild().timePutCollector.remove(this.player.getCurMap().getId());
    }
    this.player.get_guild().timePutCollector.put(this.player.getCurMap().getId(),System.currentTimeMillis());
    this.player.setKamas(this.player.getKamas()-price);
    if(this.player.getKamas()<=0)
      this.player.setKamas(0);
    SocketManager.GAME_SEND_STATS_PACKET(this.player);
    short random1=(short)(Formulas.getRandomValue(1,70));
    short random2=(short)(Formulas.getRandomValue(1,70));
    //Ajthis du Collector.
    int id=Database.getDynamics().getCollectorData().getId();
    Collector Collector=new Collector(id,this.player.getCurMap().getId(),this.player.getCurCell().getId(),(byte)3,this.player.get_guild().getId(),random1,random2,this.player,System.currentTimeMillis(),"",0,0);
    Main.world.addCollector(Collector);
    SocketManager.GAME_SEND_ADD_PERCO_TO_MAP(this.player.getCurMap());
    Database.getDynamics().getCollectorData().add(id,this.player.getCurMap().getId(),this.player.get_guild().getId(),this.player.getId(),System.currentTimeMillis(),this.player.getCurCell().getId(),3,random1,random2);

    for(Player z : this.player.get_guild().getOnlineMembers())
    {
      if(!z.isOnline())
        z.setOnline(true);
      SocketManager.GAME_SEND_gITM_PACKET(z,soufix.entity.Collector.parseToGuild(z.get_guild().getId()));
      String str="";
      str+="S"+Integer.toString(Collector.getN1(),36)+","+Integer.toString(Collector.getN2(),36)+"|";
      str+=Collector.getMap()+"|";
      str+=Main.world.getMap(Collector.getMap()).getX()+"|"+Main.world.getMap(Collector.getMap()).getY()+"|"+this.player.getName();
      SocketManager.GAME_SEND_gT_PACKET(z,str);
    }
  }

  private void getInfos(char c)
  {
    switch(c)
    {
      case 'B'://Collector
        SocketManager.GAME_SEND_gIB_PACKET(this.player,this.player.get_guild().parseCollectorToGuild());
        break;
      case 'F'://Enclos
        SocketManager.GAME_SEND_gIF_PACKET(this.player,Main.world.parseMPtoGuild(this.player.get_guild().getId()));
        break;
      case 'G'://General
        SocketManager.GAME_SEND_gIG_PACKET(this.player,this.player.get_guild());
        break;
      case 'H'://House
        SocketManager.GAME_SEND_gIH_PACKET(this.player,House.parseHouseToGuild(this.player));
        break;
      case 'M'://Members
        SocketManager.GAME_SEND_gIM_PACKET(this.player,this.player.get_guild(),'+');
        break;
      case 'T'://Collector
        SocketManager.GAME_SEND_gITM_PACKET(this.player,Collector.parseToGuild(this.player.get_guild().getId()));
        Collector.parseAttaque(this.player,this.player.get_guild().getId());
        Collector.parseDefense(this.player,this.player.get_guild().getId());
        break;
    }
  }

  private void invitationGuild(String packet)
  {
    switch(packet.charAt(0))
    {
      case 'R'://Nom this.player
        Player P=Main.world.getPlayerByName(packet.substring(1));
        if(P==null||this.player.get_guild()==null)
        {
          SocketManager.GAME_SEND_gJ_PACKET(this.player,"Eu");
          return;
        }
        if(!P.isOnline())
        {
          SocketManager.GAME_SEND_gJ_PACKET(this.player,"Eu");
          return;
        }
        if(P.isAway())
        {
          SocketManager.GAME_SEND_gJ_PACKET(this.player,"Eo");
          return;
        }
        if(P.get_guild()!=null)
        {
          SocketManager.GAME_SEND_gJ_PACKET(this.player,"Ea");
          return;
        }
        if(!this.player.getGuildMember().canDo(Constant.G_INVITE))
        {
          SocketManager.GAME_SEND_gJ_PACKET(this.player,"Ed");
          return;
        }
        if(this.player.get_guild().getMembers().size()>=(40+this.player.get_guild().getLvl()))//Limite membres max
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"155;"+(40+this.player.get_guild().getLvl()));
          return;
        }
        this.player.setInvitation(P.getId());
        P.setInvitation(this.player.getId());

        SocketManager.GAME_SEND_gJ_PACKET(this.player,"R"+packet.substring(1));
        SocketManager.GAME_SEND_gJ_PACKET(P,"r"+this.player.getId()+"|"+this.player.getName()+"|"+this.player.get_guild().getName());
        break;
      case 'E'://ou Refus
        if(packet.substring(1).equalsIgnoreCase(this.player.getInvitation()+""))
        {
          Player p=Main.world.getPlayer(this.player.getInvitation());
          if(p==null)
            return;//Pas censà¯Â¿Â½ arriver
          SocketManager.GAME_SEND_gJ_PACKET(p,"Ec");
        }
        break;
      //v2.0 - no more mercenaries
      case 'K'://Accepte
        if(packet.substring(1).equalsIgnoreCase(this.player.getInvitation()+""))
        {
          Player p=Main.world.getPlayer(this.player.getInvitation());
          if(p==null)
            return;//Pas censà¯Â¿Â½ arriver
          Guild G=p.get_guild();
          GuildMember GM=G.addNewMember(this.player);
          Database.getDynamics().getGuildMemberData().update(this.player);
          this.player.setGuildMember(GM);
          this.player.setInvitation(-1);
          p.setInvitation(-1);
          /*if(G.getId()==1)
            this.player.modifAlignement(3);*/
          //Packet
          SocketManager.GAME_SEND_gJ_PACKET(p,"Ka"+this.player.getName());
          SocketManager.GAME_SEND_gS_PACKET(this.player,GM);
          SocketManager.GAME_SEND_gJ_PACKET(this.player,"Kj");
        }
        break;
    }
  }

  private void banToGuild(String name)
  {
    if(this.player.get_guild()==null)
      return;
    Player P=Main.world.getPlayerByNamev2(name);
    int guid=-1,guildId=-1;
    Guild toRemGuild;
    GuildMember toRemMember;
    if(P==null)
    {
      int infos[]=Database.getDynamics().getGuildMemberData().isPersoInGuild(name);
      guid=infos[0];
      guildId=infos[1];
      if(guildId<0||guid<0)
        return;
      toRemGuild=Main.world.getGuild(guildId);
      toRemMember=toRemGuild.getMember(guid);
    }
    else
    {
      toRemGuild=P.get_guild();
      if(toRemGuild==null)//La guilde du this.playernnage n'est pas charger ?
      {
        toRemGuild=Main.world.getGuild(this.player.get_guild().getId());//On prend la guilde du this.player qui l'à¯Â¿Â½jecte
      }
      toRemMember=toRemGuild.getMember(P.getId());
      if(toRemMember==null)
        return;//Si le membre n'est pas dans la guilde.
      if(toRemMember.getGuild().getId()!=this.player.get_guild().getId())
        return;//Si guilde diffà¯Â¿Â½rente
    }
    //si pas la meme guilde
    if(toRemGuild.getId()!=this.player.get_guild().getId())
    {
      SocketManager.GAME_SEND_gK_PACKET(this.player,"Ea");
      return;
    }
    //S'il n'a pas le droit de kick, et que ce n'est pas lui mà¯Â¿Â½me la cible
    if(!this.player.getGuildMember().canDo(Constant.G_BAN)&&this.player.getGuildMember().getGuid()!=toRemMember.getGuid())
    {
      SocketManager.GAME_SEND_gK_PACKET(this.player,"Ed");
      return;
    }
    //Si diffà¯Â¿Â½rent : Kick
    if(this.player.getGuildMember().getGuid()!=toRemMember.getGuid())
    {
      if(toRemMember.getRank()==1) //S'il veut kicker le meneur
        return;

      toRemGuild.removeMember(toRemMember.getPlayer());
      toRemMember.getPlayer().setGuildMember(null);
      SocketManager.GAME_SEND_gK_PACKET(this.player,"K"+this.player.getName()+"|"+name);
      if(toRemMember.getPlayer().isOnline())
        SocketManager.GAME_SEND_gK_PACKET(toRemMember.getPlayer(),"K"+this.player.getName());
    }
    else
    //si quitter
    {
      Guild G=this.player.get_guild();
      if(this.player.getGuildMember().getRank()==1&&G.getMembers().size()>1) //Si le meneur veut quitter la guilde mais qu'il reste d'autre joueurs
      {
        SocketManager.GAME_SEND_MESSAGE(this.player,"Vous devez affecter un autre chef pour quitter la guilde.");
        return;
      }
      G.removeMember(this.player);
      this.player.setGuildMember(null);
      if(G.getMembers().isEmpty())
        Main.world.removeGuild(G.getId());
      SocketManager.GAME_SEND_gK_PACKET(this.player,"K"+name+"|"+name);
    }
  }

  private void changeMemberProfil(String packet)
  {
    if(this.player.get_guild()==null)
      return; //Si le this.playernnage envoyeur n'a mà¯Â¿Â½me pas de guilde

    String[] infos=packet.split("\\|");

    int guid=Integer.parseInt(infos[0]);
    int rank=Integer.parseInt(infos[1]);
    byte xpGive=Byte.parseByte(infos[2]);
    int right=Integer.parseInt(infos[3]);

    Player p=Main.world.getPlayerv2(guid); //Cherche le this.playernnage a qui l'on change les droits dans la mà¯Â¿Â½moire
    GuildMember toChange;
    GuildMember changer=this.player.getGuildMember();
    //Rà¯Â¿Â½cupà¯Â¿Â½ration du this.playernnage à¯Â¿Â½ changer, et verification de quelques conditions de base
    if(p==null) //Arrive lorsque le this.playernnage n'est pas chargà¯Â¿Â½ dans la mà¯Â¿Â½moire
    {
      int guildId=Database.getDynamics().getGuildMemberData().isPersoInGuild(guid); //Rà¯Â¿Â½cupà¯Â¿Â½re l'id de la guilde du this.playernnage qui n'est pas dans la mà¯Â¿Â½moire

      if(guildId<0)
        return; //Si le this.playernnage à¯Â¿Â½ qui les droits doivent à¯Â¿Â½tre modifià¯Â¿Â½ n'existe pas ou n'a pas de guilde

      if(guildId!=this.player.get_guild().getId()) //Si ils ne sont pas dans la mà¯Â¿Â½me guilde
      {
        SocketManager.GAME_SEND_gK_PACKET(this.player,"Ed");
        return;
      }
      toChange=Main.world.getGuild(guildId).getMember(guid);
    }
    else
    {
      if(p.get_guild()==null)
        return; //Si la this.playernne à¯Â¿Â½ qui changer les droits n'a pas de guilde
      if(this.player.get_guild().getId()!=p.get_guild().getId()) //Si ils ne sont pas de la meme guilde
      {
        SocketManager.GAME_SEND_gK_PACKET(this.player,"Ea");
        return;
      }

      toChange=p.getGuildMember();
    }

    //Và¯Â¿Â½rifie ce que le this.playernnage changeur à¯Â¿Â½ le droit de faire

    if(changer.getRank()==1) //Si c'est le meneur
    {
      if(changer.getGuid()==toChange.getGuid()) //Si il se modifie lui mà¯Â¿Â½me, reset tthis sauf l'XP
      {
        rank=-1;
        right=-1;
      }
      else
      //Si il modifie un autre membre
      {
        if(rank==1) //Si il met un autre membre "Meneur"
        {
          changer.setAllRights(2,(byte)-1,29694,this.player); //Met le meneur "Bras droit" avec tthis les droits

          //Dà¯Â¿Â½fini les droits à¯Â¿Â½ mettre au nouveau meneur
          rank=1;
          xpGive=-1;
          right=1;
        }
      }
    }
    else
    //Sinon, c'est un membre normal
    {
      if(toChange.getRank()==1) //S'il veut changer le meneur, reset tthis sauf l'XP
      {
        rank=-1;
        right=-1;
      }
      else
      //Sinon il veut changer un membre normal
      {
        if(!changer.canDo(Constant.G_RANK)||rank==1) //S'il ne peut changer les rang ou qu'il veut mettre meneur
          rank=-1; //"Reset" le rang

        if(!changer.canDo(Constant.G_RIGHT)||right==1) //S'il ne peut changer les droits ou qu'il veut mettre les droits de meneur
          right=-1; //"Reset" les droits

        if(!changer.canDo(Constant.G_HISXP)&&!changer.canDo(Constant.G_ALLXP)&&changer.getGuid()==toChange.getGuid()) //S'il ne peut changer l'XP de this.playernne et qu'il est la cible
          xpGive=-1; //"Reset" l'XP
      }

      if(!changer.canDo(Constant.G_ALLXP)&&!changer.equals(toChange)) //S'il n'a pas le droit de changer l'XP des autres et qu'il n'est pas la cible
        xpGive=-1; //"Reset" L'XP
    }
    toChange.setAllRights(rank,xpGive,right,this.player);
    SocketManager.GAME_SEND_gS_PACKET(this.player,this.player.getGuildMember());
    if(p!=null&&p.getId()!=this.player.getId()&&p.isOnline())
      SocketManager.GAME_SEND_gS_PACKET(p,p.getGuildMember());
    Database.getDynamics().getGuildMemberData().update(p);
  }

  private void joinOrLeaveTaxCollector(String packet)
  {
    int TiD=-1;
    String CollectorID=Integer.toString(Integer.parseInt(packet.substring(1)),36);
    try
    {
      TiD=Integer.parseInt(CollectorID);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(TiD==-1)
      return;
    Collector collector=Main.world.getCollector(TiD);
    if(collector==null)
      return;
    if(this.player.isDead()==1)
    {
      SocketManager.GAME_SEND_BN(this.player);
      return;
    }

    switch(packet.charAt(0))
    {
      case 'J'://Rejoindre
        if(this.player.getFight()==null&&!this.player.isAway()&&!this.player.isInPrison())
        {
          if(collector.getDefenseFight().size()>=Main.world.getMap(collector.getMap()).getMaxTeam())
            return;//Plus de place
          collector.addDefenseFight(this.player);
        }
        break;
      case 'V'://Leave
        collector.delDefenseFight(this.player);
        break;
    }
    for(Player z : Main.world.getGuild(collector.getGuildId()).getOnlineMembers())
    {
      if(!z.isOnline())
        z.setOnline(true);
      SocketManager.GAME_SEND_gITM_PACKET(z,Collector.parseToGuild(collector.getGuildId()));
      Collector.parseAttaque(z,collector.getGuildId());
      Collector.parseDefense(z,collector.getGuildId());
    }
  }

  private void leavePanelGuildCreate()
  {
    SocketManager.GAME_SEND_gV_PACKET(this.player);
  }

  /** Fin Guild Packet **/

  /**
   * Housse Packet *
   */
  private void parseHousePacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'B'://Acheter la maison
    	  GameClient.leaveExchange(this.player);
        packet=packet.substring(2);
        House.buy(this.player);
        break;
      case 'G'://Maison de guilde
        packet=packet.substring(2);
        if(packet.isEmpty())
          packet=null;
        House.parseHG(this.player,packet);
        break;
      case 'Q'://Quitter/Expulser de la maison
        packet=packet.substring(2);
        House.leave(this.player,packet);
        break;
      case 'S'://Modification du prix de vente
    	  GameClient.leaveExchange(this.player);
        packet=packet.substring(2);
        House.sell(this.player,packet);
        break;
      case 'V'://Fermer fenetre d'achat
        House.closeBuy(this.player);
        break;
    }
  }

  /** Fin Housse Packet **/

  /**
   * Enemy Packet *
   */
  private void parseEnemyPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'A'://Ajthiser
        addEnemy(packet);
        break;
      case 'D'://Delete
        removeEnemy(packet);
        break;
      case 'L'://Liste
        SocketManager.GAME_SEND_ENEMY_LIST(this.player);
        break;
    }
  }

  private void addEnemy(String packet)
  {
    if(this.player==null)
      return;
    int guid=-1;
    switch(packet.charAt(2))
    {
      case '%'://Nom de this.player
        packet=packet.substring(3);
        Player P=Main.world.getPlayerByName(packet);
        if(P==null)
        {
          SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
          return;
        }
        guid=P.getAccID();

        break;
      case '*'://Pseudo
        packet=packet.substring(3);
        Account C=Main.world.getAccountByPseudo(packet);
        if(C==null)
        {
          SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
          return;
        }
        guid=C.getId();
        break;
      default:
        packet=packet.substring(2);
        Player Pr=Main.world.getPlayerByName(packet);
        if(Pr==null||!Pr.isOnline())
        {
          SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
          return;
        }
        guid=Pr.getAccount().getId();
        break;
    }
    if(guid==-1)
    {
      SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
      return;
    }
    account.addEnemy(packet,guid);
  }

  private void removeEnemy(String packet)
  {
    if(this.player==null)
      return;
    int guid=-1;
    switch(packet.charAt(2))
    {
      case '%'://Nom de this.player
        packet=packet.substring(3);
        Player P=Main.world.getPlayerByName(packet);
        if(P==null)
        {
          SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
          return;
        }
        guid=P.getAccID();

        break;
      case '*'://Pseudo
        packet=packet.substring(3);
        Account C=Main.world.getAccountByPseudo(packet);
        if(C==null)
        {
          SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
          return;
        }
        guid=C.getId();
        break;
      default:
        packet=packet.substring(2);
        Player Pr=Main.world.getPlayerByName(packet);
        if(Pr==null||!Pr.isOnline())
        {
          SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
          return;
        }
        guid=Pr.getAccount().getId();
        break;
    }
    if(guid==-1||!account.isEnemyWith(guid))
    {
      SocketManager.GAME_SEND_FD_PACKET(this.player,"Ef");
      return;
    }
    account.removeEnemy(guid);
  }

  /** Enemy Packet **/

  /**
   * JobOption Packet *
   */
  private void parseJobOption(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'O':
        String[] infos=packet.substring(2).split("\\|");
        int pos=Integer.parseInt(infos[0]);
        int option=Integer.parseInt(infos[1]);
        int slots=Integer.parseInt(infos[2]);
        JobStat SM=this.player.getMetiers().get(pos);
        if(SM==null)
          return;
        SM.setOptBinValue(option);
        SM.setSlotsPublic(slots);
        SocketManager.GAME_SEND_JO_PACKET(this.player,SM);
        break;
    }
  }

  /** Fin JobOption Packet **/

  /**
   * House Code Packet *
   */
  private void parseHouseKodePacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'V'://Fermer fenetre du code
        House.closeCode(this.player);
        break;
      case 'K'://Envoi du code
        sendKey(packet);
        break;
    }
  }

  private void sendKey(String packet)
  {
	
    switch(packet.charAt(2))
    {
      case '0'://Envoi du code || Boost
        packet=packet.substring(4);
        if(this.player.get_savestat()>0)
        {
          try
          {
            int code=0;
            code=Integer.parseInt(packet);
            if(code<0)
              return;
            if(this.player.get_capital()<code)
              code=this.player.get_capital();
            this.player.boostStatFixedCount(this.player.get_savestat(),code);
          }
          catch(Exception e)
          {
            e.printStackTrace();
          } finally
          {
            this.player.set_savestat(0);
            SocketManager.GAME_SEND_KODE(this.player,"V");
          }
        }
       
        else if(this.player.getExchangeAction()!=null&&this.player.getExchangeAction().getType()==ExchangeAction.IN_TRUNK)
        {
        
          Trunk.open(this.player,packet,false);
        }
        else
        {
          if(this.player.getInHouse()!=null)
            this.player.getInHouse().open(this.player,packet,false);
        }
        break;
      case '1'://Changement du code
        if(this.player.getExchangeAction()!=null&&this.player.getExchangeAction().getType()==ExchangeAction.IN_TRUNK)
          Trunk.lock(this.player,packet.substring(4));
        else
          House.lockIt(this.player,packet.substring(4));
        break;
    }
  }

  /** Fin Housse Code Packet **/

  /**
   * Object Packet *
   */
  private void parseObjectPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'd':
        destroyObject(packet);
        break;
      case 'D':
    	  if(this.player.getGroupe() != null)
    	  if(this.player.getGroupe().getId() > 1 && this.player.getGroupe().getId() < 5) {
    		 this.player.sendMessage("action impossible personnage staff"); 
    		 return;
    	  }
        dropObject(packet);
        break;
      case 'M':
        movementObject(packet);
        break;
      case 'U':
        useObject(packet);
        break;
      case 'x':
        dissociateObvi(packet);
        break;
      case 'f':
        feedObvi(packet);
        break;
      case 's':
        setSkinObvi(packet);
        break;
    }
  }

  private void destroyObject(String packet)
  {
    String[] infos=packet.substring(2).split("\\|");
    try
    {
      int guid=Integer.parseInt(infos[0]);
      int qua=1;
      try
      {
        qua=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      GameObject obj=World.getGameObject(guid);
      if(obj==null||!this.player.hasItemGuid(guid)||qua<=0||this.player.getFight()!=null||this.player.isAway())
      {
        //SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(this);
        return;
      }
      if(obj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        return;
      if(qua>obj.getQuantity())
        qua=obj.getQuantity();
      int newQua=obj.getQuantity()-qua;
      if(newQua<=0)
      {
        this.player.removeItem(guid);
        Main.world.removeGameObject(guid);
        Database.getStatics().getObjectData().delete(guid);
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,guid);
      }
      else
      {
        obj.setQuantity(newQua);
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,obj);
      }
      SocketManager.GAME_SEND_STATS_PACKET(this.player);
      SocketManager.GAME_SEND_Ow_PACKET(this.player);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(this);
    }
  }

  private void dropObject(String packet)
  {
    if(this.player.getExchangeAction()!=null)
      return;

    int guid=-1;
    int qua=-1;
    try
    {
      guid=Integer.parseInt(packet.substring(2).split("\\|")[0]);
      qua=Integer.parseInt(packet.split("\\|")[1]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(guid==-1||qua<=0||!this.player.hasItemGuid(guid)||this.player.getFight()!=null||this.player.isAway())
      return;
    GameObject obj=World.getGameObject(guid);
    if(obj == null)
    	return;
    if(obj.isAttach())
      return;

    int cellPosition=Constant.getNearCellidUnused(this.player);
    if(cellPosition<0)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1145");
      return;
    }
    if(obj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
    {
      obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
      SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this.player,obj);
      if(obj.getPosition()==Constant.ITEM_POS_ARME||obj.getPosition()==Constant.ITEM_POS_COIFFE||obj.getPosition()==Constant.ITEM_POS_FAMILIER||obj.getPosition()==Constant.ITEM_POS_CAPE||obj.getPosition()==Constant.ITEM_POS_BOUCLIER||obj.getPosition()==Constant.ITEM_POS_NO_EQUIPED)
        SocketManager.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(),this.player);
    }
    if(qua>=obj.getQuantity())
    {
      this.player.removeItem(guid);
      this.player.getCurMap().getCase(cellPosition).addDroppedItem(obj);
      obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,guid);
    }
    else
    {
      obj.setQuantity(obj.getQuantity()-qua);
      GameObject obj2=GameObject.getCloneObjet(obj,qua);
      obj2.setPosition(Constant.ITEM_POS_NO_EQUIPED);
      this.player.getCurMap().getCase(cellPosition).addDroppedItem(obj2);
      SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,obj);
    }
    if(Logging.USE_LOG)
      Logging.getInstance().write("Object","Dropping : "+this.player.getName()+" a jetàƒÂ© ["+obj.getTemplate().getId()+"@"+obj.getGuid()+";"+qua+"]");
    SocketManager.GAME_SEND_Ow_PACKET(this.player);
    SocketManager.GAME_SEND_GDO_PACKET_TO_MAP(this.player.getCurMap(),'+',this.player.getCurMap().getCase(cellPosition).getId(),obj.getTemplate().getId(),0);
    SocketManager.GAME_SEND_STATS_PACKET(this.player);
  }

  //v2.7 - Replaced String += with StringBuilder
  private synchronized void movementObject(String packet)
  {
    String[] infos=packet.substring(2).split(""+(char)0x0A)[0].split("\\|");
    try
    {
      int quantity=1,id=Integer.parseInt(infos[0]),position=Integer.parseInt(infos[1]);
      try
      {
        quantity=Integer.parseInt(infos[2]);
      }
      catch(Exception ignored)
      {
      }
      GameObject object=World.getGameObject(id);
      if(!this.player.hasItemGuid(id)||object==null)
        return;
      if(this.player.getFight()!=null)
        if(this.player.getFight().getState()>Constant.FIGHT_STATE_ACTIVE)
          return;

      /** Pet subscribe **/
      if(position==Constant.ITEM_POS_FAMILIER&&!this.player.isSubscribe())
      {
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'S');
        return;
      }
      /** End pet subscribe **/

      /** Feed mount **/
      //v2.0 - added gutted fish, meats and kaliptus flower/leaf to list
      if((position==Constant.ITEM_POS_DRAGODINDE)&&(this.player.getMount()!=null))
      {
        if(object.getTemplate().getType()==41||object.getTemplate().getType()==62||object.getTemplate().getType()==63||object.getTemplate().getId()==7903||object.getTemplate().getId()==7904)
        {
          if(object.getQuantity()>0)
          {
            if(quantity>object.getQuantity())
              quantity=object.getQuantity();
            if(object.getQuantity()-quantity>0)
            {
              int newQua=object.getQuantity()-quantity;
              object.setQuantity(newQua);
              SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,object);
            }
            else
            {
              this.player.deleteItem(id);
              Main.world.removeGameObject(id);
              SocketManager.SEND_OR_DELETE_ITEM(this,id);
            }
          }
          this.player.getMount().aumEnergy(5000*quantity);
          SocketManager.GAME_SEND_Re_PACKET(this.player,"+",this.player.getMount());
          SocketManager.GAME_SEND_Im_PACKET(this.player,"0105");
          return;
        }
        SocketManager.GAME_SEND_Im_PACKET(this.player,"190");
        return;
      }
      /** End feed mount **/

      /** Feed pet **/
      if(position==Constant.ITEM_POS_FAMILIER&&object.getTemplate().getType()!=Constant.ITEM_TYPE_FAMILIER&&this.player.getObjetByPos(position)!=null)
      {
        GameObject pets=this.player.getObjetByPos(position);
        Pet p=Main.world.getPets(pets.getTemplate().getId());
        if(p==null)
          return;
        if(p.getEpo()==object.getTemplate().getId())
        {
          PetEntry pet=Main.world.getPetsEntry(pets.getGuid());
          if(pet!=null&&p.getEpo()==object.getTemplate().getId())
            pet.giveEpo(this.player);
          return;
        }
        if(object.getTemplate().getId()!=2239&&!p.canEat(object.getTemplate().getId(),object.getTemplate().getType(),-1))
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"153");
          return;
        }

        int min=0,max=0;
        try
        {
          min=Integer.parseInt(p.getGap().split(",")[0]);
          max=Integer.parseInt(p.getGap().split(",")[1]);
        }
        catch(Exception e)
        {
          // ok
        }

        PetEntry MyPets=Main.world.getPetsEntry(pets.getGuid());
        if(MyPets==null)
          return;
        if(p.getType()==2||p.getType()==3||object.getTemplate().getId()==2239)
        {
          if(object.getQuantity()-1>0)
          {//Si il en reste
            object.setQuantity(object.getQuantity()-1);
            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,object);
          }
          else
          {
            Main.world.removeGameObject(object.getGuid());
            this.player.removeItem(object.getGuid());
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,object.getGuid());
          }

          if(object.getTemplate().getId()==2239)
            MyPets.restoreLife(this.player);
          else
            MyPets.eat(this.player,min,max,p.statsIdByEat(object.getTemplate().getId(),object.getTemplate().getType(),-1),object);

          SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this.player,pets);
          SocketManager.GAME_SEND_Ow_PACKET(this.player);
          this.player.refreshStats();
          SocketManager.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(),this.player);
          SocketManager.GAME_SEND_STATS_PACKET(this.player);
          if(this.player.getParty()!=null)
            SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(this.player.getParty(),this.player);
        }
        return;
        /** End feed pet **/
      }
      else
      {
        ObjectTemplate objTemplate=object.getTemplate();
        int ObjPanoID=objTemplate.getPanoId();
        if(((ObjPanoID>=81&&ObjPanoID<=92)||(ObjPanoID>=201&&ObjPanoID<=212))&&(position==2||position==3||position==4||position==5||position==6||position==7||position==0))
        {
          String[] stats=objTemplate.getStrTemplate().split(",");
          for(String stat : stats)
          {
            String[] val=stat.split("#");
            int effect=Integer.parseInt(val[0],16);
            int spell=Integer.parseInt(val[1],16);
            int modif=Integer.parseInt(val[3],16);
            String modifi=effect+";"+spell+";"+modif;
            SocketManager.SEND_SB_SPELL_BOOST(this.player,modifi);
            this.player.addItemClasseSpell(spell,effect,modif);
          }
          this.player.addItemClasse(objTemplate.getId());
        }
        if(((ObjPanoID>=81&&ObjPanoID<=92)||(ObjPanoID>=201&&ObjPanoID<=212))&&position==-1)
        {
          String[] stats=objTemplate.getStrTemplate().split(",");
          for(String stat : stats)
          {
            String[] val=stat.split("#");
            String modifi=Integer.parseInt(val[0],16)+";"+Integer.parseInt(val[1],16)+";0";
            SocketManager.SEND_SB_SPELL_BOOST(this.player,modifi);
            this.player.removeItemClasseSpell(Integer.parseInt(val[1],16));
          }
          this.player.removeItemClasse(objTemplate.getId());
        }
        if(!Constant.isValidPlaceForItem(object.getTemplate(),position)&&position!=Constant.ITEM_POS_NO_EQUIPED&&object.getTemplate().getType()!=Constant.ITEM_TYPE_OBJET_VIVANT)
          return;
        if(!object.getTemplate().getConditions().equalsIgnoreCase("")&&!ConditionParser.validConditions(this.player,object.getTemplate().getConditions()))
        {
          SocketManager.GAME_SEND_Im_PACKET(this.player,"119|44"); // si le this.player ne và¯Â¿Â½rifie pas les conditions diverses
          return;
        }
        if(!object.getTemplate().getConditions().contains("PJ"))
        if((position==Constant.ITEM_POS_BOUCLIER&&this.player.getObjetByPos(Constant.ITEM_POS_ARME)!=null)||(position==Constant.ITEM_POS_ARME&&this.player.getObjetByPos(Constant.ITEM_POS_BOUCLIER)!=null))
        {
          if(this.player.getObjetByPos(Constant.ITEM_POS_ARME)!=null)
          {
            if(this.player.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().isTwoHanded())
            {
              SocketManager.GAME_SEND_Im_PACKET(this.player,"119|44"); // si le this.player ne và¯Â¿Â½rifie pas les conditions diverses
              this.player.sendMessage("Vous ne pouve pas équipée une arme à 2 mains avec votre bouclier");
              return;
            }
          }
          else
          {
            if(object.getTemplate().isTwoHanded())
            {
            	this.player.sendMessage("Vous ne pouve pas équipée une arme à 2 mains avec votre bouclier");
            	return;
            }
          }

        }
        if(object.getTemplate().getLevel()>this.player.getLevel())
        {// si le this.player n'a pas le level
          SocketManager.GAME_SEND_OAEL_PACKET(this);
          return;
        }

        //On ne peut à¯Â¿Â½quiper 2 items de panoplies identiques, ou 2 Dofus identiques
        if(position!=Constant.ITEM_POS_NO_EQUIPED&&(object.getTemplate().getPanoId()!=-1||object.getTemplate().getType()==Constant.ITEM_TYPE_DOFUS)&&this.player.hasEquiped(object.getTemplate().getId()))
          return;
        // FIN DES VERIFS
        GameObject exObj=this.player.getObjetByPos2(position); //Objet a l'ancienne position
        int objGUID=object.getTemplate().getId();
        // CODE OBVI
        if(object.getTemplate().getType()==Constant.ITEM_TYPE_OBJET_VIVANT)
        {
          if(exObj==null)
          {// si on place l'obvi sur un emplacement vide
            SocketManager.send(this.player,"Im1161");
            return;
          }
          if(exObj.getObvijevanPos()!=0) //already a living item equipped
          {
            SocketManager.GAME_SEND_BN(this.player);
            return;
          }
          exObj.setObvijevanPos(object.getObvijevanPos()); // L'objet qui à¯Â¿Â½tait en place a maintenant un obvi
          Database.getStatics().getObvejivanData().add(object,exObj);
          this.player.removeItem(object.getGuid(),1,false,false); // on enlà¯Â¿Â½ve l'existance de l'obvi en lui-mà¯Â¿Â½me
          SocketManager.send(this.player,"OR"+object.getGuid()); // on le prà¯Â¿Â½cise au client.
          Database.getStatics().getObjectData().delete(object.getGuid());

          exObj.refreshStatsObjet(object.parseStatsStringSansUserObvi()+",3ca#"+Integer.toHexString(objGUID)+"#0#0#0d0+"+objGUID);

          SocketManager.send(this.player,exObj.obvijevanOCO_Packet(position));
          SocketManager.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(),this.player); // Si l'obvi à¯Â¿Â½tait cape ou coiffe : packet au client
          // S'il y avait plusieurs objets
          if(object.getQuantity()>1)
          {
            if(quantity>object.getQuantity())
              quantity=object.getQuantity();

            if(object.getQuantity()-quantity>0)//Si il en reste
            {
              int newItemQua=object.getQuantity()-quantity;
              GameObject newItem=GameObject.getCloneObjet(object,newItemQua);
              this.player.addObjet(newItem,false);
              World.addGameObject(newItem,true);
              object.setQuantity(quantity);
              SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,object);
            }
          }
          else
          {
            Main.world.removeGameObject(object.getGuid());
          }
          Database.getStatics().getPlayerData().update(this.player);
          return; // on s'arrà¯Â¿Â½te là¯Â¿Â½ pour l'obvi
        } // FIN DU CODE OBVI

        if(exObj!=null)//S'il y avait dà¯Â¿Â½ja un objet sur cette place on dà¯Â¿Â½sà¯Â¿Â½quipe
        {
          GameObject obj2;
          ObjectTemplate exObjTpl=exObj.getTemplate();
          /*
          String overExo=object.findOverExo(object,-1);
          if(overExo!="" && Config.getInstance().EXOLIMI)
          {
            List<String> exos=Arrays.asList(overExo.split(";"));
            for(int i=0;i<exos.size();i++)
            {
              List<String> exoSplit=Arrays.asList(exos.get(i).split(","));
              if(Integer.valueOf(exoSplit.get(0))==111) //AP
              {
                if(this.player.getApExo()!=-2)
                {
                  this.player.sendMessage("Vous ne pouvez pas équiper cet objet car vous avez déjà  un objet exo AP équipé.");
                  return;
                }
                this.player.setApExo(object.getTemplate().getType());
              }
              if(Integer.valueOf(exoSplit.get(0))==128) //MP
              {
                if(this.player.getMpExo()!=-2)
                {
                  this.player.sendMessage("Vous ne pouvez pas équiper cet objet car vous avez déjà  un objet exo MP équipé.");
                  return;
                }
                this.player.setMpExo(object.getTemplate().getType());
              }
              if(Integer.valueOf(exoSplit.get(0))==117) //Range
              {
                if(this.player.getRaExo()!=-2)
                {
                  this.player.sendMessage("Vous ne pouvez pas équiper cet objet car vous avez déjà  un objet exagéré à  portée équipé\n" + 
                  		".");
                  return;
                }
                this.player.setRaExo(object.getTemplate().getType());
              }
            }
          }
            */
          int idSetExObj=exObj.getTemplate().getPanoId();
          if((obj2=this.player.getSimilarItem(exObj))!=null)//On le possà¯Â¿Â½de deja
          {
            obj2.setQuantity(obj2.getQuantity()+exObj.getQuantity());
            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,obj2);
            Main.world.removeGameObject(exObj.getGuid());
            this.player.removeItem(exObj.getGuid());
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,exObj.getGuid());
          }
          else
          //On ne le possà¯Â¿Â½de pas
          {
            exObj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
            if((idSetExObj>=81&&idSetExObj<=92)||(idSetExObj>=201&&idSetExObj<=212))
            {
              String[] stats=exObjTpl.getStrTemplate().split(",");
              for(String stat : stats)
              {
                String[] val=stat.split("#");
                String modifi=Integer.parseInt(val[0],16)+";"+Integer.parseInt(val[1],16)+";0";
                SocketManager.SEND_SB_SPELL_BOOST(this.player,modifi);
                this.player.removeItemClasseSpell(Integer.parseInt(val[1],16));
              }
              this.player.removeItemClasse(exObjTpl.getId());
            }
            SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this.player,exObj);
          }
          if(this.player.getObjetByPos(Constant.ITEM_POS_ARME)==null)
            SocketManager.GAME_SEND_OT_PACKET(this,-1);

          //Si objet de panoplie
          if(exObj.getTemplate().getPanoId()>0)
            SocketManager.GAME_SEND_OS_PACKET(this.player,exObj.getTemplate().getPanoId());
        }

        else
        {
          GameObject obj2;
          //On a un objet similaire
          if((obj2=this.player.getSimilarItem(object))!=null)
          {
            if(quantity>object.getQuantity())
              quantity=object.getQuantity();

            obj2.setQuantity(obj2.getQuantity()+quantity);
            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,obj2);

            if(object.getQuantity()-quantity>0)//Si il en reste
            {
              object.setQuantity(object.getQuantity()-quantity);
              SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,object);
            }
            else
            //Sinon on supprime
            {
              Main.world.removeGameObject(object.getGuid());
              this.player.removeItem(object.getGuid());
              SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,object.getGuid());
            }
          }
          else
          //Pas d'objets similaires
          {
            if(object.getPosition()>16)
            {
              int oldPos=object.getPosition();
              object.setPosition(position);
              SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this.player,object);

              if(object.getQuantity()>1)
              {
                if(quantity>object.getQuantity())
                  quantity=object.getQuantity();

                if(object.getQuantity()-quantity>0)
                {//Si il en reste
                  GameObject newItem=GameObject.getCloneObjet(object,object.getQuantity()-quantity);
                  newItem.setPosition(oldPos);

                  object.setQuantity(quantity);
                  SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,object);

                  if(this.player.addObjet(newItem,false))
                    World.addGameObject(newItem,true);
                }
              }
            }
            else
            {
            	/*
              //2.0 - Exo Limit
              if(object.getTemplate().getType()==this.player.getApExo())
              {
                this.player.setApExo(-2);
              }
              else if(object.getTemplate().getType()==this.player.getMpExo())
              {
                this.player.setMpExo(-2);
              }
              else if(object.getTemplate().getType()==this.player.getRaExo())
              {
                this.player.setRaExo(-2);
              }
              if(position!=-1)
              {
                String overExo=object.findOverExo(object,-1);
                if(overExo!="")
                {
                  List<String> exos=Arrays.asList(overExo.split(";"));
                  for(int i=0;i<exos.size();i++)
                  {
                    List<String> exoSplit=Arrays.asList(exos.get(i).split(","));
                    if(Integer.valueOf(exoSplit.get(0))==111) //AP
                    {
                      if(this.player.getApExo()!=-2)
                      {
                        this.player.sendMessage("Vous ne pouvez pas équiper cet objet car vous avez déjà  un objet exagéré AP équipé.");
                        return;
                      }
                      this.player.setApExo(object.getTemplate().getType());
                    }
                    if(Integer.valueOf(exoSplit.get(0))==128) //MP
                    {
                      if(this.player.getMpExo()!=-2)
                      {
                        this.player.sendMessage("Vous ne pouvez pas équiper cet objet car vous avez déjà  un objet exagéré MP équipé.");
                        return;
                      }
                      this.player.setMpExo(object.getTemplate().getType());
                    }
                    if(Integer.valueOf(exoSplit.get(0))==117) //Range
                    {
                      if(this.player.getRaExo()!=-2)
                      {
                        this.player.sendMessage("Vous ne pouvez pas équiper cet objet car vous avez déjà  un objet exagéré à  portée équipé.");
                        return;
                      }
                      this.player.setRaExo(object.getTemplate().getType());
                    }
                  }
                }
              }
              */
              if(position==1)
              {

              }
              object.setPosition(position);
              SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this.player,object);

              if(object.getQuantity()>1)
              {
                if(quantity>object.getQuantity())
                  quantity=object.getQuantity();

                if(object.getQuantity()-quantity>0)
                {//Si il en reste
                  int newItemQua=object.getQuantity()-quantity;
                  GameObject newItem=GameObject.getCloneObjet(object,newItemQua);
                  if(this.player.addObjet(newItem,false))
                    World.addGameObject(newItem,true);
                  object.setQuantity(quantity);
                  SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,object);
                }
              }
            }
          }
        }
        if(position==Constant.ITEM_POS_ARME)
        {
          switch(object.getTemplate().getId())
          //Incarnation
          {
            case 9544: // Tourmenteur tàƒÂ©nebres
              this.player.setFullMorph(1,false,false);
              break;
            case 9545: // Tourmenteur feu
              this.player.setFullMorph(5,false,false);
              break;
            case 9546: // Tourmenteur feuille
              this.player.setFullMorph(4,false,false);
              break;
            case 9547: // Tourmenteur gthiste
              this.player.setFullMorph(3,false,false);
              break;
            case 9548: // Tourmenteur terre
              this.player.setFullMorph(2,false,false);
              break;
            case 10125: // Bandit Archer
              this.player.setFullMorph(7,false,false);
              break;
            case 10126: // Bandit Fine Lame
              this.player.setFullMorph(6,false,false);
              break;
            case 10127: // Bandit Baroudeur
              this.player.setFullMorph(8,false,false);
              break;
            case 10133: // Bandit Ensorcelleur
              this.player.setFullMorph(9,false,false);
              break;
          }
        }
        else
        {// Tourmenteur ; on dà¯Â¿Â½morphe
          if(Constant.isIncarnationWeapon(object.getTemplate().getId()))
            this.player.unsetFullMorph();
        }

        if(object.getTemplate().getId()==2157)
        {
          if(position==Constant.ITEM_POS_COIFFE)
          {
            this.player.setGfxId((this.player.getSexe()==1) ? 8009 : 8006);
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(),this.player.getId());
            SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.player.getCurMap(),this.player);
            SocketManager.GAME_SEND_MESSAGE(this.player,"Vous avez été transformé en mercenaire.");
          }
          else if(position==Constant.ITEM_POS_NO_EQUIPED)
          {
            this.player.setGfxId(this.player.getClasse()*10+this.player.getSexe());
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(),this.player.getId());
            SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.player.getCurMap(),this.player);
            SocketManager.GAME_SEND_MESSAGE(this.player,"Tu n'es plus un mercenaire.");
          }
        }
        if(object.getTemplate().getId()!=2157&&this.player.isMorphMercenaire()&&position==Constant.ITEM_POS_COIFFE)
        {
          this.player.setGfxId(this.player.getClasse()*10+this.player.getSexe());
          SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.player.getCurMap(),this.player.getId());
          SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.player.getCurMap(),this.player);
          SocketManager.GAME_SEND_MESSAGE(this.player,"Tu n'es plus un mercenaire.");
        }

        this.player.refreshStats();
        SocketManager.GAME_SEND_STATS_PACKET(this.player);

        if(this.player.getParty()!=null)
          SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(this.player.getParty(),this.player);

        if(position==Constant.ITEM_POS_ARME||position==Constant.ITEM_POS_COIFFE||position==Constant.ITEM_POS_FAMILIER||position==Constant.ITEM_POS_CAPE||position==Constant.ITEM_POS_BOUCLIER||position==Constant.ITEM_POS_NO_EQUIPED)
          SocketManager.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(),this.player);

        //Si familier
        if(position==Constant.ITEM_POS_FAMILIER&&this.player.isOnMount())
          this.player.toogleOnMount();
        //Verif pour les thisils de mà¯Â¿Â½tier
        if(position==Constant.ITEM_POS_NO_EQUIPED&&this.player.getObjetByPos(Constant.ITEM_POS_ARME)==null)
          SocketManager.GAME_SEND_OT_PACKET(this,-1);
        if(position==Constant.ITEM_POS_ARME&&this.player.getObjetByPos(Constant.ITEM_POS_ARME)!=null)
          this.player.getMetiers().entrySet().stream().filter(e -> e.getValue().getTemplate().isValidTool(this.player.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())).forEach(e -> SocketManager.GAME_SEND_OT_PACKET(this,e.getValue().getTemplate().getId()));
        //Si objet de panoplie
        if(object.getTemplate().getPanoId()>0)
          SocketManager.GAME_SEND_OS_PACKET(this.player,object.getTemplate().getPanoId());
        if(this.player.getFight()!=null)
          SocketManager.GAME_SEND_ON_EQUIP_ITEM_FIGHT(this.player,this.player.getFight().getFighterByPerso(this.player),this.player.getFight());
      }

      // Start craft secure show/hide
      if(position==Constant.ITEM_POS_ARME||(position==Constant.ITEM_POS_NO_EQUIPED&&object.getPosition()==Constant.ITEM_POS_ARME))
      {
        ArrayList<Job> jobs=this.player.getJobs();

        if(jobs!=null)
        {
          object=this.player.getObjetByPos(Constant.ITEM_POS_ARME);

          if(object!=null)
          {
            String arg="EW+"+this.player.getId()+"|";
            StringBuilder data=new StringBuilder();

            for(Job job : jobs)
            {
              if(job.getSkills().isEmpty())
                continue;
              if(job.isMaging())//FIXME: pour l'instant.
                continue;
              if(!job.isValidTool(object.getTemplate().getId()))
                continue;

              for(GameCase cell : this.player.getCurMap().getCases())
              {
                if(cell.getObject()!=null)
                {
                  if(cell.getObject().getTemplate()!=null)
                  {
                    int io=cell.getObject().getTemplate().getId();
                    ArrayList<Integer> skills=job.getSkills().get(io);

                    if(skills!=null)
                      for(int skill : skills)
                        if(!data.toString().contains(String.valueOf(skill)))
                          data.append((data.toString().isEmpty() ? skill : ";"+skill));
                  }
                }
              }

              if(!data.toString().isEmpty())
                break;
            }

            for(Player player : this.player.getCurMap().getPlayers())
              player.send(arg+data);
          }
          else
          {
            for(Player player : this.player.getCurMap().getPlayers())
              player.send("EW+"+this.player.getId());
          }
        }

      }
      // End craft secure show/hide
      if(this.player.getFight()!=null)
      {
        Fighter target=this.player.getFight().getFighterByPerso(this.player);
        this.player.getFight().getFighters(7).stream().filter(fighter -> fighter!=null&&fighter.getPersonnage()!=null).forEach(fighter -> fighter.getPersonnage().send(this.player.getCurMap().getFighterGMPacket(this.player)));
        target.setPdv(this.player.getCurPdv());
        target.setPdvMax(this.player.getMaxPdv());
        SocketManager.GAME_SEND_STATS_PACKET(this.player);
      }
      this.player.verifEquiped();
     Database.getStatics().getPlayerData().update(this.player);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(this);
    }
  }

  private void useObject(String packet)
  {
    int guid=-1;
    int targetGuid=-1;
    short cellID=-1;
    Player target=null;
    try
    {
      String[] infos=packet.substring(2).split("\\|");
      guid=Integer.parseInt(infos[0]);
      try
      {
        targetGuid=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      try
      {
        cellID=Short.parseShort(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return;
    }
    //Si le joueur n'a pas l'objet
    if(Main.world.getPlayer(targetGuid)!=null)
      target=Main.world.getPlayer(targetGuid);
    if(!this.player.hasItemGuid(guid)||this.player.isAway())
      return;
    if(target!=null&&target.isAway())
      return;
    GameObject obj=World.getGameObject(guid);
    if(obj==null)
      return;
    ObjectTemplate T=obj.getTemplate();
    if(T.getLevel()>this.player.getLevel()||(!obj.getTemplate().getConditions().equalsIgnoreCase("")&&!ConditionParser.validConditions(this.player,obj.getTemplate().getConditions())))
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"119|43");
      return;
    }
    T.applyAction(this.player,target,guid,cellID);
    if(T.getType()==Constant.ITEM_TYPE_PAIN||T.getType()==Constant.ITEM_TYPE_VIANDE_COMESTIBLE)
    {
      if(target!=null)
        SocketManager.GAME_SEND_eUK_PACKET_TO_MAP(target.getCurMap(),target.getId(),17);
      else
        SocketManager.GAME_SEND_eUK_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),17);
    }
    else if(T.getType()==Constant.ITEM_TYPE_BIERE)
    {
      if(target!=null)
        SocketManager.GAME_SEND_eUK_PACKET_TO_MAP(target.getCurMap(),target.getId(),18);
      else
        SocketManager.GAME_SEND_eUK_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),18);
    }
  }

  private void dissociateObvi(String packet)
  {
    int guid=-1;
    int pos=-1;
    try
    {
      guid=Integer.parseInt(packet.substring(2).split("\\|")[0]);
      pos=Integer.parseInt(packet.split("\\|")[1]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return;
    }
    if((guid==-1)||(!this.player.hasItemGuid(guid)))
      return;
    GameObject obj=World.getGameObject(guid);
    int idOBVI=Database.getStatics().getObvejivanData().getId(obj); //2.0 - Parasymbic/Alyverol item dissociation bugfix
    if(Main.world.getObjTemplate(idOBVI) == null)
    	return;
    ObjectTemplate t=Main.world.getObjTemplate(idOBVI);
    Database.getStatics().getObvejivanData().delete(obj); //2.0 - Parasymbic/Alyverol item dissociation bugfix
    GameObject obV=t.createNewItem(1,true);
    String obviStats=obj.getObvijevanStatsOnly();
    if(obviStats=="")
    {
      SocketManager.GAME_SEND_MESSAGE(this.player,"Livitinem error 3. Please let a staff member know if the problem is serious.","000000");
      return;
    }
    obV.clearStats();
    obV.refreshStatsObjet(obviStats);
    if(this.player.addObjet(obV,true))
      World.addGameObject(obV,true);
    obj.removeAllObvijevanStats();
    obj.setObvijevanLook(0);
    SocketManager.send(this.player,obj.obvijevanOCO_Packet(pos));
    SocketManager.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(),this.player);
    Database.getStatics().getPlayerData().update(this.player);
  }

  private void feedObvi(String packet)
  {
    int guid=-1;
    int pos=-1;
    int victime=-1;
    try
    {
      guid=Integer.parseInt(packet.substring(2).split("\\|")[0]);
      pos=Integer.parseInt(packet.split("\\|")[1]);
      victime=Integer.parseInt(packet.split("\\|")[2]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return;
    }

    if((guid==-1)||(!this.player.hasItemGuid(guid)))
      return;
    GameObject obj=World.getGameObject(guid);
    GameObject objVictime=World.getGameObject(victime);
    obj.obvijevanNourir(objVictime);

    int qua=objVictime.getQuantity();
    if(qua<=1)
    {
      this.player.removeItem(objVictime.getGuid());
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,objVictime.getGuid());
    }
    else
    {
      objVictime.setQuantity(qua-1);
      SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,objVictime);
    }
    SocketManager.send(this.player,obj.obvijevanOCO_Packet(pos));
    Database.getStatics().getPlayerData().update(this.player);
  }

  private void setSkinObvi(String packet)
  {
    int guid=-1;
    int pos=-1;
    int val=-1;
    try
    {
      guid=Integer.parseInt(packet.substring(2).split("\\|")[0]);
      pos=Integer.parseInt(packet.split("\\|")[1]);
      val=Integer.parseInt(packet.split("\\|")[2]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return;
    }
    if((guid==-1)||(!this.player.hasItemGuid(guid)))
      return;
    GameObject obj=World.getGameObject(guid);
    if((val>=21)||(val<=0))
      return;

    obj.obvijevanChangeStat(972,val);
    SocketManager.send(this.player,obj.obvijevanOCO_Packet(pos));
    if(pos!=-1)
      SocketManager.GAME_SEND_ON_EQUIP_ITEM(this.player.getCurMap(),this.player);
  }

  /** Fin Object Packet **/

  /**
   * Group Packet *
   */
  private void parseGroupPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'A'://Accepter invitation
        acceptInvitation();
        break;
      case 'F'://Suivre membre du groupe PF+GUID
        followMember(packet);
        break;
      case 'G'://Suivez le tous PG+GUID
        followAllMember(packet);
        break;
      case 'I'://inviation
        inviteParty(packet);
        break;
      case 'R'://Refuse
        refuseInvitation();
        break;
      case 'V'://Quitter
        leaveParty(packet);
        break;
      case 'W'://Localisation du groupe
        whereIsParty();
        break;
    }
  }

  public void acceptInvitation()
  {
    if(this.player==null||this.player.getInvitation()==0)
      return;

    Player target=Main.world.getPlayer(this.player.getInvitation());

    if(target==null)
      return;

    Party party=target.getParty();

    if(party==null)
    {
      party=new Party(target,this.player);
      SocketManager.GAME_SEND_GROUP_CREATE(this,party);
      SocketManager.GAME_SEND_PL_PACKET(this,party);
      SocketManager.GAME_SEND_GROUP_CREATE(target.getGameClient(),party);
      SocketManager.GAME_SEND_PL_PACKET(target.getGameClient(),party);
      target.setParty(party);
      SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(target.getGameClient(),party);
    }
    else
    {
      SocketManager.GAME_SEND_GROUP_CREATE(this,party);
      SocketManager.GAME_SEND_PL_PACKET(this,party);
      SocketManager.GAME_SEND_PM_ADD_PACKET_TO_GROUP(party,this.player);
      party.addPlayer(this.player);
    }

    this.player.setParty(party);
    SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(this,party);
    SocketManager.GAME_SEND_PR_PACKET(target);
  }

  private void followMember(String packet)
  {
    Party g=this.player.getParty();
    if(g==null)
      return;
    int pGuid=-1;
    try
    {
      pGuid=Integer.parseInt(packet.substring(3));
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
      return;
    }
    if(pGuid==-1)
      return;
    Player P=Main.world.getPlayer(pGuid);
    if(P==null||!P.isOnline())
      return;
    if(packet.charAt(2)=='+')//Suivre
    {
      if(this.player.follow!=null)
        this.player.follow.follower.remove(this.player.getId());
      SocketManager.GAME_SEND_FLAG_PACKET(this.player,P);
      SocketManager.GAME_SEND_PF(this.player,"+"+P.getId());
      this.player.follow=P;
      P.follower.put(this.player.getId(),this.player);
      P.send("Im052;"+this.player.getName());
    }
    else if(packet.charAt(2)=='-')//Ne plus suivre
    {
      SocketManager.GAME_SEND_DELETE_FLAG_PACKET(this.player);
      SocketManager.GAME_SEND_PF(this.player,"-");
      this.player.follow=null;
      P.follower.remove(this.player.getId());
      P.send("Im053;"+this.player.getName());
    }
  }

  private void followAllMember(String packet)
  {
    Party g2=this.player.getParty();
    if(g2==null)
      return;
    int pGuid2=-1;
    try
    {
      pGuid2=Integer.parseInt(packet.substring(3));
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
      return;
    }

    if(pGuid2==-1)
      return;
    Player P2=Main.world.getPlayer(pGuid2);
    if(P2==null||!P2.isOnline())
      return;
    if(packet.charAt(2)=='+')//Suivre
    {
      for(Player T : g2.getPlayers())
      {
        if(T.getId()==P2.getId())
          continue;
        if(T.follow!=null)
          T.follow.follower.remove(this.player.getId());
        SocketManager.GAME_SEND_FLAG_PACKET(T,P2);
        SocketManager.GAME_SEND_PF(T,"+"+P2.getId());
        T.follow=P2;
        P2.follower.put(T.getId(),T);
        P2.send("Im0178");
      }
    }
    else if(packet.charAt(2)=='-')//Ne plus suivre
    {
      for(Player T : g2.getPlayers())
      {
        if(T.getId()==P2.getId())
          continue;
        SocketManager.GAME_SEND_DELETE_FLAG_PACKET(T);
        SocketManager.GAME_SEND_PF(T,"-");
        T.follow=null;
        P2.follower.remove(T.getId());
        P2.send("Im053;"+T.getName());
      }
    }
  }

  public void inviteParty(String packet)
  {
    if(this.player==null)
      return;

    String name=packet.substring(2);
    Player target=Main.world.getPlayerByName(name);

    if(target==null||!target.isOnline())
    {
      SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(this,"n"+name);
      return;
    }
    if(target.getParty()!=null)
    {
      SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(this,"a"+name);
      return;
    }
    if(target.getGroupe()!=null&&this.player.getGroupe()==null)
    {
      if(!target.getGroupe().isPlayer())
      {
        SocketManager.GAME_SEND_MESSAGE(this.player,"Vous ne pouvez pas inviter des membres du personnel dans votre groupe.");
        return;
      }
    }
    if(this.player.getParty()!=null&&this.player.getParty().getPlayers().size()==8)
    {
      SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(this,"f");
      return;
    }

    target.setInvitation(this.player.getId());
    this.player.setInvitation(target.getId());
    SocketManager.GAME_SEND_GROUP_INVITATION(this,this.player.getName(),name);
    SocketManager.GAME_SEND_GROUP_INVITATION(target.getGameClient(),this.player.getName(),name);
  }

  private void refuseInvitation()
  {
    if(this.player==null||this.player.getInvitation()==0)
      return;

    Player player=Main.world.getPlayer(this.player.getInvitation());

    if(player!=null)
    {
      player.setInvitation(0);
      SocketManager.GAME_SEND_PR_PACKET(player);
    }

    this.player.setInvitation(0);
  }

  private void leaveParty(String packet)
  {
    if(this.player==null)
      return;
    Party g=this.player.getParty();
    if(g==null)
      return;
    if(packet.length()==2)//Si aucun guid est spà¯Â¿Â½cifià¯Â¿Â½, alors c'est que le joueur quitte
    {
      g.leave(this.player);
      SocketManager.GAME_SEND_PV_PACKET(this,"");
      SocketManager.GAME_SEND_IH_PACKET(this.player,"");
    }
    else if(g.isChief(this.player.getId()))//Sinon, c'est qu'il kick un joueur du groupe
    {
      int guid=-1;
      try
      {
        guid=Integer.parseInt(packet.substring(2));
      }
      catch(NumberFormatException e)
      {
        e.printStackTrace();
        return;
      }

      if(guid==-1)
        return;
      Player t=Main.world.getPlayer(guid);
      g.leave(t);
      SocketManager.GAME_SEND_PV_PACKET(t.getGameClient(),""+this.player.getId());
      SocketManager.GAME_SEND_IH_PACKET(t,"");
    }
  }

  //v2.7 - Replaced String += with StringBuilder
  private void whereIsParty()
  {
    if(this.player==null)
      return;
    Party g=this.player.getParty();
    if(g==null)
      return;
    StringBuilder str=new StringBuilder();
    boolean isFirst=true;
    for(Player GroupP : this.player.getParty().getPlayers())
    {
      if(!isFirst)
        str.append("|");
      str.append(GroupP.getCurMap().getX()+";"+GroupP.getCurMap().getY()+";"+GroupP.getCurMap().getId()+";2;"+GroupP.getId()+";"+GroupP.getName());
      isFirst=false;
    }
    SocketManager.GAME_SEND_IH_PACKET(this.player,str.toString());
  }

  /** Fin Group Packet **/

  /**
   * MountPark Packet *
   */
  private void parseMountPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'b'://Achat d'un enclos
    	  GameClient.leaveExchange(this.player);
        buyMountPark(packet);
        break;
      case 'd'://Demande Description en Item.
        dataMount(packet,true);
        break;
      case 'p'://Demande Decription en Enclo.
        dataMount(packet,false);
        break;
      case 'f'://Libà¯Â¿Â½re la monture
        killMount(packet);
        break;
      case 'n'://Change le nom
        renameMount(packet.substring(2));
        break;
      case 'r'://Monter sur la dinde
        rideMount();
        break;
      case 's'://Vendre l'enclo
    	  GameClient.leaveExchange(this.player);
        sellMountPark(packet);
        break;
      case 'v'://Fermeture panneau d'achat
        SocketManager.GAME_SEND_R_PACKET(this.player,"v");
        break;
      case 'x'://Change l'xp donner a la dinde
        setXpMount(packet);
        break;
      case 'c'://Castrer la dinde
        castrateMount();
        break;
      case 'o':// retirer objet de l'etable
        removeObjectInMountPark(packet);
        break;
    }
  }

  //v2.0 - Removed time limit for buying paddock
  private void buyMountPark(String packet)
  {
    SocketManager.GAME_SEND_R_PACKET(this.player,"v");//Fermeture du panneau
    MountPark MP=this.player.getCurMap().getMountPark();
    Player Seller=Main.world.getPlayer(MP.getOwner());
    if(MP.getOwner()==-1)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"196");
      return;
    }
    if(MP.getPrice()==0)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"197");
      return;
    }
    if(this.player.get_guild()==null)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1135");
      return;
    }
    if(this.player.getGuildMember().getRank()!=1)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"198");
      return;
    }
    /*if((System.currentTimeMillis()-this.player.get_guild().getDate())<=2419200000L)
    {
      this.player.send("Im1103");
      return;
    }*/
    byte enclosMax=(byte)Math.floor(this.player.get_guild().getLvl()/10);
    byte TotalEncloGuild=(byte)Main.world.totalMPGuild(this.player.get_guild().getId());
    if(TotalEncloGuild>=enclosMax)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1103");
      return;
    }
    if(this.player.getKamas()<MP.getPrice())
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"182");
      return;
    }
    long NewKamas=this.player.getKamas()-MP.getPrice();
    this.player.setKamas(NewKamas);
    if(Seller!=null)
    {
      long NewSellerBankKamas=Seller.getBankKamas()+MP.getPrice();
      Seller.setBankKamas(NewSellerBankKamas);
      if(Seller.isOnline())
      {
        SocketManager.GAME_SEND_MESSAGE(this.player,"Vous avez vendu votre enclos pour "+MP.getPrice()+" kamas.");
      }
    }
    MP.setPrice(0);//On vide le prix
    MP.setOwner(this.player.getId());
    MP.setGuild(this.player.get_guild());
    Database.getDynamics().getMountParkData().update(MP);
    Database.getStatics().getPlayerData().update(this.player);
    //On rafraichit l'enclo
    for(Player z : this.player.getCurMap().getPlayers())
      SocketManager.GAME_SEND_Rp_PACKET(z,MP);
    SocketManager.GAME_SEND_STATS_PACKET(this.player);
  }

  private void dataMount(String packet, boolean b)
  {
    int id=Integer.parseInt(packet.substring(2).split("\\|")[0]);

    if(id!=0)
    {
      Mount mount=Main.world.getMountById((b ? -1 : 1)*id);
      if(mount!=null)
        SocketManager.GAME_SEND_MOUNT_DESCRIPTION_PACKET(this.player,mount);
    }
  }

  private void killMount(String packet)
  {
    if(this.player.getMount().getObjects().size()!=0)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"1106");
      return;
    }

    if(this.player.getMount()!=null&&this.player.isOnMount())
      this.player.toogleOnMount();
    SocketManager.GAME_SEND_Re_PACKET(this.player,"-",this.player.getMount());
    Database.getStatics().getMountData().delete(this.player.getMount().getId());
    Main.world.removeMount(this.player.getMount().getId());
    this.player.setMount(null);
  }

  private void renameMount(String name)
  {
    if(this.player.getMount()==null)
      return;
    this.player.getMount().setName(name);
    Database.getStatics().getMountData().update(this.player.getMount());
    SocketManager.GAME_SEND_Rn_PACKET(this.player,name);
  }

  private void rideMount()
  {
    if(!this.player.isSubscribe())
    {
      SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this,'S');
      return;
    }

    this.player.toogleOnMount();
  }

  private void sellMountPark(String packet)
  {
    SocketManager.GAME_SEND_R_PACKET(this.player,"v");//Fermeture du panneau
    int price=Integer.parseInt(packet.substring(2));
    if(price < 0 )return;
    MountPark MP1=this.player.getCurMap().getMountPark();
    if(!MP1.getEtable().isEmpty()||!MP1.getListOfRaising().isEmpty())
    {
      SocketManager.GAME_SEND_MESSAGE(this.player,"Yvous ne pouvez pas vendre une enclos qui n'est pas vide.");
      return;
    }
    if(MP1.getOwner()==-1)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"194");
      return;
    }
    if(MP1.getOwner()!=this.player.getId())
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"195");
      return;
    }
    MP1.setPrice(price);
    Database.getDynamics().getMountParkData().update(MP1);
    Database.getStatics().getPlayerData().update(this.player);
    //On rafraichit l'enclo
    for(Player z : this.player.getCurMap().getPlayers())
    {
      SocketManager.GAME_SEND_Rp_PACKET(z,MP1);
    }
  }

  private void setXpMount(String packet)
  {
    try
    {
      int xp=Integer.parseInt(packet.substring(2));
      if(xp<0)
        xp=0;
      if(xp>90)
        xp=90;
      this.player.setMountGiveXp(xp);
      SocketManager.GAME_SEND_Rx_PACKET(this.player);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void castrateMount()
  {
    if(this.player.getMount()==null)
    {
      SocketManager.GAME_SEND_Re_PACKET(this.player,"Er",null);
      return;
    }
    this.player.getMount().setCastrated();
    SocketManager.GAME_SEND_Re_PACKET(this.player,"+",this.player.getMount());
  }

  private void removeObjectInMountPark(String packet)
  {
    int cell=Integer.parseInt(packet.substring(2));
    GameMap map=this.player.getCurMap();
    if(map.getMountPark()==null)
      return;
    MountPark MP=map.getMountPark();

    if(this.player.get_guild()==null)
    {
      SocketManager.GAME_SEND_BN(this);
      return;
    }
    if(!this.player.getGuildMember().canDo(8192))
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"193");
      return;
    }

    int item=MP.getCellAndObject().get(cell);
    ObjectTemplate t=Main.world.getObjTemplate(item);
    GameObject obj=t.createNewItem(1,false); // creation de l'item au stats incorrecte

    int statNew=0;// on vas chercher la valeur de la resistance de l'item
    for(Map.Entry<Integer, Map<Integer, Integer>> entry : MP.getObjDurab().entrySet())
    {
      if(entry.getKey().equals(cell))
      {
        for(Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet())
          statNew=entry2.getValue();
      }
    }
    obj.getTxtStat().remove(812); //on retire les stats "32c"
    obj.addTxtStat(812,Integer.toHexString(statNew));// on ajthis les bonnes stats

    if(this.player.addObjet(obj,true))//Si le joueur n'avait pas d'item similaire
      World.addGameObject(obj,true);
    if(MP.delObject(cell))
      SocketManager.SEND_GDO_PUT_OBJECT_MOUNT(map,cell+";0;0"); // on retire l'objet de la map
  }

  /** Fin MountPark Packet **/

  /**
   * Quest Packet *
   */
  private void parseQuestData(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'L':
        SocketManager.QuestList(this,this.player);
        break;

      case 'S':
        int QuestID=Integer.parseInt(packet.substring(2));
        Quest quest=Quest.getQuestById(QuestID);
        SocketManager.QuestGep(this,quest,this.player);
        break;
    }
  }

  /** Fin Quest Packet **/

  /**
   * Spell Packet *
   */
  private void parseSpellPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'B':
        boostSpell(packet);
        break;
      case 'F'://Oublie de sort
        forgetSpell(packet);
        break;
      case 'M':
        moveToUsed(packet);
        break;
    }
  }

  private void boostSpell(String packet)
  {
    try
    {
      int id=Integer.parseInt(packet.substring(2));
      if(this.player.boostSpell(id))
      {
        SocketManager.GAME_SEND_SPELL_UPGRADE_SUCCED(this,id,this.player.getSortStatBySortIfHas(id).getLevel());
        SocketManager.GAME_SEND_STATS_PACKET(this.player);
      }
      else
      {
        SocketManager.GAME_SEND_SPELL_UPGRADE_FAILED(this);
      }
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
      SocketManager.GAME_SEND_SPELL_UPGRADE_FAILED(this);
    }
  }

  private void forgetSpell(String packet)
  {
    if(this.player.isForgetingSpell()==false)
      return;

    int id=Integer.parseInt(packet.substring(2));
    if(id==-1)
      this.player.setExchangeAction(null);
    if(this.player.forgetSpell(id))
    {
      SocketManager.GAME_SEND_SPELL_UPGRADE_SUCCED(this,id,this.player.getSortStatBySortIfHas(id).getLevel());
      SocketManager.GAME_SEND_STATS_PACKET(this.player);
      this.player.setisForgetingSpell(false);
    }
  }

  public void forgetSpell(int id)
  {
    if(this.player.isForgetingSpell()==false)
      return;

    if(this.player.forgetSpell(id))
    {
      SocketManager.GAME_SEND_SPELL_UPGRADE_SUCCED(this,id,this.player.getSortStatBySortIfHas(id).getLevel());
      SocketManager.GAME_SEND_STATS_PACKET(this.player);
    }
  }

  private void moveToUsed(String packet)
  {
    try
    {
      int SpellID=Integer.parseInt(packet.substring(2).split("\\|")[0]);
      int Position=Integer.parseInt(packet.substring(2).split("\\|")[1]);
      Spell.SortStats Spell=this.player.getSortStatBySortIfHas(SpellID);
      if(Spell!=null)
      {
        this.player.set_SpellPlace(SpellID,Main.world.getCryptManager().getHashedValueByInt(Position));
      }
      SocketManager.GAME_SEND_BN(this);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /** Fin Spell Packet **/

  /**
   * Waypoint Packet *
   */
  private void parseWaypointPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'U'://Use
        waypointUse(packet);
        break;
      case 'u'://use zaapi
        zaapiUse(packet);
        break;
      case 'p':
        prismUse(packet);
        break;
      case 'V'://Quitter
        waypointLeave();
        break;
      case 'v'://quitter zaapi
        zaapiLeave();
        break;
      case 'w':
        prismLeave();
        break;
    }
  }

  private void waypointUse(String packet)
  {
    try
    {
    	 if (System.currentTimeMillis() < this.timeLastprisme_zaap_zaapi) {
             player.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() - this.timeLastprisme_zaap_zaapi) / 1000+" seconde(s)");
             return;
         }
			this.timeLastprisme_zaap_zaapi = (System.currentTimeMillis()+1000);
      final short id=Short.parseShort(packet.substring(2));
      final Party party=this.player.getParty();

      if(party!=null&&this.player.getFight()==null&&party.getMaster()!=null&&party.getMaster().getName().equals(this.player.getName()))
      {
        party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1,false)&&follower1.getExchangeAction()==null).forEach(follower -> {
          follower.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_ZAAPING,null));
          follower.useZaap(id);
        });
      }

      this.player.useZaap(id);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void zaapiUse(final String packet)
  {
    if(this.player.getDeshonor()>=2)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"183");
      return;
    }
    if (System.currentTimeMillis() < this.timeLastprisme_zaap_zaapi) {
        player.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -this.timeLastprisme_zaap_zaapi) / 1000+" seconde(s)");
        return;
    }
		this.timeLastprisme_zaap_zaapi = (System.currentTimeMillis()+1000);
    final Party party=this.player.getParty();

    if(party!=null&&this.player.getFight()==null&&party.getMaster()!=null&&party.getMaster().getName().equals(this.player.getName()))
    {
      party.getPlayers().stream().filter((follower1) -> follower1.getDeshonor()>=2).forEach(follower -> {
        SocketManager.GAME_SEND_Im_PACKET(follower,"183");
      });

      party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1,false)&&follower1.getExchangeAction()==null&&follower1.getDeshonor()<2).forEach(follower -> {
        follower.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_ZAPPI,null));
        follower.getGameClient().zaapiUse(packet);
      });
    }

    this.player.Zaapi_use(packet);
  }

  private void prismUse(String packet)
  {
    if(this.player.getDeshonor()>=2)
    {
      SocketManager.GAME_SEND_Im_PACKET(this.player,"183");
      return;
    }
    if (System.currentTimeMillis() < this.timeLastprisme_zaap_zaapi) {
        player.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() - this.timeLastprisme_zaap_zaapi) / 1000+" seconde(s)");
        return;
    }
		this.timeLastprisme_zaap_zaapi = (System.currentTimeMillis()+1000);

    final Party party=this.player.getParty();

    if(party!=null&&this.player.getFight()==null&&party.getMaster()!=null&&party.getMaster().getName().equals(this.player.getName()))
    {
      party.getPlayers().stream().filter((follower1) -> follower1.getDeshonor()>=2).forEach(follower -> {
        SocketManager.GAME_SEND_Im_PACKET(follower,"183");
      });
      party.getPlayers().stream().filter((follower1) -> party.isWithTheMaster(follower1,false)&&follower1.getExchangeAction()==null&&follower1.get_align()==this.player.get_align()&&follower1.getDeshonor()<2).forEach(follower -> {
        follower.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_PRISM,null));
        follower.usePrisme(packet);
      });
    }

    this.player.usePrisme(packet);
  }

  private void waypointLeave()
  {
    this.player.stopZaaping();
  }

  private void zaapiLeave()
  {
    this.player.Zaapi_close();
  }

  private void prismLeave()
  {
    this.player.Prisme_close();
  }

  /** Fin Waypoint Packet **/

  /**
   * Other *
   */
  private void parseFoireTroll(String packet)
  {
    if(this.player.getExchangeAction()==null||this.player.getExchangeAction().getType()!=ExchangeAction.IN_TUTORIAL)
      return;
    String[] param=packet.split("\\|");
    Tutorial tutorial=(Tutorial)this.player.getExchangeAction().getValue();
    this.player.setExchangeAction(null);
    switch(packet.charAt(1))
    {
      case 'V':
        if(packet.charAt(2)!='0'&&packet.charAt(2)!='4')
          try
          {
            int index=Integer.parseInt(packet.charAt(2)+"")-1;
            tutorial.getReward().get(index).apply(this.player,null,-1,(short)-1);
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        try
        {
          Action end=tutorial.getEnd();
          if(end!=null&&this.player!=null)
            end.apply(this.player,null,-1,(short)-1);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        this.player.setAway(false);
        try
        {
          this.player.set_orientation(Byte.parseByte(param[2]));
          this.player.setCurCell(this.player.getCurMap().getCase(Short.parseShort(param[1])));
          this.player.setOldCell(this.player.getCurMap().getCase(Short.parseShort(param[1])).getId());
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;
    }
  }

  /**
   * Fin Other *
   */

  //v2.6 - only used in logging in system
  @SuppressWarnings("deprecation")
public void kick()
  {
    if(this.session.isConnected())
      this.session.close();
  }

  //v2.8 - kick system fix v2
  public void disconnect()
  {
	 
	  if (this.account != null && this.player != null)
          this.account.disconnect(this.player);
  }


  //v2.6 - kick system fix
  @SuppressWarnings("deprecation")
public void kickSession()
  {
    if(this.session.isConnected()&&!this.session.isClosing())
      this.session.close();
  }

  public void addAction(GameAction GA)
  {
    actions.put(GA.id,GA);
    if(GA.actionId==1)
      walk=true;

    if(Config.getInstance().debugMode)
      Main.world.logger.error("Game > Create action id : "+GA.id);
    if(Config.getInstance().debugMode)
      Main.world.logger.error("Game > Packet : "+GA.packet);
  }

  public synchronized void removeAction(GameAction GA)
  {
	  if(this.player.getGameClient() == null)
		  return;
    if(GA.actionId==1)
      walk=false;
    if(Config.getInstance().debugMode)
      Main.world.logger.error("Game >  Delete action id : "+GA.id);
    actions.remove(GA.id);

    if(actions.get(-1)!=null&&GA.actionId==1)//Si la queue est pas vide
    {
      //et l'actionID remove = Deplacement
      //int cellID = -1;
      String packet=actions.get(-1).packet.substring(5);
      int cell=Integer.parseInt(packet.split(";")[0]);
      ArrayList<Integer> list=null;
      try
      {
        list=PathFinding.getAllCaseIdAllDirrection(cell,this.player.getCurMap());
        //cellID = Pathfinding.getNearestCellAroundGA(this.player.getCurMap(), cell, this.player.getCurCell().getId(), null);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }

      //cellID == this.player.getCurCell().getId()
      if((list!=null&&list.contains((int)this.player.getCurCell().getId()))||distPecheur())// et on verrifie si le joueur = cellI
        this.player.getGameClient().gameAction(actions.get(-1));// On renvois comme demande
      //Risquà¯Â¿Â½ mais bon pas le choix si on veut pas à¯Â¿Â½tre emmerder avec les blà¯Â¿Â½s. Parser le bon type ?
      //this.player.getGameClient().gameAction(actions.getWaitingAccount(-1));// On renvois comme demande
      actions.remove(-1);
    }
  }

  private boolean distPecheur()
  {
    try
    {
      String packet=actions.get(-1).packet.substring(5);
      JobStat SM=this.player.getMetierBySkill(Integer.parseInt(packet.split(";")[1]));
      if(SM==null)
        return false;
      if(SM.getTemplate()==null)
        return false;
      if(SM.getTemplate().getId()!=36)
        return false;
      int dis=PathFinding.getDistanceBetween(this.player.getCurMap(),Integer.parseInt(packet.split(";")[0]),this.player.getCurCell().getId());
      int dist=JobConstant.getDistCanne(this.player.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId());
      if(dis<=dist)
        return true;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }

 
/*
  //v2.7 - Custom packet handler (Z)
  private void parseCustomPacket(String packet)
  {
    switch(packet.charAt(1))
    {
      case 'T':
        scruffemu.main.Tokenshop.open(this.getPlayer());
        break;
      case 'F':
        this.parseCustomFightPacket(packet);
        break;
      case 'M':
        if(this.player.getFight()!=null)
          SocketManager.GAME_SEND_MESSAGE(player,"Vous ne pouvez pas ouvrir le marché pendant le combat.");
        else
        {
          Hdv hdv=Main.world.getWorldMarket();
          if(hdv!=null)
          {
            String info="1,10,100;"+hdv.getStrCategory()+";"+hdv.parseTaxe()+";"+hdv.getLvlMax()+";"+hdv.getMaxAccountItem()+";-1;"+hdv.getSellTime();
            SocketManager.GAME_SEND_ECK_PACKET(this.player,11,info);
            ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.AUCTION_HOUSE_BUYING,-this.player.getCurMap().getId()); //Rà¯Â¿Â½cupà¯Â¿Â½re l'ID de la map et rend cette valeur nà¯Â¿Â½gative
            this.player.setExchangeAction(exchangeAction);
            this.player.setWorldMarket(true);
          }
        }
        break;
      case 'Z':
        this.parseFollowerPacket(packet);
        break;
    }
  }

  //v2.8 - Custom packet handler (ZZ)
  private void parseFollowerPacket(String packet)
  {
    switch(packet.charAt(2))
    {
      case 'A': //drop screen done
        if(this.getPlayer().isFollowing)
          player.teleport(this.getPlayer().getFollowerMap(),this.getPlayer().getFollowerCell());
        break;
    }
  }

  //v2.7 - Custom packet handler (ZF)
  private void parseCustomFightPacket(String packet)
  {
    switch(packet.charAt(2))
    {
      case 'T':
        this.parseTacticalModePacket(packet);
        break;
      case 'E':
        this.parseEndFightTimer();
        break;
    }
  }

  //v2.8 - Endfight teleport timer (ZFE)
  private void parseEndFightTimer()
  {
    this.getPlayer().teleportOld();
  }

  //v2.7 - Custom packet handler (ZFT)
  private void parseTacticalModePacket(String packet)
  {
    switch(packet.charAt(3))
    {
      case 'T':
        SocketManager.sendTacticalTruePacket(this);
        this.getPlayer().setTacticalMode(true);
        break;
      case 'F':
        SocketManager.sendTacticalFalsePacket(this);
        this.getPlayer().setTacticalMode(false);
        break;
    }
  }*/

  public void send(String packet)
  {
    try
    {
      this.getSession().write(packet);
      if(packet.length()<5)
          this.timeLastAct=System.currentTimeMillis();
        else if(packet.compareTo("qpong")!=0&&packet.substring(0,5).compareTo("GA0;1")!=0)
          this.timeLastAct=System.currentTimeMillis();
    }
    catch(Exception e)
    {
      Logging.getInstance().write("Error","Send fail : "+packet);
      e.printStackTrace();
    }
  }

  private void recursiveBreakingObject(BreakingObject breakingObject, final int i, int count)
  {
    if(breakingObject.isStop()||!(i<count))
    {
      if(breakingObject.isStop())
        this.player.send("Ea2");
      else
        this.player.send("Ea1");
      breakingObject.setStop(false);
      return;
    }

    new TimerWaiterPlus(() -> {
      this.player.send("EA"+(breakingObject.getCount()-i));
      ArrayList<Pair<Integer, Integer>> objects=new ArrayList<>(breakingObject.getObjects());
      this.ready();
      breakingObject.setObjects(objects);
      this.recursiveBreakingObject(breakingObject,i+1,count);
    },1000);
  }

  //v2.8 - average ping system
  private void setAveragePing(String packet)
  {
    String splitPacket=packet.substring(2);
    String[] packetArray=splitPacket.split("\\|");
    this.averagePing=Integer.parseInt(packetArray[0]);
  }

  public int getAveragePing()
  {
    return averagePing;
  }

  public boolean getCreatingCharacter()
  {
    return creatingCharacter;
  }

  public void setCreatingCharacter(boolean creatingCharacter)
  {
    this.creatingCharacter=creatingCharacter;
  }

  public boolean getCharacterSelect()
  {
    return characterSelect;
  }

  public void setCharacterSelect(boolean characterSelect)
  {
    this.characterSelect=characterSelect;
  }
  private void Shop(String packet)
  {
	  switch(packet.charAt(2))
	    {
	      case 'n':
	    	  changeName(packet);
	        break;
	      case 'c':
	    	  changement_color(packet);
		     break;
	    }
  }
  public void add_titre(String packet)
  {
		String titre = packet.substring(2);
		if(titre == null || 3 > titre.length() ||  titre.length() > 20 || titre.contains("animate") || titre.contains("moderat") || titre.contains("admin") || titre.contains("fondat")
				|| titre.contains("modo") || titre.contains("anim") || titre.contains("modo")) {
			this.player.sendMessage("Votre titre ne peut avoir que 3 lettres minimums et 20 lettres maximum");
			return;
		}
		int points=account.getPoints()-Config.getInstance().prix_titre;

	    if(points<=0)// Si le joueur n'a pas assez de point
	    {
	      int diferencia=Config.getInstance().prix_titre-account.getPoints();
	      SocketManager.GAME_SEND_MESSAGE(player,"Vous n'avez pas assez de points pour acheter, vous avez actuellement "+account.getPoints()+" points et tu as besoin "+diferencia+" plus.","FF0000");
	      SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
	      return;
	    }
	    account.setPoints(points);
	    SocketManager.GAME_SEND_MESSAGE(player,"Vous avez bien eu votre titre en échange de <b>"+Config.getInstance().prix_titre+"</b> points!","000000");
	    	
		int id = Database.getDynamics().getHeroicMobsGroups().getNextIdtitre();
		Database.getStatics().getPlayerData().logs_buy(this.player, "titre perso "+titre+" id "+id);
		int couleur = Formulas.getRandomValue(1, 5);
		World.Titre.put(id, new Titre(id, couleur, titre));
		Database.getDynamics().getHeroicMobsGroups().add_titre(id, titre, couleur);
		this.player.set_title(id);
		 SocketManager.GAME_SEND_ALTER_GM_PACKET(this.player.getCurMap(),this.player);
	  
  }
  public void changeName(String packet)
  {
    String name="";
    name+=packet.substring(2,3).toUpperCase()+packet.substring(3).toLowerCase();
    boolean isValid=true;
    if(name.length()>20||name.length()<2||name.contains("mod")||name.contains("admin")||name.contains("putain")||name.contains("administrator")||name.contains("puta")||name.contains("staff")||name.contains("owner"))
      isValid=false;
    if(isValid)
    {
      int tiretCount=0;
      char exLetterA=' ';
      char exLetterB=' ';
      for(char curLetter : name.toCharArray())
      {
        if(!(((curLetter>='a'&&curLetter<='z')||(curLetter>='A'&&curLetter<='Z'))||curLetter=='-'))
        {
          isValid=false;
          break;
        }
        if(curLetter==exLetterA&&curLetter==exLetterB)
        {
          isValid=false;
          break;
        }
        if(curLetter>='a'&&curLetter<='z')
        {
          exLetterA=exLetterB;
          exLetterB=curLetter;
        }
        if(curLetter=='-')
        {
          if(tiretCount>=1)
          {
            isValid=false;
            break;
          }
          else
          {
            tiretCount++;
          }
        }
      }
    }
    if(Database.getStatics().getPlayerData().exist(name)||!isValid)
    {
      this.player.sendMessage("Le nom saisi est déjà  utilisé ou n'est pas autorisé.");
      return;
    }
    int points=account.getPoints()-Config.getInstance().prix_name;

    if(points<=0)// Si le joueur n'a pas assez de point
    {
      int diferencia=Config.getInstance().prix_name-account.getPoints();
      SocketManager.GAME_SEND_MESSAGE(player,"Vous n'avez pas assez de points pour acheter, vous avez actuellement "+account.getPoints()+" points et tu as besoin "+diferencia+" plus.","FF0000");
      SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
      return;
    }
    account.setPoints(points);
    Database.getStatics().getPlayerData().logs_buy(this.player, "changement de nom "+this.player.getName()+" par "+name);
    SocketManager.GAME_SEND_MESSAGE(player,"Vous avez changé votre nom pour <b>"+Config.getInstance().prix_name+"</b> points!","000000");
    this.player.setName(name);
    SocketManager.GAME_SEND_STATS_PACKET(player);
	SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), player.getId());
    SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(player.getCurMap(), player);
    Database.getStatics().getPlayerData().updateName(player.getId(), name);
  }
  private void changement_color(String packet) {

		String[] infos=packet.substring(3).split("\\;");
		if (infos.length < 2)return;
		
		final int color1 = Integer.parseInt(infos[0]);
		final int color2 = Integer.parseInt(infos[1]);
		final int color3 = Integer.parseInt(infos[2]);
	    int points=account.getPoints()-Config.getInstance().prix_color;

	    if(points<=0)// Si le joueur n'a pas assez de point
	    {
	      int diferencia=Config.getInstance().prix_color-account.getPoints();
	      SocketManager.GAME_SEND_MESSAGE(player,"Vous n'avez pas assez de points pour acheter, vous avez actuellement "+account.getPoints()+" points et tu as besoin "+diferencia+" plus.","FF0000");
	      SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
	      return;
	    }
	    account.setPoints(points);
	    Database.getStatics().getPlayerData().logs_buy(this.player, "couleur");
	    SocketManager.GAME_SEND_MESSAGE(player,"Vous venez de changer votre couleur pour <b>"+Config.getInstance().prix_color+"</b> points!","000000");
			player.setColor1(color1);
			player.setColor2(color2);
			player.setColor3(color3);
			SocketManager.GAME_SEND_STATS_PACKET(player);
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), player.getId());
	        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(player.getCurMap(), player);
	        Database.getStatics().getPlayerData().updateColor(player.getId(), color1, color2, color3);
	  }
  private void changement_size(String packet) {

		;
		int size = Integer.parseInt(packet.substring(2));
		if(size < 40 && size > 120) {
		
		      return;	
		}
	    int points=account.getPoints()-Config.getInstance().prix_size;

	    if(points<=0)// Si le joueur n'a pas assez de point
	    {
	      int diferencia=Config.getInstance().prix_size-account.getPoints();
	      SocketManager.GAME_SEND_MESSAGE(player,"Vous n'avez pas assez de points pour acheter, vous avez actuellement "+account.getPoints()+" points et tu as besoin "+diferencia+" plus.","FF0000");
	      SocketManager.GAME_SEND_BUY_ERROR_PACKET(this);
	      return;
	    }
	    account.setPoints(points);
	    Database.getStatics().getPlayerData().logs_buy(this.player, "buy size "+size);
	    SocketManager.GAME_SEND_MESSAGE(player,"vous venez de changer votre couleur pour <b>"+Config.getInstance().prix_size+"</b> points!","000000");
			player.set_size(size);
			SocketManager.GAME_SEND_STATS_PACKET(player);
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), player.getId());
	        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(player.getCurMap(), player);
	  }
private void Core(String packet)
{
  switch(packet.charAt(2)) {

    case '1':
    CommandPlayer.analyse(this.player,".maitre");
      break;
    case '2':
    CommandPlayer.analyse(this.player,".tp");
     break;
    case '3':
    	      String cell = "GDZ";
    	      String plus_ou_moin = "";
    	      String places=this.getPlayer().getCurMap().getPlaces();
    	      if(places.indexOf('|')==-1||places.length()<2)
    	      {
    	        this.player.sendMessage("Les places n'ont pas ete definies");
    	        return;
    	      }
    	      if(show_cell_fight) {
    	    	  plus_ou_moin+="-" ;  
    	    	  show_cell_fight = false;
    	      }
    	      else {
    	    	  plus_ou_moin+="+" ;  
    	    	  show_cell_fight = true;
    	      }
    	    	  
    	      String team0="",team1="";
    	      String[] p=places.split("\\|");
    	      try
    	      {
    	        team0=p[0];
    	      }
    	      catch(Exception e)
    	      {
    	        // ok
    	      }

    	      try
    	      {
    	        team1=p[1];
    	      }
    	      catch(Exception e)
    	      {
    	        // ok
    	      }
    	      for(int a=0;a<=team0.length()-2;a+=2)
    	      {
    	        String code=team0.substring(a,a+2);
    	        cell+= "|"+plus_ou_moin+Main.world.getCryptManager().cellCode_To_ID(code)+";0;4";
    	      }
    	      for(int a=0;a<=team1.length()-2;a+=2)
    	      {
    	        String code=team1.substring(a,a+2);
    	        cell+= "|"+plus_ou_moin+Main.world.getCryptManager().cellCode_To_ID(code)+";0;11";
    	      }
    	      this.send(cell);
      break;
    case '4':
    	CommandPlayer.analyse(this.player,".help");
    break;
    case '5':
       CommandPlayer.analyse(this.player,".noall");
    break;
    case '6':
    	CommandPlayer.analyse(this.player,".boutique");
    break;
    case '7':
    	CommandPlayer.analyse(this.player,".hdv");
    break;
    case '8':
    	CommandPlayer.analyse(this.player,".banque");
    break;
  }
  }
public void Change_pos(String packet)
{ 
	try {
		int id = Integer.parseInt(packet.substring(2));
		Player perso = Main.world.getPlayer(id);
		if(perso != null)
		if(this.player.getFight() != null && perso.getFight() != null) {
		if(this.player.getFight().getId() == perso.getFight().getId())	{
		if(this.player.getParty() != null && perso.getParty() != null) {
		this.player.getFight().echange_pos(this.player, perso);
		}else {
		 this.player.sendMessage("Vous devez etre dans le meme groupe");	
		}
		}
		}
	}catch(Exception e) {}
	    

}
private void MorphitemChange(final String packet){
	final String verif = packet.substring(3);
	if (!verif.contains(";")){
		SocketManager.PACKET_POPUP_DEPART(this.player, "Une erreur ses produite !");
		return;
	}
	final int prix = 200;	
	final int points = this.player.getAccount().getPoints();
	if (points < prix){
		SocketManager.PACKET_POPUP_DEPART(this.player, "Tu n'a pas assez de points boutique il te faut "+prix+" points.");
		return;
	}
	final String infos[] = verif.split(";");
	final int stats = (infos.length >= 1)?Integer.parseInt(infos[0]):0;
	final int apparence = (infos.length >= 2)?Integer.parseInt(infos[1]):0;
	if (stats == apparence){
		SocketManager.PACKET_POPUP_DEPART(this.player, "Vous devez prendre deux objet différent !");
		return;
	}
	final GameObject obj = World.getGameObject(stats);
	if(obj == null){
		SocketManager.PACKET_POPUP_DEPART(this.player, "Les deux objet ne peuvent êtres lié !");
		return;
	}
	if (!this.player.hasItemGuid(obj.getGuid()) || !this.player.hasItemGuid(apparence)) {
		SocketManager.PACKET_POPUP_DEPART(this.player, "Les deux objet ne peuvent êtres lié !");
		return;
	}
	if (obj.containtTxtStats(975)){
		SocketManager.PACKET_POPUP_DEPART(this.player, "Vous ne pouvez pas encore changer cette item d'apparance !");
		return;
	}
	ObjectTemplate T = World.getGameObject(apparence).getTemplate();
	if (T == null || obj.getTemplate().getType() != T.getType() || !Constant.MimibioteItem(T.getType()) || !Constant.MimibioteItem(obj.getTemplate().getType())){
		SocketManager.PACKET_POPUP_DEPART(this.player, "Les deux objet ne peuvent êtres lié !");
		return;
	}
	this.player.getAccount().setPoints(points-prix);
	//Objects morphitem = T.createNewMorphItem(T.getID(), obj);
	if (obj.getQuantity() > 1){
		final GameObject newItem = GameObject.getCloneObjet(obj, 1);
		this.player.addObjet(newItem, false);
		World.addGameObject(newItem,true);
		newItem.addTxtStat(975, T.getName());
		newItem.addTxtStat(1100, T.getId()+"");
		obj.setQuantity(obj.getQuantity()-1);
		SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);

	}else{
		obj.addTxtStat(975, T.getName());
		obj.addTxtStat(1100, T.getId()+"");
	}
	/*if(morphitem != null && _perso.addObjet(morphitem, true)){
		_perso.get_compte().set_points(points-prix, true);
		World.addObjet(morphitem, true);
	}else{
		SocketManager.GAME_SEND_POPUP(_perso, "Une erreur ses produite !");
		return;
	}*/
	//_perso.removeItem(stats,1,true,true);
	this.player.removeItem(apparence,1,true,true);
	Database.getStatics().getObjectData().update(obj);
	SocketManager.GAME_SEND_ASK(this.player.getAccount().getGameClient(), this.player);
	SocketManager.PACKET_POPUP_DEPART(this.player, "Votre objet à bien été transformer !");
}
}
