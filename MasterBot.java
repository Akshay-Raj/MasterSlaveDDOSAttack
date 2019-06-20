

//AKSHAY RAJ BASAVANAGUDI RAJENDRA



import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class MasterBot {

	static int nPortM;
	private static final int DEFAULT_PORT_MASTER = 1234;
	private static Thread t;
	public static MasterBot masterBot;

	
	ArrayList<ConnectedSlave> slaves = null;
	
	public MasterBot()
	{
		slaves = new ArrayList<ConnectedSlave>();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		masterBot = new MasterBot();
		System.out.print("input is: " + args[0] + " value " + args[1] + "\n");
		if(args[0].compareTo("-p") == 0)
		{
			nPortM = Integer.parseInt(args[1]);
		}
		else
		{
			nPortM = DEFAULT_PORT_MASTER;
		}
		
		t = new Thread(new MasterCmdIface());
		t.start();
		
		
		/** Create a ServerSocket that will listen on server port and accept incoming
		 * slave connections */
		ServerSocket serverSo = null;
		try
		{
			serverSo = new ServerSocket(nPortM);
			
		}catch(Exception e){System.out.println("Exception is " + e);};
		// in a loop :
		//a) listen on a new socket (at port nPortM and localhost)
		//b) accept on the socket
		//c) When a new slave connect, accept() return the connected client socket and info
		//d) Save this info as an object of ConnectedSlave.java
		boolean continueToAcceptSlaves = true;
		do
		{
			try
			{
				Socket clientConnSo = serverSo.accept();
				System.out.println("a new slave with IP:" + clientConnSo.getInetAddress().getHostAddress() + " port:" + clientConnSo.getPort());
				ConnectedSlave connSlaveInfo = new ConnectedSlave(clientConnSo);
				masterBot.slaves.add(connSlaveInfo);
			}catch(Exception e){System.out.println("Exception is " + e);}
		}while(continueToAcceptSlaves);
			
	}

}




class MasterCmdIface implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// here we will read from command line and get all the user input commands viz:
		// a) list
		// b) connect
		// c) disconnect
		
		Console c = System.console();
		 if (c == null) {
	            System.err.println("No console.");
	            System.exit(1);
	        }
		 /**
		  * input command:
		  * 1.-list
		  * Will list all current slaves with the following format:
          * SlaveHostName IPAddress SourcePortNumber RegistrationDate
          * USE THE FOLLOWING FORMAT FOR THE DATE: YYYY-MM-DD
            Where YYYY is a four digit year, MM is a two digit month, and DD is a two digit day.
          * 2.-connect (IPAddressOrHostNameOfYourSlave|all) (TargetHostName|IPAddress) TargetPortNumber
               [NumberOfConnections: 1 if not specified]
               Establish a number of connections to the target host.
          * 3.-disconnect (IPAddressOrHostNameOfYourSlave|all) (TargetHostName|IPAddress) [TargetPort:all if
               no port specified]
		  */	
		 String command = null;
		 while(true)
		 {
			 command = c.readLine();
			 
			 //System.out.println(command);
			 // list, connect, disconnect
			 if(command.startsWith("list") == true)
			 {
				 //print the list of slaves
				 /** just print every entry in MasterBot.slaves */
				 Iterator<ConnectedSlave> it = MasterBot.masterBot.slaves.iterator();
				 System.out.println("The list of connected clients:");
				 while(it.hasNext())
				 {
					 ConnectedSlave slaveInfo = it.next();
					 slaveInfo.print();
				 }
			 }
			 else if(command.startsWith("connect")
					 || command.startsWith("disconnect") || command.startsWith("ipscan") || command.startsWith("tcpportscan") || command.startsWith("geoipscan"))
			 {
				 String[] tokens = command.split("\\s+");
				 /** -connect (IPAddressOrHostNameOfYourSlave|all) (TargetHostName|IPAddress) 
				  * TargetPortNumber[2] numberOfConnections: 1 if not specified] */
				 //we have SlaveNameOrIP in token[1];
				 /*
				  * 1) check if token[1] is all */
				boolean status = false;
				Iterator<ConnectedSlave> it = MasterBot.masterBot.slaves.iterator();
				while(it.hasNext())
				{
					ConnectedSlave slaveTmp = it.next();
					if(tokens[1].equals("all")
							|| slaveTmp.clientSo.getInetAddress().getHostName().compareTo(tokens[1]) == 0
							|| slaveTmp.clientSo.getInetAddress().getHostAddress().compareTo(tokens[1]) == 0)
					{
						try {
							 PrintWriter pw = new PrintWriter(slaveTmp.clientSo.getOutputStream());
							 pw.println(command);
							 pw.flush();
							 if(command.startsWith("ipscan")|| command.startsWith("geoipscan"))
							 {
								 new Thread(new ipscan(slaveTmp.clientSo)).start();
							 }
							 if(command.startsWith("tcpportscan"))
							 {
								 new Thread(new tcpportscan(slaveTmp.clientSo)).start();
							 }
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						status = true;
					}
				}
				if(status == false)
				{
						 System.out.println("Sorry, the slave details does not match any entry");
				}
			 }
			 else
			 {
				 System.out.println("Invalid command; retry");
			 }
		 }
	}

}
class ConnectedSlave {
	Socket clientSo; //client's host-name, IP, port-number
	Date regDate;
	DateFormat df;
	
	public ConnectedSlave(Socket aClientSo)
	{
		clientSo = aClientSo;
		regDate = Calendar.getInstance().getTime();
		df = new SimpleDateFormat("yyyy/MM/dd");
	}
	
	public void print()
	{
		/**
		 * SlaveHostName IPAddress SourcePortNumber RegistrationDate
		 * USE THE FOLLOWING FORMAT FOR THE DATE: YYYY-MM-DD
		 */
		System.out.println("" + clientSo.getInetAddress().getHostName() + " " + clientSo.getInetAddress().getHostAddress()
				+ " " + clientSo.getPort() + " " + df.format(regDate));
	}
}
class ipscan extends Thread
{
	private Socket ipskt;
	public ipscan(Socket ipskt)
	{
		this.ipskt = ipskt;
	}
	public void run()
	{
		try
		{
			
            BufferedReader br = new BufferedReader(new InputStreamReader(ipskt.getInputStream()));
            while(br!=null)
            {
            //System.out.println("hello");
            String str = br.readLine();
			System.out.println(str);
            }
		}catch(Exception e) {}
	}
}
class tcpportscan extends Thread
{
	private Socket tcpskt;
	public tcpportscan(Socket tcpskt)
	{
		this.tcpskt = tcpskt;
	}
	public void run()
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(tcpskt.getInputStream()));
			String str = br.readLine();
			System.out.println(str);
		}catch(Exception e) {}
	}
}