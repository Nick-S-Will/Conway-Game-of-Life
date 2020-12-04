import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

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
	static Timer t = new Timer(200, new Update());
	static Font a = new Font("Times New Roman", Font.BOLD, 60);
	static Dictionary<Vector<Integer>, JButton> cells = new Hashtable<>();
	static boolean[][] next;
	static int width, height;
	static boolean mouseDown;
	
	public static void main(String[] args) {
		sizeSelect = new JFrame("Size Selection");
		sizeSelect.setSize(400, 250);
		sizeSelect.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sizeSelect.setLocationRelativeTo(null);
		sizeSelect.setLayout(new GridLayout(2, 1, 5, 5));

		JPanel spinners = new JPanel();
		spinners.setLayout(new GridLayout(1, 2, 5, 5));
		sizeX = new JSpinner(new SpinnerNumberModel(50, 5, 50, 1));
		sizeY = new JSpinner(new SpinnerNumberModel(50, 5, 50, 1));
		sizeX.setFont(a);
		sizeY.setFont(a);
		spinners.add(sizeX);
		spinners.add(sizeY);
		sizeSelect.add(spinners);



		JButton confirm = new JButton("Confirm");
		confirm.addMouseListener(new BH());
		confirm.setFont(a);
		sizeSelect.add(confirm);

		sizeSelect.setVisible(true);
	}

	public GameOfLife() {
		width = (int) sizeX.getValue();
		height = (int) sizeY.getValue();
		int grid = Math.round(800f / height);

		start = new JFrame();
		start.setSize(200, 100);
		start.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		startButton = new JButton("Start");
		startButton.addMouseListener(new BH());
		startButton.setFont(a);
		start.add(startButton);

		game = new JFrame("The Game of Life");
		game.setSize((width-1) * grid, height * grid);
		game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.setLocationRelativeTo(null);
		game.setLayout(new GridLayout(height, width));

		next = new boolean[width][height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				JButton clone = new JButton();
				clone.setFont(new Font("Arial", Font.PLAIN, 0));
				clone.setForeground(Color.BLACK);
				clone.setBackground(Color.BLACK);
				clone.addMouseListener(new BH());

				var index = new Vector<Integer>(x, y);
				cells.put(index, clone);
				next[x][y] = false;
				game.add(clone);
			}
		}

		start.setVisible(true);
		game.setVisible(true);
	}

	private static class Update implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int liveCount = 0;

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
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

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					setColor((JButton) e.getSource(), next[x][y] ? Color.WHITE : Color.BLACK);
					if (next[x][y]) liveCount++;
				}
			}

			if (liveCount == 0) {
				t.stop();
				JOptionPane.showMessageDialog(null, "Game Over");
				startButton.setText("Start");
			}
		}

		private int surround(int x, int y) {
			int count = 0;

			for (int yIt = -1; yIt < 2; yIt++) {
				for (int xIt = -1; xIt < 2; xIt++) {
					if (!(xIt == 0 && yIt == 0)) {
						int relativeX = ((x + xIt) % width + width) % width;
						int relativeY = ((y + yIt) % height + height) % height;

						if (cells.get(new Vector<Integer>(relativeX, relativeY)).getBackground()
								== Color.WHITE) count++;
					}
				}
			}

			return count;
		}
	}

	private static class BH implements MouseListener, Cloneable {
		public void ButtonPress(JButton button) {
			switch (button.getText()) {
			case "Confirm":
				sizeSelect.setVisible(false);		
				mouseDown = false;
				new GameOfLife();
				break;
			case "Start":
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (cells.get(new Vector<Integer>(x, y)).getBackground() == Color.WHITE)
							next[x][y] = true;
					}
				}
				startButton.setText("Stop");

				t.start();
				break;
			case "Stop":				
				startButton.setText("Start");

				t.stop();
				break;
			default:
				if (!t.isRunning()) {
					setColor(button, button.getBackground().getRed() == 0 ?
							Color.WHITE : Color.BLACK);
				}
				break;
			}
		}

		public void mouseClicked(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {
			mouseDown = true;
			if (e.getSource() instanceof JButton) 
				ButtonPress((JButton) e.getSource());
		}
		public void mouseReleased(MouseEvent e) {
			mouseDown = false;
		}
		public void mouseEntered(MouseEvent e) {
			if (e.getSource() instanceof JButton && mouseDown) 
				ButtonPress((JButton) e.getSource());
		}
		public void mouseExited(MouseEvent e) {}
	}

	private static void setColor(JButton button, Color c) {
		button.setBackground(c);
		button.setForeground(c);
	}
}