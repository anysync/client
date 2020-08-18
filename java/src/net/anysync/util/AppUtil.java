// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.util;

import com.moandjiezana.toml.Toml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

public final class AppUtil
{
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
	public static final String FOLDER = "Folder";
	public static final String FILE = "File";

	public static final String I18N		 	= "i18n/as";
	public static final String NULL_HASH                   ="00000000000000000000000000000000000000000000000000000000";
	public static final String SHARED_HASH                 ="ffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

	private AppUtil() {}

    private static String gCurrentHome;
    public static String getAppHome()
	{
		if(gCurrentHome != null) return gCurrentHome;
		String dir = System.getProperty("user.home") + "/.AnySync/";
		File f = new File(dir);
		if(!f.exists())
		{
			f.mkdir();
		}
		String content = FileUtil.readFile(dir + "current");
		if(content == null || content.length() == 0)
		{
			return "";
		}
		content = content.trim();
		gCurrentHome = f.getAbsolutePath() +  File.separator + content + File.separator;
		return gCurrentHome;
	}


	public static File getCurrentFile()
	{
		return new File(System.getProperty("user.home") + "/.AnySync/current");
	}

	public static String getCurrentFileContent()
	{
		File f = getCurrentFile();
		if(!f.exists()) return null;
		return FileUtil.readFile(f.getAbsolutePath()).trim();
	}

	public static boolean currentFolderExists()
	{
		String s = getCurrentFileContent();
		if(s == null || s.length() == 0) return false;
		File f = new File(System.getProperty("user.home") + "/.AnySync/" + s);
		if(f.exists()) return true;
		else return false;
	}
	public static String getBinFileName(String hash)
	{
		return getAppHome() + "tree" + File.separator +  hashToPath(hash) + ".bin";
	}

	public static String getFolder(String name)
	{
		return getAppHome() + name + File.separator;
	}

	public static String hashToPath(String hash)
	{
		if(hash.length() != 56)
		{//error
			return "";
		}
		return String.format("%s/%s/%s/%s", hash.substring(0, 2), hash.substring(2, 4), hash.substring(4, 6), hash.substring(6));
	}

	public static boolean isEmpty(final CharSequence cs) {
	       return cs == null || cs.length() == 0;
	}

	public static String getServerAddress()
	{
		String file = System.getProperty("user.home") + "/.AnySync/anysync.rc";
		File f = new File(file);
		if(!f.exists()) return "";
		BufferedReader reader;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while(line != null)
			{
				line = line.replaceAll(" ", "");
				line = line.replaceAll("\t", "");
				if(line.contains("Server="))
				{
					String val = line.substring(7);
					val = val.replaceAll("\"", "");
					return val;
				}
				line = reader.readLine();
			}
			reader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return "";
	}

	private static Toml _toml;
	public static Toml getConfig()
	{
		if(_toml != null) return _toml;
		String file = System.getProperty("user.home") + "/.AnySync/current";
		String content = FileUtil.readFile(file);
		if(content == null) return null;

		file = System.getProperty("user.home") + "/.AnySync/" + content.trim() + "/config";
		content = FileUtil.readFile(file);
		return new Toml().read(content);
	}
}
