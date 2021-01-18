package soufix.exchanger;

import soufix.client.Account;
import soufix.database.Database;
import soufix.main.Config;
import soufix.main.Main;

public class ExchangePacketHandler
{

  public static void parser(String recv)
  {
    for(String packet : recv.split("#"))
    {
      if(packet.isEmpty())
        continue;
      try
      {
        switch(packet.charAt(0))
        {
          case 'F': //Free places
            switch(packet.charAt(1))
            {
              case '?': //Required
                int i=Main.gameServer.MAX_PLAYERS-Main.world.getOnlinePlayers().size();
                Main.exchangeClient.send("F"+i);
                break;
            }
            break;
          case 'I': // allow ip
              switch(packet.charAt(1))
              {
                case 'P': //Required
                  String IP = packet.substring(2);
                 if(!Main.world.IP_ALLOW.contains(IP)) {
                	 Main.world.IP_ALLOW.add(IP);
                 }
                  break;
              }
              break;
          case 'S': //Server
            switch(packet.charAt(1))
            {
              case 'H': //Host
                switch(packet.charAt(2))
                {
                  case 'K': //Ok
                    ExchangeClient.logger.info("The login server has validated the connection.");
                    Main.gameServer.setState(1);
                    break;
                }
                break;

              case 'K': //Key
                switch(packet.charAt(2))
                {
                  case '?': //Required
                    int i=50000-Main.gameServer.getClients().size();
                    Main.exchangeClient.send("SK"+Config.getInstance().serverId+";"+Config.getInstance().key+";"+i);
                    break;

                  case 'K': //Ok
                    ExchangeClient.logger.info("The login server has accepted the connection.");
                    Main.exchangeClient.send("SH"+Config.getInstance().Ip+";"+Config.getInstance().gamePort);
                    break;

                  case 'R': //Refused
                    ExchangeClient.logger.info("The login server has refused the connection.");
                    Main.stop("Connection refused by the login");
                    break;
                }
                break;
            }
            break;

          case 'W': //Waiting
            switch(packet.charAt(1))
            {
              case 'A': //Add
            	  int id; 
            	  Account account;
            	  try {
                id=Integer.parseInt(packet.substring(2));
                if(!Main.gameServer.waitingaccount.contains(id+";")) {
                	Main.gameServer.waitingaccount.add(id+";");
                }

            	  }
           	   catch(Exception e)
           	    {
           	      e.printStackTrace();
           	    }
                break;
              case 'K': //Kick
            	  try {
                id=Integer.parseInt(packet.substring(2));
                Database.getStatics().getPlayerData().updateAllLogged(id,0);
                Database.getStatics().getAccountData().setLogged(id,0);
                account=Main.world.getAccount(id);

                if(account!=null)
                  if(account.getGameClient()!=null)
                    account.getGameClient().kick();
            	  }
              	   catch(Exception e)
              	    {
              	      e.printStackTrace();
              	    }
                break;
            }
            break;

          case 'D': // Data
            switch(packet.charAt(1))
            {

              case 'M': // Message
                String[] split=packet.substring(2).split("\\|");
                if(split.length>1)
                {
                  //String prefix="<font color='#C35617'>["+(new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis())))+"] ("+CommandPlayer.getCanal()+") ("+split[1]+") <b>"+split[0]+"</b>";
                  //final String message="Im116;"+prefix+"~"+split[2]+"</font>";

                  //Main.world.getOnlinePlayers().stream().filter(p -> p!=null&&!p.noall).forEach(p -> p.send(message.replace("%20"," ")));
                }
                break;
            }
            break;
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
  }
}