import FogOSClient.FogOSClient;
import FogOSContent.Content;
import FogOSProxy.*;
import FogOSResource.Resource;

import FogOSService.Service;
import com.jcraft.jsch.*;
import java.awt.*;
import java.io.InputStream;


public class FogProxyCloud {
	private static FogOSClient fogos;

	private static String key = "AWS key";
	private static String host = "AWS IP";

	private static final rootPath = "D:\tmp";
	
    public static void main(String[] args) {
    	// 1. Initialize the FogOSClient instance.
		// This will automatically build the contentStore inside the core,
		// a list of services, and a list of resources
    	
        fogos = new FogOSClient(rootPath);

        
        // 2. Add manually resource, content, or service
		// 2-1. Add resources to be monitored
		Resource_CPU resource_cpu = new Resource_CPU("CPU","","percent",false);
		Resource_MEM resource_mem = new Resource_MEM("MEMORY","","KB",false);
		Resource_DISK resource_disk = new Resource_DISK("DISK","","KB",false);

		Thread t_CPU = new Thread(resource_cpu);
		Thread t_MEM = new Thread(resource_mem);
		Thread t_DISK = new Thread(resource_disk);
		
		t_CPU.start();
		t_MEM.start();
		t_DISK.start();
		
		while(true)
		{
			System.out.println(resource_cpu.getCurr());
			System.out.println(resource_mem.getCurr());
			System.out.println(resource_disk.getCurr());
			try {
			Thread.sleep(1000);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		fogos.addResource(resource_cpu);
		fogos.addResource(resource_disk);
		fogos.addResource(resource_mem);

		// 2-2. Add content manually if any
		Content test_content = new Content("test_content", "D:\tmp\test.jpg", true);
		fogos.addContent(test_content);

		// 2-3. Add service to run
		Service test_service = new Service("test_service", true);
		fogos.addService(test_service);
        
        // 3. begin the FogOS interaction
		fogos.begin();

		// 4. TODO: (hwlee) finalize the FogOS interaction
		// fogos.exit();
    }

	static class Resource_CPU extends Resource implements Runnable {

		Resource_CPU(String name, String max, String unit, boolean onDemand) {
			super(name, max, unit, onDemand);
			this.setMax("100");
			
			
			
			// TODO Auto-generated constructor stub
		}
		public void run() {
			while(true) {
			String command =  "top -bn1 | grep load | awk '{printf \"%.2f\\n\", $(NF-2)}'\n";
			this.setCurr(monitoring(key,host,command)); 

			}
		}

    	public void monitorResource() {
    		

    	}


    }

    static class Resource_MEM extends Resource implements Runnable {

    	Resource_MEM(String name, String max, String unit, boolean onDemand) {
    		super(name, max, unit, onDemand);

			String command =  "free | grep Mem |awk '{print $2}'";
			this.setMax(monitoring(key,host,command)); 
    		
    		// TODO Auto-generated constructor stub
    	}

		public void run() {
			while(true) {

			String command =  "free | grep Mem |awk '{print $3}'";
			this.setCurr(monitoring(key,host,command)); 

			}
		}

    	public void monitorResource() {
    		// TODO Auto-generated method stub

    	}

    }

    static class Resource_DISK extends Resource implements Runnable {

    	Resource_DISK(String name, String max, String unit, boolean onDemand) {
    		super(name, max, unit, onDemand);

			String command =  "df -P | grep -v ^Filesystem | awk '{sum += $2} END { print sum }'";
			this.setMax(monitoring(key,host,command)); 

    		// TODO Auto-generated constructor stub
    	}

		public void run() {
			while(true) {

			String command =  "df -P | grep -v ^Filesystem | awk '{sum += $3} END { print sum }'";
			this.setCurr(monitoring(key,host,command)); 

			}
		}
    	public void monitorResource() {
    		// TODO Auto-generated method stub

    	}
    }

    // TODO: (syseok) Need to implement monitoring stuffs inside the classes (monitorResource)
    private static String monitoring(String privkey_str, String host_ip, String command) {

        String result = null;
        
		try {
        JSch jsch=new JSch();
        jsch.addIdentity(privkey_str);
        JSch.setConfig("StrictHostKeyChecking", "no");
          	  
        String user="ubuntu";

        Session session=jsch.getSession(user, host_ip, 22);
        session.connect(30000); 
        
        ChannelExec channel = (ChannelExec)session.openChannel("exec");
        channel.setCommand(command);
        channel.setErrStream(System.err);
        channel.connect();
      

        InputStream input = channel.getInputStream();        
        
        byte[] tmp = new byte[1024];

        
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

        channel.disconnect();
        session.disconnect();
        
		} catch (Exception e)
		{
			System.out.println(e);
		}

        String[] resource_info = result.split("\\n");
    	     
		return resource_info[0];
    	
    }
//    
    
}
