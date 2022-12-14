package com.gabriel.entity;

import com.badlogic.gdx.physics.box2d.*;
import com.gabriel.Param;
import com.gabriel.Utility;
import com.gabriel.manager.GameState;
import com.gabriel.manager.Physics;
import com.gabriel.manager.Sprites;
import com.gabriel.pathfinding.Node;
import com.gabriel.world.Room;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Tim on 28/12/2016.
 */
public class Tile extends EntityBase implements Node<Tile> {

  private boolean isFloor = false;
  private boolean hasPhysics = false;
  private boolean isWeb = false;
  public Room myRoom = null;
  private HashSet<Tile> webNeighbours = new HashSet<Tile>();
  public int webEffect = 0;
  Tile webTarget;
  private final int nFloors = 35;

  public Tile(int x, int y) {
    super(x, y);
    setIsDirt();
  }

  public void setIsDirt() {
    isFloor = false;
    setTexture("pitC");
  }

  public void setIsFloor(Room room) {
    isFloor = true;
    myRoom = room;
    int floor = Utility.r.nextInt(100);
    //if (room.getIsCorridor()) setTexture("floorZ"); // TODO DEBUg
    if (floor < nFloors)  setTexture("floor" + Integer.toString(floor));
    else setTexture("floor");
  }

  public void setIsWeb() {
    updateNeighbours(true); // Update my neighbours
    if (isWeb) return;
    addWebSensor();
    Sprites.getInstance().webTiles.add(this);
    isWeb = true;
  }

  public void startWebEffect(Tile target) {
    if (webEffect == -1) return;
    webEffect = 1;
    webTint = 1f;
    webTarget = target;
  }

  public boolean tintWeb() {
    if (webTint > 0) {
      webTint = Math.max(webTint - 0.01f, 0f);
      return true;
    }
    return false;
  }

  public void moveWeb() { // Only needed during WebEffect
    if (webEffect == 1) {
      webEffect = 2;
      // Otherwise in one tick the web can move 2 places hence BigBad cannot sense that it is active underneath
    } else if (webEffect == 2) {
      webEffect = -1; // Prevents recursion
      for (Tile t : webNeighbours) t.startWebEffect(webTarget);
    }
  }



  public void addWebSensor() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.StaticBody;
    bodyDef.position.set(getX()/Param.TILE_SIZE + .5f, getY()/Param.TILE_SIZE + .5f);
    body = Physics.getInstance().world.createBody(bodyDef);
    body.setUserData(this);
    bodyDef.type = BodyDef.BodyType.StaticBody;
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(.35f);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = Param.SENSOR_ENTITY; // I am a
    fixtureDef.filter.maskBits = Param.SENSOR_COLLIDES; // I collide with
    fixtureDef.isSensor = true;
    body.createFixture(fixtureDef);
    circleShape.dispose();
  }

  public Room getTilesRoom() {
    return myRoom;
  }

  public boolean getIsFloor() {
    return isFloor;
  }

  public boolean getIsFloorNC() { // getIsFloorAndIsNotCorridor
    if (myRoom != null && myRoom.getIsCorridor()) return false;
    return isFloor;
  }


  public boolean getIsDirt() { return !isFloor; }

  public boolean getIsWeb() {
    return isWeb;
  }

  public boolean getHasPhysics() {
    return hasPhysics;
  }

  public void setHasPhysics(boolean p) {
    hasPhysics = p;
  }

  public double getHeuristic(Tile goal) { // Straight line distance
    return Math.sqrt( Math.pow( getX() - goal.getX(), 2) + Math.pow( getY() - goal.getY(), 2) );
  }

  public double getTraversalCost(Tile neighbour) {
    return 1f; // Web tiles are always one apart and always accessible
  }

  public void updateNeighbours(boolean recurse) {
    final int wnSize = webNeighbours.size();
    webNeighbours.clear();
    webNeighbours = Sprites.getInstance().getNeighbourWeb((int)getX()/Param.TILE_SIZE, (int)getY()/Param.TILE_SIZE, webNeighbours, recurse);
    if (wnSize == webNeighbours.size()) return; // Nothing changed
    boolean N = false, E = false, S = false, W = false;
    for (Tile n : webNeighbours) {
      if (n.getX() == getX() + Param.TILE_SIZE) E = true;
      else if (n.getX() == getX() - Param.TILE_SIZE) W = true;
      else if (n.getY() == getY() + Param.TILE_SIZE) N = true;
      else if (n.getY() == getY() - Param.TILE_SIZE) S = true;
    }
    if (N && S && E && W) setWebTexture("webNSEW");
    else if (N && S && E) setWebTexture("webNSE");
    else if (N && S && W) setWebTexture("webNSW");
    else if (E && W && N) setWebTexture("webEWN");
    else if (E && W && S) setWebTexture("webEWS");
    else if (E && W) setWebTexture("webEW");
    else if (N && S) setWebTexture("webNS");
    else if (N && E) setWebTexture("webNE");
    else if (N && W) setWebTexture("webNW");
    else if (S && E) setWebTexture("webSE");
    else if (S && W) setWebTexture("webSW");
    else if (N) setWebTexture("webN");
    else if (E) setWebTexture("webE");
    else if (S) setWebTexture("webS");
    else if (W) setWebTexture("webW");
    else setWebTexture("webA");
  }

  public Set<Tile> getNeighbours() {
    return webNeighbours;
  }
}
