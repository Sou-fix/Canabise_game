package soufix.exchanger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import soufix.main.Config;
import soufix.main.Main;

import org.apache.mina.core.session.IoSession;
import org.slf4j.LoggerFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

public class ExchangeClient
{
  public static Logger logger=(Logger)LoggerFactory.getLogger(ExchangeClient.class);

  private IoSession ioSession;
  private ConnectFuture connectFuture;
  private IoConnector ioConnector=new NioSocketConnector();

  public ExchangeClient()
  {
    this.ioConnector.setHandler(new ExchangeHandler());
    ExchangeClient.logger.setLevel(Level.ERROR);
  }

  public void setIoSession(IoSession ioSession)
  {
    this.ioSession=ioSession;
  }

  public IoSession getIoSession()
  {
    return ioSession;
  }

  public ConnectFuture getConnectFuture()
  {
    return connectFuture;
  }

  public void initialize()
  {
    try
    {
      this.connectFuture=this.ioConnector.connect(new InetSocketAddress(Config.getInstance().exchangeIp,Config.getInstance().exchangePort));
    }
    catch(Exception e)
    {
      ExchangeClient.logger.error("The game server don't found the login server. Exception : "+e.getMessage());
      try
      {
        Thread.sleep(2000);
      }
      catch(Exception ignored)
      {
      }
      return;
    }

    try
    {
      Thread.sleep(3000);
    }
    catch(Exception ignored)
    {
    }

    if(!ioConnector.isActive())
    {
      if(!Main.isRunning)
        return;

      ExchangeClient.logger.error("Try to connect to the login server..");
      restart();
      return;
    }
    ExchangeClient.logger.info("The exchange client was connected on address : "+Config.getInstance().exchangeIp+":"+Config.getInstance().exchangePort);
  }

  public void restart()
  {
    if(!Main.isRunning)
      return;

    ExchangeClient.logger.error("The login server was not found..");

    this.stop();
    this.connectFuture=null;
    this.ioConnector=new NioSocketConnector();
    this.ioConnector.setHandler(new ExchangeHandler());
    this.initialize();
  }

  @SuppressWarnings("deprecation")
public void stop()
  {
    if(this.ioSession!=null)
      this.ioSession.close();
    if(this.connectFuture!=null)
      this.connectFuture.cancel();

    this.connectFuture=null;
    this.ioConnector.dispose();
    ExchangeClient.logger.info("The exchange client was stopped.");
  }

  public void send(String packet)
  {
	  try {
    if(ioSession!=null&&!ioSession.isClosing()&&ioSession.isConnected())
      ioSession.write(StringToIoBuffer(packet));
  }
	   catch(Exception e)
	    {
	      e.printStackTrace();
	    }
  }
  public static IoBuffer StringToIoBuffer(String packet)
  {
	  try {
    IoBuffer ioBuffer=IoBuffer.allocate(30000);
    ioBuffer.put(packet.getBytes());
    return ioBuffer.flip();
  }
	   catch(Exception e)
	    {
	      e.printStackTrace();
	    }
	return null;
  }
}
