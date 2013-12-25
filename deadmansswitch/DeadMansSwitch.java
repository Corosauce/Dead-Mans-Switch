package deadmansswitch;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@NetworkMod(clientSideRequired = false, serverSideRequired = false)
@Mod(modid = "deadmansswitch", name="Dead Mans Switch", version="v1.0")
public class DeadMansSwitch
    implements Runnable {
	
	@Mod.Instance( value = "deadmansswitch" )
	public static DeadMansSwitch instance;
	public static String modID = "deadmansswitch";

    /** For use in preInit ONLY */
    public Configuration preInitConfig;
    
    public static int restartThreshold = 30000;
    public static int resetSwitchRate = 5000;
    public static String classNameMain = "cpw.mods.fml.relauncher.ServerLaunchWrapper";
    public static boolean forceACrashTest = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	preInitConfig = new Configuration(event.getSuggestedConfigurationFile());
    	try {
	    	classNameMain = preInitConfig.get("default", "classNameMain", classNameMain).getString();
	    	int secondsToRestart = preInitConfig.get("default", "secondsToRestart", Integer.valueOf(restartThreshold / 1000)).getInt();
	    	restartThreshold = secondsToRestart * 1000;
	    	int secondsToResetSwitch = preInitConfig.get("default", "secondsToResetSwitch", Integer.valueOf(resetSwitchRate / 1000)).getInt();
	    	resetSwitchRate = secondsToResetSwitch * 1000;
	    	forceACrashTest = preInitConfig.get("default", "forceACrashTest", false).getBoolean(false);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	preInitConfig.save();
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	TickRegistry.registerTickHandler(new DMSTickHandler(), Side.SERVER);
    	resetDeadMansSwitch(-1);
    	(new Thread(this)).start();
    }
    
    public static void restartApplication()
    {
    	try {
    		
    		StringBuilder cmd = new StringBuilder();
            cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
            for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                cmd.append(jvmArg + " ");
            }
            cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
            cmd.append(classNameMain).append(" ");
            String[] args = new String[0];
            for (String arg : args) {
                cmd.append(arg).append(" ");
            }
            Runtime.getRuntime().exec(cmd.toString());
            System.exit(0);
    	} catch (Exception ex) {
    		System.out.println("Error executing server auto restart code");
    		ex.printStackTrace();
    	}
    }
    
    //public HashMap<World, Boolean> worldsAddedTo;

    public void run() {
        try {
            while(true) {
            	
            	if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
	            	if (checkForDeadMansSwitchReset()) {
	            		//restart game!
	            		restartApplication();
	            	}
            	}
            	
            	Thread.sleep(restartThreshold);
            }
        } catch(Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    public static boolean checkForDeadMansSwitchReset() {
    	try {
	    	RandomAccessFile file = new RandomAccessFile("." + File.separator + "DeadMansSwitch.txt", "r");
	    	FileChannel fc = file.getChannel();
	    	ByteBuffer bb = ByteBuffer.allocate(8);
	    	int bytesRead = fc.read(bb);
	    	//bb.rewind();
	    	bb.flip();
	    	long timeMilliseconds = bb.getLong();
	    	//bb.get();
	    	fc.close();
	    	file.close();
	    	//System.out.println("read timeMilliseconds: " + timeMilliseconds);
	    	long timeTillRestart = System.currentTimeMillis() - timeMilliseconds;
	    	if (timeMilliseconds != -1 && timeTillRestart > restartThreshold) {
	    		//System.out.println("we should restart!");
	    		return true;
	    	}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		
    	}
    	return false;
    }
    
    public static void resetDeadMansSwitch(long timeVal) {
    	FileChannel fc = null;
    	try {
	    	RandomAccessFile file = new RandomAccessFile("." + File.separator + "DeadMansSwitch.txt", "rw");
	    	fc = file.getChannel();
	    	ByteBuffer bb = ByteBuffer.allocate(8);
	    	bb.clear();
	    	bb.putLong(timeVal);
	    	bb.flip();
	    	while(bb.hasRemaining()) {
	    		fc.write(bb);
	    	}
	    	fc.close();
	    	file.close();
	    	//System.out.println("write timeMilliseconds: " + timeVal);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		
    	}
    }

}
