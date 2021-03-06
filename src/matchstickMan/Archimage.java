package matchstickMan;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Random;

public class Archimage extends Robot
{
    private boolean dragonRight, isIncreasing = false, isDamaging = false;
    private Timeline increaseTimeline = new Timeline(new KeyFrame(Duration.millis(200), event1 ->
    {
        statusController.increaseHP(0.25);
    })),damageTimeline, magic = new Timeline();
    private int mode;
    private ImageView dragonView = new ImageView(), increaseHP = new ImageView("matchstickMan/image/increaseHP.gif"), damageHP = new ImageView("matchstickMan/image/damageHP.gif");
    private BooleanProperty superFrozen = new SimpleBooleanProperty();
    public Archimage(boolean facingRight, Color skin, int mode)
    {
        super(facingRight, skin);
        superFrozen.set(false);
        superFrozen.addListener((frozen, old, now) ->
        {
            if(now)
            {
                transfere.stop();
                shadowMove.stop();
                statusController.setFrozen(true);
                statusController.setHPLocked(true);
            }
            else
            {
                statusController.setFrozen(false);
                statusController.setHPLocked(false);
                transfere.play();
            }
        });
        increaseHP.setScaleX(1.5);
        increaseHP.setScaleY(1.5);
        increaseHP.setLayoutX(-33);
        increaseHP.setLayoutY(-20);
        hp.addListener((hp, old, now) ->
        {
            if((double)old>0&&(double)now<(double)old || (Double)hp.getValue() == 100)
            {
                isIncreasing = false;
                getChildren().remove(increaseHP);
                increaseTimeline.stop();
            }
        });
        this.mode = mode;
    }
    @Override
    public void setOpponent(MatchstickMan opponent)
    {
        this.opponent = opponent;
        damageTimeline = new Timeline(new KeyFrame(Duration.millis(200), event ->
        {
            opponent.hp.set(opponent.hp.get()-0.25);
        }));
        damageTimeline.setOnFinished(event ->
        {
            isDamaging = false;
            opponent.getChildren().remove(damageHP);
        });
        damageHP.setScaleX(1.2);
        damageHP.setScaleY(1.8);
        damageHP.setLayoutX(-50);
        damageHP.setLayoutY(40);
    }
    private class OnFinished implements EventHandler<ActionEvent>
    {
        ImageView monsterView;
        boolean flag = true;
        @Override
        public void handle(ActionEvent event)
        {
            if(flag&&!opponent.shadowMove.isMoving&&Math.abs(opponent.getLayoutX()-monsterView.getLayoutX()-1.9*MatchstickMan.ratio)<2*MatchstickMan.ratio && Platform.field-opponent.getLayoutY()<MatchstickMan.ratio)
            {
                flag = false;
                monsterView.setLayoutX(0x3fffffff);
                platform.getChildren().remove(monsterView);
                opponent.statusController.damageHP(5);
                if(!isIncreasing&&hp.get()>0 && hp.get()<100)
                {
                    isIncreasing = true;
                    getChildren().add(increaseHP);
                    increaseTimeline.setCycleCount(Timeline.INDEFINITE);
                    increaseTimeline.play();
                }
            }
        }
    }
    public void summon(int step)
    {
        if(step == 1)
        {
            superFrozen.set(true);
            ImageView effect, wing;
            if(!facingRight.get())
            {
                wing = new ImageView("matchstickMan/image/wing4.gif");
                wing.setScaleX(1.5);
                wing.setScaleY(1.5);
                wing.setLayoutX(-49);
                wing.setLayoutY(-15);
                effect = new ImageView("matchstickMan/image/summon.gif");
                effect.setLayoutX(-200);
            }
            else
            {
                wing = new ImageView("matchstickMan/image/wing3.gif");
                wing.setScaleX(1.5);
                wing.setScaleY(1.5);
                wing.setLayoutX(-124);
                wing.setLayoutY(-15);
                effect = new ImageView("matchstickMan/image/summon1.gif");
            }
            effect.setLayoutY(-60);
            getChildren().addAll(effect);
            getChildren().add(0,wing);
            new Timeline(new KeyFrame(Duration.millis(800),new KeyValue(layoutYProperty(), field-20*ratio))).play();
            new Timeline(new KeyFrame(Duration.seconds(5), event ->
            {
                getChildren().removeAll(effect,wing);
            })).play();
        }
        if(step == 5)
        {
            new Timeline(new KeyFrame(Duration.millis(500),event ->
            {
                superFrozen.set(false);
            }, new KeyValue(layoutYProperty(), field-6.5*ratio))).play();
            return;
        }
        Random random = new Random();
        int k = random.nextInt(2)+1;
        ImageView monsterView = new ImageView("matchstickMan/image/monster"+ k+".gif");
        monsterView.setLayoutY(field-100);
        OnFinished onFinished = new OnFinished();
        onFinished.monsterView = monsterView;
        Timeline judge = new Timeline(new KeyFrame(Duration.millis(1), onFinished));
        judge.setCycleCount(Timeline.INDEFINITE);
        judge.play();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5+random.nextDouble()),event ->
        {

            if(k%2==1)//facingRight
            {
                monsterView.setLayoutX(leftBorder-30);
                new Timeline(new KeyFrame(Duration.seconds(random.nextDouble()+5),event1 ->
                {
                    monsterView.setLayoutX(-0x3fffffff);
                    platform.getChildren().remove(monsterView);
                }, new KeyValue(monsterView.layoutXProperty(), rightBorder))).play();
            }
            else
            {
                monsterView.setLayoutX(rightBorder+30);
                new Timeline(new KeyFrame(Duration.seconds(random.nextDouble()+5),event1 ->
                {
                    monsterView.setLayoutX(-0x3fffffff);
                    platform.getChildren().remove(monsterView);
                }, new KeyValue(monsterView.layoutXProperty(), leftBorder))).play();
            }
            platform.getChildren().add(monsterView);
            summon(step+1);
        }));
        timeline.play();
    }
    public void tornado()
    {

    }
    public void dragon()
    {
        Timeline dragon = new Timeline(new KeyFrame(Duration.seconds(1.3), event ->
        {
            if(dragonRight)
            {
                if(300<=opponent.getLayoutX()&&opponent.getLayoutX()<=(rightBorder-leftBorder)/2)
                {
                    if(!opponent.shield.showing||opponent.shield.right) opponent.hp.set(opponent.hp.get()-10);
                    if(!isDamaging&&opponent.hp.get()>0)
                    {
                        isDamaging = true;
                        opponent.getChildren().add(damageHP);
                        damageTimeline.setCycleCount(20);
                        damageTimeline.play();
                    }
                }
            }
            else
            {
                if((rightBorder-leftBorder)/2<=opponent.getLayoutX()&&opponent.getLayoutX()<=rightBorder-300)
                {
                    if(!opponent.shield.showing||!opponent.shield.right)opponent.hp.set(opponent.hp.get()-10);
                    if(!isDamaging&&opponent.hp.get()>0)
                    {
                        isDamaging = true;
                        opponent.getChildren().add(damageHP);
                        damageTimeline.setCycleCount(20);
                        damageTimeline.play();
                    }
                }
            }
        }), new KeyFrame(Duration.seconds(2), event -> platform.getChildren().remove(dragonView)));
        dragon.play();
        if(opponent.getLayoutX()<=(rightBorder-leftBorder)/2)
        {
            dragonRight = true;//pen you mian
            dragonView = new ImageView("matchstickMan/image/dragon1.gif");
        }
        else
        {
            dragonRight = false;
            dragonView = new ImageView("matchstickMan/image/dragon2.gif");
            dragonView.setLayoutX(rightBorder-dragonView.getImage().getWidth());
        }
        dragonView.setLayoutY(Platform.field-220);
        platform.getChildren().add(0,dragonView);
    }
    public void transfere()
    {
        robot.Info info;
        robot.Archimage main;
        info = new robot.Info(this.player, opponent.player);
        main = new robot.Archimage();
        if(mode == 2)
        {
            stick.setImage(new Image("matchstickMan/image/realStick.png"));
            Random random = new Random();
            magic = new Timeline(new KeyFrame(Duration.seconds(13+random.nextDouble()*4), event ->
            {
                summon(0);
            }), new KeyFrame(Duration.seconds(3+random.nextDouble()*6), event ->
            {
                dragon();
            }));
            magic.setCycleCount(Timeline.INDEFINITE);
            magic.play();
        }
        Duration duration;
        if(mode == 2)duration = Duration.millis(6.18);
        else if (mode == 1) duration = Duration.millis(9.99);
        else duration = Duration.millis(250);
        transfere = new Timeline(new KeyFrame(duration, event ->
        {
            if(opponent.hp.get()<=0&&opponent.frozen.get()||hp.get()<=0&&frozen.get())
            {
                if(mode == 2)magic.stop();
                transfere.stop();
                stop();
            }
            double[][] ball=new double[4][2];
            for(int i=0;i<opponent.balls.size();i++)for(int j=0;j<2;j++)
            {
                if(opponent.balls.get(i).isAttacking&&!opponent.balls.get(i).aimed&&opponent.balls.get(i).isVisible())
                {
                    try {
                        ball[i][0] = opponent.balls.get(i).getLayoutX();
                        ball[i][1] = opponent.balls.get(i).getLayoutY();
                    }catch (Exception e)
                    {
                        ball[i][0] = 0x7fffffff;
                        ball[i][1] = 0x7fffffff;
                    }
                }
                else
                {
                    ball[i][0] = 0x7fffffff;
                    ball[i][1] = 0x7fffffff;
                }
            }
            info.init(ball);
            ArrayList<String> strings;
            if(mode == 2)strings = main.operate(info, true);
            else strings = main.operate(info, false);
//            if(!last.equals(strings)&&!strings.isEmpty())
//            {
//            for(int i=0;i<strings.size();i++)System.out.print(strings.get(i)+" ");
//            System.out.println();
//            }
            MotionController.auto(this, strings, 0);
        }));
        transfere.setCycleCount(Timeline.INDEFINITE);
        transfere.play();
    }
    @Override
    public void down()
    {
        shield.showing = true;
        down = new Timeline();
    }
    @Override
    public void up()
    {
        KeyFrame keyFrame = new KeyFrame(Duration.ZERO);
        if(shield.showing)keyFrame = new KeyFrame(Duration.millis(100), event ->
        {
            if(hp.get()>=0)this.frozen.set(false);
        });
        up = new Timeline(keyFrame);
        up.play();
    }
}
