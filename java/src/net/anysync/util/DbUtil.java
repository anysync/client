package net.anysync.util;

import org.apache.log4j.*;;


import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;

/*
public class DbUtil implements java.io.Serializable
{
    final static Logger log = LogManager.getLogger(DbUtil.class);

    public static Connection getConnection()
    {
        // SQLite connection string
        String namedb = AppUtil.getAppHome() + "/names/data.db";
        File f = new File(namedb);
        String url = "jdbc:sqlite:" + f.getAbsolutePath();
        System.out.println("URL:" + url);
        Connection conn = null;
        try
        {
            conn = DriverManager.getConnection(url);
        }
        catch(SQLException e)
        {
            log.error(e.getMessage());
        }
        return conn;
    }

    public static String getValue(String key)
    {
        String sql = "SELECT value FROM kv WHERE key = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
            {
                return rs.getString("value");
            }
        }
        catch(SQLException e)
        {
            log.error(e);
        }
        return null;
    }

    public static HashMap<String, String> getValues(List<String> keys)
    {
        StringBuffer buf = new StringBuffer();
        int n = keys.size();
        for(int i = 0; i < n; i++)
        {
            buf.append("'"). append(keys.get(i)).append("'");
            if(i != n - 1)
            {
                buf.append(",");
            }
        }
        String sql = "SELECT key, value FROM kv WHERE key in (" + buf  + ")";
        System.out.println("SQL:" + sql);
        HashMap<String,String> map = new HashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            ResultSet rs = pstmt.executeQuery();
            while(rs.next())
            {
                 map.put(rs.getString("key"), rs.getString("value"));
            }
        }
        catch(SQLException e)
        {
            log.error(e);
        }
        return map;
    }
}
*/