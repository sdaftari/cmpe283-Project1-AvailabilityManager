package pkgProjectOne;

import java.util.HashMap;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.Task;

public class SnapshotManager implements Runnable{
	public long delayForsnapshotThread = 120000;
	
	@SuppressWarnings("static-access")
	public void run(){
		try{
			while(true){
				takeSnapshotOfHost();
				
				takeSnapshotOfVm();

				Thread.currentThread().sleep(delayForsnapshotThread); 
			}
		}
		catch(Exception e){
			System.out.println("Snapshot capture Thread Eroor : "+ e);
		}
	}
	
	// A function to take a snapshot of VM
	public  void takeSnapshotOfVm(){
		//VM snapshots
		for (int i=0; i<ProjectUtility.listVms.length; i++)
		{
			if(ProjectUtility.listVms[i] instanceof VirtualMachine)
			{
				removeOldSnapshot((VirtualMachine)ProjectUtility.listVms[i]);
				takeNewSnapshot((VirtualMachine)ProjectUtility.listVms[i]);
			}
		}
	}
	
	// A function to take a snapshot of vHost
	public void takeSnapshotOfHost(){
		VirtualMachine host;
		try{
			for(int i = 0; i < ProjectUtility.listHosts.length; i++){

				if(ProjectUtility.listHosts[i] instanceof HostSystem){
					String vCenterHosts = getVcenterHosts(ProjectUtility.listHosts[i].getName());
					host=(VirtualMachine)new InventoryNavigator(ProjectUtility.vCenterRootFolderPath).searchManagedEntity("VirtualMachine", vCenterHosts);
					removeOldSnapshot(host);  
					takeNewSnapshot(host);
				}
			}
		}
		catch(Exception e){
			System.out.println("Error in creating host snapshot");
		}
	}
	
	@SuppressWarnings("deprecation")
	public void removeOldSnapshot(VirtualMachine objVm){
		try{
			Task objTask = ((VirtualMachine) objVm).removeAllSnapshots_Task();      
			if(objTask.waitForMe() == Task.SUCCESS) 
			{
				System.out.println("Removed all old  snapshots for : " + objVm.getName());
			}
		}
		catch(Exception e){
			System.out.println("Error while removing old snapshot for : " + objVm.getName()+ "   "+ e);
		}
	}

	@SuppressWarnings("deprecation")
	public void takeNewSnapshot(VirtualMachine objVm){
		synchronized(objVm){
			try{
				Task objTask = ((VirtualMachine) objVm).createSnapshot_Task(
						objVm.getName()+"_snapshot", null,false, false);

				if(objTask.waitForMe()== Task.SUCCESS)
				{
					System.out.println("Snapshot was created. for : "+objVm.getName());
				}
			}
			catch(Exception e){
				System.out.println("Eroor in taking snapshot");
			}
		}
	}
	
	public static String getVcenterHosts(String objHost){
		return VHOSTMAP.get(objHost);
	}

	// Mapping of vHost Names in vCenter Manager
	@SuppressWarnings("serial")
	public static final HashMap<String, String> VHOSTMAP = new HashMap<String, String>() {
		{
			put("130.65.133.11", "T13-vHost01_133.11");
			put("130.65.133.12", "T13-vHost02_133.12");
			put("130.65.133.13", "T13-vHost03_133.13");
		}
	};
}
