package featherdev.mgoa.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import featherdev.mgoa.Mgoa;
import featherdev.mgoa.game.BackgroundManager;
import featherdev.mgoa.game.CoreManager;
import featherdev.mgoa.game.EffectsManager;
import featherdev.mgoa.game.HeadsUpDisplay;
import featherdev.mgoa.objects.MusicPlayer;
import featherdev.mgoa.objects.PausedMenu;

/**
 * What are we here for, anyway?
 * 
 * @author albatross
 */

public class GameScreen implements Screen {
	
	enum WorldState { INTRO, MAIN, OUTRO, PAUSED };
	WorldState state;
	int cachedState;
	boolean justSwitchedState = false;
	
	Mgoa game;
	SpriteBatch batch;

	BackgroundManager backgroundmanager;
	CoreManager coremanager;
	EffectsManager fxmanager;
	HeadsUpDisplay hud;
	PausedMenu pausedMenu;
	
	public GameScreen() {
		game = Mgoa.getInstance();
		batch = game.batch;
		
		backgroundmanager = new BackgroundManager(game.rawmap);
		fxmanager = new EffectsManager();
		coremanager = new CoreManager(game.beatmap, game.difficulty, fxmanager);
		hud = new HeadsUpDisplay(game.song, this);
		pausedMenu = new PausedMenu(this);

		MusicPlayer.instance().load(game.song.getHandle());

		hud.setAsInputProcessor();
		
		state = WorldState.INTRO;
		justSwitchedState = true;
	}

	@Override
	public void render(float delta) {

		switch (state) {

		case INTRO:
			if (justSwitchedState){
				hud.fadein(1f);
				hud.showPlayDialog(new ClickListener(){
					@Override
					public void clicked(InputEvent event, float x, float y) {
						event.getListenerActor().remove();
						state = WorldState.MAIN;
						justSwitchedState = true;
					}
				});
				justSwitchedState = false;
			}
			
			backgroundmanager.update(delta);
			hud.update(delta);
			
			backgroundmanager.draw(batch);
			hud.draw(batch);
			break;
			
		case MAIN:
			if (justSwitchedState){
				System.out.println("attempting to play");
				MusicPlayer.instance().play();
				int score = game.records.readScore(game.song.getHandle());
				if (score != -1){
					String message = "Personal best: " + String.valueOf(score);
					TextBounds bounds = game.skin.getFont("naipol").getBounds(message);
					Vector2 top = new Vector2(
							Gdx.graphics.getWidth()/2f - bounds.width / 2f,
							Gdx.graphics.getHeight() - (bounds.height + 10f));
					
					hud.showMessage(message, top, .3f, 5f);
				}
				
				justSwitchedState = false;
			}
			
			// update
			backgroundmanager.update(delta);
			coremanager.update(delta);
			hud.setPoints(coremanager.getPoints());
			hud.setAccuracy(coremanager.getAverageAccuracy());
			hud.update(delta);

			// draw
			backgroundmanager.draw(batch);
			coremanager.draw(batch);
			fxmanager.render(delta, batch);
			hud.draw(game.batch);
			
			// state
			if (state == WorldState.MAIN && !MusicPlayer.instance().isPlaying()){
				hud.fadeout(1);
				state = WorldState.OUTRO;
			}
			break;
			
		case OUTRO:
			hud.update(delta);
			hud.draw(game.batch);
			
			if (hud.getAlpha() == 0f)
				game.setScreen(new FinishScreen(coremanager.getPoints()));
			break;
			
		case PAUSED:
			backgroundmanager.draw(batch);
			
			pausedMenu.act();
			pausedMenu.draw();
			break;
		}
	}
	@Override
	public void resize(int width, int height) {

	}
	@Override
	public void show() {
		
	}
	@Override
	public void hide() {

	}
	@Override
	public void pause() {
		MusicPlayer.instance().pause();
		cachedState = state.ordinal();
		Gdx.input.setInputProcessor(pausedMenu);
		state = WorldState.PAUSED;
	}
	@Override
	public void resume() {
		MusicPlayer.instance().play();
		hud.setAsInputProcessor();
		state = WorldState.values()[cachedState];
	}
	@Override
	public void dispose() {
		MusicPlayer.instance().dispose();
		fxmanager.dispose();
		hud.dispose();
	}

}
