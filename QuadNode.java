public class QuadNode {

    private int color = 0;
    private QuadNode[] children;

    public void makeChildren() {
        children = new QuadNode[4];
        for (int i = 0; i < 4; i++)
            children[i] = new QuadNode();
    }

    public boolean isLeaf() { return children == null; }

    public boolean setColor(int color) {
        if (color >= 0 && color <= 4) {
            this.color = color;
            return true;
        } else {
            return false;
        }
    }

    public int getColor() {
        return color;
    }

    public QuadNode getChild(int index) {
        if (children != null)
            return children[index];
        else
            return null;
    }

}
