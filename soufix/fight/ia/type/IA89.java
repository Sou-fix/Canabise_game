package soufix.fight.ia.type;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractNeedSpell;
import soufix.fight.ia.util.Function;
import soufix.fight.spells.Spell;

public class IA89 extends AbstractNeedSpell
{

  private boolean boost=false, heal=true, attack=false;

  public IA89(Fight fight, Fighter fighter, byte count)
  {
    super(fight,fighter,count);
  }

  public void apply()
  {
	  try
      {
    if(!this.stop&&this.fighter.canPlay()&&this.count>0)
    {
      int time=100,maxPo=1;
      boolean action=false;
      Fighter A=Function.getInstance().getNearestFriend(this.fight,this.fighter);
      if(this.highests != null)
      for(Spell.SortStats spellStats : this.highests)
        if(spellStats.getMaxPO()>maxPo)
          maxPo=spellStats.getMaxPO();
      Fighter L=Function.getInstance().getNearestAminoinvocnbrcasemax(this.fight,this.fighter,1,maxPo+1);// pomax +1;
      Fighter enemy=Function.getInstance().getNearestEnnemy(this.fight,this.fighter);
      if(this.fighter.getCurPa(this.fight)>0&&(L!=null||A!=null)&&!this.boost)
      {
        if(Function.getInstance().buffIfPossible(this.fight,this.fighter,(L==null ? A : L),this.buffs))
        {
          time=1000;
          action=true;
          this.boost=true;
        }
      }
      if(this.fighter.getCurPa(this.fight)>0&&!action&&!this.heal)
      {
        if(Function.getInstance().HealIfPossible(this.fight,this.fighter,true,50)!=0)
        {
          time=2000;
          action=true;
          this.heal=true;
        }
      }
      if(this.highests != null)
      if(this.fighter.getCurPa(this.fight)>0&&enemy!=null&&!action)
      {
    	  
        int num=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.highests);
        if(num!=-1)
        {
          time=num;
          action=true;
        }
      }
      if(L!=null&&(L.getPdv()*100)/L.getPdvMax()>99)
        L=Function.getInstance().getNearestAminoinvocnbrcasemax(this.fight,this.fighter,1,maxPo);
      if(L!=null)
        if(L.isHide())
          L=null;
      if(this.fighter.getCurPm(this.fight)>0&&L==null)
      {
        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,A);
        if(value!=0)
        {
          time=value;
          action=true;
          L=Function.getInstance().getNearestAminoinvocnbrcasemax(this.fight,this.fighter,1,maxPo+1);// pomax +1;
          if(maxPo==1)
            L=null;
        }
      }
      if(this.fighter.getCurPm(this.fight)>0&&!action&&L!=null&&!this.heal)
      {
        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,A);
        if(value!=0)
        {
          time=value;
          action=true;
          L=Function.getInstance().getNearestinvocateurnbrcasemax(this.fight,this.fighter,1,maxPo+1);// pomax +1;
          if(maxPo==1)
            L=null;
        }
      }
      if(this.fighter.getCurPa(this.fight)>0&&!action&&!this.heal)
      {
        if(Function.getInstance().HealIfPossible(this.fight,this.fighter,true,50)!=0)
        {
          time=2000;
          action=true;
          this.heal=true;
        }
      }
      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        if(Function.getInstance().HealIfPossible(this.fight,this.fighter,false,99)!=0)
        {
          time=2000;
          action=true;
          this.heal=true;
        }
      }

      if(this.fighter.getCurPm(this.fight)>0&&!action&&this.heal||this.fighter.getCurPm(this.fight)>0&&!action&&this.boost||this.fighter.getCurPm(this.fight)>0&&!action&&this.attack)
      {
        int value=Function.getInstance().moveFarIfPossible(this.fight,this.fighter);
        if(value!=0)
          time=value;
      }
      if(this.fighter.getCurPa(this.fight)==0&&this.fighter.getCurPm(this.fight)==0||this.heal&&this.boost&&this.fighter.getCurPm(this.fight)==0)
        this.stop=true;
      addNext(this::decrementCount,time);
    }
    else
    {
      this.stop=true;
    }
      }
      catch(Exception e)
      {
        e.printStackTrace();
 
      }
  }
}