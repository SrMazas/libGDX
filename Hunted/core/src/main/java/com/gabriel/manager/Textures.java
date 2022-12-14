package com.gabriel.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by Tim on 28/12/2016.
 */
public class Textures {
  private static Textures ourInstance;
  public static Textures getInstance() {
    return ourInstance;
  }
  private Textures() {}
  public static void create() { ourInstance = new Textures(); }

  private TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("sprites.txt"));
  private Texture splash = new Texture(Gdx.files.internal("splash.png"));
  private Texture loose = new Texture(Gdx.files.internal("loose.png"));
  private Texture title = new Texture(Gdx.files.internal("Hunted.png"));

  public TextureRegion getTexture(String name) {
    return atlas.findRegion(name);
  }
  public Texture getSplash() { return splash; }
  public Texture getLoose() { return loose; }
  public Texture getTitle() { return  title;  }


  public TextureAtlas getAtlas() { return  atlas; }
  public void dispose() {
    splash.dispose();
    atlas.dispose();
    loose.dispose();
    title.dispose();
    ourInstance = null;
  }
}
