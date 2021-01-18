package soufix.fight.turn;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.main.Constant;
import soufix.utility.TimerWaiterPlus;

public class Turn implements Runnable
{
  private final Fight fight;
  private final Fighter fighter;
  private final long start;
  private volatile boolean stop=false;

  public Turn(Fight fight, Fighter fighter)
  {
    this.fight=fight;
    this.fighter=fighter;
    new TimerWaiterPlus(this,Constant.TIME_BY_TURN+2000);
    //TimerWaiter.addNext(this,Constant.TIME_BY_TURN+2000,TimeUnit.MILLISECONDS,TimerWaiter.DataType.FIGHT);
    this.start=System.currentTimeMillis();
  }

  public long getStartTime()
  {
    return start;
  }

  public void stop()
  {
    stop=true;
  }

  public void run()
  {
    if(this.stop||this.fighter.isDead())
    {
      this.stop();
      return;
    }

    if(this.fight.getOrderPlaying()==null)
    {
      this.stop();
      return;
    }

    if(this.fight.getOrderPlaying().get(this.fight.getCurPlayer())==null)
    {
      this.stop();
      return;
    }

    if(this.fight.getOrderPlaying().get(this.fight.getCurPlayer())!=this.fighter)
    {
      this.stop();
      return;
    }
    this.fight.endTurn(false);
  }
}