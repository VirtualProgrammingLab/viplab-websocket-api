package de.uni_stuttgart.tik.ecs.ecc;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;

import de.uni_stuttgart.tik.ecs.ecc.connector.JsonWriter;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterProvider(value = JsonWriter.class)
public interface ECSMessageClient extends AutoCloseable {
	@GET
	@Path("/")
	public Response getMessages();
	
	@POST
	@Path("/")
	public Response createMessage(Object message, @HeaderParam("X-EcsReceiverMemberships") String receiver);
	
	@GET
	@Path("/{id}")
	public Response getMessage(@PathParam("id") String id);
	
	@DELETE
	@Path("/{id}")
	public Response deleteMessage(@PathParam("id") String id);
	
	@PUT
	@Path("/{id}")
	public Response updateMessage(@PathParam("id") String id);
	
	@GET
	@Path("/{id}/details")
	public Response getMessageDetails(@PathParam("id") String id);
	
	@GET
	@Path("/lifo")
	public Response getLastMessage();
	
	@GET
	@Path("/fifo")
	public Response getFirstMessage();
	
	@POST
	@Path("/lifo")
	public Response removeLastMessage();
	
	@POST
	@Path("/fifo")
	public Response removeFirstMessage();
	
}
