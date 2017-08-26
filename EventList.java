import java.util.*;

public class EventList {
	ArrayList<Event> list;

	public EventList() {
		this.list = new ArrayList<Event>();
	}

	public void add(Event event) {
		for(int i=0; i<this.list.size(); i++) {
			if(event.time() < this.list.get(i).time()) {
				this.list.add(i, event);
				return;
			}
		}
		this.list.add(event);
	}

	public void collision(EthernetFrame frame) {
		ArrayList<Event> copy = new ArrayList<Event>(this.list);
		for(int i=copy.size()-1; i>=0; i--) {
			if(copy.get(i).getFrame() == frame) {
				this.list.remove(i);
			}
		}
	}

	public Boolean complete() {
		return this.list.size() == 0;
	}

	public Event next() {
		Event event = this.list.get(0);
		this.list.remove(0);
		return event;
	}
}