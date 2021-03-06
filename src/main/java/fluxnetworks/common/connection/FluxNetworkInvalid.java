package fluxnetworks.common.connection;

import fluxnetworks.api.network.EnumSecurityType;
import fluxnetworks.api.utils.EnergyType;

import java.util.UUID;

public class FluxNetworkInvalid extends FluxNetworkBase {

    public static final FluxNetworkInvalid instance = new FluxNetworkInvalid();

    private FluxNetworkInvalid() {
        super(-1, "Please select a network", EnumSecurityType.PUBLIC, 0xb6b6b6, new UUID(-1 ,-1), EnergyType.RF, "");
    }

    @Override
    public boolean isInvalid() {
        return true;
    }
}
