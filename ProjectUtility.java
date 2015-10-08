package pkgProjectOne;

import java.net.URL;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class ProjectUtility {
	public static ServiceInstance objSi;
	public static ServiceInstance objSiVcenter;
	public static Folder adminRootFolderPath;
	public static Folder vCenterRootFolderPath;
	public static ManagedEntity[] listDcs;
	public static ManagedEntity[] listHosts;
	public static ManagedEntity[] listVms;
	
	public static String urlVcenter = "https://130.65.132.113/sdk";
	public static String userName = "administrator";
	public static String userNameVcenter = "student@vsphere.local";
	public static String password = "12!@qwQW";
	
	public ProjectUtility() {
		try{
			objSi = new ServiceInstance(new URL(urlVcenter), userName, password, true);
			
			objSiVcenter = new ServiceInstance(new URL("https://130.65.132.19/sdk"), userNameVcenter, password, true);

			adminRootFolderPath = objSi.getRootFolder();			

			vCenterRootFolderPath = objSiVcenter.getRootFolder();
			
			listDcs = new InventoryNavigator(adminRootFolderPath).searchManagedEntities(
					new String[][] { {"Datacenter", "name" }, }, true);

			listHosts = new InventoryNavigator(adminRootFolderPath).searchManagedEntities(
					new String[][] { {"HostSystem", "name" }, }, true);

			listVms = new InventoryNavigator(adminRootFolderPath).searchManagedEntities(
					new String[][] { {"VirtualMachine", "name" }, }, true);
		}
		catch (Exception e){
			System.out.println("VMMonitor object initialization eroor : " + e);
		}
	}
	
	// A function to ping a VM
	public static boolean ping(String ipAddress) throws Exception {
		String cmd = "";

		if (System.getProperty("os.name").startsWith("Windows")) {
			// For Windows
			cmd = "ping -n 3 " + ipAddress;
		} else {
			// For Linux 
			cmd = "ping -c 3 " + ipAddress;
		}

		System.out.println("Ping "+ ipAddress + "......");
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();		
		return process.exitValue() == 0;
	}
}
