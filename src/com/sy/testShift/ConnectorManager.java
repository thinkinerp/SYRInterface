package com.sy.testShift;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;

import com.mysql.jdbc.Connection;

public class ConnectorManager {

	private Config c ;
	private static Logger logger = Logger.getLogger(ConnectorManager.class);  
	
	private Connection getConn(Config c){
	        
		    String driver = "com.mysql.jdbc.Driver";
		    String url = c.getUrl();
		    String username = c.getUsername();
		    String password = c.getPassword();

		    logger.info("url" +  url + " dbuser:" + username + " dbpwd:"  +password + " start to new test enviroment"); 

		    Connection conn = null;
		    try {
		        Class.forName(driver); //classLoader,加载对应驱动
		        conn = (Connection) DriverManager.getConnection(url, username, password);
		    } catch (ClassNotFoundException e) {
		    	   logger.error(e);
		    } catch (SQLException e) {
		    	   logger.error(e);
		    }
		    return conn;
	}
	
	
	public void createProcedure( int kpi_id  ){
		
		String test_id = excute(kpi_id);
		
		logger.info("获取测试报表的 ID 是：" + test_id);
		
		if("404".equalsIgnoreCase(test_id)){
			return ;
		}
		
		String original_report_id = null ;
		if(kpi_id < 100){
			original_report_id = "0" + kpi_id ;
		}else{
			original_report_id = "" + kpi_id ;
		}
		
		String sql=" select body from mysql.proc where name = 'ETL_report_id_"+original_report_id+"_main' ; "; 
		Connection conn = null ;
		conn = this.getConn(c);
		Statement stmt = null;  
		ResultSet rs       = null;
		Blob b               = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			rs.next();
			b  = rs.getBlob("body");
			String s = BlobToString(b);
			backupJSONOrProcedure( s , kpi_id + "report_procedure_backup.txt");
			s = s.replace("report_data_"+(kpi_id < 100 ? "0" + kpi_id :kpi_id )+"_main", "report_data_"+test_id+"_main");
			s = s.replace("report_data_"+(kpi_id < 100 ? "0" + kpi_id :kpi_id )+"_banner", "report_data_"+test_id+"_banner");
			s = s.replace("" + kpi_id, test_id);

			sql ="DROP PROCEDURE IF EXISTS yonghuibi.ETL_report_id_"+test_id+"_main_test ;" ;
			
			sql ="CREATE PROCEDURE ETL_report_id_"+test_id+"_main_test() " + s ;
			
			logger.info("create PROCEDURE yonghuibi.ETL_report_id_"+test_id+"_main_test :" + stmt.execute(sql));
            sql = " SELECT content FROM yonghuibi.sys_template_reports WHERE  report_id = " + kpi_id ;
            rs.close();
            rs = stmt.executeQuery(sql);
            rs.next();
            s = rs.getString("content");
            backupJSONOrProcedure( s , kpi_id + "report_json.txt");

		} catch (SQLException e) {
			logger.error(e);
		} finally {
			try {
			if(null != conn){
					conn.close();
			}
			if(null != stmt){
				stmt.close();
			}
			if(null != rs){
				rs.close();
			}
			} catch (SQLException e) {
				logger.error(e);
			}
			
		} 
	}
	
	
	public String excute(int id){
		
          String sql = "call testshift("+id+" )" ;		  
		  Connection conn = null ;
		  CallableStatement st = null;
		  ResultSet rs = null ;
          try {
			  
        	  conn = this.getConn(c);
        	  
        	  st = conn.prepareCall(sql);
        	  
        	  st.execute(sql);
        	  rs = st.getResultSet();
        	  
        	  rs.next();
          logger.info("call testshift("+id+" ) completed!");
        	  return rs.getString("testId");
        	  
//        	  st.registerOutParameter(2, Types.VARCHAR);
//        	  st.setString(parameterIndex, x);
		} catch (SQLException e) {
			logger.error(e);
			return "404";
		} finally{
			try {
			if(null != st){
					st.close();
			}
			if(null != conn){
				conn.close();
			}
			} catch (SQLException e) {
				logger.error(e);
			}

		}
	}
	/**
	 * Blob字段的通用转换
	 * 注意可能出现乱码
	 * @return 转好的字符串，
	 * **/
	 public String BlobToString(Blob blob){ 
				 StringBuffer str=new StringBuffer();
				 InputStream in=null;//输入字节流
				 try { 
					 in = blob.getBinaryStream(); 
					 byte[] buff=new byte[(int) blob.length()]; 
					 for(int i=0;(i=in.read(buff))>0;){ 
						 	str=str.append(new String(buff)); 
					 }
					 return str.toString();
				 }catch (Exception e) { 
					 e.printStackTrace();
			     } finally{
			    	 try{
			    		 in.close();
			    	 }catch(Exception e){
			    		    logger.info("转换异常" + e);
			    		 }
			     }
				   return null ;
	 }
	public Config getC() {
		return c;
	}


	public void setC(Config c) {
		this.c = c;
	}

	public void recoverOriginalReport( int arg_kpi_id ){

				 String sql = null ; 
				Connection conn = null ;
				PreparedStatement st = null;
				ResultSet rs = null ;
		        try {
					  
		         conn = this.getConn(c);
		          
		          sql = "select distinct test_id from sys_test_enviroment where original_id = '_"+ (arg_kpi_id < 100 ? "0" + arg_kpi_id: "" + arg_kpi_id) +"_'";
		          st = (PreparedStatement)conn.prepareStatement(sql);
		      	  rs = st.executeQuery();
		      	  rs.next();
		      	  String test_id = rs.getString("test_id");
		      	  rs.close();
		      	  st.close();
		      	  
		      	  sql = "update\n" +
		      			"sys_template_reports as t1 ,\n" +
		      			"sys_template_reports as t2\n" +
		      			"   set t1.content =replace(t2.content \n" +
		      			"		           ,CONCAT('_',"+test_id+",'_')\n" +
		      			"		           , '_"+(arg_kpi_id < 100 ? "0" + arg_kpi_id: "" + arg_kpi_id)+"_')\n" +
		      			"where t1.report_id = "+arg_kpi_id+" and t2.report_id = "+test_id ;
		      	  
		      	  st = (PreparedStatement) conn.prepareStatement(sql);
		      	  st.execute();
		      	  st.close();
		      	  
		      	  sql=" select body from mysql.proc where name = 'ETL_report_id_" + test_id + "_main_test' ; "; 
		      	  st = (PreparedStatement)conn.prepareStatement(sql);
		      	  rs = st.executeQuery();
		      	  rs.next();
		      	  String body = BlobToString(rs.getBlob("body"));
		      	  rs.close();
		      	  st.close();
		      	  sql ="DROP PROCEDURE IF EXISTS  ETL_report_id_"+(arg_kpi_id < 100 ? "0" + arg_kpi_id: "" + arg_kpi_id)+"_main ";
		      	  st = conn.prepareStatement(sql);
		      	  st.execute();		      	  
		      	  st.close();   	  
		      	  body =  body.replace("_test", "").replace("_" + test_id + "_","_" + (arg_kpi_id < 100 ? "0" + arg_kpi_id: "" + arg_kpi_id) +"_");
		      	  body = body.replaceAll(test_id,arg_kpi_id + "" );
		      	  
		      	  sql ="CREATE PROCEDURE ETL_report_id_"+(arg_kpi_id < 100 ? "0" + arg_kpi_id: "" + arg_kpi_id)+"_main() " + body.replace("_test", "").replace("_" + test_id + "_","_" + (arg_kpi_id < 100 ? "0" + arg_kpi_id: "" + arg_kpi_id) +"_") ;
		      	  st = conn.prepareStatement(sql);
		      	  st.execute();
				} catch (SQLException e) {
					logger.error(e);
				} finally{
					try {
					if(null != st){
							st.close();
					}
					if(null != conn){
						conn.close();
					}
					} catch (SQLException e) {
						logger.error(e);
					}

				}
	}

	
	public void backupJSONOrProcedure( String s , String fileName ){
		File file =new File(fileName);
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
	
	public void delTestEnviroment(int id) {
		    
		//调用存储过程将出存储过程之外的权限控制删除
        String sql = "call remove_testshift("+id+" )" ;		  
        String testId = null ;
		  Connection conn = null ;
		  CallableStatement st = null;
		  ResultSet rs = null ;
        try {
			  
      	  conn = this.getConn(c);
      	  st = conn.prepareCall(sql);
      	  st.execute(sql);
      	  rs = st.getResultSet();
      	  
      	  rs.next();
      	  logger.info("call remove_testshift("+id+" ) completed!");
          testId =rs.getString("testId");
          st.close();
          // 删除存储过程
          sql ="DROP PROCEDURE IF EXISTS yonghuibi.ETL_report_id_"+testId+"_main_test" ;
      	  st = conn.prepareCall(sql);
      	  st.execute();
		} catch (SQLException e) {
			logger.error(e);
		} finally{
			try {
			if(null != st){
					st.close();
			}
			if(null != conn){
				conn.close();
			}
			} catch (SQLException e) {
				logger.error(e);
			}

		}
		
	} 
}
