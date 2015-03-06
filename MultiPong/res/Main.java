package de.pinkproblem.multipong;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import static de.pinkproblem.multipong.Direction.*;

import javax.swing.JFrame;

public class Main extends JFrame{
	
	static byte[][] field;
	

	public static void main(String[] args) {
		JFrame main = new Main();
		main.setSize(800, 600);
		main.setVisible(true);
		main.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		long time = System.currentTimeMillis();
		PongGame game = new PongGame();
//		game.setPlayer(0, new RealPlayer(LEFT, TOP));
		
		while(true) {
			long delta = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();
			
			game.process(1000);			
			field = game.getFieldArray();
			main.repaint();
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	@Override
	public void paint(Graphics g) {
		Color b;
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {
				b = (field[i][j] == 0) ? Color.BLACK : Color.WHITE;
				g.setColor(b);
				g.fillRect(i*10 + 50 , j*10 + 50, 10, 10);
			}
			
		}
	}
}
