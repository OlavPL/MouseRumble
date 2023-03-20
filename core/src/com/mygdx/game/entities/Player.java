package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.entities.projectiles.PlayerProjectile;
import com.mygdx.game.entities.projectiles.Snake;
import com.mygdx.game.entities.projectiles.Projectile;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Factories;
import com.mygdx.game.utils.Utils;
import lombok.Getter;

import java.util.ArrayList;

import static com.mygdx.game.utils.Constants.PPM;
import static com.mygdx.game.utils.Constants.TILE_SIZE;

@Getter
public class Player extends Character implements InputProcessor {

    protected final static int WIDTH = 16;
    protected final static int HEIGHT = 13;
    private final static Texture  shieldTexture = new Texture("sprites/playerShield.png");
//    private final static Texture  spriteSheet = new Texture("sprites/characterSprites/mouseWalk.png");
    private final static TextureRegion  singularMouse = new TextureRegion(
            new Texture("sprites/characterSprites/singularMouse.png")
    );
    protected final float TEXTURE_OFFSET = 0f;

    //Projectiles / attacks
    private ArrayList<Projectile> projectiles = new ArrayList<>();

    //movement
    protected float leftMove, rightMove, upMove, downMove, xMovement, yMovement;

    // status
    float maxHealth = 50;
    float health = maxHealth;
    float points = 0;
    boolean isShielded = false;
    private final Sound squeak = Gdx.audio.newSound(Gdx.files.internal("squeak.wav"));
    private final Sound thud= Gdx.audio.newSound(Gdx.files.internal("thud.wav"));

    private boolean canShoot = true;
    private float shootCD = 1f;
    private float shootCDR = 0;


    public Player(World world, float posX, float posY){
        super(world, posX, posY, WIDTH, HEIGHT,new Texture("sprites/characterSprites/mouseWalk.png"),3,4);
//        currentFrame = new TextureRegion(singularMouse);
    }

    // Update loop for player related stuff, uses world delta
    @Override
    public void update(float delta){
        stateTime += delta;
        updateMovement();

        shootCDR += delta;
        if (shootCDR >= shootCD) {
            shootCDR -= shootCD;
            canShoot = true;
        }

        // Update player projcetiles
        Utils.iterateProjectiles(world, delta, projectiles);

        if(body.getLinearVelocity().isZero())
            currentFrame = singularMouse;
        else
            currentFrame = selectFrame();
    }

    @Override
    public void render(SpriteBatch batch){
        //draw player and projectiles
        batch.draw(currentFrame,getPosition().x * PPM - WIDTH/2 - TEXTURE_OFFSET, getPosition().y * PPM - HEIGHT/2);
        if(isShielded)
            batch.draw(shieldTexture,getPosition().x * PPM - shieldTexture.getWidth()/2 , getPosition().y * PPM - shieldTexture.getHeight()/2);

        for (Projectile p: projectiles) {
            p.render(batch);
        }
    }

    //<editor-fold desc="Input handlers">
    @Override
    public boolean keyDown(int keycode) {
        switch(keycode){

            case Input.Keys.LEFT:
            case Input.Keys.A: {
                leftMove = -1;
                break;
            }
            case Input.Keys.UP:
            case Input.Keys.W: {
                upMove = 1;
                break;
            }
            case Input.Keys.DOWN:
            case Input.Keys.S: {
                downMove = -1;
                break;
            }
            case Input.Keys.RIGHT:
            case Input.Keys.D: {
                rightMove = 1;
                break;
            }
            case Input.Keys.SPACE: {
                if(canShoot)
                    shoot(body.getWorld());
                break;
            }
        }

        return false;
    }
    protected void updateMovement(){
        xMovement = 0;
        yMovement = 0;
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            xMovement -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)){
            xMovement += 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            yMovement += 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)){
            yMovement -= 1;
        }


        if(xMovement !=0 && yMovement!= 0){
            setLinearVelocity((float)(xMovement * speed * DIAGONAL_MULTI) , (float)(yMovement * speed * DIAGONAL_MULTI));
        }
        else
            setLinearVelocity(xMovement * speed, yMovement * speed);
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode){

            case Input.Keys.LEFT:
            case Input.Keys.A:{
                leftMove = 0;
                lastDirection.set(-1,0);
                break;
            }
            case Input.Keys.UP:
            case Input.Keys.W:{
                upMove = 0;
                lastDirection.set(0,1);
                break;
            }
            case Input.Keys.DOWN:
            case Input.Keys.S: {
                downMove = 0;
                lastDirection.set(0,-1);
                break;
            }
            case Input.Keys.RIGHT:
            case Input.Keys.D: {
                rightMove = 0;
                lastDirection.set(1,0);
                break;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
    //</editor-fold>

    public void shoot(World world){
        Vector2 dir = body.getLinearVelocity().nor();
        if(dir.isZero())
            dir = lastDirection.isZero()? new Vector2(0,1): lastDirection;
        float x = getPosition().x * PPM + WIDTH * dir.x;
        float y = getPosition().y * PPM + HEIGHT * dir.y;
        if(x < Constants.TILE_SIZE+ TILE_SIZE/2 || x > Constants.MAP_WIDTH * TILE_SIZE - TILE_SIZE*2 )
            return;
        if(y < Constants.TILE_SIZE*2 || y > Constants.MAP_HEIGHT * TILE_SIZE - TILE_SIZE )
            return;

        thud.play(.6f);
        System.out.println("x: "+x+", y: "+y);
        projectiles.add(new PlayerProjectile(
                world, 40,20, x, y, new Vector2(dir.x,dir.y)
        ));
        canShoot = false;
    }

    @Override
    public void receiveDamage(float damage){
        if(isShielded){
            isShielded = false;
            return;
        }
        squeak.play();
        health-= damage;
        System.out.println("Player took "+ (int)damage +" damage");
    }

    public void heal(int heal){
        health += heal;
        if(health> maxHealth)
            health = maxHealth;
        System.out.println("Player healed for "+ heal +" points");
    }
    public void shield() {
        isShielded = true;
    }

    public void addPoints(float n){ points += n;}
}
