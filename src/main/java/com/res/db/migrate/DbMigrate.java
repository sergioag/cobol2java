package com.res.db.migrate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.res.java.lib.Program;



public class DbMigrate extends Program {
	
	public static final String DATABASE_NAME =  "defaultDatabase";
	public static final String TABLE_NAME =  "default.table.name";
	public static final String DATABASE_LEGACY_NAME = "database.%0.legacy.name";
	public static final String DATABASE_PACKAGE_NAME = "database.%0.package.name";
	public static final String DATABASE_VENDOR_NAME = "database.%0.vendor";
	public static final String DATABASE_TABLE_PROPERITES = "Database%0.properties";

	public class DatabaseProperties extends Properties {

		private static final long serialVersionUID = 1L;
		private String defaultDatabase = null;
		private DatabaseTableProperties databaseTables  = null;
		
		public boolean load() {
			return load("Database.properties");
		}
		
		public boolean load(String propertiesFile) {
			try {
				super.load(new FileInputStream(new File(propertiesFile)));
				defaultDatabase=((String)super.get(DATABASE_NAME)).replace("\"", "");
				loadTables();
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		
		
		public boolean loadTables() {
				return loadTables(DATABASE_TABLE_PROPERITES.
						replace("%0",defaultDatabase.toUpperCase().replace("\"", "")));
		}
		
		public boolean loadTables(String tablesProperties) {
			try {
				if(databaseTables==null)
					databaseTables=new DatabaseTableProperties();
				databaseTables.setDatabase(this);
				databaseTables.load(new FileInputStream(new File(tablesProperties.replace("\"", ""))));
				databaseTables.setDefaultTable((String)databaseTables.get(TABLE_NAME));
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		
		public String getDefaultDatabase() {
			return defaultDatabase;
		}

		public void setDefaultDatabase(String defaultDatabase) {
			this.defaultDatabase = defaultDatabase.replace("\"", "");
		}

		public String getLegacyName() {
			return (String)get(DATABASE_LEGACY_NAME.replace("%0", defaultDatabase));
		}
		public String getLegacyName(String dbName) {
			return (String)get(DATABASE_LEGACY_NAME.replace("%0", dbName));
		}
		
		public String getPackage() {
			return (String)get(DATABASE_PACKAGE_NAME.replace("%0", defaultDatabase));
		}
		public String getPackage(String dbName) {
			return (String)get(DATABASE_PACKAGE_NAME.replace("%0", dbName));
		}
		
		public String getVendor() {
			return (String)get(DATABASE_VENDOR_NAME.replace("%0", defaultDatabase));
		}
		public String getVendor(String dbName) {
			return (String)get(DATABASE_VENDOR_NAME.replace("%0", dbName));
		}
		
		public DatabaseTableProperties getTables() {
			return databaseTables;
		}
		public void setTables(Properties tables) {
			databaseTables=(DatabaseTableProperties)tables;
		}
		
		public String getPackageKey() {
			return DATABASE_PACKAGE_NAME.replace("%0", defaultDatabase);
		}
	}
	
	public static final String DATABASE_CLASS_NAME = "table.%0.class.name";
	public static final String DATABASE_FILE_NAME = "table.%0.file.name";
	public static final String DATABASE_COLUMN_NAME = "table.%0.column.%1.name";
	public static final String DATABASE_COLUMN_TYPE = "table.%0.column.%1.type";
	public static final String DATABASE_COLUMN_NULLS = "table.%0.column.%1.nulls";


	public class DatabaseTableProperties extends Properties {

		private static final long serialVersionUID = 1L;
		private String defaultTable=null;
		private DatabaseProperties  database = null;
		
		public String getTableName(int idx) {
			return (String)get("table."+String.valueOf(idx)+".name"); 
		}
		
		public void setDefaultTable(String defaultTable) {
			this.defaultTable = defaultTable;
		}
		
		public String getClassName() {
			if(defaultTable!=null)
				return (String) get(DATABASE_CLASS_NAME.replace("%0",defaultTable));
			return null;
		}
		
		public String getClassName(String table) {
			if(table!=null)
				return (String) get(DATABASE_CLASS_NAME.replace("%0",table));
			return null;
		}
		
		public String getColumnName(int idx) {
			if(defaultTable!=null)
				return (String) get(DATABASE_CLASS_NAME.replace("%0",defaultTable).
						replace("%1", String.valueOf(idx)));
			return null;
		}
		
		public String getColumnType(int idx) {
			if(defaultTable!=null)
				return (String) get(DATABASE_COLUMN_TYPE.replace("%0",defaultTable).
						replace("%1", String.valueOf(idx)));
			return null;
		}
		
		public String getNulls(int idx) {
			if(defaultTable!=null)
				return (String) get(DATABASE_COLUMN_NULLS.replace("%0",defaultTable).
						replace("%1", String.valueOf(idx)));
			return null;
		}

		
		public String getColumnName(int idx,int col) {
			if(defaultTable!=null)
				return (String) get(DATABASE_CLASS_NAME.replace("%0",getTableName(idx)).
						replace("%1", String.valueOf(col)));
			return null;
		}
		
		public String getColumnType(int idx,int col) {
			if(defaultTable!=null)
				return (String) get(DATABASE_COLUMN_TYPE.replace("%0",getTableName(idx)).
						replace("%1", String.valueOf(col)));
			return null;
		}
		
		public String getNulls(int idx,int col) {
			if(defaultTable!=null)
				return (String) get(DATABASE_COLUMN_NULLS.replace("%0",getTableName(idx)).
						replace("%1", String.valueOf(col)));
			return null;
		}

		public void setDatabase(DatabaseProperties database) {
			this.database = database;
		}

		public String get(String key) {
			String ret=null;
			if((ret=super.getProperty(key))!=null)
				ret=ret.replace("\"", "").toLowerCase();
			return ret;
		}
		
		public DatabaseProperties getDatabase() {
			return database;
		}
		
	}
	
	@SuppressWarnings("unused")
	private String dbFileName=null,dbName=null,
				   tablesFileName,tableName=null,
				   packageName=null,vendorName=null;
	
	private DatabaseProperties dataBaseProps = new DatabaseProperties();
	
	public static void main(String[] args) {
		
		DbMigrate instance = new DbMigrate();
		
		
		instance.getArguments(args);
		
		instance.initialize();
		
		if(!instance.createTables()) {
			System.out.println("Warning: Tables Creation was not successfull or tables already exist.");
		}
		
		if(!instance.populateTables()) {
			System.out.println("Error: Tables population not successful. Exiting with error.");
		}
	}

	private void initialize() {
		
		//Get the database properties.
		if(!dataBaseProps.load()) {
			if(!dataBaseProps.load(dbFileName)) {
				System.out.println("Database.Properties file could not be loaded. Exiting.");
				return;
			}
		} 
		
		if(dataBaseProps.getDefaultDatabase()==null) {
			if(dbName==null) {
				System.out.println("No Default Database or Database Name specified. Exiting.");
				System.exit(1);
			} else {
				dataBaseProps.setDefaultDatabase(dbName);
			}
		}
		
		if(!openConnection()) {
			System.out.println("Database Connection could not be established. Exiting.");
			System.exit(1);
		}
	}

	private void getArguments(String[] args) {
		
		//Get the arguments.
		int i=0;
		while(i<args.length) {
			if(args[i].equalsIgnoreCase("-dbproperties")) {
				dbFileName=args[++i].toLowerCase();
				dataBaseProps.setDefaultDatabase(dbFileName);
				dataBaseProps.put(DATABASE_NAME,dbFileName);
				if(!dataBaseProps.load(dbFileName)) {
					System.out.println("Invalid arguments. Exiting: "+i);
					return;
				}
			} else 
				if(args[i].equalsIgnoreCase("-dbname")) {
					dbName=args[++i].toLowerCase();
				} else 
				if(args[i].equalsIgnoreCase("-tableproperties")) {
					tablesFileName=args[++i].toLowerCase();
					if(!dataBaseProps.loadTables(tablesFileName)) {
						System.out.println("Invalid arguments. Exiting: "+i);
						return;
					}
				} else 
				if(args[i].equalsIgnoreCase("-tablename")) {
					tableName=args[++i].toLowerCase();
				} else 
					if(args[i].equalsIgnoreCase("-package")) {
						packageName=args[++i].toLowerCase();
					}
			
			i++;
		}
	}
	
	private boolean createTables() {
		if(dataBaseProps.getTables()==null) {
			if(tablesFileName==null)
				return false;
			dataBaseProps.loadTables(tablesFileName);
		}
		if(tableName!=null) {
			return createTable(tableName);
		} else {
			boolean ret=true;
			for(int i=1;i<Integer.MAX_VALUE;i++) {
				if(dataBaseProps.getTables().getTableName(i)==null) break;
				ret&=createTable(dataBaseProps.getTables().getTableName(i));
			}
			return ret;
		}
	}
	
	private boolean createTable(String tableName) {
		String sql = "create table %0 (%1)";
		sql=sql.replace("%0", tableName.toUpperCase().replace("\"",""));
		StringBuffer columns=new StringBuffer();
		for(int i=1;i<Integer.MAX_VALUE;i++) {
			String name=(String)dataBaseProps.getTables().get("table." +tableName.trim()+".column."+
					String.valueOf(i)+".name");
			String type=(String)dataBaseProps.getTables().get("table." +tableName.trim()+".column."+
					String.valueOf(i)+".type");
			if(name==null||type==null) break;
			if(columns.length()>0)
				columns.append(',');
			//TODO Map type for vendors here
			columns.append(name).append(' ').append(type);
		}
		if(columns.length()>0) {
			sql=sql.replace("%1", columns.toString());
			try {
				if(__dao().prepareStatement(sql))
					__dao().executeStatement();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private boolean populateTables() {
		if(dataBaseProps.getTables()==null) {
			if(tablesFileName==null)
				return false;
			dataBaseProps.loadTables(tablesFileName);
		}
		if(tableName!=null) {
			return populateTable(tableName);
		} else {
			boolean ret=true;
			for(int i=1;i<Integer.MAX_VALUE;i++) {
				if(dataBaseProps.getTables().getTableName(i)==null) break;
				ret&=populateTable(dataBaseProps.getTables().getTableName(i));
			}
			return ret;
		}
	}
	
	private boolean populateTable(String tableName) {
		String sql = "insert into %0 (%1)";
		sql=sql.replace("%0", tableName.toUpperCase().replace("\"",""));
		StringBuffer columns=new StringBuffer();
		for(int i=1;i<Integer.MAX_VALUE;i++) {
			String name=(String)dataBaseProps.getTables().get("table." +tableName.trim()+".column."+
					String.valueOf(i)+".name");
			String type=(String)dataBaseProps.getTables().get("table." +tableName.trim()+".column."+
					String.valueOf(i)+".type");
			if(name==null||type==null) break;
			if(columns.length()>0)
				columns.append(',');
			//TODO Map type for vendors here
			columns.append(name).append(' ').append(type);
		}
		if(columns.length()>0) {
			sql=sql.replace("%1", columns.toString());
			try {
				if(__dao().prepareStatement(sql))
					__dao().executeStatement();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
	private boolean openConnection() {
		try {
			return __dao().openConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
		
}
