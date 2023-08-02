package com.mygdx.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import static com.mygdx.game.utils.Constants.PPM;

@Getter
@Setter
public class Cobra extends Character {
    float AGGRO_RANGE = 50;
    private final static int WIDTH = 16;
    private final static int HEIGHT = 16;

    // movement
    private Vector2 movementVector = new Vector2();
    private final static float DEFAULT_SPEED = 7;
    private float speed = DEFAULT_SPEED;
    private Body aggroSensor;
    boolean destroy = false;
    private final Player player;

    //stats
    private final float POINTS_VALUE = 5;

    // "AI"
    private final float RETREAT_DURATION  = 2;
    private float retreatCountdown = 0;
    private final float maxWanderDuration = 4f;
    private float wanderDuration = 0f;
    private Behaviour behaviour = Behaviour.IDLE;
    private final Vector2 retreatVector = new Vector2(0,0);
    private boolean isInContact = false;


    public Cobra(World world, float posX, float posY, Player player) {
        super(world, posX, posY, WIDTH, HEIGHT, new Texture("sprites/characterSprites/cobraSheet.png"),3, 8,
                Gdx.audio.newSound(Gdx.files.internal("sounds/snake_hissing.mp3")),
                Gdx.audio.newSound(Gdx.files.internal("sounds/karate_chop.mp3"))
        );
        this.player = player;
        attack = 2;
        createCobraSensor(world,posX, posY);
        setIdle();
    }

    @Override
    public void update(float delta) {
//        stateTime += delta;
        animationHandler.update(delta);

        switch(behaviour){
            case IDLE:{
                wanderDuration += delta;
                wander();
                break;
            }
            case RETREATING:{
                retreatCountdown += delta;
                setLinearVelocity(getRetreatVector().x * speed/2, getRetreatVector().y * speed/2);

                //Turn back to aggressive and reset retreat when retreat is ran out
                if(retreatCountdown >= RETREAT_DURATION){
                    retreatCountdown = 0;
                    if(isInContact){
                        defaultAttack();
                        retreat();
                        break;
                    }
                    setAggressive();
                }
                break;
            }
            case AGGRESSIVE:{
                //            Get point between player and Cobra and set movement towards that point
                moveToPlayer();
                break;
            }
        }

        // Get current frame of animation for the current stateTime
//        currentFrame = selectFrame();
        updateMovement();
    }

    @Override
    public void render(SpriteBatch sBatch) {

//        sBatch.draw(currentFrame, body.getPosition().x * PPM - (float)WIDTH/2, body.getPosition().y * PPM - (float)HEIGHT/2);
        sBatch.draw(animationHandler.getCurrentFrame(), body.getPosition().x * PPM - (float)WIDTH/2, body.getPosition().y * PPM - (float)HEIGHT/2);
    }
    @Override
    public void die(){
        destroy = true;
        soundGetHit.play();
        player.addPoints(POINTS_VALUE);
    }
    @Override
    public void defaultAttack(){
        player.receiveDamage(attack);
    }

    @Override
    public void updateMovement() {
        setLinearVelocity((float)(movementVector.x * speed * getDIAGONAL_MULTI()),(float)(movementVector.y * speed * getDIAGONAL_MULTI()));
    }

    public void setAggressive(){
        behaviour = Behaviour.AGGRESSIVE;
        speed = DEFAULT_SPEED;
    }
    public void setIdle(){
        behaviour = Behaviour.IDLE;
        speed = DEFAULT_SPEED/3;
    }


    public void wander(){
        if(body.getLinearVelocity().isZero() || wanderDuration >= maxWanderDuration) {
            movementVector = new Vector2().setToRandomDirection();

            if( wanderDuration >= maxWanderDuration){
                wanderDuration -= maxWanderDuration;
            }
        }
    }
    public void retreat(){
        behaviour = Behaviour.RETREATING;
        speed = DEFAULT_SPEED * 0.66f;
//        float angle = MathUtils.atan2(body.getPosition().y + player.getPosition().y,body.getPosition().y + player.getPosition().y);
        float angle = MathUtils.atan2(
                body.getPosition().y - player.getPosition().y,
                body.getPosition().x - player.getPosition().x
        );
        //        setRetreatVector(vec);
        movementVector = new Vector2().set(MathUtils.cos( angle ) * 1, MathUtils.sin( angle) * 1). nor();
    }

    private void moveToPlayer(){
        float angle = MathUtils.atan2(
                player.getPosition().y - body.getPosition().y,
                player.getPosition().x - body.getPosition().x
        );
        movementVector = new Vector2().set(MathUtils.cos( angle) * 1, MathUtils.sin( angle) * 1). nor();
//        setLinearVelocity(vec.x * speed, vec.y * speed);
    }

    private void createCobraSensor(World world, float x, float y){
        BodyDef bdef = new BodyDef();
        bdef.fixedRotation = true;
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x/ Constants.PPM,y/Constants.PPM);
        this.aggroSensor = world.createBody(bdef);

        CircleShape shape = new CircleShape();
        shape.setRadius(AGGRO_RANGE / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0f;
        fixtureDef.isSensor = true;

        this.body.createFixture(fixtureDef).setUserData(this);
    }
}
