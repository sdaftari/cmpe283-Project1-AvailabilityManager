package pkgProjectOne;

import java.rmi.RemoteException;
import java.util.ArrayList;

import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class RecoveryManager implements Runnable{
	VhostManager objVhost = new VhostManager();
	VmManager objVm = new VmManager();
	public static ArrayList<String> listRecoveryVM = new ArrayList<String>();
	public long delayHeartBeatThread = 3000;
	
	@SuppressWarnings("static-access")
	public void run()
	{
		try{
			while(true){
				Thread.currentThread().sleep(delayHeartBeatThread); 
				monitorHeartBeatOfVm();
			}	
		}
		catch (Exception e){
			System.out.println("Exception in VMFailure Recovery Thread :" + e);
		}
	}
	
	//Monitor the heartbeats
	@SuppressWarnings("static-access")
	public void monitorHeartBeatOfVm(){
		System.out.println("*****************************Start Checking HeartBeat*****************************");
		try{
			for(int i = 0; i < ProjectUtility.listVms.length; i++)
			{
				if(ProjectUtility.listVms[i] instanceof VirtualMachine)
				{
					VirtualMachine vm = (VirtualMachine) ProjectUtility.listVms[i];
					System.out.println("*****VM Name : "+ vm.getName()+" ***** "+"vm ip : "+vm.getGuest().getIpAddress() +" ***** " +vm.getGuestHeartbeatStatus());

					if(listRecoveryVM.contains(vm.getName()))
					{
						if(!ProjectUtility.ping(vm.getGuest().getIpAddress()))
						{  
							if(vm.getRuntime().getPowerState() == vm.getRuntime().powerState.poweredOff)
							{
								objVm.powerOnVm(vm);
							}
							System.out.println(vm.getName()+"  : Failure recovery already going on !!!!");
							continue;
						}
						else
							listRecoveryVM.remove(vm.getName());
					}

					if(!ProjectUtility.ping(vm.getGuest().getIpAddress()))  // VM not responding to ping
					{
						System.out.println("Ping to VM : "+ vm.getName()+"  Failed ");
						if(vm.getRuntime().getPowerState()==vm.getRuntime().powerState.poweredOff) 
						{
							if(VmAlarmManager.getStatusOfAlarms(vm))  // checking whether user has powered off by checking alarmstate
								System.out.println("*****"+vm.getName() + " powered off by user No failure recovery required-----");
							else
								System.out.println("User has not powered off the system, some other problem !!!");
						}else
							vmFailOverOvercome(vm);
					}
					else // VM is responding to ping
						System.out.println(vm.getName() +" : responding to ping and working Fine ");
				}
			}
			System.out.println("*****************************HeartBeat Cycle Ends*****************************");
		}
		catch (Exception e){
			System.out.println("heartbeat monitor exception : "+e);
		}
	}
	
	//A method to implement failure overcome
	@SuppressWarnings("static-access")
	public void vmFailOverOvercome(VirtualMachine vm){
		System.out.println("*****Failure Recovery for VM : "+ vm.getName() +"  started *****");
		HostSystem hostParent = null;
		hostParent = objVhost.getParentVhost(vm);
		System.out.println("ParentVhost of : " +vm.getName()+" is "+ hostParent.getName());
		try{
			// VM fails but vHost runs properly
			if(ProjectUtility.ping(objVhost.getIpAddressOfHost(hostParent))) 
			{
				System.out.println("ParentVhost is responding to ping ");
				Task objTask = vm.revertToCurrentSnapshot_Task(null);

				if(objTask.waitForTask()==Task.SUCCESS)
				{
					listRecoveryVM.add(vm.getName());             //adding to the recovery list
					System.out.println("****************"+vm.getName()+"reverted to snapshot****************");
					if(vm.getRuntime().getPowerState()==vm.getRuntime().powerState.poweredOff)
					{
						objVm.powerOnVm(vm);
					}
				}else
					System.out.println("*****Failure in reverting  VM : "+vm.getName() +" to its snapshot.*****");
			}
			//vHost not responding properly so revert the vHost from snapshot
			else   
			{ 
				System.out.println("********Parent vHost: "+ hostParent.getName()+" not Responding to ping,starting failure recovery for this vHost********");
				if(objVhost.revertHostToSnapshot(hostParent))
				{
					listRecoveryVM.add(vm.getName());    
					System.out.println("****************vHost reverted to snapshot****************");
					//check if vHost is powered off. If yes, power it on.
					if(objVhost.getPowerStateofHost(hostParent) == vm.getRuntime().getPowerState().poweredOff)
					{
						objVhost.powerOnHost(hostParent);
					}

					if(vm.getRuntime().getPowerState() == vm.getRuntime().powerState.poweredOff)
					{
						objVm.powerOnVm(vm);
					}
				}
				else  
				{
					 // Parent vHost is not pinging even after reverting the snapshot. So find another live vHost and migrate the VM to another host
					System.out.println("vHost Unrecoverable by reverting to snapshot.");
					System.out.println("Searhing another vHost for migrating VM .......");
					HostSystem newHostIp= searchHost(hostParent);
					if(newHostIp != null)  
					{
						System.out.println("New alive host found : "+ newHostIp);
						System.out.println("********Starting Migration to New Host********");
						vm.powerOffVM_Task();						
						//vmMigrate(vm,newHostIp);
						registerToNewHost(vm, hostParent, newHostIp);
						vm.powerOnVM_Task(null);
					}
					// No live host is available so add new host and migrate VM to it
					else                        
					{
						System.out.println("No other alive Host Found. Adding New Host");
						String newIp= objVhost.addVhost();
						vm.powerOffVM_Task();
						//vmMigrate(vm,newIp);
						registerToNewHost(vm, hostParent, newHostIp);
						vm.powerOnVM_Task(null);
					}
				}
			}
		}
		catch (Exception e)
		{		
			System.out.println("Exception in handling vm failure : "+e);
		}
	}
	
	@SuppressWarnings("static-access")
	public HostSystem searchHost(HostSystem parentHost){
		String newHostIpAddress = null;
		try{
			if(ProjectUtility.listHosts.length > 1) 
			{
				for(int i = 0; i < ProjectUtility.listHosts.length;i++)
				{
					if(ProjectUtility.listHosts[i].getName() != parentHost.getName())
					{
						if(ProjectUtility.ping(objVhost.getIpAddressOfHost((HostSystem)ProjectUtility.listHosts[i])))
						{
							newHostIpAddress = objVhost.getIpAddressOfHost((HostSystem)ProjectUtility.listHosts[i]);
							System.out.println("New  alive Host : "+objVhost.getIpAddressOfHost((HostSystem)ProjectUtility.listHosts[i])+" found ");
							//return newHostIpAddress;
							return ((HostSystem)ProjectUtility.listHosts[i]);
						}  						
					}
				}
			}
		}
		catch (Exception e){}

		//return newHostIpAddress;
		return null;
	}
	
	// A function to migrate a VM
	@SuppressWarnings("static-access")
	public static void vmMigrate(VirtualMachine vm, String host) throws Exception {
		HostSystem objHs = (HostSystem) new InventoryNavigator(ProjectUtility.adminRootFolderPath).searchManagedEntity("HostSystem", host);
		ComputeResource cr = (ComputeResource) objHs.getParent();
		Task task = vm.migrateVM_Task(cr.getResourcePool(), objHs, VirtualMachineMovePriority.highPriority, null);
		System.out.println("Try to migrate " + vm.getName() + " to " + objHs.getName());
		if (task.waitForTask() == task.SUCCESS) {
			System.out.println("Migrate virtual machine: " + vm.getName() + " successfully!");
		} else {
			System.out.println("Migrate vm failed!");
		}
	}
	
	public static boolean registerToNewHost(VirtualMachine vm,HostSystem sourceHost,HostSystem targetHost) {
		//System.out.println("Reverting to " +vm.getSnapshot()+"...");
		                ComputeResource cr = (ComputeResource) targetHost.getParent();
		                
		   
		                    String vmName=vm.getName();
		                    //Folder rootFolder = MakeConnection.si.getRootFolder();
		    Datacenter dc;
		try {
		dc = (Datacenter) new InventoryNavigator(ProjectUtility.vCenterRootFolderPath)
		.searchManagedEntity("Datacenter", "T13-DC");
		String vmxPath = "[nfs1team13]" + vmName + "/" + vmName	+ ".vmx";
		ResourcePool rp = cr.getResourcePool();

		Task unRegisterVM = sourceHost.getParent().destroy_Task(); 
		if (unRegisterVM.waitForTask() == Task.SUCCESS) {
			Task registerVM = dc.getVmFolder().registerVM_Task(vmxPath,
				vmName, false, rp, targetHost);
				if (registerVM.waitForTask() == Task.SUCCESS) {
						System.out.println("Registered Success");
				}
		}
		/*************/
		} catch (InvalidProperty e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (RuntimeFault e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		return false;
		}
}
