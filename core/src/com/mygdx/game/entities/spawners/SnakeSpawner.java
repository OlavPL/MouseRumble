package com.mygdx.game.entities.spawners;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.effects.WaterSplash;
import com.mygdx.game.entities.projectiles.AnimatedProjectile;
import com.mygdx.game.entities.projectiles.Projectile;
import com.mygdx.game.entities.projectiles.Snake;
import com.mygdx.game.utils.Factories;
import com.mygdx.game.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

import static com.mygdx.game.utils.Constants.PPM;
@Getter
@Setter
public class SnakeSpawner {
    private Body body;
    private float spawnCDMax = 10;
    private float spawnCDMin = 5;
    private float spawnCD;
    private float spawnTimer;
    private float projectileSpeed = 10;
    private float radius, angle;
    private float snakeMaxDist = 1000;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private ArrayList<WaterSplash> animationEffects = new ArrayList<>();
    World world;

    public SnakeSpawner(World world, float posX, float posY, float angle){
        this.world = world;
        body = Factories.createBox(world, posX, posY, 5,5,true,true,true);
        body.setUserData(this);
        this.angle = angle;
        spawnTimer = 0;
        spawnCD = MathUtils.random(spawnCDMin, spawnCDMax);
    }

    public void update(float delta){
        spawnTimer += delta;

        if(spawnTimer >= spawnCD){
            spawnTimer -= spawnCD;
            setRandomCD();
            spawnSnake();
        }
        Utils.iterateProjectiles(world,delta,projectiles, animationEffects);
        Utils.iterateAnimEffects(world, delta, animationEffects);

//        for(WaterSplash ws : animationEffects){
//            ws.update(delta);
//            if(ws.destroy){
//            }
//        }
    }

    public void render(SpriteBatch sBatch){
        for(Projectile proj : projectiles){
            proj.render(sBatch);
        }
        for(WaterSplash ws : animationEffects){
            ws.render(sBatch);
        }
    }
    public Snake getNewSnake(World world){
        Vector2 dir = getRandQuarterVector(angle);
        return new Snake(world, snakeMaxDist,projectileSpeed,
                body.getPosition().x * PPM + (Snake.WIDTH + radius) * dir.x,
                body.getPosition().y * PPM + (Snake.HEIGHT + radius) * dir.y,
                dir);
    }
    public void spawnSnake(){
        Vector2 dir = getRandQuarterVector(angle);
        projectiles.add( new Snake(world, snakeMaxDist,projectileSpeed,
                body.getPosition().x * PPM + (Snake.WIDTH + radius) * dir.x,
                body.getPosition().y * PPM + (Snake.HEIGHT + radius) * dir.y,
                dir)
        );
    }

    // Gets angle and returns vector with random angle between object's angle and 90 degrees higher
    private Vector2 getRandQuarterVector(float angle){
        angle += (float)(90*Math.random());
        Vector2 vec = new Vector2();
        vec.set(MathUtils.cos(MathUtils.degreesToRadians * angle) * 1, MathUtils.sin(MathUtils.degreesToRadians * angle) * 1);
        return vec;
    }

    public void setRandomCD(){
        spawnCD = MathUtils.random(spawnCDMin, spawnCDMax);
    }

    public void levelUp(){
        spawnCDMax *= 0.8f;
        spawnCDMin *= 0.8f;
    }

    public void dispose(){
        for(Projectile proj : projectiles){
            proj.dispose();
        }
        for(WaterSplash ws : animationEffects){
            ws.dispose();
        }
    }
}