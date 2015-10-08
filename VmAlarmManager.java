package pkgProjectOne;

import java.rmi.RemoteException;

import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmState;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.VirtualMachine;

public class VmAlarmManager {
	public static AlarmManager objAlarmManager = ProjectUtility.objSi.getAlarmManager();
	
	public void setAlarmOnAllVms() {
		for (int i = 0; i < ProjectUtility.listVms.length; i++)
		{
				
			try{
				if(ProjectUtility.listVms[i] instanceof VirtualMachine)
				{
					VirtualMachine objVm = (VirtualMachine) ProjectUtility.listVms[i];
					Alarm objAlarms[] = objAlarmManager.getAlarm(objVm);
					for (int j = 0; j < objAlarms.length; j++) {
						
						if (objAlarms[j].getAlarmInfo().getName()
								.contains("PoweredOff_"+objVm.getName())) {
							objAlarms[j].removeAlarm();
						}
					}
					setVmAlarms(objVm);
				}
			}
			catch (Exception e){
				System.out.println("Exception in creating alarm : "+ e);			
			}
		}		
	}
	
	public void setVmAlarms(VirtualMachine objVm)
	{
		AlarmSpec objSpec = new AlarmSpec(); 
		StateAlarmExpression objAlarmExpression = alarmExpression();
		@SuppressWarnings("unused")
		AlarmAction methodAction = methodToTriggerAlarm(methodToPowerOn());
		objSpec.setExpression(objAlarmExpression);
		objSpec.setName("PoweredOff_" + objVm.getName());

		objSpec.setDescription("Monitor VM power off state");
		objSpec.setEnabled(true);  
		try {
			objAlarmManager.createAlarm(objVm, objSpec);

			System.out.println("*****New alarm set on VM : "+ objVm.getName()+"*****");
		} catch (InvalidName e) {
			e.printStackTrace();
		} catch (DuplicateName e) {
			e.printStackTrace();
		} catch (RuntimeFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	static StateAlarmExpression alarmExpression()
	{
		StateAlarmExpression objAlarmExpression = new StateAlarmExpression();
		objAlarmExpression.setType("VirtualMachine");
		objAlarmExpression.setStatePath("runtime.powerState");
		objAlarmExpression.setOperator(StateAlarmOperator.isEqual);
		objAlarmExpression.setRed("poweredOff");

		return objAlarmExpression;
	}
	
	static MethodAction methodToPowerOn() 
	{
		MethodAction objAction = new MethodAction();
		objAction.setName("PowerOnVM_Task");
		MethodActionArgument objArgument = new MethodActionArgument();
		objArgument.setValue(null);
		objAction.setArgument(new MethodActionArgument[] { objArgument });
		return objAction;
	}
	
	static AlarmTriggeringAction methodToTriggerAlarm(Action action) 
	{
		AlarmTriggeringAction actionOnAlarm = new AlarmTriggeringAction();
		actionOnAlarm.setYellow2red(true);
		actionOnAlarm.setAction(action);
		return actionOnAlarm;
	}
	
	public static boolean getStatusOfAlarms(VirtualMachine vm)
	{
		AlarmState [] objState = vm.getTriggeredAlarmState();

		if(objState != null){
			if(objState.length>0)
				return true;
			else return false;
		}
		return false;
	}
}
