package soufix.database.active;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import soufix.database.DAO;
import soufix.database.Database;
import soufix.main.Logging;
import soufix.main.Main;

import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.LoggerFactory;

import java.sql.*;

public abstract class AbstractDAO<T> implements DAO<T>
{

  protected final Object locker=new Object();
  private HikariDataSource dataSource;
  protected Logger logger=(Logger)LoggerFactory.getLogger(AbstractDAO.class+" - [D]");

  public AbstractDAO(HikariDataSource dataSource)
  {
    this.setDataSource(dataSource);
    logger.setLevel(Level.ERROR);
  }

  protected void execute(String query)
  {
    synchronized(locker)
    {
      Connection connection=null;
      Statement statement=null;
      try
      {
        connection=getDataSource().getConnection();
        statement=connection.createStatement();
        statement.execute(query);
        logger.debug("SQL request executed successfully {}",query);
      }
      catch(SQLException e)
      {
        logger.error("Can't execute SQL Request :"+query,e);
      } finally
      {
        close(statement);
        close(connection);
      }
    }
  }

  protected void execute(PreparedStatement statement)
  {
    synchronized(locker)
    {
      Connection connection=null;
      try
      {
        connection=statement.getConnection();
        statement.execute();
        logger.debug("SQL request executed successfully {}",statement.toString());
      }
      catch(SQLException e)
      {
        logger.error("Can't execute SQL Request :"+statement.toString(),e);
      } finally
      {
        close(statement);
        close(connection);
      }
    }
  }

  protected Result getData(String query)
  {
    synchronized(locker)
    {
      Connection connection=null;
      try
      {
        if(!query.endsWith(";"))
          query=query+";";
        connection=getDataSource().getConnection();
        Statement statement=connection.createStatement();
        Result result=new Result(connection,statement.executeQuery(query));
        logger.debug("SQL request executed successfully {}",query);
        return result;
      }
      catch(SQLException e)
      {
        logger.error("Can't execute SQL Request :"+query,e);
      }
      return null;
    }
  }

  protected PreparedStatement getPreparedStatement(String query) throws SQLException
  {
    try
    {
      Connection connection=getDataSource().getConnection();
      return connection.prepareStatement(query);
    }
    catch(SQLException e)
    {
      logger.error("Can't getWaitingAccount datasource connection",e);
      getDataSource().close();
      if(!Database.getDynamics().initializeConnection())
        Main.stop("PreparedStatement failed");
      return null;
    }
  }

  protected void close(PreparedStatement statement)
  {
    if(statement==null)
      return;
    try
    {
      if(!statement.isClosed())
      {
        statement.clearParameters();
        statement.close();
      }
    }
    catch(Exception e)
    {
      logger.error("Can't close statement",e);
    }
  }

  protected void close(Connection connection)
  {
    if(connection==null)
      return;
    try
    {
      if(!connection.isClosed())
      {
        connection.close();
        logger.trace("{} released",connection);
      }
    }
    catch(Exception e)
    {
      logger.error("Can't close connection",e);
    }
  }

  protected void close(Statement statement)
  {
    if(statement==null)
      return;
    try
    {
      if(!statement.isClosed())
        statement.close();
    }
    catch(Exception e)
    {
      logger.error("Can't close statement",e);
    }
  }

  protected void close(ResultSet resultSet)
  {
    if(resultSet==null)
      return;
    try
    {
      if(!resultSet.isClosed())
        resultSet.close();
    }
    catch(Exception e)
    {
      logger.error("Can't close resultSet",e);
    }
  }

  protected void close(Result result)
  {
    if(result!=null)
    {
      if(result.resultSet!=null)
        close(result.resultSet);
      if(result.connection!=null)
        close(result.connection);
      logger.trace("Connection {} has been released",result.connection);
    }
  }

  protected void sendError(String msg, Exception e)
  {
    e.printStackTrace();
    logger.error("Error dynamics database "+msg+" : "+e.getMessage());
    Logging.getInstance().write("EUURE2","Error statics database "+msg+" : "+e.getMessage());
  }

  public HikariDataSource getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(HikariDataSource dataSource)
  {
    this.dataSource = dataSource;
  }

  protected class Result
  {
    public final Connection connection;
    public final ResultSet resultSet;

    protected Result(Connection connection, ResultSet resultSet)
    {
      this.connection=connection;
      this.resultSet=resultSet;
    }
  }
}
