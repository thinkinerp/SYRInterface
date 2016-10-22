package com.sy.testShift;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public class ShiftManager {

	  public static void main(String[] args) {
		  
			Cmds cmds = new Cmds();		  
			if(args.length == 2){
				for(String s : args){
					System.out.println(s.indexOf("-"));
					if(s.indexOf("-") >= 0){
						cmds.setOptions(s);
					}else{
						cmds.setReport_id(s);
					}
				}
			}
			
			ConnectorManager cm = new ConnectorManager();
            cm.setC(getConfig());
			if("-n".equalsIgnoreCase(cmds.getOptions())){
				
				cm.createProcedure(Integer.parseInt(cmds.getReport_id()));  
				
			} else if("-dr".equalsIgnoreCase(cmds.getOptions())){
				
		        cm.delTestEnviroment(Integer.parseInt(cmds.getReport_id()));
		        
			} else if("-d".equalsIgnoreCase(cmds.getOptions())){
				
				cm.delTestEnviroment(Integer.parseInt(cmds.getReport_id()));
				
				
			}else if("-r".equalsIgnoreCase(cmds.getOptions())){
				
				cm.recoverOriginalReport(Integer.parseInt(cmds.getReport_id()));
				
			}else if("-np".equalsIgnoreCase(cmds.getOptions())){
				  System.out.println("暂时不支持");
			}

	}
	
	 private static Config getConfig(){
		 
		 Config c = new Config();
		 
	        Properties pps = new Properties();
            try {
           	 
//				pps.load(ShiftManager.class.getClassLoader().getResourceAsStream("com/sy/testShift/sql_conn.properties"));
//				pps.load(ShiftManager.class.getClassLoader().getResourceAsStream("../../"));
				URL url = ShiftManager.class.getResource("/");
				
/*				System.out.println("dir"+url);
				System.out.println(url.getPath().replace("SYRInterface/bin/", ""));
				System.out.println(url.getPath().replace("SYRInterface/bin/", "") + "sql_conn.properties");*/
				pps.load(new FileInputStream( "sql_conn.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
            Enumeration enum1 = pps.propertyNames();//得到配置文件的名字
            Class clazz = Config.class ;
            Field[] fs = clazz.getDeclaredFields();
            while(enum1.hasMoreElements()) {
                String strKey = (String) enum1.nextElement();
                String strValue = pps.getProperty(strKey);
                for(Field f : fs){
            		try {
                if(f.getName().equalsIgnoreCase(strKey)){
                	Field field;
				field = c.getClass().getDeclaredField(f.getName());
                	field.setAccessible(true);
                	field.set(c, strValue);
                }
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                }                 
                
                
           }
            
		 return c ;
	 } 
	  
}
