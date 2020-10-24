import FlexID.InterfaceType;
import FlexID.Locator;
import FogOSClient.FogOSClient;
import FogOSContent.Content;
import FogOSResource.Resource;
import FogOSResource.ResourceType;
import FogOSSecurity.SecureFlexIDSession;
import FogOSService.Service;
import FogOSService.ServiceContext;
import FogOSService.ServiceType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class FogProxyCloud {
	private static FogOSClient fogos;
	
	private static final String key = "key";
	private static final String host = "ip address";
	private static final int BUFFER_SIZE = 8192;

	private static final String rootPath = "D:\\tmp";
	//private static final String rootPath = "C:\\Users\\HMLEE\\FogOS";
    
    public static void main(String[] args) throws NoSuchAlgorithmException, InterruptedException, IOException {
    	// 1. Initialize the FogOSClient instance.
		// This will automatically build the contentStore inside the core,
		// a list of services, and a list of resources
        fogos = new FogOSClient(rootPath);

        // 2. Add manually resource, content, or service
		// 2-1. Add resources to be monitored
		/*
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
		*/

		// 2-2. Add content manually if any
		Content test_content = new Content("test_content", "D:\\tmp\\test.png", true);
		//Content test_content = new Content("test_content", "C:\\Users\\HMLEE\\FogOS\\test.txt", true);
		fogos.addContent(test_content);


		// 2-3. Add service to run
		KeyPairGenerator proxyKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
		proxyKeyPairGenerator.initialize(2048);
		KeyPair proxyKeyPair = proxyKeyPairGenerator.genKeyPair();
		Locator proxyLoc = new Locator(InterfaceType.WIFI, "127.0.0.1", 5551);
		Locator serverLoc = new Locator(InterfaceType.ETH, "52.78.23.173", 80);
		ServiceContext testServiceCtx1 = new ServiceContext("FogClientTestService",
				ServiceType.Streaming, proxyKeyPair, proxyLoc, true, serverLoc);
		Service testService1 = new Service(testServiceCtx1) {
			@Override
			public void initService() throws Exception {
				super.initService();
			}

			@Override
			public void processInputFromServer() {
				System.out.println("[Service] Start: processInputFromServer()");
				// Fetch data from the buffer
				byte[] buf = new byte[16384];
				int len = this.getInputFromServer(buf);

				System.out.println("[Service] Received in processInputFromServer()");
				System.out.print("First 5 bytes: " + buf[0] + " " + buf[1] + " " + buf[2] + " " + buf[3] + " " + buf[4]);
				System.out.println();

				System.out.print("Last 5 bytes: " + buf[len-5] + " " + buf[len-4] + " " + buf[len-3] + " " + buf[len-2] + " " + buf[len-1]);
				System.out.println();

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				this.putOutputToPeer(buf, len);
				System.out.println("[Service] Finish: processInputFromServer()");
			}

			@Override
			public void processOutputToServer() {
				System.out.println("[Service] Start: processOutputToServer()");
				// Fetch data from the buffer
				byte[] buf = new byte[16384];
				int len = this.getOutputToServer(buf);

				// Send data to Server
				try {
					this.getServerSession().send(buf, len);
				} catch (IOException e) {
					e.printStackTrace();
				}

				System.out.println("[Service] Written in processOutputToServer()");
				System.out.print("First 5 bytes: " + buf[0] + " " + buf[1] + " " + buf[2] + " " + buf[3] + " " + buf[4]);
				System.out.println();

				System.out.print("Last 5 bytes: " + buf[len-5] + " " + buf[len-4] + " " + buf[len-3] + " " + buf[len-2] + " " + buf[len-1]);
				System.out.println();

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println("[Service] Finish: processInputFromServer()");
			}

			@Override
			public void processInputFromPeer() {
				System.out.println("[Service] Start: processInputFromPeer()");
				byte[] buf = new byte[16384];
				int len = this.getInputFromPeer(buf);
				//System.out.println("[Service] Received in processInputFromPeer(): " + new String(buf));

				System.out.println("[Service] Received in processInputFromPeer()");
				System.out.print("First 5 bytes: " + buf[0] + " " + buf[1] + " " + buf[2] + " " + buf[3] + " " + buf[4]);
				System.out.println();

				System.out.print("Last 5 bytes: " + buf[len-5] + " " + buf[len-4] + " " + buf[len-3] + " " + buf[len-2] + " " + buf[len-1]);
				System.out.println();

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (this.getContext().isProxy())
					this.putOutputToServer(buf, len);
				System.out.println("[Service] Finish: processInputFromPeer()");
			}

			@Override
			public void processOutputToPeer() {
				System.out.println("[Service] Start: processOutputToPeer()");
				// Fetch Data from the Buffer
				byte[] buf = new byte[16384];
				int len = this.getOutputToPeer(buf);
				// Send Data to Peer
				SecureFlexIDSession secureFlexIDSession = this.getPeerSession();
				secureFlexIDSession.send(buf, len);

				System.out.println("[Service] Written in processOutputToPeer()");
				System.out.print("First 5 bytes: " + buf[0] + " " + buf[1] + " " + buf[2] + " " + buf[3] + " " + buf[4]);
				System.out.println();

				System.out.print("Last 5 bytes: " + buf[len-5] + " " + buf[len-4] + " " + buf[len-3] + " " + buf[len-2] + " " + buf[len-1]);
				System.out.println();

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("[Service] Finish: processOutputToPeer()");
			}
		};
		fogos.addService(testService1);

        
        // 3. begin the FogOS interaction
		fogos.begin();
		System.out.println("[FogProxyCloud] FogOS Core begins.");

		// Explicitly register content
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("title", "TestContent");
		attributes.put("desc", "Test");
		fogos.registerContent(test_content, attributes);
		//fogos.registerContent("test.jpg", "D:\\tmp\\test.jpg");

		// Explicitly register service

		fogos.registerService(testService1);
//		 4. finalize the FogOS interaction
		fogos.exit();
		System.out.println("[FogProxyCloud] FogOS Core quits.");
		System.exit(0);
    }

    private static String monitoring(String privkey_str, String host_ip, String command) {

        String result = null;
		String[] resource_info = null;
        
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

		if (result != null)
        	resource_info = result.split("\\n");
    	     
		return resource_info[0];
    	
    }
}
