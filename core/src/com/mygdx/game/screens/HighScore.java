package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.HackathonRumble;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Locale;

import static com.mygdx.game.utils.Constants.skin;

public class HighScore implements Screen {
    HackathonRumble parent;
    Stage stage;
    com.mygdx.game.utils.HighScore[] scores;
    Locale locale = new Locale("no", "NO");
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
    public HighScore(HackathonRumble parent){
        this.parent = parent;
        stage = new Stage(new ScreenViewport());
        scores = com.mygdx.game.utils.HighScore.deSerialize(false);
        System.out.println(Arrays.toString(scores));

    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Top 5 High Scores",skin);

        TextButton mainMenu = new TextButton("Main Menu",skin);

        table.add(title).fillX().uniformX();
        table.row().pad(20,0,20,0);
        if (scores.length>0)
            for (com.mygdx.game.utils.HighScore hs : scores) {
                table.add(new Label("Score: "+hs.getScore()+", time: "+dateFormat.format(hs.getDate()),skin)).fillX().uniformX();
                table.row().pad(20,0,20,0);
            }
        else{
            table.add(new Label("There are no High Scores yet, go make one!",skin)).fillX().uniformX();
            table.row().pad(40,0,40,0);
        }
        table.add(mainMenu).fillX().uniformX();

        stage.getViewport().update(HackathonRumble.W_WIDTH, HackathonRumble.W_HEIGHT,true);

        mainMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parent.changeScreen(ScreenType.MENUSCREEN);
            }
        });

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
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

    @Override
    public void dispose() {
        stage.dispose();
    }
}
