package pkgProjectOne;

import com.vmware.vim25.mo.*;

public class VmManager {
	public  String getVMIp(VirtualMachine vm){
		return vm.getGuest().getIpAddress();
	}
	
	public boolean powerOnVm(VirtualMachine objVm){
		boolean powerOn = false;
		try{
			if(objVm != null){
				Task objTask = objVm.powerOnVM_Task(null);
				@SuppressWarnings("deprecation")
				String taskStatus = objTask.waitForMe();
				if(taskStatus==Task.SUCCESS)
				{
					System.out.println("VM : " + objVm.getName() + " is Powered On.");
					powerOn = true;
				}
				else
				{
					System.out.println("VM : " + objVm.getName() + " Failure while powered On.");
					powerOn = false;
				}			
			}
		}
		catch (Exception e){
			System.out.println("Exception while Power On : "+e);
		}
		return powerOn;
	}
	
	public boolean powerOffVm(VirtualMachine objVm){
		boolean powerOff = false;
		try{
			if(objVm != null){
				Task objTask = objVm.powerOffVM_Task();
				@SuppressWarnings("deprecation")
				String taskStatus = objTask.waitForMe();
				if(taskStatus == Task.SUCCESS)
				{
					System.out.println("VM : " + objVm.getName() + " is Powered Off.");
					powerOff = true;
				}
				else
				{
					System.out.println("vm:" + objVm.getName() + " Failure while powered Off.");
					powerOff = false;
				}			
			}
		}
		catch (Exception e){
			System.out.println("Exception while Power off : "+e);
		}
		return powerOff;
	}
}
