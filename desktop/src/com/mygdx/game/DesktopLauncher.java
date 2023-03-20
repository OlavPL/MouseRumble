package com.mygdx.game;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mygdx.game.HackathonRumble;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1280,720);
		config.setResizable(false);
		config.setForegroundFPS(60);
		config.setWindowIcon(Files.FileType.Internal,"sprites/characterSprites/singularMouse.png");
		config.useVsync(true);
		config.setTitle("MouseRumble");
		new Lwjgl3Application(new HackathonRumble(), config);
	}
}
