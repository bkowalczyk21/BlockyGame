import java.lang.invoke.MethodHandles;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.Box;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.Rectangle2D;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Blocky {

    private int cells;
    private QuadTree tree;

    private final JFrame frame;

    private final Color[] colors = {
        Color.BLACK,
        new Color(255, 128, 128),
        new Color(128, 255, 128),
        new Color(128, 128, 255),
        new Color(255, 255, 128)
    };

    private final String[] colorNames = {
        "Black",
        "Red",
        "Green",
        "Blue",
        "Yellow"
    };

    private JLabel colorGoal = new JLabel();
    private JLabel goalSize = new JLabel();
    private int goalNumber;
    private int currentSize;

    private JPanel statusPanel;
    private BlockDisplay blockDisplay;
    private JPanel actionPanel;

    private class BlockDisplay extends JPanel implements MouseInputListener, ComponentListener, ActionListener {

        private static final long serialVersionUID = 1L;

        private int mouseMode = 0;

        private QuadTree tree;

        BlockDisplay(QuadTree tree) {
            super(true);

            this.tree = tree;

            addMouseListener(this);
            addMouseMotionListener(this);
            addComponentListener(this);

        }

        private Rectangle2D.Double startSelection;
        private Rectangle2D.Double endSelection;
        private Point startsq;
        private Point endsq;

        private AffineTransform saveXform;
        private Stroke saveStroke;

        private Graphics2D txGraphics(Graphics gr) {
            Graphics2D g = (Graphics2D) gr;
            saveXform = g.getTransform();

            AffineTransform Tx = new AffineTransform();
            Tx.setToIdentity();

            Dimension size = getSize();
            Tx.translate(size.getWidth()/2, size.getHeight()/2);
            double length = Math.min(size.getWidth(), size.getHeight());
            Tx.scale(length, length);
            g.transform(Tx);

            saveStroke = g.getStroke();
            g.setStroke(new BasicStroke((float) (3.0/length)));

            return g;
        }

        private void restoreGraphics(Graphics2D g)
        {
            g.setStroke(saveStroke);
            g.setTransform(saveXform);
        }

        private void paintBorder(Graphics2D g)
        {
            g.setColor(Color.BLACK);
            g.draw(new Rectangle2D.Double(-0.5, -0.5, 1, 1));
        }

        private void paintStartSelection(Graphics2D g, boolean set)
        {
            if (startSelection != null) {
                paintBorder(g);

                if (set)
                    g.setColor(Color.ORANGE);
                else
                    g.setColor(colors[0]);

                g.draw(startSelection);
            }
        }

        private void paintEndSelection(Graphics2D g, boolean set)
        {
            if (endSelection != null) {
                paintBorder(g);

                if (set)
                    g.setColor(Color.CYAN);
                else
                    g.setColor(colors[0]);

                g.draw(endSelection);
            }
        }

        public void paint(Graphics gr) {
            super.paint(gr);
            Graphics2D g = txGraphics(gr);

            drawTree(g, 1, tree.getRoot(), 0, 0);

            paintStartSelection(g, true);
            paintEndSelection(g, true);

            restoreGraphics(g);
        }

        private void drawTree(Graphics2D g, double length, QuadNode node, double x, double y) {
            if (node.isLeaf()) {
                g.setColor(colors[node.getColor()]);
                g.fill(new Rectangle2D.Double(x - length/2, y - length/2, length, length));
                g.setColor(colors[0]);
                g.draw(new Rectangle2D.Double(x - length/2, y - length/2, length, length));
            } else {
                drawTree(g, length/2, node.getChild(0), x-length/4, y-length/4);
                drawTree(g, length/2, node.getChild(1), x+length/4, y-length/4);
                drawTree(g, length/2, node.getChild(2), x+length/4, y+length/4);
                drawTree(g, length/2, node.getChild(3), x-length/4, y+length/4);
            }
        }

        private Point mouseInBounds(MouseEvent e) {
            Dimension size = getSize();
            int width = (int) size.getWidth();
            int height = (int) size.getHeight();
            int length = Math.min(width, height);

            int x = e.getX();
            int y = e.getY();

            if (x >= (width/2 - length/2) &&
                x <= (width/2 + length/2) &&
                y >= (height/2 - length/2) &&
                y <= (height/2 + length/2))
            {
                int offsetX = x - (width/2 - length/2);
                int offsetY = y - (height/2 - length/2);
                int units = length/cells;

                return new Point(offsetX/units, offsetY/units);
            } else
                return null;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Point sq = mouseInBounds(e);
            if (mouseMode == 0 && sq != null) {
                mouseMode = 1;
                ((CardLayout) actionPanel.getLayout()).show(actionPanel, "END");
            } else if (mouseMode == 1) {
                Graphics2D g = txGraphics(getGraphics());
                paintStartSelection(g, false);
                startSelection = null;
                paintEndSelection(g, true);
                restoreGraphics(g);

                if (sq == null || endSelection == null) {
                    mouseMode = 0;
                    ((CardLayout) actionPanel.getLayout()).show(actionPanel, "START");
                } else {
                    mouseMode = 2;
                    ((CardLayout) actionPanel.getLayout()).show(actionPanel, "EXECUTE");
                }
            } else if (mouseMode == 2) {
                cancelAction();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            return;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            return;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            return;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            return;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            return;
        }

        private Rectangle2D.Double startSelection(Point sq, QuadNode node, int level, int posx, int posy, double x, double y, double width, double height) {
            if (node.isLeaf()) {
                return new Rectangle2D.Double(x, y, width, height);
            }
            else {
                level *= 2;
                final int blocksize = cells/level;
                if (sq.x < posx + blocksize && sq.y < posy + blocksize) {
                    return startSelection(sq, node.getChild(0), level, posx, posy, x, y, width/2.0, height/2.0);
                } else if (sq.x >= posx + blocksize && sq.y < posy + blocksize) {
                    return startSelection(sq, node.getChild(1), level, posx + blocksize, posy, x+1.0/level, y, width/2.0, height/2.0);
                } else if (sq.x >= posx + blocksize && sq.y >= posy + blocksize) {
                    return startSelection(sq, node.getChild(2), level, posx + blocksize, posy + blocksize, x+1.0/level, y+1.0/level, width/2.0, height/2.0);
                } else if (sq.x < posx + blocksize && sq.y >= posy + blocksize) {
                    return startSelection(sq, node.getChild(3), level, posx, posy + blocksize, x, y+1.0/level, width/2.0, height/2.0);
                } else {
                    return null;
                }
            }
        }

        private Rectangle2D.Double endSelection(Point sq, QuadNode node, int level, int posx, int posy, double x, double y, double width, double height) {
            if (node.isLeaf()) {
                return new Rectangle2D.Double(x, y, width, height);
            }
            else {
                level *= 2;
                final int blocksize = cells/level;
                if (sq.x < posx + blocksize && sq.y < posy + blocksize &&
                        startsq.x < posx + blocksize && startsq.y < posy + blocksize) {
                    return endSelection(sq, node.getChild(0), level, posx, posy, x, y, width/2.0, height/2.0);
                } else if (sq.x >= posx + blocksize && sq.y < posy + blocksize &&
                        startsq.x >= posx + blocksize && startsq.y < posy + blocksize) {
                    return endSelection(sq, node.getChild(1), level, posx + blocksize, posy, x+1.0/level, y, width/2.0, height/2.0);
                } else if (sq.x >= posx + blocksize && sq.y >= posy + blocksize &&
                        startsq.x >= posx + blocksize && startsq.y >= posy + blocksize) {
                    return endSelection(sq, node.getChild(2), level, posx + blocksize, posy + blocksize, x+1.0/level, y+1.0/level, width/2.0, height/2.0);
                } else if (sq.x < posx + blocksize && sq.y >= posy + blocksize &&
                        startsq.x < posx + blocksize && startsq.y >= posy + blocksize) {
                    return endSelection(sq, node.getChild(3), level, posx, posy + blocksize, x, y+1.0/level, width/2.0, height/2.0);
                } else {
                    return new Rectangle2D.Double(x, y, width, height);
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (mouseMode == 2) return;

            Point sq = mouseInBounds(e);

            if (sq != null) {
                if (mouseMode == 0) {
                    startsq = sq;
                    Graphics2D g = txGraphics(getGraphics());
                    paintStartSelection(g, false);
                    startSelection = startSelection(sq, tree.getRoot(), 1, 0, 0, -0.5, -0.5, 1.0, 1.0);
                    paintStartSelection(g, true);
                    restoreGraphics(g);
                }
                
                else if (mouseMode == 1) {
                    Graphics2D g = txGraphics(getGraphics());
                    paintEndSelection(g, false);
                    
                    if (sq.x == startsq.x && sq.y == startsq.y)
                        endSelection = startSelection;
                    else 
                        endSelection = endSelection(sq, tree.getRoot(), 1, 0, 0, -0.5, -0.5, 1.0, 1.0);
                        
                    paintStartSelection(g, true);
                    if (endSelection != null) {
                        paintEndSelection(g, true);
                        endsq = sq;
                    }
                    restoreGraphics(g);
                }
            }
        }

        @Override
        public void componentResized(ComponentEvent e) {
            paint(getGraphics());

        }

        @Override
        public void componentMoved(ComponentEvent e) {
            return;
        }

        @Override
        public void componentShown(ComponentEvent e) {
            return;
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            return;
        }

        private void cancelAction()
        {
            Graphics2D g = txGraphics(getGraphics());
            paintEndSelection(g, false);
            endSelection = null;
            restoreGraphics(g);

            mouseMode = 0;
            ((CardLayout) actionPanel.getLayout()).show(actionPanel, "START");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch(e.getActionCommand()) {
                case "CANCEL":
                    cancelAction();
                    return;

                case "ROTL":
                    tree.rotateLeft(startsq.x, startsq.y, endsq.x, endsq.y);
                    break;

                case "ROTR":
                    tree.rotateRight(startsq.x, startsq.y, endsq.x, endsq.y);
                    break;

                case "SWAPH":
                    tree.swapHorizontal(startsq.x, startsq.y, endsq.x, endsq.y);
                    break;

                case "SWAPV":
                    tree.swapVertical(startsq.x, startsq.y, endsq.x, endsq.y);
                    break;

                case "SMASH":
                    tree.smash(startsq.x, startsq.y, endsq.x, endsq.y);
                    break;
            }

            updateGoalSize();
            cancelAction();

            paint(getGraphics());

        }



    }

    private int depthext(int depth) {
        int d = 1;
        for (int i = 0; i < depth; i++) {
            d *= 2;
        }
        return d;
    }

    private void updateGoalSize() {
        currentSize = tree.largestBlobSize(goalNumber);
        goalSize.setText(String.format("Largest Blob Size: %d", currentSize));
    }

    private Blocky(int depth) {
        this.cells = depthext(depth);
        this.tree = new QuadTree(depth);
        this.goalNumber = (int) (Math.random() * 4 + 1);

        frame = new JFrame("Blocky");

        colorGoal = new JLabel(String.format("Target color: %s", colorNames[goalNumber]));
        goalSize = new JLabel();
        updateGoalSize();

        addWindowContents(tree);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(100, 100); // x, y
		frame.setResizable(true);
		frame.pack();
		frame.setVisible(true);

    }

    private void addWindowContents(QuadTree tree) {
        final Container pane = frame.getContentPane();

        pane.setLayout(new BorderLayout(0, 20));
        
        statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
        statusPanel.add(Box.createRigidArea(new Dimension(20,20)));
        statusPanel.add(colorGoal);
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(goalSize);
        statusPanel.add(Box.createRigidArea(new Dimension(20,20)));

        blockDisplay = new BlockDisplay(tree);
        blockDisplay.setPreferredSize(new Dimension(400, 400));

        actionPanel = new JPanel(new CardLayout());

        JPanel actionStart = new JPanel();
        actionStart.add(new JLabel("Click a block to begin selection"));

        JPanel actionEnd = new JPanel();
        actionEnd.add(new JLabel("Move mouse and click to broaden selection"));

        JPanel actionExecute = new JPanel();
        JButton button;
        button = new JButton("Rotate Left");
        button.setActionCommand("ROTL");
        button.addActionListener(blockDisplay);
        actionExecute.add(button);

        button = new JButton("Rotate Right");
        button.setActionCommand("ROTR");
        button.addActionListener(blockDisplay);
        actionExecute.add(button);

        button = new JButton("Swap Horizontal");
        button.setActionCommand("SWAPH");
        button.addActionListener(blockDisplay);
        actionExecute.add(button);

        button = new JButton("Swap Vertical");
        button.setActionCommand("SWAPV");
        button.addActionListener(blockDisplay);
        actionExecute.add(button);

        button = new JButton("Smash");
        button.setActionCommand("SMASH");
        button.addActionListener(blockDisplay);
        actionExecute.add(button);

        button = new JButton("Cancel");
        button.setActionCommand("CANCEL");
        button.addActionListener(blockDisplay);
        actionExecute.add(button);

        actionPanel.add(actionStart, "START");
        actionPanel.add(actionEnd, "END");
        actionPanel.add(actionExecute, "EXECUTE");

        pane.add(statusPanel, BorderLayout.PAGE_START);
        pane.add(blockDisplay, BorderLayout.CENTER);
        pane.add(actionPanel, BorderLayout.PAGE_END);
        
    }

	public static void main(String [] args) {

        final int depth;
        if (args.length > 0) {
            try {
                depth = Integer.parseInt(args[0]);
                if (depth < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.err.printf("%s: error: argument is not a non-negative integer\n", MethodHandles.lookup().lookupClass());
                System.exit(1);
                return;
            }
        } else {
            depth = 5;
        }

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Blocky(depth);
			}
		});
	}
}