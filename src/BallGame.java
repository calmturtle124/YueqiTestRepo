import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class BallGame extends JFrame {
    private static final int MOVE_TIMER_DELAY = 30;
    private static final int PROCESS_TIMER_DELAY = 1000;

    private JPanel ballsPanel;
    private Timer moveTimer;
    private Timer processUpdateTimer;
    private String currentUser;
    private HashMap<String, Ball> ballsMap;
    private Random random;

    public BallGame() {
        setTitle("Ball Game");
        setSize(500, 500); // default size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ballsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (Ball ball : ballsMap.values()) {
                    g.setColor(ball.getColor());
                    g.fillOval(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
                }
            }
        };
        add(ballsPanel, BorderLayout.CENTER);

        JPanel commandPanel = new JPanel();
        JButton startButton = new JButton("New Game");
        JButton switchButton = new JButton("Switch");
        JButton quitButton = new JButton("Quit");

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    startMonitor();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        switchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Objects.equals(currentUser, "maoyueqi")) {
                    currentUser = "root";
                } else {
                    currentUser = "maoyueqi";
                }
                try {
                    getNewBallsMap();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitGame();
            }
        });

        commandPanel.add(startButton);
        commandPanel.add(switchButton);
        commandPanel.add(quitButton);

        add(commandPanel, BorderLayout.SOUTH);

        random = new Random();
        currentUser = "maoyueqi";
        ballsMap = new HashMap<>();

        moveTimer = new Timer(MOVE_TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveUpdate();
                repaint();
            }
        });

        processUpdateTimer = new Timer(PROCESS_TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    getNewBallsMap();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                repaint();
            }
        });

        setVisible(true);
    }

    private void startMonitor() throws IOException {
        String inputWidth = JOptionPane.showInputDialog("Enter the width of the panel:");
        String inputHeight = JOptionPane.showInputDialog("Enter the height of the panel:");
        int iw = Integer.parseInt(inputWidth);
        int ih = Integer.parseInt(inputHeight);

        setSize(iw, ih);

        ballsMap.clear();
        getNewBallsMap(); // change name to create balls

        moveTimer.start();
        processUpdateTimer.start();
    }

    private void getNewBallsMap() throws IOException {
        Process p = Runtime.getRuntime().exec("ps aux");
        InputStream is = p.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        HashMap tmp = new HashMap<>();
        reader.readLine(); // first line
        while ((line = reader.readLine()) != null) {
            String[] ls = line.split("\\s+");
            if (!Objects.equals(ls[0], currentUser)) {
                continue;
            }
            long sizeLong = Long.parseLong(ls[5]);
            double sizeLongInMB = sizeLong / 1024.0;
            double logSize = Math.log(sizeLongInMB);
            int size = (int) Math.round(logSize * 2) + 1;

            float speed = Float.parseFloat(ls[2]) + 0.1f; // cpu, get ceiling based on .1

            if (ballsMap.containsKey(ls[1])) {
                Ball b = ballsMap.get(ls[1]);
                b.size = size;
                b.speed = speed;
                tmp.put(ls[1], b);
            } else {
                int x = random.nextInt(getWidth() - size);
                int y = random.nextInt(getHeight() - size);
                int angle = random.nextInt(360);

                if (Objects.equals(currentUser, "maoyueqi")) {
                    // pid = 256^2* R + 256* G + B
                    int pid = Integer.parseInt(ls[1]);
                    int r = pid / 65536;
                    int g = (pid/256) % 256;
                    int b = pid % 256;
                    Color color = new Color(r, g, b);
                    tmp.put(ls[1],  new Ball(x, y, speed, angle, color, size));
                } else {
                    // pid = 256^2* B + 256* G + R
                    int pid = Integer.parseInt(ls[1]);
                    int r = pid % 256;
                    int g = (pid/256) % 256;
                    int b = pid / 65536;
                    Color color = new Color(r, g, b);
                    tmp.put(ls[1],  new Ball(x, y, speed, angle, color, size));
                }
            }
        }
        reader.close();
        ballsMap.clear();
        ballsMap.putAll(tmp);
    }


    private void quitGame() {
        moveTimer.stop();
        processUpdateTimer.stop();
        dispose(); // System.exit()
    }

    private void moveUpdate() {
        for (Ball ball : ballsMap.values()) {
            ball.move(getWidth(), getHeight());
        }
    }

    public static void main(String[] args) {
        new BallGame();
    }

    private class Ball {
        private int x;
        private int y;
        private float speed;
        private int angle;
        private Color color;
        private int size;

        public Ball(int x, int y, float speed, int angle, Color color, int size) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.angle = angle;
            this.color = color;
            this.size = size;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Color getColor() {
            return color;
        }

        public int getSize() {return size;}

        // citation: this method is initially came from chatGPT
        public void move(int width, int height) {
            double radians = Math.toRadians(angle);
            int nx = (int) (speed * Math.cos(radians));
            int ny = (int) (speed * Math.sin(radians));

            x += nx;
            y += ny;

            // turn
            if (x < 0 || x + this.size > width) {
                angle = 180 - angle;
            }
            if (y < 0 || y + this.size > height) {
                angle = -angle;
            }
        }

    }
}