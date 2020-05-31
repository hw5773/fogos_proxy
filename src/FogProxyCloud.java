import FogOSProxy.*;

public class FogProxyCloud {
    public static void main(String[] args) {
        // TODO: Determine a name (by an argument?)
        String name;

        // TODO: Create a keypair: priv: a private key, pub: a public key
        byte[] priv;
        byte[] pub;

        FogProxy proxy = new FogProxy(name, priv, pub);

        // TODO: Add resources: "cpu", "memory", "network", etc.
        // Please define classes regarding resources and add them to the proxy.

        // TODO: Add content (later)

        // TODO: Add service (later)
    }
}
