package com.sy.testShift.test;

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
