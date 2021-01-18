package soufix.database.passive;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import soufix.database.Database;
import soufix.database.passive.data.AccountData;
import soufix.database.passive.data.AreaData;
import soufix.database.passive.data.BanIpData;
import soufix.database.passive.data.CommandData;
import soufix.database.passive.data.GroupData;
import soufix.database.passive.data.GuildData;
import soufix.database.passive.data.HouseData;
import soufix.database.passive.data.MountData;
import soufix.database.passive.data.MountParkData;
import soufix.database.passive.data.ObjectData;
import soufix.database.passive.data.ObvejivanData;
import soufix.database.passive.data.PetData;
import soufix.database.passive.data.PlayerData;
import soufix.database.passive.data.PubData;
import soufix.database.passive.data.QuestPlayerData;
import soufix.database.passive.data.ServerData;
import soufix.database.passive.data.StakeData;
import soufix.database.passive.data.TrunkData;
import soufix.main.Config;
import soufix.main.Main;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.LoggerFactory;

public class StaticsDatabase
{
  //connection
  private HikariDataSource dataSource;
  private Logger logger=(Logger)LoggerFactory.getLogger(StaticsDatabase.class);
  //data
  private AccountData accountData;
  private CommandData commandData;
  private PlayerData playerData;
  private ServerData serverData;
  private BanIpData banIpData;
  private AreaData areaData;
  private GroupData groupData;
  private GuildData guildData;
  private HouseData houseData;
  private TrunkData trunkData;
  private MountData mountData;
  private MountParkData mountParkData;
  private ObjectData objectData;
  private ObvejivanData obvejivanData;
  private PubData pubData;
  private PetData petData;
  private QuestPlayerData questPlayerData;
  private StakeData stakeData;

  public void initializeData()
  {
    this.accountData=new AccountData(this.dataSource);
    this.commandData=new CommandData(this.dataSource);
    this.playerData=new PlayerData(this.dataSource);
    this.serverData=new ServerData(this.dataSource);
    this.banIpData=new BanIpData(this.dataSource);
    this.areaData=new AreaData(this.dataSource);
    this.guildData=new GuildData(this.dataSource);
    this.groupData=new GroupData(this.dataSource);
    this.houseData=new HouseData(this.dataSource);
    this.trunkData=new TrunkData(this.dataSource);
    this.mountData=new MountData(this.dataSource);
    this.mountParkData=new MountParkData(this.dataSource);
    this.objectData=new ObjectData(this.dataSource);
    this.obvejivanData=new ObvejivanData(this.dataSource);
    this.pubData=new PubData(this.dataSource);
    this.petData=new PetData(this.dataSource);
    this.questPlayerData=new QuestPlayerData(this.dataSource);
    this.stakeData=new StakeData(this.dataSource);
  }

  public boolean initializeConnection()
  {
    try
    {
      logger.setLevel(Level.ERROR);
      logger.trace("Reading database config");

      HikariConfig config=new HikariConfig();
      config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
      config.addDataSourceProperty("serverName",Config.getInstance().loginHostDB);
      config.addDataSourceProperty("port",Config.getInstance().loginPortDB);
      config.addDataSourceProperty("databaseName",Config.getInstance().loginNameDB);
      config.addDataSourceProperty("user",Config.getInstance().loginUserDB);
      config.addDataSourceProperty("password",Config.getInstance().loginPassDB);
      config.setAutoCommit(true); // AutoCommit, c'est cool
      config.setMaximumPoolSize(20);
      config.setMinimumIdle(1);
      this.dataSource=new HikariDataSource(config);

      if(!Database.tryConnection(this.dataSource))
      {
        logger.error("Please check your username and password and database connection");
        Main.stop("statics try connection failed");
        return false;
      }
      logger.info("Database connection established");
      initializeData();
      logger.info("Database data loaded");
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public HikariDataSource getDataSource()
  {
    return dataSource;
  }

  public AccountData getAccountData()
  {
    return accountData;
  }

  public CommandData getCommandData()
  {
    return commandData;
  }

  public PlayerData getPlayerData()
  {
    return playerData;
  }

  public ServerData getServerData()
  {
    return serverData;
  }

  public BanIpData getBanIpData()
  {
    return banIpData;
  }

  public AreaData getAreaData()
  {
    return areaData;
  }

  public GuildData getGuildData()
  {
    return guildData;
  }

  public GroupData getGroupData()
  {
    return groupData;
  }

  public HouseData getHouseData()
  {
    return houseData;
  }

  public TrunkData getTrunkData()
  {
    return trunkData;
  }

  public MountData getMountData()
  {
    return mountData;
  }

  public MountParkData getMountParkData()
  {
    return mountParkData;
  }

  public ObjectData getObjectData()
  {
    return objectData;
  }

  public ObvejivanData getObvejivanData()
  {
    return obvejivanData;
  }

  public PubData getPubData()
  {
    return pubData;
  }

  public PetData getPetData()
  {
    return petData;
  }

  public QuestPlayerData getQuestPlayerData()
  {
    return questPlayerData;
  }

  public StakeData getStakeData()
  {
    return stakeData;
  }
}
