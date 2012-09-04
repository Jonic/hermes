/**
 * Synchronizes the color of the GlitchySquares
 */
class GlitchyGroup extends Group<GlitchySquare> {
  
  GlitchyGroup(World w) {
    super(w);
  }
  
  public void update() {
    color c = pickColor();
    for (GlitchySquare s : getObjects()) {
      if (s.usingKey()) {
        s.setColor(c);
      }
    }
  }
  
  public color pickColor() {
    return color(int(random(256)), int(random(256)), int(random(256)));
  }
}
