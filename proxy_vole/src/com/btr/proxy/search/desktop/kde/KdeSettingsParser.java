package com.btr.proxy.search.desktop.kde;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.btr.proxy.util.Logger;
import com.btr.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Parser for the KDE settings file.
 * The KDE proxy settings are stored in the file:
 * <p>
 * <i>.kde/share/config/kioslaverc</i>
 * </p>
 * in the users home directory.
 *
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

class KdeSettingsParser {
	
	/*************************************************************************
	 * Constructor
	 ************************************************************************/
	
	public KdeSettingsParser() {
		super();
	}
	
	/*************************************************************************
	 * Parse the settings file and extract all network.proxy.* settings from it.
	 * @return the parsed properties.
	 * @throws IOException on read error.
	 ************************************************************************/
	
	public Properties parseSettings() throws IOException {
		// Search for existing settings.
		File settingsFile = findSettingsFile();
		if (settingsFile == null) {
			return null;
		}

		// Read settings from file.
		BufferedReader fin = new BufferedReader(
				new InputStreamReader(
					new FileInputStream(settingsFile)));

		Properties result = new Properties();
		try {
			String line = fin.readLine();
			
			// Find section start.
			while (line != null && !"[Proxy Settings]".equals(line.trim())) {
				line = fin.readLine();
			}
			if (line == null) {
				return result;
			}
			
			// Read full section
			line = "";
			while (line != null && !line.trim().startsWith("[")) {
				line = line.trim();
				int index = line.indexOf('=');
				if (index > 0) {
					String key = line.substring(0, index).trim();
					String value = line.substring(index+1).trim();
					result.setProperty(key, value);
				}
								
				line = fin.readLine();
			}
		} finally {
			fin.close();
		}

		return result;
	}
	
	/*************************************************************************
	 * Finds all the KDE network settings file. 
	 * @return a file or null if does not exist. 
	 ************************************************************************/
	
	private File findSettingsFile() {
		File userDir = new File(System.getProperty("user.home"));
		File settingsFile = new File(userDir, ".kde"+File.separator+"share"+File.separator+"config"+File.separator+"kioslaverc");
		Logger.log(getClass(), LogLevel.TRACE, "Searching Kde settings in {0}", settingsFile);
		if (!settingsFile.exists()) {
			Logger.log(getClass(), LogLevel.DEBUG, "Settings not found");
			return null;
		}
		Logger.log(getClass(), LogLevel.TRACE, "Settings found");
		return settingsFile;
	}
	
}
