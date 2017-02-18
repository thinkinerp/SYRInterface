CREATE DEFINER=`root`@`localhost` PROCEDURE `remove_testshift`(in arg_kpi_id  int)
begin
	
	declare testId int ;
	declare reportId varchar(50) ;

       START TRANSACTION;
         if(arg_kpi_id<100 )
         then
            set reportId = concat('_0' , arg_kpi_id ,'_');
         else
            set reportId = concat('_' , arg_kpi_id ,'_');
         end if ;


        select test_id  into testId 
          FROM sys_test_enviroment
        WHERE  original_id = reportId ;
        -- select testId ,reportId ;  

        set @sql_test := CONCAT('DROP TABLE IF EXISTS report_data_',testId,'_main');
        prepare STMT FROM @sql_test;
        EXECUTE STMT;

        set @sql_test := CONCAT('DROP TABLE IF EXISTS report_data_',testId,'_banner' );
        prepare STMT FROM @sql_test;
        EXECUTE STMT;

	DELETE FROM sys_test_enviroment
	WHERE original_id = reportId

	;

       DELETE 
	 FROM sys_role_resources 
	WHERE obj_id = testId 
	  AND obj_type = 1 ;

       DELETE FROM kpi_bases WHERE kpi_id = testId;

       DELETE FROM sys_template_reports WHERE report_id = testId ;
       
	SELECT testId ;
       COMMIT;

	
END