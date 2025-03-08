import javax.sound.sampled.*;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleFPSGame extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 800;
    private static final int PLAYER_SIZE = 30;
    private static final int BULLET_SPEED = 10;
    private static final int TARGET_SIZE = 20;
    private static final int ENEMY_SPEED = 1;
    private static final int GAME_DURATION = 600; // Game duration in seconds

    private Timer timer;
    private int playerX, playerY;
    private List<Point> bullets;
    private List<Point> targets;
    private boolean[] keys;
    private int score;
    private boolean gameOver;
    private boolean gameWon;
    private boolean gamePaused;
    private boolean gameStarted; // Track if the game has started
    private boolean inSettings; // Track if the settings menu is active
    private boolean inStartMenu;
    private Random random;
    private int timeLeft; // Time left in seconds

    // Sound clips
    private Clip shootSound;
    private Clip hitSound;
    private Clip gameOverSound;

    public SimpleFPSGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        playerX = WIDTH / 2;
        playerY = HEIGHT / 2;
        bullets = new ArrayList<>();
        targets = new ArrayList<>();
        keys = new boolean[256];
        score = 0;
        gameOver = false;
        gameWon = false;
        gamePaused = false;
        gameStarted = false; // Game starts in the menu
        inSettings = false; // Settings menu is not active by default
        inStartMenu = true;
        random = new Random();
        timeLeft = GAME_DURATION;

        // Load sounds
        loadSounds();

        // Add some targets
        for (int i = 0; i < 9; i++) {
            targets.add(new Point(random.nextInt(WIDTH), random.nextInt(1)));
        }

        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    private void loadSounds() {
        try {
            // Load shooting sound
            AudioInputStream shootAudio = AudioSystem.getAudioInputStream(new File("sounds/shoot.wav"));
            shootSound = AudioSystem.getClip();
            shootSound.open(shootAudio);

            // Load hit sound
            AudioInputStream hitAudio = AudioSystem.getAudioInputStream(new File("sounds/hit.wav"));
            hitSound = AudioSystem.getClip();
            hitSound.open(hitAudio);

            // Load game over sound
            AudioInputStream gameOverAudio = AudioSystem.getAudioInputStream(new File("sounds/gameover.wav"));
            gameOverSound = AudioSystem.getClip();
            gameOverSound.open(gameOverAudio);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            System.out.println("Error loading sound files!");
        }
    }

    private void playSound(Clip sound) {
        if (sound != null) {
            sound.setFramePosition(0); // Rewind to the beginning
            sound.start();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!gameStarted) {
            // Draw start menu
            drawStartMenu(g);
            return; // Don't draw the game if it hasn't started
        }

        if (inSettings) {
            // Draw settings menu
            drawSettingsMenu(g);
            return; // Don't draw the game if in settings
        }

        // Draw player
        g.setColor(Color.GREEN);
        g.fillRect(playerX - PLAYER_SIZE / 2, playerY - PLAYER_SIZE / 2, PLAYER_SIZE, PLAYER_SIZE);

        // Draw bullets
        g.setColor(Color.WHITE);
        for (Point bullet : bullets) {
            g.fillOval(bullet.x - 5, bullet.y - 5, 10, 10);
        }

        // Draw targets
        g.setColor(Color.BLUE);
        for (Point target : targets) {
            g.fillOval(target.x - TARGET_SIZE / 2, target.y - TARGET_SIZE / 2, TARGET_SIZE, TARGET_SIZE);
        }

        // Draw score
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);

        // Draw timer
        g.drawString("Time Left: " + timeLeft/20 + "s", WIDTH - 120, 20);

        // Draw game over message
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", WIDTH / 2 - 100, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to Restart", WIDTH / 2 - 80, HEIGHT / 2 + 40);
            g.drawString("Press Enter to exit the game", WIDTH / 2 - 80, HEIGHT / 2 + 60);
            g.drawString("Press Shift to go to the main menu", WIDTH / 2 - 80, HEIGHT / 2 + 80);
        }

        // Draw game won message
        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME WON", WIDTH / 2 - 100, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to Restart", WIDTH / 2 - 80, HEIGHT / 2 + 40);
            g.drawString("Press Enter to exit the game", WIDTH / 2 - 80, HEIGHT / 2 + 60);
            g.drawString("Press Shift to go to the main menu", WIDTH / 2 - 80, HEIGHT / 2 + 80);
        }

        // Draw game paused message
        if (gamePaused) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME PAUSED", WIDTH / 2 - 100, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press P to Resume", WIDTH / 2 - 80, HEIGHT / 2 + 40);
            g.drawString("Press Shift to go to the main menu", WIDTH / 2 - 80, HEIGHT / 2 + 80);
        }
    }

    private void drawStartMenu(Graphics g) {
        // Draw background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("StArWaRs", WIDTH / 2 - 120, HEIGHT / 2 - 50);

        // Draw instructions
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press ENTER to Start", WIDTH / 2 - 100, HEIGHT / 2 + 20);
        g.drawString("Press S for Settings", WIDTH / 2 - 100, HEIGHT / 2 + 50);
        g.drawString("Use WASD or Arrow Keys to Move", WIDTH / 2 - 150, HEIGHT / 2 + 80);
        g.drawString("Press SPACE to Shoot", WIDTH / 2 - 100, HEIGHT / 2 + 110);
        g.drawString("Press Esc to EXIT",WIDTH/2 - 100, HEIGHT/ 2 + 140);
    }

    private void drawSettingsMenu(Graphics g) {
        // Draw background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Settings", WIDTH / 2 - 60, HEIGHT / 2 - 50);

        // Draw instructions
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Volume: [Not Implemented]", WIDTH / 2 - 100, HEIGHT / 2 + 20);
        g.drawString("Press ESC to Return to Main Menu", WIDTH / 2 - 150, HEIGHT / 2 + 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameStarted || gameOver || gameWon || gamePaused || inSettings) {
            return; // Don't update the game if it hasn't started or is paused/over
        }

        // Move player
        if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) playerY -= 5;
        if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]) playerY += 5;
        if (keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]) playerX -= 5;
        if (keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]) playerX += 5;

        // Move bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Point bullet = bullets.get(i);
            bullet.y -= BULLET_SPEED; // Bullets move upward

            // Remove bullets that go off-screen
            if (bullet.y < 0) {
                bullets.remove(i);
                score -= 10; // Deduct 10 points for missing
            }

            // Check for collisions with targets
            for (int j = targets.size() - 1; j >= 0; j--) {
                Point target = targets.get(j);
                if (bullet.distance(target) < TARGET_SIZE / 2) {
                    // Bullet hits the target
                    targets.remove(j);
                    bullets.remove(i);
                    score += 10; // Add 10 points for hitting the target
                    playSound(hitSound); // Play hit sound
                    break;
                }
            }
        }

        // Move targets
        for (Point target : targets) {
            target.y += ENEMY_SPEED; // Targets move downward

            // Check if target reaches player
            if (new Point(playerX, playerY).distance(target) < PLAYER_SIZE / 2 + TARGET_SIZE / 2) {
                gameOver = true;
                playSound(gameOverSound); // Play game over sound
            }
        }

        // Update timer
        if (timeLeft > 0) {
            timeLeft--;
        } else {
            gameOver = true;
            playSound(gameOverSound); // Play game over sound
        }

        // Check if score is less than -50
        if (score <= -50) {
            gameOver = true;
            playSound(gameOverSound); // Play game over sound
        }

        // Check if score is 50 or more
        if (score >= 50) {
            gameWon = true;
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;

        // Start the game when ENTER is pressed
        if (e.getKeyCode() == KeyEvent.VK_ENTER && !gameStarted) {
            gameStarted = true;
        }

        // Open settings menu when O is pressed
        if (e.getKeyCode() == KeyEvent.VK_O && !gameStarted) {
            inSettings = true;
        }

        // Return to main menu from settings
        if (e.getKeyCode() == KeyEvent.VK_O && inSettings) {
            inSettings = false;
        }
        if (e.getKeyCode()== KeyEvent.VK_ESCAPE && !gameStarted){
            System.exit(0);
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE && gameStarted){
            System.exit(80);
        }
        if (e.getKeyCode()== KeyEvent.VK_SHIFT && gameWon||gameOver){
            inStartMenu = true;
        }

        // Shoot bullet
        if (e.getKeyCode() == KeyEvent.VK_SPACE && gameStarted && !gameOver && !gameWon && !gamePaused && !inSettings) {
            bullets.add(new Point(playerX, playerY));
            playSound(shootSound); // Play shooting sound
        }

        // Pause the Game
        if (e.getKeyCode() == KeyEvent.VK_P && gameStarted && !inSettings) {
            gamePaused = !gamePaused;
            if (gamePaused) {
                timer.stop();
            } else {
                timer.start();
            }
        }

        // Restart game
        if (e.getKeyCode() == KeyEvent.VK_R && (gameOver || gameWon)) {
            resetGame();
        }

        // Exit the game
        if (e.getKeyCode() == KeyEvent.VK_ENTER && (gameOver)) {
            System.exit(10);
        }
        if (e.getKeyCode()==KeyEvent.VK_ENTER && gameWon){
            System.exit(11);
        }
        // Return to main menu
        if (e.getKeyCode() == KeyEvent.VK_SHIFT && (gameOver || gameWon || gamePaused)) {
            gameStarted = false;
            gameOver = false;
            gameWon = false;
            gamePaused = false;
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void resetGame() {
        playerX = WIDTH / 2;
        playerY = HEIGHT / 2;
        bullets.clear();
        targets.clear();
        score = 0;
        gameOver = false;
        gameWon = false;
        gamePaused = false;
        timeLeft = GAME_DURATION;

        // Add new targets
        for (int i = 0; i < 9; i++) {
            targets.add(new Point(random.nextInt(WIDTH), random.nextInt(1)));
        }

        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public static void main(String[] args) {
        // Create the game instance
        SimpleFPSGame game = new SimpleFPSGame();

        // Create the JFrame
        JFrame frame = new JFrame("StArWaRs");
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Disable resizing and decorations
        frame.setResizable(false);
        frame.setUndecorated(true); // Remove title bar and borders

        // Get the default graphics device
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Enable full-screen mode
        if (device.isFullScreenSupported()) {
            device.setFullScreenWindow(frame);
        } else {
            System.err.println("Full-screen mode not supported");
            frame.setSize(WIDTH, HEIGHT); // Fallback to windowed mode
        }

        // Make the frame visible
        frame.setVisible(true);
    }
}