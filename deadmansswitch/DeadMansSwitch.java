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
    
    public static int restartThreshold = 30;
    public static int resetSwitchRate = 5;
    public static String classNameMain = "cpw.mods.fml.relauncher.ServerLaunchWrapper";
    public static boolean forceACrashTest = false;
    
    //shared thread access variables
    public static long lastResetTime = -1;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	preInitConfig = new Configuration(event.getSuggestedConfigurationFile());
    	try {
	    	classNameMain = preInitConfig.get("default", "classNameMain", classNameMain).getString();
	    	int secondsToRestart = preInitConfig.get("default", "secondsToRestart", Integer.valueOf(restartThreshold)).getInt();
	    	restartThreshold = secondsToRestart;
	    	int secondsToResetSwitch = preInitConfig.get("default", "secondsToResetSwitch", Integer.valueOf(resetSwitchRate)).getInt();
	    	resetSwitchRate = secondsToResetSwitch;
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
            	
            	Thread.sleep(restartThreshold * 1000);
            }
        } catch(Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    public static boolean checkForDeadMansSwitchReset() {
    	long timeTillRestart = (System.currentTimeMillis()/1000) - lastResetTime;
    	//System.out.println("timeTillRestart: " + (timeTillRestart - restartThreshold));
    	if (lastResetTime != -1) {
    		if (timeTillRestart > restartThreshold) {
	    		//System.out.println("we should restart!");
	    		return true;
    		}
    	}
    	return false;
    }
    
    public static void resetDeadMansSwitch(long timeVal) {
    	lastResetTime = timeVal;
    	//System.out.println("reset switch: " + timeVal);
    }

}
