package com.github.markusbernhardt.proxy.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.ProxySearch.ScriptingEngineType;
import com.github.markusbernhardt.proxy.ProxySearch.Strategy;
import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Small test application that allows you to select a proxy search strategy and
 * then validate URLs against it.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class ProxyTester extends JFrame {

	private static final long serialVersionUID = 1L;

    private JComboBox<ProxySearch.Strategy> modes;
    private JComboBox<ProxySearch.ScriptingEngineType> engines;
	private JButton testButton;
	private JTextField urlField;

	private JTextArea logArea;

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

		p.add(new JLabel("Mode:"));
        this.modes = new JComboBox<ProxySearch.Strategy>(ProxySearch.Strategy.values());
        p.add(this.modes);
        p.add(new JLabel("ScriptingEngine:"));
        this.engines = new JComboBox<ProxySearch.ScriptingEngineType>(ProxySearch.ScriptingEngineType.values());
        p.add(this.engines);

		p.add(new JLabel("URL:"));
		this.urlField = new JTextField(30);
		this.urlField.setText("http://www.google.com/");
		p.add(this.urlField);

		this.testButton = new JButton("Test");
		this.testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				testUrl();
			}
		});
		p.add(this.testButton);

		this.logArea = new JTextArea(5, 50);
		JPanel contenPane = new JPanel(new BorderLayout());
		contenPane.add(p, BorderLayout.NORTH);
		contenPane.add(new JScrollPane(this.logArea), BorderLayout.CENTER);
		setContentPane(contenPane);

		pack();
		setLocationRelativeTo(null);
		installLogger();
	}

	/*************************************************************************
	 * Install the framework logger.
	 ************************************************************************/

	private void installLogger() {
		Logger.setBackend(new Logger.LogBackEnd() {
			public void log(Class<?> clazz, LogLevel loglevel, String msg, Object... params) {
				ProxyTester.this.logArea.append(loglevel + "\t" + MessageFormat.format(msg, params) + "\n");
			}
		});
	}

	/*************************************************************************
	 * Test the given URL with the given Proxy Search.
	 ************************************************************************/

	protected void testUrl() {
		try {
			if (this.urlField.getText().trim().length() == 0) {
				ProxyTester.this.logArea.append("ERROR\tPlease enter an URL first.\n");
				return;
			}

			this.logArea.setText("");

			Strategy pss = (Strategy) this.modes.getSelectedItem();
			ScriptingEngineType engineType = (ScriptingEngineType) this.engines.getSelectedItem();
			ProxySearch ps = new ProxySearch();
			ps.setScriptingEngine(engineType);
			ps.addStrategy(pss);
			ProxySelector psel = ps.getProxySelector();
			if (psel == null) {
				ProxyTester.this.logArea.append("ERROR\tNo proxy settings available for this mode.\n");
				return;
			}
			ProxySelector.setDefault(psel);

			URL url = new URL(this.urlField.getText().trim());
			List<Proxy> result = psel.select(url.toURI());
			if (result == null || result.size() == 0) {
				ProxyTester.this.logArea.append("INFO\tNo proxy found for this url.\n");
				return;
			}

			ProxyTester.this.logArea.append("INFO\tProxy Settings found using " + pss + " strategy.\n");
			ProxyTester.this.logArea.append("INFO\tProxy used for URL is: " + result.get(0) + "\n");

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error:" + e.getMessage(), "Error checking URL.",
			        JOptionPane.ERROR_MESSAGE);
		}

	}

	/*************************************************************************
	 * Main entry point for the application.
	 * 
	 * @param args
	 *            command line arguments.
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
