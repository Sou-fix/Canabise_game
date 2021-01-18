package soufix.exchanger;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import soufix.main.Main;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

public class ExchangeHandler extends IoHandlerAdapter
{
  @Override
  public void sessionCreated(IoSession arg0) throws Exception
  {
	  try {
    Main.exchangeClient.setIoSession(arg0);
	  }
 	   catch(Exception e)
 	    {
 	      e.printStackTrace();
 	    }
  }

  @Override
  public void messageReceived(IoSession arg0, Object arg1) throws Exception
  {
	  try {
    String packet=ioBufferToString(arg1);
    ExchangeClient.logger.info(packet);
    ExchangePacketHandler.parser(packet);
	  }
  	   catch(Exception e)
  	    {
  	      e.printStackTrace();
  	    }
  }

  @Override
  public void messageSent(IoSession arg0, Object arg1) throws Exception
  {
	  try {
    ExchangeClient.logger.info(ioBufferToString(arg1));
	  }
 	   catch(Exception e)
 	    {
 	      e.printStackTrace();
 	    }
  }

  @Override
  public void sessionClosed(IoSession arg0) throws Exception
  {
    Main.exchangeClient.restart();
  }

  @Override
  public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception
  {
    arg1.printStackTrace();
  }

  public static String ioBufferToString(Object o)
  {
	    try
	    {
    IoBuffer ioBuffer=IoBuffer.allocate(((IoBuffer)o).capacity());
    ioBuffer.put((IoBuffer)o);
    ioBuffer.flip();


      return ioBuffer.getString(Charset.forName("UTF-8").newDecoder());
    }
    catch(CharacterCodingException e)
    {
      e.printStackTrace();
    }
    return "undefined";
  }
}

