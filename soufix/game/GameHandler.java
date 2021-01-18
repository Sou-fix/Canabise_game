package soufix.game;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import soufix.main.Config;
import soufix.main.Logging;
import soufix.main.Main;

public class GameHandler extends IoHandlerAdapter
{

  @SuppressWarnings("deprecation")
  @Override
  public void sessionCreated(IoSession arg0) throws Exception
  {
	  Main.gameServer.setSessions(Main.gameServer.getSessions()+1);
	  Main.world.logger.info("Session "+arg0.getId()+" created");
	  if(Main.world.IP_ALLOW.contains(arg0.getRemoteAddress().toString().substring(1).split("\\:")[0])) {
      arg0.setAttachment(new GameClient(arg0));
      Main.refreshTitle();
	  }else {
		  if(arg0.isConnected())
		  arg0.close();	
		  Logging.getInstance().write("DDOS","IP NOT ALLOWED CONX GAME SANS REALM "+arg0.getRemoteAddress().toString().substring(1).split("\\:")[0]);
	  }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void messageReceived(IoSession arg0, Object arg1) throws Exception
  {
	  final String message=(String)arg1;
    IoBuffer.allocate(message.length());
    GameClient client=(GameClient)arg0.getAttachment();
    String packet=(String)arg1;
    String[] s=packet.split("\n");
    Integer i = new Integer(0);
    do {
        client.parsePacket(s[i]);
        if(Config.getInstance().debugMode)
            Main.world.logger.trace((client.getPlayer() == null ? "" : client.getPlayer().getName()) + " <-- " + s[i]);
        i++;
    } while (i == s.length - 1);
  }

  @Override
  public void messageSent(IoSession arg0, Object arg1) throws Exception
  {
	  final String message=(String)arg1;
	    IoBuffer.allocate(message.length());
  }

  @SuppressWarnings("deprecation")
  @Override
  public void sessionClosed(IoSession arg0) throws Exception
  {
	   Main.world.logger.info("Session "+arg0.getId()+" closed");
	   Main.gameServer.setSessions(Main.gameServer.getSessions()-1);
    GameClient client=(GameClient)arg0.getAttachment();
    if(client!=null)
      client.disconnect();
    arg0.getWriteRequestQueue().dispose(arg0);
    Main.refreshTitle();
  }

  @Override
  public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception
  {
	  if(arg1==null)
	      return;
	    if(arg1.getMessage()!=null&&(arg1 instanceof org.apache.mina.filter.codec.RecoverableProtocolDecoderException||arg1.getMessage().startsWith("Une connexion ")||arg1.getMessage().startsWith("Connection reset by peer")||arg1.getMessage().startsWith("Connection timed out")
	    		||arg1.getMessage().startsWith("Connexion ré-initialisée par le correspondant") ||arg1.getMessage().startsWith("Connexion terminée par expiration du délai d'attente") ||arg1.getMessage().startsWith("Aucun chemin d'accès pour atteindre l'hôte cible")
	   		||arg1.getMessage().startsWith("Le réseau n'est pas accessible")))
	     return;
    arg1.printStackTrace();
    Main.world.logger.warn("Exception connexion client : "+arg1.getMessage());
    //this.kick(arg0);
  }
  
@SuppressWarnings("deprecation")
public void inputClosed(IoSession ioSession) throws Exception
  {
	ioSession.close(true);
  }

  @Override
  public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception
  {
    Main.world.logger.info("Session "+arg0.getId()+" idle");
  }

  @Override
  public void sessionOpened(IoSession arg0) throws Exception
  {
    Main.world.logger.info("Session "+arg0.getId()+" opened");
  }

}
