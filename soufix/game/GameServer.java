package soufix.game;

import soufix.client.Player;
import soufix.main.Config;
import soufix.main.Main;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class GameServer
{

  public short MAX_PLAYERS=19999;
  public IoAcceptor acceptor;
  public final List<String> waitingaccount = new ArrayList<>();

private int sessions=0;
  private int maxConnections	= 0;

  public GameServer()
  {
    acceptor=new NioSocketAcceptor();
    acceptor.getSessionConfig().setReadBufferSize(256);
    acceptor.getFilterChain().addLast("codec",new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF8"),LineDelimiter.NUL,new LineDelimiter("\n\0"))));
    acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,40*40 /*10 Minutes*/);
    acceptor.setCloseOnDeactivation(true);
    acceptor.setHandler(new GameHandler());

  }

  public boolean start()
  {
    if(acceptor.isActive())
    {
      Main.logger.warn("Error already start but try to launch again");
      return false;
    }

    try
    {
      acceptor.bind(new InetSocketAddress(Config.getInstance().gamePort));
      Main.logger.info("Game server started on address : {}:{}",Config.getInstance().Ip,Config.getInstance().gamePort);
      return true;
    }
    catch(IOException e)
    {
    	System.exit(0);
      Main.logger.error("Error while starting game server",e);
      return false;
    }
  }

  @SuppressWarnings("deprecation")
public void stop()
  {
    if(!acceptor.isActive())
    {
      acceptor.getManagedSessions().values().stream().filter(session -> session.isConnected()||!session.isClosing()).forEach(session -> session.close());
      acceptor.dispose();
      acceptor.unbind();
      Process p;
		StringBuffer output = new StringBuffer();
		try {
			p = Runtime.getRuntime().exec("fuser -k " + Config.getInstance().gamePort + "/tcp");
			p.waitFor();
		BufferedReader reader = 
              new BufferedReader(new InputStreamReader(p.getInputStream()));

      String line = "";			
      while ((line = reader.readLine())!= null) {
      	output.append(line + "\n");
      }
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      Main.logger.error("The game server was stopped.");
    }
  }

  @SuppressWarnings("deprecation")
  public synchronized List<GameClient> getClients()
  {
    return acceptor.getManagedSessions().values().stream().filter(session -> session.getAttachment()!=null).map(session -> (GameClient)session.getAttachment()).collect(Collectors.toList());
  }
  public IoAcceptor geAcceptor() {
	return acceptor;
}
  //v2.8 - correct number
  public int getPlayersNumberByIp()
  {
	  try {
    ArrayList<String> IPs=new ArrayList<String>();
    for(Player player : Main.world.getOnlinePlayers())
      if(player.getGameClient()!=null)
      {
        boolean same=false;
        for(String IP : IPs)
          if(player.getGameClient().getAccount().getCurrentIp().equals(IP))
            same=true;
        if(same==false)
          IPs.add(player.getGameClient().getAccount().getCurrentIp());
      }
    return IPs.size();
	  }
 	   catch(Exception e)
 	    {
 	      e.printStackTrace();
 	      return 0;
 	    }
  }

  public void setState(int state)
  {
    Main.exchangeClient.send("SS"+state);
  }


  public void a()
  {
    Main.logger.warn("Unexpected behaviour detected");
  }

  public void kickAll()
  {
    for(Player player : Main.world.getOnlinePlayers())
      if(player!=null&&player.getGameClient()!=null)
      {
        player.send("M04");
        player.getGameClient().kick();
      }
  }

  public void kickAll(boolean kickGm)
  {
    for(Player player : Main.world.getOnlinePlayers())
    {
      if(player!=null&&player.getGameClient()!=null)
      {
        if(player.getGroupe()!=null&&!player.getGroupe().isPlayer()&&kickGm)
          continue;
        player.send("M04");
        player.getGameClient().disconnect();
      }
    }
  }

  public String getServerTime()
  {
    return "BT"+(new Date().getTime()+3600000*2);
  }

  public int getSessions()
  {
    return sessions;
  }

  public void setSessions(int sessions)
  {
    this.sessions=sessions;
  }
  public void newClient() {
		if (this.acceptor.getManagedSessionCount() > this.maxConnections) {
			this.maxConnections = this.acceptor.getManagedSessionCount();
		}
	}
  public int getMaxPlayer() {
		return this.maxConnections;
	}
}
