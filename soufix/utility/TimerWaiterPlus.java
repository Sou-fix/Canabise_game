package soufix.utility;

import java.util.Timer;
import java.util.TimerTask;

public class TimerWaiterPlus
{
  Timer timer;

  public TimerWaiterPlus(Runnable runnable, long time)
  {
    timer=new Timer();
    TimerTask delayedThreadStartTask=new TimerTask()
    {
      public void run()
      {
        new Thread(runnable).start();
        timer.cancel();
      }
    };
    timer.schedule(delayedThreadStartTask,time);
  }
}