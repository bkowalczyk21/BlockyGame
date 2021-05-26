public class QuadTree {
    QuadNode root;
    int gridLength;
    int numUnitCells = gridLength * gridLength;

    public QuadTree(int depth) {
        root = new QuadNode();

        randomTree(root, 0, depth);
        
        gridLength = 2**depth;
    }

    public QuadNode getRoot() { return root; }

    private void randomTree(QuadNode node, int level, int maxdepth) {
        double r = Math.random();
        if (level < maxdepth && r < Math.exp(-0.25 * level)) {
            node.makeChildren();
            for (int i = 0; i < 4; i++)
                randomTree(node.getChild(i), level+1, maxdepth);
        }
        else {
            node.setColor((int) (r * 4) + 1);
        }
    }

    public int largestBlobSize(int color) {
        return 0;
    }

    public void rotateLeft(int b1x, int b1y, int b2x, int b2y) {
        /*
        change the order of the children the children in the node's children array
        ie. 
            temp = children[0]
            children[0] = children[1]
            children[1] = children[2]
            children[2] = children[3]
            children[3] = temp
        */
        QuadNode block = findBlockInTree(int b1x, int b1y, int b2x, int b2y)
    }


    public void rotateRight(int b1x, int b1y, int b2x, int b2y) {
        /*
        same thing as left rotation, children array rotated the other way
        ie. 
            temp = children[0]
            children[0] = children[3]
            children[3] = children[2]
            children[2] = children[1]
            children[1] = temp
        */
        QuadNode block = findBlockInTree(int b1x, int b1y, int b2x, int b2y)
      
    }


    public void swapHorizontal(int b1x, int b1y, int b2x, int b2y) {
        /* 
        rearrange children array to switch rows
        ie.
        temp0 = children[0]
        temp1 = children[1]
        children[0] = children[3]
        children[1] = children[2]
        children[3] = temp0
        children[2] = temp1
        */
        QuadNode block = findBlockInTree(int b1x, int b1y, int b2x, int b2y)
    }


    public void swapVertical(int b1x, int b1y, int b2x, int b2y) {
        /* 
        rearrange children array to switch cols
        ie.
        temp0 = children[0]
        temp3 = children[3]
        children[0] = children[1]
        children[3] = children[2]
        children[1] = temp0
        children[2] = temp3
        */
        QuadNode block = findBlockInTree(int b1x, int b1y, int b2x, int b2y)
        
    }


    public void smash(int b1x, int b1y, int b2x, int b2y) {
        /* 
        if block is not at max depth, create 4 new children for the node passed in (discard old ones if they exist)
        */
        QuadNode block = findBlockInTree(int b1x, int b1y, int b2x, int b2y)
        
    }

    // get to the cell in the tree specified by the coordinates passed as parameters
    public QuadNode findBlockInTree(int b1x, int b1y, int b2x, int b2y){

        if ()
    }
}
