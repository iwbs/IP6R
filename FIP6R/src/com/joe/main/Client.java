package com.joe.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Client {

	private static void createAndShowGUI(Properties prop) {
		// Create and set up the window.
		JFrame frame = new JFrame("FIP6R");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add content to the window.
		final GUI gui = new GUI();
		gui.setProfile(prop);
		gui.initDataHandler();
		frame.getContentPane().add(gui);
		frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
            	gui.closeDataHandler();
            }
        });

		// Display the window.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		FileInputStream file;
		final Properties prop = new Properties();
		try {
			file = new FileInputStream("./config.properties");
			prop.load(file);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(prop);
			}
		});
	}

}
