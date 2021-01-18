package soufix.command;
import java.io.Console;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.fusesource.jansi.AnsiConsole;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.game.GameHandler;
import soufix.game.scheduler.entity.WorldSave;
import soufix.main.Config;
import soufix.main.Main;


public class ConsoleInputAnalyzer implements Runnable{
	private Thread _t;

	public ConsoleInputAnalyzer()
	{
		this._t = new Thread(this,"Console");
		_t.setDaemon(true);
		_t.start();
	}
	public void run() {
		while (Main.isRunning){
			try{
			Console console = System.console();
		    String command = console.readLine();
		    evalCommand(command);
		    }catch(Exception e){}
		    finally
		    {
		    	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
		    }
		}
	}
	public void evalCommand(String command)
	{
		String[] args = command.split(" ");
		String fct =args[0].toUpperCase();
		if(fct.equals("SAVE"))
		{
			WorldSave.cast(0);
		}else if(fct.equals("BDD"))
		{
			Database.launchDatabase();
		}else if(fct.equals("perso"))
		{
			Main.world.getPlayers().stream().filter(player -> player!=null).filter(Player::isOnline).forEach(player -> {
		        Database.getStatics().getPlayerData().update(player);
		        if(player.getGuildMember()!=null)
		          Database.getDynamics().getGuildMemberData().update(player);
		      });
		}else
		if(fct.equals("EXIT"))
		{
            Main.stop("Exit by administrator");
		}else
			if(fct.equalsIgnoreCase("ANTIBUG"))
			{	
				if(Main.anti_bug){
					Main.anti_bug=false;
					sendEcho("<Anti Bug Off>");
					return;
				}
				if(!Main.anti_bug){
					Main.anti_bug=true;
					sendEcho("<Anti Bug On>");
					return;
				}
					
			}else
			if(fct.equals("GAME"))
			{
				 try {
	         Main.gameServer.acceptor = new NioSocketAcceptor();
	         Main.gameServer.acceptor.getFilterChain().addLast("codec",new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF8"),LineDelimiter.NUL,new LineDelimiter("\n\0"))));
	         Main.gameServer.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,60*10 /*10 Minutes*/);
	         Main.gameServer.acceptor.setHandler(new GameHandler());  
				Main.gameServer.acceptor.bind(new InetSocketAddress(Config.getInstance().gamePort));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	           
			}else
			if(fct.equals("SETON"))
			{
				 Main.gameServer.setState(1);
			}else
		if(fct.equalsIgnoreCase("ANNOUNCE"))
		{	
				String announce = command.substring(9);
				String PrefixConsole = "<b>Serveur</b> : ";
				SocketManager.GAME_SEND_MESSAGE_TO_ALL(PrefixConsole+announce, "ff0000");
				sendEcho("<Announce:> "+announce);
		}else
				if(fct.equalsIgnoreCase("ECHANGE"))
				{	
					Main.exchangeClient.stop();
					Main.exchangeClient.restart();
						sendEcho("<Restart game echange>");
				}else
		if(fct.equals("INFOS"))
		{
			long uptime = System.currentTimeMillis()
					- Config.getInstance().startTime;
			final int jour = (int) (uptime / 86400000L);
			uptime %= 86400000L;
			final int hour = (int) (uptime / 3600000L);
			uptime %= 3600000L;
			final int min = (int) (uptime / 60000L);
			uptime %= 60000L;
			final int sec = (int) (uptime / 1000L);
			final int nbPlayer = Main.world.getOnlinePlayers().size();
			final int nbPlayerIp = Main.gameServer.getPlayersNumberByIp();
			final int maxPlayer = Main.gameServer.getMaxPlayer();
			String mess6 = "===========\nUptime : " + jour + "j " + hour + "h "
					+ min + "m " + sec + "s.\n";
			if (nbPlayer > 0) {
				mess6 = String.valueOf(mess6) + "Joueurs en ligne : " + nbPlayer
						+ "\n";
			}
			if (nbPlayerIp > 0) {
				mess6 = String.valueOf(mess6) + "Joueurs uniques en ligne : "
						+ nbPlayerIp + "\n";
			}
			if (maxPlayer > 0) {
				mess6 = String.valueOf(mess6) + "Record de connexion : "
						+ maxPlayer + "\n";
			}
			mess6 = String.valueOf(mess6) + "===========";
			sendEcho(mess6);
		}
		else
		{
			sendError("Commande non reconnue ou incomplete.");
		}
	}

	public static void sendInfo(String msg)
	{
		AnsiConsole.out.println(msg);
	}
	public static void sendError(String msg)
	{
		AnsiConsole.out.println(msg);
	}
	public static void send(String msg)
	{
		AnsiConsole.out.println(msg);
	}
	public static void sendDebug(String msg)
	{
		//if(Ancestra.CONFIG_DEBUG)common.Console.println(msg, common.Console.ConsoleColorEnum.YELLOW);
	}
	public static void sendEcho(String msg)
	{
		AnsiConsole.out.println(msg);
	}
	
}
