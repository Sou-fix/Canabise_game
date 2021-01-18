package soufix.command;

import java.text.SimpleDateFormat;
import java.util.Date;

import soufix.Hdv.Hdv;
import soufix.client.Player;
import soufix.client.other.Party;
import soufix.common.SocketManager;
import soufix.game.GameClient;
import soufix.game.action.ExchangeAction;
import soufix.main.Config;
import soufix.main.Main;

public class CommandPlayer {
	private static String canal;

	static {
		CommandPlayer.canal = "Ravens";
	}

	public static boolean analyse(final Player perso, final String msg) {
		if (msg.charAt(0) != '.' || msg.charAt(1) == '.') {
			return false;
		}
		if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("points")) {
			perso.sendMessage("Vous avez <b>" + perso.getAccount().getPoints() + "</b> points boutique");
			return true;
		} 
		if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("all") || msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("monde")) {
			if (perso.noall) {
				SocketManager.GAME_SEND_MESSAGE(perso,
						"Votre canal " + CommandPlayer.canal + " est d\u00e9sactiv\u00e9.", "C35617");
				return true;
			}
            if (perso.getGroupe() == null && System.currentTimeMillis() < perso.getGameClient().getTimeLastTaverne()) {
                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() - perso.getGameClient().getTimeLastTaverne()) / 1000+" seconde(s)");
                return true;
            }
            if(msg.substring(5).compareTo("") == 0) {
            	 perso.sendMessage("Message vide");
            	return true;
            }
			perso.getGameClient().setTimeLastTaverne(System.currentTimeMillis()+15000);
			final String prefix = "[" + new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis()))
					+ "] (" + CommandPlayer.canal + ") <b><a href='asfunction:onHref,ShowPlayerPopupMenu,"
					+ perso.getName() + "'>" + perso.getName() + "</a></b> : ";
			for (final Player p : Main.world.getOnlinePlayers()) {
				if (!p.noall) {
					SocketManager.GAME_SEND_MESSAGE(p, String.valueOf(prefix) + msg.substring(5, msg.length() - 1),
							"C35617");
				}
			}
			
			return true;
		} else {
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("noall")) {
				if (perso.noall) {
					perso.noall = false;
					SocketManager.GAME_SEND_MESSAGE(perso,
							"Vous avez activ\u00e9 le canal " + CommandPlayer.canal + ".", "C35617");
				} else {
					perso.noall = true;
					SocketManager.GAME_SEND_MESSAGE(perso,
							"Vous avez d\u00e9sactiv\u00e9 le canal " + CommandPlayer.canal + ".", "C35617");
				}
				return true;
			}
			if (msg.length() > 9
					&& msg.substring(1, 10).equalsIgnoreCase("celldeblo"))
			{//180min
				if (perso.isInPrison())
					return true;
				if (perso.cantTP())
					return true;
				if (perso.getFight() != null)
					return true;
				boolean autorised = true;
				switch (perso.getCurMap().getId())
				{
				case 10700:
				case 8905:
				case 8911:
				case 8916:
				case 8917:
				case 9827:
				case 8930:
				case 8932:
				case 8933:
				case 8934:
				case 8935:
				case 8936:
				case 8938:
				case 8939:
				case 9230:
					autorised = false;
					break;
				}
				if (!autorised)
					return true;
				if (System.currentTimeMillis() < perso.getGameClient().getTimeLastTaverne()) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() - perso.getGameClient().getTimeLastTaverne()) / 1000+" seconde(s)");
	                return true;
	            }
				perso.getGameClient().setTimeLastTaverne(System.currentTimeMillis()+15000);
				perso.teleport(perso.getCurMap(), perso.getCurMap().getRandomFreeCellId());
				return true;
			}
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("staff")) {
				String message = "Liste des membres du staff connect\u00e9s :";
				boolean vide = true;
				 for(Player player : Main.world.getOnlinePlayers())
				 {
					if (player == null) {
						continue;
					}
					if (player.getGroupe() == null) {
						continue;
					}
					if (player.isInvisible()) {
						continue;
					}
					message = String.valueOf(message) + "\n- <b><a href='asfunction:onHref,ShowPlayerPopupMenu,"
							+ player.getName() + "'>[" + player.getGroupe().getName() + "] " + player.getName()
							+ "</a></b>";
					vide = false;
				}
				if (vide) {
					message = "Il n'y a aucun membre du staff connect\u00e9. Vous pouvez tout de m\u00eame allez voir sur notre Forum.";
				}
				SocketManager.GAME_SEND_MESSAGE(perso, message);
				return true;
			}
			if (msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("pass")) {
				if(perso.getAutoSkip()== true){
					perso.setAutoSkip(false);
				SocketManager.GAME_SEND_MESSAGE(perso,"Auto pass Off,","008000");
				}else{
					perso.setAutoSkip(true);
					SocketManager.GAME_SEND_MESSAGE(perso,"Auto pass On", "008000");
				}
				return true;
			}
			if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("vip")) {
				SocketManager.PACKET_POPUP_DEPART(perso, 
						"- Vos points acquis par vote sur RPG passent à 15 PB par vote.."
						+ "\n- Vos points acquis par vote sur Serveur-Prive passent à 12 PB par vote."
						+ "\n- Avoir accès à la <b>banque</b>. gratuitement pour les VIP."
						+ "\n- L'accélération <b>*3</b> de votre temps de craft."
						+ "\n- <b>10.000</b> pods de plus."
						+ "\n<b>.ipdrop</b> - Permets de récupérer le drop de vos mules."
						+ "\n Vous obtenez un bonus de 25% d'expérience à chaque combat pour toute la team"
						+ "\n Vous obtenez un bonus de 25% d'expérience métier "
						+ "\n Vous obtenez un bonus de 25% de drop à chaque combat pour toute la team"
						+ "\nAugmente la chance de reussite d'un <b>Exo</b> 1/20.");
				return true;
			}
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("maitre")) {
			
				if (System.currentTimeMillis() < perso.getGameClient().timeLasttpcommande) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+700);
				    if(perso.getParty()!=null)
				    {
				    	if(perso.getParty().getMaster() != null)
				    	if(perso.getParty().getMaster().getId() == perso.getId()) {
				    		 perso.getParty().setMaster(null);
				    		 perso.getParty().clear_groupe();

				    		 perso.sendMessage("Mode Maitre off.");
				    		 return true;
				    	}
				      perso.sendMessage("Vos etes déjà dans un groupe.");
				      return true;
				    }
				    int nbr = 0;
				    for (final Player z : perso.getCurMap().getPlayers()) {
    					if (z.getGameClient() == null) {
    						continue;
    					}
    					if(!z.getAccount().getCurrentIp().equals(perso.getAccount().getCurrentIp()))
    					continue;
    					if(perso.getId() == z.getId())continue;
    					if(z.getParty() != null)
        					continue;
    					if (perso.getParty() != null && nbr == 8)
    						continue;
    					nbr++;
    					perso.getGameClient().inviteParty("zz"+z.getName());
    					z.getGameClient().acceptInvitation();
    					SocketManager.GAME_SEND_PR_PACKET(z);
    					SocketManager.GAME_SEND_MESSAGE(z,"Vous suivez maintenant "+perso.getName()+"");
    				}
				    if(nbr == 0 || perso.getParty() == null) {
				    	SocketManager.GAME_SEND_MESSAGE(perso,"Aucune mule n'est sur la map");	
				    	return true;
				    }
				    final Party party=perso.getParty();
				    party.setMaster(perso);
				    party.moveAllPlayersToMaster(null);
				    SocketManager.GAME_SEND_MESSAGE(perso,"Vous êtes désormais le maitre de votre groupe");
				    return true;
			}
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("banque")) {
				if (perso.getFight() != null) {
					return true;
				}
				if(perso.getGameClient().show_cell_BANK) {
					GameClient.leaveExchange(perso);
					perso.getGameClient().show_cell_BANK = false;
				}
				else {
				GameClient.leaveExchange(perso);
				final int cost = perso.getBankCost();
				if(perso.getAccount().getSubscribeRemaining() == 0L)
				if (cost > 0) {
					final long playerKamas = perso.getKamas();
					final long kamasRemaining = playerKamas - cost;
					final long bankKamas = perso.getAccount().getBankKamas();
					long totalKamas = bankKamas + playerKamas;
					if (kamasRemaining < 0L) {
						if (bankKamas >= cost) {
							perso.setBankKamas(bankKamas - cost);
						} else {
							if (totalKamas < cost) {
								SocketManager.GAME_SEND_MESSAGE_SERVER(perso, "10|" + cost);
								return true;
							}
							perso.setKamas(0L);
							perso.setBankKamas(totalKamas - cost);
							SocketManager.GAME_SEND_STATS_PACKET(perso);
							SocketManager.GAME_SEND_Im_PACKET(perso, "020;" + playerKamas);
						}
					} else {
						perso.setKamas(kamasRemaining);
						SocketManager.GAME_SEND_STATS_PACKET(perso);
						SocketManager.GAME_SEND_Im_PACKET(perso, "020;" + cost);
					}
				}
				SocketManager.GAME_SEND_ECK_PACKET(perso.getGameClient(), 5, "");
				SocketManager.GAME_SEND_EL_BANK_PACKET(perso);
				perso.setAway(true);
				perso.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_BANK,0));
				perso.getGameClient().show_cell_BANK = true;
				}
				return true;
				
			}
			if (msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("boutique")) {
				soufix.main.Boutique.open(perso);
				return true;
			} 
			if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("hdv")) {
				if(perso.getFight()!=null)
				SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne pouvez pas ouvrir le marché pendant le combat.");
		        else
		        {
		          Hdv hdv=Main.world.getWorldMarket();
		          if(hdv!=null)
		          {
		            String info="1,10,100;"+hdv.getStrCategory()+";"+hdv.parseTaxe()+";"+hdv.getLvlMax()+";"+hdv.getMaxAccountItem()+";-1;"+hdv.getSellTime();
		            SocketManager.GAME_SEND_ECK_PACKET(perso,11,info);
		            ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.AUCTION_HOUSE_BUYING,-perso.getCurMap().getId()); //Rï¿½cupï¿½re l'ID de la map et rend cette valeur nï¿½gative
		            perso.setExchangeAction(exchangeAction);
		            perso.setWorldMarket(true);
		          }
		        }
				return true;
			} 
			if (msg.length() > 2 && msg.substring(1, 3).equalsIgnoreCase("tp")) {
				if(perso.getFight() != null)return true;
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
					 perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+1000);
				for (final String s : Main.world.maps_dj) {
					if (Integer.parseInt(s) == perso.getCurMap().getId()) {
						SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");	
						return true;
					}
				}
				if (perso.isInPrison())
					return true;
				if (perso.cantTP())
					return true;
				if (perso.getFight() != null)
					return true;
				boolean autorised = true;
				switch (perso.getCurMap().getId())
				{
				case 10700:
				case 8905:
				case 8911:
				case 8916:
				case 8917:
				case 9827:
				case 8930:
				case 8932:
				case 8933:
				case 8934:
				case 8935:
				case 8936:
				case 8938:
				case 8939:
				case 9230:
					autorised = false;
					break;
				}
				if (!autorised)
					return true;
				if(perso.getCurMap().getId() == 9877){
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");	
					return true;
				}
				if(perso.getParty() != null && perso.getParty().isChief(perso.getId())){
					for (final Player z :perso.getParty().getPlayers()) {
						if (z.getGameClient() == null) {
							continue;
						}
						if(z.getFight() != null)continue;
						if(!z.getAccount().getCurrentIp().equals(perso.getAccount().getCurrentIp()))
						continue;
						if(z.getId() == perso.getId())
	    					continue;
						z.teleport(perso.getCurMap(), perso.getCurCell().getId());
					}
				}else{
					SocketManager.GAME_SEND_MESSAGE(perso,"Mets toi Maître avant", "008000");	
				}
				return true;
			}
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("ipdrop")) {
				if(perso.getAccount().getSubscribeRemaining() == 0L){
	               	 SocketManager.GAME_SEND_MESSAGE(perso,"Réservé au V.I.P.","008000");	 
	                return true;	 
	                }
				 if(perso.ipDrop)
				    {
				      perso.ipDrop=false;
				      SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne gagnerez plus tous les drops de cette IP.");
				    }
				    else
				    {
				    	
				      perso.ipDrop=true;
				      SocketManager.GAME_SEND_MESSAGE(perso,"Vous allez maintenant gagner tous les drops de cette IP.");
				      for(Player z : Main.world.getOnlinePlayers())
				      {
				        if(z==null)
				          continue;
				        if(z.getAccount().getCurrentIp().equals(perso.getAccount().getCurrentIp()))
				        {
				          if(z.ipDrop && z.getId() != perso.getId()) {
				        	  z.ipDrop=false;
						      SocketManager.GAME_SEND_MESSAGE(z,"Vous ne gagnerez plus tous les drops de cette IP.");  
				          }
				        }
				      }
				    }
				    return true;
			} 
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("start") || msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("astrub")) {
				if (perso.isInPrison()) {
					return true;
				}
				if (perso.getFight() != null) {
					return true;
				}
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+1000);
				perso.teleport((short) 7411, 311);
				return true;
			}
			if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("pvp")) {
				if (perso.isInPrison()) {
					return true;
				}
				if (perso.getFight() != null) {
					return true;
				}
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+1000);
				perso.teleport((short) 952, 297);
				return true;
			}
			else {
				if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("infos")) {
					long uptime = System.currentTimeMillis() - Config.getInstance().startTime;
					final int jour = (int) (uptime / 86400000L);
					uptime %= 86400000L;
					final int hour = (int) (uptime / 3600000L);
					uptime %= 3600000L;
					final int min = (int) (uptime / 60000L);
					uptime %= 60000L;
					final int sec = (int) (uptime / 1000L);
					final int nbPlayer = Main.world.getOnlinePlayers().size();
					//final int nbPlayerIp = Main.gameServer.getPlayersNumberByIp();
					final int maxPlayer = Main.gameServer.getMaxPlayer();
					String mess = "<b>" + Config.getInstance().name + "</b>\n" + "Uptime : " + jour + "j " + hour + "h "
							+ min + "m " + sec + "s.";
					if (nbPlayer > 0) {
						mess = String.valueOf(mess) + "\nJoueurs en ligne : " + nbPlayer;
					}
				//	if (nbPlayerIp > 0)  mess = String.valueOf(mess) + "\nJoueurs uniques en ligne : " + nbPlayerIp;
					
					if (maxPlayer > 0) {
						mess = String.valueOf(mess) + "\nRecord de connexion : " + maxPlayer;
					}
					SocketManager.GAME_SEND_MESSAGE(perso, mess);
					return true;
				}
				SocketManager.GAME_SEND_MESSAGE(perso,
						"Les commandes disponibles sont  :\n<b>.infos</b> - Permet d'obtenir des informations sur le serveur."
						+ "\n<b>.start</b> - Permet de se téléporter au zaap d'Astrub."
						+ "\n<b>.pvp</b> - Permet de se téléporter à la zone pvp."
						+ "\n<b>.staff</b> - Permet de voir les membres du staff connect\u00e9s."
						+ "\n<b>.boutique</b> - Permet d'accéder à la boutique."
						+ "\n<b>.points</b> - Affiche ses points boutique.."
						+ "\n<b>.all</b> - Permet d'envoyer un message \u00e0 tous les joueurs."
						+ "\n<b>.noall</b> - Permet de ne plus recevoir les messages du canal .all."
						+ "\n<b>.celldeblo</b> - Permet de téléporter à une cellule libre si vous êtes bloqués."
						+ "\n<b>.banque</b> - Ouvrir la banque n’importe où."
						+ "\n<b>.maitre</b> - Permet de créer une escouade et d'inviter toutes tes mules dans ton groupe."
						+ "\n<b>.tp</b> - Permet de téléporter tes personnages sur ta map actuelle (hors donjons)."
						+ "\n<b>.pass</b> - Permet au joueur de passer automatiquement ses tours."
						+ "\n<b>.hdv</b> - Permet d'accéder au HDV."
						+ "\n<b>.vip</b> - Affiche les privilèges VIP."
						);
				return true;
			}
		}
	}
}
