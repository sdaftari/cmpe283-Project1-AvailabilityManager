package pkgProjectOne;

import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.Permission;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VhostManager {
	public ManagedEntityStatus getHeartbeat(HostSystem vHost){
		return vHost.getOverallStatus();
	}
	
	public static String getIpAddressOfHost(HostSystem objHost){
		return objHost.getName();
	}
	
	public HostSystem getParentVhost(VirtualMachine objVm){
		HostSystem objVhost = null;

		for(int i=0; i < ProjectUtility.listHosts.length; i++){
			ManagedEntity[] vmList;

			if(ProjectUtility.listHosts[i] instanceof HostSystem)
			{
				try {
					vmList =((HostSystem) ProjectUtility.listHosts[i]).getVms();

					for(int j =0; j < vmList.length; j++)
					{
						if(vmList[j].getName().equalsIgnoreCase(objVm.getName()))	
						{
							objVhost = (HostSystem)ProjectUtility.listHosts[j];
							break;
						}
					}
				}catch (Exception e){
					System.out.println("Exception in Returning parent Vhost : "+e);
				}
			}
		}
		return objVhost;
	}
	
	@SuppressWarnings("deprecation")
	public String  addVhost(){		
		String newIpOfHost = "130.65.133.16";
		HostConnectSpec objSpec = new HostConnectSpec();
		objSpec.setHostName("130.65.133.16");
		objSpec.setUserName("root");
		objSpec.setPassword("12!@qwQW");
		objSpec.setSslThumbprint("74:DE:ED:D1:99:B4:98:51:FA:8D:64:3A:97:D4:3B:10:D5:41:6E:48");
		ComputeResourceConfigSpec compResSpec = new ComputeResourceConfigSpec();
		Task task  = null;
		try {			
			Permission permission = new Permission();
			permission.setPropagate(true);
			permission.setEntity(ProjectUtility.objSi.getMOR());

			task = ((Datacenter)ProjectUtility.listDcs[0]).getHostFolder().addStandaloneHost_Task(objSpec, compResSpec, true);
			try {
				if(task.waitForMe() == Task.SUCCESS){
					System.out.println("Created Host Succesfully");
					return newIpOfHost;
				}
			} catch (Exception e) {
				System.out.println("Error in creating a new vHost2 : " + e);
			}
		} catch (Exception e) {
			System.out.println("Error in creating a new vHost : " + e);
		}

		return "";
	}
	
	public VirtualMachinePowerState getPowerStateofHost(HostSystem objHost){
		try{
			VirtualMachine host;
			String vCenterHost = SnapshotManager.getVcenterHosts(objHost.getName());
			host = (VirtualMachine)new InventoryNavigator(ProjectUtility.vCenterRootFolderPath).searchManagedEntity("VirtualMachine", vCenterHost);
			return host.getRuntime().getPowerState();
		}
		catch(Exception e){
			System.out.println("Exception while getting host power sate : "+e);
		}		
		return null;
	}
	
	public boolean powerOnHost(HostSystem host){
		boolean powerOn = false;
		VirtualMachine vmHost;

		try{
			String vCenterHost = SnapshotManager.getVcenterHosts(host.getName());
			vmHost=(VirtualMachine)new InventoryNavigator(ProjectUtility.vCenterRootFolderPath).searchManagedEntity("VirtualMachine", vCenterHost);
			Task objTask = vmHost.powerOnVM_Task(null);
			String taskStatus = objTask.waitForTask();
			if(taskStatus==Task.SUCCESS)
			{
				powerOn = true;
			}
			else
			{
				powerOn = false;
			}
		}
		catch (Exception e){
			System.out.println("Exception while Power on : "+e);
		}
		return powerOn;
	}
	
	@SuppressWarnings("static-access")
	public boolean revertHostToSnapshot(HostSystem host){
		try {
			VirtualMachine hostVm;
			String vCenterHost = SnapshotManager.getVcenterHosts(host.getName());
			hostVm=(VirtualMachine)new InventoryNavigator(ProjectUtility.vCenterRootFolderPath).searchManagedEntity("VirtualMachine", vCenterHost);

			Task objTask = hostVm.revertToCurrentSnapshot_Task(null);
			if(objTask.waitForTask() == objTask.SUCCESS)
			{
				System.out.println("Vhost  "+ host.getName() + " reverted to snapshot successfully !!!");
				return true;
			}
			else
			{
				System.out.println("Vhost snapshot reversal unsuccessful.");
			    return false;
			}
		}
		catch (Exception e){
			System.out.println("error in reverting : "+e);
		}
		return false;
	}
}
