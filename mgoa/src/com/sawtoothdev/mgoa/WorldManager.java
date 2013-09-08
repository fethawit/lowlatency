package com.sawtoothdev.mgoa;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WorldManager implements IDrawableGameObject {

	public final OneShotMusicPlayer music;
	private final HUD hud;
	private final FxBox fxBox;
	private final CoreManager coreManager;
	private final VisualsManager visuals;
	
	private final OrthographicCamera worldCamera;
	private final OrthographicCamera screenCamera;
	
	public enum WorldState { INITIALIZED, ACTIVE, PAUSED, FINISHED };
	private WorldState state;
	
	public WorldManager(BeatMap map, FileHandle audioFile){
		worldCamera = new OrthographicCamera(10, 6);
		screenCamera = new OrthographicCamera();
		screenCamera.setToOrtho(false);
		
		music = new OneShotMusicPlayer(audioFile);
		hud = new HUD(audioFile, screenCamera);
		fxBox = new FxBox(worldCamera);
		coreManager = new CoreManager(music, fxBox, hud, map.NORMAL, worldCamera);
		visuals = new VisualsManager(map.ORIGINAL, music);
		
		state = WorldState.INITIALIZED;
	}
	
	@Override
	public void update(float delta) {
		
		visuals.update(delta);
		coreManager.update(delta);
		fxBox.update(delta);
		hud.update(delta);
	}

	@Override
	public void draw(SpriteBatch batch) {
		
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		worldCamera.update();
		screenCamera.update();
		
		visuals.draw(batch);
		fxBox.draw(batch);
		coreManager.draw(batch);
		hud.draw(batch);
	}
	
	public void start(){
		music.setLooping(false);
		music.play();
		state = WorldState.ACTIVE;
	}
	
	public WorldState getState(){
		return state;
	}

}
