package pkgProjectOne;

public class AvailabilityManager {

	public static void main(String[] args) {
		// Initialize connections
		new ProjectUtility();
		
		VmAlarmManager objAlarmManager = new VmAlarmManager();
		
		// Set alarms on VM
		objAlarmManager.setAlarmOnAllVms();
		
		// Display VM details
		VmUsageMonitor.displayVmDetails();
		
		try {
			new Thread(new SnapshotManager()).start();
			Thread.sleep(20000);
			new Thread(new RecoveryManager()).start();
		}
		catch (Exception e){
			System.out.println("Error while creating threads : "+e);
		}
	}

}
