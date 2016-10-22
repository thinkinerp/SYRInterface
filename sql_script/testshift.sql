CREATE DEFINER=`root`@`localhost` PROCEDURE `testshift`(in arg_kpi_id  int)
begin
	
	declare testId int ;
	declare reportId varchar(50) ;
	IF EXISTS( SELECT * FROM kpi_bases where kpi_id = arg_kpi_id )
    AND EXISTS (SELECT * FROM sys_template_reports where report_id = arg_kpi_id)
   THEN
       START TRANSACTION;
         if(arg_kpi_id<100 )
         then
            set reportId = concat('_0' , arg_kpi_id ,'_');
         else
            set reportId = concat('_' , arg_kpi_id ,'_');
         end if ;

   	     SELECT MAX(kpi_id) + 1 INTO  testId
         FROM kpi_bases where locate('99' ,kpi_id ) > 0
    	 ;

        -- select testId ;
        INSERT INTO sys_role_resources(role_id ,obj_type,obj_id )
        select	role_id ,obj_type,testId
          FROM sys_role_resources
        WHERE  obj_type = 1 AND obj_id = arg_kpi_id ;
        
        INSERT INTO sys_test_enviroment(original_id , test_id , created_date)
	SELECT reportId , testId , now() ;

        set @sql_test := CONCAT('create table report_data_',testId,'_main like ' ,'report_data_template' );
        prepare STMT FROM @sql_test;
        EXECUTE STMT;

        set @sql_test := CONCAT('create table report_data_',testId,'_banner like ' ,'report_data_template_banner' );
        prepare STMT FROM @sql_test;
        EXECUTE STMT;

        -- DELETE FROM sys_role_resources WHERE obj_type = 1 AND obj_id = arg_kpi_id ;
        -- DELETE FROM sys_group_reports WHERE report_id = arg_kpi_id ;
        
       INSERT  INTO kpi_bases(id, kpi_id, kpi_group, kpi_name, num, chart_type, kpi_desc, kpi_calc, kpi_target, up_down, kpi_period, owner_dept, computer_dept, data_source, link, unit, role_ids, create_user, update_user, memo, load_time, created_at, updated_at)
       SELECT testId, testId, kpi_group, kpi_name, num, chart_type, kpi_desc, kpi_calc, kpi_target, up_down, kpi_period, owner_dept, computer_dept, data_source, link, unit, role_ids, create_user, update_user, memo, load_time, created_at, updated_at
       FROM yonghuibi.kpi_bases
       where kpi_id = arg_kpi_id;
       
       INSERT INTO sys_template_reports(`report_id`,`template_id`,`content`,`created_at`,`updated_at`,`title`,`remark`)

       select testId, template_id, 
                 replace(content ,reportId,
                             concat( '_' , testId , '_')  ), created_at, updated_at, title, remark
         from sys_template_reports
       where report_id = arg_kpi_id 
       ; 
       
       select testId ;

       COMMIT;
   END IF  ;
	
END