package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.entities.projectiles.PlayerProjectile;
import com.mygdx.game.entities.projectiles.Projectile;
import com.mygdx.game.utils.Constants;
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
    private final ArrayList<Projectile> projectiles = new ArrayList<>();

    //movement
    protected float leftMove, rightMove, upMove, downMove, xMovement, yMovement;
    protected Vector2 aimVector;
    protected Vector2 lastAimVector = new Vector2(1,0);


    // status
    float points = 0;


    public Player(World world, float posX, float posY){
        super(
                world, posX, posY, WIDTH, HEIGHT,new Texture("sprites/characterSprites/mouseWalk.png"),
                3,4, Gdx.audio.newSound(Gdx.files.internal("sounds/squeak.wav")),
                Gdx.audio.newSound(Gdx.files.internal("sounds/thud.wav"))
        );
        aimVector = new Vector2(0,0);
//        currentFrame = new TextureRegion(singularMouse);
    }

    // Update loop for player related stuff, uses world delta
    @Override
    public void update(float delta){
        stateTime += delta;
        updateMovement();

        if(!canShoot)
            shootCDR += delta;
        if (shootCDR >= shootCD) {
            shootCDR = 0;
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
        batch.draw(currentFrame,getPosition().x * PPM - (float) (WIDTH/2) - TEXTURE_OFFSET, getPosition().y * PPM - (float)(HEIGHT/2));
        if(isShielded)
            batch.draw(shieldTexture,getPosition().x * PPM - (float)(shieldTexture.getWidth()/2) , getPosition().y * PPM - (float)(shieldTexture.getHeight()/2));

        for (Projectile p: projectiles) {
            p.render(batch);
        }
    }
    @Override
    public void updateMovement(){
        xMovement = rightMove + leftMove;
        yMovement = upMove + downMove;
//        if(Gdx.input.isKeyPressed(Input.Keys.A)){
//            xMovement -= 1;
//        }
//        if(Gdx.input.isKeyPressed(Input.Keys.D)){
//            xMovement += 1;
//        }
//        if(Gdx.input.isKeyPressed(Input.Keys.W)){
//            yMovement += 1;
//        }
//        if(Gdx.input.isKeyPressed(Input.Keys.S)){
//            yMovement -= 1;
//        }


        if(xMovement !=0 && yMovement!= 0){
            setLinearVelocity((float)(xMovement * speed * DIAGONAL_MULTI) , (float)(yMovement * speed * DIAGONAL_MULTI));
        }
        else
            setLinearVelocity(xMovement * speed, yMovement * speed);
    }


    //<editor-fold desc="Input handlers">
    @Override
    public boolean keyDown(int keycode) {
        switch(keycode){

            case Input.Keys.A: {
                leftMove = -1;
                break;
            }
            case Input.Keys.W: {
                upMove = 1;
                break;
            }
            case Input.Keys.S: {
                downMove = -1;
                break;
            }
            case Input.Keys.D: {
                rightMove = 1;
                break;
            }
            case Input.Keys.SPACE: {
                if(canShoot)
                    shoot(body.getWorld());
                break;
            }

            case Input.Keys.LEFT:{
                aimVector.x -= 1;
                break;
            }
            case Input.Keys.UP:{
                aimVector.y += 1;
                break;
            }
            case Input.Keys.DOWN:{
                aimVector.y -= 1;
                break;
            }
            case Input.Keys.RIGHT:{
                aimVector.x += 1;
                break;
            }
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode){

            case Input.Keys.A:{
                leftMove = 0;
                lastDirection.set(-1,0);
                break;
            }
            case Input.Keys.W:{
                upMove = 0;
                lastDirection.set(0,1);
                break;
            }
            case Input.Keys.S: {
                downMove = 0;
                lastDirection.set(0,-1);
                break;
            }
            case Input.Keys.D: {
                rightMove = 0;
                lastDirection.set(1,0);
                break;
            }
            case Input.Keys.LEFT:{
                aimVector.add(1,0);
                lastAimVector.set(-1,0);
                break;
            }
            case Input.Keys.UP:{
                aimVector.y -= 1;
                lastAimVector.set(0,1);
                break;
            }
            case Input.Keys.DOWN:{
                aimVector.y += 1;
                lastAimVector.set(0,-1);
                break;
            }
            case Input.Keys.RIGHT:{
                aimVector.x -= 1;
                lastAimVector.set(1,0);
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
        canShoot = false;
        Vector2 dir = aimVector.isZero()? lastAimVector : aimVector;
        float x = getPosition().x * PPM + WIDTH * dir.x;
        float y = getPosition().y * PPM + HEIGHT * dir.y;
        if(x < Constants.TILE_SIZE+ (float)(TILE_SIZE/2) || x > Constants.MAP_WIDTH * TILE_SIZE - TILE_SIZE*2 )
            return;
        if(y < Constants.TILE_SIZE*2 || y > Constants.MAP_HEIGHT * TILE_SIZE - TILE_SIZE )
            return;

        soundShoot.play(.6f);
        projectiles.add(new PlayerProjectile(
                world, 40,20, x, y, new Vector2(dir.x,dir.y)
        ));
    }

    @Override
    public void receiveDamage(float damage){
        if(isShielded){
            isShielded = false;
            return;
        }
        this.soundGetHit.play();
        health-= damage;
        System.out.println("Player took "+ (int)damage +" damage");
    }

    @Override
    public void die() {

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
