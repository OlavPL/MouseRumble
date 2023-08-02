package com.mygdx.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.handlers.BodyAnimationHandler;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Factories;
import com.mygdx.game.utils.Utils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Character {
    // body
    protected World world;
    protected Body body;
    protected int width;
    protected int height;

    // movement
    protected float speed = 10;
    protected final double DIAGONAL_MULTI = Math.cos(Math.PI/4);
    protected Vector2 lastDirection = new Vector2(1,0);

    //stats
    protected float maxHealth, health;
    protected float attack = 1;
    protected boolean isShielded = false;
    protected boolean canShoot = true;
    protected float shootCD = 0.5f;
    protected float shootCDR = 0;

    protected final Sound soundGetHit, soundShoot;

    protected BodyAnimationHandler animationHandler;



    public Character(World world, float posX, float posY, int width, int height, Texture spriteSheet,
                     int frameRows, int frameColumns, Sound soundGetHit, Sound soundShoot
    ){
        this.world = world;
        this.width = width;
        this.height= height;
        body = Factories.createBody(world,posX, posY,false, true);
        Factories.createFixtureDef(body, width, height,false);
        body.getFixtureList().get(0).setUserData(this);
        animationHandler = new BodyAnimationHandler(this.body, spriteSheet, frameColumns, frameRows);
        this.soundShoot = soundShoot;
        this.soundGetHit = soundGetHit;
    }

    public abstract void update(float delta);
    public abstract void render(SpriteBatch sBatch);


    public abstract void defaultAttack();
    public void receiveDamage(float damage){
        health-= damage;

        if(health <= 0)
            die();
    }
    public abstract void die();

    public void setLinearVelocity(float x, float y){
        body.setLinearVelocity(x,y);
    }
    public abstract void updateMovement();
    public Vector2 getPosition(){return body.getPosition();}

    public void setPosition(float x, float y){
        body.setTransform(x / Constants.PPM, y / Constants.PPM, 0);
        System.out.println("X: " + x + ", y: "+y);
    };

}
