package com.btr.proxy.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.btr.proxy.search.ProxySearch;
import com.btr.proxy.search.ProxySearch.Strategy;

/*****************************************************************************
 * Small test application that allows you to select a proxy search strategy
 * and then validate URLs against it. 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class ProxyTester extends JFrame {
	
	private JComboBox modes;
	private JButton testButton;
	private JTextField urlField;

	/*************************************************************************
	 * Constructor
	 ************************************************************************/
	
	public ProxyTester() {
		super();
		init();
	}
	
	/*************************************************************************
	 * Initializes the GUI.
	 ************************************************************************/
	
	private void init() {
		setTitle("Proxy Vole Tester");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel p = new JPanel();
		setContentPane(p);

		p.add(new JLabel("Mode:"));

		this.modes = new JComboBox(ProxySearch.Strategy.values());
		p.add(this.modes);

		p.add(new JLabel("URL:"));
		this.urlField = new JTextField(30); 
		this.urlField.setText("http://proxy-vole.kenai.com");
		p.add(this.urlField);
		
		this.testButton = new JButton("Test");
		this.testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				testUrl();
			}
		});
		p.add(this.testButton);

		pack();
		setLocationRelativeTo(null);
	}

	/*************************************************************************
	 * Test the given URL with the given Proxy Search.
	 ************************************************************************/
	
	protected void testUrl() {
		try {
			if (this.urlField.getText().trim().length() == 0) {
				JOptionPane.showMessageDialog(this, "Please enter an URL first.");
				return;
			}

			Strategy pss = (Strategy) this.modes.getSelectedItem();
			ProxySearch ps = new ProxySearch();
			ps.addStrategy(pss);
			ProxySelector psel = ps.getProxySelector();
			if (psel == null) {
				JOptionPane.showMessageDialog(this, "No proxy settings available for this mode.");
				return;
			}
			ProxySelector.setDefault(psel);
			
			URL url = new URL(this.urlField.getText().trim()); 
			List<Proxy> result = psel.select(url.toURI());
			if (result == null || result.size() == 0) {
				JOptionPane.showMessageDialog(this, "No proxy found for this url.");
				return;
			}
			
			JOptionPane.showMessageDialog(this, 
					"Proxy Settings found using "+pss+" strategy.\n" +
					"Proxy used for URL is: "+result.get(0));
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error:"+e.getMessage(), "Error checking URL.", JOptionPane.ERROR_MESSAGE);
		}
		
	}

	/*************************************************************************
	 * Main entry point for the application.
	 * @param args command line arguments.
	 ************************************************************************/
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setLookAndFeel();
				
				ProxyTester mainFrame = new ProxyTester();
				mainFrame.setVisible(true);
			}

		});
	}

	/*************************************************************************
	 * Change the L&F to the system default.
	 ************************************************************************/
	
	private static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Use default
		}
	}
}

