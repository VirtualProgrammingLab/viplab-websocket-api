package de.uni_stuttgart.tik.ecs.ecc;

import java.util.List;

public class Details {

	public List<Object> receivers;
	public List<Object> senders;
	public String url;
	public String content_type;
	public Owner owner;

	public class Owner {
		public long pid;
		public boolean itsyou;
	}
}
