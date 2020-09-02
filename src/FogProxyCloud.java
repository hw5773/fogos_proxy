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
	private static final String rootPath = "D:\tmp";
    
    public static void main(String[] args) {
    	// 1. Initialize the FogOSClient instance.
		// This will automatically build the contentStore inside the core,
		// a list of services, and a list of resources
        fogos = new FogOSClient(rootPath);

        // 2. Add manually resource, content, or service
		// 2-1. Add resources to be monitored
		Resource resource_cpu = new Resource("CPU","","percent",false) {
			@Override
			public void monitorResource() {

			}
		};

		Resource resource_mem = new Resource("MEMORY","","MB",false) {
			@Override
			public void monitorResource() {

			}
		};

		Resource resource_disk = new Resource("DISK","","GB",false) {
			@Override
			public void monitorResource() {

			}
		};

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

		// 4. finalize the FogOS interaction
		fogos.exit();
    }

    /*
    // TODO: (syseok) Need to implement monitoring stuffs inside the classes (monitorResource)
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
    */
}
