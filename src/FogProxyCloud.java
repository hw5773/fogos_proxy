import FlexID.InterfaceType;
import FlexID.Locator;
import FogOSClient.FogOSClient;
import FogOSContent.Content;
import FogOSResource.Resource;
import FogOSResource.ResourceType;
import FogOSService.Service;
import FogOSService.ServiceContext;
import FogOSService.ServiceType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class FogProxyCloud {
	private static FogOSClient fogos;
	
	private static String key = "key";
	private static String host = "ip address";
	
	private static final String rootPath = "D:\\tmp";
    
    public static void main(String[] args) throws NoSuchAlgorithmException {
    	// 1. Initialize the FogOSClient instance.
		// This will automatically build the contentStore inside the core,
		// a list of services, and a list of resources
        fogos = new FogOSClient(rootPath);

        // 2. Add manually resource, content, or service
		// 2-1. Add resources to be monitored
		Resource resource_cpu = new Resource("CPU", ResourceType.CPU, "","percent",false) {
			@Override
			public void monitorResource() {
				this.setMax("1");
				String command =  "top -bn1 | grep load | awk '{printf \"%.2f\\n\", $(NF-2)}'\n";
				this.setCurr(monitoring(key,host,command)); 
			}
		};

		Resource resource_mem = new Resource("MEMORY", ResourceType.Memory, "","KB",false) {
			@Override
			public void monitorResource() {
				String command =  "free | grep Mem |awk '{print $2}'";
				this.setMax(monitoring(key,host,command)); 	
				command =  "free | grep Mem |awk '{print $3}'";
				this.setCurr(monitoring(key,host,command)); 

			}
		};

		Resource resource_disk = new Resource("DISK", ResourceType.Disk, "","KB",false) {
			@Override
			public void monitorResource() {
				String command =  "df -P | grep -v ^Filesystem | awk '{sum += $2} END { print sum }'";
				this.setMax(monitoring(key,host,command)); 
				command =  "df -P | grep -v ^Filesystem | awk '{sum += $3} END { print sum }'";
				this.setCurr(monitoring(key,host,command)); 

			}
		};

		
		fogos.addResource(resource_cpu);
		fogos.addResource(resource_disk);
		fogos.addResource(resource_mem);

		// 2-2. Add content manually if any
		Content test_content = new Content("test_content", "D:\tmp\test.jpg", true);
		fogos.addContent(test_content);

		// 2-3. Add service to run
		KeyPairGenerator serviceKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
		serviceKeyPairGenerator.initialize(2048);
		KeyPair serviceKeyPair = serviceKeyPairGenerator.genKeyPair();
		Locator serviceLoc = new Locator(InterfaceType.WIFI, "127.0.0.1", 5550);
		Locator proxyLoc = new Locator(InterfaceType.ETH, "127.0.0.1", 5551);
		ServiceContext testServiceCtx = new ServiceContext("FogClientTestService",
				ServiceType.Streaming, serviceKeyPair, serviceLoc, true, proxyLoc);
		Service testService = new Service(testServiceCtx) {
			@Override
			public void initService() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, InterruptedException {
				super.initService();
			}

			@Override
			public void processInputFromProxy() {
				super.processInputFromProxy();
			}

			@Override
			public void processOutputToProxy() {
				super.processOutputToProxy();
			}

			@Override
			public void processInputFromPeer() {

			}

			@Override
			public void processOutputToPeer() {

			}
		};
		fogos.addService(testService);
        
        // 3. begin the FogOS interaction
		fogos.begin();
		System.out.println("[FogProxyCloud] FogOS Core begins.");

//		 4. finalize the FogOS interaction
		fogos.exit();
		System.out.println("[FogProxyCloud] FogOS Core quits.");
		System.exit(0);
    }

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
}
