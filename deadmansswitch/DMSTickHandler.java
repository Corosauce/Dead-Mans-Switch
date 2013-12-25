package deadmansswitch;

import java.util.EnumSet;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class DMSTickHandler implements ITickHandler
{

	public long lastCheckTime = 0;
	
	@Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    	
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.SERVER)))
        {
        	onTickInGame();
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.SERVER);
    }

    @Override
    public String getLabel() { return null; }
    

    public void onTickInGame()
    {
    	World world = DimensionManager.getWorld(0);
    	if (world != null) {
    		if (lastCheckTime + DeadMansSwitch.resetSwitchRate < System.currentTimeMillis() / 1000) {
    			DeadMansSwitch.resetDeadMansSwitch(System.currentTimeMillis() / 1000);
    			lastCheckTime = System.currentTimeMillis() / 1000;
    			
    			if (DeadMansSwitch.forceACrashTest) {
    				System.out.println("--- DEAD MANS SWITCH forceACrashTest is set to true, FORCING A CRASH TO TEST AUTO SERVER RESTART ---");
    				World worldCrash = null;
    				if (true) worldCrash.getWorldTime();
    			}
    		}
    	}
    }
}
