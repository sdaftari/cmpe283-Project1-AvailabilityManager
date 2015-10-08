package pkgProjectOne;

import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.VirtualMachine;

public class VmUsageMonitor {
	public static void displayVmDetails() {
		try{
			if(ProjectUtility.listHosts == null || ProjectUtility.listHosts.length == 0) {
				return;
			}

			System.out.println("*****************vHost List*************************");
			for(int i=0; i< ProjectUtility.listHosts.length; i++) {
				System.out.println("Host IP : " + (i+1) + ": "+ ProjectUtility.listHosts[i].getName());
			}

			System.out.println("***************************************************");

			System.out.println("********************VM List************************");

			for(int i = 0; i < ProjectUtility.listVms.length; i++) {
				VirtualMachine objVm = (VirtualMachine) ProjectUtility.listVms[i];
				//VirtualMachineConfigInfo vmConfigInfo = objVm.getConfig();
				VirtualMachinePowerState vmPowerState = objVm.getRuntime().getPowerState();
				objVm.getResourcePool();
				System.out.println("---------------------------------------------------");
				System.out.println("Virtual Machine " + (i+1));
				System.out.println("VM Name: " + objVm.getName());
				System.out.println("VM OS: " + objVm.getConfig().getGuestFullName());
				System.out.println("VM CPU Number: " + objVm.getConfig().getHardware().numCPU);
				System.out.println("VM Memory: " + objVm.getConfig().getHardware().memoryMB);
				System.out.println("VM Power State: " + vmPowerState.name());
				System.out.println("VM Running State: " + objVm.getGuest().guestState);
				System.out.println("VM IP: " + objVm.getGuest().getIpAddress());
				System.out.println("VM CPU: " + objVm.getConfig().getHardware().getNumCPU());
				System.out.println("VM Memory: " + objVm.getConfig().getHardware().getMemoryMB());
				System.out.println("VM VMTools: " + objVm.getGuest().toolsRunningStatus);
				System.out.println("---------------------------------------------------");
			}
		} catch (Exception e){
			System.out.print(e);
		}
	}

	public void removeConnection(){
		ProjectUtility.objSi.getServerConnection().logout();
		System.out.println("Connection to Vcenter exited !!!");
	}
}
