import FogOSProxy.*;
import FogOSResource.Resource;

import com.jcraft.jsch.*;
import java.awt.*;
import java.io.InputStream;


public class FogProxyCloud {
	
    static Resource_CPU resource_cpu = new Resource_CPU("CPU","","percent",false);
    static Resource_MEM resource_mem = new Resource_MEM("MEMORY","","MB",false);
    static Resource_DISK resource_disk = new Resource_DISK("DISK","","GB",false);
    
    static boolean ischanged=false;
    
    private static Thread monitoringThread;
    
    public static void main(String[] args) {

        String name = "FogOSProxy";

        
        String priv_str = new String("private key"); //key for AWS
        byte[] priv = priv_str.getBytes();
        
        String host = new String("IP Address"); // AWS IP address
     
        String pub_str = new String("public_key"); //not used
        byte[] pub = pub_str.getBytes();
        
        FogProxy proxy = new FogProxy(name, priv, pub);

        monitoringThread = new Thread(new monitoring(priv_str, host));
        monitoringThread.start();
        

        //add CPU, Memory, Disk Resource
        
        proxy.addResource(resource_cpu);
        proxy.addResource(resource_mem);
        proxy.addResource(resource_disk);
      
        
        // TODO: Add content (later)
      

        // TODO: Add service (later)
   
      
    }
    
    
    static class Resource_CPU extends Resource {

    	Resource_CPU(String name, String max, String unit, boolean onDemand) {
    		super(name, max, unit, onDemand);
    		// TODO Auto-generated constructor stub
    	}

    	public void monitorResource() {
    		// TODO Auto-generated method stub
    		
    	}
    	
    }
    static class Resource_MEM extends Resource {

    	Resource_MEM(String name, String max, String unit, boolean onDemand) {
    		super(name, max, unit, onDemand);
    		// TODO Auto-generated constructor stub
    	}


    	public void monitorResource() {
    		// TODO Auto-generated method stub
    		
    	}
    	
    }
    static class Resource_DISK extends Resource {


    	Resource_DISK(String name, String max, String unit, boolean onDemand) {
    		super(name, max, unit, onDemand);
    		// TODO Auto-generated constructor stub
    	}

    	public void monitorResource() {
    		// TODO Auto-generated method stub
    		
    	}
    	
    }
    private static class monitoring implements Runnable{
    	
    	String privkey_str = null;
    	String host_ip = null;;
    	public monitoring(String privatekey,String host) {
    		privkey_str = new String(privatekey);
    		host_ip = new String(host);
    	}
    	public void run() {
    		// TODO Auto-generated method stub
    		while(true)
    		{
    		try{
    			  System.out.println("starting ssh...");
    	          JSch jsch=new JSch();
    	          jsch.addIdentity(privkey_str);
    	          JSch.setConfig("StrictHostKeyChecking", "no");
    	            	  
    	          String user="ubuntu";

    	          Session session=jsch.getSession(user, host_ip, 22);
    	          session.connect(30000); 	
    	          
    	          String command =  "free -m | awk 'NR==2{printf \"%s/%s\\n\", $3,$2 }'\n" +  //memory
    	          		"df -h | awk '$NF==\"/\"{printf \"%d/%d\\n\", $3,$2}'\n" + //disk
    	          		"top -bn1 | grep load | awk '{printf \"%.2f\\n\", $(NF-2)}'\n" + //CPU
    	          		"ps aux | grep nginx | grep master"; //service - ex. nginx
    	          //https://unix.stackexchange.com/questions/119126/command-to-display-memory-usage-disk-usage-and-cpu-load;
    	          
    	          ChannelExec channel = (ChannelExec)session.openChannel("exec");
    	          channel.setCommand(command);
    	          channel.setErrStream(System.err);
    	          channel.connect();
    	        

    	          InputStream input = channel.getInputStream();        
    	          
    	          byte[] tmp = new byte[1024];
    	          String result = null;
    	          
    	          while (true) {
    	            while (input.available() > 0) {
    	                int i = input.read(tmp, 0, 1024);
    	                if (i < 0) break;
    	                result=new String(tmp, 0, i);                                
    	            }
    	            if (channel.isClosed()){
    	            	System.out.println("exit-status: " + channel.getExitStatus());
    	                break;
    	            }       
    	            Thread.sleep(1000);
    	          }

    	          System.out.println("closing ssh...");
    	          channel.disconnect();
    	          session.disconnect();
    	          
    	          String[] resource_info = result.split("\\n");
    	          

    	          if(!resource_mem.getCurr().equals(resource_info[0]))
    	          {
    	        	  resource_mem = new Resource_MEM("MEMORY",resource_info[0],"MB",false);
    	        	  ischanged = true;
    	          }
    	          if(!resource_disk.getCurr().equals(resource_info[1]))
    	          {
    	        	  resource_disk = new Resource_DISK("DISK",resource_info[1],"GB",false);
    	        	  ischanged = true;
    	          }
    	          if(!resource_cpu.getCurr().equals(resource_info[2]))
    	          {
    	        	  resource_cpu = new Resource_CPU("CPU",resource_info[2],"%",false);
    	        	  ischanged = true;
    	          }
    	          
    	          if(ischanged) { //update only when there is any change

    			      System.out.println(resource_mem.getName()+" : " + resource_mem.getCurr() + resource_mem.getUnit());
    			      System.out.println(resource_disk.getName()+" : " + resource_disk.getCurr() + resource_disk.getUnit());
    			      System.out.println(resource_cpu.getName()+" : " + resource_cpu.getCurr() + resource_cpu.getUnit());
    			      ischanged=false;
    	    	  }
    	    	  
    		      System.out.println(resource_info[3]);
    		      
    	          Thread.sleep(3000);
    	          
	    		}
	            catch(Exception e){
	              System.out.println(e);
	            }
    		
    		}
    	}
    	
    }
    
}
