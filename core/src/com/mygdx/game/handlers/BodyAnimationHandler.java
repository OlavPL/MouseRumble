package com.mygdx.game.handlers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.game.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter

public class BodyAnimationHandler {
    Texture spriteSheet;
//    Animation<TextureRegion> moveRightAnim, moveLeftAnim, moveUpAnim, moveDownAnim, idleAnimation;
    HashMap<String, Animation<TextureRegion>> animations = new HashMap<>();
    TextureRegion currentFrame;
    int frameColumns;
    int frameRows;
    float stateTime;
    Body body;

    public BodyAnimationHandler(Body body, Texture spriteSheet, int frameColumns, int frameRows){
        this.body = body;
        this.spriteSheet = spriteSheet;
        this.frameColumns = frameColumns;
        this.frameRows = frameRows;
        stateTime = 0;

        Animation<TextureRegion>[] animationArr = Utils.createMovementAnimations(spriteSheet, frameColumns, frameRows);
        for (int i = 0; i < animationArr.length; i++) {
            switch (i){
                case 0: animations.put("moveRightAnim" ,animationArr[i]);
                case 1: animations.put("moveLeftAnim" ,animationArr[i]);
                case 2: animations.put("moveUpAnim" ,animationArr[i]);
                case 3: animations.put("moveDownAnim" ,animationArr[i]);
            }
        }
        stateTime = 0;

    }
    public void update(float delta){
        stateTime += delta;
        currentFrame = selectFrame();
    }

    public TextureRegion selectFrame(){
        if( Math.abs(body.getLinearVelocity().x) > Math.abs(body.getLinearVelocity().y ) ){
            if(body.getLinearVelocity().x < 0) {
                return animations.get("moveLeftAnim").getKeyFrame(stateTime, true);
            }

            return animations.get("moveRightAnim").getKeyFrame(stateTime,true);
        }
        else {
            if (body.getLinearVelocity().y > 0)
                return animations.get("moveUpAnim").getKeyFrame(stateTime, true);

            animations.get("moveDownAnim").getKeyFrame(stateTime, true);
        }

        if(animations.get("idleAnimation") != null)
            return animations.get("idleAnimation").getKeyFrame(stateTime,true);

        return animations.get("moveDownAnim").getKeyFrame(stateTime, true);
    }
}
