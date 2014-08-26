/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package freenet.pluginmanager;

import freenet.node.fcp.FCPPluginClient;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * FCP server or client plugins which transfer FCP messages to each other using a {@link FCPPluginClient} must implement this interface to provide a function
 * which handles the received messages.
 * 
 * For symmetry, the child interfaces {@link ClientSideMessageHandler} and {@link ServerSideMessageHandler} do not provide any different functions.
 * They exist nevertheless to allow JavaDoc to explain differences in what the server and client are allowed to do.
 * You <b>must</b> follow the restrictions which are explained there.
 * 
 * FIXME: This shall replace the interfaces {@link FredPluginFCPServer} and {@link FredPluginFCPClient}. Instead of having two different message handling
 * function signatures for client and server as it is in the existing interfaces, this should have one message handling function which is the same.
 * This will keep the {@link FCPPluginClient} send() functions simple. So please design a common message handling function and then delete the old interfaces.
 * 
 * @author xor (xor@freenetproject.org)
 * @see PluginRespirator#connecToOtherPlugin(String, FredPluginFCPClient) PluginRespirator provides the function to obtain FCP connections to a server plugin.
 * @see FCPPluginClient A client will be represented as class FCPPluginClient to the client and server plugin. It's Java provides an overview of the internal
 *                      code paths through which plugin FCP messages flow.
 */
public interface FredPluginFCPMessageHandler { 
   
    /**
     * Plugins which provide FCP services to clients must implement this interface.<br/>
     * The purpose of this interface is to provide a message handling function for processing messages received from the clients.
     * 
     * FIXME: Migrate JavaDoc of {@link FredPluginFCPServer} to this, then delete that interface.
     * 
     * @see FredPluginFCPMessageHandler The parent interface FredPluginFCPMessageHandler provides an overview.
     * @see ClientSideFCPMessageHandler The interface of the client plugin which handles the messages sent by the server.
     */
    public interface ServerSideFCPMessageHandler extends FredPluginFCPMessageHandler {
        public static enum ClientPermissions {
            /** The client plugin is running within the same node as the server plugin.
             * TODO FIXME: Is there any reason not to assume {@link #ACCESS_FCP_FULL}? */
            ACCESS_DIRECT,
            ACCESS_FCP_RESTRICTED,
            ACCESS_FCP_FULL
        };
        
        /**
         * <p>Is called to handle messages from your clients.<br/>
         * <b>Must not</b> block for very long and thus must only do small amounts of processing.<br/>
         * You <b>must not</b> keep a hard reference to the passed {@link FCPPluginClient} object outside of the scope of this function:
         * This would prevent client plugins from being unloaded.</p>
         * 
         * <p>If you ...<br/>
         * - Need a long time to compute a reply.<br/>
         * - Want send messages to the client after having exited this function; maybe even triggered by events at your plugin, not by client messages.<br/>
         * Then you should:<br/>
         * - Obtain the ID of the client via {@link FCPPluginClient#getID()}, store it, and exit this message handling function.<br/>
         * - Compute your reply in another thread.</br>
         * - Once you're ready to send the reply, use {@link PluginRespirator#getPluginClientByID(java.util.UUID)} to obtain the client.<br/>
         * </p>
         *  
         * @param client The client which sent the message. To be used for sending back a reply.<br/>
         *               You <b>must not</b> keep a hard reference to this object outside of the scope of this function: This would prevent client plugins from
         *               being unloaded. See the head of the documentation of this function for an explanation of how to store a pointer to a certain client.
         * @param permissions Permissions of the client.
         *                    FIXME: Maybe move this to {@link FCPPluginClient}. If it is moved, still mention it in the JavaDoc here.
         * @param messageIdentifier The identifier of the client message as specified by the client. Must be passed through when sending a reply.
         * @param parameters Part 1 of client message: Human-readable parameters. Shall be small amount of data.
         * @param data Part 2 of client message: Non-human readable, large size bulk data. Can be null.
         */
        void handleFCPPluginClientMessage(FCPPluginClient client, ClientPermissions permissions, String messageIdentifier,
                SimpleFieldSet parameters, Bucket data);
    }

    /**
     * Client plugins which connect to a FCP server plugin must implement this interface.<br/>
     * The purpose of this interface is to provide a message handling function for processing messages received from the server.
     * 
     * FIXME: Migrate JavaDoc of {@link FredPluginFCPServer} to this, then delete that interface.
     * 
     * @see FredPluginFCPMessageHandler The parent interface FredPluginFCPMessageHandler provides an overview.
     * @see ServerSideFCPMessageHandler The interface of the server plugin which handles the messages sent by the client.
     */
    public interface ClientSideFCPMessageHandler extends FredPluginFCPMessageHandler {
        /**
         * @param client The client which you used to send the original message.
         * @param messageIdentifier The identifier of the message which the server is replying to. The JavaDoc of the server-side message handler instructs it
         *                          to specify the messageIdentifier of replies to be the same as the messageIdentifier you specified in the message which
         *                          caused the reply to be sent. However the server is free to send messages to you on its own without any original message from
         *                          your side, for example for event propagation. In that case, the messageIdentifier might not match any previous message from
         *                          your side. 
         * @param parameters Part 1 of server reply: Human-readable parameters. Shall be small amount of data.
         * @param data Part 2 of server reply: Non-human readable, large size bulk data. Can be null.
         */
        void handleFCPPluginServerMessage(FCPPluginClient client, String messageIdentifier, SimpleFieldSet parameters, Bucket data);
    }
}