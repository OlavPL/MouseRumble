package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.entities.Cobra;
import com.mygdx.game.entities.Pickup.PPHeal;
import com.mygdx.game.entities.Pickup.PPShield;
import com.mygdx.game.entities.Pickup.PowerUp;
import com.mygdx.game.entities.spawners.SnakeSpawner;
import com.mygdx.game.MouseRumble;
import com.mygdx.game.entities.Player;
import com.mygdx.game.entities.projectiles.Projectile;
import com.mygdx.game.handlers.WorldContactListener;
import com.mygdx.game.utils.HighScore;
import com.mygdx.game.utils.Utils;

import java.util.ArrayList;

import static com.mygdx.game.utils.Constants.PPM;
import static com.mygdx.game.utils.Constants.TILE_SIZE;

public class SinglePlayerGame implements Screen {
    private static final int SCALE = 3;
    private final MouseRumble parent;

    // Map handling
    private final TiledMap map;
    private final int tileWidth, tileHeight, mapWidth, mapHeight;
    private final OrthogonalTiledMapRenderer tmrenderer;
    private final Box2DDebugRenderer debugRenderer;
    private final OrthographicCamera gameCamera;

    //World and listeners
    private final World world;

    //rendering
    private final SpriteBatch sBatch;
    private final Hud hud;

    // player
    private final Player player;

    //Spawners
    private final ArrayList<Rectangle> arenaBounds = new ArrayList<>();
    private final ArrayList<Cobra> cobras = new ArrayList<>();
    private float cobraSpawnCD = 3f;
    private float cobraSpawnTimer = 0;
    private ArrayList<SnakeSpawner> snakeSpawners;
    private final ArrayList<Projectile> snakes;
    private final ArrayList<PowerUp> powers;

    private float powerUpSpawnTimer = 0;
    private float powerUpCD = 15f;

    private final HighScore[] scores;

    // Spawns get progressively faster based on gametime / level threshold
    private float gameTime = 0;
    private float stageLevel = 1;
    private float pointsMultiplier = 1;
    private final float LEVEL_THRESHOLD = 30;

    //sound

    private Music themeSong;

    public SinglePlayerGame(MouseRumble parent){
        this.parent = parent;
        scores = HighScore.deSerialize(true);

        //TiledMap
        map = new TmxMapLoader().load("map/Arena2.tmx");
        tmrenderer = new OrthogonalTiledMapRenderer(map);
        tileWidth = map.getProperties().get("tilewidth", Integer.class);
        tileHeight = map.getProperties().get("tileheight", Integer.class);
        mapWidth = map.getProperties().get("width", Integer.class);
        mapHeight = map.getProperties().get("height", Integer.class);
        debugRenderer = new Box2DDebugRenderer();


        // World logic
        world = new World(new Vector2(0,0),false);
        Utils.parseTiledCollisionLayer(world, map.getLayers().get("collision-layer").getObjects());
        world.setContactListener(new WorldContactListener());

        //sound
        themeSong = Gdx.audio.newMusic(Gdx.files.internal("sounds/authenticOctopusGameGrindyourGears.mp3"));
        themeSong.setLooping(true);
        themeSong.play();

        // renderer
        sBatch = new SpriteBatch();
        hud = new Hud(sBatch);

        //player
//        player = new Player(world, (TILE_SIZE*12), (TILE_SIZE*12));
        player = new Player(world, (TILE_SIZE*0), (TILE_SIZE*0));
        Gdx.input.setInputProcessor(player);

        //cameras
        float mapW = Gdx.graphics.getWidth();
        float mapH = Gdx.graphics.getHeight();
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(false, mapW / SCALE, mapH / SCALE);

        // populate spawners
        snakes = new ArrayList<>();
        powers = new ArrayList<>();
        snakeSpawners = new ArrayList<>();

        Utils.parseTiledArenaBounds(map.getLayers().get("spawn_bounds").getObjects(), arenaBounds);
        Utils.parseTiledSpawnLayer(world, map.getLayers().get("spawn_layer").getObjects(), player, snakeSpawners);
    }

    private void update(float delta){
        world.step(1/60f, 6,2);
        gameTime += delta;
        if(gameTime>= LEVEL_THRESHOLD)
            updateLevel();

        //Player
        player.update(delta);
        if(player.getHealth() <=0)
            endGame();
        player.addPoints(delta*(stageLevel *1.1f));
        hud.updateScore((int)player.getPoints());
        hud.updateLives((int)player.getHealth());



        //Spawners
        cobraSpawnTimer += delta;
//         On cooldown spawn Cobra from random spawner
        if(cobraSpawnTimer >= cobraSpawnCD){
            spawnCobra();
            cobraSpawnTimer -= cobraSpawnCD;
        }
        Utils.iterateCobras(world, delta,cobras);

        for (SnakeSpawner spawner : snakeSpawners) {
            spawner.update(delta);
        }

        Utils.iterateProjectiles(world,delta, snakes);

        Utils.cleanPowerUps(world,powers);

        powerUpSpawnTimer += delta;
        if(powerUpSpawnTimer >= powerUpCD){
            spawnPowerUp();
            powerUpSpawnTimer -= powerUpSpawnTimer;
        }


    }

    @Override
    public void render(float delta) {
//      run update logic
        update(delta);
        //Update camera for rendering game related stuff
        tmrenderer.setView(gameCamera);
        updateCamera();

//      clear screen before rerender
        ScreenUtils.clear(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameCamera.update();
        sBatch.setProjectionMatrix(gameCamera.combined);

        // render tilemap
        tmrenderer.setView(gameCamera);
        tmrenderer.render();

        //Begin render of sprites
        sBatch.begin();
            player.render(sBatch);
            for (Cobra c : cobras) {
                c.render(sBatch);
            }
            for (SnakeSpawner spawner : snakeSpawners) {
                spawner.render(sBatch);
            }
            for(PowerUp pp : powers){
                pp.render(sBatch);
            }
        sBatch.end();

        //set view and Render Hud
        sBatch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();


        // Render debug view
//        debugRenderer.render(world, gameCamera.combined.scl(PPM));
    }
    public void updateCamera(){
        Vector3 position = gameCamera.position;
        position.x = player.getPosition().x * PPM;
        position.y = player.getPosition().y * PPM;
        gameCamera.position.set(position);

        gameCamera.update();
    }


    @Override
    public void dispose() {
        world.dispose();
        sBatch.dispose();
        tmrenderer.dispose();
        map.dispose();
        themeSong.dispose();

        player.getSpriteSheet().dispose();
        for(Projectile p :player.getProjectiles()){
            p.getTexture().dispose();
        }
        for(PowerUp pp : powers){
            pp.getTexture().dispose();
        }
        for(SnakeSpawner spawner : snakeSpawners){
            spawner.dispose();
        }
    }

    // Spawn random PowerUp on a random spot within map, excluding outer 2 tiles around the map
    private void spawnPowerUp(){

        Vector2 pos = Utils.getRandomPos(
                ( (( (mapWidth-4) * tileWidth )) + tileWidth * 2 ),
                ( (( (mapWidth-4) * tileHeight )) + tileHeight * 2)
        );
//        Vector2 pos = new Vector2(
//            (float)((Math.random() * ((mapWidth-4)*tileWidth)) +tileWidth*2),
//            (float)((Math.random() * ((mapHeight-4)*tileHeight)) +tileHeight*2)
//        );
        if(!isValidSpawnLocation(pos)) {
            spawnPowerUp();
            return;
        }

        int id = (int)(Math.random()*2);
        switch (id) {
            case 0 : {
                powers.add(new PPHeal(world, "HealingCheese", pos.x, pos.y));
                break;
            }
            case 1 : {
                powers.add(new PPShield(world, "ShieldingCheese", pos.x, pos.y));
                break;
            }
        }
    }
    private void spawnCobra(){
        Vector2 pos = new Vector2(
            (float)((Math.random() * ((mapWidth-4)*tileWidth)) +tileWidth*2),
            (float)((Math.random() * ((mapHeight-4)*tileHeight)) +tileHeight*2)
        );

        if(!isValidSpawnLocation(pos)){
            spawnCobra();
            return;
        }

        cobras.add(new Cobra(world,pos.x, pos.y, player));
    }

    private void updateLevel(){
        stageLevel = (int)(gameTime / LEVEL_THRESHOLD);
        pointsMultiplier = 1 + stageLevel *0.1f;
        cobraSpawnCD *= 0.9f;
        for (SnakeSpawner spawner: snakeSpawners) {
            spawner.levelUp();
        }
        for (SnakeSpawner spawner : snakeSpawners){
            spawner.setSpawnCDMax(spawner.getSpawnCDMax() * 0.9f);
            spawner.setSpawnCDMin(spawner.getSpawnCDMin() * 0.9f);
        }

        powerUpCD *= 1.2f;
        gameTime = 0;
        System.out.println("Stage Level Up");
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    private boolean isValidSpawnLocation(Vector2 pos){
        for( Rectangle bounds : arenaBounds){
            if(bounds.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    private void endGame(){
        // Serialize if score is good enough
        HighScore.serialize(scores, (int) player.getPoints());
        parent.changeScreen(ScreenType.HIGH_SCORE);
        themeSong.stop();
    }
}