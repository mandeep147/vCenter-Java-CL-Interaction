package com.mandeep.VIConsole;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.*;

public class VIJavaConsoleInteract {

	public static void main(String[] args) throws InterruptedException {

		String IP, userID, password, operation = null, name = null, query = null;

		if (args.length != 3) {
			System.out.println("Usage: java ConsoleInteract <ip> <UserID> <Password>");
		}

		IP = args[0];
		userID = args[1];
		password = args[2];
		
		ServiceInstance si;
		
		try {
			si = new ServiceInstance(new URL("https://" + IP + "/sdk/"), userID, password, true);
			
			System.out.println("CMPE281 HW2 from Mandeep Kaur");

			while (true) {

				System.out.print("MandeepKaur-969> ");
				Scanner in = new Scanner(System.in);

				StringTokenizer st = new StringTokenizer(in.nextLine()," ");
				int numberOfTokens = st.countTokens();

				if(numberOfTokens == 0 || numberOfTokens > 3 || numberOfTokens == 2){
					operation = "";
				}

				else if( numberOfTokens == 1){
					operation = st.nextToken();
				}

				else{
					while(st.hasMoreTokens()){
						query = st.nextToken();
						name = st.nextToken();
						operation = st.nextToken();
					}
				}
				
				switch (operation) {
				case "exit":
					exitSystem(si);
					break;

				case "help":
					viewHelp();
					break;

				case "host":
					getAllHostName(si);
					break;

				case "info":
					if( query.equals("vm")){
						displayVMInfo(connectToVM(name, si), name);
					}
					else if( query.equals("host")){
						displayHostInfo(name, connectToHost(name, si));
					}
					break;

				case "datastore":
					displayDatastore(name, connectToHost(name, si));
					break;

				case "network":
					displayNetworkInfo(name , connectToHost(name, si));
					break;

				case "vm":
					getAllVMNames(si);
					break;

				case "on":
					
					powerOnVM(name, connectToVM(name, si), connectToHost(name, si));
					break;

				case "off":
				
					powerOffVM(name, connectToVM(name, si));
					break;

				case "shutdown":
				
					invokeShutdown(name, connectToVM(name, si)); 
					break;
				
				default: System.out.println("Invalid operation. Type help to know the syntax");
				break;

				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		}
	}		

	private static HostSystem connectToHost(String name, ServiceInstance si) throws InvalidProperty, RuntimeFault, RemoteException {
		Folder rootFolder = si.getRootFolder();
		HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", name);
		
		return host;
	}

	private static VirtualMachine connectToVM(String name, ServiceInstance si) throws InvalidProperty, RuntimeFault, RemoteException {
		Folder rootFolder = si.getRootFolder();
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine",name);
		if (vm == null) {
			System.out.println("No VM " + name + " is found");
			si.getServerConnection().logout();
		}
		vm.getResourcePool();
		return vm;
	}

	private static void exitSystem(ServiceInstance si) {
		System.out.println("Program is terminated.");
		si.getServerConnection().logout();
		System.exit(0);
	}

	private static void viewHelp() {
		System.out.println("Usage Instructions: \n");
		System.out.printf("%-35s%s", "exit", "Exit the program\n");
		System.out.printf("%-35s%s", "host", "Enumerate all hosts\n");
		System.out.printf("%-35s%s", "host hname info", "Show info of host name\n");
		System.out.printf("%-35s%s", "host hname datastore", "Enumerate datastores of host hname\n");
		System.out.printf("%-35s%s", "host hname network", "Enumerate datastores of host hname\n");
		System.out.printf("%-35s%s", "vm", "Enumerate all virtual machines\n");
		System.out.printf("%-35s%s", "vm vname info", "Show info of VM vname \n");
		System.out.printf("%-35s%s", "vm vname on", "Power on VM vname and wait until task completes\n");
		System.out.printf("%-35s%s", "vm vname off", "Power off VM vname and wait until task completes\n");
		System.out.printf("%-35s%s", "vm vname shutdown", "Shutdown guest of VM vname\n");
		System.out.println();
	}

	private static void getAllHostName(ServiceInstance si) {
		ManagedEntity[] managedEntities = null;
		
		try {
			managedEntities = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		HostSystem host = null;

		int i = 0;

		for (ManagedEntity managedEntity : managedEntities) {
			host = (HostSystem) managedEntity;

			if (host != null) {
				System.out.println("host[" + i + "] Name = " + host.getName());
				i++;
			}
		}
		System.out.println();
	}

	private static void displayVMInfo(VirtualMachine vm, String name) {
		
		try{
			System.out.println("Name = " + name+"\nGuest Full Name = "+vm.getGuest().getGuestFullName()+
				" \nGuest State = "+vm.getGuest().getGuestState()+"\nIP Address = "+vm.getGuest().getIpAddress()+
				"\nTool Running Status = "+vm.getGuest().getToolsRunningStatus()+
				"\nPower State = "+vm.getSummary().getRuntime().powerState);		
		System.out.println();
		}catch(Exception e){
			System.out.println("VM "+name+" not found");
		}
	}

	private static void displayHostInfo(String hostName, HostSystem host) {
		
		try{
			System.out.println("Host: \n\t\t"+
						"Name = " + hostName + "\n\t\tProductFullName = " + host.getConfig().getProduct().getFullName()
				+ "\n\t\tCPU cores = " + host.getHardware().getCpuInfo().getNumCpuCores() + "\n\t\tRAM = "
				+ host.getHardware().getMemorySize() / (1024 * 1024 * 1024)+" GB");
		
			System.out.println();
	
		}catch(Exception e){
			System.out.println("host "+hostName+" not found");		
		}
		
	}

	private static void displayDatastore(String hostName, HostSystem host) {
		
		HostDatastoreBrowser hdb;
		System.out.println("Host: ");
		try {
			hdb = host.getDatastoreBrowser();

			Datastore[] ds = hdb.getDatastores();
			System.out.println("\t\tName = " +hostName);

			for (int i = 0; i < ds.length; i++) {
		
				System.out.println("\t\tDatastore["+i+"]: Name =" + ds[i].getName() + 
						",capacity = " + (ds[i].getSummary().getCapacity() / (1024 * 1024 * 1024))+
						" GB ,FreeSpace = " + (ds[i].getSummary().getFreeSpace() / (1024 * 1024 * 1024)) + 
						" GB");
			}
		} catch (RemoteException e) {
			System.out.println("host "+hostName+" not found");
		}
		System.out.println();
	}

	private static void displayNetworkInfo(String hostName, HostSystem host) {
		
		System.out.println("Host: \n\t\tName = " +hostName);
		
		Network[] networkInfo = null;
		
		try {
			networkInfo = host.getNetworks();

			for(int i = 0; i < networkInfo.length; i++){
				System.out.println("\t\tNetwork[" +i+"]: name = " + networkInfo[i].getName());
			}
			
		} catch (RemoteException e) {
			System.out.println("host "+hostName+" not found");
		}
		System.out.println();
	}

	private static void getAllVMNames(ServiceInstance si) {
		
		ManagedEntity[] managedEntities = null;
		
		try {
		
			managedEntities = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("VirtualMachine");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		VirtualMachine virtualMachine = null;

		int i = 0;

		for (ManagedEntity managedEntity : managedEntities) {
			virtualMachine = (VirtualMachine) managedEntity;

			if (virtualMachine != null) {
				System.out.println("vm[" + i + "]: Name = " + virtualMachine.getName());
				i++;
			}
		}	
		System.out.println();
	}

	@SuppressWarnings("deprecation")
	private static void powerOnVM(String vmName, VirtualMachine vm, HostSystem host) throws InvalidProperty, RuntimeFault, RemoteException{
		Task task = null;
		try {
			task = vm.powerOnVM_Task(host);
			if (task.waitForMe() == Task.SUCCESS) {

				System.out.println("Name = " + vm.getName() + "\nPower On VM: Status= success, completion time = " + task.getTaskInfo().getCompleteTime().getTime());
			}	
		} catch (RemoteException e) {
			System.out.println("Name = " + vm.getName() + "\nPower On VM: Status = " + task.getTaskInfo().error.localizedMessage
					+ ", completion time = " + task.getTaskInfo().getCompleteTime().getTime());
		}	
		System.out.println();	
	}

	@SuppressWarnings("deprecation")
	private static void powerOffVM(String vmName, VirtualMachine vm) throws InvalidProperty, RuntimeFault, RemoteException {
		Task task = null;

		try {
			task = vm.powerOffVM_Task();

			if (task.waitForMe() == Task.SUCCESS) {
				System.out.println("Name = " + vm.getName() + "\nPower Off VM: Status = " + task.getTaskInfo().getState()
						+ ", completion time = " + task.getTaskInfo().getCompleteTime().getTime());
			}

		} catch (RemoteException e) {

			System.out.println("Name = " + vm.getName() + "\nPower Off VM: Status = " + task.getTaskInfo().error.localizedMessage
					+ ", completion time = " + task.getTaskInfo().getCompleteTime().getTime());
		}
		System.out.println();
	}

	private static void invokeShutdown(String vmName, VirtualMachine vm) throws InterruptedException, InvalidProperty, RuntimeFault, RemoteException {
		try {
			vm.shutdownGuest();
			
			long start = System.currentTimeMillis();
			long end = start + 180000;
			
			while(vm.getRuntime().getPowerState().equals(VirtualMachinePowerState.poweredOn ) && System.currentTimeMillis() <= end){
				Thread.sleep(2000);
			}
			
			if(vm.getRuntime().getPowerState().equals(VirtualMachinePowerState.poweredOff)){
				System.out.println("Name = " + vm.getName() + "\nShutdown Guest: completed, time = " +new Date());
			}
			
		} catch (RemoteException e) {
				System.out.println("Name = " + vm.getName() + "\nGraceful shutdown failed. Now try a hard power off.");
				
				hardPowerOffVM(vm);		
		}
		System.out.println();
	}

	@SuppressWarnings("deprecation")
	private static void hardPowerOffVM(VirtualMachine vm) throws InvalidProperty, RuntimeFault, RemoteException {
		Task task = null;

		try {
			task = vm.powerOffVM_Task();

			if (task.waitForMe() == Task.SUCCESS) {
				System.out
				.println("\nPower Off VM: Status = " + task.getTaskInfo().getState()
						+ ", completion time = " + task.getTaskInfo().getCompleteTime().getTime());	
			}
		} catch (RemoteException e) {

			System.out.println("\nPower Off VM: Status = " + task.getTaskInfo().error.localizedMessage
					+ ", completion time = " + task.getTaskInfo().getCompleteTime().getTime());
		}
		System.out.println();
	}
}