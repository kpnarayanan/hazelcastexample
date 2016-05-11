package io.vertx.spi.cluster.hazelcast.impl;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import io.vertx.core.net.impl.ServerID;

public class HazelcastServerID extends ServerID implements DataSerializable {

	public HazelcastServerID() {
	}

	public HazelcastServerID(ServerID serverID) {
		super(serverID.port, serverID.host);
	}

	@Override
	public void writeData(ObjectDataOutput dataOutput) throws IOException {
		dataOutput.writeInt(port);
		dataOutput.writeUTF(host);

	}

	@Override
	public void readData(ObjectDataInput dataInput) throws IOException {
		port = dataInput.readInt();
		host = dataInput.readUTF();
	}

	// We replace any ServerID instances with HazelcastServerID - this allows them to be serialized more optimally using
	// DataSerializable
	public static <V> V convertServerID(V val) {
		if (val.getClass() == ServerID.class) {
			ServerID sid = (ServerID)val;
			HazelcastServerID hsid = new HazelcastServerID(sid);
			return (V)hsid;
		} else {
			return val;
		}
	}

}
