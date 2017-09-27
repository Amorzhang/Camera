package com.example.dell.camera.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DELL on 2017/9/26.
 */
public class IPManager {
    public Map<String,String> getConnectIp() throws Exception {
        Map<String,String> connect = new HashMap<String,String>();
        BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitted = line.split(" +");
            if (splitted != null && splitted.length >= 4) {
                String ip = splitted[0];
                String max = splitted[3];
                connect.put(max, ip);
            }
        }
        return connect;
    }

}
