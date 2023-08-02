package com.mygdx.game.effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.utils.Utils;

public class WaterSplash {
    public static final float FRAME_LENGTH = 0.15f;
    public static final int OFFSET = 0;
    public static final int FRAME_COLS = 5;
    public static final int SIZE = 32;

    public static Animation<TextureRegion> anim = null;
    float x,y;
    float stateTime;
    public boolean destroy = false;

    public WaterSplash(float x, float y){
        this.x = x;
        this.y = y;
        stateTime = 0;

        if(anim == null){
//            anim = new Animation<>(FRAME_LENGTH, TextureRegion.split(new Texture("sprites/effects/LightFX/FX003/blue_splash.png"),SIZE,SIZE)[0]);
            anim = Utils.createAnimation(
                new Texture("sprites/effects/LightFX/FX003/blue_splash.png"),
                    FRAME_COLS,
                    FRAME_LENGTH
            );
        }
    }

    public void update (float delta){
        stateTime += delta;
        if(anim.isAnimationFinished(stateTime))
            destroy = true;
    }

    public void render (SpriteBatch spriteBatch){
        spriteBatch.draw(anim.getKeyFrame(stateTime, false), x - (float)SIZE/2, y);
    }

    public void dispose(){this.dispose();}


}
