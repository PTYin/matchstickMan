package robot;

import java.util.*;

public class Archimage
{
    private Info now;
    private long nowtime;
    private boolean strong;
    private boolean going;
    private boolean naturalGoing;

    private long time;
    private boolean wait;
    private double point;

    private double state;
    private double situation;

    final double inf=1e30;
    final double eps=1e-10;

    final Random rand=new Random();
    final Calculator calc=new Calculator();
    final String[] key={"up","left","right","down","switch","attack","move","drop","rotate"};

    public Archimage(){
        wait=false;
        point=1e30;
        going=true;
    }

    public ArrayList<String> operate(Info now,boolean strong) {
        this.now=now;
        this.strong=strong;
        nowtime=System.currentTimeMillis();

        ArrayList<String> s=new ArrayList<String>();

        if(strong) {
            calc.import_time(nowtime);
            calc.import_info(now);

            calc.calc_speed();
            calc.calc_time();
            calc.calc_dis();

            if(now.B.hplocked) calc.calc_move();
        }
        /*
        System.out.println("A.x = "+now.A.x);
        System.out.println("B.isdrop = "+now.B.isdrop);
        System.out.println("wait = "+wait);
        System.out.println("time = "+time);
        System.out.println("nowtime = "+nowtime);
        System.out.println();
        */
        if(now.B.isdrop) {
            if(!wait &&( point>3000 || nowtime-time>1300 )) {
                wait=true;
                time = nowtime;
            }
            else if(wait && time+500 <= nowtime) {
                point=now.A.x;
                wait=false;
            }
        }
        else point=inf;

        judgeState();
        judgeSituation();

        if((s=naturalReact()).size()!=0 || (s=attack()).size()!=0) {
            going=(rand.nextInt(4)==0);
            naturalGoing=rand.nextBoolean();
            return s;
        }

        double dis=Math.abs(now.A.x-now.B.x);

        if(going&&!strong) {
            if(!now.A.isrotate) s.add("rotate");
            else return go(false);
        }
        else if(situation<0.1618 || dis > 888+rand.nextInt(200)) {
            if(dis<1300 && rand.nextInt(400) < 1300-dis) return move(true);
            else if(dis>1300 && rand.nextInt(250) > 1550-dis) s.add("drop");
            else s = go(rand.nextDouble() > 0.1618 * state + 0.1618 * situation);
        }
        else {
            if(!now.A.isdrop && rand.nextInt(5)==1) s.add("drop");
            else {
                if(!now.A.isrotate) s.add("rotate");
                else {
                    if(now.A.x>1730 || now.A.x<80) naturalGoing^=true;
                    return go(naturalGoing);
                }
            };
        }

        return s;
    }
    private void judgeState() {
        int p1=-10,p2=10,p3=4,p4=-4,p5=10;
        double total=p1+p2+p3+p4+p5;
        double val=now.B.hp/100*p1+now.A.hp/100*p2+now.A.ballnumber/4*p3+now.B.ballnumber/4*p4+rand.nextDouble()*p5;
        //val越大则情况越乐观
        state=val/total;
        state=Math.min(state,1);
        state=Math.max(state,1e-30);
    }
    private void judgeSituation() {
        int p1=-4,p2=4,p3=10,p4=-10,p5=5;
        double total=p1+p2+p3+p4+p5;
        double val=now.B.hp/100*p1+now.A.hp/100*p2+now.A.ballnumber/4*p3+now.B.ballnumber/4*p4+rand.nextDouble()*p5;
        //val越大则情况越乐观
        situation=val/total;
        situation=Math.min(situation,1);
        situation=Math.max(situation,1e-30);
    }
    private ArrayList<String> naturalReact() {
        ArrayList<String> s=new ArrayList<>();

        if(!now.A.isdrop) {
            double tt = (0.618 * situation + 0.382 * state);
            tt = tt * tt; tt /= 2;
            if (situation > 1e-29 && rand.nextDouble() < tt ) {
                //System.out.println("tt = "+tt);
                //System.out.println("state = "+state);
                //System.out.println("situation = "+situation);
                return s;
            }
        }

        boolean go=true,move=true,defend=true;

        //System.out.println("point: "+point);
        //System.out.println("abs(now.A.x-point): "+Math.abs(now.A.x-point));
        if(now.B.isdrop && Math.abs(now.A.x-point)<200) defend=false;
        if(near()) defend=false;
        if(ball_danger()){
            if(defend) return defend();
            go=false;
        }
        if(be_rotated()) go=false;

        if(defend && go && move) return s;
        if(!defend&&go&&move)
            return rand.nextBoolean()?go(true):move(true);
        while(true) {
            int t=rand.nextInt(3);
            if(t==0&&go) return go(true);
            else if(t==1&&move) return move(true);
            else if(t==2&&defend) return defend();
        }
    }
    private ArrayList<String> attack() {
        ArrayList<String> s=new ArrayList<>();

        if(now.A.ballnumber==0) return s;
        if(now.B.hplocked)  {
            if(!strong || now.A.isattacking || now.A.isdrop) return s;
            double dis=Math.abs(calc.get_pos()-now.A.x);
            double tt=calc.get_ballSpeed()*(calc.get_time()-nowtime);
            if(tt > dis) return ballattack();
        }

        //距离近时，应当直接攻击
        double dis=Math.abs(now.A.x-now.B.x);
        if(dis<200 && !now.A.isattacking && !now.A.isdrop) {
            if(rand.nextInt(200)<dis) return ballattack();
            return go(false);
        }

        if(now.B.isdefending && !now.A.isdrop) {
            if(dis > 888+66*now.B.ballnumber+rand.nextInt((int)((1-state)*235)+1)) {
                s.add("drop");
                return s;
            }
        }
        if(now.B.isdrop && !now.A.isdrop) {
            if(!now.A.isattacking)
            {
                if (!now.B.isdefending)
                {
                    if (dis < 450) return ballattack();
                    if (dis < 600) return go(false);
                    if (dis < 1200 - 66 * now.B.ballnumber + rand.nextInt((int) ((1 - state) * 255) + 1))
                        return move(false);
                }
                else
                {
                    if (dis < 233)
                    {
                        if (rand.nextInt(233) >= dis) return ballattack();
                        return go(false);
                    }
                    if (dis < 300) return go(false);
                    if (dis < 1200 - 66 * now.B.ballnumber + rand.nextInt((int) ((1 - state) * 255) + 1))
                        return move(false);
                }
            }
            if(dis > 888+66*now.B.ballnumber+rand.nextInt((int)((1-state)*235)+1)) {
                s.add("drop");
                return s;
            }
        }

        if(!now.A.isattacking && !now.A.isdrop && now.B.ballnumber<now.A.ballnumber && now.A.hp>=now.B.ballnumber*10) {
            int tt=Math.max((int)(0.6618/situation),(int)(0.3382/state));
            if(tt<1000000000) tt=(int)(0.6618/situation+0.3382/state);
            if(tt==0 || rand.nextInt(tt)==0) {
                if(rand.nextDouble()>dis/1000 && !now.B.isdefending) return ballattack();
                if(dis > 700) return move(false);
                if(dis > 200) return go(false);
                if(rand.nextInt(200) >= dis) return ballattack();
                return go(false);
            }
        }
        return s;
    }
    private boolean near() {
        if(Math.abs(now.A.x-now.B.x)<200) return true;
        return false;
    }
    private boolean ball_danger() {
        double tt;
        tt=Math.abs(now.A.x-now.ball[0][0]);
        tt=Math.min(tt,Math.abs(now.A.x-now.ball[1][0]));
        tt=Math.min(tt,Math.abs(now.A.x-now.ball[2][0]));
        tt=Math.min(tt,Math.abs(now.A.x-now.ball[3][0]));
        return tt-225<-eps;
    }
    private boolean be_rotated() {
        if(!now.B.isrotate) return false;
        return Math.abs(now.A.x-now.B.x)<200;
    }
    private ArrayList<String> go(boolean away) {
        ArrayList<String> s=new ArrayList<>();
        boolean right=true;
        if(away) right=now.B.x<now.A.x;
        else right=now.B.x>now.A.x;
        if(right && now.A.x > 1730) right^=true;
        else if(!right && now.A.x < 80) right^=true;
        s.add(right?"right":"left");
        return s;
    }
    private ArrayList<String> move(boolean away) {
        ArrayList<String> s=new ArrayList<>();
        boolean right=true;
        if(away) right=now.B.x<now.A.x;
        else right=now.B.x>now.A.x;
        if(right && now.A.x > 1450) right^=true;
        else if(!right && now.A.x < 350) right^=true;
        s.add(right?"right":"left");
        s.add("move");
        return s;
    }
    private ArrayList<String> defend() {
        double a,b,t;
        for(int i=0;i<3;i++)
            for(int j=i+1;j<4;j++) {
                a=Math.abs(now.ball[i][0]-now.A.x);
                b=Math.abs(now.ball[j][0]-now.A.x);
                if(a>b) {
                    t=now.ball[i][0];now.ball[i][0]=now.ball[j][0];now.ball[j][0]=t;
                    t=now.ball[i][1];now.ball[i][1]=now.ball[j][1];now.ball[j][1]=t;
                }
            }
        ArrayList<String> s=new ArrayList<String>();
        if(now.ball[0][0]>10000)
            s.add(now.B.x-now.A.x>eps?"right":"left");
        else {
            s.add(now.ball[0][0]>now.A.x?"right":"left");
        }
        s.add("down");
        return s;
    }
    private ArrayList<String> ballattack() {
        ArrayList<String> s=new ArrayList<>();
        boolean right=now.B.x>now.A.x;;
        if(!now.A.facingright && right) s.add("right");
        else if(now.A.facingright && !right) s.add("left");
        s.add("attack");
        return s;
    }
}