package com.wildfire.main;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * 轻量 JSON 配置封装，用于保存 1.12 客户端本地玩家设置。
 */
public class Configuration {

    public static String ROOT_FOLDER = System.getProperty("user.dir");
    public JSONObject SAVE_VALUES = new JSONObject();
    private File CFG_FILE;
    private File CFG_LOC;

    public Configuration(String saveLoc, String cfgName) {
        this.CFG_FILE = new File(ROOT_FOLDER + "/config/" + saveLoc + "/" + cfgName + ".json");
        this.CFG_LOC = new File(ROOT_FOLDER + "/config/" + saveLoc + "/");
        if (!this.CFG_LOC.exists()) {
            this.CFG_LOC.mkdirs();
        }

    }

    public void finish() {
        if (this.CFG_FILE.exists()) {
            this.load();
            this.updateConfig();
        }
    }

    public void setParameter(String key, Object value) {
        this.SAVE_VALUES.put(key, value);
    }

    public void setDefaultParameter(String key, Object defaultValue) {
        if (!this.SAVE_VALUES.containsKey(key)) {
            this.SAVE_VALUES.put(key, defaultValue);
        }

    }

    public Object getParameter(String key) {
        try {
            return this.SAVE_VALUES.get(key);
        } catch (Exception var3) {
            return "-1";
        }
    }

    public void updateConfig() {
        try (FileReader configurationFile = new FileReader(this.CFG_FILE)) {
            JSONObject obj = (JSONObject) (new JSONParser()).parse(configurationFile);

            for (Object o : this.SAVE_VALUES.keySet()) {
                String key = (String) o;
                obj.put(key, this.SAVE_VALUES.get(key));
            }

            FileOutputStream writer = new FileOutputStream(this.CFG_FILE);
            writer.write(obj.toJSONString().getBytes());
            writer.close();
        } catch (Exception var5) {
        }

    }


    /**


     * 保存当前 SAVE_VALUES 到磁盘 JSON 文件。


     */


    public void save() {
        try {
            FileOutputStream writer = new FileOutputStream(this.CFG_FILE);
            JSONObject obj = new JSONObject();

            for (Object o : this.SAVE_VALUES.keySet()) {
                String key = (String) o;
                obj.put(key, this.SAVE_VALUES.get(key));
            }

            writer.write(obj.toJSONString().getBytes());
            writer.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }

    /**

     * 从磁盘 JSON 文件读取配置项。

     */

    public void load() {
        try (FileReader configurationFile = new FileReader(this.CFG_FILE)) {
            JSONObject obj = (JSONObject) (new JSONParser()).parse(configurationFile);

            for (Object o : obj.keySet()) {
                String key = (String) o;
                this.SAVE_VALUES.put(key, obj.get(key));
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

}
