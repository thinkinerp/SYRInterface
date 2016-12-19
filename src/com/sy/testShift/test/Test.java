package com.sy.testShift.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.sy.testShift.Cmds;
import com.sy.testShift.ConnectorManager;


public class Test {

	
	
	@org.junit.Test
	public void Backtest(){
		ConnectorManager c = new ConnectorManager();
		c.createProcedure(31);
//		c.delTestEnviroment(31);
//		c.recoverOriginalReport(31);
	}
	
	
	@org.junit.Test
	public void testReplace() {
		
		String s  = "report_data_049_main";
		Integer i = 49 ;
		s = s.replace("report_data_" +  (i< 100 ? "0" + i : i ) , "report_data_9901_main");
		
		File file =new File("json.txt");
		try {
	        if(!file.exists()){
				file.createNewFile();
	         }		
	        
		     FileWriter fileWritter = new FileWriter(file.getName());
	         BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	         bufferWritter.write(s);
	         bufferWritter.close();
	             
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	@org.junit.Test
	public void TEST() {
		//ConnectorManager c = new ConnectorManager();
		//c.createProcedure();

		String[] args = { "-n" , "60" };
		Cmds cmds = new Cmds();
		
		if(args.length == 2){
		
			for(String s : args){
				
				if("-".equalsIgnoreCase(s)){
					cmds.setOptions(s);
				}else{
					cmds.setReport_id(s);
				}
				
			}
		}
	}

}
