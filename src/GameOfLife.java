import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;

public class GameOfLife {
	static JFrame sizeSelect, game, start;
	static JSpinner sizeX, sizeY;
	static JButton startButton;
	static JButton[][] tiles; // 2D Array of Game Tiles
	static boolean[][] next; // 2D Array of Tile States in Following Update
	static Font a = new Font("Times New Roman", Font.BOLD, 60);
	static Timer t = new Timer(200, new Update());
	static int width, height; // Width and Height of the Game Grid
	static boolean mouseDown; // Tracks Left Mouse Button State

	// Makes the Size Selection Frame
	public static void main(String[] args) {
		// Makes Frame
		sizeSelect = new JFrame("Size Selection");
		sizeSelect.setSize(400, 250);
		sizeSelect.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sizeSelect.setLocationRelativeTo(null);
		sizeSelect.setLayout(new GridLayout(2, 1, 5, 5));

		// Adds Spinners to Frame
		JPanel spinners = new JPanel();
		spinners.setLayout(new GridLayout(1, 2, 5, 5));
		sizeX = new JSpinner(new SpinnerNumberModel(50, 5, 50, 1));
		sizeY = new JSpinner(new SpinnerNumberModel(50, 5, 50, 1));
		sizeX.setFont(a);
		sizeY.setFont(a);
		spinners.add(sizeX);
		spinners.add(sizeY);
		sizeSelect.add(spinners);

		// Adds Confirm Button to Frame
		JButton confirm = new JButton("Confirm");
		confirm.addMouseListener(new ButtonHandler());
		confirm.setFont(a);
		sizeSelect.add(confirm);

		// Renders Frame Visible
		sizeSelect.setVisible(true);
	}

	// Makes the Start and Game Frames
	public GameOfLife() {
		// Gets Size of Grid
		width = (int) sizeX.getValue();
		height = (int) sizeY.getValue();
		int grid = Math.round(800f / height); // Pixel Size of Each Tile

		// Makes Start Frame
		start = new JFrame();
		start.setSize(200, 100);
		start.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Adds Start Button to Start Frame
		startButton = new JButton("Start");
		startButton.addMouseListener(new ButtonHandler());
		startButton.setFont(a);
		start.add(startButton);

		// Makes Game Frame
		game = new JFrame("The Game of Life");
		game.setSize((width-1) * grid, height * grid);
		game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.setLocationRelativeTo(null);
		game.setLayout(new GridLayout(height, width));

		// Sets Tile Arrays' Sizes
		tiles = new JButton[width][height];
		next = new boolean[width][height];

		// Fills Tile Grid
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Creates Tile Clones
				JButton clone = new JButton();
				clone.setFont(new Font("Arial", Font.PLAIN, 0));
				clone.setForeground(Color.BLACK);
				clone.setBackground(Color.BLACK);
				clone.addMouseListener(new ButtonHandler());

				// Sets Tiles Arrays' States
				tiles[x][y] = clone;
				next[x][y] = false;
				// Adds To Grid
				game.add(clone);
			}
		}

		// Renders Frames Visible
		start.setVisible(true);
		game.setVisible(true);
	}

	private static class ButtonHandler implements MouseListener {
		public void buttonPress(JButton b) {
			switch (b.getText()) {
				// Instantiates Game
				case "Confirm":
					sizeSelect.setVisible(false);
					mouseDown = false;
					new GameOfLife();
					break;
				// Plays Simulation
				case "Start":
					// Saves Set Tiles
					for (int y = 0; y < height; y++) {
						for (int x = 0; x < width; x++) {
							if (tiles[x][y].getBackground() == Color.WHITE) next[x][y] = true;
						}
					}
					startButton.setText("Stop");

					t.start();
					break;
				// Pauses Simulation
				case "Stop":
					startButton.setText("Start");

					t.stop();
					break;
				// Inverts Selected Tile State
				default:
					if (!t.isRunning())
						tileSetColor(b, b.getBackground() == Color.BLACK ? Color.WHITE : Color.BLACK);
					break;
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			mouseDown = true;
			if (e.getSource() instanceof JButton) buttonPress(((JButton) e.getSource()));
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			if (e.getSource() instanceof JButton && mouseDown) buttonPress(((JButton) e.getSource()));
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			mouseDown = false;
		}

		// Extra MouseListener Events
		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
	}

	// Used by Timer
	private static class Update implements ActionListener {
		// Updates Board State
		@Override
		public void actionPerformed(ActionEvent e) {
			int liveCount = 0;

			// Sets All Next States
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					// Logic is Based on Surrounding Tile Count
					switch (surround(x, y)) {
						case 2:
							break;
						case 3:
							next[x][y] = true;
							break;
						default:
							next[x][y] = false;
					}
				}
			}

			// Sets All States
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					tileSetColor(tiles[x][y], next[x][y] ? Color.WHITE : Color.BLACK);
					if (next[x][y]) liveCount++;
				}
			}

			// Ends Game
			if (liveCount == 0) {
				t.stop();
				JOptionPane.showMessageDialog(null, "Game Over");
				startButton.setText("Start");
			}
		}

		// Returns Count of Surrounding Live Tiles
		private static int surround(int x, int y) {
			int count = 0;

			// Checks 8 Surrounding Tiles
			for (int yIt = -1; yIt < 2; yIt++) {
				for (int xIt = -1; xIt < 2; xIt++) {
					if (!(xIt == 0 && yIt == 0)) {
						// Calculates Surrounding Indexes in case of Looping
						int relativeX = ((x + xIt) % width + width) % width;
						int relativeY = ((y + yIt) % height + height) % height;

						// Counts Live Tiles
						if (tiles[relativeX][relativeY].getBackground() == Color.WHITE) count++;
					}
				}
			}

			return count;
		}
	}

	private static void tileSetColor(JButton b, Color c) {
		b.setBackground(c);
		b.setForeground(c);
	}
}