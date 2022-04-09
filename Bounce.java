package Bounce;

import javafx.scene.control.ScrollBar;
import javafx.stage.Screen;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Bounce extends Frame implements WindowListener, ComponentListener, ActionListener, AdjustmentListener, Runnable {
    private static final long serialVersionUID = 10L; // serial version ID

    // declare constants
    private final int WIDTH = 640; // initial frame width
    private final int HEIGHT = 400; // initial frame height
    private final int BUTTONH = 20; // button height
    private final int BUTTONHS = 5; // button height spacing
    private final int MAXObj = 100; // maximum object size
    private final int MINObj = 10; // minimum object size
    private final int SPEED = 50; // initial speed
    private final int SBvisible = 10; // visible Scroll Bar
    private final int SBunit = 1; // Scroll Bar unit step size
    private final int SBblock = 10; // Scroll Bar block step size
    private final int SCROLLBARH = BUTTONH; // scrollbar height
    private final int SOBJ = 21; // initial object width
    private final int DELAY = 15; // timer delay constant

    // declare other variables
    private int WinWidth = WIDTH; // initial frame width
    private int WinHeight = HEIGHT; // initial frame height
    private int ScreenWidth; // drawing screen width
    private int ScreenHeight; // drawing screen height
    private int WinTop = 10; // top of frame
    private int WinLeft = 10; // left side of frame
    private int BUTTONW = 50; // initial button width
    private int CENTER = (WIDTH / 2); // initial screen center
    private int BUTTONS = BUTTONW / 4; // initial button spacing
    private Insets I; // insets of frame
    private int SObj = SOBJ; // initial object width
    private int SpeedSBmin = 1; // speed scrollbar minimum value
    private int SpeedSBmax = 100 + SBvisible; // speed scrollbar maximum value with visible offset
    private int SpeedSBinit = SPEED; // initial speed scrollbar value
    private int ScrollBarW; // scrollbar width
    private Objc Obj; // object to draw
    private Label SPEEDL = new Label("Speed", Label.CENTER); // label for speed scroll bar
    private Label SIZEL = new Label("Size", Label.CENTER); // label for size scroll bar
    private boolean running; // boolean for run method
    private boolean TimePause; // boolean for pause mode
    private boolean started; // boolean for start mode
    private int speed; // int for scrollbar speed
    private int delay; // int for timer delay
    Thread thethread; // thread for timer delay
    Scrollbar SpeedScrollBar, ObjSizeScrollBar; // scroll bars
    Button Start, Shape, Clear, Tail, Quit; // buttons

    Bounce() {
        setLayout(null); // use null layout of frame
        setVisible(true); // make it visible
        MakeSheet(); // Determine the sizes for the sheet
        started = false; // start in pause mode

        try {
            initComponents(); // try to initialize the components
        } catch (Exception e) {
            e.printStackTrace();
        }
        SizeScreen(); // size the items on the screen;
        start();
    }
    public static void main(String[] args) {
        Bounce b = new Bounce(); // create an object
    }

    private void MakeSheet() { // gets the insets and adjusts the sizes of the itmes
        I = getInsets();
        // make screen size the width of the frame less the left and right insets
        ScreenWidth = WinWidth - I.left - I.right;
        // make screen height the height of the frame less the top and bottom insets and space for
        // two rows of buttons and two button spaces
        ScreenHeight = WinHeight - I.top - 2 * (BUTTONH + BUTTONHS) - I.bottom;
        setSize(WinWidth, WinHeight); // set the frame size
        CENTER = (ScreenWidth / 2); // determine the center of the screen
        BUTTONW = ScreenWidth / 11; // determine the width of the buttons (11 units)
        BUTTONS = BUTTONW / 4; // determine the button spacing
        ScrollBarW = 2 * BUTTONW; // determine the scroll bar width
        setBackground(Color.lightGray);
    }

    public void initComponents() throws Exception, IOException {
        delay = DELAY;
        Start = new Button("Run"); // create the start button
        Shape = new Button("Circle"); // create the Shape button
        Clear = new Button("Clear"); // create the clear button
        Tail = new Button("No Tail"); // create the tail button
        Quit = new Button("Quit"); // create the quit button
        add("Center", Start); // add the start button to the frame
        add("Center", Shape); // add the shape button to the frame
        add("Center", Tail); // add the tail button to the frame
        add("Center", Clear); // add the clear button to the frame
        add("Center", Quit); // add the quit button to the frame

        SpeedScrollBar = new Scrollbar(Scrollbar.HORIZONTAL); // create the speed scroll bar
        SpeedScrollBar.setMaximum(SpeedSBmax); // set the max speed
        SpeedScrollBar.setMinimum(SpeedSBmin); // set the min speed
        SpeedScrollBar.setUnitIncrement(SBunit); // set the unit increment
        SpeedScrollBar.setBlockIncrement(SBblock); // set the block increment
        SpeedScrollBar.setValue(SpeedSBinit); // set the initial value
        SpeedScrollBar.setVisibleAmount(SBvisible); // set the visible size
        SpeedScrollBar.setBackground(Color.gray); // set the background color
        ObjSizeScrollBar = new Scrollbar(Scrollbar.HORIZONTAL); // create the size scroll bar
        ObjSizeScrollBar.setMaximum(MAXObj); // set the max speed
        ObjSizeScrollBar.setMinimum(MINObj); // set the min speed
        ObjSizeScrollBar.setUnitIncrement(SBunit); // set the unit increment
        ObjSizeScrollBar.setBlockIncrement(SBblock); // set the block increment
        ObjSizeScrollBar.setValue(SOBJ); // set the initial value
        ObjSizeScrollBar.setVisibleAmount(SBvisible); // set the visible size
        ObjSizeScrollBar.setBackground(Color.gray); // set the background color
        Obj = new Objc(SObj, ScreenWidth, ScreenHeight); // create the object
        Obj.setBackground(Color.white); // set the background color
        add(SpeedScrollBar); // add the speed scroll bar to the frame
        add(ObjSizeScrollBar); // add the size scroll bar to the frame
        add(SPEEDL); // add the speed label to the frame
        add(SIZEL); // add the size label to the frame
        add(Obj); // add the object to the frame
        SpeedScrollBar.addAdjustmentListener(this); // add the speed scroll bar listener
        ObjSizeScrollBar.addAdjustmentListener(this); // add the size scroll bar listener

        Start.addActionListener(this); // add the start button listener
        Shape.addActionListener(this); // add the shape button listener
        Tail.addActionListener(this); // add the tail button listener
        Clear.addActionListener(this); // add the clear button listener
        Quit.addActionListener(this); // add the quit button listener
        this.addComponentListener(this); // add the component listener
        this.addWindowListener(this); // add the window listener
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(getPreferredSize());
        setBounds(WinLeft, WinTop, WIDTH, HEIGHT); // size and position the frame
        TimePause = true;
        running = true;
        validate(); // validate the layout
    }

    private void SizeScreen() {
        // position the buttons
        Start.setLocation(CENTER - 2 * (BUTTONW + BUTTONS) - BUTTONW / 2, ScreenHeight + BUTTONHS + I.top);
        Shape.setLocation(CENTER - BUTTONW - BUTTONS - BUTTONW / 2, ScreenHeight + BUTTONHS + I.top);
        Tail.setLocation(CENTER - BUTTONW / 2, ScreenHeight + BUTTONHS + I.top);
        Clear.setLocation(CENTER + BUTTONS + BUTTONW / 2, ScreenHeight + BUTTONHS + I.top);
        Quit.setLocation(CENTER + BUTTONW + 2 * BUTTONS + BUTTONW / 2, ScreenHeight + BUTTONHS + I.top);
        SpeedScrollBar.setLocation(I.left + BUTTONS, ScreenHeight + BUTTONHS + I.top);
        ObjSizeScrollBar.setLocation(WinWidth - ScrollBarW - I.right - BUTTONS, ScreenHeight + BUTTONHS + I.top);
        SPEEDL.setLocation(I.left + BUTTONS, ScreenHeight + BUTTONHS + BUTTONH + I.top);
        SIZEL.setLocation(WinWidth - ScrollBarW - I.right, ScreenHeight + BUTTONHS + BUTTONH + I.top);

        // size the button
        Start.setSize(BUTTONW, BUTTONH);
        Shape.setSize(BUTTONW, BUTTONH);
        Tail.setSize(BUTTONW, BUTTONH);
        Clear.setSize(BUTTONW, BUTTONH);
        Quit.setSize(BUTTONW, BUTTONH);
        SpeedScrollBar.setSize(ScrollBarW, SCROLLBARH);
        ObjSizeScrollBar.setSize(ScrollBarW, SCROLLBARH);
        SPEEDL.setSize(ScrollBarW, SCROLLBARH);
        SIZEL.setSize(ScrollBarW, SCROLLBARH);
        Obj.setBounds(I.left, I.top, ScreenWidth, ScreenHeight);
    }

    public void start() {
        if (thethread == null) { // create a thread if it does not exist
            thethread = new Thread(this); // create a new thread
            thethread.start(); // start the thread
            Obj.repaint();
        }
    }

    public void stop() {
        // set running flag to false, interrupt the thread
        // remove all listeners and exit
        running = false;
        thethread.interrupt();
        Start.removeActionListener(this);
        Shape.removeActionListener(this);
        Clear.removeActionListener(this);
        Tail.removeActionListener(this);
        Quit.removeActionListener(this);
        SpeedScrollBar.removeAdjustmentListener(this);
        ObjSizeScrollBar.removeAdjustmentListener(this);
        this.removeComponentListener(this);
        this.removeWindowListener(this);
        dispose();
        System.exit(0);
    }

    public void run() {
        while (running) {
            if (!TimePause) {
                started = true;
                Obj.Size();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {

                }
                Obj.repaint();
                Obj.move();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException exception) {

            }
        }
    }

    public void checkObjSize() {
        int x = Obj.getX();
        int y = Obj.getY();
        int obj = Obj.getObjSize();
        int right = x + (obj - 1 / 2) + 1;
        int bottom = y + (obj - 1 / 2) + 1;
        if (right > ScreenWidth) {
            Obj.setX(ScreenWidth - (obj - 1 / 2) - 2);
        }

        if (bottom > ScreenHeight) {
            Obj.setY(ScreenHeight - (obj - 1/ 2) - 2);
        }
    }


    public void componentResized(ComponentEvent e) {
        WinWidth = getWidth();
        WinHeight = getHeight();
        MakeSheet();
        checkObjSize();
        SizeScreen();
        Obj.reSize(ScreenWidth, ScreenHeight);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source == Start) {
            if (Start.getLabel() == "Pause") {
                TimePause = true;
                Start.setLabel("Run");
            } else {
                Start.setLabel("Pause");
                TimePause = false;
            }
            thethread.interrupt();
        }

        if (source == Shape) {
            if (!started) {
                Obj.Clear();
            }
            if (Shape.getLabel() == "Circle") {
                Shape.setLabel("Square");
                Obj.rectangle(false);
            } else {
                Shape.setLabel("Circle");
                Obj.rectangle(true);
            }
            Obj.repaint();
        }

        if (source == Tail) {
            if (Tail.getLabel() == "Tail") {
                Obj.setTail(true);
                Tail.setLabel("No Tail");
            } else {
                Obj.setTail(false);
                Tail.setLabel("Tail");
            }
        }

        if (source == Clear) {
            Obj.Clear();
            Obj.repaint();
        }

        if (source == Quit) {
            stop();
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        int TS; // integer for size scrollbar value
        Scrollbar sb = (Scrollbar)e.getSource(); // get the scrollbar that triggered the event
        if (sb == SpeedScrollBar) {
            speed = (SpeedSBmax + SpeedSBmin - sb.getValue()) - 10; // reverse scrollbar values so that left decreases and right increases
            delay = speed / DELAY; // set delay to be speed divided by delay constant
            thethread.interrupt(); // interrupt the thread so that the new delay can be applied

        }

        if (sb == ObjSizeScrollBar) {
            TS = e.getValue(); // get the value
            TS = (TS / 2) * 2 + 1; // Make odd to account for center position
            if (Obj.checkSize(TS)) { // check if new size will fit within the screen
                Obj.newSize(TS); // pass the new size to the drawing object
                Obj.Size(); // apply the new size to the drawing object
            } else {
                sb.setValue(Obj.getObjSize()); // if not, set scrollbar back to its previous value
            }
        }
        if (!Obj.getTail()) {
            Obj.Clear(); // if not in tail mode, clear the screen
        }
        Obj.repaint(); // force a repaint
    }

    public void componentHidden(ComponentEvent e) {

    }

    public void componentShown(ComponentEvent e) {

    }

    public void componentMoved(ComponentEvent e) {

    }

    public void windowClosing(WindowEvent e) {
        stop();
    }

    public void windowClosed(WindowEvent e) {

    }

    public void windowOpened(WindowEvent e) {

    }

    public void windowActivated(WindowEvent e) {

    }

    public void windowDeactivated(WindowEvent e) {

    }

    public void windowIconified(WindowEvent e) {

    }

    public void windowDeiconified(WindowEvent e) {

    }

}

class Objc extends Canvas {
    private static final long serialVersionUID = 11L;
    private int ScreenWidth;
    private int ScreenHeight;
    private int SObj;
    private int NewSize;
    private int x, y;
    private int xmin, xmax, ymin, ymax, xold, yold;
    private boolean right, down;
    private boolean tail;
    private boolean rect;
    private boolean clear;

    public Objc(int SB, int w, int h) {
        ScreenWidth = w;
        ScreenHeight = h;
        SObj = SB;
        NewSize = SObj;
        rect = true;
        clear = false;
        minMax();
        x = xmin + 1;
        y = ymin + 1;
        down = true;
        right = true;
        tail = true;
    }

    public int getObjSize() {
        return NewSize;
    }

    public void minMax() {
        xmin = (SObj) / 2 + 1;
        xmax = ScreenWidth - 2 - (SObj - 1) / 2;
        ymin = (SObj) / 2 + 1;
        ymax = ScreenHeight - 2 - (SObj - 1) / 2;
    }

    public void move() {
        if (!checkX()) {
            right = !right;
        }
        if (!checkY()) {
            down = !down;
        }
        if (right) {
            x += 1;
        } else {
            x -= 1;
        }
        if (down) {
            y += 1;
        } else {
            y -= 1;
        }
    }

    public void setTail(boolean tail) {
        this.tail = tail;
    }

    public boolean getTail() {
        return this.tail;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return this.x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return this.y;
    }

    public boolean checkX() {
        return x > xmin && x < xmax;
    }

    public boolean checkY() {
        return y > ymin && y < ymax;
    }

    public boolean checkSize(int NSObj) {
        int left = x - (NSObj / 2);
        int right = x + (NSObj / 2);
        int top = y - (NSObj / 2);
        int bottom = y + (NSObj / 2);
        return left > xmin && right < xmax && top > ymin && bottom < ymax;
    }


    public void newSize(int NS) {
        NewSize = NS;
        minMax();
    }
    public void Size() {
        SObj = NewSize;
        minMax();
    }

    public void rectangle(boolean r) {
        rect = r;
    }

    public void reSize(int w, int h) {
        ScreenWidth = w;
        ScreenHeight = h;
        minMax();
    }

    public void Clear() {
        clear = true;
    }

    public void paint(Graphics g) {
        g.setColor(Color.red);
        g.drawRect(0, 0, ScreenWidth - 1, ScreenHeight - 1);
        update(g);
    }

    public void update(Graphics g) {
        if (clear) {
            super.paint(g);
            clear = false;
            g.setColor(Color.red);
            g.drawRect(0, 0, ScreenWidth - 1, ScreenHeight - 1);
        }

        if (!tail) {
            g.setColor(getBackground());
            if (rect) {
                g.fillRect(xold - (SObj - 1) / 2, yold - (SObj - 1) / 2, SObj, SObj);
            } else {
                g.fillOval(xold - (SObj - 1) / 2 - 1, yold - (SObj - 1) / 2 - 1, SObj + 2, SObj + 2);
            }
        }

        if (rect) {
            g.setColor(Color.lightGray);
            g.fillRect(x - (SObj - 1) / 2, y - (SObj - 1) / 2, SObj, SObj);
            g.setColor(Color.black);
            g.drawRect(x - (SObj - 1) / 2, y - (SObj - 1) / 2, SObj - 1, SObj - 1);
        } else {
            g.setColor(Color.lightGray);
            g.fillOval(x - (SObj - 1) / 2, y - (SObj - 1) / 2, SObj, SObj);
            g.setColor(Color.black);
            g.drawOval(x - (SObj - 1) / 2, y - (SObj - 1) / 2, SObj - 1, SObj - 1);
        }
        xold = x;
        yold = y;
    }

}

