/*
Copyright (c) 2013 AWARE Mobile Context Instrumentation Middleware/Framework
http://www.awareframework.com

AWARE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) any later version (GPLv3+).

AWARE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details: http://www.gnu.org/licenses/gpl.html
*/
package com.aware.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.IntentService;
import android.app.DownloadManager.Query;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aware.Aware;
import com.aware.R;
import com.aware.providers.Aware_Provider.Aware_Plugins;
import com.aware.utils.Aware_Plugin;
import com.aware.utils.Http;

/**
 * UI to manage installed plugins. 
 * @author denzil
 *
 */
public class Plugins_Manager extends Activity {
    
    private static final String TAG = "AWARE::Plugin Manager";
    
    /**
     * Received broadcast: ACTION_AWARE_ACTIVATE_PLUGIN<br/>
     * Extra: package_name (String) of the plugin to activate <br/>
     * This will request the framework to activate a plugin for use in another application/plugin
     */
    public static final String ACTION_AWARE_ACTIVATE_PLUGIN = "ACTION_AWARE_ACTIVATE_PLUGIN";
    
    /**
     * Received broadcast: ACTION_AWARE_DEACTIVATE_PLUGIN<br/>
     * Extra: package_name (String) of the plugin to activate <br/>
     * This will request the framework to deactivate a plugin for use in another application/plugin
     */
    public static final String ACTION_AWARE_DEACTIVATE_PLUGIN = "ACTION_AWARE_DEACTIVATE_PLUGIN";
    
    /**
     * Extra (String) for plugin package name
     */
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    
    private static LayoutInflater mInflater = null;
    private static PackageManager mPkgManager = null;
    private static LinearLayout mPlugins_list = null;
    private static ProgressBar mLoader = null;
    private static ArrayList<Long> AWARE_PLUGIN_DOWNLOAD_IDS = new ArrayList<Long>();
    
    private static JSONArray online_packages = new JSONArray();
    
    private class PollPackages extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		mLoader.setVisibility(View.VISIBLE);
    	}
    	
    	@Override
    	protected Void doInBackground(Void... params) {
    		Http fetch = new Http();
	    	HttpResponse response = fetch.dataGET("http://www.awareframework.com/index.php/awaredev/get_addons");
	    	if( response != null && response.getStatusLine().getStatusCode() == 200 ) {
	    		try {
					String data = EntityUtils.toString(response.getEntity());
					online_packages = new JSONArray(data);
				} catch (ParseException e) {
					if( Aware.DEBUG ) Log.d( Aware.TAG, e.getMessage() );
				} catch (IOException e) {
					if( Aware.DEBUG ) Log.d( Aware.TAG, e.getMessage() );
				} catch (JSONException e) {
					if( Aware.DEBUG ) Log.d( Aware.TAG, e.getMessage() );
				}
	    	} else {
	    		if( Aware.DEBUG ) Log.d(Aware.TAG, "Unable to fetch packages from online repository...");
	    	}
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    		update_UI();
    		mLoader.setVisibility(View.GONE);
    	}
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugins_manager);
        
        mInflater = getLayoutInflater();
        mPkgManager = getPackageManager();
        
        mPlugins_list = (LinearLayout) findViewById(R.id.plugin_list);
        
        mLoader = (ProgressBar) findViewById(R.id.loading_addons);
        mLoader.setIndeterminate(true);
        
        new PollPackages().execute();
    }
    
    /**
     * Detect if the package was updated from the server
     * @param package_name
     * @param version
     * @return
     */
    private boolean updated_package( String package_name, int version ) {
    	for( int i=0; i< online_packages.length(); i++ ){
    		try {
				JSONObject pkg = online_packages.getJSONObject(i);
				if( pkg.getString("package").equals(package_name) ) {
					if ( pkg.getInt("version") > version ) {
						return true;
					} else {
						return false;
					}
				}
			} catch (JSONException e) {
				if( Aware.DEBUG) Log.d(Aware.TAG, e.getMessage());
			}
    	}
    	return false;
    }
    
    /**
     * Detect if the package doesn't exist online 
     * @param package_name
     * @return
     */
    private boolean is_online( String package_name ) {
    	for( int i=0; i< online_packages.length(); i++ ){
    		try {
				JSONObject pkg = online_packages.getJSONObject(i);
				if( pkg.getString("package").equals(package_name) ) {
					return true;
				}
			} catch (JSONException e) {
				if( Aware.DEBUG) Log.d(Aware.TAG, e.getMessage());
			}
    	}
    	return false;
    }
    
    /**
     * Check if plugin has settings UI
     * @param package_name
     * @return
     */
    private boolean hasSettings( String package_name ) {
    	boolean settings = false;
    	try {
			PackageInfo pkgInfo = getPackageManager().getPackageInfo(package_name, PackageManager.GET_ACTIVITIES);
			ActivityInfo[] activities = pkgInfo.activities;
			if( activities != null ) {
				for(ActivityInfo info : activities ) {
					if( info.name.contains("Settings") ) {
						settings = true;
						break;
					}
				}
			}
		} catch (NameNotFoundException e) {
			if( Aware.DEBUG ) Log.d( Aware.TAG, e.getMessage());
		}
    	return settings;
    }
    
    private static void get_online_package( Context context, String package_name, String name ) {
    	//Create the folder where all the databases will be stored on external storage
        File folders = new File(Environment.getExternalStorageDirectory()+"/AWARE/plugins/");
        folders.mkdirs();
    	
    	String url = "http://www.awareframework.com/addons/" + package_name + ".apk";
    	DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
    	
    	if( name.length() > 0 ) {
    		request.setDescription("Downloading " + name );
    	}else request.setDescription("Downloading " + package_name );
    	
    	request.setTitle("AWARE add-on");
    	request.setDestinationInExternalPublicDir("/", "AWARE/plugins/" + package_name + ".apk");
    	
    	DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
    	AWARE_PLUGIN_DOWNLOAD_IDS.add(manager.enqueue(request));
    }
    
    /**
     * Updates the UI
     */
    private void update_UI() {
    	
    	mPlugins_list.removeAllViews();
        
        for(int i=0; i<online_packages.length(); i++ ) {
        	
        	final View row = mInflater.inflate(R.layout.plugin_row, null);
            final TextView name = (TextView) row.findViewById(R.id.plugin_name);
            final TextView version = (TextView) row.findViewById(R.id.plugin_version);
            final TextView author = (TextView) row.findViewById(R.id.plugin_author);
            
            final ToggleButton toggle = (ToggleButton) row.findViewById(R.id.plugin_toggle);
            final ImageButton settings = (ImageButton) row.findViewById(R.id.plugin_settings);
            final ImageButton update_download = (ImageButton) row.findViewById(R.id.plugin_update_download);
        	
        	try {
        		
        		JSONObject online_pkg = online_packages.getJSONObject(i);
        		
				final String pkg_name = online_pkg.getString("name");
				final String pkg_package = online_pkg.getString("package");
				final String pkg_author = online_pkg.getString("author");
				final int pkg_version = online_pkg.getInt("version");
				
				Cursor local_pkg = getContentResolver().query(Aware_Plugins.CONTENT_URI, null, Aware_Plugins.PLUGIN_PACKAGE_NAME + "='" + pkg_package + "'", null, null);
				if( local_pkg == null || ! local_pkg.moveToFirst() ) { //we don't have it, so add it as a possible to download option
					
					name.setText(pkg_name);
					author.setText("Author: " + pkg_author);
					version.setText("Version: "+ pkg_version);
					
					toggle.setVisibility(ToggleButton.GONE);
					settings.setVisibility(View.INVISIBLE);
					
					update_download.setVisibility(View.VISIBLE);
					update_download.setImageResource(R.drawable.ic_action_download);
					update_download.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Log.d(Aware.TAG, "Try to download package...");
							get_online_package(getApplicationContext(), pkg_package, pkg_name);
						}
					});
					
				} else if( local_pkg != null && local_pkg.moveToFirst() ) {
					name.setText(pkg_name);
					int local_version = local_pkg.getInt(local_pkg.getColumnIndex(Aware_Plugins.PLUGIN_VERSION));
					
					if( updated_package( pkg_package, local_version ) ) { //online is more recent
						author.setText("Author: " + pkg_author);
						version.setText("Updated: "+ pkg_version);
						
						update_download.setImageResource(R.drawable.ic_action_update);
						update_download.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Log.d(Aware.TAG, "Try to update package...");
								get_online_package(getApplicationContext(), pkg_package, pkg_name);
							}
						});
					} 
					if( local_version > pkg_version ) { //local is more recent
						author.setText("Author: You");
						version.setText("Build: " + local_version);
						update_download.setVisibility(View.GONE);
					} else { //same version local and online. Use online information because has authorship
						author.setText("Author: " + pkg_author);
						version.setText("Version: "+ pkg_version);
						update_download.setVisibility(View.GONE);
					}
					
					int pluginStatus = local_pkg.getInt(local_pkg.getColumnIndex(Aware_Plugins.PLUGIN_STATUS));
					switch(pluginStatus) {
	                    case Aware_Plugin.STATUS_PLUGIN_OFF:
	                        toggle.setChecked(false);
	                        break;
	                    case Aware_Plugin.STATUS_PLUGIN_ON:
	                        toggle.setChecked(true);
	                        
	                        Intent launch = new Intent();
	                        launch.setClassName(pkg_package, pkg_package+".Plugin");
	                        startService(launch);
	                        
	                        if( Aware.DEBUG ) Log.d(TAG,pkg_name + " started...");
	                        
	                        break;
	                }
					
					toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
	                    @Override
	                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	                        if( isChecked ) {
	                        	
	                            Intent launch = new Intent();
	                            launch.setClassName(pkg_package, pkg_package+".Plugin");
	                            startService(launch);
	                            
	                            if( Aware.DEBUG ) Log.d(TAG,pkg_name + " started...");
	                            
	                            ContentValues rowData = new ContentValues();
	                            rowData.put(Aware_Plugins.PLUGIN_STATUS, Aware_Plugin.STATUS_PLUGIN_ON);
	                            getContentResolver().update(Aware_Plugins.CONTENT_URI, rowData, Aware_Plugins.PLUGIN_PACKAGE_NAME + "=?", new String[]{ pkg_package });
	                            
	                        } else {
	                            Intent terminate = new Intent();
	                            terminate.setClassName(pkg_package, pkg_package+".Plugin");
	                            stopService(terminate);
	                            
	                            if( Aware.DEBUG ) Log.d(TAG,pkg_package + " terminated...");
	                            
	                            ContentValues rowData = new ContentValues();
	                            rowData.put(Aware_Plugins.PLUGIN_STATUS, Aware_Plugin.STATUS_PLUGIN_OFF);
	                            getContentResolver().update(Aware_Plugins.CONTENT_URI, rowData, Aware_Plugins.PLUGIN_PACKAGE_NAME + "=?", new String[]{ pkg_package });
	                            
	                        }
	                    }
	                });
	                
					if( ! hasSettings( pkg_package ) ) {
	                	settings.setVisibility(View.INVISIBLE);
	                } else {
	                	settings.setVisibility(ImageButton.VISIBLE);
	                	settings.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent settings = new Intent("android.intent.action.MAIN");
								settings.setComponent(new ComponentName(pkg_package, pkg_package+".Settings"));
								try {
									startActivity(settings);
								} catch (ActivityNotFoundException e) {
									Toast.makeText(getApplicationContext(), "Plugin without settings!", Toast.LENGTH_SHORT).show();
								}
							}
						});
	                }
				}
				
				mPlugins_list.addView(row);
				
				if( local_pkg != null && ! local_pkg.isClosed() ) local_pkg.close();
				
			} catch (JSONException e) {
				if( Aware.DEBUG) Log.d(Aware.TAG, e.getMessage());
			}
        }
        
        //Check local only deployments
        Cursor plugins = getContentResolver().query(Aware_Plugins.CONTENT_URI, null, null, null, null);
        if( plugins != null && plugins.moveToFirst() ) {
        	do {
        	    if( ! is_online( plugins.getString(plugins.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME )) ) ) {
            		
        	    	final View row = mInflater.inflate(R.layout.plugin_row, null);
                    final TextView name = (TextView) row.findViewById(R.id.plugin_name);
                    final TextView version = (TextView) row.findViewById(R.id.plugin_version);
                    final TextView author = (TextView) row.findViewById(R.id.plugin_author);
                    
                    final ToggleButton toggle = (ToggleButton) row.findViewById(R.id.plugin_toggle);
                    final ImageButton settings = (ImageButton) row.findViewById(R.id.plugin_settings);
                    final ImageButton update_download = (ImageButton) row.findViewById(R.id.plugin_update_download);
                	
                    final String pluginPackage = plugins.getString(plugins.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME));
                    final String pluginName = plugins.getString(plugins.getColumnIndex(Aware_Plugins.PLUGIN_NAME));
                    final int pluginStatus = plugins.getInt(plugins.getColumnIndex(Aware_Plugins.PLUGIN_STATUS));
                    final int pluginVersion = plugins.getInt(plugins.getColumnIndex(Aware_Plugins.PLUGIN_VERSION));
                    
                    name.setText(pluginName);
                    author.setText("Author: You");
                    version.setText("Build: "+ pluginVersion);
                    update_download.setVisibility(View.GONE);
                    
                    if( ! hasSettings(pluginPackage) ) {
                    	settings.setVisibility(View.INVISIBLE);
                    } else {
                    	settings.setVisibility(View.VISIBLE);
                    }
                    
                    switch(pluginStatus) {
                        case Aware_Plugin.STATUS_PLUGIN_OFF:
                            toggle.setChecked(false);
                            break;
                        case Aware_Plugin.STATUS_PLUGIN_ON:
                            toggle.setChecked(true);
                            
                            Intent launch = new Intent();
                            launch.setClassName(pluginPackage, pluginPackage+".Plugin");
                            startService(launch);
                            
                            if( Aware.DEBUG ) Log.d(TAG,pluginName + " started...");
                            
                            break;
                    }
                    
                    toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if( isChecked ) {
                            	
                                Intent launch = new Intent();
                                launch.setClassName(pluginPackage, pluginPackage+".Plugin");
                                startService(launch);
                                
                                if( Aware.DEBUG ) Log.d(TAG,pluginName + " started...");
                                
                                ContentValues rowData = new ContentValues();
                                rowData.put(Aware_Plugins.PLUGIN_STATUS, Aware_Plugin.STATUS_PLUGIN_ON);
                                getContentResolver().update(Aware_Plugins.CONTENT_URI, rowData, Aware_Plugins.PLUGIN_PACKAGE_NAME + "=?", new String[]{ pluginPackage });
                                
                            } else {
                                Intent terminate = new Intent();
                                terminate.setClassName(pluginPackage, pluginPackage+".Plugin");
                                stopService(terminate);
                                
                                if( Aware.DEBUG ) Log.d(TAG,pluginName + " terminated...");
                                
                                ContentValues rowData = new ContentValues();
                                rowData.put(Aware_Plugins.PLUGIN_STATUS, Aware_Plugin.STATUS_PLUGIN_OFF);
                                getContentResolver().update(Aware_Plugins.CONTENT_URI, rowData, Aware_Plugins.PLUGIN_PACKAGE_NAME + "=?", new String[]{ pluginPackage });
                                
                            }
                        }
                    });
                    
                    settings.setOnClickListener(new View.OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						Intent settings = new Intent("android.intent.action.MAIN");
    						settings.setComponent(new ComponentName(pluginPackage, pluginPackage+".Settings"));
    						try {
    							startActivity(settings);
    						} catch (ActivityNotFoundException e) {
    							Toast.makeText(getApplicationContext(), "Plugin without settings!", Toast.LENGTH_SHORT).show();
    						}
    					}
    				});
                    
                    mPlugins_list.addView(row);
            	}
        	    
            }while(plugins.moveToNext());
        }
        if( plugins!= null && ! plugins.isClosed() ) plugins.close();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	new PollPackages().execute();
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	new PollPackages().execute();
    }
    
    private static int getVersion( String package_name ) {
    	try {
			PackageInfo pkgInfo = mPkgManager.getPackageInfo(package_name, PackageManager.GET_META_DATA);
			return pkgInfo.versionCode;
		} catch (NameNotFoundException e) {
			if( Aware.DEBUG ) Log.d( Aware.TAG, e.getMessage());
		}
    	return 0;
    }
    
    /**
     * Downloads missing plugins on the background for the user
     * @author denzil
     *
     */
    public static class Plugin_Downloader extends IntentService {
		public Plugin_Downloader() {
			super("Plugin Downloader");
		}

		@Override
		protected void onHandleIntent(Intent intent) {
			if( intent.getStringExtra("MISSING_PLUGIN") != null ) {
	            String missing_package = intent.getStringExtra("MISSING_PLUGIN");
	            get_online_package(getApplicationContext(), missing_package, "");
	        }
		}
    }
    
    /**
     * BroadcastReceiver that will monitor requests to activate plugins from other applications/plugins using the framework
     * ACTION_AWARE_ACTIVATE_PLUGIN <br/>
     * -- EXTRA_PACKAGE_NAME (string) of the plugin to activate if exists.<br/>
     * ACTION_AWARE_DEACTIVATE_PLUGIN <br/>
     * -- EXTRA_PACKAGE_NAME (string) of the plugin to deactivate if exists.<br/>
     * @author denzil
     *
     */
    public static class Plugin_Controller extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		
    		if( intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE) ) {
            	DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            	long download_id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            	
            	for( int i = 0; i < AWARE_PLUGIN_DOWNLOAD_IDS.size(); i++ ) {
            	    long queue = AWARE_PLUGIN_DOWNLOAD_IDS.get(i);
            	    if( download_id == queue ) {
            	        if( Aware.DEBUG ) Log.d(Aware.TAG, "AWARE plugin received...");
                        
                        Cursor cur = manager.query(new Query().setFilterById(queue));
                        if( cur != null && cur.moveToFirst() ) {
                            if( cur.getInt(cur.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL ) {
                                String filePath = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                
                                if( Aware.DEBUG ) Log.d(Aware.TAG, "Plugin to install:" + filePath);
                                
                                File mFile = new File( Uri.parse(filePath).getPath() );
                                Intent promptInstall = new Intent(Intent.ACTION_VIEW);
                                promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                promptInstall.setDataAndType(Uri.fromFile(mFile), "application/vnd.android.package-archive");
                                context.startActivity(promptInstall);
                            }
                        }
                        if( cur != null && ! cur.isClosed() ) cur.close();
            	    }
            	}
            	AWARE_PLUGIN_DOWNLOAD_IDS.remove(download_id); //dequeue
            }
    		
    		if( intent.getAction().equals(ACTION_AWARE_ACTIVATE_PLUGIN) ) {
            	String extra_package = intent.getStringExtra(EXTRA_PACKAGE_NAME);
                if( extra_package != null ) {
                    
                    Cursor installed = context.getContentResolver().query(Aware_Plugins.CONTENT_URI, null, Aware_Plugins.PLUGIN_PACKAGE_NAME + "='"+extra_package+"'", null, null);
                    if( installed != null && installed.moveToFirst() ) {
                        
                        String package_name = installed.getString(installed.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME));
                        String plugin_name = installed.getString(installed.getColumnIndex(Aware_Plugins.PLUGIN_NAME));
                        
                        Intent launch = new Intent();
                        launch.setClassName(package_name, package_name + ".Plugin");
                        context.startService(launch);
                    
                        if( Aware.DEBUG ) Log.d(TAG, plugin_name + " started...");
                        
                        ContentValues rowData = new ContentValues();
                        rowData.put(Aware_Plugins.PLUGIN_STATUS, Aware_Plugin.STATUS_PLUGIN_ON);
                        context.getContentResolver().update(Aware_Plugins.CONTENT_URI, rowData, Aware_Plugins.PLUGIN_PACKAGE_NAME + "='"+package_name+"'", null);
                    
                    } else {
                        if(Aware.DEBUG) Log.w(TAG, extra_package + " is not installed in this device!");
                        
                        Intent pluginDownloader = new Intent(context, Plugin_Downloader.class);
                        pluginDownloader.putExtra("MISSING_PLUGIN", extra_package);
                        context.startService(pluginDownloader);
                    }
                    if( installed != null && ! installed.isClosed() ) installed.close();
                } else {
                    if(Aware.DEBUG) Log.w(TAG,"Forgot to set package_name EXTRA on the broadcast");
                }
            }
        	
        	if( intent.getAction().equals(ACTION_AWARE_DEACTIVATE_PLUGIN) ) {
        		String extra_package = intent.getStringExtra(EXTRA_PACKAGE_NAME);
        		if( extra_package != null ) {
        			Cursor installed = context.getContentResolver().query(Aware_Plugins.CONTENT_URI, null, Aware_Plugins.PLUGIN_PACKAGE_NAME + "='"+extra_package+"'", null, null);
                    if( installed != null && installed.moveToFirst() ) {
                        
                        String package_name = installed.getString(installed.getColumnIndex(Aware_Plugins.PLUGIN_PACKAGE_NAME));
                        String plugin_name = installed.getString(installed.getColumnIndex(Aware_Plugins.PLUGIN_NAME));
                        
                        Intent terminate = new Intent();
                        terminate.setClassName(package_name, package_name+".Plugin");
                        context.stopService(terminate);
                    
                        if( Aware.DEBUG ) Log.d(TAG, plugin_name + " terminated...");
                        
                        ContentValues rowData = new ContentValues();
                        rowData.put(Aware_Plugins.PLUGIN_STATUS, Aware_Plugin.STATUS_PLUGIN_OFF);
                        context.getContentResolver().update(Aware_Plugins.CONTENT_URI, rowData, Aware_Plugins.PLUGIN_PACKAGE_NAME + "='"+package_name+"'", null);
                    
                    }
                    if( installed != null && ! installed.isClosed() ) installed.close();
        		} else {
        			if(Aware.DEBUG) Log.w(TAG,"Forgot to set package_name EXTRA on the broadcast");
        		}
        	}
    	}
    }
    
    /**
     * System triggered<br/>
     * - ACTION_PACKAGE_ADDED: new package is installed on the device<br/>
     * - ACTION_PACKAGE_REMOVED: new package is removed from the device<br/>
     * @author denzil
     *
     */
    public static class PluginMonitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	if( mPkgManager == null ) mPkgManager = context.getPackageManager();
            Bundle extras = intent.getExtras();
            
            Uri packageUri = intent.getData();
            if( packageUri == null ) return;
            String packageName = packageUri.getSchemeSpecificPart();
            if( packageName == null ) return;
            
            if( ! packageName.matches("com.aware.plugin.*") ) return;
            
            if( intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ) {
                
                if( extras.getBoolean(Intent.EXTRA_REPLACING) ) {
                    if(Aware.DEBUG) Log.d(TAG,packageName + " is updating!");
                    ContentValues rowData = new ContentValues();
                    rowData.put(Aware_Plugins.PLUGIN_VERSION, getVersion(packageName));
                    context.getContentResolver().update(Aware_Plugins.CONTENT_URI, rowData, Aware_Plugins.PLUGIN_PACKAGE_NAME + "='" + packageName+"'", null);
                    
                    Cursor current_status = context.getContentResolver().query(Aware_Plugins.CONTENT_URI, new String[]{Aware_Plugins.PLUGIN_STATUS}, Aware_Plugins.PLUGIN_PACKAGE_NAME + "='"+packageName+"'", null, null);
                    if( current_status != null && current_status.moveToFirst() ) {
                        if( current_status.getInt(current_status.getColumnIndex(Aware_Plugins.PLUGIN_STATUS)) == Aware_Plugin.STATUS_PLUGIN_ON ) {
                            Intent aware = new Intent(Aware.ACTION_AWARE_REFRESH);
                            context.sendBroadcast(aware);
                        }
                    }
                    if( current_status != null && ! current_status.isClosed() ) current_status.close();
                    return;
                }
                
                ApplicationInfo appInfo = null;
                try {
                    appInfo = mPkgManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
                } catch( final NameNotFoundException e ) {
                    appInfo = null;
                }
                String appName = ( appInfo != null ) ? (String) mPkgManager.getApplicationLabel(appInfo):"";
                
                ContentValues rowData = new ContentValues();
                rowData.put(Aware_Plugins.PLUGIN_PACKAGE_NAME, appInfo.packageName);
                rowData.put(Aware_Plugins.PLUGIN_NAME, appName);
                rowData.put(Aware_Plugins.PLUGIN_VERSION, getVersion(packageName));
                rowData.put(Aware_Plugins.PLUGIN_STATUS, Aware_Plugin.STATUS_PLUGIN_ON);
                context.getContentResolver().insert(Aware_Plugins.CONTENT_URI, rowData);
                
                if( Aware.DEBUG ) Log.d(TAG,"AWARE plugin added and activated:" + appInfo.packageName);
                
                Intent aware = new Intent(Aware.ACTION_AWARE_REFRESH);
                context.sendBroadcast(aware);
            }
            
            if( intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) ) {
                
                if( extras.getBoolean(Intent.EXTRA_REPLACING) ) {
                    if(Aware.DEBUG) Log.d(TAG,packageName + " is updating!");
                    return;
                }
            
                context.getContentResolver().delete(Aware_Plugins.CONTENT_URI, Aware_Plugins.PLUGIN_PACKAGE_NAME + "='" + packageName + "'", null);
                if( Aware.DEBUG ) Log.d(TAG,"AWARE plugin removed:" + packageName);
            }
        }
    }
}
