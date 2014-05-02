package featherdev.mgoa.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

import featherdev.lwbd.Beat;
import featherdev.mgoa.Mgoa;
import featherdev.mgoa.Utilities;
import featherdev.mgoa.objects.IDrawable;
import featherdev.mgoa.objects.IUpdateable;
import featherdev.mgoa.subsystems.Effects;
import featherdev.mgoa.subsystems.MusicPlayer;
import featherdev.mgoa.subsystems.Stats;

class BeatCore implements IUpdateable, IDrawable, Poolable {
	enum CoreState { ALIVE, DYING, DEAD }
	enum Accuracy { STELLAR, PERFECT, EXCELLENT, GOOD, ALMOST };
	static OrthographicCamera cam = new OrthographicCamera(10, 6);
	
	// beat
	private Beat beat;
	// geometry
	private Sprite ring, core;
	private Color ec;
	private float shrinkRate;
	private Vector2 position;
	// state
	private CoreState state;
	private float timeToLive;
	private float timeAlive;
	private boolean beenHit;
	
	public BeatCore() {
		
		ring = Mgoa.instance().textures.createSprite("game/ring");
		core = Mgoa.instance().textures.createSprite("game/ring");
		
		ring.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		core.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		ec = new Color();
		position = new Vector2();
		
		reset();
	}

	private void onHit(long songTimeMs) {
		Gdx.app.log("BeatCore", "HIT");
		if (beenHit || state == CoreState.DEAD)
			return;

		// calculate how accurate the hit was
		long diff = songTimeMs - beat.timeMs;
		Accuracy acc;
		if (diff < -210)
			acc = Accuracy.ALMOST;
		else if (diff < -150)
			acc = Accuracy.GOOD;
		else if (diff <= -90)
			acc = Accuracy.EXCELLENT;
		else if (diff <= -30)
			acc = Accuracy.PERFECT;
		else if (diff <= 40)
			acc = Accuracy.STELLAR;
		else if (diff <= 120)
			acc = Accuracy.PERFECT;
		else if (diff <= 200)
			acc = Accuracy.EXCELLENT;
		else if (diff <= 280)
			acc = Accuracy.GOOD;
		else
			acc = Accuracy.ALMOST;
		
		// calculate the point value & record in stats
		int points = (int) (getScoreValue() / (acc.ordinal() + 1));
		Stats.instance().points += points;
		
		// generate an effect
		Effects.instance().makeExplosion(position, ec);
		
		// kill me
		state = CoreState.DYING;
		beenHit = true;
	}
	private int getScoreValue() {
		double sv = beat.energy * 100;
		sv = Math.ceil(sv);
		return (int) (sv);
	}
	private boolean checkIsTouched(){
		if (Gdx.input.isTouched()){
			Vector2 touchpos = Utilities.getTouchInWorld(cam);
			
			Rectangle hitbox = new Rectangle(position.x - .5f, position.y - .5f, 1f, 1f);
			return hitbox.contains(touchpos.x, touchpos.y);
		}
		else
			return false;

	}	

	public void update(float delta) {

		Color color = core.getColor();
		
		switch (state){
		case ALIVE:
			// check input
			if (checkIsTouched())
				onHit(MusicPlayer.instance().time());
			
			// update color
			if (color.a < 1){
				color.a += delta * 2;
				if (color.a > 1)
					color.a = 1;
			}
			
			// tick life timer
			timeAlive += delta;
			ring.scale(delta * -shrinkRate);
			
			// check life
			if (timeAlive >= timeToLive && state != CoreState.DYING)
				state = CoreState.DYING;
				
			break;
		case DYING:
			// check input
			if (checkIsTouched())
				onHit(MusicPlayer.instance().time());
			
			if (color.a > .05)
				color.a -= (delta * 2);
			else
				state = CoreState.DEAD;
			
			break;
		case DEAD:
			break;
		default:
			break;
		}
		
		core.setColor(color);
		ring.setColor(color);
		//core.rotate(delta * 360);
	}
	public void draw(SpriteBatch batch){
		ring.draw(batch);
		core.draw(batch);
	}
	public void reset() {
		ring.setScale(1.8f);
		ring.setSize(1f, 1f);
		core.setSize(1f, 1f);
		ring.setColor(Color.WHITE);
		core.setColor(Color.WHITE);
		beat = null;
		ec.set(Color.WHITE);
		shrinkRate = 0;
		timeToLive = 0;
		timeAlive = 0;
		position.set(0, 0);
		beenHit = false;
	}
	public void init(Beat b, Vector2 pos, float lifetime) {
		beat = b;
		ec.set(getEnergyColor(b.energy));
		ring.setColor(ec);
		core.setColor(ec);
		
		shrinkRate = (ring.getScaleX() - core.getScaleX()) / lifetime;
		timeToLive = lifetime;
		position.set(pos);
		Vector2 spritepos = new Vector2(pos.x - ring.getWidth() / 2f, pos.y - ring.getHeight() / 2f);
		ring.setPosition(spritepos.x, spritepos.y);
		core.setPosition(spritepos.x, spritepos.y);
		ring.setOrigin(ring.getWidth() / 2f, ring.getHeight() / 2f);
		core.setOrigin(core.getWidth() / 2f, core.getHeight() / 2f);
		
		state = CoreState.ALIVE;
	}
	public Vector2 getPosition() {
		return position;
	}
	public boolean isDead(){
		return state == CoreState.DEAD;
	}
	public Color getColor() {
		return ec;
	}
	public static Color getEnergyColor(float energy){
		Color color = new Color();
		
		if (energy > .75f)
			color = Color.RED;
		else if (energy > .6f)
			color = Color.ORANGE;
		else if (energy > .45f)
			color = Color.YELLOW;
		else if (energy > .3f)
			color = Color.GREEN;
		else if (energy > .15f)
			color = Color.BLUE;
		else
			color = new Color(141, 0, 255, 1);
		
		return color;
	}
	
}